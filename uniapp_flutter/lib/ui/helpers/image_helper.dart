import 'package:flutter/services.dart';

import '../../utils/common_constants.dart';

class UniappImageHelper {
  static final String defaultImagePath = CommonConstants.DEFAULT_APP_LOGO;

  /// This method return is the AssetImage exists in the root bundle and
  /// if doesn't exist in the root Bundle it returns a default/alternate image path as replacement.
  Future<String> assetImageExists(String imagePath, String alternateImagePath) {
    if (alternateImagePath == null || alternateImagePath.isEmpty) {
      alternateImagePath = defaultImagePath;
    }

    return rootBundle
        .load(imagePath)
        .then((value) => imagePath)
        .catchError((onError) => alternateImagePath);
  }
}
