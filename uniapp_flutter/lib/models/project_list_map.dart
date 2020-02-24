import '../models/map_info.dart';
import '../models/map_legend.dart';

class ProjectListMap {
  List<MapInfo> _layers;
  List<MapInfo> _markers;
  MapLegend _legend;

  ProjectListMap.fromJson(Map<String, dynamic> parsedJson) {
    _layers = parsedJson['map_layers_info'];
    _markers = parsedJson['map_markers_info'];
    _legend = parsedJson['map_legend'];
  }

  MapLegend get legend => _legend;

  List<MapInfo> get markers => _markers;

  List<MapInfo> get layers => _layers;
}