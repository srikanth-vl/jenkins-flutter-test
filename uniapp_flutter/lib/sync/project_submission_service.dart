import 'dart:convert';
import 'dart:io';
import '../event/event_utils.dart';
import 'package:http/http.dart' as http;
import 'package:synchronized/synchronized.dart';

import '../models/project_submission_result.dart';
import '../models/submission_fields.dart';
import '../resources/uniapp_response.dart';
import '../utils/common_constants.dart';
import '../utils/project_submission_constants.dart';
import '../utils/project_submission_upload_status.dart';
import '../utils/media_upload_status.dart';
import '../utils/network_utils.dart';
import '../db/databaseHelper.dart';
import '../db/models/user_meta_data_table.dart';
import '../db/dbSchemaConstants.dart';
import '../db/models/project_submission.dart';
import '../ua_app_context.dart';
import '../log/uniapp_logger.dart';
import '../sync/form_text_data_sync_service.dart';

class ProjectSubmissionService {
  DatabaseHelper databaseHelper =  DatabaseHelper();
  Logger logger =  getLogger("ProjectSubmissionService");
  ProjectSubmissionService() {
    databaseHelper = UAAppContext.getInstance().unifiedAppDBHelper;
  }
  Future<ProjectSubmissionResult> uploadToServer(ProjectSubmission projectSubmission) async {
    UserMetaDataTable userMetaDataTable =  await databaseHelper.getUserMeta(projectSubmission.userId);
    var requestParam = createRequestParams(projectSubmission, userMetaDataTable);
    if (requestParam == null ) {
      //TODO throw exception log it
      throw Exception('Failed to load post');
    }
    ProjectSubmissionResult result = null;
    try {
      print(CommonConstants.BASE_URL + projectSubmission.submissionApi);
      final response = await http.post(
          CommonConstants.BASE_URL + projectSubmission.submissionApi,
          headers: {"Content-Type": "application/json"}, body: requestParam);
      if (response == null || response.statusCode != 200 ||
          response.body == null) {
        //TODO throw exception log it
        print('Failed to load post');
      }

      UniappResponse uniappResponse = UniappResponse.fromJson(jsonDecode(response.body));

      if (uniappResponse == null || uniappResponse.result == null) {
        //TODO throw exception log it
      }
      result = ProjectSubmissionResult(
          uniappResponse.result.status, uniappResponse.result.success,
          uniappResponse.result.message);
    } on Error catch (e) {
      result = new ProjectSubmissionResult(
          CommonConstants.DEFAULT_APP_ERROR_CODE,
          false,
          ProjectSubmissionConstants.PROJECT_TO_SUBMIT_IN_BACKGROUND_SYNC);
      logger.e("Error occured while submission was" +
          " in progress -- stopping sync");
    } on SocketException catch(e){
      result = new ProjectSubmissionResult(
          CommonConstants.DEFAULT_APP_ERROR_CODE,
          false,
          ProjectSubmissionConstants.PROJECT_TO_SUBMIT_IN_BACKGROUND_SYNC);
      logger.e('SocketException :: Could not connect to server');
    }
    return result;
  }

  Future<int> updateSyncStatus(ProjectSubmission projectSubmission, ProjectSubmissionUploadStatus status, ProjectSubmissionResult result
      , int serverSyncTimestamp) async {
    String serverResponse = null;

    // TODO Log JSON PARSING EXCEPTION

    serverResponse = jsonEncode(result.toJson());
    int id = await UAAppContext.getInstance().unifiedAppDBHelper.updateProjectSubmission(projectSubmission.userId, projectSubmission.appId
        , projectSubmission.projectId, serverSyncTimestamp, projectSubmission.timeStamp, status, serverResponse, projectSubmission.updateRetryCount);
    // TODO LOG UPDATE SUCCESS MESSAGE

    eventBus.fire(PostSubmissionEvent());
    await Future.delayed(Duration(seconds: 1));
    return id;
  }

  String createRequestParams(ProjectSubmission projectSubmission, UserMetaDataTable userMetaData)  {
    Map requestObject =  Map();
    List submissionArray = List();
    Map<String, String> params = Map();
    Map projectSubmissionObject = new Map();
    projectSubmissionObject[CommonConstants.PROJECT_SUBMIT_FORM_ID_KEY] = projectSubmission.formId;
    projectSubmissionObject[CommonConstants.PROJECT_SUBMIT_MD_INSTANCE_ID_KEY] =projectSubmission.mdInstanceId;
    projectSubmissionObject[CommonConstants.PROJECT_SUBMIT_PROJECT_ID_KEY] =projectSubmission.projectId;
    projectSubmissionObject[CommonConstants.PROJECT_SUBMIT_USER_TYPE_KEY] =projectSubmission.userType;
    projectSubmissionObject[CommonConstants.PROJECT_SUBMIT_INSERT_TS_KEY] =projectSubmission.timeStamp;
    Map <String, String > additionalProperties= projectSubmission.additionalProperties;
    // get token from additional properties if available else form user_meta table;
    String token  = null;
    if(additionalProperties != null && additionalProperties[CommonConstants.USER_TOKEN] != null && additionalProperties[CommonConstants.USER_TOKEN].isNotEmpty) {
      token = additionalProperties[CommonConstants.USER_TOKEN];
      additionalProperties.remove(CommonConstants.USER_TOKEN);
    } else {
      if(userMetaData != null) {
        token = userMetaData.token;
      }
    }
    Map<String, String> additionPropertiesJson = new Map();
    for (String key in additionalProperties.keys) {
      additionPropertiesJson[key] = additionalProperties[key];
    }
    projectSubmissionObject[CommonConstants.PROJECT_ADDITIONAL_PROPERTIES] =  additionPropertiesJson;
    List<SubmissionField> fields =  projectSubmission.submissionObject;
    List<Map<String, dynamic>> jsonArray = projectSubmission.submissionObject.map((v) =>
        v.toJson()).toList();
//    String submission = jsonEncode(fieldlist);
//  List jsonArray = jsonObject.getJSONArray(CommonConstants.PROJECT_SUBMIT_FIELDS_KEY);
    projectSubmissionObject[CommonConstants.PROJECT_SUBMIT_FIELDS_KEY] =jsonArray;
    submissionArray.add(projectSubmissionObject);
    requestObject[CommonConstants.PROJECT_SUBMIT_SUBMIT_DATA_KEY] = submissionArray;
    requestObject[CommonConstants.PROJECT_SUBMIT_SUPER_APP_KEY] = CommonConstants.SUPER_APP_ID;
    requestObject[CommonConstants.PROJECT_SUBMIT_USER_ID_KEY] = projectSubmission.userId;
    requestObject[CommonConstants.PROJECT_SUBMIT_TOKEN_KEY] = token;
    requestObject[CommonConstants.PROJECT_SUBMIT_APP_ID_KEY] = projectSubmission.appId;
    params[CommonConstants.PROJECT_SUBMISSION_PARAMETER_KEY] = jsonEncode(requestObject).toString();
    var body = json.encode(params);
    return body;
  }

  void updateMediaStatusToFailed(ProjectSubmission submissionData) async{
    int formSubmissionTimestamp = submissionData.timeStamp;
    String projectId = submissionData.projectId;
    Map contentValues = new Map();
    contentValues[FormMediaEntry.COLUMN_FORM_MEDIA_REQUEST_STATUS] = MediaUploadStatusHelper.getValue(MediaUploadStatus.FAILED);
    await databaseHelper.updateFormMediaForProjectAtGivenTimestamp(contentValues, projectId, formSubmissionTimestamp);

  }


  Future<bool> callProjectSubmitService(ProjectSubmission projectSubmission) async {

    // Logic
    // 1. Upload to server
    // 2. Update Status in DB
    // 3. In case of error - Update Status in DB with error details
    int projectStatus = await UAAppContext.getInstance().unifiedAppDBHelper.getProjectSubmissionStatus(projectSubmission.appId, projectSubmission.userId, projectSubmission.projectId, projectSubmission.timeStamp);

    ProjectSubmissionResult result ;
    if (!(projectStatus == ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.UNSYNCED)
        || projectStatus == ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.SERVER_ERROR))) {
      return true;
    }

    result = await uploadToServer(projectSubmission);
    ProjectSubmissionUploadStatus uploadStatus;
    if(result.isSuccessful) {
      uploadStatus = ProjectSubmissionUploadStatus.SYNCED;
    } else {
      if (result.statusCode == ProjectSubmissionConstants.SUBMISSION_VALIDATION_ERROR) {
        uploadStatus = ProjectSubmissionUploadStatus.VALIDATION_ERROR;
        updateMediaStatusToFailed(projectSubmission);
      } else if (result.statusCode == ProjectSubmissionConstants.APP_VERSION_MISMATCH_ERROR) {
        uploadStatus = ProjectSubmissionUploadStatus.APP_VERSION_MISMATCH_ERROR;
        updateMediaStatusToFailed(projectSubmission);
      } else if(result.statusCode == ProjectSubmissionConstants.PROJECT_DELETED_ERROR) {
        uploadStatus = ProjectSubmissionUploadStatus.DELETED;
        updateMediaStatusToFailed(projectSubmission);
      } else {
        uploadStatus = ProjectSubmissionUploadStatus.SERVER_ERROR;
      }
    }
    updateSyncStatus(projectSubmission, uploadStatus, result, new DateTime.now().millisecondsSinceEpoch);
    //TODO
//      Utils.logDebug(LogTags.PROJECT_SUBMIT, "Project Submission  : Uploaded successfully -- " + result);
    return true;

  }


  Future<ProjectSubmissionResult> uploadProjectInRealTime(ProjectSubmission projectSubmission) async{
    // Check if connection available, if not return
    //    the project will be submitted in background sync
    //
    // Get AppBackgroundSync.getSyncLock()
    // Lock on SYNC_LOCK
    // Check projectSubmission status in DB
    //     if uploaded then return
    //    else upload now
    // Release SYNC_LOCK Object

    // TODO Use Utils Method Written by Arjun
     bool isOnline = await NetworkUtils().hasActiveInternet();
    if (!isOnline) {
      // Is not online
      return new ProjectSubmissionResult(CommonConstants.DEFAULT_APP_ERROR_CODE, false,
          ProjectSubmissionConstants.PROJECT_TO_SUBMIT_IN_BACKGROUND_SYNC);
    }

    //TODO Print log
   logger.i('XXXX: In RealTime upload : Waiting for Lock : ${projectSubmission.projectId}');

    Lock SYNC_LOCK = TextDataBackgroundSyncService.getSyncLock();
   return await  SYNC_LOCK.synchronized (() async {
    //TODO Print log
     logger.i("${LogTags.PROJECT_LIST_SYNC} :: XXXX: In RealTime upload : After getting Lock : ${ projectSubmission.projectId}");

    print("PROJECT SUBMISSION INFO : " + projectSubmission.appId + "  "
        + projectSubmission.userId + "  " + projectSubmission.projectId);
    int projectStatus = await databaseHelper.getProjectSubmissionStatus(
          projectSubmission.appId, projectSubmission.userId,
          projectSubmission.projectId, projectSubmission.timeStamp);
      String message;
      bool isSuccess;
      if (projectStatus != ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.UNSYNCED)) {
        if(projectStatus == ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.SYNCED)
            || projectStatus == ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.SYNCED_WITH_MEDIA)) {
          isSuccess = true;
          message = ProjectSubmissionConstants.PROJECT_SUBMITTED_SUCCESSFULLY;
        } else if(projectStatus == ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.VALIDATION_ERROR)) {
          isSuccess = false;
          message = ProjectSubmissionConstants.PROJECT_SUBMISSION_VALIDATION_ERROR;
        } else {
          isSuccess = false;
          message = ProjectSubmissionConstants.PROJECT_SUBMISSION_FAILED;
        }
        return new ProjectSubmissionResult(CommonConstants.DEFAULT_APP_ERROR_CODE, isSuccess, message);
      }

      ProjectSubmissionResult result = null;
//      try {
        result = await uploadToServer(projectSubmission);
        ProjectSubmissionUploadStatus uploadStatus;
        if(result.isSuccessful) {
          uploadStatus = ProjectSubmissionUploadStatus.SYNCED;
        } else {
          if (result.statusCode == ProjectSubmissionConstants.SUBMISSION_VALIDATION_ERROR) {
            uploadStatus = ProjectSubmissionUploadStatus.VALIDATION_ERROR;
            updateMediaStatusToFailed(projectSubmission);
          } else if (result.statusCode == ProjectSubmissionConstants.APP_VERSION_MISMATCH_ERROR) {
            uploadStatus = ProjectSubmissionUploadStatus.APP_VERSION_MISMATCH_ERROR;
            updateMediaStatusToFailed(projectSubmission);
          } else if(result.statusCode == ProjectSubmissionConstants.PROJECT_DELETED_ERROR) {
            uploadStatus = ProjectSubmissionUploadStatus.DELETED;
            updateMediaStatusToFailed(projectSubmission);
          } else {
            uploadStatus = ProjectSubmissionUploadStatus.FAILED;
          }
        }
        updateSyncStatus(projectSubmission, uploadStatus, result, new DateTime.now().millisecondsSinceEpoch);

        //TODO Print logs using utils
       logger.i('${LogTags.PROJECT_LIST_SYNC} :: XXXX: In RealTime upload : After submitting project :  ${projectSubmission.projectId}');
       logger.i('${LogTags.PROJECT_SUBMIT} :: Project Submission  : Uploaded successfully --  ${result.toJson()}');
        return result;

    });
  }
  }

