
import '../../ua_app_context.dart';
import '../../db/models/project_master_data_table.dart';
import '../../models/form_model.dart';
import '../../models/uniapp_form.dart';
import '../../models/project_type_configuartion.dart';

import '../widgets/formelements/form_camera.dart';
import '../widgets/formelements/form_checkbox.dart';
import '../widgets/formelements/form_dropdown.dart';
import '../widgets/formelements/form_edit_text.dart';
import '../widgets/formelements/form_geotag.dart';
import '../widgets/formelements/form_multiline_edit_text.dart';
import '../widgets/formelements/form_radio_button.dart';
import '../widgets/formelements/form_searchable_dropdown.dart';
import '../widgets/formelements/form_switch.dart';
import '../widgets/formelements/form_video.dart';
import '../widgets/global/empty_container.dart';

class RenderFormFields {
  renderFields(
    value,
    appId,
    projectId,
    currentLocation,
    externalProjectId,
    ProjectTypeConfiguration projectTypeConfiguration,
    ProjectMasterDataTable projectMasterDataTable,
    List<UniappForm> uniAppForm,
  ) {
    switch (value["uitype"]) {
      case 'checkbox':
        return FormCheckbox(
          id: value["key"],
          title: value["label"],
          multipleValues: value["multipleValues"],
        );

      case 'dropdown':
        return FormDropdown(
          id: value["key"],
          title: value["label"],
          multipleValues: value["multipleValues"],
          initValue: value['value'],
        );

      case 'edittext':
        return FormEditText(
          id: value["key"],
          formEditTextModel: FormEditTextModel(
            editTextTitleLabel: value["label"] ?? "",
            editTextTitleFlex: 1,
            editTextUnitsOrMeasurementLabel: value["uom"] ?? "",
            editTextUnitsOrMeasurementFlex: 0,
            editTextPresentValueLabel: value["defaultValue"] ?? "",
            editTextPresentValueFlex: 0,
            editTextHint: value["label"] ?? "",
            initValue: value['value'] ?? "",
          ),
        );

      case 'geotag':
        return FormGeotag(
          id: value["key"],
          gpsValidation: value['gps_validation'],
          projLat: projectMasterDataTable.projectLat,
          projLon: projectMasterDataTable.projectLon,
        );

      case 'geotagimagefused':
        return FormCameraWidget(
          id: value["key"],
          label: value["label"],
          appId: appId,
          projectId: projectId,
          userId: UAAppContext.getInstance().userID,
          externalProjectId: externalProjectId,
          currentPosition: currentLocation,
          maxValue: value['maxValue'],
          projectLat: projectMasterDataTable.projectLat,
          projectLon: projectMasterDataTable.projectLon,
          gpsValidation: value["gps_validation"],
        );

      case 'textbox':
        return FormMultiEditText(
          id: value["key"],
          formEditTextModel: FormEditTextModel(
            editTextTitleLabel: value["label"] ?? "",
            editTextTitleFlex: 1,
            editTextUnitsOrMeasurementLabel: value["uom"] ?? "",
            editTextUnitsOrMeasurementFlex: 1,
            editTextPresentValueLabel: value["defaultValue"] ?? "",
            editTextPresentValueFlex: 0,
            editTextHint: value["label"] ?? "",
            initValue: value['value'] ?? '',
          ),
        );

      case 'radio':
        return FormRadioButton(
          id: value["key"],
          title: value["label"],
          multipleValues: value["multipleValues"],
          appId: appId,
          projectId: projectId,
          currentLocation: currentLocation,
          externalProjectId: externalProjectId,
          projectTypeConfiguration: projectTypeConfiguration,
          projectMasterDataTable: projectMasterDataTable,
          uniAppForm: uniAppForm,
        );

      case "searchabledropdown":
        return FormSearchableDropDown(
          id: value["key"],
          title: value["label"],
          multipleValues: value["multipleValues"],
        );

      case 'switch':
        return FormSwitch(
          id: value["key"],
          title: value["label"],
          defaultValue: value["defaultValue"],
        );

      case 'video':
        return FormVideoWidget(
          id: value["key"],
          label: value["label"],
          appId: appId,
          projectId: projectId,
          userId: UAAppContext.getInstance().userID,
          externalProjectId: externalProjectId,
          currentPosition: currentLocation,
          maxValue: value['maxValue'],
        );

      default:
        return EmptyContainer();
    }
  }
}
