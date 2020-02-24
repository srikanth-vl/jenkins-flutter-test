import '../utils/common_constants.dart';
import '../utils/media_upload_status.dart';
import '../utils/network_utils.dart';

import '../db/databaseHelper.dart';
import '../db/models/form_media_table.dart';
import '../ua_app_context.dart';
import '../utils/media_action_type.dart';
import '../utils/project_submission_upload_status.dart';
import 'media_sync_service.dart';

class MediaRequestHandler {

  DatabaseHelper dbHelper;
  MediaRequestHandler(){
    dbHelper = UAAppContext.getInstance().unifiedAppDBHelper;
  }

  Future getMediaRequestForUpload() async {
    bool isOnline = await networkUtils.hasActiveInternet();
    if(!isOnline){
      return;
    }

    List<int> statusList = [MediaUploadStatusHelper.getValue(MediaUploadStatus.NEW),
      MediaUploadStatusHelper.getValue(MediaUploadStatus.PENDING)];
    int batchSize = CommonConstants.BATCH_SIZE;

    List<FormMediaTable> formMediaEntries = await dbHelper.getFormMediaEntries(statusList, batchSize);

    for(FormMediaTable formMedia in formMediaEntries){

      if (formMedia.formSubmissionTimestamp <= 0) {
        // Media is from the current form session or are discarded images from previous sessions
        continue;
      }

      int syncStatus = await dbHelper.getProjectSubmissionStatus(formMedia.mediaAppId,
          formMedia.mediaUserId, formMedia.mediaProjectId, formMedia.formSubmissionTimestamp);

      if (ProjectSubmissionUploadStatusHelper.getValue(ProjectSubmissionUploadStatus.SYNCED) == syncStatus) {
        int status = formMedia.mediaRequestStatus;

        if (formMedia.mediaAtionType == MediaActionTypeHelper.getValue(MediaActionType.UPLOAD)) {
          bool hasInternet = await NetworkUtils().hasActiveInternet();
          if (hasInternet) {
            await uploadMedia(formMedia, status);
          }
        } else if(formMedia.mediaAtionType == MediaActionTypeHelper.getValue(MediaActionType.DOWNLOAD)){
          // call download service;
        }
      }
    }
  }

  void uploadMedia(FormMediaTable formMedia, int status) async {
    int mediaMaxRetries = UAAppContext.getInstance().appMDConfig.mediaretries;
    MediaSyncService mediaSyncService = new MediaSyncService(formMedia, mediaMaxRetries);
    await mediaSyncService.uploadMediaToServerUsingHTTP();
  }
}