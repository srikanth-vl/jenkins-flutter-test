import '../../localization/app_translations.dart';
import 'package:flutter/material.dart';
import '../widgets/login_widget.dart';
import '../../ua_app_context.dart';

class LoginScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    UAAppContext.getInstance().context = context;
    return SafeArea(
      child: Scaffold(
        // resizeToAvoidBottomPadding: false,
        body: Center(
          child: SingleChildScrollView(
            child: LoginWidget(
              title: AppTranslations.of(context).text("login"),
            ),
          ),
        ),
      ),
    );
  }
}
