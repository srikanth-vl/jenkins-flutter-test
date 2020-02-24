class LocalizationConfig {
  int _currentServerTime;
  int _version;
  String _config;

  LocalizationConfig.fromJson(Map<String, dynamic> parsedJson) {
    _currentServerTime = parsedJson['currenttime'];
    _version = parsedJson['version'];
    _config = parsedJson['config'];
  }

  int get currentServerTime => _currentServerTime;

  int get version => _version;

  String get config => _config;
}