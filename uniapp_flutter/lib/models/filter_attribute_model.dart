
import '../models/filter_config_model.dart';

class FilterAttributeModel{

  String _attributeName;
  FilterConfigModel _child;

  FilterAttributeModel(){

  }

  String get attributeName => _attributeName;

  set attributeName(String value) {
    _attributeName = value;
  }

  FilterConfigModel get child => _child;

  set child(FilterConfigModel value) {
    _child = value;
  }

  FilterAttributeModel.fromJson(Map<String, dynamic> json) {
    attributeName = json['attributeName'];
    child = json['child'] != null
        ? new FilterConfigModel.fromJson(json['child'])
        : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['attributeName'] = attributeName;
    if(child != null){
      data['child'] = child.toJson();
    }
    return data;
  }

}