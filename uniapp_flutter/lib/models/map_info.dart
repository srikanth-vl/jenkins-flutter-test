class MapInfo {

  String _mapEntityName;
  Map<String, String> _additionalInfo;
  String _downloadUrl;
  bool _hasToggle;
  String _iconUrl;

  MapInfo.fromJson(Map<String, dynamic> parsedJson) {
    _mapEntityName = parsedJson['map_entity_name'];
    _additionalInfo = parsedJson['map_entity_additional_info'];
    _downloadUrl = parsedJson['download_url'];
    _hasToggle = parsedJson['has_toggle'];
    _iconUrl = parsedJson['icon_url'];
  }

  String get iconUrl => _iconUrl;

  bool get hasToggle => _hasToggle;

  String get downloadUrl => _downloadUrl;

  Map<String, String> get additionalInfo => _additionalInfo;

  String get mapEntityName => _mapEntityName;
}