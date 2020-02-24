import 'package:flutter/material.dart';

import '../../helpers/form_values.dart';
import '../../themes/uniapp_css.dart';
import '../../themes/text_theme.dart';

class FormSwitch extends StatefulWidget {
  final String id;
  final String title;
  final String defaultValue;

  FormSwitch({
    @required this.id,
    @required this.title,
    @required this.defaultValue,
  });

  @override
  _FormSwitchState createState() => _FormSwitchState();
}

class _FormSwitchState extends State<FormSwitch> {
  bool _selectedValue;

  @override
  void initState() {
    super.initState();

    _selectedValue = (widget.defaultValue == "true") ? true : false;
    updateFormMapFormValue(_selectedValue);
  }

  void updateFormMapFormValue(bool _selecedValue) {
    formMap.formValues[widget.id] = _selectedValue;
  }

  @override
  Widget build(BuildContext context) {
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
              padding: const EdgeInsets.fromLTRB(8.0, 8.0, 8.0, 0.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: <Widget>[
                  Expanded(
                    child: Text(
                      widget.title ?? "",
                      style: UniappTextTheme.defaultWidgetStyle,
                    ),
                  ),
                  Container(
                    child: Switch.adaptive(
                      value: _selectedValue,
                      onChanged: (newValue) => setState(() {
                        _selectedValue = newValue;
                        updateFormMapFormValue(_selectedValue);
                      }),
                    ),
                  ),
                ],
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
