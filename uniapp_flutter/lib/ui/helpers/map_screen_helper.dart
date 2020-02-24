import '../../utils/common_constants.dart';

import '../../models/project_icon_info.dart';
import '../../utils/map_utils.dart';

import '../../models/legend_attribute.dart';
import '../../models/map_config.dart';
import '../../models/map_marker.dart';
import '../../models/map_project_info.dart';
import 'package:latlong/latlong.dart';

class MapScreenHelper {

  Map<LegendAttribute, int> initializeLegendMarkerCount(List<MapProjectInfo> projectInfoList,
      ProjectIconInfo iconInfo, Map<LegendAttribute, bool> legendAttributes){

    Map<LegendAttribute, int> initiallegendMarkerCount = new Map();
    for(MapProjectInfo info in projectInfoList) {
      ProjectIconInfo projectIconInfo = new ProjectIconInfo();
      if(info.iconInfo != null){
        projectIconInfo = info.iconInfo;
      } else if(iconInfo != null){
        projectIconInfo.defaultMarker = iconInfo.defaultMarker == null ? "" : iconInfo.defaultMarker;
        projectIconInfo.staticUrl = iconInfo.staticUrl;
        projectIconInfo.dynamicKeyName = iconInfo.dynamicKeyName;
        List<LegendAttribute> legendAttrs = new List();
        List<String> dynamicUrlValues = MapUtils().getProjectIconInfoValues(projectIconInfo, info.projectInfo);
        for(String value in dynamicUrlValues){
          for(LegendAttribute attrKey in legendAttributes.keys){
            if(value.toLowerCase() == attrKey.label.toLowerCase()){
              legendAttrs.add(attrKey);
            }
          }
        }
        if(legendAttrs.isEmpty){
          legendAttrs.add(new LegendAttribute(key: CommonConstants.SOURCE_KEY, label: CommonConstants.DEFAULT_LABEL, url: projectIconInfo.defaultMarker));
        }
        projectIconInfo.markerAttributes = legendAttrs;
      }
      if(projectIconInfo.markerAttributes != null && projectIconInfo.markerAttributes.isNotEmpty) {
        for(LegendAttribute attribute in projectIconInfo.markerAttributes){
          if(checkIfLegendAttributeIsPresent(attribute, initiallegendMarkerCount.keys.toList())){
            if(initiallegendMarkerCount[attribute] == null){
              initiallegendMarkerCount[attribute] = 1;
            } else {
              initiallegendMarkerCount[attribute] = initiallegendMarkerCount[attribute] + 1;
            }
          } else{
            initiallegendMarkerCount[attribute] = 1;
          }
        }
      }
    }
    return initiallegendMarkerCount;
  }

  Map<LegendAttribute, bool> initializeLegendMarkerToggleInfo(MapConfig mapConfig, String appId) {
    Map<LegendAttribute, bool> legendMarkerToggleInfo = new Map();
    if (mapConfig != null) {
      List<MapMarker> markers = mapConfig.mapMarkers[appId];
      if(markers != null && markers.isNotEmpty){
        for(MapMarker marker in markers) {
          if(marker.legendAttributes != null && marker.legendAttributes.isNotEmpty) {
            for(LegendAttribute attribute in marker.legendAttributes) {
              if(!checkIfLegendAttributeIsPresent(attribute, legendMarkerToggleInfo.keys.toList())){
                legendMarkerToggleInfo[attribute] = true;
              }
            }
          }
        }
      }
    }
    return legendMarkerToggleInfo;
  }

  List<String> getLegendAttributeKeys(MapConfig mapConfig, String appId){
    List<String> attrKeys = new List();
    if (mapConfig != null) {
      List<MapMarker> markers = mapConfig.mapMarkers[appId];
      MapMarker chkMarker;
      if(markers != null && markers.isNotEmpty) {
        if (markers.length == 1) {
          chkMarker = markers[0];
        } else {
          for (MapMarker marker in markers) {
            if (marker.mapEntityName != 'DEFAULT') {
              chkMarker = marker;
              break;
            }
          }
        }
      }
      if(chkMarker != null && chkMarker.legendAttributes != null && chkMarker.legendAttributes.isNotEmpty) {
        for(LegendAttribute attr in chkMarker.legendAttributes){
          attrKeys.add(attr.key);
        }
      }
    }
    return attrKeys;
  }

  bool checkIfLegendAttributeIsPresent(LegendAttribute attr, List<LegendAttribute> legendAttrs){
    for(LegendAttribute attribute in legendAttrs){
      if(attr.label == attribute.label){
        return true;
      }
    }
    return false;
  }

  int getMarkerCountForLegend(Map<LegendAttribute, int> markerCount, String label){
    for(LegendAttribute attr in markerCount.keys){
      if(attr.label == label){
        return markerCount[attr];
      }
    }
    return 0;
  }

  bool getMarkerToggleInfo(Map<LegendAttribute, bool> legendMarkerToggleInfo, String label){
    for(LegendAttribute attr in legendMarkerToggleInfo.keys){
      if(attr.label == label){
        return legendMarkerToggleInfo[attr];
      }
    }
    return true;
  }

  LatLng initializeMapCenter(List<MapProjectInfo> projectInfoList){
    LatLng center;
    double centerLat = 0.0;
    double centerLon = 0.0;
    int count = 0;
    for(MapProjectInfo info in projectInfoList){
      if(info.lat != null && info.lat.isNotEmpty && info.long != null && info.long.isNotEmpty){
        double latitude = double.parse(info.lat);
        double longitude = double.parse(info.long);
        if(latitude != 0.0 && longitude != 0.0){
          centerLat += latitude;
          centerLon += longitude;
          count++;
        }
      }
    }
    center = new LatLng(centerLat/count, centerLon/count);
    return center;
  }
}