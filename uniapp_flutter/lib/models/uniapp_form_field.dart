import 'table_structure.dart';
import 'multiple_value.dart';
import 'uniapp_validation.dart';
import 'selectable_window.dart';
import 'expandable.dart';
import 'gps_validation.dart';

class UniappFormField {
  String key;
  String label;
  String uitype;
  String datatype;
  bool editable;
  bool display;
  String uom;
  Selectablewindow selectablewindow;
  UniappValidation validations;
  Expandable expandable;
  String defaultValue;
  List<MultipleValue> multipleValues;
  int maxChars;
  String filteringKey;
  int maxValue;
  GpsValidation gpsValidation;
  List<TableRowStructure> tableRowStructure;

  UniappFormField({this.key, this.label, this.uitype, this.datatype, this.editable, this.display, this.uom,
    this.selectablewindow, this.validations, this.expandable, this.defaultValue, this.multipleValues, this.maxChars,
    this.filteringKey, this.maxValue, this.gpsValidation});

  UniappFormField.fromJson(Map<String, dynamic> json) {
    key = json['key'];
    label = json['label'];
    uitype = json['uitype'];
    datatype = json['datatype'];
    editable = json['editable'];
    display = json['display'];
    uom = json['uom'];
    selectablewindow = json['selectablewindow'] != null ? new Selectablewindow.fromJson(json['selectablewindow']) : null;
    validations = json['validations'] != null ? new UniappValidation.fromJson(json['validations']) : null;
    expandable = json['expandable'] != null ? new Expandable.fromJson(json['expandable']) : null;
    defaultValue = json['default'];
    multipleValues = new List<MultipleValue>();
    if (json['multiplevalues'] != null) {
      json['multiplevalues'].forEach((v) { multipleValues.add(MultipleValue.fromJson(v)); });
    }
    maxChars = json['max_chars'];
    filteringKey = json['filtering_key'];
    maxValue = json['max'];
    if(json['gps_validation'] != null && json['gps_validation'].toString().isNotEmpty) {
      gpsValidation = GpsValidation.fromJson(json['gps_validation']);
    }
    tableRowStructure = new List<TableRowStructure>();
    if (json['tablestructure'] != null) {
      json['tablestructure'].forEach((v) {
        tableRowStructure.add(new TableRowStructure.fromJson(v));
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['key'] = this.key;
    data['label'] = this.label;
    data['uitype'] = this.uitype;
    data['datatype'] = this.datatype;
    data['editable'] = this.editable;
    data['display'] = this.display;
    data['uom'] = this.uom;
    if (this.selectablewindow != null) {
      data['selectablewindow'] = this.selectablewindow.toJson();
    }
    if (this.validations != null) {
      data['validations'] = this.validations.toJson();
    }
    if (this.expandable != null) {
      data['expandable'] = this.expandable.toJson();
    }
    data['default'] = this.defaultValue;
    if (this.multipleValues != null) {
      data['multiplevalues'] = this.multipleValues.map((v) => v.toJson()).toList();
    }
    data['max_chars'] = this.maxChars;
    data['filtering_key'] = this.filteringKey;
    data['max'] = this.maxValue;
    data['gps_validation'] = this.gpsValidation;
    if (this.tableRowStructure != null) {
      data['tablestructure'] =
          this.tableRowStructure.map((v) => v.toJson()).toList();
    }
    return data;
  }
}