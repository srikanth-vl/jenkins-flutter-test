import 'package:flutter/material.dart';

import '../global/empty_container.dart';
import '../../../models/multiple_value.dart';
import '../../themes/text_theme.dart';
import '../../themes/uniapp_css.dart';

class FormCheckbox extends StatefulWidget {
  final String id;
  final String title;
  final List<MultipleValue> multipleValues;

  FormCheckbox({
    @required this.id,
    @required this.title,
    @required this.multipleValues,
  });

  @override
  _FormCheckboxState createState() => _FormCheckboxState();
}

class _FormCheckboxState extends State<FormCheckbox> {
  List<bool> _selectedValue = new List();
  List<Map> _multipleValuesJson = new List();

  @override
  void initState() {
    super.initState();

    for (int i = 0; i < widget.multipleValues.length; i++) {
      _selectedValue.add(false);
    }

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
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Container(
              padding: const EdgeInsets.fromLTRB(8.0, 8.0, 8.0, 0.0),
              child: Text(
                widget.title ?? "",
                style: UniappTextTheme.defaultWidgetStyle,
              ),
            ),
            Container(
              width: MediaQuery.of(context).size.width,
              child: _renderCheckboxes(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _eachCheckboxTile(int index) {
    return Container(
      child: CheckboxListTile(
        value: _selectedValue[index] ?? false,
        onChanged: (updatedValue) {
          setState(() {
            _selectedValue[index] = updatedValue;

            // TODO: find out the actual way for submission.
            // formMap.formValues[widget.id] = _selectedValue;
          });
        },
        title: Text(
          _multipleValuesJson.elementAt(index)["name"],
          style: UniappTextTheme.smallHeader,
        ),
        controlAffinity: ListTileControlAffinity.trailing,
      ),
    );
  }

  Widget _renderCheckboxes() {
    return ListView.builder(
        shrinkWrap: true,
        physics: ClampingScrollPhysics(),
        itemCount: _multipleValuesJson.length,
        itemBuilder: (BuildContext context, int index) {
          if (_multipleValuesJson.elementAt(index)["id"] != "" &&
              _multipleValuesJson.elementAt(index)["name"] != "") {
            return _eachCheckboxTile(index);
          } else {
            return EmptyContainer();
          }
        });
  }
}
