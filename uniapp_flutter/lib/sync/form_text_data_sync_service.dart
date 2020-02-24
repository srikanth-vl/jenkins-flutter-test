
import 'package:synchronized/synchronized.dart';

import '../ua_app_context.dart';
import '../utils/network_utils.dart';
import '../log/uniapp_logger.dart';
import 'form_text_submission_sync_handler.dart';

class TextDataBackgroundSyncService  {
  static var lock = new Lock();
  static Lock SYNC_LOCK = new Lock();

  Logger logger = getLogger("TextDataBackgroundSyncService");
  static Object getSyncLock() {
    return SYNC_LOCK;
  }

  static bool isTextSubmissionSyncThreadRunning = false;

  void execute() async{
    if(!UAAppContext.getInstance().isLoggedIn)
      return;
    bool isOnline = await networkUtils.hasActiveInternet();
    if (isOnline) {
      logger.i('request for new Text submissiom sync thread');
      await lock.synchronized(() async {
        if (!isTextSubmissionSyncThreadRunning) {
          logger.i("request task called");
          TextDatSubmissionSyncHandler textDatSubmissionSyncHandler = new TextDatSubmissionSyncHandler();
          textDatSubmissionSyncHandler.execute();
        }
      });
    }
  }
}