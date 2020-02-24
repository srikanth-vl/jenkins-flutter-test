
class GpsValidation {

  String _type;
  String _source;
  String _key;
  int _radius;
  String _keyType;

  GpsValidation.fromJson(Map<String, dynamic> parsedJson) {

    _type = parsedJson['type'];
    _source = parsedJson['source'];
    _key = parsedJson['key'];
    _radius = parsedJson['radius'];
    _keyType = parsedJson['key_type'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();

    if(this._type != null && this._type.isNotEmpty){
      data['type'] = this._type;
    }
    if(this._radius != null){
      data['radius'] = this._radius;
    }
    if(this._source != null && this._source.isNotEmpty){
      data['source'] = this._source;
    }
    if(this._key != null && this._key.isNotEmpty){
      data['key'] = this._key;
    }
    if(this._keyType != null && this._keyType.isNotEmpty){
      data['key_type'] = this._keyType;
    }

    return data;
  }

  String get type => _type;

  set type(String value) {
    _type = value;
  }

  String get source => _source;

  String get keyType => _keyType;

  set keyType(String value) {
    _keyType = value;
  }

  int get radius => _radius;

  set radius(int value) {
    _radius = value;
  }

  String get key => _key;

  set key(String value) {
    _key = value;
  }

  set source(String value) {
    _source = value;
  }


}
