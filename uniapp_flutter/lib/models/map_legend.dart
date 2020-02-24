class MapLegend {

  bool _isLegendVisible;
  String _legendOrientation;

  MapLegend.fromJson(Map<String, dynamic> parsedJson) {
    _isLegendVisible = parsedJson['legend_visibility'];
    _legendOrientation = parsedJson['legend_orientation'];
  }

  String get legendOrientation => _legendOrientation;

  bool get isLegendVisible => _isLegendVisible;
}