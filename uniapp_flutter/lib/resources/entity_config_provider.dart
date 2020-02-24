import 'dart:io';

import '../models/root_config.dart';
import '../models/sub_app.dart';

import '../resources/entity_config_service.dart';
import '../utils/common_utils.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import '../log/uniapp_logger.dart';
import '../utils/network_utils.dart';
import '../error/all_custom_exception.dart';

class EntityMetaDataConfigurationProvider {
  static final EntityMetaDataConfigurationProvider ourInstance = new EntityMetaDataConfigurationProvider();
  Logger logger = getLogger("EntityMetaDataConfigurationProvider");
  static EntityMetaDataConfigurationProvider getInstance() {
    return ourInstance;
  }

  int lastSyncTime = 0;

  EntityMetaDataConfigurationProvider() {
    this.lastSyncTime = 0;
  }

  Future initEntityMetaDataConfiguration() async {

    // Fetch EntityMetaDataConfigurationProvider from the DB
    int count = await UAAppContext.getInstance().unifiedAppDBHelper.getEntityMetaDataConfiguration(CommonConstants.SUPER_APP_ID);
    if (count == null || count == 0) {
      // check if network is available
      bool isOnline = await networkUtils.hasActiveInternet();
      if(!isOnline){
        logger.e("App is offline");
        CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
      }
      else {
        // Fetching the app meta config from the server
        try {
          logger.i("EntityMetaDataConfig fetch from server");
          await fetchEntityMetaDataConfigFromServer();
          logger.i("EntityMetaDataConfig fetched from server");
        } on AppOfflineException catch (e) {
          CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
        } on SocketException catch (e){
          CommonUtils.showToast(CommonConstants.NETWORK_ERROR, UAAppContext.getInstance().context);
        } on UAException catch (e) {
          CommonUtils.showToast(CommonConstants.SOMETHING_WENT_WRONG, UAAppContext.getInstance().context);
        }
      }
    } else {
      logger.i("$count EntityMetaDataConfiguration pesent in DB");
    }
  }

  Future fetchEntityMetaDataConfigFromServer() async {
    // Check if server available
    bool isOnline = await networkUtils.hasActiveInternet();
    if(!isOnline) {
      logger.e('Network is unavailable, Please connect to network and try again');
      throw AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR,'Network is unavailable, Please connect to network and try again');
    }

    EntityMetaDataConfigurationService entityMetaDataConfigurationService = new EntityMetaDataConfigurationService();
    RootConfig rootConfig =  UAAppContext.getInstance().rootConfig;
    List<SubApp> subApps = rootConfig == null  || rootConfig.config == null ? List() : rootConfig.config;
    // fetch for SuperAppID
    try {
      await entityMetaDataConfigurationService.callEntityMetaDataConfigurationService(CommonConstants.DEFAULT_PROJECT_ID);
    } on  UAException catch (e) {
//    // Error - exit app
      throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error fetching EntityMetaDataConfig - exit");
    }
    for(SubApp subApp in subApps) {
      if(subApp.fetchEntityMetaData) {
        try {
          await entityMetaDataConfigurationService.callEntityMetaDataConfigurationService(subApp.appId);
        } on  UAException catch (e) {
//    // Error - exit app
          throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error fetching EntityMetaDataConfig - exit");
        }
      }
    }
    this.lastSyncTime = DateTime.now().millisecondsSinceEpoch;
  }

  Future fetchFromServerForBackgroundSync() async {
    int syncFrequency =
        UAAppContext.getInstance().appMDConfig.serviceFrequency.appmetaconfig;
    if ((DateTime.now().millisecondsSinceEpoch - lastSyncTime) <=
        syncFrequency) {
      print("EntityMetaDataConfig was synced recently, will try in future");

    }
    await fetchEntityMetaDataConfigFromServer();
  }
}
