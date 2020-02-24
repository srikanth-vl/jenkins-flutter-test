import 'dart:async';
import 'dart:convert';

import '../db/databaseHelper.dart';
import '../utils/common_constants.dart';
import '../db/models/config_table.dart';
import '../models/app_meta_data_config.dart';
import 'app_meta_config_service.dart';
import '../ua_app_context.dart';
import '../utils/network_utils.dart';
import '../utils/common_utils.dart';
import '../error/all_custom_exception.dart';
import '../log/uniapp_logger.dart';
import 'dart:io';

class AppMetaConfigProvider {
  final DatabaseHelper databaseHelper =  DatabaseHelper();
  Logger  logger = getLogger("AppMetaConfigProvider");
  AppMetaConfigProvider._privateConstructor();
  static final AppMetaConfigProvider _instance = AppMetaConfigProvider._privateConstructor();
  static AppMetaConfigProvider get instance { return _instance;}
  AppMetaConfigService _appMetaConfigService = AppMetaConfigService();
  int _lastSyncTime = 0 ;

  Future<AppMetaDataConfig> initAppMDConfig() async {
    // Fetch appMetaData from the DB
    AppMetaDataConfig appMetaConfig = await fetchFromDB();
    if (appMetaConfig == null ) {
      // Check if server available
      bool isOnline = await networkUtils.hasActiveInternet();
      if(!isOnline) {
        logger.e("App is offline");
        CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
        return null;
      }
      // Fetching the app meta config from the server
      try {
        appMetaConfig = await _appMetaConfigService.callAppMetaDataService('0');
        logger.i("AppMetadata Config fetched from server");
      }  on AppOfflineException catch (e) {
        CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
      } on SocketException catch (e){
        CommonUtils.showToast(CommonConstants.NETWORK_ERROR, UAAppContext.getInstance().context);
      } on UAException catch (e) {
        CommonUtils.showToast(CommonConstants.SOMETHING_WENT_WRONG, UAAppContext.getInstance().context);
      }
    }
    UAAppContext.getInstance().appMDConfig = appMetaConfig;
    return appMetaConfig;
    // Initializing singleton class

  }

  Future<AppMetaDataConfig> fetchAppMetaDataFromServer(String appMDVersion) async {
    bool isOnline = await networkUtils.hasActiveInternet();
    if(!isOnline) {
      logger.e('Network is unavailable, Please connect to network and try again');
      throw AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR,'Network is unavailable, Please connect to network and try again');
    }
    //:todo throw exception if some exception occurred
    AppMetaDataConfig appMetaData = null;
    try {
      appMetaData =
      await _appMetaConfigService.callAppMetaDataService(appMDVersion);
    }  on  UAException catch (e) {
//    // Error - exit app
      throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error fetching AppMetaData - exit");
    }
//
    if (appMetaData == null) {
//    // Error - exit app
      throw new AppCriticalException(UAAppErrorCodes.APP_MD_FETCH, "Error fetching AppMetaData (null) - exit");
    }
    // set last sync timestamp
    _lastSyncTime = DateTime.now().millisecondsSinceEpoch;
    return appMetaData;
  }

  //todo throw exception if some exception occurred
  fetchFromServerForBackgroundSync() async {
    AppMetaDataConfig appMetaData = UAAppContext.getInstance().appMDConfig;
    String currentVersion ;
    if(appMetaData  != null) {
      currentVersion =  appMetaData.version.toString();
      int syncFrequency = appMetaData.serviceFrequency.appmetaconfig;
      if ((DateTime.now().millisecondsSinceEpoch - _lastSyncTime) <= syncFrequency) {
        logger.i("AppMetaData Config was synced recently, will try in future");
        return;
      }
    }
    await fetchAppMetaDataFromServer(currentVersion);
  }

  Future<AppMetaDataConfig> fetchFromDB() async{
    AppMetaDataConfig appMetaConfig ;
    ConfigFile existingAppMetaConfigFile =  await databaseHelper.getConfig(CommonConstants.DEFAULT_USER, CommonConstants.APP_META_CONFIG_NAME);
    if(existingAppMetaConfigFile != null && existingAppMetaConfigFile.content != null && existingAppMetaConfigFile.content != '') {
      String existingAppMetaConfig = existingAppMetaConfigFile.content;
      Map formJsonMap =jsonDecode(existingAppMetaConfig);
      appMetaConfig = AppMetaDataConfig.fromJson(formJsonMap);
    }
    return appMetaConfig;
  }

}