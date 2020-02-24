class Selectablewindow {
  String past;
  String future;
  String select;
  String interval;

  Selectablewindow({this.past, this.future, this.select, this.interval});

  Selectablewindow.fromJson(Map<String, dynamic> json) {
    past = json['past'];
    future = json['future'];
    select = json['select'];
    interval = json['interval'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['past'] = this.past;
    data['future'] = this.future;
    data['select'] = this.select;
    data['interval'] = this.interval;
    return data;
  }
}