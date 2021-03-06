import 'uniapp_form.dart';
class Forms {
  String initialFormId;
  List<UniappForm> form;

  Forms({this.initialFormId, this.form});

  Forms.fromJson(Map<String, dynamic> json) {
    initialFormId = json['initial_form_id'];
    if (json['form'] != null) {
      form = new List<UniappForm>();
      json['form'].forEach((v) { form.add(new UniappForm.fromJson(v)); });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['initial_form_id'] = this.initialFormId;
    if (this.form != null) {
      data['form'] = this.form.map((v) => v.toJson()).toList();
    }
    return data;
  }
}