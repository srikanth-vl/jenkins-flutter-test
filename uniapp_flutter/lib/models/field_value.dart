class FieldValue {
  String _label;
  String _value;
  String _uom;

//  FieldValue({this.label, this.value, this.uom});

  FieldValue.fromJson(Map<String, dynamic> json) {
    this._label = json['label'];
    this._value = json['value'];
    this._uom = json['uom'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['label'] = this._label;
    data['value'] = this._value;
    data['uom'] = this._uom;
    return data;
  }

  String get uom => _uom;

  String get value => _value;

  String get label => _label;

}