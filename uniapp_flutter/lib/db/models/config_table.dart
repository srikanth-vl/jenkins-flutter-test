import '../dbSchemaConstants.dart';

class ConfigFile {
  String _userId;
  String _configName;
  String _content;
  int _configVersion;
  int _lastSyncTs;

  ConfigFile(this._userId, this._configName, this._content, this._configVersion,
      this._lastSyncTs);

  // Convert a ConfigFile object into a Map object
  Map<String, dynamic> toMap() {
    var map = Map<String, dynamic>();
    map[ConfigFilesEntry.COLUMN_USER_ID] = this._userId;
    map[ConfigFilesEntry.COLUMN_CONFIG_NAME] = this._configName;
    map[ConfigFilesEntry.COLUMN_CONFIG_FILE_CONTENT] = this._content;
    map[ConfigFilesEntry.COLUMN_CONFIG_VERSION] = this._configVersion;
    map[ConfigFilesEntry.COLUMN_CONFIG_LAST_SYNC_TS] = this._lastSyncTs;

    return map;
  }

  // Extract a config object from a Map object
  ConfigFile.fromMapObject(Map<String, dynamic> map) {
    this._userId = map[ConfigFilesEntry.COLUMN_USER_ID];
    this._configName = map[ConfigFilesEntry.COLUMN_CONFIG_NAME];
    this._content = map[ConfigFilesEntry.COLUMN_CONFIG_FILE_CONTENT];
    this._configVersion = map[ConfigFilesEntry.COLUMN_CONFIG_VERSION];
    this._lastSyncTs = map[ConfigFilesEntry.COLUMN_CONFIG_LAST_SYNC_TS];
  }

  set configName(String value) {
    _configName = value;
  }

  set lastSyncTs(int value) {
    _lastSyncTs = value;
  }

  set configVersion(int value) {
    _configVersion = value;
  }

  String get userId => _userId;

  set content(String value) {
    _content = value;
  }

  set userId(String value) {
    _userId = value;
  }

  String get configName => _configName;

  String get content => _content;

  int get configVersion => _configVersion;

  int get lastSyncTs => _lastSyncTs;
}
