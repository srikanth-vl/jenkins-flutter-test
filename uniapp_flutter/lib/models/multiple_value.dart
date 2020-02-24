import 'expandable.dart';

class MultipleValue {
  String _value;
  Expandable _expandable;

  MultipleValue(
    this._value,
    this._expandable,
  );

  MultipleValue.fromJson(Map<String, dynamic> json) {
    _value = json['value'];
    _expandable = json['expandable'] != null
        ? new Expandable.fromJson(json['expandable'])
        : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['value'] = this._value;
    if (this._expandable != null) {
      data['expandable'] = this._expandable.toJson();
    }
    return data;
  }

String get value => _value;
Expandable get expandable => _expandable;

}
