import 'bridge_value.dart';
class FormBridge {
  String _key;
  String _label;
  List<BridgeValue> _bridgeValues;

//  FormBridge({this.key, this.label, this.bridgevalues});

  FormBridge.fromJson(Map<String, dynamic> json) {
    _key = json['key'];
    _label = json['label'];
    if (json['bridgevalues'] != null) {
      _bridgeValues = new List<BridgeValue>();
      json['bridgevalues'].forEach((v) { _bridgeValues.add(new BridgeValue.fromJson(v)); });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['key'] = this._key;
    data['label'] = this._label;
    if (this._bridgeValues != null) {
      data['bridgevalues'] = this._bridgeValues.map((v) => v.toJson()).toList();
    }
    return data;
  }

  List<BridgeValue> get bridgeValues => _bridgeValues;

  String get label => _label;

  String get key => _key;

}