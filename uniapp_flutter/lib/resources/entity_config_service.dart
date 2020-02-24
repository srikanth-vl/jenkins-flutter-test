import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/entity_meta_data_configuration.dart';
import '../event/event_utils.dart';
import '../utils/network_utils.dart';
import '../resources/uniapp_response.dart';
import '../db/databaseHelper.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import '../error/all_custom_exception.dart';
import '../log/uniapp_logger.dart';

class EntityMetaDataConfigurationService {
  DatabaseHelper _mDbHelper;
  final logger = getLogger("EntityMetaDataConfigurationService");
  EntityMetaDataConfigurationService() {
    _mDbHelper = DatabaseHelper();
  }

  Future<EntityMetaDataConfiguration> callEntityMetaDataConfigurationService(String appId) async {
    // Logic
    // 1. Fetch from Server
    // 2. Save to DB
    // 4. In case of error - return the error type

    String content = await fetchFromServer(appId);
    if (content == null) {
      return null;
    } else if (content == CommonConstants.NO_NEW_ROOT_CONFIG) {
//            NO NEW EntityMetaDataConfiguration
      return null;
    } else {
      EntityMetaDataConfiguration entityMetaDataConfiguration = null;
      // Add new EntityMetaDataConfiguration to the DB
      logger.i('UNIAPP EntityMetaDataConfiguration RESPONSE : ${content}');

      entityMetaDataConfiguration = EntityMetaDataConfiguration.fromJson(jsonDecode(content));
      await storeToDB(content, entityMetaDataConfiguration);

    }
  }

  Future<String> fetchFromServer(String appId) async {
    var body = createRequestParams(appId);
    if(body == null) {
      // throw exception
      //  log error
      logger.e("Error creating request parameters - EntityMetaDataConfiguration");
      throw new AppCriticalException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "Error creating request parameters - EntityMetaDataConfiguration");
    }
    bool isOnline = await networkUtils.hasActiveInternet();
    if(!isOnline) {
      logger.d("cannot fetch EntityMetaDataConfiguration from server, app is offline");
      throw new AppOfflineException(UAAppErrorCodes.ROOT_CONFIG_FETCH, "App is Offline");
    }

    final response = await http.post(CommonConstants.BASE_URL + "entitymetadataconfig",
        headers: {"Content-Type": "application/json"},
        body: body);

    if (response == null ||
        response.statusCode != 200 ||
        response.body == null) {
      // throw exception log it
      logger.e("Error getting EntityMetaDataConfiguration from server ");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting EntityMetaDataConfiguration from server ");
    }
    UniappResponse uniappResponse = UniappResponse.fromJson(jsonDecode(response.body));
    if (uniappResponse == null || uniappResponse.result == null) {
      // throw exception log it
      logger.e("Error getting EntityMetaDataConfiguration from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting EntityMetaDataConfiguration from server , uniapp response ::${uniappResponse == null ?uniappResponse : uniappResponse.toJson()}");

    }
    int code = uniappResponse.result.status;
    String content;
    if (code == 200) {
      logger.d("Successfully fetched EntityMetaDataConfiguration from server");
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
      logger.e("Error getting EntityMetaDataConfiguration from server (invalid data) : response code : ${code}");
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_INVALID_DATA_ERROR
          , 'Error getting EntityMetaDataConfiguration from server (invalid data) : response code : ${code}');
    }
  }

  Future storeToDB(String content, EntityMetaDataConfiguration entityMetaDataConfiguration) async  {
    // Add new EntityMetaDataConfiguration to the DB
    if(entityMetaDataConfiguration != null && entityMetaDataConfiguration.entities != null && entityMetaDataConfiguration.entities.isNotEmpty) {
      await _mDbHelper.insertEntityMetaDataConfiguration(entityMetaDataConfiguration);
    }
  }

  String createRequestParams(String appId) {
    Map entityMetaDataConfigParameters = new Map();
    entityMetaDataConfigParameters[CommonConstants.ENTITY_CONFIG_REQUEST_USER_ID_KEY] =
        UAAppContext.getInstance().userID;
    entityMetaDataConfigParameters[CommonConstants.ENTITY_CONFIG_REQUEST_TOKEN_KEY] =
        UAAppContext.getInstance().token;
    entityMetaDataConfigParameters[CommonConstants.ENTITY_CONFIG_REQUEST_SUPER_APP_KEY] =
        CommonConstants.SUPER_APP_ID;
    entityMetaDataConfigParameters[CommonConstants.ENTITY_CONFIG_REQUEST_APP_ID_KEY] =
        appId;

    var body = json.encode(entityMetaDataConfigParameters);
    return body;
  }
}
