import 'package:flutter/material.dart';

import '../../helpers/image_helper.dart';

class ImageAvatar extends StatelessWidget {
  final String imagePath;
  final double radius;

  const ImageAvatar({
    @required this.imagePath,
    @required this.radius,
  });

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: UniappImageHelper().assetImageExists(imagePath, null),
      builder: (BuildContext context, AsyncSnapshot snapshot) {
        if (snapshot.hasData == false) {
          return CircularProgressIndicator();
        }
        return Center(
          child: Container(
            width: radius,
            height: radius,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              image: DecorationImage(
                fit: BoxFit.scaleDown,
                image: AssetImage(snapshot.data),
              ),
            ),
          ),
        );
      },
    );
  }
}
