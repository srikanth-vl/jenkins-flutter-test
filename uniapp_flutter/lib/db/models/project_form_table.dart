import '../dbSchemaConstants.dart';

class ProjectFormTable {
  String _userId;
  String _appId;
  String _projectId;
  String _formType;
  String _formData;
  String _mdInstanceId;
  int _formVersion;

  ProjectFormTable(this._userId, this._appId, this._projectId,
      this._formType, this._formData, this._mdInstanceId, this._formVersion);

  // Convert a UserMetaEntry object into a Map object
  Map<String, dynamic> toMap() {
    var map = Map<String, dynamic>();
    map[ProjectFormTableEntry.COLUMN_USER_ID] = this._userId;
    map[ProjectFormTableEntry.COLUMN_APP_ID] = this._appId;
    map[ProjectFormTableEntry.COLUMN_PROJECT_ID] = this._projectId;
    map[ProjectFormTableEntry.COLUMN_FORM_TYPE] = this._formType;
    map[ProjectFormTableEntry.COLUMN_FORM_DATA] = this._formData;
    map[ProjectFormTableEntry.COLUMN_MD_INSTANCE_ID] = this._mdInstanceId;
    map[ProjectFormTableEntry.COLUMN_VERSION] = this._formVersion;
    return map;
  }

  // Extract a config object from a Map object
  ProjectFormTable.fromMapObject(Map<String, dynamic> map) {
    this._userId = map[ProjectFormTableEntry.COLUMN_USER_ID];
    this._appId = map[ProjectFormTableEntry.COLUMN_APP_ID];
    this._projectId = map[ProjectFormTableEntry.COLUMN_PROJECT_ID];
    this._formType = map[ProjectFormTableEntry.COLUMN_FORM_TYPE];
    this._formData = map[ProjectFormTableEntry.COLUMN_FORM_DATA];
    this._mdInstanceId = map[ProjectFormTableEntry.COLUMN_MD_INSTANCE_ID];
    this._formVersion = map[ProjectFormTableEntry.COLUMN_VERSION];
  }

  int get formVersion => _formVersion;

  set formVersion(int value) {
    _formVersion = value;
  }

  String get mdInstanceId => _mdInstanceId;

  set mdInstanceId(String value) {
    _mdInstanceId = value;
  }

  String get formData => _formData;

  set formData(String value) {
    _formData = value;
  }

  String get formType => _formType;

  set formType(String value) {
    _formType = value;
  }

  String get projectId => _projectId;

  set projectId(String value) {
    _projectId = value;
  }

  String get appId => _appId;

  set appId(String value) {
    _appId = value;
  }

  String get userId => _userId;

  set userId(String value) {
    _userId = value;
  }
}
