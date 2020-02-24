class Urlendpoints {
  String rootconfig;
  String login;
  String projecttype;
  String projectlist;
  String syncdata;
  String logout;

  Urlendpoints(
      {this.rootconfig,
        this.login,
        this.projecttype,
        this.projectlist,
        this.syncdata,
        this.logout});

  Urlendpoints.fromJson(Map<String, dynamic> json) {
    rootconfig = json['rootconfig'];
    login = json['login'];
    projecttype = json['projecttype'];
    projectlist = json['projectlist'];
    syncdata = json['syncdata'];
    logout = json['logout'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['rootconfig'] = this.rootconfig;
    data['login'] = this.login;
    data['projecttype'] = this.projecttype;
    data['projectlist'] = this.projectlist;
    data['syncdata'] = this.syncdata;
    data['logout'] = this.logout;
    return data;
  }
}