class UserLoginData {
  String _username;
  String _password;
  String _user_id;
  String _token;
  int _timestamp;
  bool _is_logged_in;

  UserLoginData.fromJson(Map<String, dynamic> parsedJson, String password) {
    if(parsedJson !=  null) {
      print("YOLOOOOOOO" + parsedJson.toString());
      _username = parsedJson['userid'];
      _password = password;
      _user_id = parsedJson['userid'];
      _token = parsedJson['tokenid'];
      _timestamp = parsedJson['currenttime'];
      _is_logged_in = true;
    }
  }

  String get username => _username;

  String get password => _password;

  String get user_id => _user_id;

  String get token => _token;

  int get timestamp => _timestamp;

  bool get is_logged_in => _is_logged_in;
}