
import '../models/filter_attribute_model.dart';

class FilterConfigModel {

  String _key;
  List<FilterAttributeModel> _filterElements;

  FilterConfigModel(){

  }

  String get key => _key;

  set key(String value) {
    _key = value;
  }

  List<FilterAttributeModel> get filterElements => _filterElements;

  set filterElements(List<FilterAttributeModel> value) {
    _filterElements = value;
  }

  FilterConfigModel.fromJson(Map<String, dynamic> json) {
    key = json['key'];
    if (json['filter_attribute_models'] != null) {
      filterElements = new List<FilterAttributeModel>();
      json['filter_attribute_models'].forEach((v) { filterElements.add(new FilterAttributeModel.fromJson(v)); });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['key'] = this.key;
    if (this.filterElements != null && this.filterElements.isNotEmpty) {
      data['filter_attribute_models'] = this.filterElements.map((v) => v.toJson()).toList();
    }
    return data;
  }
}

