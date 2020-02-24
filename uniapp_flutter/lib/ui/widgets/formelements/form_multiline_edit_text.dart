import 'package:flutter/material.dart';

import '../../themes/uniapp_css.dart';
import '../../../models/form_model.dart';
import '../../helpers/form_values.dart';
import '../../themes/text_theme.dart';

class FormMultiEditText extends StatefulWidget {
  final String id;
  final FormEditTextModel formEditTextModel;
  FormMultiEditText({this.id, this.formEditTextModel});

  @override
  _FormMultiEditTextState createState() => _FormMultiEditTextState();
}

class _FormMultiEditTextState extends State<FormMultiEditText> {
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
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
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
              padding: const EdgeInsets.fromLTRB(8.0, 0.0, 8.0, 8.0),
              child: TextField(
                keyboardType: TextInputType.multiline,
                maxLines: null,
                minLines: 3,
                obscureText: false,
                controller: controller,
                style: UniappTextTheme.defaultTextStyle,
                decoration: InputDecoration(
                  hintText: widget.formEditTextModel.editTextHint,
                  border: OutlineInputBorder(
                    borderRadius: UniappCSS.widgetBorderRadius,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
