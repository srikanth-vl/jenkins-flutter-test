import '../models/project_icon_info.dart';

class MapProjectInfo {
  String projectname;
  String long;
  String projectid;
  String lat;
  String projectIcon;
  bool showProjectMarker;
  ProjectIconInfo iconInfo;
  Map<String, String> projectInfo;

  MapProjectInfo({
    this.projectname,
    this.long,
    this.projectid,
    this.lat,
    this.projectIcon,
    this.projectInfo,
  });

  MapProjectInfo.fromJson(Map<String, dynamic> json) {
    projectname = json['projectname'];
    long = json['long'];
    projectid = json['projectid'];
    lat = json['lat'];
    projectIcon = json['projecticon'];
    showProjectMarker = json['showProjectMarker'] ?? null;
    projectInfo = new Map();

    iconInfo = json['icon_info'] != null
        ? new ProjectIconInfo.fromJson(json['icon_info'])
        : null;

    if (json['projectinfo'] != null && json['projectinfo'] != '') {
      json['projectinfo'].forEach((key, value) {
        if (value != null && value != '') {
          projectInfo[key] = value;
        }
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['projectname'] = this.projectname;
    data['long'] = this.long;
    data['projectid'] = this.projectid;
    data['lat'] = this.lat;
    data['projecticon'] = this.projectIcon;
    data['showProjectMarker'] = this.showProjectMarker;
    if (this.iconInfo != null) {
      data['icon_info'] = this.iconInfo.toJson();
    }
    data['projectinfo'] = Map();
    if (this.projectInfo != null) {
      this.projectInfo.forEach((key, value) {
        if (value != null && value.isNotEmpty) {
          data['projectinfo'][key] = value;
        }
      });
    }
    return data;
  }
}
