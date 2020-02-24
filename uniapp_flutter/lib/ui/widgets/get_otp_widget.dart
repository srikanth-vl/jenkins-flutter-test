import '../../utils/common_constants.dart';

import '../../utils/screen_navigate_utils.dart';
import 'package:flutter/material.dart';
import 'package:toast/toast.dart';
import '../../resources/get_otp_bloc.dart';

class GetOTPWidget extends StatefulWidget{
  @override
  _GetOTPWidgetState createState() => _GetOTPWidgetState();

}

class _GetOTPWidgetState extends State<GetOTPWidget> {

  final usernameController = TextEditingController();
  GetOTPService getOTPService = GetOTPService();

  @override
  void dispose(){
    usernameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context){

    final usernameField = TextField(
      controller: usernameController,
      decoration: InputDecoration(
        prefixIcon: Icon(Icons.person),
        labelText: 'Enter your username',
        fillColor: Colors.white,
        filled: true,
        border: new OutlineInputBorder(
          borderRadius: new BorderRadius.circular(4.0),
          borderSide: new BorderSide(),
        ),
      ),
      keyboardType: TextInputType.emailAddress,
      style: new TextStyle(
        fontFamily: "Poppins",
      ),
    );

    final getOTPButton = Material(
        elevation: 4.0,
        borderRadius: BorderRadius.circular(4.0),
        color: Theme.of(context).accentColor,
        child: MaterialButton(
          minWidth: MediaQuery.of(context).size.width,
          padding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
          onPressed: () {
            String username = usernameController.text;
            if(username != null && username.isNotEmpty){
              _generateOTP(username);
            }
            else {
              Toast.show('Please enter your username', context, gravity: Toast.TOP);
              // Reason for displaying toast on top because then
              // the user can view the message even the virtual keyboard
              // is present on the screen.
            }
          },
          child: Text(
            'GET OTP',
            textAlign: TextAlign.center,
            //style:(color: Colors.white, fontWeight: FontWeight.bold),
          ),
        )
    );

    return Container(
        child: Padding(
            padding: const EdgeInsets.all(36.0),
            child: Column(
              children: <Widget>[
                usernameField,
                SizedBox(
                  height: 32,
                ),
                getOTPButton,
              ],
            )
        )
    );
  }

  _generateOTP(String username) {
    getOTPService.requestOTPGeneration(username).then((String response) {
      if(response != null && response.isNotEmpty ) {
        ScreenNavigateUtils().navigateToChangePasswordScreen(context);
      }
    });
  }

}