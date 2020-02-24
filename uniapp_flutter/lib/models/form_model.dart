import 'package:flutter/foundation.dart';

class FormEditTextModel {
  final String editTextTitleLabel;
  final int editTextTitleFlex;
  final String editTextUnitsOrMeasurementLabel;
  final int editTextUnitsOrMeasurementFlex;
  final String editTextPresentValueLabel;
  final int editTextPresentValueFlex;
  final String editTextHint;
  final String initValue;

  FormEditTextModel({
    @required this.editTextTitleLabel,
    this.editTextUnitsOrMeasurementLabel,
    this.editTextPresentValueLabel,
    this.editTextHint,
    this.editTextTitleFlex,
    this.editTextUnitsOrMeasurementFlex,
    this.editTextPresentValueFlex,
    this.initValue,
  });
}

class FormHeaderModel {
  final String headerType;
  final String headerLabel;
  final int headerLabelFlex;
  final headerValue;
  final int headerValueFlex;

  FormHeaderModel({
    @required this.headerType,
    @required this.headerLabel,
    @required this.headerLabelFlex,
    @required this.headerValue,
    @required this.headerValueFlex,
  });
}
