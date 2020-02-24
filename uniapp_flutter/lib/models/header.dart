class Header {
  String _key;
  String _label;
  String _icon;
  bool _display;
  bool _submittable;
  String _uitype;

//  Header({this.key, this.label, this.icon, this.display, this.submittable});

  Header.fromJson(Map<String, dynamic> json) {
    _key = json['key'];
    _label = json['label'];
    _icon = json['icon'];
    _display = json['display'];
    _submittable = json['submittable'];
    _uitype = json['uitype'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['key'] = this._key;
    data['label'] = this._label;
    data['icon'] = this._icon;
    data['display'] = this._display;
    data['submittable'] = this._submittable;
    data['uitype'] = this._uitype;
    
    return data;
  }

  bool get submittable => _submittable;

  bool get display => _display;

  String get icon => _icon;

  String get label => _label;

  String get key => _key;

  String get uitype => _uitype;
}