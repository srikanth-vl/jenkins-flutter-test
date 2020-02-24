class Colorscheme {
  String coloraccent;
  String colorprimary;
  String colorprimarydark;

  Colorscheme({this.coloraccent, this.colorprimary, this.colorprimarydark});

  Colorscheme.fromJson(Map<String, dynamic> json) {
    coloraccent = json['coloraccent'];
    colorprimary = json['colorprimary'];
    colorprimarydark = json['colorprimarydark'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['coloraccent'] = this.coloraccent;
    data['colorprimary'] = this.colorprimary;
    data['colorprimarydark'] = this.colorprimarydark;
    return data;
  }
}
