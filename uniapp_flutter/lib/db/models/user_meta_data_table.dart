import '../dbSchemaConstants.dart';

class UserMetaDataTable {
  String _userId;
  String _password;
  String _token;
  int _lastNetworkSync;
  int _isLoggedin;
  int _lastLoginTs;
  String _userDetails;

  UserMetaDataTable(
      this._userId,
      this._password,
      this._token,
      this._lastNetworkSync,
      this._isLoggedin,
      this._lastLoginTs,
      this._userDetails);

  // Convert a UserMetaEntry object into a Map object
  Map<String, dynamic> toMap() {
    var map = Map<String, dynamic>();
    map[UserMetaEntry.COLUMN_USER_ID] = this._userId;
    map[UserMetaEntry.COLUMN_PASSWORD] = this._password;
    map[UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME] = this._lastNetworkSync;
    map[UserMetaEntry.COLUMN_IS_LOGGED_IN] = this._isLoggedin;
    map[UserMetaEntry.COLUMN_LAST_LOGIN_TS] = this._lastLoginTs;
    map[UserMetaEntry.COLUMN_USER_DETAILS] = this._userDetails;
    map[UserMetaEntry.COLUMN_TOKEN] = this._token;
    return map;
  }

  // Extract a config object from a Map object
  UserMetaDataTable.fromMapObject(Map<String, dynamic> map) {
    this._userId = map[UserMetaEntry.COLUMN_USER_ID];
    this._password = map[UserMetaEntry.COLUMN_PASSWORD];
    this._lastNetworkSync = map[UserMetaEntry.COLUMN_LAST_NETWORK_SYNC_TIME];
    this._isLoggedin = map[UserMetaEntry.COLUMN_IS_LOGGED_IN];
    this._lastLoginTs = map[UserMetaEntry.COLUMN_LAST_LOGIN_TS];
    this._userDetails = map[UserMetaEntry.COLUMN_USER_DETAILS];
    this._token = map[UserMetaEntry.COLUMN_TOKEN];
  }

  UserMetaDataTable.fromJson(Map<String, dynamic> parsedJson, String password) {
    if (parsedJson != null) {
      print("UserLoginData" + parsedJson.toString());
      _userId = parsedJson['userid'];
      _password = password;
      _token = parsedJson['tokenid'];
      _lastLoginTs = parsedJson['currenttime'];
      _lastNetworkSync = _lastLoginTs;
      _isLoggedin = 1;
      _userDetails = parsedJson['userdetails'];
    }
  }

  String get userDetails => _userDetails;

  set userDetails(String value) {
    _userDetails = value;
  }

  int get lastLoginTs => _lastLoginTs;

  set lastLoginTs(int value) {
    _lastLoginTs = value;
  }

  int get isLoggedin => _isLoggedin;

  set isLoggedin(int value) {
    _isLoggedin = value;
  }

  int get lastNetworkSync => _lastNetworkSync;

  set lastNetworkSync(int value) {
    _lastNetworkSync = value;
  }

  String get token => _token;

  set token(String value) {
    _token = value;
  }

  String get password => _password;

  set password(String value) {
    _password = value;
  }

  String get userId => _userId;

  set userId(String value) {
    _userId = value;
  }
}
