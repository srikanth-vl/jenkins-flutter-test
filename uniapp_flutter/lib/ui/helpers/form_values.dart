import 'package:uuid/uuid.dart';

import '../../ua_app_context.dart';
import '../../db/models/project_master_data_table.dart';
import '../../models/uniapp_form.dart';
import '../../models/submission_fields.dart';
import '../../models/project_specific_form.dart';
import '../../models/project_type_configuartion.dart';
import '../../utils/common_constants.dart';
import '../../utils/common_utils.dart';

class FormValues {
  Map<String, List<String>> groupingAttributeToElements = Map();
  Map<String, dynamic> metadataValues = Map<String, dynamic>();
  Map formValues = new Map<String, dynamic>();
  List<String> cameraUuids = new List();
  bool isAbsorbing = false;
  String appId = "";
  String currentFormId = "";
  ProjectMasterDataTable project;
  ProjectTypeConfiguration projectTypeConfiguration;
  Map<String, UniappForm> formIdToFormMap = Map();
  Map<String, String> keyToDataType = Map();
  bool toggleAbsorbingState() {
    isAbsorbing = !isAbsorbing;
    return isAbsorbing;
  }

  List<UniappForm> listOfForm = List();

  List<SubmissionField> submissionHelperFunction() {
    List<SubmissionField> submissionFieldList = [];

    formValues.forEach((k, v) {
      if (k != null) {
        SubmissionField temp = SubmissionField();
        temp.key = k;
        //TODO: Check for other instance type of v
        if (v.runtimeType == String) {
          temp.val = v ?? null;
        } else if (v is List<String> && v != null) {
          String csv = v.join(', ');
          temp.val = csv.replaceAll(" ", "");
        }
        temp.dt = null ?? "";
        temp.uom = null;
        temp.ui = null;

        submissionFieldList.add(temp);
      }
    });

    return submissionFieldList;
  }

  initializeFormMap(
      String appId, String projectId, String formActionType) async {
    formMap.formValues.clear();
    formMap.metadataValues.clear();
    formMap.cameraUuids.clear();
    formMap.project = null;
    formMap.projectTypeConfiguration = null;
    formMap.currentFormId = "";

    ProjectTypeConfiguration ptc =
        await CommonUtils.getProjectTypeConfig(appId);
    formMap.projectTypeConfiguration = ptc;

    String pId = Uuid().v1().toString();
    if (projectId != null &&
        formActionType.compareTo(CommonConstants.UPDATE_FORM_KEY) == 0) {
      pId = projectId;
    }
    formMap.metadataValues["appId"] = appId;
    formMap.metadataValues["projectId"] = pId;
    formMap.metadataValues["userId"] = UAAppContext.getInstance().userID;

    // TODO: @Jaya.
    Map<String, ProjectSpecificForm> forms = null;
    if (pId != null && pId.isNotEmpty && ptc.content.containsKey(pId)) {
      forms = ptc.content[pId];
    } else {
      forms = ptc.content[CommonConstants.DEFAULT_PROJECT_ID];
    }
    List<UniappForm> formList = List();
    if (forms != null) {
      formMap.metadataValues["formId"] = forms[formActionType].forminstanceid;
      formMap.metadataValues["mdInstanceId"] =
          forms[formActionType].mdinstanceid;
      if (formActionType == CommonConstants.INSERT_FORM_KEY) {
        formList = forms[CommonConstants.INSERT_FORM_KEY].forms.form;
        formMap.metadataValues["state"] = "New";
        formMap.formValues["proj_state"] = "New";
      } else {
        formList = forms[CommonConstants.UPDATE_FORM_KEY].forms.form;
        formMap.metadataValues["state"] = "In Progress";
      }
      this.listOfForm = formList;
      for (UniappForm form in listOfForm) {
        formIdToFormMap[form.formid] = form;
      }
    }
    return pId;
  }
}

final formMap = FormValues();
