import 'dart:convert';
import '../event/event_utils.dart';
import '../utils/common_utils.dart';
import 'package:path/path.dart';
import '../db/databaseHelper.dart';
import '../db/models/form_media_table.dart';
import '../resources/media_request_listener.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import '../utils/media_subtype.dart';
import '../utils/media_type.dart';
import 'dart:io';
import '../utils/media_upload_status.dart';
import 'uniapp_response.dart';
import 'package:http/http.dart' as http;
import 'package:image/image.dart' as Img;
import 'package:http_parser/http_parser.dart' as httpParser;


class MediaSyncService implements MediaRequestListener {

  FormMediaTable formMedia;
  int maxRetries;
  DatabaseHelper dbHelper;

  MediaSyncService(FormMediaTable formMediaTable, int maxRetries){
    this.formMedia = formMediaTable;
    this.maxRetries = maxRetries;
    this.dbHelper = UAAppContext.getInstance().unifiedAppDBHelper;
  }

  @override
  void onRequestFailed() async {

    int retries = formMedia.mediaUploadRetries;
    int req_status = MediaUploadStatusHelper.getValue(MediaUploadStatus.PENDING);
    retries++;

    if(formMedia.mediaRequestStatus == MediaUploadStatusHelper.getValue(MediaUploadStatus.PENDING)
        && retries == maxRetries) {
      req_status = MediaUploadStatusHelper.getValue(MediaUploadStatus.FAILED);
    }

    updateFormMedia(req_status, retries, CommonConstants.UPLOAD_TIMESTAMP_DEFAULT
        , MediaSubTypeHelper.getValue(MediaSubType.FULL));

    Map<String, dynamic> values = formMedia.toMap();
    await dbHelper.updateFormMedia(values, formMedia.mediaAppId, formMedia.mediaUserId, formMedia.mediaUuid);
  }

  @override
  void onRequestSuccessful() async {

    print("MEDIA SYNC SUCCESSFUL : " + formMedia.mediaUserId);

    File imgFile = new File(formMedia.mediaLocalPath);
    String fileNme = basename(imgFile.path);
    String fileName = fileNme.substring(0, fileNme.indexOf("."));

    if(formMedia.mediaType == MediaTypeHelper.getValue(MediaType.IMAGE) && formMedia.mediaUuid.compareTo(fileName) == 0) {
      resizeImage(formMedia.mediaLocalPath);
    }
    int retries = formMedia.mediaUploadRetries;

    if(formMedia.mediaRequestStatus == MediaUploadStatusHelper.getValue(MediaUploadStatus.PENDING)) {
      retries++;
    }

    updateFormMedia(MediaUploadStatusHelper.getValue(MediaUploadStatus.SYNCED),
        retries, new DateTime.now().millisecondsSinceEpoch , MediaSubTypeHelper.getValue(MediaSubType.PREVIEW));

    Map<String, dynamic> values = formMedia.toMap();
    await dbHelper.updateFormMedia(values, formMedia.mediaAppId, formMedia.mediaUserId, formMedia.mediaUuid);

  }

  void updateFormMedia(int uploadStatus, int retries, int uploadTimeStamp, int mediaSubtype){
    formMedia.mediaRequestStatus = uploadStatus;
    formMedia.mediaUploadRetries = retries;
    formMedia.mediaSubtype = mediaSubtype;
  }

  void resizeImage(String localPath){
    File imageFile = new File(localPath);
    Img.Image imageTemp = Img.decodeImage(imageFile.readAsBytesSync());
    Img.Image resizedImage = Img.copyResize(imageTemp, width: 816, height: 612);
    // Save the thumbnail as a JPG.
    new File(localPath).writeAsBytesSync(Img.encodeJpg(resizedImage));
  }

  void uploadMediaToServerUsingHTTP() async {

    httpParser.MediaType MEDIA_TYPE = null;

    String lat = CommonConstants.DEFAULT_LATITUDE.toString();
    String lon = CommonConstants.DEFAULT_LONGITUDE.toString();
    String accuracy = CommonConstants.DEFAULT_ACCURACY.toString();
    if(formMedia.mediaHasGeotag){
      lat = formMedia.mediaLatitude.toString();
      lon = formMedia.mediaLongitude.toString();
      accuracy = formMedia.mediaGpsAccuracy.toString();
    }

    if(MediaTypeHelper.getValue(MediaType.IMAGE) == formMedia.mediaType){
      MEDIA_TYPE = new httpParser.MediaType("image", formMedia.mediaExtension); // iF ERROR:: HANDLE
    } else if(MediaTypeHelper.getValue(MediaType.VIDEO) == formMedia.mediaType){
      MEDIA_TYPE = new httpParser.MediaType("video", formMedia.mediaExtension);
    }

    var request = new http.MultipartRequest("POST", Uri.parse(CommonConstants.BASE_URL
        + CommonConstants.UPLOAD_SERVICE_URL));

    request.fields['superapp'] = CommonConstants.SUPER_APP_ID;
    request.fields['appid'] = formMedia.mediaAppId;
    request.fields['projectid'] = formMedia.mediaProjectId;
    request.fields['imageid'] = formMedia.mediaUuid;
    request.fields['token'] = UAAppContext.getInstance().token;
    request.fields['userid'] = formMedia.mediaUserId;
    request.fields['syncts'] = new DateTime.now().millisecondsSinceEpoch.toString();
    request.fields['insert_ts'] = formMedia.formSubmissionTimestamp.toString();
    request.fields['lat'] = lat;
    request.fields['lon'] = lon;
    request.fields['key'] = "";
    request.fields['gps_accuracy'] = accuracy;
    request.fields['mediatype'] = formMedia.getMediaTypeName(formMedia.mediaType);
    request.fields['media_subtype'] = formMedia.getMediaSubTypeName(formMedia.mediaSubtype);
    request.fields['media_ext'] = formMedia.mediaExtension;
    if(formMedia.additionalProps != null && formMedia.additionalProps.isNotEmpty){
    request.fields['additional_props'] = jsonEncode(formMedia.additionalProps);}

    http.MultipartFile multipartFile = await http.MultipartFile.fromPath(
        'media', formMedia.mediaLocalPath, filename: formMedia.mediaUuid + "." + formMedia.mediaExtension, contentType: MEDIA_TYPE); //returns a Future<MultipartFile>

    request.files.add(multipartFile);

    UniappResponse uniappResponse;

    final response = await request.send();
    final respStr = await response.stream.bytesToString();

      if (response.statusCode == 200) {

        uniappResponse = UniappResponse.fromJson(jsonDecode(respStr));
        if(uniappResponse ==  null || uniappResponse.result ==  null ) {
          //todo throw exception log it
        }
        int code = uniappResponse.result.status;
        if (code == 200) {

          await onRequestSuccessful();
        } else if (code == 350) {
          // todo token expired broadcast logout event
          // todo stop backround sync service
          //todo update sharedpreferences
          await onRequestFailed();
          CommonUtils.handleTokenExpiry(UAAppContext.getInstance().context);
        }
      }
    eventBus.fire(PostSubmissionEvent());
    await Future.delayed(Duration(seconds: 1));
  }
}