import 'package:flutter/material.dart';
import 'dart:collection';

import './global/empty_container.dart';
import '../themes/color_theme.dart';
import '../themes/text_theme.dart';
import '../themes/uniapp_css.dart';
import '../../ui/helpers/recase_text.dart';
import '../../event/event_utils.dart';
import '../../models/dyamic_forms.dart';
import '../../models/button.dart';
import '../../models/uniapp_form.dart';
import '../../models/uniapp_form_field.dart';
import '../../models/filter_attribute_model.dart';
import '../../models/filter_config_model.dart';
import '../../models/sub_app.dart';
import '../../utils/common_utils.dart';
import '../../ua_app_context.dart';
import '../../db/models/project_master_data_table.dart';

class FilterWidget extends StatefulWidget {
  final String appId;
  final List<ProjectMasterDataTable> projectList;
  final Map filterKeyToValue;

  FilterWidget({
    @required this.appId,
    @required this.projectList,
    @required this.filterKeyToValue,
  });
  @override
  FilterWidgetState createState() => FilterWidgetState();
}

class FilterWidgetState extends State<FilterWidget> {
  DynamicForms filteringForm;
  Map<String, String> mKeyToSelectedValuesMap;
  List<String> attributes;
  SubApp app;
  Map<String, FilterAttributeModel> mKeyToValueMapFromDropDown = new Map();
  List<FilterConfigModel> hierarchicalFilterModels = new List();
  Map<String, FilterConfigModel> flatFilterModels = new Map();

  @override
  void initState() {
    super.initState();
    UAAppContext.getInstance().context = context;
    filteringForm = CommonUtils.getSubAppFromConfig(widget.appId).filteringForm;
    app = CommonUtils.getSubAppFromConfig(widget.appId);
    attributes = app.filteringAttributes;
    hierarchicalFilterModels =
        _getFilteringMapping(widget.projectList, attributes);
    flatFilterModels = _getFilterConfigList(attributes);
    mKeyToSelectedValuesMap = widget.filterKeyToValue;
    if (mKeyToSelectedValuesMap == null) {
      mKeyToSelectedValuesMap = new Map();
    }
    Map<String, String> keyToSelectedValuesMap = mKeyToSelectedValuesMap;
    if (keyToSelectedValuesMap != null && keyToSelectedValuesMap.isNotEmpty) {
      for (String key in keyToSelectedValuesMap.keys) {
        for (FilterAttributeModel model
            in flatFilterModels[key].filterElements) {
          if (model.attributeName == keyToSelectedValuesMap[key]) {
            mKeyToValueMapFromDropDown[key] = model;
          }
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    UniappForm initialForm;
    if (filteringForm != null) {
      String initialId = filteringForm.initialFormId;
      if (initialId != null &&
          initialId.isNotEmpty &&
          filteringForm.form != null &&
          filteringForm.form.isNotEmpty) {
        for (UniappForm form in filteringForm.form) {
          if (form.formid == initialId) {
            initialForm = form;
            break;
          }
        }
        if (initialForm != null &&
            initialForm.fields != null &&
            initialForm.fields.isNotEmpty) {
          return Padding(
            padding: const EdgeInsets.all(8.0),
            child: CustomScrollView(
              slivers: <Widget>[
                _renderFormFields(initialForm.fields),
                _loadButtonBar(initialForm.buttons),
              ],
            ),
          );
        }
      }
    }
    return EmptyContainer();
  }

  Widget _renderFormFields(List<UniappFormField> fields) {
    List<Widget> fieldWidgets = new List();
    for (UniappFormField field in fields) {
      if (!attributes.contains(field.key)) {
        continue;
      }

      switch (field.uitype) {
        case "dropdown":
          {
            FilterConfigModel model = flatFilterModels[field.key];
            if (field.filteringKey != null &&
                mKeyToValueMapFromDropDown.containsKey(field.filteringKey) &&
                mKeyToValueMapFromDropDown[field.filteringKey] != null) {
              for (FilterConfigModel nmodel in hierarchicalFilterModels) {
                if (nmodel.key == field.filteringKey) {
                  for (FilterAttributeModel attrModel
                      in nmodel.filterElements) {
                    if (attrModel.attributeName ==
                        mKeyToValueMapFromDropDown[field.filteringKey]
                            .attributeName) {
                      model = attrModel.child;
                      break;
                    }
                  }
                  break;
                }
              }
            }
            fieldWidgets.add(new Container(
              padding: EdgeInsets.symmetric(horizontal: 4.0, vertical: 4.0),
              child: Container(
                // padding: ,
                child: _getDropdownButton(
                    model.filterElements, model.key, field.label),
              ),
            ));
          }
      }
    }
    for (String key in mKeyToValueMapFromDropDown.keys) {
      if (mKeyToValueMapFromDropDown[key] != null) {
        mKeyToSelectedValuesMap[key] =
            mKeyToValueMapFromDropDown[key].attributeName;
      }
    }
    return SliverList(
      delegate: SliverChildListDelegate([
        Card(
            child: Padding(
          padding: UniappCSS.smallHorizontalAndVerticalPadding,
          child: Column(
            mainAxisSize: MainAxisSize.max,
            children: fieldWidgets,
          ),
        )),
      ]),
    );
  }

  List<Widget> _renderFormButtons(List<Button> formButtons) {
    List<Widget> buttons = new List();
    for (Button button in formButtons) {
      buttons.add(Expanded(
        child: Container(
          height: 48.0,
          child: RaisedButton(
            color: UniappColorTheme.submitButtonColor,
            child: Text(
              button.label,
              style: UniappTextTheme.smallInvertedHeader,
            ),
            onPressed: () {
              switch (button.expandable.type) {
                case 15:
                  _onSubmitFilters(mKeyToSelectedValuesMap);
                  break;

                default:
                  break;
              }
            },
          ),
        ),
      ));
    }
    return buttons;
  }

  Widget _loadButtonBar(List<Button> formButtons) {
    return SliverList(
      delegate: SliverChildListDelegate([
        Container(
            width: double.infinity,
            height: 64.0,
            padding: UniappCSS.smallHorizontalAndVerticalPadding,
            child: Row(
              mainAxisSize: MainAxisSize.max,
              children: _renderFormButtons(formButtons),
            )),
      ]),
    );
  }

  _onSubmitFilters(Map<String, String> keyToValueMap) {
    // Submit Filters
    LinkedHashMap<String, String> data = new LinkedHashMap();
    if (keyToValueMap != null && keyToValueMap.isNotEmpty) {
      if (attributes != null && attributes.isNotEmpty) {
        for (String attribute in attributes) {
          if (keyToValueMap.containsKey(attribute)) {
            data[attribute] = keyToValueMap[attribute];
          }
        }
      }
    }
    eventBus.fire(PostFilterEvent(data));
    Navigator.pop(context);
  }

  Widget _getDropdownButton(
      List<FilterAttributeModel> listOfAttr, String key, String hint) {
    return Container(
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
                    hint ?? "",
                    style: UniappTextTheme.smallHeader,
                  ),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(8.0, 0.0, 8.0, 0.0),
            child: Container(
                width: double.maxFinite,
                child: DropdownButton<String>(
                  isExpanded: true,
                  iconSize: 36,
                  iconDisabledColor: Colors.black,
                  iconEnabledColor: Colors.black,
                  items: listOfAttr.map((FilterAttributeModel value) {
                    return new DropdownMenuItem<String>(
                      value: value.attributeName,
                      child: new Text(
                        RecaseText().reCase("titleCase", value.attributeName),
                      ),
                    );
                  }).toList(),
                  value: mKeyToValueMapFromDropDown[key] == null ||
                          !_checkIfAttributeAlreadyPresent(
                              mKeyToValueMapFromDropDown[key], listOfAttr)
                      ? listOfAttr[0].attributeName
                      : mKeyToValueMapFromDropDown[key].attributeName,
                  onChanged: (value) {
                    setState(() {
                      if (value == hint) {
                        mKeyToValueMapFromDropDown[key] = null;
                      } else {
                        for (FilterAttributeModel model in listOfAttr) {
                          if (model.attributeName == value) {
                            mKeyToValueMapFromDropDown[key] = model;
                            break;
                          }
                        }
                      }
                    });
                  },
                )),
          ),
          SizedBox(
            height: 4.0,
          ),
        ],
      ),
    );

    // return DropdownButton<String>(
    //   isExpanded: true,
    //   iconSize: 40,
    //   iconDisabledColor: Colors.black,
    //   iconEnabledColor: Colors.white,
    //   items: listOfAttr.map((FilterAttributeModel value) {
    //     return new DropdownMenuItem<String>(
    //       value: value.attributeName,
    //       child: new Text(value.attributeName, ),
    //     );
    //   }).toList(),
    //     value: mKeyToValueMapFromDropDown[key] == null || !_checkIfAttributeAlreadyPresent(mKeyToValueMapFromDropDown[key], listOfAttr)
    //   ? hint : mKeyToValueMapFromDropDown[key].attributeName,
    //     onChanged: (value) {
    //     setState(() {
    //       if(value == hint){
    //         mKeyToValueMapFromDropDown[key] = null;
    //       } else {
    //         for (FilterAttributeModel model in listOfAttr) {
    //           if (model.attributeName == value) {
    //             mKeyToValueMapFromDropDown[key] = model;
    //             break;
    //           }
    //         }
    //       }
    //     });
    //   },
    // );
  }

  List<FilterConfigModel> _getFilteringMapping(
      List<ProjectMasterDataTable> projectList, List<String> attributes) {
    List<FilterConfigModel> filterModels = new List();
    for (String attr in attributes) {
      FilterConfigModel model = new FilterConfigModel();
      model.key = attr;
      for (ProjectMasterDataTable project in projectList) {
        Map<String, String> dimensionMap =
            project.projectFilteringDimensionNames;
        _getFilterAttribute(attr, attributes, dimensionMap, model);
      }
      filterModels.add(model);
    }
    return filterModels;
  }

  _getFilterAttribute(String key, List<String> attributes,
      Map<String, String> dimensionMap, FilterConfigModel model) {
    if (key == null || dimensionMap[key] == null) {
      return;
    }
    String nextkey;
    bool flagKeyFound = false;
    for (String attri in attributes) {
      if (flagKeyFound) {
        nextkey = attri;
        break;
      }
      if (attri == key) {
        flagKeyFound = true;
      }
    }

    FilterConfigModel configModel = new FilterConfigModel();
    FilterAttributeModel filterAttributeModel = new FilterAttributeModel();
    filterAttributeModel.attributeName = dimensionMap[key];
    FilterConfigModel childconfigModel = new FilterConfigModel();
    filterAttributeModel.child = childconfigModel;
    childconfigModel.key = nextkey;
    configModel = model;
    if (model.filterElements != null && model.filterElements.isNotEmpty) {
      bool flag = false;
      for (FilterAttributeModel attributeModel in configModel.filterElements)
        if (attributeModel.attributeName == dimensionMap[key]) {
          flag = true;
          filterAttributeModel = attributeModel;
        }
      if (!flag) {
        configModel.filterElements.add(filterAttributeModel);
      }
    } else {
      model.filterElements = new List();
      model.filterElements.add(filterAttributeModel);
    }
    _getFilterAttribute(
        nextkey, attributes, dimensionMap, filterAttributeModel.child);
  }

  Map<String, FilterConfigModel> _getFilterConfigList(List<String> attributes) {
    Map<String, FilterConfigModel> map = new Map();
    for (String attr in attributes) {
      FilterConfigModel model = new FilterConfigModel();
      model.key = attr;
      model.filterElements = new List();
      for (ProjectMasterDataTable project in widget.projectList) {
        Map<String, String> dimensionMap =
            project.projectFilteringDimensionNames;
        FilterAttributeModel attrModel = new FilterAttributeModel();
        if (dimensionMap[attr] != null && dimensionMap[attr].isNotEmpty) {
          attrModel.attributeName = dimensionMap[attr];
          if (!_checkIfAttributeAlreadyPresent(
              attrModel, model.filterElements)) {
            model.filterElements.add(attrModel);
          }
        }
      }
      map[attr] = model;
    }
    return map;
  }

  bool _checkIfAttributeAlreadyPresent(
      FilterAttributeModel model, List<FilterAttributeModel> filterElements) {
    for (FilterAttributeModel attributeModel in filterElements) {
      if (attributeModel.attributeName == model.attributeName) {
        return true;
      }
    }
    return false;
  }
}
