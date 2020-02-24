import 'dart:convert';

class UniappResponseResult {
  bool success;
  int status;
  String message;
  String metadata;
  String content;

  UniappResponseResult(
      {this.success, this.status, this.message, this.metadata, this.content});

  UniappResponseResult.fromJson(Map<String, dynamic> json) {
    success = json['success'];
    status = json['status'];
    message = json['message'];
    metadata = json['metadata'];
    content = jsonEncode(json['content']);
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['success'] = this.success;
    data['status'] = this.status;
    data['message'] = this.message;
    data['metadata'] = this.metadata;
    if (this.content != null) {
      data['content'] = this.content;
    }
    return data;
  }
}
