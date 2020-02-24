import 'expandable.dart';
class Button {
  String _key;
  String _label;
  Expandable _expandable;
  String _api;

//  Button({this.key, this.label, this.expandable, this.api});

  Button.fromJson(Map<String, dynamic> json) {
    this._key = json['key'];
    this._label = json['label'];
    this._expandable = json['expandable'] != null ? new Expandable.fromJson(json['expandable']) : null;
    this._api = json['api'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['key'] = this._key;
    data['label'] = this._label;
    if (this._expandable != null) {
      data['expandable'] = this._expandable.toJson();
    }
    data['api'] = this._api;
    return data;
  }

  Expandable get expandable => _expandable;

  String get label => _label;

  String get key => _key;

  String get api => _api;

}