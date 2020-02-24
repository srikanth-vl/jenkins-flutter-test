import '../../utils/screen_navigate_utils.dart';
import '../../blocs/change_password_bloc.dart';
import '../../utils/common_constants.dart';
import 'package:flutter/material.dart';
import 'package:toast/toast.dart';

class ChangePasswordWidget extends StatefulWidget{
  bool _obscurePasswordText = true;
  bool _obscureConfirmPasswordText = true;
  @override
  _ChangePasswordWidgetState createState() => _ChangePasswordWidgetState();
}

class _ChangePasswordWidgetState extends State<ChangePasswordWidget> {

  final otpController = TextEditingController();
  final passwordController = TextEditingController();
  final confirmPasswordController = TextEditingController();
  ChangePasswordBloc changePasswordBloc = ChangePasswordBloc();

  @override
  void dispose() {
    otpController.dispose();
    passwordController.dispose();
    confirmPasswordController.dispose();
    changePasswordBloc.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {

    final otpField = TextField(
      controller: otpController,
      decoration: InputDecoration(
        labelText: 'Enter the OTP received',
        fillColor: Colors.white,
        filled: true,
        border: new OutlineInputBorder(
            borderRadius: new BorderRadius.circular(4.0),
            borderSide: new BorderSide()
        ),
      ),
      keyboardType: TextInputType.number,
      style: new TextStyle(
          fontFamily: 'Poppins'
      )
    );


    final changePasswordButton = Material(
      elevation: 4.0,
      borderRadius: BorderRadius.circular(4.0),
      color: Theme.of(context).accentColor,
      child: MaterialButton(
        minWidth: MediaQuery.of(context).size.width,
        padding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
        onPressed: () {
          String otp = otpController.text;
          String password = passwordController.text;
          String confirmPassword = confirmPasswordController.text;
          if(password != '' &&
              confirmPassword != '' &&
              password == confirmPassword &&
                otp != ''){
            _changePassword(otp, password);
          }
          else {
            Toast.show(CommonConstants.PASSWORDS_DONT_MATCH, context, gravity: Toast.TOP);
          }
        },
        child: Text(
          'CHANGE PASSWORD',
          textAlign: TextAlign.center,
          //style:(color: Colors.white, fontWeight: FontWeight.bold),
        ),
      )
    );

    return Container(
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: <Widget>[
            otpField,
            SizedBox(height: 16.0),
            _renderPasswordField(passwordController, 'Password'),
            SizedBox(height: 16.0,),
            _renderPasswordField(confirmPasswordController, 'Confirm Password'),
            SizedBox(height: 16.0,),
            changePasswordButton,
          ],
        )
      )
    );
  }
  
  bool returnSelectedPasswordFieldObscurity(String label){
    return label == 'Password'? widget._obscurePasswordText : widget._obscureConfirmPasswordText;
  }

  void setStateOfPasswordField(String label){
    label == 'Password' ? widget._obscurePasswordText = !widget._obscurePasswordText :
    widget._obscureConfirmPasswordText = !widget._obscureConfirmPasswordText;
  }

  Widget _renderPasswordField(TextEditingController controllerName, String label){
    return TextField(
      obscureText: returnSelectedPasswordFieldObscurity(label),
      controller: controllerName,
      decoration: new InputDecoration(
          prefixIcon: Icon(Icons.lock),
          labelText: label,
          suffixIcon: new GestureDetector(
            onTap: () {
              setState(() {
                setStateOfPasswordField(label);
              });
            },
            child: Icon(returnSelectedPasswordFieldObscurity(label)? Icons.visibility : Icons.visibility_off),
          ),
          fillColor: Colors.white,
          filled: true,
          border: new OutlineInputBorder(
            borderRadius: new BorderRadius.circular(4.0),
            borderSide: new BorderSide(),
          )
      ),
      keyboardType: TextInputType.visiblePassword,
      style: new TextStyle(
        fontFamily: 'Poppins',
      ),
    );
  }

  void _changePassword(String otp, String newPassword){
    changePasswordBloc.changeUserPassword(otp, newPassword);
    changePasswordBloc.changePasswordControllingStream.listen((bool isPasswordChangeAllowed) {
      if(isPasswordChangeAllowed) {
        _navigateToLoginScreen();
      }
    });
  }

  void _navigateToLoginScreen(){
    ScreenNavigateUtils().navigateToLoginScreen(context, true, true);
  }
}