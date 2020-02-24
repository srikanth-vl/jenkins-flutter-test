import 'package:shared_preferences/shared_preferences.dart';
import 'common_constants.dart';

class SharedPreferenceUtil {
  Future<bool> getBoolPreference(String key) async {
    SharedPreferences preferences = await SharedPreferences.getInstance();
    return preferences.getBool(key) ?? false;
  }

  Future<int> getIntPreference(String key) async {
    SharedPreferences preferences = await SharedPreferences.getInstance();
    return preferences.getInt(key) ?? 0;
  }

  Future<double> getDoublePreference(String key) async {
    SharedPreferences preferences = await SharedPreferences.getInstance();
    return preferences.getDouble(key) ?? 0.0;
  }

  Future<String> getStringPreference(String key) async {
    SharedPreferences preferences = await SharedPreferences.getInstance();
    return preferences.getString(key) ?? null;
  }

  setPreferenceValue(String key, var value, String dataType) async {
    SharedPreferences preferences = await SharedPreferences.getInstance();
    Future<bool> status;

    switch (dataType) {
      case CommonConstants.PREFERENCE_TYPE_INT:
        status = preferences.setInt(key, value);
        break;
      case CommonConstants.PREFERENCE_TYPE_BOOL:
        status = preferences.setBool(key, value);
        break;
      case CommonConstants.PREFERENCE_TYPE_DOUBLE:
        status = preferences.setDouble(key, value);
        break;
      case CommonConstants.PREFERENCE_TYPE_STRING:
        status = preferences.setString(key, value);
        break;
      case CommonConstants.PREFERENCE_TYPE_STRING_LIST:
        status = preferences.setStringList(key, value);
        break;
    }

    return status;
  }
}

final sharedPreferenceUtil = new SharedPreferenceUtil();
