import 'package:flutter/material.dart';

import '../../helpers/image_helper.dart';
import '../../themes/uniapp_css.dart';

class ImageBanner extends StatelessWidget {
  final String imagePath;
  final double imageHeight;
  final double imageWidth;
  ImageBanner({
    @required this.imagePath,
    @required this.imageHeight,
    this.imageWidth,
  });

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: UniappImageHelper().assetImageExists(imagePath, null),
      builder: (BuildContext context, AsyncSnapshot snapshot) {
        if (!snapshot.hasData) {
          return CircularProgressIndicator();
        }
        return Container(
          constraints:
              BoxConstraints.expand(width: imageWidth, height: imageHeight),
          child: Column(
            children: <Widget>[
              Padding(
                padding: UniappCSS.smallHorizontalAndVerticalPadding,
                child: Image.asset(
                  snapshot.data,
                  fit: BoxFit.fill,
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}
