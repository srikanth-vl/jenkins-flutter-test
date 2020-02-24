import 'dart:convert';
import 'dart:io';
import '../utils/common_utils.dart';

import '../db/models/config_table.dart';
import '../models/root_config.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import 'root_config_service.dart';
import '../log/uniapp_logger.dart';
import '../utils/network_utils.dart';
import '../error/all_custom_exception.dart';
class RootConfigProvider {
  static final RootConfigProvider ourInstance = new RootConfigProvider();
  Logger logger = getLogger("RootConfigProvider");
  static RootConfigProvider getInstance() {
    return ourInstance;
  }

  int lastSyncTime = 0;

  RootConfigProvider() {
    this.lastSyncTime = 0;
  }

  Future<RootConfig> initRootConfig() async {

    // Fetch rootConfig from the DB
    String userId = UAAppContext.getInstance().userID;
    ConfigFile rootConfigFile = await UAAppContext.getInstance()
        .unifiedAppDBHelper
        .getConfig(userId, CommonConstants.ROOT_CONFIG_NAME);
    RootConfig rootConfig = null;
    if (rootConfigFile == null || rootConfigFile.content == '') {
      // check if network is available
      bool isOnline = await networkUtils.hasActiveInternet();
      if(!isOnline){
        logger.e("App is offline");
        CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
      }
      else {
        UAAppContext.getInstance().rootConfig = null;
        // Fetching the app meta config from the server
        try {
          logger.i("rootconfig fetch from server");
          rootConfig = await fetchRootConfigFromServer();
          logger.i("rootconfig fetched from server");
        } on AppOfflineException catch (e) {
          CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
        } on SocketException catch (e){
          CommonUtils.showToast(CommonConstants.NETWORK_ERROR, UAAppContext.getInstance().context);
        } on UAException catch (e) {
          CommonUtils.showToast(CommonConstants.SOMETHING_WENT_WRONG, UAAppContext.getInstance().context);
        }
      }
    } else {
      // Initializing singleton class
      logger.i("rootconfig fetched from db");
      rootConfig = RootConfig.fromJson(jsonDecode(rootConfigFile.content));
    }
    UAAppContext.getInstance().rootConfig = rootConfig;
    return rootConfig;
  }

  Future<RootConfig> fetchRootConfigFromServer() async {
    // Check if server available
    bool isOnline = await networkUtils.hasActiveInternet();
    if(!isOnline) {
      logger.e('Network is unavailable, Please connect to network and try again');
      throw AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR,'Network is unavailable, Please connect to network and try again');
    }

    RootConfigService rootConfigService = new RootConfigService();
    RootConfig rootConfig = null;
    try {
      rootConfig = await rootConfigService.callRootConfigService();
    } on  UAException catch (e) {
//    // Error - exit app
      throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error fetching AppMetaData - exit");
    }
    if (rootConfig == null) {
      // Error - exit app
      throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error fetching rootconfig (null) - exit");
    }
    this.lastSyncTime = DateTime.now().millisecondsSinceEpoch;

    return rootConfig;
  }

  Future<RootConfig> fetchFromServerForBackgroundSync() async {
    int syncFrequency =
        UAAppContext.getInstance().appMDConfig.serviceFrequency.rootconfig;
    if ((DateTime.now().millisecondsSinceEpoch - lastSyncTime) <=
        syncFrequency) {
      print("Root Config was synced recently, will try in future");
      return UAAppContext.getInstance().rootConfig;
    }
    return await fetchRootConfigFromServer();
  }
}
