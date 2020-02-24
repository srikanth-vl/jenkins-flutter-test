import 'dart:io' show Platform;
import '../models/legend_attribute.dart';
import '../models/map_project_info.dart';

import '../models/project_icon_info.dart';
import '../utils/string_utils.dart';
import 'package:url_launcher/url_launcher.dart';

class MapUtils {

  openMap(double lat, double lon) async {
    if(Platform.isAndroid) {
      var url = 'https://www.google.com/maps/search/?api=1&query=$lat,$lon';
      if (await canLaunch(url)) {
        await launch(url);
      } else {
        throw 'Could not launch $url';
      }
    } else if(Platform.isIOS) {
      var url = 'http://maps.apple.com/&q=$lat,$lon';
      if (await canLaunch(url)) {
        await launch(url);
      } else {
        throw 'Could not launch $url';
      }
    }
  }
  String getProjectIcon(ProjectIconInfo iconInfo, Map<String, String> keyToValue) {
    String iconUrl = "";
    if (iconInfo == null)
      return null;
    if(iconInfo.staticUrl != null && iconInfo.staticUrl.isNotEmpty) {
      iconUrl = iconInfo.staticUrl;
    }

    List<String> dynamicUrlValues = List<String>();
    List keyList = StringUtils.getStringListFromDelimiter(",", iconInfo.dynamicKeyName);
    if(keyList != null && keyList.isNotEmpty) {
      for (String key in keyList) {
        if (keyToValue[key] == null) {
          return iconInfo.defaultMarker;
        }
        dynamicUrlValues.add(keyToValue[key].toLowerCase().replaceAll(" ", ""));
      }
      if (dynamicUrlValues != null && dynamicUrlValues.isNotEmpty) {
        iconUrl = iconUrl + StringUtils.getconcatenatedStringFromStringList(
            "_", dynamicUrlValues);
        iconUrl = iconUrl + ".png";
      } else {
        iconUrl = null;
      }
    }
    if (iconUrl == null) {
      return iconInfo.defaultMarker;
    }
    return iconUrl;
  }

  List<MapProjectInfo> updateProjectIconInfoLegendAttributes(
      List<MapProjectInfo> projectInfoList,
      Map<LegendAttribute, bool> legendAttributes) {
    for (MapProjectInfo projectInfo in projectInfoList) {
      List<LegendAttribute> legendAttrs = new List();
      if (projectInfo.iconInfo != null) {
        List<String> dynamicValues = getProjectIconInfoValues(
            projectInfo.iconInfo, projectInfo.projectInfo);
        for (String value in dynamicValues) {
          for (LegendAttribute attrKey in legendAttributes.keys) {
            if (value.toLowerCase() == attrKey.label.toLowerCase()) {
              legendAttrs.add(attrKey);
            }
          }
        }
        if(legendAttrs.isEmpty){
          legendAttrs.add(new LegendAttribute(key: "source", label: "Default", url: projectInfo.iconInfo.defaultMarker));
        }
        projectInfo.iconInfo.markerAttributes = legendAttrs;
      }
    }
    return projectInfoList;
  }

  List<String> getProjectIconInfoValues(ProjectIconInfo iconInfo, Map<String, String> keyToValue) {
    List<String> dynamicUrlValues = List<String>();
    List keyList = StringUtils.getStringListFromDelimiter(
        ",", iconInfo.dynamicKeyName);
    if (keyList != null && keyList.isNotEmpty) {
      for (String key in keyList) {
        if(keyToValue[key] != null && keyToValue[key].isNotEmpty){
          dynamicUrlValues.add(keyToValue[key].toLowerCase().replaceAll(" ", ""));
        } else{
          return List();
        }
      }
    }
    return dynamicUrlValues;
  }
}

