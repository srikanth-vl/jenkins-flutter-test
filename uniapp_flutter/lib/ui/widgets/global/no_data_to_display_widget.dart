import 'package:flutter/material.dart';

import './image_banner.dart';
import '../../../utils/common_constants.dart';

class NoDataToDisplayWidget extends StatelessWidget {
  final TextStyle textStyle;
  const NoDataToDisplayWidget({
    this.textStyle,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        ImageBanner(
          imagePath: CommonConstants.NO_DATA_TO_DISPLAY_IMAGE,
          imageHeight: 360.0,
        ),
      ],
    );
  }
}
