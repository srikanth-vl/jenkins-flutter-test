import 'project_specific_form.dart';

class ProjectTypeConfiguration {
  int currenttime;
  String userId;
  String projecttype;
  Map<String, Map<String, ProjectSpecificForm>> content;
  String deptname;

  ProjectTypeConfiguration({
    this.currenttime,
    this.userId,
    this.projecttype,
    this.content,
    this.deptname,
  });

  ProjectTypeConfiguration.fromJson(Map<String, dynamic> json) {
    currenttime = json['currenttime'];
    userId = json['user_id'];
    projecttype = json['projecttype'];
    content = Map();
    if (json['content'] != null && json['content'] != '') {
      Map<String, Map<String, ProjectSpecificForm>> projectToTypeFormMap =
          Map<String, Map<String, ProjectSpecificForm>>();
      json['content'].forEach((projectId, actionFormConfig) {
        Map<String, ProjectSpecificForm> typeToformMap =
            Map<String, ProjectSpecificForm>();
        if (actionFormConfig != null && actionFormConfig != '') {
          actionFormConfig.forEach((formType, form) {
            if (form != null) {
              typeToformMap[formType] = ProjectSpecificForm.fromJson(form);
            }
          });
          projectToTypeFormMap[projectId] = typeToformMap;
        }
      });
      content = projectToTypeFormMap;
    }
    this.deptname = json['deptname'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['currenttime'] = this.currenttime;
    data['user_id'] = this.userId;
    data['projecttype'] = this.projecttype;
    if (this.content != null) {
      Map<String, Map<String, ProjectSpecificForm>> proejctTotypeFormMap =
          this.content;
      Map jsonMap = Map();
      proejctTotypeFormMap.forEach((projectId, actionFormConfig) {
        Map<String, ProjectSpecificForm> formTypeFormMap = actionFormConfig;
        Map formTypeToformJson = Map();
        formTypeFormMap.forEach((formType, form) {
          formTypeToformJson[formType] = form.toJson();
        });
        jsonMap[projectId] = formTypeToformJson;
      });
    }
    data['deptname'] = this.deptname;
    return data;
  }
}
