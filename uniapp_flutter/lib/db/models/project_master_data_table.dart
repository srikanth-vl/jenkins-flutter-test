import '../dbSchemaConstants.dart';
import '../../models/project_field.dart';
import 'dart:convert';
import '../../models/project_icon_info.dart';

class ProjectMasterDataTable {
  String _projectAppId;
  String _projectUserId;
  String _projectId;
  String _projectName;
  String _projectLat;
  String _projectLon;
  String _projectBBox;
  String _projectCircle;
  String _projectLastSubDate;
  String _projectState;
  String _projectExtProjId;
  List<ProjectField> _projectFields;
  String _projectUserType;
  String _projectValidations;
  ProjectIconInfo _projectIcon;
  int _projectServerSyncTs;
  int _projectShowMap;
  Map<String, String> _projectFilteringDimensionNames;
  String _projectGroupingDimentionNames;
  int _projectLastUpdatedTs;
  int _projectAssignedStatus;
  String _projectFieldsString;

  Map<String, String> _keyToValueMap;
  ProjectMasterDataTable(
      this._projectAppId,
      this._projectUserId,
      this._projectId,
      this._projectName,
      this._projectLat,
      this._projectLon,
      this._projectBBox,
      this._projectCircle,
      this._projectLastSubDate,
      this._projectState,
      this._projectExtProjId,
      this._projectFields,
      this._projectUserType,
      this._projectValidations,
      this._projectIcon,
      this._projectServerSyncTs,
      this._projectShowMap,
      this._projectFilteringDimensionNames,
      this._projectGroupingDimentionNames,
      this._projectLastUpdatedTs,
      this._projectAssignedStatus);

  Map<String, dynamic> toMap() {
    var map = Map<String, dynamic>();
    map[ProjectTableEntry.COLUMN_PROJECT_APP_ID] = this._projectAppId;
    map[ProjectTableEntry.COLUMN_PROJECT_USER_ID] = this._projectUserId;
    map[ProjectTableEntry.COLUMN_PROJECT_ID] = this._projectId;
    map[ProjectTableEntry.COLUMN_PROJECT_NAME] = this._projectName;
    map[ProjectTableEntry.COLUMN_PROJECT_LAT] = this._projectLat;
    map[ProjectTableEntry.COLUMN_PROJECT_LON] = this._projectLon;
    map[ProjectTableEntry.COLUMN_PROJECT_BBOX] = this._projectBBox;
    map[ProjectTableEntry.COLUMN_PROJECT_CIRCLE_VALIDATION] =
        this._projectCircle;
    map[ProjectTableEntry.COLUMN_PROJECT_LAST_SUB_DATE] =
        this._projectLastSubDate;
    map[ProjectTableEntry.COLUMN_PROJECT_STATE] = this._projectState;
    map[ProjectTableEntry.COLUMN_PROJECT_EXTERNAL_PROJECT_ID] =
        this._projectExtProjId;
    if (this._projectFields != null && this._projectFields.isNotEmpty) {
      List<Map<String, dynamic>> fieldlist =
          this._projectFields.map((v) => v.toJson()).toList();
      map[ProjectTableEntry.COLUMN_PROJECT_FIELDS] = jsonEncode(fieldlist);
    }
    map[ProjectTableEntry.COLUMN_PROJECT_USER_TYPE] = this._projectUserType;
    map[ProjectTableEntry.COLUMN_PROJECT_VALIDATIONS] =
        this._projectValidations;
    map[ProjectTableEntry.COLUMN_PROJECT_ICON] = this._projectIcon;
    if (this.projectIcon != null) {
      map[ProjectTableEntry.COLUMN_PROJECT_ICON] =
          jsonEncode(this._projectIcon.toJson());
    }
    map[ProjectTableEntry.COLUMN_SERVER_SYNC_TS] = this._projectServerSyncTs;
    map[ProjectTableEntry.COLUMN_SHOW_MAP] = this._projectShowMap;
    if(this._projectFilteringDimensionNames != null && this._projectFilteringDimensionNames.isNotEmpty) {
      map[ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES] =
          jsonEncode(this._projectFilteringDimensionNames);
      print("project data filter dimen ::  ${jsonEncode(this._projectFilteringDimensionNames)}");
    }
    map[ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES] =
        this._projectGroupingDimentionNames;
    map[ProjectTableEntry.COLUMN_PROJECT_LAST_UPDATED_TS] =
        this._projectLastUpdatedTs;
    map[ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS] =
        this._projectAssignedStatus;
    return map;
  }

  // Extract a config object from a Map object
  ProjectMasterDataTable.fromMapObject(Map<String, dynamic> map) {
    this._projectFieldsString = map[ProjectTableEntry.COLUMN_PROJECT_FIELDS] ?? '';

    this._projectAppId = map[ProjectTableEntry.COLUMN_PROJECT_APP_ID];
    this._projectUserId = map[ProjectTableEntry.COLUMN_PROJECT_USER_ID];
    this._projectId = map[ProjectTableEntry.COLUMN_PROJECT_ID];
    this._projectName = map[ProjectTableEntry.COLUMN_PROJECT_NAME];
    this._projectLat = map[ProjectTableEntry.COLUMN_PROJECT_LAT];
    this._projectLon = map[ProjectTableEntry.COLUMN_PROJECT_LON];
    this._projectBBox = map[ProjectTableEntry.COLUMN_PROJECT_BBOX];
    this._projectCircle =
        map[ProjectTableEntry.COLUMN_PROJECT_CIRCLE_VALIDATION];
    this._projectLastSubDate =
        map[ProjectTableEntry.COLUMN_PROJECT_LAST_SUB_DATE];
    this._projectState = map[ProjectTableEntry.COLUMN_PROJECT_STATE];
    this._projectExtProjId =
        map[ProjectTableEntry.COLUMN_PROJECT_EXTERNAL_PROJECT_ID];
    List<ProjectField> fields = List();
    if (map[ProjectTableEntry.COLUMN_PROJECT_FIELDS] != null &&
        map[ProjectTableEntry.COLUMN_PROJECT_FIELDS] != '') {
      jsonDecode(map[ProjectTableEntry.COLUMN_PROJECT_FIELDS]).forEach((a) {
        ProjectField projectField = ProjectField.fromJson(a);
        fields.add(projectField);
      });
    }
    this._projectFields = fields;
    this._projectUserType = map[ProjectTableEntry.COLUMN_PROJECT_USER_TYPE];
    this._projectValidations =
        map[ProjectTableEntry.COLUMN_PROJECT_VALIDATIONS];
    this._projectIcon = map[ProjectTableEntry.COLUMN_PROJECT_ICON] != null
        ? ProjectIconInfo.fromJson(
            jsonDecode(map[ProjectTableEntry.COLUMN_PROJECT_ICON]))
        : null;
    this._projectServerSyncTs = map[ProjectTableEntry.COLUMN_SERVER_SYNC_TS];
    this._projectShowMap = map[ProjectTableEntry.COLUMN_SHOW_MAP];

    if (map[ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES] != null &&
        map[ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES]
            .toString()
            .isNotEmpty) {
      this.projectFilteringDimensionNames = new Map<String, String>();
      jsonDecode(map[ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES]).forEach((key, value) {
        this.projectFilteringDimensionNames[key] = value;
      });
    }
//    this._projectFilteringDimensionNames =
//    map[ProjectTableEntry.COLUMN_FILTERING_DIMENSION_VALUES];
    this._projectGroupingDimentionNames =
        map[ProjectTableEntry.COLUMN_GROUPING_DIMENSION_VALUES];
    this._projectLastUpdatedTs =
        map[ProjectTableEntry.COLUMN_PROJECT_LAST_UPDATED_TS];
    this._projectAssignedStatus =
        map[ProjectTableEntry.COLUMN_PROJECT_ASSINGED_STATUS];
  }

  int get projectAssignedStatus => _projectAssignedStatus;

  set projectAssignedStatus(int value) {
    _projectAssignedStatus = value;
  }

  int get projectLastUpdatedTs => _projectLastUpdatedTs;

  set projectLastUpdatedTs(int value) {
    _projectLastUpdatedTs = value;
  }

  String get projectGroupingDimentionNames => _projectGroupingDimentionNames;

  set projectGroupingDimentionNames(String value) {
    _projectGroupingDimentionNames = value;
  }

  Map<String, String> get projectFilteringDimensionNames =>
      _projectFilteringDimensionNames;

  set projectFilteringDimensionNames(Map<String, String> value) {
    _projectFilteringDimensionNames = value;
  }

  int get projectShowMap => _projectShowMap;

  set projectShowMap(int value) {
    _projectShowMap = value;
  }

  int get projectServerSyncTs => _projectServerSyncTs;

  set projectServerSyncTs(int value) {
    _projectServerSyncTs = value;
  }

  ProjectIconInfo get projectIcon => _projectIcon;

  set projectIcon(ProjectIconInfo value) {
    _projectIcon = value;
  }

  String get projectValidations => _projectValidations;

  set projectValidations(String value) {
    _projectValidations = value;
  }

  String get projectUserType => _projectUserType;

  set projectUserType(String value) {
    _projectUserType = value;
  }

  List<ProjectField> get projectFields => _projectFields;

  set projectFields(List<ProjectField> value) {
    _projectFields = value;
  }

  String get projectExtProjId => _projectExtProjId;

  set projectExtProjId(String value) {
    _projectExtProjId = value;
  }

  String get projectState => _projectState;

  set projectState(String value) {
    _projectState = value;
  }

  String get projectLastSubDate => _projectLastSubDate;

  set projectLastSubDate(String value) {
    _projectLastSubDate = value;
  }

  String get projectCircle => _projectCircle;

  set projectCircle(String value) {
    _projectCircle = value;
  }

  String get projectBBox => _projectBBox;

  set projectBBox(String value) {
    _projectBBox = value;
  }

  String get projectLon => _projectLon;

  set projectLon(String value) {
    _projectLon = value;
  }

  String get projectLat => _projectLat;

  set projectLat(String value) {
    _projectLat = value;
  }

  String get projectName => _projectName;

  set projectName(String value) {
    _projectName = value;
  }

  String get projectId => _projectId;

  set projectId(String value) {
    _projectId = value;
  }

  String get projectUserId => _projectUserId;

  set projectUserId(String value) {
    _projectUserId = value;
  }

  String get projectAppId => _projectAppId;

  set projectAppId(String value) {
    _projectAppId = value;
  }

  String get projectFieldsString => _projectFieldsString;

  set projectFieldsString(String value) {
    _projectFieldsString = value;
  }

  Map<String, String> getKeyToValueMap() {
    if (_keyToValueMap == null) {
      Map<String, String> keyToValue = new Map();
      if (_projectFields != null && _projectFields.isNotEmpty) {
        for (ProjectField projectField in _projectFields) {
          keyToValue[projectField.key] =
              projectField.value != null ? projectField.value.value : null;
        }
      }
      _keyToValueMap = keyToValue;
    }
    return _keyToValueMap;
  }
}
