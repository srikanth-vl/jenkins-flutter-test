class ServiceFrequency {
  int appmetaconfig;
  int rootconfig;
  int projecttype;
  int projectlist;
  int servererrsync;
  int media;
  int mapconfig;
  int localizationConfig;

  ServiceFrequency(
      {this.appmetaconfig,
        this.rootconfig,
        this.projecttype,
        this.projectlist,
        this.servererrsync,
        this.media,
        this.mapconfig,
        this.localizationConfig});

  ServiceFrequency.fromJson(Map<String, dynamic> json) {
    appmetaconfig = json['appmetaconfig'];
    rootconfig = json['rootconfig'];
    projecttype = json['projecttype'];
    projectlist = json['projectlist'];
    servererrsync = json['servererrsync'];
    media = json['media'];
    mapconfig = json['mapconfig'];
    localizationConfig = json['localizationConfig'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['appmetaconfig'] = this.appmetaconfig;
    data['rootconfig'] = this.rootconfig;
    data['projecttype'] = this.projecttype;
    data['projectlist'] = this.projectlist;
    data['servererrsync'] = this.servererrsync;
    data['media'] = this.media;
    data['mapconfig'] = this.mapconfig;
    data['localizationConfig'] = this.localizationConfig;
    return data;
  }
}