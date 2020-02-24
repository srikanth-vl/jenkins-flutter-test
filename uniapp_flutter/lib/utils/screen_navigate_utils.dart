import '../models/gps_validation.dart';
import 'package:flutter/material.dart';

import './common_constants.dart';
import '../db/models/project_master_data_table.dart';
import '../log/uniapp_logger.dart';
import '../models/route_parameters.dart';
import '../ua_app_context.dart';
import '../ui/helpers/form_values.dart';
import '../utils/common_utils.dart';

// TODO: @Rohan add a return functionality when used with await.

class ScreenNavigateUtils {
  static Logger logger = getLogger("NavigateUtils");
  RouteParameters routeParameters = RouteParameters();

  navigateToLoginScreen(
      BuildContext context, bool willReplace, bool willPopUntil) {
    routeParameters.context = context;
    routeParameters.willPopUntil = willPopUntil;
    routeParameters.willReplace = willReplace;
    routeParameters.routeName = CommonConstants.loginRoute;
    navigateThroughNamedRoutes(routeParameters);
  }

  navigateToHomeScreen(BuildContext context, bool willReplace) {
    routeParameters.context = context;
    routeParameters.willReplace = willReplace;
    routeParameters.routeName = CommonConstants.homeRoute;
    navigateThroughNamedRoutes(routeParameters);
  }

  navigateToGetOTPScreen(BuildContext context) {
    routeParameters.context = context;
    routeParameters.routeName = CommonConstants.getOTPRoute;
    navigateThroughNamedRoutes(routeParameters);
  }

  navigateToChangePasswordScreen(BuildContext context) {
    routeParameters.context = context;
    routeParameters.routeName = CommonConstants.changePasswordRoute;
    navigateThroughNamedRoutes(routeParameters);
  }

  navigateToDownloadMapScreen(BuildContext context) {
    routeParameters.context = context;
    routeParameters.routeName = CommonConstants.downloadsRoute;
    navigateThroughNamedRoutes(routeParameters);
  }

  navigateToProjectFormScreen(BuildContext context, String appId,
      String projectId, String formActionType) async {
    routeParameters.context = context;
    routeParameters.appId = appId;
    routeParameters.projectId = projectId;
    routeParameters.formActionType = formActionType;
    routeParameters.routeName = CommonConstants.projectFormRoute;
    await formMap.initializeFormMap(appId, projectId, formActionType);
    navigateThroughNamedRoutes(routeParameters);
  }

  navigateToFilterScreen(BuildContext context, String appId) {
    routeParameters.context = context;
    routeParameters.appId = appId;
    routeParameters.routeName = CommonConstants.filterRoute;
    List<ProjectMasterDataTable> projectList =
        UAAppContext.getInstance().projectList;
    if (projectList != null && projectList.isNotEmpty) {
      navigateThroughNamedRoutes(routeParameters);
    } else {
      CommonUtils.showToast(CommonConstants.NO_PROJECTS_FOUND, context);
    }
  }

//  navigateToProjectListScreen(BuildContext context, String appId, String key, bool willReplace) {
//    routeParameters.context = context;
//    routeParameters.routeName = CommonConstants.projectListRoute;
//    routeParameters.willReplace = willReplace;
//    routeParameters.appId = appId;
//    routeParameters.groupingKey = key;
//    navigateThroughNamedRoutes(routeParameters);
//  }

  navigateToProjectListScreen(
    BuildContext context,
    String appId,
    String sortType,
    String groupingKey,
    String groupingValue,
    List<ProjectMasterDataTable> projectMasterDataTableList,
    bool willReplace,
  ) {
    routeParameters.context = context;
    routeParameters.routeName = CommonConstants.projectListRoute;
    routeParameters.willReplace = willReplace;
    routeParameters.appId = appId;
    routeParameters.sortType = sortType;
    routeParameters.groupingKey = groupingKey;
    routeParameters.groupingValue = groupingValue;
    routeParameters.projectMasterDataTableList = projectMasterDataTableList;
    navigateThroughNamedRoutes(routeParameters);
  }

  navigateToProjectGroupScreen(BuildContext context, String appId,
      String sortType, List<String> groupingAttributes, bool willReplace) {
    routeParameters.context = context;
    routeParameters.appId = appId;
    routeParameters.groupingAttributes = groupingAttributes;
    routeParameters.willReplace = willReplace;
    routeParameters.routeName = CommonConstants.projectGroupRoute;
    routeParameters.sortType = sortType;
    navigateThroughNamedRoutes(routeParameters);
  }

  navigateToGeoTaggingScreen(BuildContext context, String geoTaggingId,
      GpsValidation gpsValidation, String projLat, String projLon) {
    routeParameters.context = context;
    routeParameters.geoTaggingWidgetId = geoTaggingId;
    routeParameters.routeName = CommonConstants.geoTaggingRoute;
    routeParameters.gpsValidation = gpsValidation;
    routeParameters.projLat = projLat;
    routeParameters.projLon = projLon;
    return navigateThroughNamedRoutes(routeParameters);
  }

  // TODO: @Rohan Preview screen implement this popuntil.
  navigateThroughNamedRoutes(RouteParameters params) {
    if (params.context == null || params.routeName == null) return;
    logger.i("Navigating to: ${params.routeName}");
    return params.willReplace
        ? params.willPopUntil
            ? Navigator.pushNamedAndRemoveUntil(
                params.context, params.routeName, (route) => false)
            : Navigator.of(params.context)
                .pushReplacementNamed(params.routeName, arguments: params)
        : Navigator.of(params.context)
            .pushNamed(params.routeName, arguments: params);
  }
}
