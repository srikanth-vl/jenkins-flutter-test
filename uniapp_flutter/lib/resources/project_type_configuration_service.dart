import 'dart:async';
import 'dart:core';
import '../event/event_utils.dart';
import 'package:http/http.dart' as http;
import '../error/all_custom_exception.dart';
import 'dart:convert';
import '../db/databaseHelper.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import '../models/project_type_configuartion.dart';
import '../models/sub_app.dart';
import 'uniapp_response.dart';
import '../db/models/project_form_table.dart';
import '../models/project_specific_form.dart';
import '../log/uniapp_logger.dart';
class ProjectTypeService {

  DatabaseHelper _databaseHelper;
  Logger logger = getLogger("ProjectTypeService");
  ProjectTypeService() {
    _databaseHelper = DatabaseHelper();
  }

  String createRequestParams(String appId,  Map<String, Map<String, int>>formVersion) {
    Map jsonObject = Map();
    Map  exitingVersionInfo;
    if (formVersion == null || formVersion.isEmpty)
      exitingVersionInfo = null;
    else
      exitingVersionInfo = createJsonObjectFromMap(formVersion);
    jsonObject[CommonConstants.PROJECT_TYPE_CONFIG_USER_ID_KEY] = UAAppContext.getInstance().userID;
    jsonObject[CommonConstants.PROJECT_TYPE_CONFIG_TOKEN_KEY] = UAAppContext.getInstance().token;
    jsonObject[CommonConstants.PROJECT_TYPE_CONFIG_SUPER_APP_KEY] = CommonConstants.SUPER_APP_ID;
    jsonObject[CommonConstants.PROJECT_TYPE_CONFIG_APP_ID_KEY]= appId;
    jsonObject[CommonConstants.PROJECT_TYPE_CONFIG_FORM_VERSION_KEY] =  exitingVersionInfo;
    var body = json.encode(jsonObject);
    return body;
  }

  Future<ProjectTypeConfiguration> callProjectTypeConfigService(String appId, Map<String, Map<String, int>> formVersion) async {

    // Logic
    // 1. Fetch from Server
    // 2. Save to DB
    // 3. Return the AppMetaData
    // 4. In case of error - return the error type

    String content = await fetchFromServer(appId, formVersion);
    if (content == null) {
      return null;
    }

    // Add new AppMataDataConfig to the App Context
    ProjectTypeConfiguration projectTypeConfiguration = null;
    projectTypeConfiguration = ProjectTypeConfiguration.fromJson(jsonDecode(content));
    await storeToDB(projectTypeConfiguration);
    return projectTypeConfiguration;
  }

  Future<String> fetchFromServer(String appId, Map<String, Map<String, int>> formVersion) async{

    var requestParam = createRequestParams(appId, formVersion);
    if (requestParam == null ) {
      logger.e("Error creating request parameters - ProjectType Config");
      throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error creating request parameters - ProjectType Config");
    }
    final response = await http.post(CommonConstants.BASE_URL + "projecttype",headers:{"Content-Type": "application/json"},body: requestParam);
    if(response == null || response.statusCode != 200 || response.body ==  null) {
      // throw exception log it
      logger.e('Error getting ProjectType from server : received responseString : ${response}' );
      throw ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
          , 'Error getting ProjectType from server : received responseString : ${response}' );
    }
    UniappResponse uniappResponse = UniappResponse.fromJson(jsonDecode(response.body));
    if(uniappResponse ==  null || uniappResponse.result ==  null ) {
      // throw exception log it
      logger.e("Error getting ProjectType from server , uniapp response ::${uniappResponse == null ? uniappResponse : uniappResponse.toJson()}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting ProjectType from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
    }
    int code = uniappResponse.result.status;
    String content ;
    if (code == 200) {
      logger.d("Successfully fetched ProjectTypeConfiguration from server for appId: ${appId}");
      content = uniappResponse.result.content;
      if (content == "{}") {
        // Same version on the server
        return '{}';
      } else {
        return content;
      }
    } else if (code == 350) {
      // token expired broadcast logout event
      eventBus.fire(TokenExpiredEvent());
      // stop background sync service
      throw new TokenExpiredException(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Tokn expired");
      //todo update sharedpreferences

    } else {
      logger.e('Error getting ProjectTypeConfiguration from server (invalid data) : uniapp response code : ${uniappResponse.result.status}');
      throw ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
          , 'Error getting ProjectTypeConfiguration from server (invalid data) : uniapp response code : ${uniappResponse.result.status}');
    }

  }
  storeToDB(ProjectTypeConfiguration projectTypeConfiguration) async {
    // Add new projectTypeConfiguration to the DB
    List<ProjectFormTable> dbForms = List<ProjectFormTable> ();
    for ( String projectId in projectTypeConfiguration.content.keys ) {
      Map<String, ProjectSpecificForm> actionTypeToFormMap = projectTypeConfiguration.content[projectId];
      for (String formType in actionTypeToFormMap.keys) {
        ProjectSpecificForm formData = actionTypeToFormMap[formType];
        String formJsonString =  jsonEncode(formData.toJson());
        ProjectFormTable dbProjectForm =  ProjectFormTable(projectTypeConfiguration.userId, projectTypeConfiguration.projecttype, projectId, formType, formJsonString, formData.mdinstanceid, formData.formversion);
        await  _databaseHelper.insertProjectForm(dbProjectForm);
        //TODO log error
      }
    }
  }

  Future<Map<String, ProjectTypeConfiguration>> fetchAppIdToProjectTypeConfigurationFromDb(String userId, List<SubApp> ptmList)  async{
    // If ptmList is null or empty then return all app ids
    // else return for only the appids in the ptmList
    Map<String, ProjectTypeConfiguration> ptcMap =  Map();
    if(ptmList == null) {
      ptmList =  UAAppContext.getInstance().rootConfig.config;
    }
    ProjectTypeConfiguration ptc ;
    for (SubApp ptm in ptmList)
    {
      String appId = ptm.appId;
      ptc = await fetchProjectTypeConfigurationFromDb(userId, appId);
      if(ptc != null) {
        ptcMap[appId] = ptc;
      }
    }
    return ptcMap;

  }

  Future<ProjectTypeConfiguration> fetchProjectTypeConfigurationFromDb(String userId, String appId) async {
    // fetch from DB
    ProjectTypeConfiguration ptc =  ProjectTypeConfiguration();
    ptc = await this._databaseHelper.getProjectFormForApp(userId, appId);
    return  ptc;
  }

  Map createJsonObjectFromMap(Map<String, Map<String, int>> formVersionMap){
    Map jsonObject = Map();
    for (String projectId in formVersionMap.keys) {
      Map formTypeToVersionJson = Map();
      Map<String, int> formTypeToVersionMap = formVersionMap[projectId];
      for (String formType in formTypeToVersionMap.keys) {
        formTypeToVersionJson[formType] =  formTypeToVersionMap[formType];
      }
      jsonObject[projectId]=  formTypeToVersionJson;
    }

    return jsonObject;
  }
}
