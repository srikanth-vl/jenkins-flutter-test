import '../helpers/appbar_helper.dart';

import '../../ui/themes/text_theme.dart';

import '../../localization/app_translations.dart';

import '../../blocs/project_grouping_attribute_bloc.dart';
import '../widgets/project_group_list_view_widget.dart';
import 'package:flutter/material.dart';
import '../widgets/global/uniapp_appbar.dart';
import '../../ua_app_context.dart';

class ProjectGroupScreen extends StatefulWidget {
  final String appId;
  final List<String> attributes;
  final String sortType;
  ProjectGroupScreen(
      {@required this.appId,
      @required this.attributes,
      @required this.sortType});
  @override
  ProjectGroupScreenState createState() => ProjectGroupScreenState(appId);
}

class ProjectGroupScreenState extends State<ProjectGroupScreen> {
  String appId;
  ProjectGroupBloc projectGroupBloc = ProjectGroupBloc();

  ProjectGroupScreenState(this.appId) {
    projectGroupBloc.fetchProjects(this.appId);
  }

  @override
  void dispose() {
    super.dispose();
    projectGroupBloc.dispose();
  }

  @override
  void initState() {
    super.initState();
    UAAppContext.getInstance().context = context;
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: UniAppBar(
          appbarParams: AppbarParameterMapping()
              .getAppbarMapping("ProjectGroupingScreen"),
          showMultilineAppbar:
              AppbarParameterMapping().getActionCount("ProjectGroupingScreen"),
          //
          showProjectListView: false,
          active: false,
          appId: null,
        ),
        body: StreamBuilder(
            stream: projectGroupBloc.projectGroups,
            builder:
                (context, AsyncSnapshot<Map<String, List<String>>> snapshot) {
              if (snapshot.hasError) {
                return Center(
                  child: Text(
                    'Error: ${snapshot.error}',
                    style: UniappTextTheme.defaultErrorTextStyle,
                  ),
                );
              }

              switch (snapshot.connectionState) {
                case ConnectionState.waiting:
                  return Center(
                    child: Text(
                      'Loading...',
                      style: UniappTextTheme.defaultErrorTextStyle,
                    ),
                  );
                default:
                  if (snapshot.data == null || snapshot.data.isEmpty) {
                    return Center(
                      child: Text(
                        "No Data found",
                        style: UniappTextTheme.defaultErrorTextStyle,
                      ),
                    );
                  }
                  return ProjectGroupListViewWidget(
                    appId: appId,
                    attributeKeyToValuesMap: snapshot.data,
                    attributes: widget.attributes,
                    sortType: widget.sortType,
                  );
              }
            }),
      ),
    );
  }
}
