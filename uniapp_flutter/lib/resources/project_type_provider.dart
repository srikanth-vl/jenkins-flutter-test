import '../models/root_config.dart';
import '../models/sub_app.dart';

import '../resources/project_type_configuration_service.dart';
import '../models/project_type_configuartion.dart';
import '../models/project_specific_form.dart';
import '../db/databaseHelper.dart';
import '../ua_app_context.dart';
import '../utils/network_utils.dart';
import '../log/uniapp_logger.dart';
import '../error/all_custom_exception.dart';
class ProjectTypeProvider {

  static ProjectTypeProvider _ourInstance = new ProjectTypeProvider();
  ProjectTypeService _projectTypeService ;
  DatabaseHelper _databaseHelper ;
  Logger logger =  getLogger("ProjectTypeProvider");
  static ProjectTypeProvider getInstance() {
    return _ourInstance;
  }

  int _lastSyncTime = 0;

  ProjectTypeProvider() {
    _projectTypeService = ProjectTypeService();
    _databaseHelper = DatabaseHelper();
    this._lastSyncTime = 0;
  }

  Future<Map<String, ProjectTypeConfiguration>> initProjectType() async {

    // Fetch Project Type Count from the DB
    int projectTypeCount = await _databaseHelper.getProjectTypeCountForUser(UAAppContext.getInstance().userID);
    if (projectTypeCount > 0) {
      // print('There are  ${projectTypeCount} project types form available for this user');
      // There are project types available for this user - good to start the app
      // TODO : If the project type was saved and the thread failed for some reason, then
      // null will be returned and the ProjectList call fails
      // Do not return null
      // Fetch the map and return
      Map<String, ProjectTypeConfiguration> appToPTCMap = await _projectTypeService.fetchAppIdToProjectTypeConfigurationFromDb(
          UAAppContext.getInstance().userID, UAAppContext.getInstance().rootConfig.config);

      return appToPTCMap;
    }

    // Fetching the project type config from the server

      Map<String,
          ProjectTypeConfiguration> appToPTCMap ;
    try {
      appToPTCMap = await fetchProjectTypeFromServer();
    } on UAException catch (e){
      logger.e("could not fetch Project type Configuration: UAException");
    } catch (e) {
      logger.e("ProjectType config fetched from server");
    }
    if(appToPTCMap ==  null) {
      logger.e("could not fetch Project type Configuration");
      appToPTCMap = Map();
    }
    return appToPTCMap;
  }
  Future<Map<String, ProjectTypeConfiguration>> fetchProjectTypeFromServer() async{

// Get the app ids for this user from rootconfig
    RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
    if (rootConfig.config == null || rootConfig.config.length == 0) {
      //  log warning
      logger.e("Rootconfig not found for App");
      throw AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Rootconfig not found for App");
    }
    String userId = UAAppContext.getInstance().userID;
    Map<String, ProjectTypeConfiguration> appToPTCMap = new Map();
    for (SubApp ptm in rootConfig.config) {
      String appId = ptm.appId;
      // check is server available
      bool isOnline = await networkUtils.hasActiveInternet();
      if(isOnline) {
        ProjectTypeConfiguration existingConfiguration  = await _projectTypeService.fetchProjectTypeConfigurationFromDb(userId, appId);
        Map<String, Map<String, int>> formVersionMap = getFormVersionMap(existingConfiguration);
        ProjectTypeConfiguration ptc = await _projectTypeService.callProjectTypeConfigService(ptm.appId, formVersionMap);
        if(ptc == null) {
          return null;
        }
        logger.d("fetched ProjectTypeConfiguration for  app ${appId}");
        appToPTCMap[ptm.appId] = ptc;
      } else {
        logger.d("App is online didn't fetch ProjectTypeConfiguration for  app ${appId}");
      }
    }
    this._lastSyncTime = DateTime.now().millisecondsSinceEpoch;
    return appToPTCMap;
  }
  void fetchFromServerForBackgroundSync() async {

    Map<String, ProjectTypeConfiguration> result = null;
    int syncFrequency = UAAppContext.getInstance().appMDConfig.serviceFrequency.projecttype;
    if ((DateTime.now().millisecondsSinceEpoch - _lastSyncTime) <= syncFrequency) {
     logger.d("ProjectType Config was synced recently, will try in future");
    }
// This result holds only the delta (changes in version) - the next step (syncing
// project list) requires ProjectTypeConfiguration
    result = await fetchProjectTypeFromServer();
  }
  Map<String, Map<String, int>> getFormVersionMap(ProjectTypeConfiguration projectTypeConfiguration) {
    if(projectTypeConfiguration == null || projectTypeConfiguration.content == null || projectTypeConfiguration.content == '') {
      return null;
    }

    Map<String, Map<String, ProjectSpecificForm>> projectIdToFormTypeMap = projectTypeConfiguration.content;
    Map<String, Map<String, int>> formVersionMap = Map();
    for (String projectId in projectIdToFormTypeMap.keys) {
      Map<String, int> formTypeToVersionMap = new Map();
      Map<String, ProjectSpecificForm> formTypeToFormMap = projectIdToFormTypeMap[projectId] == null ? new Map() : projectIdToFormTypeMap[projectId];
      for (String formType in formTypeToFormMap.keys) {
        ProjectSpecificForm form = formTypeToFormMap[formType];
        formTypeToVersionMap[formType] = form.formversion;
      }
      formVersionMap[projectId]=  formTypeToVersionMap;
    }
    return formVersionMap;
  }
}