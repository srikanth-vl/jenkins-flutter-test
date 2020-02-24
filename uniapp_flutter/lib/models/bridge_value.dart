import 'expandable.dart';
class BridgeValue{
  String _value;
  Expandable _expandable;
  String _bgColor;
  List<String> _mandatoryFields;

  BridgeValue({value, expandable, bgColor, mandatoryFields});

  BridgeValue.fromJson(Map<String, dynamic> json) {
    this._value = json['value'];
    this._expandable = json['expandable'] != null ? new Expandable.fromJson(json['expandable']) : null;
    this._bgColor = json['bg-color'];
    if (json['mandatory_fields'] != null) {
      this._mandatoryFields = new List<String>();
      json['mandatory_fields'].forEach((v) { this._mandatoryFields.add(v.toString()); });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['value'] = this._value;
    if (this._expandable != null) {
      data['expandable'] = this._expandable.toJson();
    }
    data['bg-color'] = this._bgColor;
    if (this._mandatoryFields != null) {
      data['mandatory_fields'] = this._mandatoryFields.map((v) => v).toList();
    }
    return data;
  }

  String get value => _value;

  Expandable get expandable => _expandable;

  String get bgColor => _bgColor;

  List<String> get mandatoryFields => _mandatoryFields;
}