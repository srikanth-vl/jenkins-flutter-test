import 'package:flutter/material.dart';

import '../../helpers/form_values.dart';
import '../../../models/form_model.dart';
import '../../themes/uniapp_css.dart';
import '../../themes/text_theme.dart';

class FormEditText extends StatefulWidget {
  final String id;
  final FormEditTextModel formEditTextModel;
  FormEditText({this.id, this.formEditTextModel});

  @override
  _FormEditTextState createState() => _FormEditTextState();
}

class _FormEditTextState extends State<FormEditText> {
  final controller = TextEditingController();

  @override
  void initState() {
    super.initState();

//    controller.text = (widget.formEditTextModel.initValue != null &&
//            widget.formEditTextModel.initValue.isNotEmpty)
//        ? widget.formEditTextModel.initValue
//        : null;

    controller.addListener(() {
      formMap.formValues[widget.id] = controller.text;
    });
  }

  @override
  Widget build(BuildContext context) {
//    controller.text = (widget.formEditTextModel.initValue != null &&
//            widget.formEditTextModel.initValue.isNotEmpty)
//        ? widget.formEditTextModel.initValue
//        : null;

  controller.text = (formMap.formValues[widget.id] != null &&
                    formMap.formValues[widget.id].toString().isNotEmpty)
                ? formMap.formValues[widget.id]
                : widget.formEditTextModel.initValue ?? "";

    return Padding(
      padding: UniappCSS.smallHorizontalPadding,
      child: Container(
        decoration: BoxDecoration(
          borderRadius: UniappCSS.widgetBorderRadius,
          border: UniappCSS.widgetBorder,
        ),
        child: Column(
          children: <Widget>[
            Padding(
              padding: UniappCSS.smallHorizontalAndVerticalPadding,
              child: Row(
                children: <Widget>[
                  Expanded(
                    flex: widget.formEditTextModel.editTextTitleFlex,
                    child: Text(
                      widget.formEditTextModel.editTextTitleLabel,
                      style: UniappTextTheme.defaultWidgetStyle,
                      textAlign: TextAlign.left,
                    ),
                  ),
                  Expanded(
                    flex:
                        widget.formEditTextModel.editTextUnitsOrMeasurementFlex,
                    child: Text(
                      widget.formEditTextModel.editTextUnitsOrMeasurementLabel,
                      style: UniappTextTheme.defaultTextStyle,
                      textAlign: TextAlign.left,
                    ),
                  ),
                  Expanded(
                    flex: widget.formEditTextModel.editTextPresentValueFlex,
                    child: Text(
                      widget.formEditTextModel.editTextPresentValueLabel,
                      style: UniappTextTheme.defaultTextStyle,
                      textAlign: TextAlign.left,
                    ),
                  ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(8.0, 0.0, 8.0, 0.0),
              child: TextField(
                obscureText: false,
                controller: controller,
                style: UniappTextTheme.defaultTextStyle,
                decoration: InputDecoration(
                  hintText: widget.formEditTextModel.editTextHint ?? "",
                  // contentPadding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
                  border: OutlineInputBorder(
                    borderRadius: UniappCSS.widgetBorderRadius,
                  ),
                ),
              ),
            ),
            SizedBox(
              height: 4.0,
            ),
          ],
        ),
      ),
    );
  }
}
