import '../models/dyamic_forms.dart';

import '../models/project_icon_info.dart';
import 'data_formatter.dart';

class SubApp {
  String _parentAppId;
  String _name;
  String _icon;
  String _appId;
  int _clientExpiryDays;
  int _expiryAlertThreshold;
  String _sortType;
  int _order;
  List<String> _filteringAttributes;
  List<String> _groupingAttributes;
  bool _displayProjectIcon;
  DynamicForms _filteringForm;
  List<Map<String, String>> _groupEntitiesList;
  String _desc;
  Formatter _formatter;
  ProjectIconInfo _projectIconInfo;
  String _status;
  List<Map<String, String>> _groupEntities;
  Map<String, String> _mapOverLayInfo;
  bool _filterEnabled;
  bool _fetchEntityMetaData;
  bool _mapEnabled;
  bool _searchEnabled;

  SubApp.fromJson(Map<String, dynamic> parsedJson) {
    _mapOverLayInfo = Map<String, String>();
    if(parsedJson['map_overlay_info'] !=  null) {
      parsedJson['map_overlay_info'].forEach((key, value){
        _mapOverLayInfo[key] = value;
      });
    }
    _parentAppId = parsedJson['parent_app_id'];
    _name = parsedJson['name'];
    _icon = parsedJson['icon'];
    _appId = parsedJson['app_id'];
    _clientExpiryDays = parsedJson['client_expiry_interval'];
    _expiryAlertThreshold = parsedJson['alert_interval'];
    _sortType = parsedJson['sort_type'];
    _order = parsedJson['order'];
    _filteringAttributes = parsedJson['attributes'] == null
        ? []
        : parsedJson['attributes'].cast<String>();
    _groupingAttributes = parsedJson['grouping_attributes'] == null
        ? []
        : parsedJson['grouping_attributes'].cast<String>();
    _displayProjectIcon = parsedJson['display_project_icon'] ?? null;
    _filteringForm = parsedJson['filtering_form'] != null
        ? new DynamicForms.fromJson(parsedJson['filtering_form'])
        : null;
    _groupEntitiesList = new List();
    if (parsedJson['group_entities'] != null) {
      _groupEntities = new List<Map<String, String>>();
      parsedJson['group_entities'].forEach((value) {
        if (value != null && value != '') {
          Map<String, String> entities = new Map();
          value.forEach((key, v) {
            entities[key] = v;
          });
          _groupEntities.add(entities);
        }
      });
    }
    _desc = parsedJson['desc'];
    _formatter = parsedJson['formatter'] != null
        ? new Formatter.fromJson(parsedJson['formatter'])
        : null;
    _projectIconInfo = parsedJson['proj_icon_config'] != null
        ? new ProjectIconInfo.fromJson(parsedJson['proj_icon_config'])
        : null;
    _status = parsedJson['status'];
    _filterEnabled = parsedJson['filter_enabled'] ?? null;
    _fetchEntityMetaData = parsedJson['fetch_entity_meta_data'] == null ? false : parsedJson['fetch_entity_meta_data'];
    _mapEnabled = parsedJson['map_enabled'] ?? null;
    _searchEnabled = parsedJson['search_enabled'] ?? null;
  }

  bool get fetchEntityMetaData => _fetchEntityMetaData;

  set fetchEntityMetaData(bool value) {
    _fetchEntityMetaData = value;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    if (this._filteringForm != null) {
      data['filtering_form'] = this._filteringForm.toJson();
    }
    data['icon'] = this._icon;
    if (this._projectIconInfo != null) {
      data['proj_icon_config'] = this._projectIconInfo.toJson();
    }

    data['grouping_attributes'] = this._groupingAttributes;
    if (this._formatter != null) {
      data['formatter'] = this._formatter.toJson();
    }
    data['sort_type'] = this._sortType;
    data['display_project_icon'] = this._displayProjectIcon;
    data['parent_app_id'] = this._parentAppId;
    data['client_expiry_interval'] = this._clientExpiryDays;
    data['name'] = this._name;

    data['attributes'] = this._filteringAttributes;
    data['app_id'] = this._appId;
    if (this._groupEntities != null) {
      data['group_entities'] = this._groupEntities;
    }
    data['desc'] = this._desc;
    data['map_overlay_info']=_mapOverLayInfo;
    data['filter_enabled'] = this._filterEnabled;
    data['fetch_entity_meta_data'] = this._fetchEntityMetaData;
    data['map_enabled'] = this._mapEnabled;
    data['search_enabled'] = this._searchEnabled;
    return data;
  }

  List<Map<String, String>> get groupEntities => _groupEntities;

  set groupEntities(List<Map<String, String>> value) {
    _groupEntities = value;
  }

  String get status => _status;

  ProjectIconInfo get projectIconInfo => _projectIconInfo;

  Formatter get formatter => _formatter;

  String get desc => _desc;

  List<Map<String, String>> get groupEntitiesList => _groupEntitiesList;

  DynamicForms get filteringForm => _filteringForm;

  bool get displayProjectIcon => _displayProjectIcon;

  List<String> get groupingAttributes => _groupingAttributes;

  List<String> get filteringAttributes => _filteringAttributes;

  int get order => _order;

  String get sortType => _sortType;

  int get expiryAlertThreshold => _expiryAlertThreshold;

  int get clientExpiryDays => _clientExpiryDays;

  String get appId => _appId;

  String get icon => _icon;

  String get name => _name;

  String get parentAppId => _parentAppId;

  Map<String, String> get mapOverLayInfo => _mapOverLayInfo;

  set mapOverLayInfo(Map<String, String> value) {
    _mapOverLayInfo = value;
  }

  bool get filterEnabled => _filterEnabled;

  set filterEnabled(bool value) {
    _filterEnabled = value;
  }

  bool get mapEnabled => _mapEnabled;

  set mapEnabled(bool value) {
    _mapEnabled = value;
  }

  bool get searchEnabled => _searchEnabled;

  set searchEnabled(bool value) {
    _searchEnabled = value;
  }


}
