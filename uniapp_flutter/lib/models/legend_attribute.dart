
class LegendAttribute{

  String key;
  String label;
  String url;

  LegendAttribute({this.key, this.label, this.url});

  LegendAttribute.fromJson(Map<String, dynamic> json) {
    this.key = json['attribute_key'];
    this.label = json['attribute_label'];
    this.url = json['attribute_url'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['attribute_key'] = this.key;
    data['attribute_label'] = this.label;
    data['attribute_url'] = this.url;
    return data;
  }
}