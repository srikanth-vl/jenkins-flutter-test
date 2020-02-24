import '../dbSchemaConstants.dart';

class EntityMetaDataTable {
  String _superAppId;
  String _appId;
  String _projectId;
  String _userId;
  String _parentEntity;
  String _entityName;
  String _elements;
  int _insertTimestamp;

  EntityMetaDataTable(
      this._superAppId,
      this._appId,
      this._projectId,
      this._userId,
      this._parentEntity,
      this._entityName,
      this._elements,
      this._insertTimestamp);

  // Convert a UserMetaEntry object into a Map object
  Map<String, dynamic> toMap() {
    var map = Map<String, dynamic>();
    map[EntityMetaEntry.COLUMN_SUPER_APP_ID] = this._superAppId;
    map[EntityMetaEntry.COLUMN_APP_ID] = this._appId;
    map[EntityMetaEntry.COLUMN_PROJECT_ID] = this._projectId;
    map[EntityMetaEntry.COLUMN_USER_ID] = this._userId;
    map[EntityMetaEntry.COLUMN_PARENT_ENTITY] = this._parentEntity;
    map[EntityMetaEntry.COLUMN_ENTITY_NAME] = this._entityName;
    map[EntityMetaEntry.COLUMN_ELEMENTS] = this._elements;
    map[EntityMetaEntry.COLUMN_INSERT_TIMESTAMP] = this._insertTimestamp;
    return map;
  }

  // Extract a config object from a Map object
  EntityMetaDataTable.fromMapObject(Map<String, dynamic> map) {
    this._superAppId = map[EntityMetaEntry.COLUMN_SUPER_APP_ID];
    this._appId = map[EntityMetaEntry.COLUMN_APP_ID];
    this._projectId = map[EntityMetaEntry.COLUMN_PROJECT_ID];
    this._userId = map[EntityMetaEntry.COLUMN_USER_ID];
    this._parentEntity = map[EntityMetaEntry.COLUMN_PARENT_ENTITY];
    this._entityName = map[EntityMetaEntry.COLUMN_ENTITY_NAME];
    this._elements = map[EntityMetaEntry.COLUMN_ELEMENTS];
    this._insertTimestamp = map[EntityMetaEntry.COLUMN_INSERT_TIMESTAMP];
  }

  int get insertTimestamp => _insertTimestamp;

  set insertTimestamp(int value) {
    _insertTimestamp = value;
  }

  String get elements => _elements;

  set elements(String value) {
    _elements = value;
  }

  String get entityName => _entityName;

  set entityName(String value) {
    _entityName = value;
  }

  String get parentEntity => _parentEntity;

  set parentEntity(String value) {
    _parentEntity = value;
  }

  String get userId => _userId;

  set userId(String value) {
    _userId = value;
  }

  String get projectId => _projectId;

  set projectId(String value) {
    _projectId = value;
  }

  String get appId => _appId;

  set appId(String value) {
    _appId = value;
  }

  String get superAppId => _superAppId;

  set superAppId(String value) {
    _superAppId = value;
  }
}
