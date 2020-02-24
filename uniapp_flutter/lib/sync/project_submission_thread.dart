import 'dart:core';

import '../models/project_submission_result.dart';
import '../db/databaseHelper.dart';
import '../sync/project_submission_service.dart';
import '../ua_app_context.dart';
import '../db/dbSchemaConstants.dart';
import '../db/models/project_submission.dart';
import '../utils/common_constants.dart';
import '../utils/network_utils.dart';
import '../utils/project_submission_constants.dart';

class ProjectSubmissionThread {
  // Map<String, String> mContentValues;
  ProjectSubmission projectSubmission;
  List<String> mMediaUUids = new List();
  DatabaseHelper databaseHelper;

  ProjectSubmissionThread(ProjectSubmission projectSubmission, List uuids) {
    // this.mContentValues = contentValues;
    this.projectSubmission = projectSubmission;
    this.mMediaUUids = uuids;
    databaseHelper = UAAppContext.getInstance().unifiedAppDBHelper;
  }

  Future<ProjectSubmissionResult> callSubmissionThread() async {
    int submissionTimestamp = this.projectSubmission.timeStamp;

    Map<String, dynamic> mediaValues = Map<String, dynamic>();

    mediaValues[FormMediaEntry.COLUMN_FORM_SUBMISSION_TIMESTAMP] =
        submissionTimestamp;
    // Update submissionTimestamp for all the media entries in the DB
    for (String uuid in mMediaUUids) {
      int status = await databaseHelper.updateFormMedia(
          mediaValues, projectSubmission.appId, projectSubmission.userId, uuid);
      if (status == CommonConstants.DATABASE_INSERT_ERROR_CODE) {
//        Utils.logError(UAAppErrorCodes.PROJECT_SUBMISSION_ERROR, "Error while inserting in Media table ");
      }
    }

    // Add the submission to the DB
    int status = await databaseHelper
        .addOrUpdateProjectSubmission(this.projectSubmission);
    if (status == CommonConstants.DATABASE_INSERT_ERROR_CODE) {
//      Utils.logError(UAAppErrorCodes.PROJECT_SUBMISSION_ERROR, "Error while inserting in Project Submission table ");
    }
    bool hasInternet = await NetworkUtils().hasActiveInternet();
    print('isOnline:: ${hasInternet}');
    if (!hasInternet) {
      // For Offline submission
      return new ProjectSubmissionResult(
          CommonConstants.DEFAULT_APP_ERROR_CODE,
          false,
          ProjectSubmissionConstants.PROJECT_TO_SUBMIT_IN_BACKGROUND_SYNC);
    } else {
      // For Online Submission
      ProjectSubmissionService projectSubmitService =
          new ProjectSubmissionService();
      ProjectSubmissionResult result;
      result = await projectSubmitService
          .uploadProjectInRealTime(this.projectSubmission);
      return result;
    }
  }

  ProjectSubmission getProjectSubmissionObjectFromDBObject(Map contentValues) {
    print("FORM VALUE : " + contentValues.values.toString());

    // List<SubmissionField> _submissionObject = new List<SubmissionField>();
    // SubmissionField field1 = new SubmissionField(
    //   key: "structure_type",
    //   val: "RFD",
    //   dt: "string",
    //   uom: null,
    //   ui: null,
    // );
    // SubmissionField field2 = new SubmissionField(
    //     key: "structure_possible",
    //     val: "Yes",
    //     dt: "string",
    //     uom: null,
    //     ui: null);
    // _submissionObject.add(field1);
    // _submissionObject.add(field2);

    // ProjectSubmission project = new ProjectSubmission(
    //     "87b58940-f1dd-3d4a-973e-0dd2b523723d",
    //     "1111111111",
    //     "2",
    //     "bf7b665a-39a0-34c9-b730-719aad9f9d1c##87b58940-f1dd-3d4a-973e-0dd2b523723d##00000000-0000-0000-0000-000000000000##1##1",
    //     1560196248776,
    //     "2dc3d485-7615-11e9-912d-9f5b0f906304",
    //     _submissionObject,
    //     "http://138.68.30.58:9000/api/uniapp/syncdata",
    //     "bf7b665a-39a0-34c9-b730-719aad9f9d1c##87b58940-f1dd-3d4a-973e-0dd2b523723d##00000000-0000-0000-0000-000000000000##1##1",
    //     0,
    //     "",
    //     1560196248776,
    //     3,
    //     new Map<String, String>());
    // return project;
  }
}
