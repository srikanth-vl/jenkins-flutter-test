import 'dart:async';
import 'dart:convert';
import 'dart:io';
import '../db/models/user_meta_data_table.dart';
import '../error/server_fetch_exception.dart';
import '../error/ua_app_error_codes.dart';
import '../ua_app_context.dart';
import 'package:flutter/cupertino.dart';
import 'package:toast/toast.dart';
import 'uniapp_response.dart';
import '../utils/common_constants.dart';
import '../models/user_login_data.dart';
import 'package:http/http.dart';
import '../utils/common_constants.dart';
import '../log/uniapp_logger.dart';

class LoginApiProvider {
  Logger logger = getLogger('LoginApiProvider');

  Future<UserMetaDataTable> authenticateLoginCredentials(
      String username, String password) async {
    BuildContext context = UAAppContext.getInstance().context;
    String url = CommonConstants.BASE_URL + "authenticate";
    Map<String, String> header = {"Content-type": "application/json"};

    Map parameters = {
      'mobile': username,
      'password': password,
      'superapp': CommonConstants.SUPER_APP_ID
    };
    var jsonbody = json.encode(parameters);
    try {
      Response response = await post(url, headers: header, body: jsonbody);

      if (response == null ||
          response.statusCode != 200 ||
          response.body == null) {
        //todo log error
        //  Utils.logError(UAAppErrorCodes.SERVER_FETCH_ERROR, "Error getting Login Response from server");
        throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR,
            "Error getting Login Response from server");
      }
      // check the status code for the result
      switch (response.statusCode) {
        case 200:
          print("UNIAPP LOGIN RESPONSE " + "${response.body}");
          UniappResponse uniappResponse =
              UniappResponse.fromJson(json.decode(response.body));
          if (uniappResponse == null || uniappResponse.result == null) {
            logger.e(
                "Error getting AppMetaData from server , uniapp response ::${uniappResponse == null ? uniappResponse : uniappResponse.toJson()}");
            throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR,
                "Error getting AppMetaData from server , uniapp response ::${uniappResponse == null ? uniappResponse : uniappResponse.toJson()}");
          }
          int code = uniappResponse.result.status;
          String content;
          if (code == 200) {
            content = uniappResponse.result.content;
            return UserMetaDataTable.fromJson(jsonDecode(content), password);
          } else if (code == 375) {
            Toast.show(CommonConstants.USER_NOT_FOUND, context,
                duration: Toast.LENGTH_LONG, gravity: Toast.BOTTOM);
          } else if (code == 370) {
            Toast.show(CommonConstants.USER_PASSWORD_MISMATCH, context,
                duration: Toast.LENGTH_LONG, gravity: Toast.BOTTOM);
          } else {
            Toast.show(CommonConstants.SOMETHING_WENT_WRONG, context,
                duration: Toast.LENGTH_LONG, gravity: Toast.BOTTOM);
          }
          return null;
      }
    } on SocketException catch (e) {
      logger.e('Could not connect to server');
      throw new ServerFetchException(
          UAAppErrorCodes.SERVER_FETCH_ERROR, 'Could not connect to server');
    }
  }
}
