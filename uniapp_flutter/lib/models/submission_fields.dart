class SubmissionField {
  String key;
  String val;
  String dt;
  String uom;
  String ui;

  SubmissionField({this.key, this.val, this.dt, this.uom, this.ui});

  SubmissionField.fromJson(Map<String, dynamic> json) {
    key = json['key'];
    val = json['val'];
    dt = json['dt'];
    uom = json['uom'];
    ui = json['ui'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['key'] = this.key;
    data['val'] = this.val;
    data['dt'] = this.dt;
    data['uom'] = this.uom;
    data['ui'] = this.ui;
    return data;
  }
}