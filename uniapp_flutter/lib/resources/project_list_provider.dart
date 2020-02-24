import '../log/uniapp_logger.dart';
import '../utils/network_utils.dart';

import '../db/models/project_master_data_table.dart';
import '../models/sub_app.dart';
import '../resources/project_type_configuration_service.dart';

import 'project_list_service.dart';
import '../models/project_type_configuartion.dart';
import '../ua_app_context.dart';
import '../db/databaseHelper.dart';

class ProjectListProvider {
  static final ProjectListProvider ourInstance = new ProjectListProvider();
  static DatabaseHelper _databaseHelper = DatabaseHelper();
  NetworkUtils _networkUtils ;
  UAAppContext appContext;
  static ProjectListProvider getInstance() {
    return ourInstance;
  }
 Logger logger  = getLogger("ProjectListProvider");
  int lastSyncTime;

  ProjectListProvider() {
    this.lastSyncTime = 0;
    _networkUtils= NetworkUtils();
  }

  Future initProjectList(
      Map<String, ProjectTypeConfiguration> appToPTCMap) async {
    // Fetch Project Count from the DB
    int projectCount = await _databaseHelper
        .getProjectCountForUser(UAAppContext.getInstance().userID);
    if (projectCount > 0) {
      // print('There are ${projectCount} projects available for this user');
      // There are projects available for this user - good to start the app
      return;
    }
    // Fetching the project list config from the server
    if (appToPTCMap != null) {
      await fetchProjectListFromServer(appToPTCMap);
    }
    // todo log success message
    return;
  }

  void fetchProjectListFromServer(
      Map<String, ProjectTypeConfiguration> appToPTCMap) async {
    ProjectListService projectListService = new ProjectListService();

    for(String app in appToPTCMap.keys) {
      ProjectTypeConfiguration ptc = appToPTCMap[app];
      bool isOnline = await networkUtils.hasActiveInternet();
      if (isOnline){
      //  Check if server available
      try {
        await projectListService.callProjectListService(ptc, null);
      } catch (u) {
        //todo throw exception
        // Error - exit app
        u.printStackTrace();
      }}
    }
    this.lastSyncTime = DateTime.now().millisecondsSinceEpoch;
    return;
  }

  Future<void> fetchFromServerForBackgroundSync() async {
    int syncFrequency = UAAppContext.getInstance().appMDConfig.serviceFrequency.projectlist;
    if ((DateTime.now().millisecondsSinceEpoch - lastSyncTime) <= syncFrequency) {
      logger.d("ProjectList was synced recently, will try in future");
      //todo log success Message
      return;
    }
// Fetch all apps for this user
    String userId = UAAppContext.getInstance().userID;
    List<SubApp> ptmList = UAAppContext.getInstance().rootConfig == null ? null : UAAppContext.getInstance().rootConfig.config;
    ProjectTypeService projectTypeService = ProjectTypeService();
    Map<String, ProjectTypeConfiguration> ptcMap = await projectTypeService
        .fetchAppIdToProjectTypeConfigurationFromDb(userId, ptmList);
    ProjectListService pls = new ProjectListService();
    for(String app in ptcMap.keys)  {
      //  Check if server available
      bool isOnline = await networkUtils.hasActiveInternet();
      ProjectTypeConfiguration ptc = ptcMap[app];
      if (isOnline){
      var res = await pls.handleProjectSync(ptc.projecttype, ptc);
      } else {
        logger.d("App is online didn't fetch ProjectTypeConfiguration for  app ${ptc.projecttype}");
      }
    }

    lastSyncTime = DateTime.now().millisecondsSinceEpoch;
  }
  Future<List<ProjectMasterDataTable>> fetchProjects(String appId, String userId,String groupKey, String groupValue) async{
    List<ProjectMasterDataTable> projects =  await _databaseHelper.getProjectsForUser(userId, appId,groupKey, groupValue);
    if(projects ==  null) {
      projects =  new List();
    }
    return projects;
  }

  Future<Map<String,List<String>>> fetchProjectGroupsAttribute(String appId, String userId) async {
    SubApp subApp =  UAAppContext.getInstance().rootConfig.config.firstWhere((a) => a.appId == appId);
    if(subApp == null)
      return Map();
    Map<String, List<String>> projectGroupsAttribute = await _databaseHelper.getProjectGroupsAttributeValues(userId, appId, subApp.groupingAttributes);
    return projectGroupsAttribute;
  }

  Future<ProjectMasterDataTable> fetchProjectFromDB(String userId, String appId, String projectId) async{
    ProjectMasterDataTable plc = await _databaseHelper.getLatestProjectEntry(userId, appId, projectId);
    return plc;
  }
}
