import 'package:flutter/material.dart';
import 'dart:io';

import '../helpers/appbar_helper.dart';
import '../widgets/global/uniapp_appbar.dart';
import '../widgets/image_preview_widget.dart';
import '../../localization/app_translations.dart';

class ImagePreviewScreen extends StatelessWidget {
  final String imageName;
  final File image;
  final String appId;

  ImagePreviewScreen({
    this.imageName,
    this.appId,
    this.image,
  });

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        backgroundColor: Colors.black,
        appBar: UniAppBar(
          appbarParams: AppbarParameterMapping().getAppbarMapping("ImagePreviewScreen"),
          showMultilineAppbar:
              AppbarParameterMapping().getActionCount("ImagePreviewScreen"),
          //
          showProjectListView: false,
          appId: null,
          active: false,
        ),
        body: ImagePreviewWidget(
          imageName: imageName,
          image: image,
          appId: appId,
        ),
      ),
    );
  }
}
