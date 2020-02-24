import 'dart:convert';
import 'dart:io';

import '../error/all_custom_exception.dart';
import '../log/uniapp_logger.dart';
import '../resources/uniapp_response.dart';
import '../ua_app_context.dart';
import '../utils/common_constants.dart';
import 'package:flutter/cupertino.dart';
import 'package:http/http.dart';
import 'package:toast/toast.dart';

class ChangePasswordProvider {
  Logger logger = getLogger('GenerateOTPProvider');
  BuildContext context;

  Future<String> requestOTPGeneration(String username) async {
    context = UAAppContext.getInstance().context;
    String url = CommonConstants.BASE_URL + "generatepasswordresetotp";
    Map <String, String> header = {"Content-Type": "application/json"};

    Map parameters = {
      'user_id': username,
      'super_app': CommonConstants.SUPER_APP_ID
    };

    var body = json.encode(parameters);

    try {
      Response response = await post(url, headers: header, body: body);

      if(response == null || response.body == null || response.statusCode != 200) {
        throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR,
            "Error in getting response from the server.");
      }

      if(response.statusCode == 200){
        UniappResponse uniappResponse = UniappResponse.fromJson(json.decode(response.body));
        String message = uniappResponse.result.message;
        switch(uniappResponse.result.status){
          // Mobile number not registered
          case 125:
            Toast.show(message, context,
              duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
          break;
          // SMS Sending Failed
          case 126:
            Toast.show(message, context,
            duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
          break;

          case 200:
            UAAppContext.getInstance().userID = username; // For holding username
            return message;

          // No user found
          case 375:
            Toast.show(CommonConstants.USER_NOT_FOUND, context,
                duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
          break;

          // Handling remaining issues
          default:
            Toast.show(CommonConstants.SOMETHING_WENT_WRONG, context,
              duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
        }
        return null;
      }
    } on SocketException catch(e) {
      logger.e('Could not connect to the server');
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, 'Could not connect to the server');
    }
  }

  Future<bool> changeUserPassword(String otp, String newPassword) async {
    context = UAAppContext.getInstance().context;
    String url = CommonConstants.BASE_URL + 'resetpassword';
    Map<String, String> header = {"Content-Type": "application/json"};
    String username = UAAppContext.getInstance().userID;
    print(otp);
    var _otp = int.parse(otp);

    Map parameters = {
      'user_id': username,
      'super_app': CommonConstants.SUPER_APP_ID,
      'otp': _otp,
      'new_password': newPassword
    };

    var body = json.encode(parameters);

    try {
      Response response = await post(url, headers: header, body: body);

      if(response == null || response.body == null || response.statusCode != 200) {
        throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR,
            "Error in getting response from the server.");
      }

      if(response.statusCode == 200){
        UniappResponse uniappResponse = UniappResponse.fromJson(json.decode(response.body));
        String msg = uniappResponse.result.message;

        // Note: case if new and confirm password don't match handled in widget
        switch(uniappResponse.result.status) {
          //Invalid or expired OTP entered
          case 121:
            Toast.show(msg, context, duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
          break;

          // Old password and new password is same
          case 123:
            Toast.show(msg, context, duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
          break;

          case 200:
            UAAppContext.getInstance().userID = null; // discarding value of userId
            return true;

          // User id not found
          case 375:
            Toast.show(msg, context, duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
          break;

          default:
            Toast.show(CommonConstants.SOMETHING_WENT_WRONG, context,
            duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
        }
        return false;
      }
    } on SocketException catch(e) {
      logger.e('Could not connect to the server');
      throw new ServerFetchException(UAAppErrorCodes.SERVER_FETCH_ERROR, 'Could not connect to the server');
    }
  }
}
