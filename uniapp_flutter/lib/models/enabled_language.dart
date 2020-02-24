class EnabledLanguage {
  String name;
  String locale;

  EnabledLanguage({this.name, this.locale});

  EnabledLanguage.fromJson(Map<String, dynamic> json) {
    name = json['name'];
    locale = json['locale'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['name'] = this.name;
    data['locale'] = this.locale;
    return data;
  }
}