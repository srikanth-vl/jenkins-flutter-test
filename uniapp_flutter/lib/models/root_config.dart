import 'dart:convert';
import '../models/sub_app.dart';

class RootConfig {
  int _currentServerTime;
  String _userId;
  int _version;
  List<SubApp> _applications;

  RootConfig.fromJson(Map<String, dynamic> json) {
    _currentServerTime = json['currenttime'];
    if (json['application'] != null) {
      _applications = new List<SubApp>();
      json['application'].forEach((v) { _applications.add(new SubApp.fromJson(v)); });
    }
    _userId = json['user_id'];
    _version = json['version'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['currenttime'] = this._currentServerTime;
    if (this._applications != null) {
      data['application'] = this._applications.map((v) => v.toJson()).toList();
    }
    data['user_id'] = this.userId;
    data['version'] = this.version;
    return data;
  }

  int get currentServerTime => _currentServerTime;

  String get userId => _userId;

  int get version => _version;

  List<SubApp> get config => _applications;
}