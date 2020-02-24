import 'field_value.dart';
class ProjectField {
  String key;
  FieldValue value;

  ProjectField({this.key, this.value});

  ProjectField.fromJson(Map<String, dynamic> json) {
    key = json['key'];
    value =
    json['value'] != null ? new FieldValue.fromJson(json['value']) : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['key'] = this.key;
    if (this.value != null) {
      data['value'] = this.value.toJson();
    }
    return data;
  }
}