
import '../utils/network_utils.dart';
import 'package:synchronized/synchronized.dart';

import '../db/databaseHelper.dart';
import '../db/models/project_submission.dart';
import '../models/app_meta_data_config.dart';
import '../sync/project_submission_service.dart';
import '../sync/form_text_data_sync_service.dart';
import '../utils/project_submission_constants.dart';
import '../utils/project_submission_upload_status.dart';
import '../ua_app_context.dart';
import '../log/uniapp_logger.dart';

class AllProjectSubmissionService {
  DatabaseHelper databaseHelper = DatabaseHelper();
  Logger logger = getLogger("AllProjectSubmissionService");
  AllProjectSubmissionService() {
    databaseHelper = UAAppContext.getInstance().unifiedAppDBHelper;
  }

  //to sync all submissions irrespective of user
  Future<bool> uploadAllUnSyncedProjects() async {

    List<int> uploadStatusList = List();
    uploadStatusList.add(ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.UNSYNCED));
    uploadStatusList.add((ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.SERVER_ERROR)));

    List<ProjectSubmission> unSyncedProjectSubmission = await databaseHelper.getAllProjectsToSubmit(uploadStatusList);

    bool uploadStatus = true;

    if (unSyncedProjectSubmission == null || unSyncedProjectSubmission.isEmpty) {
      return true;
    }

    AppMetaDataConfig appMetaConfig = UAAppContext.getInstance().appMDConfig;
//        Long serverErrorSyncFrequency = (appMetaConfig.mServiceFrequency == null
//                || appMetaConfig.mServiceFrequency.get(Constants.SERVICE_FREQUENCY_SERVER_ERROR) == null ) ? ProjectSubmissionConstants.DEFAULT_RETRY_FREQUENCY: appMetaConfig.mServiceFrequency.get(Constants.SERVICE_FREQUENCY_SERVER_ERROR);
    int retryCount = appMetaConfig.retries == 0 ? ProjectSubmissionConstants.DEFAULT_SUBMISSION_RETRIES : appMetaConfig.retries ;

    ProjectSubmissionService pss = new ProjectSubmissionService();
    int i = 0;
    for (ProjectSubmission submissionData in unSyncedProjectSubmission) {
      bool isOnline = await networkUtils.hasActiveInternet();
      if(!isOnline) {
        continue;
      }
      if(submissionData.submissionStatus == ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.SYNCED)
          || submissionData.submissionStatus == ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.FAILED)){
        continue;
      }

      if (submissionData.submissionStatus == ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.SERVER_ERROR)
          && submissionData.updateRetryCount > retryCount) {
        await _setProjectStatusAsFailed(submissionData);
        continue;
      }
      i++;

      Lock textSubmissionSynclock = TextDataBackgroundSyncService.getSyncLock();

      await textSubmissionSynclock.synchronized(() async {
        uploadStatus = await pss.callProjectSubmitService(submissionData);
      });
      logger.i('XXXX: In Offline upload : After Lock Release ${submissionData.projectId}');
    }
    // if upload failed stop execution
    if(!uploadStatus) {
      return false;
    }
    logger.i('Successfully uploaded projects');
    return true;
  }

  Future _setProjectStatusAsFailed(ProjectSubmission submissionData) async {
    await databaseHelper.updateProjectSubmissionStatus(submissionData.userId, submissionData.appId, submissionData.projectId, submissionData.timeStamp, ProjectSubmissionUploadStatus.FAILED);
  }
}

