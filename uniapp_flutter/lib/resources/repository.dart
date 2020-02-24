import 'dart:async';
import 'dart:convert';
import '../blocs/change_password_bloc.dart';
import '../db/models/user_meta_data_table.dart';

import '../models/localization_config.dart';
import '../models/map_config.dart';
import '../models/root_config.dart';
import '../models/user_login_data.dart';
import '../resources/localization_config_api_provider.dart';
import '../resources/map_config_api_provider.dart';
import '../resources/root_config_provider.dart';
import '../utils/common_constants.dart';
import '../ua_app_context.dart';
import 'login_api_provider.dart';
import '../resources/map_config_provider.dart';
import '../resources/change_password_provider.dart';
import '../db/models/config_table.dart';

class Repository {
  final loginApiProvider = LoginApiProvider();
  final localizationApiProvider = LocalizationConfigApiProvider();
  final rootConfigApiProvider = RootConfigProvider.getInstance();
  final mapConfigApiProvider = MapConfigProvider();
  final changePasswordProvider = ChangePasswordProvider();

  Future<UserMetaDataTable> authenticateCredentials(
          String username, String password) =>
      loginApiProvider.authenticateLoginCredentials(username, password);

  Future<String> requestOTPGeneration(String username) =>
      changePasswordProvider.requestOTPGeneration(username);

  Future<bool> changeUserPassword(String otp, String newPassword) =>
      changePasswordProvider.changeUserPassword(otp, newPassword);

  Future<LocalizationConfig> fetchLocalizationConfig() =>
      localizationApiProvider.fetchLocalizationConfig();

  Future<RootConfig> fetchRootConfig(String username, String token) =>
      rootConfigApiProvider.initRootConfig();

  Future<MapConfig> fetchMapConfig(String username, String token) async {
    MapConfig mapConfig = await mapConfigApiProvider.initMapConfig();
    if (mapConfig != null) {
//      if(UAAppContext.getInstance().unifiedAppDBHelper != null) {
//        ConfigFile configFile = ConfigFile(username, CommonConstants.ROOT_CONFIG_DB_NAME, jsonEncode(mapConfig.toJson()), mapConfig.version, DateTime.now().millisecondsSinceEpoch );
//        UAAppContext.getInstance().unifiedAppDBHelper.insertConfig(configFile);
//        UAAppContext.getInstance().mapConfig =  mapConfig;
//
//      }
      return mapConfig;
    }
    return null;
  }
}
