import 'offline_map_file.dart';
import 'map_marker.dart';

class MapConfig {
  int currenttime;
  List<OfflineMapFile> offlineMapFiles;
  String boundingBox;
  int minZoom;
  int maxZoom;
  Map<String,List<MapMarker>> mapMarkers;
  int version;
  String offlineMapSourceName;

  MapConfig({this.currenttime, this.offlineMapFiles, this.boundingBox, this.minZoom, this.maxZoom, this.mapMarkers, this.version, this.offlineMapSourceName});

  MapConfig.fromJson(Map<String, dynamic> json) {
    currenttime = json['currenttime'];
    offlineMapFiles = new List<OfflineMapFile>();
    if (json['offline_map_files'] != null) {
      json['offline_map_files'].forEach((v) { offlineMapFiles.add(OfflineMapFile.fromJson(v)); });
    }
    boundingBox = json['bounding_box'];
    minZoom = json['min_zoom'];
    maxZoom = json['max_zoom'];
    mapMarkers =  new Map();
    if(json['map_markers'] != null && json['map_markers'] != '') {
      json['map_markers'].forEach((key, value) {
        if(value !=  null && value != '') {
          List<MapMarker> markers= new List<MapMarker>();
          value.forEach((v) { markers.add(MapMarker.fromJson(v)); });
          mapMarkers[key] = markers;
        }
      });
    }
    version = json['version'];
    offlineMapSourceName = json['offline_map_source_name'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['currenttime'] = this.currenttime;
    if (this.offlineMapFiles != null) {
      data['offline_map_files'] = this.offlineMapFiles.map((v) => v.toJson()).toList();
    }
    data['bounding_box'] = this.boundingBox;
    data['min_zoom'] = this.minZoom;
    data['max_zoom'] = this.maxZoom;
    data['map_markers'] = Map<String,dynamic>();
    if (this.mapMarkers != null) {
      this.mapMarkers.forEach((appId, markerList){
        if(markerList != null && markerList.isNotEmpty) {
          List<MapMarker> markers = markerList;
          List markerJson = markers.map((v) => v.toJson()).toList();
          data['map_markers'][appId]= markerJson;
        }
      });
    }
    data['version'] = this.version;
    data['offline_map_source_name'] = this.offlineMapSourceName;
    return data;
  }
}