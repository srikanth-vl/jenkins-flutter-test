import '../models/project_icon_info.dart';

import 'uniapp_validation.dart';
import 'project_field.dart';
class Project {
  String projectname;
  String priority;
  String long;
  String userType;
  int lastSubDate;
  String lastSyncTs;
  String extProjId;
  Map<String,String> attributes;
  String state;
  UniappValidation validations;
  List<ProjectField> fields;
  String projectid;
  String lat;
  String bBoxValidation;
  String centroidValidation;
  ProjectIconInfo projectIconInfo;

  Project(
      {this.projectname,
        this.priority,
        this.long,
        this.userType,
        this.lastSubDate,
        this.lastSyncTs,
        this.extProjId,
        this.attributes,
        this.state,
        this.validations,
        this.fields,
        this.projectid,
        this.lat});

  Project.fromJson(Map<String, dynamic> json) {
    projectname = json['projectname'];
    priority = json['priority'];
    long = json['long'];
    userType = json['user_type'];
    lastSubDate = json['last_sub_date'];
    lastSyncTs = json['last_sync_ts']  == 0 || json['last_sync_ts'] ==  null ?  null : json['last_sync_ts'];
    extProjId = json['ext_proj_id'];
    attributes  = new Map();
    if(json['attributes'] !=  null && json['attributes']!= '') {
      json['attributes'].forEach((key, value){
        attributes[key] = value;
      });
    }
    state = json['state'];
    validations = json['validations'] != null
        ? new UniappValidation.fromJson(json['validations'])
        : null;
    projectIconInfo = json['project_icon'] !=  null ? new ProjectIconInfo.fromJson(json['project_icon']) : null;
    if (json['fields'] != null) {
      fields = new List<ProjectField>();
      json['fields'].forEach((v) {
        fields.add(new ProjectField.fromJson(v));
      });
    }
    projectid = json['projectid'];
    lat = json['lat'];
    bBoxValidation = json['bbox'];
    centroidValidation = json['circle_validation'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['projectname'] = this.projectname;
    data['priority'] = this.priority;
    data['long'] = this.long;
    data['user_type'] = this.userType;
    data['last_sub_date'] = this.lastSubDate;
    data['last_sync_ts'] = this.lastSyncTs;
    data['ext_proj_id'] = this.extProjId;
    data['attributes'] = this.attributes;
    data['state'] = this.state;
    if (this.validations != null) {
      data['validations'] = this.validations.toJson();
    }
    if (this.projectIconInfo != null) {
      data['project_icon'] = this.projectIconInfo.toJson();
    }
    if (this.fields != null) {
      data['fields'] = this.fields.map((v) => v.toJson()).toList();
    }
    data['projectid'] = this.projectid;
    data['lat'] = this.lat;
    data['bbox'] = bBoxValidation ;
    data['circle_validation'] = centroidValidation ;
    return data;
  }
}