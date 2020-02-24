import '../../ui/widgets/offline_file_download.dart';
import 'package:flutter/material.dart';

import '../helpers/appbar_helper.dart';
import '../widgets/global/uniapp_appbar.dart';

class DownloadScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        appBar: UniAppBar(
          appbarParams:
              AppbarParameterMapping().getAppbarMapping("DownloadScreen"),
          showMultilineAppbar:
              AppbarParameterMapping().getActionCount("DownloadScreen"),
          showProjectListView: false,
          active: false,
          appId: null,
        ),
        body: DownloadListViewWidget(),
      ),
    );
  }
}
