import 'map_project_info.dart';
import 'map_config.dart';

class LoadMapInfo {
  List<MapProjectInfo> mapProjectInfoList;
  MapConfig mapConfig;
  String appId;
  String iconBasePath;

  LoadMapInfo(List<MapProjectInfo> mapProjectInfoList, MapConfig mapConfig, String appId, String iconBasePath) {
    this.mapConfig = mapConfig;
    this.mapProjectInfoList = mapProjectInfoList;
    this.appId = appId;
    this.iconBasePath = iconBasePath;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    if (this.mapProjectInfoList != null) {
      data['map_project_info_list'] = this.mapProjectInfoList.map((v) => v.toJson()).toList();
    }
    data['map_config'] = this.mapConfig.toJson();
    data['appid'] = this.appId;
    data['icon_base_path'] = this.iconBasePath;
    return data;
  }
}