import 'dart:async';
import 'dart:core';
import '../event/event_utils.dart';
import 'package:http/http.dart' as http;
import '../db/models/config_table.dart';
import '../db/models/project_submission.dart';
import '../models/project.dart';
import '../models/root_config.dart';
import '../utils/media_upload_status.dart';
import '../utils/project_submission_upload_status.dart';
import 'dart:convert';
import '../db/databaseHelper.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import '../models/project_type_configuartion.dart';
import '../models/sub_app.dart';
import 'uniapp_response.dart';
import '../models/project_list_configuartion.dart';
import '../db/models/project_master_data_table.dart';
import '../log/uniapp_logger.dart';
import '../error/all_custom_exception.dart';
class ProjectListService {
  DatabaseHelper _databaseHelper ;
  Logger logger = getLogger("ProjectListService");
  ProjectListService() {
    _databaseHelper =  DatabaseHelper();
  }

  Future<ProjectListConfiguration> callProjectListService(ProjectTypeConfiguration projectTypeConfiguration, List<String> projectIds) async {

    // Logic
    // 1. Fetch from Server
    // 2. Save to DB
    // 3. Return the ProjectList
    // 4. In case of error - return the error type

    String content = await fetchFromServer(projectTypeConfiguration, projectIds);
    if (content == null || content == '' || content == '{}') {
      return null;
    }
    ProjectListConfiguration projectList = null;
    projectList = ProjectListConfiguration.fromJson(jsonDecode(content));
    await storeToDB(projectList, projectTypeConfiguration);
    return projectList;
  }
  Future<String> fetchFromServer(ProjectTypeConfiguration projectTypeConfiguration, List<String> projectIds)async{
    var requestParam = createRequestParams(projectTypeConfiguration, projectIds);
    if (requestParam == null ) {
      // throw exception log it
      logger.e("Error creating request parameters - ProjectListConfiguration Config");
      throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error creating request parameters - ProjectListConfiguration Config");
    }
    final response = await http.post(CommonConstants.BASE_URL + "projectlist",headers:{"Content-Type": "application/json"},body: requestParam);
    if (response == null || response.statusCode != 200 || response.body ==  null) {
      // throw exception log it
      logger.e('Error getting ProjectList from server : received responseString : ${response}' );
      throw ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
          , 'Error getting ProjectList from server : received responseString : ${response}' );
    }
    UniappResponse uniappResponse = UniappResponse.fromJson(jsonDecode(response.body));
    if(uniappResponse ==  null || uniappResponse.result ==  null ) {
      // throw exception log it
      logger.e("Error getting ProjectListConfiguration from server , uniapp response ::${uniappResponse == null ? uniappResponse : uniappResponse.toJson()}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting ProjectListConfiguration from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
    }
    int code = uniappResponse.result.status;
    String content ;
    if (code == 200) {
      content = uniappResponse.result.content;
      if (content == "{}") {
        // Same version on the server
        return '{}';
      } else {
        return content.toString();
      }
    } else if (code == 350) {
      // token expired broadcast logout event
      eventBus.fire(TokenExpiredEvent());
      // stop background sync service
      throw new TokenExpiredException(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Tokn expired");
      //todo update sharedpreferences

    } else {
      logger.e('Error getting ProjectListConfiguration from server (invalid data) : uniapp response code : ${uniappResponse.result.status}');
      throw ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
          , 'Error getting ProjectListConfiguration from server (invalid data) : uniapp response code : ${uniappResponse.result.status}');

    }
  }
  void storeToDB(ProjectListConfiguration projectList, ProjectTypeConfiguration projectTypeConfiguration) async {
    // Add new ProjectList to the DB
    String appId = projectTypeConfiguration.projecttype;
    String userId =  projectTypeConfiguration.userId;
    // get rootConfig  for user
    ConfigFile rootConfigFile = await _databaseHelper.getConfig(projectTypeConfiguration.userId, CommonConstants.ROOT_CONFIG_NAME);
    String rootConfigString = rootConfigFile != null && rootConfigFile.content != null ? rootConfigFile.content : null;
    if (rootConfigString != null && rootConfigString.isNotEmpty) {
      RootConfig rootConfig = null;
      rootConfig = RootConfig.fromJson(jsonDecode(rootConfigString));
      List<String> filteringAttributes = List();
      List<String> groupingAttributes =  List();
      if (rootConfig != null && rootConfig.config != null && rootConfig.config.length !=  0) {
        for (SubApp projectTypeModel in rootConfig.config) {
          if (projectTypeModel.appId ==appId) {
            if (projectTypeModel.filteringAttributes != null && projectTypeModel.filteringAttributes.length > 0) {
              filteringAttributes.addAll(projectTypeModel.filteringAttributes);
            }
            if (projectTypeModel.groupingAttributes != null && projectTypeModel.groupingAttributes.length > 0) {
              groupingAttributes.addAll(projectTypeModel.groupingAttributes);
            }
            break;
          }
        }
        //  Iterate through all the project in list and store in DB
        for (Project project in projectList.projects) {
          if (project != null) {
            await writeProjectToDb(appId, userId, project, projectList.currenttime, projectList.showmap, filteringAttributes, groupingAttributes);
          }
        }
      }
    } else {
      logger.e('Rootconfig not found');
    }
    return;
  }

  Future<void> writeProjectToDb(String appId, String userId, Project project, int lastServerTs,
      bool shouldShowMap, List<String> filteringAttributes, List<String> groupingAttributes) async {
    String projectValidationString = project.validations == null ? '' : jsonEncode(project.validations.toJson());
    String groupingDimensionValues = getHashSeparatedValues(groupingAttributes, project.attributes);
    ProjectMasterDataTable projectMasterData = new ProjectMasterDataTable(appId,
        userId, project.projectid, project.projectname, project.lat, project.long,
        project.bBoxValidation, project.centroidValidation, project.lastSubDate != null ? project.lastSubDate.toString() : "", project.state, project.extProjId, project.fields, project.userType,
        projectValidationString , project.projectIconInfo, lastServerTs, shouldShowMap ? 1: 0, project.attributes, groupingDimensionValues,project.lastSyncTs == null ? 0 :int.parse(project.lastSyncTs), 1);
    await _databaseHelper.insertProjectMasterData(projectMasterData);
  }

  String getHashSeparatedValues(List<String> attributes, Map<String, String> projectListAttributesMap) {
    String dimensionValues = "";
    if(attributes != null && attributes.isNotEmpty) {
      for(String attribute in attributes) {
        String attributeValue = '';
        String value = projectListAttributesMap[attribute] ;
        if(value == null || value == '' || value == 'null') {
          attributeValue = dimensionValues.length == 0 ? '' : '#';
        } else {
          attributeValue = dimensionValues.length == 0 ? value : '#'+value;
        }
        dimensionValues = dimensionValues+attributeValue;
      }
    }
    return dimensionValues;
  }

  String createRequestParams(ProjectTypeConfiguration projectTypeConfiguration, List<String> projectIds) {
    Map jsonObject = new Map();
    jsonObject[CommonConstants.PROJECT_LIST_CONFIG_USER_ID_KEY] = UAAppContext.getInstance().userID;
    jsonObject[CommonConstants.PROJECT_LIST_CONFIG_TOKEN_KEY] = UAAppContext.getInstance().token;
    jsonObject[CommonConstants.PROJECT_LIST_CONFIG_SUPER_APP_KEY] = CommonConstants.SUPER_APP_ID;
    jsonObject[CommonConstants.PROJECT_LIST_CONFIG_APP_ID_KEY] = projectTypeConfiguration.projecttype;

    // Fetching MetaDataInstanceId
    List mdInstanceIds = new List();
    if(projectTypeConfiguration == null || projectTypeConfiguration.content == null || projectTypeConfiguration.content.isEmpty) {
      return null;
    }
    projectTypeConfiguration.content.forEach((projectId, projectForm){
      if(projectForm != null && projectForm.isNotEmpty) {
        projectForm.forEach((action, formInfo) {
          mdInstanceIds.add(formInfo.mdinstanceid);
        });
      }
    });
    if (projectIds != null && projectIds.isNotEmpty) {

      List projectIdsToSend = new List();
      int i= 0;
      for (String projectId in projectIds) {
        i++;
        projectIdsToSend.add(projectId);
        if(i == 4000) {
          break;
        }
      }
      jsonObject[CommonConstants.PROJECT_LIST_CONFIF_PROJECT_ID_LIST_KEY] = projectIdsToSend;
    }

    jsonObject[CommonConstants.PROJECT_LIST_CONFIG_MD_INSTANCE_ID_KEY] = mdInstanceIds;
    return jsonEncode(jsonObject);
  }
  void handleProjectSync(String appId, ProjectTypeConfiguration ptc) async {

    String userId = UAAppContext.getInstance().userID;

    //Stage 1 Uploads newer version of the project to the server
//        bool synced = uploadUnSyncedProjects(userId, appId);
//        if(!synced) {
//            Utils.logError(LogTags.PROJECT_LIST_SYNC, "Error uploading Projects to server for appId: " +appId + " , userId:" + userId);
//            return;
//        }

    Map<String, int> pidToTSMap = await fetchFromServerProjectIDToTSMap(ptc);
    Map<String, int> existingAssignedProjectIdToTs = await _databaseHelper.getProjectIdToLastSyncTsMap(userId, appId, true);
    Map<String, int> existingAllProjectIdToTs = await _databaseHelper.getProjectIdToLastSyncTsMap(userId, appId, false  );

    // Stage 2 - Identify projects to sync based on timestamp from server
    if(pidToTSMap == null) {
      return;
    }
    List<String> projectsTODOwnload = filterProjectsTODOwnload(existingAssignedProjectIdToTs, pidToTSMap);
    List<String> projectsToDelete = filterProjectsToDelete(userId, appId, existingAllProjectIdToTs, pidToTSMap);
    // Stage 3 - Sync projects with server
    // 1. Downloads newer version of the project from the server
    // 2. Deletes unassigned projects for the user

    bool downloaded = await downloadUpdatedProjects(ptc, projectsTODOwnload);
    if(!downloaded)  {
      logger.e("Error downloading Projects from server for appId: " +appId + " , userId:" + userId);
      return;
    };

    bool deleted = await deleteUnassignedProjects(userId, appId, projectsToDelete);
    if(!deleted) {
      logger.e("Error deleting Projects from server for appId: " +appId + " , userId:" + userId);
      return;
    };

  }
  Future<Map<String, int>> fetchFromServerProjectIDToTSMap(ProjectTypeConfiguration projectTypeConfiguration) async{
    var requestParam = createRequestParams(projectTypeConfiguration, null);
    if (requestParam == null ) {
      // throw exception log it
      logger.e("Error creating request parameters - ProjectListConfiguration Config");
      throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error creating request parameters - ProjectListConfiguration Config");
    }
    final response = await http.post(CommonConstants.BASE_URL + "projectlistwithts",headers:{"Content-Type": "application/json"},body: requestParam);
    if (response == null || response.statusCode != 200 || response.body ==  null) {
      // throw exception log it
      logger.e('Error getting ProjectList from server : received responseString : ${response}' );
      throw ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
          , 'Error getting ProjectList from server : received responseString : ${response}' );
    }
    UniappResponse uniappResponse = UniappResponse.fromJson(jsonDecode(response.body));
    if(uniappResponse ==  null || uniappResponse.result ==  null ) {
      // throw exception log it
      logger.e("Error getting ProjectListConfiguration from server , uniapp response ::${uniappResponse == null ? uniappResponse : uniappResponse.toJson()}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting ProjectListConfiguration from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");

    }
    int code = uniappResponse.result.status;
    String content ;
    if (code == 200) {
      content = uniappResponse.result.content;
      if (content == "{}") {
        return null;
      } else {
        Map<String, dynamic> contentMap= jsonDecode(content);
        Map<String, int> projectIdToTimestamp = new Map();

        if(contentMap['projects'] != null) {
          contentMap['projects'].forEach((projectId, ts) {
            projectIdToTimestamp[projectId] = ts;
          });

        }return projectIdToTimestamp;

      }
    } else if (code == 350) {

      // token expired broadcast logout event
      eventBus.fire(TokenExpiredEvent());
      // stop background sync service
      throw new TokenExpiredException(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Tokn expired");
      //todo update sharedpreferences

    }else {
      logger.e('Error getting ProjectListConfiguration from server (invalid data) : uniapp response code : ${uniappResponse.result.status}');
      throw ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
          , 'Error getting ProjectListConfiguration from server (invalid data) : uniapp response code : ${uniappResponse.result.status}');
    }
  }
  List<String> filterProjectsTODOwnload(Map<String, int> existingProjectIdToTs, Map<String, int> serverProjectIdToTs) {

    List<String> projectsToFetch = new List();
    for (String projectId in serverProjectIdToTs.keys) {
      if (!existingProjectIdToTs.containsKey(projectId) || existingProjectIdToTs[projectId] < serverProjectIdToTs[projectId]) {
        projectsToFetch.add(projectId);
      }
    }
    return projectsToFetch;
  }


  List<String> filterProjectsToDelete(String userId, String appId
      , Map<String, int> existingProjectIdToTs, Map<String, int> serverProjectIdToTs) {

    List<String> projectsToDelete = new List();
    for (String projectId in existingProjectIdToTs.keys) {
      if(!serverProjectIdToTs.containsKey(projectId)) {
        projectsToDelete.add(projectId);
      }
    }
    return projectsToDelete;
  }


  Future setProjectStatusAsFailed(ProjectSubmission submissionData) async {
    await _databaseHelper.updateProjectSubmissionStatus(submissionData.userId, submissionData.appId, submissionData.projectId, submissionData.timeStamp, ProjectSubmissionUploadStatus.FAILED);
  }

  Future<bool> downloadUpdatedProjects(ProjectTypeConfiguration ptc, List<String> projectIDs)  async{

    if (projectIDs.isNotEmpty) {
      await callProjectListService(ptc, projectIDs);
    }
    logger.d("Successfully downloaded projects from server for appId: " +ptc.projecttype + " , userId:" + ptc.userId);
    return true;
  }

  Future<bool> deleteUnassignedProjects(String userId, String appId, List<String> projectsToDelete)  async{
    if (projectsToDelete == null || projectsToDelete.isEmpty) {
      return true;
    }
    for (String projectId in projectsToDelete) {
      await unAssignProjectFromUser(userId,appId,projectId);
//                If status of any one project data submission is 0 do nothing else delete
//                        delete from project table only if project data submission is deleted

      List<ProjectSubmissionUploadStatus> uploadStatusList = new List();
      uploadStatusList.add(ProjectSubmissionUploadStatus.UNSYNCED);
      uploadStatusList.add(ProjectSubmissionUploadStatus.SERVER_ERROR);

      List<MediaUploadStatus> mediaStatusList = new List();
      mediaStatusList.add(MediaUploadStatus.NEW);
      mediaStatusList.add(MediaUploadStatus.PENDING);

//       get unSynced projectSubmission count also
      int formSubmissionDataCount  = await _databaseHelper.getProjectSubmissionCountForGivenStatusList(appId, userId, projectId
          , uploadStatusList);
//       get unSynced Media count
      int mediaCount = await _databaseHelper.getFormMediaCountForGivenStatusList(appId, userId, projectId, mediaStatusList);

//                 if no unSynced media and projectSubmission data found delete project else do nothing
      if(formSubmissionDataCount == 0 && mediaCount == 0) {

//         delete all project submission
        await _databaseHelper.deleteAllProjectSubmission(appId, userId, projectId);
        //TODO get media paths for project
//        List<String> mediaPaths = await _databaseHelper.getMediaPathsForProject(appId, userId, projectId);
        // delete all media from DB
        await _databaseHelper.deleteAllTheMediaForProject(appId, userId, projectId);
        // delete all media from storage
        // TODO delete all media from storage
        // delete project
        await _databaseHelper.deleteProject(appId, userId, projectId);

      } else {
        continue;
      }
    }
    logger.e("Successfully deleted projects from server for appId: " +appId + " , userId:" + userId);
    return true;
  }


  Future<int> unAssignProjectFromUser(String userId, String appId, String projectId) async {
    int id = await _databaseHelper.updateAssignedStatusOfProjectFromUser(userId, appId
        , projectId, 0);
    logger.e( " -- unassigned the project ::  "
        + projectId + " -- from user ::  " + userId + " -- AppId :: " + appId);
    return id;

  }
}