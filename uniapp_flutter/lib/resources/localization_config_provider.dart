import 'dart:convert';
import 'dart:io';

import '../localization/localization_utils.dart';
import 'localization_config_service.dart';
import '../utils/common_utils.dart';
import '../utils/network_utils.dart';
import '../utils/common_constants.dart';
import '../error/all_custom_exception.dart';
import '../ua_app_context.dart';
import 'package:logger/logger.dart';
import '../log/uniapp_logger.dart';
class LocalizationConfigProvider{

  static final LocalizationConfigProvider ourInstance = new LocalizationConfigProvider();
  int lastSyncTime;
  Logger logger = getLogger("LocalizationConfigProvider");
  LocalizationConfigProvider() {
    this.lastSyncTime = 0;
  }

  static LocalizationConfigProvider getInstance() {
    return ourInstance;
  }

  Future initLocalizationConfig() async{

    // Fetch localizationConfig from the file
    String localizationConfig;
    Map<String, dynamic> localizationConfigJSON = LocalizationUtils().getData();
    if (localizationConfigJSON == null || localizationConfigJSON.isEmpty) {
      // Check if server available
      bool isOnline =  await networkUtils.hasActiveInternet();
      if (!isOnline) {
        logger.e("App is offline");
        CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
        return;
      }
      // Fetching the app meta config from the server
      try {
        localizationConfig = await fetchLocalizationConfigFromServer();
        localizationConfigJSON = jsonDecode(localizationConfig);
        logger.i(' - Fetched Localization Config from Server: $localizationConfig');
      } on AppOfflineException catch (e) {
        CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
      } on SocketException catch (e){
        CommonUtils.showToast(CommonConstants.NETWORK_ERROR, UAAppContext.getInstance().context);
      } on UAException catch (e) {
        CommonUtils.showToast(CommonConstants.SOMETHING_WENT_WRONG, UAAppContext.getInstance().context);
      }
    } else {
      logger.i(' - Fetched Localization Config from Local file: ${localizationConfig}');
    }
    if(localizationConfigJSON != null) {
      UAAppContext.getInstance().setLocalizationJson(localizationConfigJSON);
    }
  }

  Future<String> fetchLocalizationConfigFromServer() async {
    // Check if server available
    NetworkUtils networkUtils = NetworkUtils();
    bool isOnline = await networkUtils.hasActiveInternet();
    if (!isOnline) {
      //throw appOffline exception
      logger.e("App is offline - during Map Config fetch");
      throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during localizationConfig fetch");
    }

    LocalizationService localizationService = new LocalizationService();
    String localizationConfig;
    try {
      localizationConfig = await localizationService.callLocalizationService();
    } on UAConfigException {
      // Error - exit app
      throw new  AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error fetching localizationConfig - exit");
    }
    if (localizationConfig == null) {
      // Error - exit app
      throw new AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error fetching localizationConfig (null) - exit");
    }
    this.lastSyncTime = DateTime.now().millisecondsSinceEpoch;
    Map<String, dynamic> localizationConfigjson = jsonDecode(localizationConfig);
    UAAppContext.getInstance().setLocalizationJson(localizationConfigjson);
    return localizationConfig;
  }

  Future fetchFromServerForBackgroundSync() async {

    int syncFrequency = UAAppContext.getInstance().appMDConfig.serviceFrequency.projectlist;
    if ((DateTime.now().millisecondsSinceEpoch - lastSyncTime) <= syncFrequency) {
      // todo log message
      print("LogTags.APP_BACKGROUND_SYNC"+ "localizationConfig was synced recently, will try in future");

    }
    fetchLocalizationConfigFromServer();
  }
}