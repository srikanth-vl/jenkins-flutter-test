class Formatter {
  String date;
  String string;
  String double;
  String time;
  String timestamp;

  Formatter({this.date, this.string, this.double, this.time, this.timestamp});

  Formatter.fromJson(Map<String, dynamic> json) {
    date = json['date'];
    string = json['string'];
    double = json['double'];
    time = json['time'];
    timestamp = json['timestamp'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['date'] = this.date;
    data['string'] = this.string;
    data['double'] = this.double;
    data['time'] = this.time;
    data['timestamp'] = this.timestamp;
    return data;
  }
}