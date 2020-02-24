import 'dart:convert';

import '../event/event_utils.dart';
import '../utils/network_utils.dart';
import 'package:http/http.dart' as http;
import '../resources/uniapp_response.dart';
import '../db/databaseHelper.dart';
import '../db/models/config_table.dart';
import '../models/root_config.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import '../error/all_custom_exception.dart';
import '../log/uniapp_logger.dart';
class RootConfigService {
  DatabaseHelper _mDbHelper;
  final logger = getLogger(LogTags.ROOT_CONFIG);
  RootConfigService() {
    _mDbHelper = DatabaseHelper();
  }

  Future<RootConfig> callRootConfigService() async {
    // Logic
    // 1. Fetch from Server
    // 2. Save to DB
    // 3. Return the AppMetaData
    // 4. In case of error - return the error type

    String content = await fetchFromServer();
    if (content == null) {
      return null;
    } else if (content == CommonConstants.NO_NEW_ROOT_CONFIG) {
//            return existing rootConfig
      return UAAppContext.getInstance().rootConfig;
    } else {
      RootConfig rootConfig = null;
      // Add new RootConfig to the DB
      logger.i('UNIAPP ROOT CONFIG RESPONSE : ${content}');

      rootConfig = RootConfig.fromJson(jsonDecode(content));

      UAAppContext.getInstance().rootConfig = rootConfig;

      storeToDB(content, rootConfig);
      //todo download image
      return rootConfig;
    }
  }

  Future<String> fetchFromServer() async {
    var body = createRequestParams();
    if(body == null) {
      // throw exception
      //  log error
      logger.e("Error creating request parameters - Root Config");
      throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error creating request parameters - Root Config");
    }
    bool isOnline = await networkUtils.hasActiveInternet();
    if(!isOnline) {
      logger.d("cannot fetch RootConfig from server, app is offline");
      throw new AppOfflineException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "App is Offline");
    }
    
    final response = await http.post(CommonConstants.BASE_URL + "rootconfigdata",
        headers: {"Content-Type": "application/json"},
        body: body);

    if (response == null ||
        response.statusCode != 200 ||
        response.body == null) {
      // throw exception log it
      logger.e("Error getting RootConfig from server ");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting RootConfig from server ");
    }
    UniappResponse uniappResponse = UniappResponse.fromJson(jsonDecode(response.body));
    if (uniappResponse == null || uniappResponse.result == null) {
      // throw exception log it
      logger.e("Error getting RootConfig from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting RootConfig from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");

    }
    int code = uniappResponse.result.status;
    String content;
    if (code == 200) {
      logger.d("Successfully fetched RootConfig from server");
      content = uniappResponse.result.content;
      if (content == "{}") {
        // Same version on the server
        return CommonConstants.NO_NEW_ROOT_CONFIG;
      } else {
        return content;
      }
    } else if (code == 350) {
      //  token expired , broadcast logout event
      eventBus.fire(TokenExpiredEvent());
      // stop backround sync service
      throw new TokenExpiredException(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Tokn expired");
      //:todo update sharedpreferences
    } else {
      // log error and exception
      logger.e("Error getting RootConfig from server (invalid data) : response code : ${code}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
          , 'Error getting RootConfig from server (invalid data) : response code : ${code}');
    }
  }

  void storeToDB(String content, RootConfig rootConfig) {
    // Add new RootConfig to the DB
    ConfigFile configFile = ConfigFile(
        rootConfig.userId,
        CommonConstants.ROOT_CONFIG_NAME,
        content,
        rootConfig.version,
        rootConfig.currentServerTime);
    _mDbHelper.insertConfig(configFile);
  }

  String createRequestParams() {
    Map rootConfigParameters = new Map();
    rootConfigParameters[CommonConstants.ROOT_CONFIG_USER_ID_KEY] =
        UAAppContext.getInstance().userID;
    rootConfigParameters[CommonConstants.ROOT_CONFIG_TOKEN_KEY] =
        UAAppContext.getInstance().token;
    rootConfigParameters[CommonConstants.ROOT_CONFIG_SUPER_APP_KEY] =
        CommonConstants.SUPER_APP_ID;
    RootConfig existingRootConfig = UAAppContext.getInstance().rootConfig;

    int version = existingRootConfig == null ? 0 : existingRootConfig.version;
    String existingVersion = version == 0 ? '0' : version.toString();
    rootConfigParameters[CommonConstants.ROOT_CONFIG_VERSION_KEY] =
        existingVersion;
    var body = json.encode(rootConfigParameters);
    return body;
  }
}
