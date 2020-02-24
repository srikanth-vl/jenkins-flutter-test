import 'dart:async';
import 'dart:collection';
import 'dart:convert';
import 'ui/screens/map_screen.dart';
import 'utils/common_constants.dart';

import 'models/map_marker.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'db/models/project_master_data_table.dart';
import 'models/load_map_info.dart';
import 'models/map_config.dart';
import 'models/map_project_info.dart';
import 'models/project_icon_info.dart';
import 'models/root_config.dart';
import 'models/sub_app.dart';
import 'ua_app_context.dart';
import 'ui/screens/projectform_screen.dart';
import 'utils/map_utils.dart';
import 'utils/network_utils.dart';

typedef void MapRendererCreatedCallback(MapRendererController controller);

class MapRenderer extends StatefulWidget {
  final List<ProjectMasterDataTable> projectList;
  final MapRendererCreatedCallback onMapRendererCreated;
  final String appId;

  const MapRenderer({
    this.onMapRendererCreated,
    this.projectList,
    this.appId,
  });

  @override
  State<StatefulWidget> createState() =>
      MapRendererState(projectList: projectList, appId: appId);
}

class MapRendererState extends State<MapRenderer> {
  BuildContext ctxt;
  List<ProjectMasterDataTable> projectList;
  String appId;

  MapRendererState({
    this.projectList,
    this.appId,
  });

  @override
  void initState() {
    super.initState();
    ctxt = context;
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<bool>(
      future: NetworkUtils().hasActiveInternet(),
      builder: (BuildContext context, AsyncSnapshot<bool> snapshot) {
        if (!snapshot.hasData) {
          // while data is loading:
          return Center(
            child: CircularProgressIndicator(),
          );
        } else {
          // data loaded:
          final androidDeviceInfo = snapshot.data;
          /*if (!snapshot.data) {
            if (defaultTargetPlatform == TargetPlatform.android) {
              return AndroidView(
                viewType: 'mapview',
                onPlatformViewCreated: _onPlatformViewCreated,
              );
            }
          }*/
          List<MapProjectInfo> mapProjectInfoList = new List();
          for (ProjectMasterDataTable project in projectList) {
            MapProjectInfo mapProjectInfo = getMapProjectInfo(project);
            mapProjectInfoList.add(mapProjectInfo);
          }

          return MapScreen(appId: appId, projectInfoList: mapProjectInfoList, isOnline: snapshot.data,);
        }
      },
    );

    // TODO add other platforms
    /*return Center(
      child: Text(
          '$defaultTargetPlatform is not yet supported by the map view plugin'),
    );*/
  }

  void _onPlatformViewCreated(int id) {
    if (widget.onMapRendererCreated == null) {
      return;
    }
    widget.onMapRendererCreated(
        new MapRendererController(id, ctxt, projectList, appId));
  }

  Map<String, bool> getMarkerVisibilityInfo(String appId) {
    MapConfig mapConfig = UAAppContext.getInstance().mapConfig;
    // TODO: add a null check on mapconfig.
    List<MapMarker> mapMarkers = mapConfig.mapMarkers[appId];
    Map<String, bool> markerInfoMap = new Map();
    if(mapConfig != null && mapConfig.mapMarkers != null) {
      List<MapMarker> mapMarkers = mapConfig.mapMarkers[appId];
      if(mapMarkers == null) {
        return markerInfoMap;
      }
      for (MapMarker marker in mapMarkers) {
        markerInfoMap[marker.iconUrl] = true;
      }
    }
    return markerInfoMap;
  }

  MapProjectInfo getMapProjectInfo(ProjectMasterDataTable project){
    MapProjectInfo mapProjectInfo = new MapProjectInfo();
    mapProjectInfo.showProjectMarker = true;
    mapProjectInfo.projectname = project.projectName;
    mapProjectInfo.projectid = project.projectId;
    mapProjectInfo.lat = project.projectLat;
    mapProjectInfo.long = project.projectLon;
    mapProjectInfo.projectInfo = project.getKeyToValueMap();
    mapProjectInfo.iconInfo = project.projectIcon;
    return mapProjectInfo;
  }
}

class MapRendererController {
  MethodChannel _channel;
  BuildContext cont;
  List<ProjectMasterDataTable> projectList;
  String appId;

  MapRendererController(int id, BuildContext context,
      List<ProjectMasterDataTable> projectList, String appId) {
    this._channel = new MethodChannel('mapview$id');
    this._channel.setMethodCallHandler(_methodCallHandler);
    this.cont = context;
    this.projectList = projectList;
    this.appId = appId;
  }

  Future<void> _methodCallHandler(MethodCall call) async {
    switch (call.method) {
      case 'projectform':
        Navigator.of(cont).push(
          MaterialPageRoute(
            builder: (BuildContext context) => ProjectFormScreen(
              appId: appId,
              projectId: call.arguments.toString(),
              formActiontype: CommonConstants.UPDATE_FORM_KEY,
            ),
          ),
        );
        break;
      case 'navigation':
        List<String> point = call.arguments.toString().split("##");
        MapUtils().openMap(
            double.parse(point.elementAt(0)), double.parse(point.elementAt(1)));
        break;
      default:
        print(
            'TestFairy: Ignoring invoke from native. This normally shouldn\'t happen.');
    }
  }

  Future<void> loadMap(String url) async {
    return _channel.invokeMethod(
        'loadMap', {"config": _getProjectListDataAndMapConfig(projectList)});
  }

  String _getProjectListDataAndMapConfig(
      List<ProjectMasterDataTable> projectList) {
    List<MapProjectInfo> mapProjectInfo = _getMapProjectInfo(projectList);
    LoadMapInfo loadMapInfo = new LoadMapInfo(
        mapProjectInfo, UAAppContext.getInstance().mapConfig, appId, "");

    String loadMapConfigJSON = jsonEncode(loadMapInfo.toJson());
    return loadMapConfigJSON;
  }

  List<MapProjectInfo> _getMapProjectInfo(
      List<ProjectMasterDataTable> projectList) {
    List<MapProjectInfo> mapProjectInfoList = List<MapProjectInfo>();
    for (ProjectMasterDataTable project in projectList) {
      RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
      ProjectIconInfo iconInfo;
      for (SubApp app in rootConfig.config) {
        if (app.appId.compareTo(project.projectAppId) == 0) {
          iconInfo = app.projectIconInfo;
        }
      }
      String icon = MapUtils().getProjectIcon(iconInfo, project.getKeyToValueMap());

      Map<String, String> projectInfoMap =
          getProjectInfoMap(project, project.projectAppId);
      projectInfoMap["Project"] = project.projectName;

      MapProjectInfo mapProjectInfo = new MapProjectInfo(
          projectname: project.projectName,
          long: project.projectLon,
          projectid: project.projectId,
          lat: project.projectLat,
          projectIcon: icon,
          projectInfo: projectInfoMap);
      mapProjectInfoList.add(mapProjectInfo);
    }
    return mapProjectInfoList;
  }

  Map<String, String> getProjectInfoMap(
      ProjectMasterDataTable project, String appId) {
    Map<String, String> projectInfoMap = new HashMap();
    List<SubApp> subApps = UAAppContext.getInstance().rootConfig.config;
    Map<String, String> keyToValueMap = project.getKeyToValueMap();

    if(subApps != null) {
      for (SubApp subApp in subApps) {
        if (subApp.appId.compareTo(appId) == 0) {
          Map<String, String> mapOverlaysInfo = subApp.mapOverLayInfo;
          if (mapOverlaysInfo != null && mapOverlaysInfo.isNotEmpty) {
            for (String key in subApp.mapOverLayInfo.keys) {
              projectInfoMap[mapOverlaysInfo[key]] = keyToValueMap[key];
            }
          }
        }
      }
    }
    return projectInfoMap;
  }
}