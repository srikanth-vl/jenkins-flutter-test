import 'package:flutter/material.dart';

import '../../../utils/string_utils.dart';
import '../../helpers/form_values.dart';
import '../../themes/uniapp_css.dart';
import '../../themes/text_theme.dart';

class FormDropdown extends StatefulWidget {
  final String id;
  final String title;
  final String initValue;
  final List multipleValues;

  FormDropdown({
    @required this.id,
    @required this.title,
    @required this.initValue,
    @required this.multipleValues,
  });

  @override
  State<StatefulWidget> createState() => _FormDropdownState();
}

class _FormDropdownState extends State<FormDropdown> {
  List<Map> _multipleValuesJson = new List();
  String _selectedValue;

  @override
  void initState() {
    super.initState();

    widget.multipleValues.forEach((f) {
      _multipleValuesJson.add({
        "id": f.value,
        "name": f.value,
        "expandable": f.expandable,
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    _selectedValue = (formMap.formValues[widget.id] != null &&
            formMap.formValues[widget.id].toString().isNotEmpty)
        ? formMap.formValues[widget.id]
        : (widget.initValue != null && widget.initValue.isNotEmpty)
            ? widget.initValue
            : null;

    formMap.formValues[widget.id] = _selectedValue;

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
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(8.0, 0.0, 8.0, 8.0),
              child: Container(
                width: double.maxFinite,
                child: DropdownButton<String>(
                  isExpanded: true,
                  hint: Text("Select"),
                  value: _selectedValue,
                  onChanged: (String newValue) {
                    setState(() {
                      _selectedValue = newValue;
                      formMap.formValues[widget.id] = newValue;
                    });
                  },
                  items: _multipleValuesJson.map((Map map) {
                    return DropdownMenuItem<String>(
                      value: map["id"].toString(),
                      child: Text(
                        StringUtils.getTranslatedString(map["name"].toString()),
                        style: UniappTextTheme.defaultTextStyle,
                      ),
                    );
                  }).toList(),
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
