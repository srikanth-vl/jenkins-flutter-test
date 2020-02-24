import 'legend_attribute.dart';

class ProjectIconInfo {
  String _defaultMarker;
  String _staticUrl;
  String _dynamicKeyName;
  List<LegendAttribute> _markerAttributes;

  ProjectIconInfo(){

  }

  ProjectIconInfo.fromJson(Map<String, dynamic> parsedJson) {
    if(parsedJson == null || parsedJson == '') {
      return;
    }
    _staticUrl = parsedJson['static_url'];
    _dynamicKeyName = parsedJson['dynamic_key_name'];
    _defaultMarker = parsedJson['default'];

    if(parsedJson['legend_attributes']  != null && parsedJson['legend_attributes'] != ''){
      markerAttributes = new List();
      parsedJson['legend_attributes'].forEach((value){
        if(value != null){
          markerAttributes.add(value);
        }
      });
    }
  }
  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['static_url'] = this._staticUrl;
    data['dynamic_key_name'] = this._dynamicKeyName;
    data['default'] = this._defaultMarker;
    if(markerAttributes != null){
      data['legend_attributes'] = markerAttributes;
    }
    return data;
  }

  String get defaultMarker => _defaultMarker;

  set staticUrl(String value) {
    _staticUrl = value;
  }

  set defaultMarker(String value) {
    _defaultMarker = value;
  }

  String get dynamicKeyName => _dynamicKeyName;

  String get staticUrl => _staticUrl;

  set dynamicKeyName(String value) {
    _dynamicKeyName = value;
  }

  List<LegendAttribute> get markerAttributes => _markerAttributes;

  set markerAttributes(List<LegendAttribute> value) {
    _markerAttributes = value;
  }
}