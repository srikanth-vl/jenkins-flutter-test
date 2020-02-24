import 'dart:convert';
import 'dart:io';
import '../utils/screen_navigate_utils.dart';
import 'package:flutter/cupertino.dart';
import 'package:http/http.dart';
import 'package:toast/toast.dart';

import '../error/all_custom_exception.dart';
import '../error/server_fetch_exception.dart';
import '../log/uniapp_logger.dart';
import '../resources/uniapp_response.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import '../utils/shared_preference_util.dart';

class LogoutService {
  Logger logger = getLogger('Logout Service');

  offlineLogout(BuildContext context) {
    _updateSharedPreferences();
    _navigateToLoginScreen(context);
  }

  logout(BuildContext context) async {
    String url = CommonConstants.BASE_URL + 'logout';
    Map<String, String> header = {"Content-type": "application/json"};
    Map parameters = _createRequestParameters();

    var jsonbody = json.encode(parameters);
    try {
      Response response = await post(url, headers: header, body: jsonbody);
      if (response == null ||
          response.statusCode != 200 ||
          response.body == null) {
        logger.e("Error getting Logout response from server");
        throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR,
            "Error getting Logout Response from server");
      }

      // check the status code for the result
      switch (response.statusCode) {
        case 200:
          print("UNIAPP LOGOUT RESPONSE " + "${response.body}");

          UniappResponse uniappResponse =
              UniappResponse.fromJson(json.decode(response.body));
          if (uniappResponse == null || uniappResponse.result == null) {
            logger.e(
                "Error getting Logout response from server , uniapp response ::"
                "${uniappResponse == null ? uniappResponse : uniappResponse.toJson()}");
            throw new ServerFetchException(
                UAAppErrorCodes.SERVER_FETCH_ERROR,
                "Error getting logout response from server , uniapp response ::"
                "${uniappResponse == null ? uniappResponse : uniappResponse.toJson()}");
          }

          print("LOGOUT STATUS CODE : ${uniappResponse.result.status}");

          int code = uniappResponse.result.status;
          if (code == 200) {
            await _updateSharedPreferences();
            _navigateToLoginScreen(context);
          } else {
            Toast.show(uniappResponse.result.message, context,
                duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
          }
      }
    } on SocketException catch (exception) {
      _onErrorResponse();
    }
  }

  _onErrorResponse() {
    logger.e("Error fetching Logout response from server");
    Toast.show(CommonConstants.SOMETHING_WENT_WRONG,
        UAAppContext.getInstance().context,
        duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
    throw new ServerFetchException(
        UAAppErrorCodes.SERVER_FETCH_ERROR, 'Could not connect to server');
  }

  _createRequestParameters() {
    Map parameters = {
      'userid': UAAppContext.getInstance().userID,
      'token': UAAppContext.getInstance().token,
      'superapp': CommonConstants.SUPER_APP_ID
    };
    return parameters;
  }

  _updateSharedPreferences() async {
     await sharedPreferenceUtil.setPreferenceValue(
        CommonConstants.IS_LOGGED_IN_SHARED_PREFERENCE,
        false,
        CommonConstants.PREFERENCE_TYPE_BOOL);
  }

  _navigateToLoginScreen(BuildContext context) {
    UAAppContext.getInstance().isLoggedIn = false;
    ScreenNavigateUtils().navigateToLoginScreen(context, true, true);
  }
}

final logoutService = LogoutService();
