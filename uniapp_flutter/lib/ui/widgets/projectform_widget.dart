import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:toast/toast.dart';
import 'package:uuid/uuid.dart';

import './formelements/form_get_directions.dart';
import './formelements/form_headers.dart';
import './global/empty_container.dart';
import './global/sliver_footer.dart';

import '../helpers/form_values.dart';
import '../helpers/render_form_fields.dart';
import '../screens/projectform_screen.dart';
import '../themes/uniapp_css.dart';

import '../../db/models/project_master_data_table.dart';
import '../../db/models/project_submission.dart';
import '../../localization/app_translations.dart';
import '../../models/expandable.dart';
import '../../models/form_model.dart';
import '../../models/project_field.dart';
import '../../models/project_type_configuartion.dart';
import '../../models/uniapp_form.dart';
import '../../models/sub_app.dart';
import '../../models/project_specific_form.dart';
import '../../resources/project_list_provider.dart';
import '../../resources/project_type_configuration_service.dart';
import '../../sync/project_submission_thread.dart';
import '../../ua_app_context.dart';
import '../../utils/string_utils.dart';
import '../../utils/common_constants.dart';
import '../../utils/geolocator_util.dart';
import '../../utils/project_submission_upload_status.dart';
import '../../models/bridge_value.dart';
import '../../models/form_bridge.dart';
import '../../validations/client_validation_response.dart';
import '../../validations/form_data_validation_service.dart';
import '../../models/header.dart';
import '../../ui/themes/color_theme.dart';
import '../../ui/themes/text_theme.dart';

class ProjectFormWidget extends StatefulWidget {
  final String appId;
  final String projectId;
  final String currentFormId;
  final String formActionType;

  ProjectFormWidget({
    @required this.appId,
    @required this.projectId,
    this.currentFormId,
    this.formActionType,
  });

  @override
  _ProjectFormWidgetState createState() => _ProjectFormWidgetState();
}

class _ProjectFormWidgetState extends State<ProjectFormWidget> {
  String initialFormId;
  String currentFormId;

  bool isAbsorbing = false;

  ProjectTypeConfiguration projectTypeConfiguration;
  ProjectMasterDataTable projectMasterDataTable;
  Position _currentLocation = Position();

  List<UniappForm> uniAppForm = new List<UniappForm>();
  List<ProjectField> projectFields = new List<ProjectField>();

  Map uniAppFormMap = new Map();
//  Map<String, Header> headersMap = new Map();
  Map fieldsMap = new Map();
  Map buttonsMap = new Map();
  Map newFieldsMap = new Map();

  @override
  void initState() {
    super.initState();
    _fetchLocation();
  }

  Future _fetchLocation() {
    locationUtil.getCurrentPosition().then((Position location) {
      _currentLocation = location;
    });
    return null;
  }

  Future<bool> _fetchConfigurations() async {
    if (formMap.projectTypeConfiguration == null) {
      projectTypeConfiguration =
          await ProjectTypeService().fetchProjectTypeConfigurationFromDb(
        UAAppContext.getInstance().userID,
        widget.appId,
      );
      formMap.projectTypeConfiguration = projectTypeConfiguration;
    } else {
      projectTypeConfiguration = formMap.projectTypeConfiguration;
    }

    if (formMap.project == null) {
      if (widget.formActionType != null &&
          widget.formActionType == CommonConstants.UPDATE_FORM_KEY) {
        projectMasterDataTable = await ProjectListProvider().fetchProjectFromDB(
          UAAppContext.getInstance().userID,
          widget.appId,
          widget.projectId,
        );
        formMap.project = projectMasterDataTable;
      } else {
        projectMasterDataTable = new ProjectMasterDataTable(
          formMap.metadataValues["appId"],
          formMap.metadataValues["userId"],
          formMap.metadataValues["projectId"],
          "",
          CommonConstants.DEFAULT_LATITUDE.toString(),
          CommonConstants.DEFAULT_LONGITUDE.toString(),
          "0",
          "",
          "0",
          formMap.metadataValues["state"],
          null,
          null,
          'Default',
          null,
          null,
          0,
          0,
          null,
          null,
          0,
          1,
        );
        formMap.project = projectMasterDataTable;
      }
    } else {
      projectMasterDataTable = formMap.project;
    }
    if (projectMasterDataTable != null) {
      if (projectMasterDataTable.projectFields != null) {
        projectFields = projectMasterDataTable.projectFields.toList();
      }
    }

    Map<String, ProjectSpecificForm> forms = null;
    if (widget.projectId != null &&
        widget.projectId.isNotEmpty &&
        projectTypeConfiguration.content.containsKey(widget.projectId)) {
      forms = projectTypeConfiguration.content[widget.projectId];
    } else {
      forms =
          projectTypeConfiguration.content[CommonConstants.DEFAULT_PROJECT_ID];
    }

    if (forms != null) {
      ProjectSpecificForm mProjectSpecificUpdateForm =
          forms[widget.formActionType];
      if (mProjectSpecificUpdateForm != null) {
        String initialFormid = mProjectSpecificUpdateForm.forms.initialFormId;
        for (UniappForm uniForm in mProjectSpecificUpdateForm.forms.form) {
          if (uniForm.formid.compareTo(initialFormid) == 0) {
            initialFormId = initialFormid;
            break;
          }
        }
        uniAppForm = mProjectSpecificUpdateForm.forms.form;
      }
    }

    uniAppForm.forEach((f) {
      uniAppFormMap[f.formid] = f;
    });

    // TODO: Write code to update currentFormId.
    currentFormId = widget.currentFormId;
    if (uniAppFormMap.containsKey(initialFormId) && currentFormId == null) {
      currentFormId = initialFormId;
    }
    formMap.currentFormId = currentFormId;

    return true;
  }

  /// TODO: To figure why this code was written
  /// Where this code needs to used.

  // Future _setMetadataRequiredForSubmit() async {
  //   projectTypeConfiguration =
  //       await ProjectTypeService().fetchProjectTypeConfigurationFromDb(
  //     UAAppContext.getInstance().userID,
  //     widget.appId,
  //   );
  //   return projectTypeConfiguration;
  // }

  String _fetchProjectLatitude() {
    return projectMasterDataTable.projectLat ?? "";
  }

  String _fetchProjectLongitude() {
    return projectMasterDataTable.projectLon ?? "";
  }

  String _fetchProjectExternalProjectId() {
    return projectMasterDataTable.projectExtProjId ?? "";
  }

  void _toggleAbsorbing() {
    setState(() {
      isAbsorbing = !isAbsorbing;
    });
  }

  createNewProjectAndSubmitToDB() async {
    if (formMap.formValues['proj_name'] == null ||
        formMap.formValues['proj_name'].isEmpty) {
      formMap.formValues['proj_name'] = Uuid().v1().toString();
    }
    projectMasterDataTable.projectName = formMap.formValues["proj_name"];
    String geotag = formMap.formValues["geotag"];
    if (geotag != null && geotag.isNotEmpty) {
      List<String> latlong =
          StringUtils.getStringListFromDelimiter(",", geotag);
      print("latlong ${latlong.toString()}");
      projectMasterDataTable.projectLat = latlong[0];
      projectMasterDataTable.projectLon = latlong[1];
    }
    SubApp subApp = UAAppContext.getInstance()
        .rootConfig
        .config
        .firstWhere((a) => a.appId == widget.appId);
    String groupAttribute =
        getHashSeparatedValues(subApp.groupingAttributes, formMap.formValues);
    projectMasterDataTable.projectGroupingDimentionNames = groupAttribute;
    await UAAppContext.getInstance()
        .unifiedAppDBHelper
        .insertProjectMasterData(projectMasterDataTable);
  }

  String getHashSeparatedValues(
      List<String> attributes, Map<String, dynamic> projectListAttributesMap) {
    String dimensionValues = "";
    Map<String, List<String>> groupKeyToValuesMap =
        formMap.groupingAttributeToElements;
    if (attributes != null && attributes.isNotEmpty) {
      for (String attribute in attributes) {
        String attributeValue = '';
        String value = projectListAttributesMap[attribute];
        if (value == null || value == '' || value == 'null') {
          attributeValue = dimensionValues.length == 0 ? '' : '#';
        } else {
          List<String> valuesList = groupKeyToValuesMap[attribute];
          if (valuesList == null) {
            valuesList = List();
          }
          if (!valuesList.contains(value)) {
            valuesList.add(value);
            groupKeyToValuesMap[attribute] = valuesList;
          }
          attributeValue = dimensionValues.length == 0 ? value : '#' + value;
        }
        dimensionValues = dimensionValues + attributeValue;
      }
    }
    return dimensionValues;
  }

  void _onPressedButton(eachButton) async {
    if (eachButton["expandable"] != null) {
      Expandable expandableInstance = eachButton["expandable"];
      switch (expandableInstance.type) {
        case 10:
          Navigator.of(context).pop();
          break;

        case 12:
          // For calling validations:
          bool isValid = await _validateForm();
          if (!isValid) {
            return;
          }
          if (expandableInstance.subform != null &&
              expandableInstance.subform != "") {
            Navigator.of(context).push(
              MaterialPageRoute(
                builder: (BuildContext context) => ProjectFormScreen(
                  appId: formMap.metadataValues["appId"] ?? "",
                  projectId: formMap.metadataValues["projectId"] ?? "",
                  currentFormId: expandableInstance.subform ?? "",
                  formActiontype:
                      widget.formActionType ?? CommonConstants.UPDATE_FORM_KEY,
                ),
              ),
            );
          }
          break;

        case 13:
          // Disabling the UI.
          _toggleAbsorbing();
          formMap.metadataValues['timeStamp'] =
              DateTime.now().millisecondsSinceEpoch;
          formMap.metadataValues["updateRetryCount"] =
              UAAppContext.getInstance().appMDConfig.retries;
          formMap.metadataValues["additionalProperties"] =
              UAAppContext.getInstance().token;
          formMap.metadataValues["submissionApi"] =
              eachButton["api"] ?? "submit";
          formMap.metadataValues["submissionStatus"] =
              ProjectSubmissionUploadStatusHelper.getValue(
                  ProjectSubmissionUploadStatus.UNSYNCED);

          formMap.metadataValues["response"] = null;
          formMap.metadataValues["serverSyncTs"] = 0;

          formMap.metadataValues["submissionFieldList"] =
              formMap.submissionHelperFunction();

          Map<String, String> additionalProperties = Map();
          additionalProperties[CommonConstants.USER_TOKEN] =
              formMap.metadataValues["additionalProperties"];

          ProjectSubmission projectSubmission = ProjectSubmission(
            formMap.metadataValues["appId"],
            formMap.metadataValues["userId"],
            formMap.metadataValues["userType"] ?? 'Default',
            formMap.metadataValues["formId"],
            formMap.metadataValues["timeStamp"],
            formMap.metadataValues["projectId"],
            formMap.metadataValues["submissionFieldList"],
            formMap.metadataValues["submissionApi"],
            formMap.metadataValues["mdInstanceId"],
            formMap.metadataValues["submissionStatus"],
            formMap.metadataValues["response"],
            formMap.metadataValues["serverSyncTs"],
            formMap.metadataValues["updateRetryCount"],
            additionalProperties,
          );

          List<String> uuids = formMap.cameraUuids;

          ProjectSubmissionThread projectSubmissionThread =
              new ProjectSubmissionThread(
            projectSubmission,
            uuids,
          );

          // TODO: until this call is complete show a spinner and disable UI.
          projectSubmissionThread.callSubmissionThread().then(
            (response) {
              Toast.show(response.message, context,
                  duration: Toast.LENGTH_LONG, gravity: Toast.BOTTOM);

              // Enabling the UI
              _toggleAbsorbing();

              if (response.statusCode == 200 ||
                  response.statusCode ==
                      CommonConstants.DEFAULT_APP_ERROR_CODE) {
                formMap.formValues.clear();
                formMap.cameraUuids.clear();

                // TODO: restore the "appid" for named routes.
                // formMap.metadataValues.clear();

                // TODO: Change to this:
                Navigator.pop(context, widget.formActionType);
                Navigator.pop(context, widget.formActionType);
              }
            },
          );

          break;

        default:
          break;
      }
    } else {
      Toast.show(
        "No action available for this event at the moment. \nPlease try again later!",
        context,
        duration: Toast.LENGTH_LONG,
        gravity: Toast.BOTTOM,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _fetchConfigurations(),
      builder: (BuildContext context, AsyncSnapshot snapshot) {
        if (snapshot.data == null) {
          return Center(
            child: CircularProgressIndicator(),
          );
        } else {
          return new GestureDetector(
            onTap: () {
              FocusScope.of(context).requestFocus(new FocusNode());
            },
            child: formMap.formIdToFormMap[currentFormId] != null &&
                    formMap.formIdToFormMap[currentFormId].formBridge != null
                ? _renderFormBridges()
                : Padding(
                    padding: UniappCSS.smallHorizontalAndVerticalPadding,
                    child: CustomScrollView(
                      slivers: <Widget>[
                        _renderHeadersWithGetDirections(),
                        _loadFieldsMap(),
                        _renderProgressBar(),
                        _loadButtonBar(),
                      ],
                    ),
                  ),
          );
        }
      },
    );
  }

  Widget _renderProgressBar() {
    if (isAbsorbing == true) {
      return SliverList(
        delegate: SliverChildListDelegate([
          Center(
            child: Container(
              width: MediaQuery.of(context).size.width,
              padding: UniappCSS.smallVerticalPadding,
              child: Card(
                elevation: UniappCSS.largeCardElevation,
                child: Column(
                  children: <Widget>[
                    Padding(
                      padding: UniappCSS.smallHorizontalAndVerticalPadding,
                      child: CircularProgressIndicator(),
                    ),
                    Padding(
                      padding: UniappCSS.smallHorizontalAndVerticalPadding,
                      child: Text(
                        AppTranslations.of(context)
                            .text("submission_in_progress"),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ]),
      );
    } else {
      return SliverList(
        delegate: SliverChildListDelegate([
          EmptyContainer(),
        ]),
      );
    }
  }

  Widget _renderHeadersWithGetDirections() {
    return SliverList(
      delegate: SliverChildListDelegate([
        AbsorbPointer(
          absorbing: isAbsorbing,
          child: Stack(
            children: <Widget>[
              Container(
                width: MediaQuery.of(context).size.width,
                child: _loadHeaderMap(),
              ),
              _formGetDirectionsWidget(),
            ],
          ),
        ),
      ]),
    );
  }

  Widget _formGetDirectionsWidget() {
    String lat = _fetchProjectLatitude();
    String lon = _fetchProjectLongitude();
    if (lat == null ||
        lat == 'null' && lat == "-1.0" ||
        lat.isEmpty ||
        lon == null ||
        lon == 'null' ||
        lon == '-1.0' ||
        lon.isEmpty) {
      return EmptyContainer();
    }
    return Positioned(
      top: 8.0,
      right: 8.0,
      child: FormGetDirections(
        latitude: _fetchProjectLatitude(),
        longitude: _fetchProjectLongitude(),
      ),
    );
  }

  /* Widget _loadHeaderMap() {
    uniAppForm.forEach((f) {
      if (f.formid == currentFormId && f.header != null) {
        f.header.forEach((eachHeader) {
          // To send the external project id along with the form.
          if (eachHeader.display == false && eachHeader.submittable == true) {
            headersMap[eachHeader.key] = {
              "key": eachHeader.key,
              "label": StringUtils.getTranslatedString(eachHeader.label),
              "icon": eachHeader.icon,
              "display": eachHeader.display,
              "submittable": eachHeader.submittable,
              "uitype": eachHeader.uitype,
            };
          }
          headersMap[eachHeader.key] = {
            "key": eachHeader.key,
            "label": StringUtils.getTranslatedString(eachHeader.label),
            "icon": eachHeader.icon,
            "display": eachHeader.display,
            "submittable": eachHeader.submittable,
            "uitype": eachHeader.uitype,
          };
        });

        if (projectFields != null && projectFields.isNotEmpty) {
          projectFields.forEach((eachProjectField) {
            if (headersMap.containsKey(eachProjectField.key)) {
              headersMap[eachProjectField.key]["value"] =
                  (formMap.formValues[eachProjectField.key] != "" &&
                          formMap.formValues[eachProjectField.key] != null)
                      ? formMap.formValues[eachProjectField.key]
                      : eachProjectField.value.value;
            }
          });
        } else {
          headersMap.keys.forEach((eachHeaderKey) {
            if (formMap.formValues.containsKey(eachHeaderKey)) {
              headersMap[eachHeaderKey]["value"] =
                  formMap.formValues[eachHeaderKey];
            }
          });
        }

        headersMap.values.forEach((eachHeader) {
          if (eachHeader["display"] == false &&
              eachHeader["submittable"] == true) {
            formMap.formValues[eachHeader['key']] = eachHeader['value'];
          }
        });
      }
    });

    if (headersMap.values.length > 0) {
      return Card(
        color: UniappColorTheme.headerColor,
        elevation: UniappCSS.largeCardElevation,
        child: Padding(
          padding: UniappCSS.smallHorizontalAndVerticalPadding,
          child: ListView.separated(
            shrinkWrap: true,
            physics: ClampingScrollPhysics(),
            itemCount: headersMap.keys.length,
            separatorBuilder: (BuildContext context, index) {
              var eachHeaderValue = headersMap.values.elementAt(index);

              if (eachHeaderValue["display"] == false ||
                  eachHeaderValue["key"] == null ||
                  eachHeaderValue["key"] == "") {
                return EmptyContainer();
              }

              return Divider(
                color: UniappColorTheme.invertedColor,
              );
            },
            itemBuilder: (BuildContext context, index) {
              var eachHeaderValue = headersMap.values.elementAt(index);

              if (eachHeaderValue["display"] == true &&
                  eachHeaderValue["key"] != null &&
                  eachHeaderValue["key"] != "") {
                if (eachHeaderValue["uitype"] == "image") {
                  eachHeaderValue["value"] =
                      formMap.formValues[eachHeaderValue["key"]];
                }

                return FormHeaders(
                  id: eachHeaderValue["key"],
                  appId: widget.appId,
                  userId: UAAppContext.getInstance().userID,
                  formHeaderModel: FormHeaderModel(
                    headerType: eachHeaderValue["uitype"] ?? "text",
                    headerLabelFlex: 1,
                    headerValueFlex: 1,
                    headerLabel: eachHeaderValue["label"] ?? "-",
                    headerValue: eachHeaderValue["value"] ?? "-",
                  ),
                );
              } else {
                return EmptyContainer();
              }
            },
          ),
        ),
      );
    } else {
      return SizedBox(
        height: 8.0,
      );
    }
  }*/
  Widget _loadFieldsMap() {
    uniAppForm.forEach((f) {
      if (f.formid == currentFormId && f.fields != null) {
        f.fields.forEach((eachField) {
          if (eachField.key != null) {
            formMap.keyToDataType[StringUtils.getKey(eachField.key)] =
                eachField.datatype;
          }
          fieldsMap[eachField.key] = {
            "key": eachField.key,
            "label": StringUtils.getTranslatedString(eachField.label) +
                (eachField.validations != null &&
                        eachField.validations.mandatory
                    ? "*"
                    : ""),
            "uitype": eachField.uitype,
            "datatype": eachField.datatype,
            "editable": eachField.editable,
            "display": eachField.display,
            "uom": eachField.uom,
            "value": eachField.defaultValue ?? "",
            "selectablewindow": eachField.selectablewindow,
            "validations": eachField.validations,
            "expandable": eachField.expandable,
            "defaultValue": eachField.defaultValue,
            "multipleValues": eachField.multipleValues,
            "maxChars": eachField.maxChars,
            "maxValue": eachField.maxValue,
            "gps_validation": eachField.gpsValidation,
          };
        });

        if (projectFields != null && projectFields.isNotEmpty) {
          projectFields.forEach((eachProjectField) {
            if (fieldsMap.containsKey(eachProjectField.key)) {
              fieldsMap[eachProjectField.key]["label"] =
                  eachProjectField.value.label ??
                      fieldsMap[eachProjectField.key]["label"];
              fieldsMap[eachProjectField.key]["value"] =
                  eachProjectField.value.value ??
                      ((fieldsMap[eachProjectField.key]["value"] != "" ||
                              fieldsMap[eachProjectField.key]["value"] != null)
                          ? fieldsMap[eachProjectField.key]["value"]
                          : fieldsMap[eachProjectField.key]["defaultValue"]);
              fieldsMap[eachProjectField.key]["uom"] =
                  eachProjectField.value.uom ??
                      fieldsMap[eachProjectField.key]["uom"];
            }
          });
        } else {
          fieldsMap.values.forEach((fValue) {
            if (fValue["display"] == false) {
              formMap.formValues[fValue["key"]] = fValue["value"] ?? "";
            }
          });
        }
      }
    });

    return SliverList(
      delegate: SliverChildBuilderDelegate(
        (BuildContext context, int index) {
          var eachFieldValue = fieldsMap.values.elementAt(index);

          if (eachFieldValue["display"] == true &&
              eachFieldValue["key"] != null &&
              eachFieldValue["key"] != "") {
            return Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: 4.0,
              ),
              child: Container(
                color: UniappColorTheme.formFieldsColor,
                child: Column(
                  children: <Widget>[
                    RenderFormFields().renderFields(
                      eachFieldValue,
                      widget.appId,
                      widget.projectId,
                      _currentLocation,
                      _fetchProjectExternalProjectId(),
                      projectTypeConfiguration,
                      projectMasterDataTable,
                      uniAppForm,
                    ),
                    SizedBox(
                      height: 8.0,
                    ),
                  ],
                ),
              ),
            );
          } else {
            return EmptyContainer();
          }
        },
        childCount: fieldsMap.keys.length,
      ),
    );
  }

  Widget _loadButtonBar() {
    uniAppForm.forEach((f) {
      if (f.formid == currentFormId && f.header != null) {
        f.buttons.forEach((eachButton) {
          buttonsMap[eachButton.key] = {
            "key": eachButton.key,
            "label": eachButton.label,
            "expandable": eachButton.expandable,
            "api": eachButton.api,
          };
        });
      }
    });

    return SliverFooter(
      child: Align(
        alignment: Alignment.bottomCenter,
        child: Container(
          width: double.infinity,
          height: 64.0,
          padding: UniappCSS.smallHorizontalPadding,
          child: Row(
            mainAxisSize: MainAxisSize.max,
            children: _renderFormButtons(),
          ),
        ),
      ),
    );
  }

  List<Widget> _renderFormButtons() {
    List<Widget> buttonList = new List<Widget>();
    buttonsMap.values.forEach((eachButton) {
      if (eachButton["expandable"] != null) {
        Expandable expandableInstance = eachButton["expandable"];

        switch (expandableInstance.type) {
          case 10:
            buttonList.add(
              Expanded(
                child: AbsorbPointer(
                  absorbing: isAbsorbing,
                  child: Container(
                    height: UniappCSS.buttonBarButtonHeight,
                    child: RaisedButton(
                      color: UniappColorTheme.cancelButtonColor,
                      child: Text(
                        StringUtils.getTranslatedString(
                          eachButton["label"],
                        ),
                        style: UniappTextTheme.smallHeader,
                      ),
                      onPressed: () {
                        _onPressedButton(eachButton);
                      },
                    ),
                  ),
                ),
              ),
            );
            break;

          case 12:
            buttonList.add(
              Expanded(
                child: Container(
                  height: UniappCSS.buttonBarButtonHeight,
                  child: RaisedButton(
                    color: UniappColorTheme.previewButtonColor,
                    child: Text(
                      StringUtils.getTranslatedString(eachButton["label"]),
                      style: UniappTextTheme.smallInvertedHeader,
                    ),
                    onPressed: () {
                      _onPressedButton(eachButton);
                    },
                  ),
                ),
              ),
            );
            break;

          case 13:
            buttonList.add(
              Expanded(
                child: AbsorbPointer(
                  absorbing: isAbsorbing,
                  child: Container(
                    height: UniappCSS.buttonBarButtonHeight,
                    child: RaisedButton(
                      color: UniappColorTheme.submitButtonColor,
                      child: Text(
                        StringUtils.getTranslatedString('Submit'),
                        style: UniappTextTheme.smallInvertedHeader,
                      ),
                      onPressed: () async {
                        if (widget.formActionType.compareTo(
                                    CommonConstants.INSERT_FORM_KEY) ==
                                0 &&
                            formMap.metadataValues["state"]
                                    .toString()
                                    .compareTo("New") ==
                                0) {
                          await createNewProjectAndSubmitToDB();
                        }
                        _onPressedButton(eachButton);
                      },
                    ),
                  ),
                ),
              ),
            );
            break;

          default:
            buttonList.add(
              EmptyContainer(),
            );
            break;
        }
      } else {
        Toast.show(
          "No action available for this event at the moment. \nPlease try again later!",
          context,
          duration: Toast.LENGTH_LONG,
          gravity: Toast.BOTTOM,
        );
      }
    });
    return buttonList;
  }

  _renderFormBridges() {
    UniappForm form = formMap.formIdToFormMap[currentFormId];
    FormBridge formBridge = form.formBridge;
    List<BridgeValue> bridges =
        formBridge == null || formBridge.bridgeValues == null
            ? List()
            : formBridge.bridgeValues;
    return Padding(
      padding: UniappCSS.smallHorizontalAndVerticalPadding,
      child: Container(
          child: ListView.builder(
        itemCount: bridges.length,
        itemBuilder: (context, index) {
          return RaisedButton(
            color: UniappColorTheme.clickableLinkColor,
            child: Text(bridges.elementAt(index).value),
            onPressed: () {
              if (bridges.elementAt(index).expandable != null &&
                  bridges.elementAt(index).expandable.subform != null &&
                  bridges.elementAt(index).expandable.subform.isNotEmpty) {
                _onPressedBridgeButton(bridges.elementAt(index));
              }
            },
          );
        },
      )),
    );
  }

  List<Widget> _getBridges(List<BridgeValue> bridges) {
    List<Widget> widgets = List();
    for (BridgeValue bridge in bridges) {
      Widget raisedButton = Container(
        width: double.infinity,
        child: RaisedButton(
          color: UniappColorTheme.clickableLinkColor,
          child: Text(bridge.value),
          onPressed: () {
            if (bridge.expandable != null &&
                bridge.expandable.subform != null &&
                bridge.expandable.subform.isNotEmpty) {
              _onPressedBridgeButton(bridge);
            }
          },
        ),
      );
      widgets.add(raisedButton);
    }
    return widgets;
  }

  Future<bool> _validateForm() async {
    ClientValidationResponse clientValidationResponse =
        new ClientValidationResponse();
    FormDataValidationService formDataValidationService =
        new FormDataValidationService();
    Map<String, UniappForm> forms = formMap.formIdToFormMap;
    String formId = widget.currentFormId ?? initialFormId;

    UniappForm form = formMap.formIdToFormMap[formId];
    Map<String, dynamic> keyToValueForValidation = Map();
    print("form ${form}");
    for (String key in formMap.metadataValues.keys) {
      keyToValueForValidation[key] = formMap.metadataValues[key];
    }
    for (String key in formMap.formValues.keys) {
      keyToValueForValidation[StringUtils.getKey(key)] =
          formMap.formValues[key];
    }
    clientValidationResponse =
        await formDataValidationService.validateFormSubmission(
            form == null ? List() : form.fields,
            keyToValueForValidation,
            formMap.formValues,
            formMap.keyToDataType);
    if (!clientValidationResponse.mIsValid) {
      _showValidationFailedDialog(clientValidationResponse.mMessage, null);
      return false;
    }
    return true;
  }

  void _onPressedBridgeButton(BridgeValue eachButton) async {
    if (eachButton.expandable != null) {
      Expandable expandableInstance = eachButton.expandable;

      if (expandableInstance.subform != null &&
          expandableInstance.subform != "") {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (BuildContext context) => ProjectFormScreen(
              appId: formMap.metadataValues["appId"] ?? "",
              projectId: formMap.metadataValues["projectId"] ?? "",
              currentFormId: expandableInstance.subform ?? "",
              formActiontype:
                  widget.formActionType ?? CommonConstants.UPDATE_FORM_KEY,
            ),
          ),
        );
      }
      // }

    } else {
      Toast.show(
        "No action available for this event at the moment. \nPlease try again later!",
        context,
        duration: Toast.LENGTH_LONG,
        gravity: Toast.BOTTOM,
      );
    }
  }

  void _showValidationFailedDialog(
      String message, Map<String, List<String>> keyToErrorMessage) {
    // flutter defined function
    showDialog(
      barrierDismissible: false,
      context: context,
      builder: (BuildContext context) {
        // return object of type Dialog
        return AlertDialog(
          title: new Text("Validation Failed"),
          content: new Text(message),
          actions: <Widget>[
            // usually buttons at the bottom of the dialog
            new FlatButton(
              child: new Text("Close"),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Widget _loadHeaderMap() {
    UniappForm f = formMap.formIdToFormMap[currentFormId];
    if (f.header != null && f.header.length > 0) {
      return Card(
        color: UniappColorTheme.headerColor,
        elevation: UniappCSS.largeCardElevation,
        child: Padding(
          padding: UniappCSS.smallHorizontalAndVerticalPadding,
          child: ListView.separated(
            shrinkWrap: true,
            physics: ClampingScrollPhysics(),
            itemCount: f.header.length,
            separatorBuilder: (BuildContext context, index) {
              var eachHeaderValue = f.header.elementAt(index);

              if (eachHeaderValue.display == false ||
                  eachHeaderValue.key == null ||
                  eachHeaderValue.key == "") {
                return EmptyContainer();
              }

              return Divider(
                color: UniappColorTheme.invertedColor,
              );
            },
            itemBuilder: (BuildContext context, index) {
              return _getHeaderWidget(f.header.elementAt(index));
            },
          ),
        ),
      );
    } else {
      return SizedBox(
        height: 8.0,
      );
    }
  }

  Widget _getHeaderWidget(Header header) {
    dynamic value = formMap.formValues[header.key];
    if ((header.submittable == null || !header.submittable) &&
        (value == null || value.isEmpty)) {
      value = projectMasterDataTable.getKeyToValueMap()[header.key];
    }
    {
      if (header.display != null &&
          !header.display &&
          (value == null || value.isEmpty) &&
          header.submittable) {
        formMap.formValues[header.key] =
            projectMasterDataTable.getKeyToValueMap()[header.key];
      }
    }
    if ((header.display == null || header.display) &&
        header.key != null &&
        header.key != "") {
      return FormHeaders(
        id: header.key,
        appId: widget.appId,
        userId: UAAppContext.getInstance().userID,
        formHeaderModel: FormHeaderModel(
          headerType: header.uitype ?? "text",
          headerLabelFlex: 1,
          headerValueFlex: 1,
          headerLabel: header.label ?? "-",
          headerValue: value ?? "-",
        ),
      );
    } else
      return EmptyContainer();
  }
}
