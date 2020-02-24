import 'uniapp_response_result.dart';
class UniappResponse {
  String id;
  UniappResponseResult result;

  UniappResponse({this.id, this.result});

  UniappResponse.fromJson(Map<String, dynamic> json) {
    id = json['id'];
    result =
    json['result'] != null ? new UniappResponseResult.fromJson(json['result']) : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['id'] = this.id;
    if (this.result != null) {
      data['result'] = this.result.toJson();
    }
    return data;
  }
}
