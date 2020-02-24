class EntityMetaDataConfiguration {
  List<Entities> entities;

  EntityMetaDataConfiguration({this.entities});

  EntityMetaDataConfiguration.fromJson(Map<String, dynamic> json) {
    if (json['entities'] != null) {
      entities = new List<Entities>();
      json['entities'].forEach((v) {
        entities.add(new Entities.fromJson(v));
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    if (this.entities != null) {
      data['entities'] = this.entities.map((v) => v.toJson()).toList();
    }
    return data;
  }
}

class Entities {
  String superAppId;
  String appId;
  String projectId;
  String userId;
  String parentEntity;
  String entityName;
  int insertTs;
  String elements;

  Entities(
      {this.superAppId,
        this.appId,
        this.projectId,
        this.userId,
        this.parentEntity,
        this.entityName,
        this.insertTs,
        this.elements});

  Entities.fromJson(Map<String, dynamic> json) {
    superAppId = json['super_app_id'];
    appId = json['app_id'];
    projectId = json['project_id'];
    userId = json['user_id'];
    parentEntity = json['parent_entity'];
    entityName = json['entity_name'];
    insertTs = json['insert_ts'];
    elements = json['elements'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['super_app_id'] = this.superAppId;
    data['app_id'] = this.appId;
    data['project_id'] = this.projectId;
    data['user_id'] = this.userId;
    data['parent_entity'] = this.parentEntity;
    data['entity_name'] = this.entityName;
    data['insert_ts'] = this.insertTs;
    data['elements'] = this.elements;
    return data;
  }


}