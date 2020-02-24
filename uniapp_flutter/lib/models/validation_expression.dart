class ValidationExpression {
  String expr;
  String errorMsg;

  ValidationExpression({this.expr, this.errorMsg});

  ValidationExpression.fromJson(Map<String, dynamic> json) {
    expr = json['expr'];
    errorMsg = json['error_msg'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['expr'] = this.expr;
    data['error_msg'] = this.errorMsg;
    return data;
  }
}