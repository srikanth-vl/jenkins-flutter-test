import 'package:flutter/material.dart';

import '../helpers/appbar_helper.dart';
import '../widgets/global/uniapp_appbar.dart';
import '../../ua_app_context.dart';
import '../widgets/navigation_widget.dart';

class NavigationScreen extends StatelessWidget {

  final double projLat;
  final double projLon;

  NavigationScreen({
    @required this.projLat,
    @required this.projLon,});

  @override
  Widget build(BuildContext context) {
    UAAppContext.getInstance().context = context;
    return SafeArea(
      child: Scaffold(
        appBar: UniAppBar(
          appbarParams:
          AppbarParameterMapping().getAppbarMapping("NavigationScreen"),
          showMultilineAppbar:
          AppbarParameterMapping().getActionCount("NavigationScreen"),
          //
          showProjectListView: false,
          active: false,
          appId: null,
        ),
        body: NavigationWidget(ctxt: context, projLat: projLat, projLon: projLon,),
      ),
    );
  }
}
