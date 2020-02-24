import 'package:Bluis/db/models/project_master_data_table.dart';
import 'package:flutter/cupertino.dart';

import 'gps_validation.dart';

class RouteParameters {
  BuildContext _context;

  String _appId,
      _projectId,
      _formActionType,
      _sortType,
      _groupingKey,
      _groupingValue,
      _geoTaggingWidgetId,
      _routeName;
  GpsValidation _gpsValidation;
  String _projLat;
  String _projLon;

  // NOTE: To replace a route, assign willReplace to be true.
  // For popping multiple routes, assign willReplace and popUntil both to be true.
  // By default, both carry false as values, so by default, new route will be pushed.
  // See ../utils/screen_navigate_utils.dart to see usage of the instances of the class.

  bool _willReplace = false, _willPopUntil = false;

  List<String> _groupingAttributes = List();
  List<ProjectMasterDataTable> _projectMasterDataTableList;

  set context(BuildContext c) {
    _context = c;
  }

  set appId(String id) {
    _appId = id;
  }

  set projectId(String id) {
    _projectId = id;
  }

  set formActionType(String type) {
    _formActionType = type;
  }

  set sortType(String type) {
    _sortType = type;
  }

  set groupingKey(String key) {
    _groupingKey = key;
  }

  set groupingValue(String value) {
    _groupingValue = value;
  }

  set geoTaggingWidgetId(String id) {
    _geoTaggingWidgetId = id;
  }

  set willReplace(bool value) {
    _willReplace = value;
  }

  set willPopUntil(bool value) {
    _willPopUntil = value;
  }

  set groupingAttributes(List<String> list) {
    _groupingAttributes = list;
  }

  set projectMasterDataTableList(List<ProjectMasterDataTable> projectMasterDataTableList) {
    _projectMasterDataTableList = projectMasterDataTableList;
  }

  set routeName(String name) {
    _routeName = name;
  }

  BuildContext get context => _context;

  String get appId => _appId;

  String get projectId => _projectId;

  String get sortType => _sortType;

  String get formActionType => _formActionType;

  String get groupingKey => _groupingKey;

  String get groupingValue => _groupingValue;

  String get geoTaggingWidgetId => _geoTaggingWidgetId;

  bool get willReplace => _willReplace;

  bool get willPopUntil => _willPopUntil;

  List<String> get groupingAttributes => _groupingAttributes;

  List<ProjectMasterDataTable> get projectMasterDataTableList =>
      _projectMasterDataTableList;

  String get routeName => _routeName;

  String get projLon => _projLon;

  set projLon(String value) {
    _projLon = value;
  }

  String get projLat => _projLat;

  set projLat(String value) {
    _projLat = value;
  }

  GpsValidation get gpsValidation => _gpsValidation;

  set gpsValidation(GpsValidation value) {
    _gpsValidation = value;
  }
}
