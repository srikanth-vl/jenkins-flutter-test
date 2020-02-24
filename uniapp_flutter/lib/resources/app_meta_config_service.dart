import 'dart:async';
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../db/databaseHelper.dart';
import '../db/models/config_table.dart';
import '../models/app_meta_data_config.dart';
import '../utils/common_constants.dart';
import 'uniapp_response.dart';
import '../ua_app_context.dart';
import '../log/uniapp_logger.dart';
import '../error/all_custom_exception.dart';
import '../event/event_utils.dart';
class AppMetaConfigService {
  DatabaseHelper databaseHelper =  DatabaseHelper();
  Logger logger = getLogger('AppMetaConfigService');
  Future<AppMetaDataConfig> callAppMetaDataService(String appMDVersion) async {

    // Logic
    // 1. Fetch from Server
    // 2. Save to DB
    // 3. Return the AppMetaData
    // 4. In case of error - return the error type
    AppMetaDataConfig appMetaData = null;
    String content = await fetchFromServer(appMDVersion);

    if (content == null) {
      return null;
    } else if (content =='{}') {
      ConfigFile existingAppMetaConfigFile = await databaseHelper.getConfig(CommonConstants.DEFAULT_USER, CommonConstants.APP_META_CONFIG_NAME);
      String existingAppMetaConfig = existingAppMetaConfigFile.content;
      Map formJsonMap =jsonDecode(existingAppMetaConfig);
      appMetaData = AppMetaDataConfig.fromJson(formJsonMap);
    } else {
      // Add new AppMataDataConfig to the App Context
      Map formJsonMap =jsonDecode(content);
      appMetaData = AppMetaDataConfig.fromJson(formJsonMap);
      //:TODO  Download if any new images that come in the AppMDConfig
      await storeToDB(content, appMetaData);
    }
    UAAppContext.getInstance().appMDConfig = appMetaData;
    return appMetaData;
  }
  Future<String> fetchFromServer(String appMDVersion) async {
    var body  = createRequestParams('0');
    if(body == null) {
      // throw exception
      logger.e("Error creating request parameters - AppMetaData Config");
      throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error creating request parameters - AppMetaData Config");
    }

    final response = await http.post(CommonConstants.BASE_URL + "appmetaconfigjson",headers:{"Content-Type": "application/json"},body:body);
    if(response == null || response.statusCode != 200 || response.body ==  null) {
      // throw exception log it
      logger.e('Error getting AppMetaData from server : received responseString : ${response}');
      throw ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR
          , 'Error getting AppMetaData from server : received responseString : ${response}' );
    }
    UniappResponse uniappResponse = UniappResponse.fromJson(jsonDecode(response.body));
    if(uniappResponse ==  null || uniappResponse.result ==  null ) {
      // throw exception log it
      logger.e("Error getting AppMetaData from server , uniapp response ::${uniappResponse == null ? uniappResponse : uniappResponse.toJson()}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting AppMetaData from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
    }
    int code = uniappResponse.result.status;
    String content ;
    if (code == 200) {
      content = uniappResponse.result.content;
      if (content == "{}") {
        // Same version on the server
        return '{}';
      } else {
        return content;
      }
    } else if (code == 350) {
      // token expired broadcast logout event
      eventBus.fire(TokenExpiredEvent());
      // stop backround sync service
      throw new TokenExpiredException(UAAppErrorCodes.USER_TOKEN_EXPIRY_ERROR, "Tokn expired");
      //:todo update sharedpreferences
    } else {
      logger.e('Error getting AppMetaData from server (invalid data) : uniapp response code : ${uniappResponse.result.status}');
      throw ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
          , 'Error getting AppMetaData from server (invalid data) : uniapp response code : ${uniappResponse.result.status}');
    }
  }

  createRequestParams(String appMDVersion) {
    Map data = {
      'superapp': CommonConstants.SUPER_APP_ID,
      'versionId':'0'
    };
    var body = json.encode(data);
    return body;

  }

  storeToDB(String content, AppMetaDataConfig appMetaData) async {
    // Add new AppMetaDataConfig to the DB
    ConfigFile configFile =  ConfigFile(CommonConstants.DEFAULT_USER, CommonConstants.APP_META_CONFIG_NAME, content, int.parse(appMetaData.version), 0);
    await  databaseHelper.insertConfig(configFile);
  }
}
