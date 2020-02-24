class TableRowStructure {
  int weightsum;
  bool repeat;
  List<TableColumnComponent> components;

  TableRowStructure({this.weightsum, this.repeat, this.components});

  TableRowStructure.fromJson(Map<String, dynamic> json) {
    weightsum = json['weightsum'];
    repeat = json['repeat'];
    if (json['components'] != null) {
      components = new List<TableColumnComponent>();
      json['components'].forEach((v) {
        components.add(new TableColumnComponent.fromJson(v));
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['weightsum'] = this.weightsum;
    data['repeat'] = this.repeat;
    if (this.components != null) {
      data['components'] = this.components.map((v) => v.toJson()).toList();
    }
    return data;
  }
}

class TableColumnComponent {
  int weight;
  String aligned;
  String uitype;
  String datatype;
  String label;
  bool editable;
  bool display;
  String key;

  TableColumnComponent(
      {this.weight,
        this.aligned,
        this.uitype,
        this.datatype,
        this.label,
        this.editable,
        this.display,
        this.key});

  TableColumnComponent.fromJson(Map<String, dynamic> json) {
    weight = json['weight'];
    aligned = json['aligned'];
    uitype = json['uitype'];
    datatype = json['datatype'];
    label = json['label'];
    editable = json['editable'];
    display = json['display'];
    key = json['key'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['weight'] = this.weight;
    data['aligned'] = this.aligned;
    data['uitype'] = this.uitype;
    data['datatype'] = this.datatype;
    data['label'] = this.label;
    data['editable'] = this.editable;
    data['display'] = this.display;
    data['key'] = this.key;
    return data;
  }
}