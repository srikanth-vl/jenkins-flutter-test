import 'package:flutter/material.dart';

import '../helpers/appbar_helper.dart';
import '../widgets/video_preview_widget.dart';
import '../widgets/global/uniapp_appbar.dart';

class VideoPreviewScreen extends StatelessWidget {
  final String videoPath;

  VideoPreviewScreen({
    @required this.videoPath,
  });

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        backgroundColor: Colors.black,
        appBar: UniAppBar(
          appbarParams:
              AppbarParameterMapping().getAppbarMapping("VideoPreviewScreen"),
          showMultilineAppbar:
              AppbarParameterMapping().getActionCount("VideoPreviewScreen"),

          //
          showProjectListView: false,
          active: false,
          appId: null,
        ),
        body: VideoPreviewWidget(
          videoPath: videoPath,
        ),
      ),
    );
  }
}
