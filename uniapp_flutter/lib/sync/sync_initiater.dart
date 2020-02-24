
import 'dart:async';

import '../resources/entity_config_provider.dart';
import '../resources/media_request_handler.dart';
import '../event/event_utils.dart';
import '../models/app_meta_data_config.dart';
import '../ua_app_context.dart';
import '../log/uniapp_logger.dart';
import 'app_background_sync_servcie.dart';
import '../sync/form_text_data_sync_service.dart';
class SyncInitiator {
  Logger logger = getLogger("SyncInitiater");
  static bool backGroundSyncServiceIntiated =  false;
  initializeBackgroundService() {
    if(backGroundSyncServiceIntiated) {
      return;
    }
    AppMetaDataConfig appMDConfig = UAAppContext.getInstance().appMDConfig;
    if(appMDConfig != null && appMDConfig.fetchEntityMetaData != null && appMDConfig.fetchEntityMetaData) {
      EntityMetaDataConfigurationProvider entityMetaDataConfigurationProvider =  EntityMetaDataConfigurationProvider.getInstance();
      entityMetaDataConfigurationProvider.initEntityMetaDataConfiguration();
    } else {
      logger.i('entityMetaDataConfiguration Initiate :: fetchEntityMetaData Flag for appMDConfig is false');
    }
    logger.i('background sync interval ${appMDConfig.syncinterval}');
    Timer.periodic(Duration(milliseconds: appMDConfig.syncinterval), (timer) async {
      if(!UAAppContext.getInstance().isLoggedIn)
        return;
      logger.i("Auto sync Start");
      eventBus.fire(PreSyncEvent());
      TextDataBackgroundSyncService textDataBackgroundSyncService = TextDataBackgroundSyncService();
      await  textDataBackgroundSyncService.execute();
      AppBackGroundSyncService appBackGroundSyncService = new AppBackGroundSyncService();
      await appBackGroundSyncService.execute();
      MediaRequestHandler handler = new MediaRequestHandler();
      await handler.getMediaRequestForUpload();
      logger.i("Auto sync completed");
      eventBus.fire(PostSyncEvent());
    });
    backGroundSyncServiceIntiated =  true;
  }

  initiateManualBackGroundSync() async {
    if(!UAAppContext.getInstance().isLoggedIn)
      return;
    logger.i("Manual sync Start");
    eventBus.fire(PreSyncEvent());
    TextDataBackgroundSyncService textDataBackgroundSyncService = TextDataBackgroundSyncService();
    await  textDataBackgroundSyncService.execute();
    AppBackGroundSyncService appBackGroundSyncService = new AppBackGroundSyncService();
    await  appBackGroundSyncService.execute();
    MediaRequestHandler handler = new MediaRequestHandler();
    await handler.getMediaRequestForUpload();
    eventBus.fire(PostSyncEvent());
    logger.i("Manual sync completed");
  }

}