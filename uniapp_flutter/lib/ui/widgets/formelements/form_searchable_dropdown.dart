import 'package:flutter/material.dart';

import '../../helpers/form_values.dart';
import '../../../utils/string_utils.dart';
import '../../themes/uniapp_css.dart';
import '../global/searchable_dropdown.dart';
import '../../themes/text_theme.dart';

class FormSearchableDropDown extends StatefulWidget {
  final String id;
  final String title;
  final List multipleValues;

  FormSearchableDropDown({
    @required this.id,
    @required this.title,
    @required this.multipleValues,
  });

  @override
  _FormSearchableDropDownState createState() => _FormSearchableDropDownState();
}

class _FormSearchableDropDownState extends State<FormSearchableDropDown> {
  String _selectedValue;
  List<Map> _multipleValuesJson = new List();

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
                      widget.title,
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
                child: SearchableDropdown(
                  items: _multipleValuesJson.map((Map map) {
                    return DropdownMenuItem<String>(
                      value: map["id"].toString(),
                      child: Text(
                        StringUtils.getTranslatedString(map["name"].toString()),
                        style: UniappTextTheme.defaultTextStyle,
                      ),
                    );
                  }).toList(),
                  isExpanded: true,
                  value: _selectedValue,
                  hint: Text('Select'),
                  searchHint: Text(
                    widget.title,
                    style: UniappTextTheme.defaultWidgetStyle,
                  ),
                  onChanged: (value) {
                    setState(() {
                      _selectedValue = value;
                      formMap.formValues[widget.id] = _selectedValue;
                    });
                  },
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
