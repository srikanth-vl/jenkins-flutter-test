import '../db/models/config_table.dart';
import '../models/map_config.dart';
import '../utils/common_utils.dart';
import '../utils/network_utils.dart';
import 'dart:convert';
import 'dart:io';
import '../utils/common_constants.dart';
import '../error/all_custom_exception.dart';
import '../ua_app_context.dart';
import 'map_config_service.dart';
import 'package:logger/logger.dart';
import '../error/all_custom_exception.dart';
import '../log/uniapp_logger.dart';
class MapConfigProvider{

  static final MapConfigProvider ourInstance = new MapConfigProvider();
  int lastSyncTime;
Logger logger = getLogger("MapConfigProvider");
  MapConfigProvider() {
    this.lastSyncTime = 0;
  }

  static MapConfigProvider getInstance() {
    return ourInstance;
  }

  Future<MapConfig> initMapConfig() async{

    // Fetch rootConfig from the DB
    String userId = UAAppContext.getInstance().userID;

    ConfigFile mapConfigFile = await UAAppContext.getInstance().unifiedAppDBHelper.getConfig(userId,
        CommonConstants.MAP_CONFIG_DB_NAME);

    MapConfig mapConfig = null;

    if (mapConfigFile == null || mapConfigFile.content == null || mapConfigFile.content == '') {
      UAAppContext.getInstance().mapConfig = null;
      // Check if server available
      NetworkUtils networkUtils = NetworkUtils();
      bool isOnline = await networkUtils.hasActiveInternet();
      if (!isOnline) {
        // throw exception
        logger.e("App is offline");
        CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
      }
      try {
        // Fetching the app meta config from the server
        logger.i("mapconfig fetched from server");
        mapConfig = await fetchMapConfigFromServer();
        logger.i("mapconfig fetched from server");
      } on AppOfflineException catch (e) {
        CommonUtils.showToast(CommonConstants.NETWORK_UNAVAILABLE, UAAppContext.getInstance().context);
      } on SocketException catch (e){
        CommonUtils.showToast(CommonConstants.NETWORK_ERROR, UAAppContext.getInstance().context);
      } on UAException catch (e) {
        CommonUtils.showToast(CommonConstants.SOMETHING_WENT_WRONG, UAAppContext.getInstance().context);
      }
      //todo log error
    } else {
      // Initializing singleton class
      logger.i("mapconfig fetched from DB");
      mapConfig = MapConfig.fromJson(jsonDecode(mapConfigFile.content));
    }
    UAAppContext.getInstance().mapConfig = mapConfig;
    return mapConfig;
  }

  Future<MapConfig> fetchMapConfigFromServer() async {
    // Check if server available
    NetworkUtils networkUtils = NetworkUtils();
    bool isOnline = await networkUtils.hasActiveInternet();
    if (!isOnline) {
      //throw appOffline exception
      logger.e("App is offline - during Map Config fetch");
      throw new AppOfflineException(UAAppErrorCodes.APP_OFFLINE_ERROR, "App is offline - during Map Config fetch");
    }

    MapConfigService mapConfigService = new MapConfigService();
    MapConfig mapConfig = null;
    try {
      mapConfig = await mapConfigService.callMapConfigService();
    } on UAConfigException {
      // Error - exit app
      throw new  AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error fetching Map Config - exit");
    }
    if (mapConfig == null) {
      // Error - exit app
      throw new AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error fetching MapConfig (null) - exit");
    }
    this.lastSyncTime = DateTime.now().millisecondsSinceEpoch;
    return mapConfig;
  }

  Future<MapConfig> fetchFromServerForBackgroundSync() async {

    int syncFrequency = UAAppContext.getInstance().appMDConfig.serviceFrequency.mapconfig;
    if ((DateTime.now().millisecondsSinceEpoch - lastSyncTime) <= syncFrequency) {
      // todo log message
      print("LogTags.APP_BACKGROUND_SYNC"+ "Map Config was synced recently, will try in future");
      return UAAppContext.getInstance().mapConfig;
    }
    return fetchMapConfigFromServer();
  }
}