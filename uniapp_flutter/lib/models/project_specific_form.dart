import 'forms.dart';

class ProjectSpecificForm {
  int formversion;
  int isActive;
  String forminstanceid;
  String mdinstanceid;
  Forms forms;

  ProjectSpecificForm({
    this.formversion,
    this.isActive,
    this.forminstanceid,
    this.mdinstanceid,
    this.forms,
  });

  ProjectSpecificForm.fromJson(Map<String, dynamic> json) {
    formversion = json['formversion'];
    isActive = json['is_active'];
    forminstanceid = json['forminstanceid'];
    mdinstanceid = json['mdinstanceid'];
    forms = json['forms'] != null ? new Forms.fromJson(json['forms']) : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['formversion'] = this.formversion;
    data['is_active'] = this.isActive;
    data['forminstanceid'] = this.forminstanceid;
    data['mdinstanceid'] = this.mdinstanceid;
    if (this.forms != null) {
      data['forms'] = this.forms.toJson();
    }
    return data;
  }
}
