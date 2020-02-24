import '../../models/project_type_configuartion.dart';
import '../../resources/project_list_provider.dart';
import '../../resources/project_type_provider.dart';

import '../../models/root_config.dart';
import '../../models/sub_app.dart';
import '../../sync/sync_initiater.dart';
import '../../utils/screen_navigate_utils.dart';
import 'package:flutter/cupertino.dart';

import '../../ua_app_context.dart';

class HomeScreenHelper {
  navigateToProjectGroupScreen(
      BuildContext context,
      String appId,
      String sortType,
      List<String> groupingAttributes,
      bool willReplace) async {
    if (groupingAttributes != null && groupingAttributes.isNotEmpty) {
      ProjectListProvider projectListProvider =
          ProjectListProvider.getInstance();
      await projectListProvider.fetchProjectGroupsAttribute(
          appId, UAAppContext.getInstance().userID);
      ScreenNavigateUtils().navigateToProjectGroupScreen(
          context, appId, sortType, groupingAttributes, willReplace);
    } else {
      ScreenNavigateUtils().navigateToProjectListScreen(
          context, appId, sortType, null, null, null, willReplace);
    }
  }

  callBackgroundThreads() {
    SyncInitiator syncInitiater = new SyncInitiator();
    syncInitiater.initializeBackgroundService();
  }

  skipHomeScreen(BuildContext context) async {
    callBackgroundThreads();
    RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
    if (rootConfig != null &&
        rootConfig.config != null &&
        rootConfig.config.isNotEmpty) {
      if (rootConfig.config.length == 1) {
        SubApp subApp = rootConfig.config[0];
        if (subApp != null) {
          await callProjectConfigurations();
          navigateToProjectGroupScreen(context, subApp.appId, subApp.sortType,
              subApp.groupingAttributes, true);
        }
      } else {
        _navigateToHomeScreen(context);
      }
    }
  }

  _navigateToHomeScreen(BuildContext context) {
    if (context == null) return;
    ScreenNavigateUtils().navigateToHomeScreen(context, true);
  }

  callProjectConfigurations() async {
    ProjectTypeProvider projectTypeProvider = ProjectTypeProvider.getInstance();
    Map<String, ProjectTypeConfiguration> map =
        await projectTypeProvider.initProjectType();
    ProjectListProvider projectListProvider = ProjectListProvider.getInstance();
    await projectListProvider.initProjectList(map);
    return;
  }
}
