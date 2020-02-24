class Expandable {
  int _type;
  String _iconUrl;
  String _subform;
  String _text;

//  Expandable({this.type, this.iconUrl, this.subform, this.text});

  Expandable.fromJson(Map<String, dynamic> json) {
    this._type = json['type'];
    this._iconUrl = json['icon_url'];
    this._subform = json['subform'];
    this._text = json['text'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['type'] = this._type;
    data['icon_url'] = this._iconUrl;
    data['subform'] = this._subform;
    data['text'] = this._text;
    return data;
  }

  String get text => _text;

  String get subform => _subform;

  String get iconUrl => _iconUrl;

  int get type => _type;
}
