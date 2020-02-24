import '../models/legend_attribute.dart';

import '../event/event_utils.dart';
import 'dart:io';
import '../db/databaseHelper.dart';
import '../db/models/config_table.dart';
import '../resources/uniapp_response.dart';
import '../models/map_marker.dart';
import 'download_service.dart';
import '../models/map_config.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../error/all_custom_exception.dart';
import '../log/uniapp_logger.dart';
class MapConfigService {

  DatabaseHelper _databaseHelper;
  Logger logger =  getLogger("MapConfigService");
  MapConfigService() {
    _databaseHelper = UAAppContext.getInstance().unifiedAppDBHelper;
  }

  Future<MapConfig> callMapConfigService() async {

    // Logic
    // 1. Fetch from Server
    // 2. Save to DB
    // 3. Return the MapConfig
    // 4. In case of error - return the error type

    String content = await fetchFromServer();
    if (content == null) {
      return null;
    } else if (content ==(CommonConstants.NO_NEW_MAP_CONFIG)) {
      //  Log info
      logger.d("MapConfig : No New Version");
      return UAAppContext.getInstance().mapConfig;
    } else {
      MapConfig mapConfig = null;
      // Add new RootConfig to the DB
      mapConfig = MapConfig.fromJson(jsonDecode(content));

      await storeToDB(content, mapConfig);

      if (mapConfig.offlineMapFiles != null) {
        // TODO download mapfile
      }
      if (mapConfig.mapMarkers != null) {
        Directory dir = UAAppContext.getInstance().appDir;
        String filePath = dir.path + CommonConstants.ICON_PATH;
        for (String key in mapConfig.mapMarkers.keys) {
          // TODO download map markers
          for(MapMarker marker in mapConfig.mapMarkers[key]) {
            DownloadService().downloadFile(marker.iconUrl, marker.iconUrl.substring(marker.iconUrl.lastIndexOf("/") + 1), filePath);
            if( marker.legendAttributes!= null ) {
              for(LegendAttribute attr in marker.legendAttributes){
                DownloadService().downloadFile(attr.url, attr.url.substring(attr.url.lastIndexOf("/") + 1), filePath);
              }
            }
          }
        }
      }
      UAAppContext.getInstance().mapConfig = mapConfig;
      return mapConfig;
    }
  }

  Future<String> fetchFromServer() async{

    var requestParam = createRequestParams();
    if (requestParam == null || requestParam == '') {
      // throw exception
      // log error
      logger.e("Error creating request parameters - Map Config");
      throw new AppCriticalException(UAAppErrorCodes.MAP_CONFIG_FETCH, "Error creating request parameters - Map Config");
    }


    final response = await http.post(
        CommonConstants.BASE_URL +"mapconfigdata",
        headers: {"Content-Type": "application/json"},
        body: requestParam);

    if (response == null ||
        response.statusCode != 200 ||
        response.body == null) {
      // log error
      logger.e("Error getting MapConfig from server 2");
      // throw exception
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting MapConfig from server 2");
    }
    UniappResponse uniappResponse =
    UniappResponse.fromJson(jsonDecode(response.body));
    if (uniappResponse == null || uniappResponse.result == null) {
      // throw exception log it
      logger.e("Error getting MapConfig from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting MapConfig from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
    }
    int code = uniappResponse.result.status;
    String content;
    if (code == 200) {
      logger.d("Successfully fetched MapConfig from server");
      content = uniappResponse.result.content;
      if (content == "{}") {
        // Same version on the server
        return CommonConstants.NO_NEW_MAP_CONFIG;
      } else {
        return content;
      }
    } else if (code == 350) {
      //  token expired , broadcast logout event
      eventBus.fire(TokenExpiredEvent());
      // stop backround sync service
      throw new TokenExpiredException(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Tokn expired");
      //:todo update sharedpreferences
      return content;
    } else {
      // log error and exception
      logger.e("Error getting MapConfig from server (invalid data) : response code : ${code} received content : " + content.toString());
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
          , 'Error getting MapConfig from server (invalid data) : response code : ${code}');
    }
  }

  void storeToDB(String content, MapConfig mapConfig) async {
    // Add new RootConfig to the DB
    ConfigFile configFile = ConfigFile(UAAppContext.getInstance().userID, CommonConstants.MAP_CONFIG_DB_NAME, content, mapConfig.version, mapConfig.currenttime);
    // Add RootConfig to DB
    await _databaseHelper.insertConfig(configFile);
  }

  String createRequestParams() {
    Map mapConfigParameters = Map();
    mapConfigParameters[CommonConstants.MAP_CONFIG_USER_ID_KEY] = UAAppContext.getInstance().userID;
    mapConfigParameters[CommonConstants.MAP_CONFIG_TOKEN_KEY] = UAAppContext.getInstance().token;
    mapConfigParameters[CommonConstants.MAP_CONFIG_SUPER_APP_KEY] = CommonConstants.SUPER_APP_ID;

    MapConfig existingMapConfig = UAAppContext.getInstance().mapConfig;

    int version = existingMapConfig == null ? null : existingMapConfig.version;
    String existingVersion = version == null ? '0' : version.toString();
    mapConfigParameters[CommonConstants.MAP_CONFIG_VERSION_KEY]=  existingVersion;
    var body = json.encode(mapConfigParameters);
    return body;

  }
}
