import 'package:flutter/widgets.dart';

import '../../../ui/themes/color_theme.dart';
import '../../../ui/widgets/global/image_avatar.dart';

class ImageHaloContainer extends StatelessWidget {
  final String imagePath;
  final double radius;

  const ImageHaloContainer({@required this.imagePath, @required this.radius});

  double determineImageRadius(double radius) {
    int divisor = 2;
    double imageRadius = 0;
    while (divisor < radius) {
      imageRadius += radius / divisor;
      divisor = divisor * 4;
    }
    return imageRadius;
  }

  Widget build(BuildContext context) {
    return Container(
        height: radius,
        width: radius,
        decoration: BoxDecoration(
          color: UniappColorTheme.listHaloColor,
          shape: BoxShape.circle,
          image: DecorationImage(
            image: AssetImage('assets/images/transparent.png'),
          ),
        ),
        child: ImageAvatar(
          imagePath: imagePath,
          radius: determineImageRadius(radius),
        ));
  }
}
