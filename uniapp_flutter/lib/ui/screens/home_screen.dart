import 'package:flutter/material.dart';

import '../helpers/home_screen_helper.dart';
import '../helpers/appbar_helper.dart';
import '../widgets/global/uniapp_appbar.dart';
import '../widgets/home_widget.dart';
import '../../ua_app_context.dart';

class HomeScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    UAAppContext.getInstance().context = context;
    HomeScreenHelper().callBackgroundThreads();
    return SafeArea(
      child: Scaffold(
        appBar: UniAppBar(
          appbarParams:
              AppbarParameterMapping().getAppbarMapping("HomeScreen"),
          showMultilineAppbar:
              AppbarParameterMapping().getActionCount("HomeScreen"),
          //
          showProjectListView: false,
          active: false,
          appId: null,
        ),
        body: HomeWidget(),
      ),
    );
  }
}
