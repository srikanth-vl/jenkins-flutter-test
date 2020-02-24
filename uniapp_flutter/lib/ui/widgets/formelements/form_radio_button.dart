import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';

import '../global/empty_container.dart';
import '../../../db/models/project_master_data_table.dart';
import '../../helpers/form_values.dart';
import '../../helpers/render_form_fields.dart';
import '../../../models/uniapp_form.dart';
import '../../../models/multiple_value.dart';
import '../../../models/project_field.dart';
import '../../../models/uniapp_form_field.dart';
import '../../../models/project_type_configuartion.dart';
import '../../themes/uniapp_css.dart';
import '../../../utils/string_utils.dart';
import '../../themes/text_theme.dart';
import '../../themes/color_theme.dart';

class FormRadioButton extends StatefulWidget {
  final String id;
  final String title;
  final String appId;
  final String projectId;
  final Position currentLocation;
  final String externalProjectId;
  final List<UniappForm> uniAppForm;
  final List<MultipleValue> multipleValues;
  final ProjectTypeConfiguration projectTypeConfiguration;
  final ProjectMasterDataTable projectMasterDataTable;

  FormRadioButton({
    @required this.id,
    @required this.title,
    @required this.appId,
    @required this.projectId,
    @required this.currentLocation,
    @required this.externalProjectId,
    @required this.uniAppForm,
    @required this.multipleValues,
    @required this.projectTypeConfiguration,
    @required this.projectMasterDataTable,
  });

  @override
  FormRadioButtonState createState() => FormRadioButtonState();
}

class FormRadioButtonState extends State<FormRadioButton> {
  var groupValue;
  List<ProjectField> projectFields = new List<ProjectField>();
  List<Map> _multipleValuesJson = new List();

  Map newFieldsMap = new Map();
  Map uniAppFormMap = new Map();

  @override
  void initState() {
    super.initState();

    if (widget.projectMasterDataTable.projectFields != null) {
      projectFields = widget.projectMasterDataTable.projectFields.toList();
    }
    widget.uniAppForm.forEach((f) {
      uniAppFormMap[f.formid] = f;
    });

    widget.multipleValues.forEach((f) {
      _multipleValuesJson.add({
        "id": f.value,
        "name": f.value,
        "expandable": f.expandable,
      });
    });

    setState(() {
      groupValue = _multipleValuesJson[0]["name"] ?? "";
      formMap.formValues[widget.id] = groupValue;
    });
  }

  void selectedRadio(e) {
    if (e != null && widget.id != null) {
      setState(() {
        groupValue = e;
        formMap.formValues[widget.id] = groupValue;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.fromLTRB(8.0, 8.0, 8.0, 0.0),
          child: Container(
            width: double.infinity,
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

                // TODO: Understand how to implement this with Wrap!!!
                Container(
                  // color: Colors.black12,
                  width: MediaQuery.of(context).size.width,
                  height: 48.0,
                  child: _renderRadioButtons(),
                ),
                // Wrap(
                //   spacing: 8.0,
                //   runSpacing: 4.0,
                //   children: <Widget>[
                //     _renderRadioButtons(),
                //     // _renderRadioButtons(),
                //   ],
                // ),
              ],
            ),
          ),
        ),
        _generateExpandableFieldsMap(),
      ],
    );
  }

  Widget _renderRadioButtons() {
    return ListView.builder(
        scrollDirection: Axis.horizontal,
        shrinkWrap: true,
        itemCount: _multipleValuesJson.length,
        itemBuilder: (BuildContext context, int index) {
          if (_multipleValuesJson.elementAt(index)["id"] != "" &&
              _multipleValuesJson.elementAt(index)["name"] != "") {
            return Container(
              height: MediaQuery.of(context).size.height,
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Radio(
                    value: _multipleValuesJson.elementAt(index)["id"],
                    groupValue: groupValue,
                    onChanged: (e) => selectedRadio(e),
                  ),
                  Text(
                    _multipleValuesJson.elementAt(index)["name"],
                    style: UniappTextTheme.defaultTextStyle,
                  ),
                ],
              ),
            );
          } else {
            return EmptyContainer();
          }
        });
  }

  Widget _generateExpandableFieldsMap() {
    for (var f in widget.multipleValues) {
      if (formMap.formValues[widget.id] == f.value && f.expandable != null) {
        if (f.expandable.type == 1 && f.expandable.subform != null) {
          if (uniAppFormMap.containsKey(f.expandable.subform)) {
            if (uniAppFormMap[f.expandable.subform].fields != null) {
              List<UniappFormField> newField =
                  uniAppFormMap[f.expandable.subform].fields;

              newField.forEach((eachField) {
                formMap.keyToDataType[StringUtils.getKey(eachField.key)] = eachField.datatype;
                newFieldsMap[eachField.key] = {
                  "key": eachField.key,
                  "label": StringUtils.getTranslatedString(eachField.label) + (eachField.validations!= null && eachField.validations.mandatory ? "*" :""),
                  "uitype": eachField.uitype,
                  "datatype": eachField.datatype,
                  "editable": eachField.editable,
                  "display": eachField.display,
                  "uom": eachField.uom,
                  "selectablewindow": eachField.selectablewindow,
                  "validations": eachField.validations,
                  "expandable": eachField.expandable,
                  "defaultValue": eachField.defaultValue,
                  "multipleValues": eachField.multipleValues,
                  "maxChars": eachField.maxChars,
                };

                projectFields.forEach((eachProjectField) {
                  if (newFieldsMap.containsKey(eachProjectField.key)) {
                    newFieldsMap[eachProjectField.key]["label"] =
                        eachProjectField.value.label ??
                            newFieldsMap[eachProjectField.key]["label"];
                    newFieldsMap[eachProjectField.key]["value"] =
                        eachProjectField.value.value ??
                            newFieldsMap[eachProjectField.key]["value"];
                    newFieldsMap[eachProjectField.key]["uom"] =
                        eachProjectField.value.uom ??
                            newFieldsMap[eachProjectField.key]["uom"];
                  }
                });
              });

              for (var val in newFieldsMap.values) {
                return _renderExpandableFields(val);
              }
            }
          }
        }
      }
    }
    return EmptyContainer();
  }

  Widget _renderExpandableFields(eachFieldValue) {
    return Container(
      color: UniappColorTheme.invertedColor,
      child: Column(
        children: <Widget>[
          SizedBox(
            height: 8.0,
          ),
          RenderFormFields().renderFields(
            eachFieldValue,
            widget.appId,
            widget.projectId,
            widget.currentLocation,
            widget.externalProjectId,
            widget.projectTypeConfiguration,
            widget.projectMasterDataTable,
            widget.uniAppForm,
          ),
        ],
      ),
    );
  }
}
