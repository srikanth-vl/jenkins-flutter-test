import 'dart:convert';
import '../../utils/media_type.dart';
import '../../utils/media_subtype.dart';
import '../dbSchemaConstants.dart';

class FormMediaTable {

  String _mediaAppId;
  String _mediaUserId;
  String _mediaUuid;
  int _formSubmissionTimestamp;
  String _mediaProjectId;
  String _mediaLocalPath;
  String _mediaBitmap;
  bool _mediaHasGeotag;
  double _mediaLatitude;
  double _mediaLongitude;
  double _mediaGpsAccuracy;
  int _mediaType;
  int _mediaSubtype;
  String _mediaExtension;
  int _mediaClickTs;
  int _mediaUploadTs;
  int _mediaAtionType;
  int _mediaUploadRetries;
  int _mediaRequestStatus;
  Map<String, String> _additionalProps;

  FormMediaTable(
      this._mediaAppId,
      this._mediaUserId,
      this._mediaUuid,
      this._formSubmissionTimestamp,
      this._mediaProjectId,
      this._mediaLocalPath,
      this._mediaBitmap,
      this._mediaHasGeotag,
      this._mediaLatitude,
      this._mediaLongitude,
      this._mediaGpsAccuracy,
      this._mediaType,
      this._mediaSubtype,
      this._mediaExtension,
      this._mediaClickTs,
      this._mediaUploadTs,
      this._mediaAtionType,
      this._mediaUploadRetries,
      this._mediaRequestStatus,
      this._additionalProps);

  // Convert a UserMetaEntry object into a Map object
  Map<String, dynamic> toMap() {
    var map = Map<String, dynamic>();
    map[FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID] = this._mediaAppId;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID] = this._mediaUserId;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_UUID] = this._mediaUuid;
    map[FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP] =
        this._formSubmissionTimestamp;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID] = this._mediaProjectId;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_LOCAL_PATH] = this._mediaLocalPath;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_BITMAP] = this._mediaBitmap;

    int value = 0;
    if(this._mediaHasGeotag){
      value = 1;
    }
    map[FormMediaEntry.COLUMN_FORM_MEDIA_HAS_GEOTAG] = value;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_LATITUDE] = this._mediaLatitude == null ? null : this._mediaLatitude.toString();
    map[FormMediaEntry.COLUMN_FORM_MEDIA_LONGITUDE] = this._mediaLongitude == null ? null : this._mediaLongitude.toString();
    map[FormMediaEntry.COLUMN_FORM_MEDIA_GPS_ACCURACY] = this._mediaGpsAccuracy == null ? null : this._mediaGpsAccuracy.toString();
    map[FormMediaEntry.COLUMN_FORM_MEDIA_TYPE] = this._mediaType;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_SUBTYPE] = this._mediaSubtype;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_EXTENSION] = this._mediaExtension;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_CLICK_TS] = this._mediaClickTs;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP] =
        this._mediaUploadTs;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_ACTION_TYPE] = this._mediaAtionType;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_RETRIES] =
        this._mediaUploadRetries;
    map[FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS] =
        this._mediaRequestStatus;
    map[FormMediaEntry.COLUMN_ADDITIONAL_PROPERTIES] = jsonEncode(this._additionalProps);
    return map;
  }

  // Extract a config object from a Map object
  FormMediaTable.fromMapObject(Map<String, dynamic> map) {
    this._mediaAppId = map[FormMediaEntry.COLUMN_FORM_MEDIA_APP_ID];
    this._mediaUserId = map[FormMediaEntry.COLUMN_FORM_MEDIA_USER_ID];
    this._mediaUuid = map[FormMediaEntry.COLUMN_FORM_MEDIA_UUID];
    this._formSubmissionTimestamp =
        map[FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP];
    this._mediaProjectId = map[FormMediaEntry.COLUMN_FORM_MEDIA_PROJECT_ID];
    this._mediaLocalPath = map[FormMediaEntry.COLUMN_FORM_MEDIA_LOCAL_PATH];
    this._mediaBitmap = map[FormMediaEntry.COLUMN_FORM_MEDIA_BITMAP];
    int value = map[FormMediaEntry.COLUMN_FORM_MEDIA_HAS_GEOTAG];
    if(value == 0){
      this.mediaHasGeotag = false;
    } else if(value == 1){
      this._mediaHasGeotag = true;
    }

    this._mediaLatitude = (map[FormMediaEntry.COLUMN_FORM_MEDIA_LATITUDE] != null &&
        map[FormMediaEntry.COLUMN_FORM_MEDIA_LATITUDE] != '') ? double.parse(map[FormMediaEntry.COLUMN_FORM_MEDIA_LATITUDE].toString()) : 0.0;

    this._mediaLatitude = (map[FormMediaEntry.COLUMN_FORM_MEDIA_LONGITUDE] != null &&
        map[FormMediaEntry.COLUMN_FORM_MEDIA_LONGITUDE] != '') ? double.parse(map[FormMediaEntry.COLUMN_FORM_MEDIA_LONGITUDE].toString()) : 0.0;

    this._mediaLatitude = (map[FormMediaEntry.COLUMN_FORM_MEDIA_GPS_ACCURACY] != null &&
        map[FormMediaEntry.COLUMN_FORM_MEDIA_GPS_ACCURACY] != '') ? double.parse(map[FormMediaEntry.COLUMN_FORM_MEDIA_GPS_ACCURACY].toString()) : 0.0;

    this._mediaType = map[FormMediaEntry.COLUMN_FORM_MEDIA_TYPE];
    this._mediaSubtype = map[FormMediaEntry.COLUMN_FORM_MEDIA_SUBTYPE];
    this._mediaExtension = map[FormMediaEntry.COLUMN_FORM_MEDIA_EXTENSION];
    this._mediaClickTs = map[FormMediaEntry.COLUMN_FORM_MEDIA_CLICK_TS];
    this._mediaUploadTs =
        map[FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_TIMESTAMP];
    this._mediaAtionType = map[FormMediaEntry.COLUMN_FORM_MEDIA_ACTION_TYPE];
    this._mediaUploadRetries =
        map[FormMediaEntry.COLUMN_FORM_MEDIA_UPLOAD_RETRIES];
    this._mediaRequestStatus =
        map[FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS];
    this._additionalProps = Map();

    if(map[FormMediaEntry.COLUMN_ADDITIONAL_PROPERTIES] != null) {
      jsonDecode(map[FormMediaEntry.COLUMN_ADDITIONAL_PROPERTIES]).forEach((key, value){
        this._additionalProps[key] = value;
      });
    }
  }

  Map<String, String> get additionalProps => _additionalProps;

  set additionalProps(Map<String, String> value) {
    _additionalProps = value;
  }

  int get mediaRequestStatus => _mediaRequestStatus;

  set mediaRequestStatus(int value) {
    _mediaRequestStatus = value;
  }

  int get mediaUploadRetries => _mediaUploadRetries;

  set mediaUploadRetries(int value) {
    _mediaUploadRetries = value;
  }

  int get mediaAtionType => _mediaAtionType;

  set mediaAtionType(int value) {
    _mediaAtionType = value;
  }

  int get mediaUploadTs => _mediaUploadTs;

  set mediaUploadTs(int value) {
    _mediaUploadTs = value;
  }

  int get mediaClickTs => _mediaClickTs;

  set mediaClickTs(int value) {
    _mediaClickTs = value;
  }

  String get mediaExtension => _mediaExtension;

  set mediaExtension(String value) {
    _mediaExtension = value;
  }

  int get mediaSubtype => _mediaSubtype;

  set mediaSubtype(int value) {
    _mediaSubtype = value;
  }

  int get mediaType => _mediaType;

  set mediaType(int value) {
    _mediaType = value;
  }

  double get mediaGpsAccuracy => _mediaGpsAccuracy;

  set mediaGpsAccuracy(double value) {
    _mediaGpsAccuracy = value;
  }

  double get mediaLongitude => _mediaLongitude;

  set mediaLongitude(double value) {
    _mediaLongitude = value;
  }

  double get mediaLatitude => _mediaLatitude;

  set mediaLatitude(double value) {
    _mediaLatitude = value;
  }

  bool get mediaHasGeotag => _mediaHasGeotag;

  set mediaHasGeotag(bool value) {
    _mediaHasGeotag = value;
  }

  String get mediaBitmap => _mediaBitmap;

  set mediaBitmap(String value) {
    _mediaBitmap = value;
  }

  String get mediaLocalPath => _mediaLocalPath;

  set mediaLocalPath(String value) {
    _mediaLocalPath = value;
  }

  String get mediaProjectId => _mediaProjectId;

  set mediaProjectId(String value) {
    _mediaProjectId = value;
  }

  int get formSubmissionTimestamp => _formSubmissionTimestamp;

  set formSubmissionTimestamp(int value) {
    _formSubmissionTimestamp = value;
  }

  String get mediaUuid => _mediaUuid;

  set mediaUuid(String value) {
    _mediaUuid = value;
  }

  String get mediaUserId => _mediaUserId;

  set mediaUserId(String value) {
    _mediaUserId = value;
  }

  String get mediaAppId => _mediaAppId;

  set mediaAppId(String value) {
    _mediaAppId = value;
  }

  String getMediaSubTypeName(int mediaSubType){
    String mediaSubTypeName = "";

    if (mediaSubType == MediaSubTypeHelper.getValue(MediaSubType.FULL))
      mediaSubTypeName = "full";

    else if (mediaSubType == MediaSubTypeHelper.getValue(MediaSubType.PREVIEW))
      mediaSubTypeName = "preview";

    else if (mediaSubType == MediaSubTypeHelper.getValue(MediaSubType.THUMBNAIL))
      mediaSubTypeName = "thumbnail";

    return mediaSubTypeName;
  }

   String getMediaTypeName(int type) {
    String mediaType = "";

    if (type == MediaTypeHelper.getValue(MediaType.IMAGE))
      mediaType = "image";
     else if (type == MediaTypeHelper.getValue(MediaType.VIDEO))
      mediaType = "video";
     else if (type == MediaTypeHelper.getValue(MediaType.AUDIO))
      mediaType = "audio";
    else if (type == MediaTypeHelper.getValue(MediaType.BLOB))
      mediaType = "blob";
    else if (type == MediaTypeHelper.getValue(MediaType.TEXT))
      mediaType = "text";
    else if (type == MediaTypeHelper.getValue(MediaType.PDF))
      mediaType = "pdf";
    else if (type == MediaTypeHelper.getValue(MediaType.OTHERS))
      mediaType = "others";
    return mediaType;
  }
}
