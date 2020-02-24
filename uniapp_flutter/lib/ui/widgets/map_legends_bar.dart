import 'dart:collection';

import '../../utils/common_utils.dart';
import 'package:flutter/material.dart';

import 'global/empty_container.dart';
import '../../models/map_config.dart';
import '../../models/map_marker.dart';
import '../../models/map_project_info.dart';
import '../../models/project_icon_info.dart';
import '../../models/sub_app.dart';
import '../../ua_app_context.dart';
import '../../utils/map_utils.dart';
import 'dart:io';

class MapLegendsBar extends StatelessWidget {
  Map<String, double> positionMap = Map();
  String appId;
  List<MapProjectInfo> projectInfoList;
  MapConfig mapConfig = UAAppContext.getInstance().mapConfig;
  ValueChanged<Map<String, bool>> markerVisibilityInfo;
  Map<String, bool> showMarkerInfo;

  MapLegendsBar(
      {this.appId,
      this.projectInfoList,
      this.markerVisibilityInfo,
      this.showMarkerInfo});

  @override
  Widget build(BuildContext context) {
    print("appId${appId}");

    List<MapMarker> mapMarkers = mapConfig == null ||
            mapConfig.mapMarkers == null ||
            mapConfig.mapMarkers[appId] == null
        ? List()
        : mapConfig.mapMarkers[appId];
    //Remove DEFAULT marker at 0 position

    positionMap["top"] = -8.0;
    positionMap["right"] = 32.0;

    return BottomAppBar(
      child: LegendWidget(
        legends: mapMarkers,
        appId: appId,
        projectInfoList: projectInfoList,
        showMarkerInfo: showMarkerInfo,
        markerVisibilityInfo: markerVisibilityInfo,
      ),
    );
  }
}

class LegendWidget extends StatelessWidget {
  LegendWidget({
    @required this.legends,
    @required this.appId,
    this.projectInfoList,
    this.showMarkerInfo,
    this.markerVisibilityInfo,
  });

  final List<MapProjectInfo> projectInfoList;
  final List<MapMarker> legends;
  final legendsBarHeight = 64.0;
  final String appId;
  final Map<String, bool> showMarkerInfo;
  final ValueChanged<Map<String, bool>> markerVisibilityInfo;
  Map<String, bool> markerVisibleInfo = new Map();

  eachLegend() {
    Map<String, String> mapMarkerCount = getMarkerCount();
    if (legends.length > 0) {
      return Container(
        height: legendsBarHeight,
        margin: EdgeInsets.symmetric(vertical: 6.0),
        child: ListView.builder(
          scrollDirection: Axis.horizontal,
          itemCount: legends.length,
          itemBuilder: (BuildContext context, index) {
            return eachLegendWidget(legends[index], mapMarkerCount);
          },
        ),
      );
    }
    return EmptyContainer();
  }

  eachLegendWidget(MapMarker legend, Map<String, String> mapMarkerCount) {
    Map<String, double> positionMap = new Map();
    positionMap["top"] = -8.0;
    positionMap["right"] = -0.0;
    final iconSize = 40.0;
    bool _isVisible = showMarkerInfo[legend.iconUrl];

    return InkWell(
      onTap: () {
        print("on Tap");
        bool markerVisibility = markerVisibleInfo[legend.iconUrl];
        markerVisibleInfo[legend.iconUrl] = !markerVisibility;
        onClickMarkerInLegend();
      },
      child: Container(
        padding: EdgeInsets.all(8.0),
        child: Opacity(
          opacity: _isVisible ? 1.0 : 0.4,
          child: Stack(
            children: <Widget>[
              FutureBuilder<File>(
                  future: CommonUtils.getIconFromStorage(legend.iconUrl),
                  builder:
                      (BuildContext context, AsyncSnapshot<File> snapshot) {
                    File imgFile;
                    if (snapshot.hasData) {
                      imgFile = snapshot.data;
                      return IconButton(
                        tooltip: legend.mapEntityAdditionalInfo['label'],
                        icon: (Platform.isIOS || imgFile == null)
                            ? Image.network(legend.iconUrl)
                            : Image.file(imgFile),
                        iconSize: iconSize,
                        color: Colors.white,
                        onPressed: () {
                          print("on Pressed");
                          bool markerVisibility =
                              markerVisibleInfo[legend.iconUrl];
                          markerVisibleInfo[legend.iconUrl] = !markerVisibility;
                          onClickMarkerInLegend();
                        },
                      );
                    } else {
                      return CircularProgressIndicator();
                    }
                  }),
              Positioned(
                right: 0,
                child: new Container(
                  padding: EdgeInsets.all(1),
                  decoration: new BoxDecoration(
                    color: Colors.red,
                    borderRadius: BorderRadius.circular(15),
                  ),
                  constraints: BoxConstraints(
                    minWidth: 24,
                    minHeight: 24,
                  ),
                  child: new Text(
                    mapMarkerCount[legend.mapEntityName] == null
                        ? '0'
                        : mapMarkerCount[legend.mapEntityName],
                    style: new TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    markerVisibleInfo = showMarkerInfo;
    return eachLegend();
  }

  //getting MarkerCount
  Map<String, String> getMarkerCount() {
    List<SubApp> apps = UAAppContext.getInstance().rootConfig.config;
    ProjectIconInfo projectIconInfoFromAppConfig;
    for (SubApp app in apps) {
      if (app.appId.compareTo(appId) == 0) {
        projectIconInfoFromAppConfig = app.projectIconInfo;
      }
    }
    MapConfig mapConfig = UAAppContext.getInstance().mapConfig;
    List<MapMarker> mapMarkers = mapConfig.mapMarkers[appId];
    Map<String, String> markerCount = new HashMap();

    for (MapMarker mapMarker in mapMarkers) {
      String iconName1 = "";
      if(mapMarker.iconUrl != null) {
        iconName1 = mapMarker.iconUrl.substring(mapMarker.iconUrl.lastIndexOf("/") + 1);
      }
      for (MapProjectInfo project in projectInfoList) {
        String iconName2 = "";
        String projIconUrl = MapUtils().getProjectIcon(
            project.iconInfo == null
                ? projectIconInfoFromAppConfig
                : project.iconInfo,
            project.projectInfo);

        if(projIconUrl != null) {
          iconName2 = projIconUrl.substring(projIconUrl.lastIndexOf("/") + 1);
        }
        if (iconName1.compareTo(iconName2) == 0) {
          if (markerCount[mapMarker.mapEntityName] == null) {
            markerCount[mapMarker.mapEntityName] = "1";
          } else {
            String count = markerCount[mapMarker.mapEntityName];
            int countValue = int.parse(count);
            countValue = countValue + 1;
            markerCount[mapMarker.mapEntityName] = countValue.toString();
          }
        }
      }
    }
    return markerCount;
  }

  void onClickMarkerInLegend() {
    // change the showMarkerInfo
    markerVisibilityInfo(markerVisibleInfo);
  }
}
