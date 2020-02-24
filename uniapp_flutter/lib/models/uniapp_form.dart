import 'header.dart';
import 'button.dart';
import 'form_bridge.dart';
import 'uniapp_form_field.dart';

class UniappForm {
  String formid;
  String name;
  String title;
  String subtitle;
  FormBridge formBridge;
  List<Header> header;
  List<UniappFormField> fields;
  List<Button> buttons;

  UniappForm({
    this.formid,
    this.name,
    this.title,
    this.subtitle,
    this.formBridge,
    this.header,
    this.fields,
    this.buttons,
  });

  UniappForm.fromJson(Map<String, dynamic> json) {
    this.formid = json['formid'];
    this.name = json['name'];
    this.title = json['title'];
    this.subtitle = json['subtitle'];
    this.formBridge = json['form_bridge'] != null
        ? new FormBridge.fromJson(json['form_bridge'])
        : null;
    if (json['header'] != null) {
      this.header = new List<Header>();
      json['header'].forEach((v) {
        this.header.add(new Header.fromJson(v));
      });
    }
    if (json['fields'] != null) {
      this.fields = new List<UniappFormField>();
      json['fields'].forEach((v) {
        this.fields.add(new UniappFormField.fromJson(v));
      });
    }
    if (json['buttons'] != null) {
      this.buttons = new List<Button>();
      json['buttons'].forEach((v) {
        this.buttons.add(new Button.fromJson(v));
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['formid'] = this.formid;
    data['name'] = this.name;
    data['title'] = this.title;
    data['subtitle'] = this.subtitle;
    if (this.formBridge != null) {
      data['form_bridge'] = this.formBridge.toJson();
    }
    if (this.header != null) {
      data['header'] = this.header.map((v) => v.toJson()).toList();
    }
    if (this.fields != null) {
      data['fields'] = this.fields.map((v) => v.toJson()).toList();
    }
    if (this.buttons != null) {
      data['buttons'] = this.buttons.map((v) => v.toJson()).toList();
    }
    return data;
  }
}
