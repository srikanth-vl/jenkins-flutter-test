import 'project.dart';
class ProjectListConfiguration {
  List<String> _types;
  int _currenttime;
  List<Project> _projects;
  bool _showMap;
  String _userid;

//  ProjectListConfiguration(
//      {this.types, this.currenttime, this.projects, this.showmap, this.userid});

  ProjectListConfiguration.fromJson(Map<String, dynamic> json) {
    this._types = json['types'].cast<String>();
    this._currenttime = json['currenttime'];
    if (json['projects'] != null) {
      this._projects = new List<Project>();
      json['projects'].forEach((v) {
        this._projects.add(new Project.fromJson(v));
      });
    }
    this._showMap = json['showmap'];
    this._userid = json['userid'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['types'] = this.types;
    data['currenttime'] = this.currenttime;
    if (this.projects != null) {
      data['projects'] = this.projects.map((v) => v.toJson()).toList();
    }
    data['showmap'] = this.showmap;
    data['userid'] = this.userid;
    return data;
  }

  String get userid => _userid;

  bool get showmap => _showMap;

  List<Project> get projects => _projects;

  int get currenttime => _currenttime;

  List<String> get types => _types;
}