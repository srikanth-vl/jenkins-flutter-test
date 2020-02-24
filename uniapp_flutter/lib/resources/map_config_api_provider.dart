import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart';
import '../models/map_config.dart';
import '../resources/uniapp_response.dart';
import '../utils/common_constants.dart';

class MapConfigApiProvider {
  final _base_url = 'http://138.68.30.58:9000/api/uniapp/';

  Future<MapConfig> fetchMapConfig(String username, String token) async {
    String url = _base_url + "mapconfigdata";

    Map<String, String> header = {"Content-type": "application/json"};
    String super_app_id = CommonConstants.SUPER_APP_ID;

    Map parameters = {
      'userid': username,
      'token': token,
      'superapp': super_app_id
    };
    var jsonbody = json.encode(parameters);

    Response response = await post(url, headers: header, body: jsonbody);
    if (response == null || response.statusCode != 200 || response.body ==  null) {
      //todo throw exception log it
    }

    // check the status code for the result
    UniappResponse uniappResponse = UniappResponse.fromJson(jsonDecode(response.body));
    if(uniappResponse ==  null || uniappResponse.result ==  null ) {
      //todo throw exception log it
      return null;
    }
    int code = uniappResponse.result.status;
    String content ;
    if (code == 200) {
      content = uniappResponse.result.content;
      if (content == "{}") {
        // Same version on the server
        return null;
      } else {
        return  MapConfig.fromJson(json.decode(content));;
      }
    } else if (code == 350) {

      // todo token expired broadcast logout event
      // todo stop backround sync service

      //todo update sharedpreferences
      return null;
    }
  }
}
