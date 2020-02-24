import '../dbSchemaConstants.dart';
import 'dart:convert';
import '../../models/submission_fields.dart';

class ProjectSubmission {
  String _appId;
  String _userId;
  String _userType;
  String _formId;
  int _timeStamp;
  String _projectId;
  List<SubmissionField> _submissionObject;
  String _submissionApi;
  String _mdInstanceId;
  int _submissionStatus;
  String _response;
  int _serverSyncTs;
  int _updateRetryCount;
  Map<String, String> _additionalProperties;

  ProjectSubmission(
      this._appId,
      this._userId,
      this._userType,
      this._formId,
      this._timeStamp,
      this._projectId,
      this._submissionObject,
      this._submissionApi,
      this._mdInstanceId,
      this._submissionStatus,
      this._response,
      this._serverSyncTs,
      this._updateRetryCount,
      this._additionalProperties);

  // Convert a UserMetaEntry object into a Map object
  Map<String, dynamic> toMap() {
    var map = Map<String, dynamic>();
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID] = this._appId;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID] =
        this._userId;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE] =
        this._userType;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID] =
        this._formId;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP] =
        this._timeStamp;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID] =
        this._projectId;
    if(this._submissionObject != null && this._submissionObject.isNotEmpty) {
      List<Map<String, dynamic>> fieldlist = this._submissionObject.map((v) =>
          v.toJson()).toList();
      map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS] = jsonEncode(fieldlist);
    }
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API] =
        this._submissionApi;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID] =
        this._mdInstanceId;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS] =
        this._submissionStatus;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE] =
        this._response;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS] =
        this._serverSyncTs;
    map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT] =
        this._updateRetryCount;
    map[ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES] = jsonEncode(this._additionalProperties);

    return map;
  }

  // Extract a config object from a Map object
  ProjectSubmission.fromMapObject(Map<String, dynamic> map) {
    this._appId = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_APP_ID];
    this._userId = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_ID];
    this._userType = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_USER_TYPE];
    this._formId = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FORM_ID];
    this._timeStamp = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_TIMESTAMP];
    this._projectId = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_PROJECT_ID];
    List<SubmissionField> fields  = List();
    if(map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS] != null && map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS] != '') {
      (jsonDecode(map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_FIELDS])).forEach((a) {
        SubmissionField projectField  =  SubmissionField.fromJson(a);
        fields.add(projectField);
      });
    } ;
    this._submissionObject = fields;

    this._submissionApi = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_API];
    this._mdInstanceId = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_MD_INSTANCE_ID];
    this._submissionStatus = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_UPLOAD_STATUS];
    this._response = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RESPONSE];
    this._serverSyncTs = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_SERVER_SYNC_TS];
    this._updateRetryCount = map[ProjectSubmissionEntry.COLUMN_PROJECT_SUBMISSION_RETRY_COUNT];
    this._additionalProperties = Map();
    if(map[ProjectSubmissionEntry .COLUMN_ADDITIONAL_PROPERTIES] != null) {
      jsonDecode(map[ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES]).forEach((key, value){
        this._additionalProperties[key] = value;
      });
    }
    map[ProjectSubmissionEntry.COLUMN_ADDITIONAL_PROPERTIES];
  }

  Map<String, String> get additionalProperties => _additionalProperties;

  set additionalProperties(Map<String, String> value) {
    _additionalProperties = value;
  }

  int get updateRetryCount => _updateRetryCount;

  set updateRetryCount(int value) {
    _updateRetryCount = value;
  }

  int get serverSyncTs => _serverSyncTs;

  set serverSyncTs(int value) {
    _serverSyncTs = value;
  }

  String get response => _response;

  set response(String value) {
    _response = value;
  }

  int get submissionStatus => _submissionStatus;

  set submissionStatus(int value) {
    _submissionStatus = value;
  }

  String get mdInstanceId => _mdInstanceId;

  set mdInstanceId(String value) {
    _mdInstanceId = value;
  }

  String get submissionApi => _submissionApi;

  set submissionApi(String value) {
    _submissionApi = value;
  }

  List<SubmissionField> get submissionObject => _submissionObject;

  set submissionObject(List<SubmissionField> value) {
    _submissionObject = value;
  }

  String get projectId => _projectId;

  set projectId(String value) {
    _projectId = value;
  }

  int get timeStamp => _timeStamp;

  set timeStamp(int value) {
    _timeStamp = value;
  }

  String get formId => _formId;

  set formId(String value) {
    _formId = value;
  }

  String get userType => _userType;

  set userType(String value) {
    _userType = value;
  }

  String get userId => _userId;

  set userId(String value) {
    _userId = value;
  }

  String get appId => _appId;

  set appId(String value) {
    _appId = value;
  }
}
