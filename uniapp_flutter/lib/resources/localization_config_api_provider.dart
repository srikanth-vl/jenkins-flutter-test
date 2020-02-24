import 'dart:async';
import 'dart:convert';

import '../models/localization_config.dart';
import 'package:http/http.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';

class LocalizationConfigApiProvider {
//  final _base_url = 'http://138.68.30.58:9000/api/uniapp/';

  Future<LocalizationConfig> fetchLocalizationConfig() async {
    String url = CommonConstants.BASE_URL + "localizationconfigdata";

    Map<String, String> header = {"Content-type": "application/json"};

    Map parameters = {
      'userid': UAAppContext.getInstance().userID,
      'token':  UAAppContext.getInstance().token,
      'superapp': CommonConstants.SUPER_APP_ID,
    };
    var jsonbody = json.encode(parameters);

    // print("LOCALIZATION BODY ${parameters.toString()}");

    Response response = await post(url, headers: header, body: jsonbody);

    // print("LOCALIZATION RESPONSE ${response.body.toString()}");

    // check the status code for the result
    if (response.statusCode == 200) {
      // If the call to the server was successful, parse the JSON
      // print("UNIAPP LOCALIZATION RESPONSE " + "${response.body}");
      return LocalizationConfig.fromJson(json.decode(response.body));
    } else {
      // If that call was not successful, throw an error.
      throw Exception('Failed to load post');
    }
  }
}
