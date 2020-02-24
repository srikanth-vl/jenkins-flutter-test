import 'legend_attribute.dart';

class MapMarker {

  String mapEntityName;
  Map<String,String> mapEntityAdditionalInfo;
  String downloadUrl;
  bool hasToggle;
  String iconUrl;
  List<LegendAttribute> legendAttributes;

  MapMarker({this.mapEntityName, this.mapEntityAdditionalInfo, this.downloadUrl,
    this.hasToggle, this.iconUrl, this.legendAttributes});

  MapMarker.fromJson(Map<String, dynamic> json) {
    mapEntityName = json['map_entity_name'];
    if(json['map_entity_additional_info']!= null && json['map_entity_additional_info'] != '') {
      mapEntityAdditionalInfo =  new Map();
      json['map_entity_additional_info'].forEach((key,value){
        mapEntityAdditionalInfo[key] = value;
      });
    }

    downloadUrl = json['download_url'];
    hasToggle = json['has_toggle'];
    iconUrl = json['icon_url'];

    if(json['legend_attributes']  != null && json['legend_attributes'] != ''){
      legendAttributes = new List();
      json['legend_attributes'].forEach((value){
        if(value != null){
          legendAttributes.add(LegendAttribute.fromJson(value));
        }
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['map_entity_name'] = this.mapEntityName;
    if (this.mapEntityAdditionalInfo != null) {
      data['map_entity_additional_info'] = this.mapEntityAdditionalInfo;
    }
    data['download_url'] = this.downloadUrl;
    data['has_toggle'] = this.hasToggle;
    data['icon_url'] = this.iconUrl;

    if(legendAttributes != null){
      data['legend_attributes'] = legendAttributes;
    }
    return data;
  }
}