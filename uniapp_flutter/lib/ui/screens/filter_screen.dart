import 'package:flutter/material.dart';

import '../helpers/appbar_helper.dart';
import '../widgets/filter_widget.dart';
import '../widgets/global/uniapp_appbar.dart';
import '../../db/models/project_master_data_table.dart';

class FilterScreen extends StatelessWidget {
  final String appId;
  final List<ProjectMasterDataTable> projectList;
  final Map filterKeyToValue;

  FilterScreen({
    @required this.appId,
    @required this.projectList,
    @required this.filterKeyToValue,
  });

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: UniAppBar(
          appbarParams:
              AppbarParameterMapping().getAppbarMapping("FilterScreen"),
          showMultilineAppbar:
              AppbarParameterMapping().getActionCount("FilterScreen"),
          showProjectListView: false,
          active: false,
          appId: null,
        ),
        body: FilterWidget(
          appId: appId,
          projectList: projectList,
          filterKeyToValue: filterKeyToValue,
        ),
      ),
    );
  }
}
