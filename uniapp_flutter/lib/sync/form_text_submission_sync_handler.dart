

import '../resources/all_project_submission_service.dart';
import '../utils/network_utils.dart';
import '../log/uniapp_logger.dart';
import 'form_text_data_sync_service.dart';

class TextDatSubmissionSyncHandler {
  Logger logger = getLogger("TextDataBackgroundSyncService");
  execute() async{
    logger.i("initiate form Text Submission sync");
    bool isOnline = await networkUtils.hasActiveInternet();
    if (!isOnline) {
      logger.i( "Cannot initiate form Text Submission sync without active internet connection");
      return null;
    }
    // todo handle when thread is interrupted
//  else if (Thread.currentThread().isInterrupted()) {
//  logger.i(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "Cannot initiate form Text Submission sync");
//  return null;
    TextDataBackgroundSyncService.isTextSubmissionSyncThreadRunning = true;
    AllProjectSubmissionService allProjectListService = new AllProjectSubmissionService();
    logger.i(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "Started form Text Submission Upload");
    await allProjectListService.uploadAllUnSyncedProjects();
    logger.i(LogTags.TEXT_SUBMISSION_SYNC_THREAD, "Completed form Text Submission Upload");
    TextDataBackgroundSyncService.isTextSubmissionSyncThreadRunning = false;
    return null;
  }
}
