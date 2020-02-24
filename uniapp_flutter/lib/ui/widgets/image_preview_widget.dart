import 'package:flutter/material.dart';
import 'dart:io';

import '../../utils/common_utils.dart';
import 'global/empty_container.dart';

class ImagePreviewWidget extends StatefulWidget {
  final String imageName;
  final String appId;
  final File image;

  ImagePreviewWidget({
    this.imageName,
    this.appId,
    this.image,
  });

  @override
  _ImagePreviewWidgetState createState() => _ImagePreviewWidgetState();
}

class _ImagePreviewWidgetState extends State<ImagePreviewWidget> {
  @override
  Widget build(BuildContext context) {
    return Container(
      child: Center(
        child: _previewPlaceholder(),
      ),
    );
  }

  Widget _previewPlaceholder() {
    if (widget.image != null) {
      return Container(
        width: MediaQuery.of(context).size.width,
        child: Image.file(
          widget.image,
          fit: BoxFit.cover,
        ),
      );
    } else {
      return FutureBuilder<File>(
          future: CommonUtils.getFileFromDB(widget.appId, widget.imageName),
          builder: (BuildContext context, AsyncSnapshot<File> snapshot) {
            File imgFile;
            if (snapshot.hasData) {
              imgFile = snapshot.data;
              return Image.file(
                imgFile,
                fit: BoxFit.cover,
              );
            }
            return EmptyContainer();
          });
    }
  }
}
