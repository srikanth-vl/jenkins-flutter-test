import '../../ua_app_context.dart';

import '../widgets/get_otp_widget.dart';
import 'package:flutter/material.dart';

class GetOTPScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    UAAppContext.getInstance().context = context;
    return SafeArea(
      child: Scaffold(
        body: Center(
          child: SingleChildScrollView(
            child: GetOTPWidget(),
          ),
        ),
      ),
    );
  }
}
