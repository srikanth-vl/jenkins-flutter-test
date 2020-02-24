class ProjectSubmissionResult {

  int _statusCode;
  bool _isSuccessful;
  String _message;

  int get statusCode => _statusCode;

  set statusCode(int value) {
    _statusCode = value;
  }

  bool get isSuccessful => _isSuccessful;

  String get message => _message;

  set message(String value) {
    _message = value;
  }

  ProjectSubmissionResult(this._statusCode, this._isSuccessful, this._message);

  set isSuccessful(bool value) {
    _isSuccessful = value;
  }


  ProjectSubmissionResult.fromJson(Map<String, dynamic> json) {
    _statusCode = json['status'];
    _isSuccessful = json['success'];
    _message = json['message'];

  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['status'] = _statusCode;
    data['success'] =_isSuccessful;
    data['message'] = _message;
    return data;
  }
}
