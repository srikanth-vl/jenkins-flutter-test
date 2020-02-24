import '../../ua_app_context.dart';

import '../widgets/change_password_widget.dart';
import 'package:flutter/material.dart';

class ChangePasswordScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    UAAppContext.getInstance().context = context;
    return SafeArea(
      child: Scaffold(
        body: Center(
          child: SingleChildScrollView(
            child: ChangePasswordWidget(),
          ),
        ),
      ),
    );
  }
}
