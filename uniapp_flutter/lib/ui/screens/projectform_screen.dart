import 'package:flutter/material.dart';

import '../helpers/appbar_helper.dart';
import '../widgets/projectform_widget.dart';
import '../widgets/global/uniapp_appbar.dart';
import '../../ua_app_context.dart';

class ProjectFormScreen extends StatelessWidget {
  final String appId;
  final String projectId;
  final String currentFormId;
  final String formActiontype;

  ProjectFormScreen({
    @required this.appId,
    @required this.projectId,
    this.currentFormId,
    this.formActiontype,
  });

  BuildContext buildContext;

  @override
  Widget build(BuildContext context) {
    UAAppContext.getInstance().context = context;
    this.buildContext = context;
    return SafeArea(
      child: Scaffold(
        resizeToAvoidBottomPadding: true,
        appBar: UniAppBar(
          appbarParams:
              AppbarParameterMapping().getAppbarMapping("ProjectFormScreen"),
          showMultilineAppbar:
              AppbarParameterMapping().getActionCount("ProjectFormScreen"),
          //
          showProjectListView: false,
          active: false,
          appId: null,
        ),
        body: ProjectFormWidget(
          appId: appId,
          projectId: projectId,
          currentFormId: currentFormId,
          formActionType: formActiontype,
        ),
      ),
    );
  }
}
