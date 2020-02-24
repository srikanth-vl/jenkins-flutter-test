import 'validation_expression.dart';
class UniappValidation {
  bool mandatory;
  List<ValidationExpression> validationExpression;
//  List<Api> api;

  UniappValidation({this.mandatory, this.validationExpression});

  UniappValidation.fromJson(Map<String, dynamic> json) {
    mandatory = json['mandatory'];
    this.validationExpression = new List<ValidationExpression>();
    if (json['expr'] != null) {
      json['expr'].forEach((v) { validationExpression.add(new ValidationExpression.fromJson(v)); });
    }
//    if (json['api'] != null) {
//      api = new List<Api>();
//      json['api'].forEach((v) { api.add(new Api.fromJson(v)); });
//    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['mandatory'] = this.mandatory;
    if (this.validationExpression != null) {
      data['expr'] = this.validationExpression.map((v) => v.toJson()).toList();
    }
//    if (this.api != null) {
//      data['api'] = this.api.map((v) => v.toJson()).toList();
//    }
    return data;
  }
}