import '../db/models/form_media_table.dart';

class ImageCaptureUtils {
  Map<String, FormMediaTable> imagesCaptured = new Map();

  addToMap(String uuid, FormMediaTable mediaEntry) {
    imagesCaptured[uuid] = mediaEntry;
  }

  FormMediaTable getFomMap(String uuid) {
    if (imagesCaptured.containsKey(uuid)) {
      return imagesCaptured[uuid];
    } else {
      return null;
    }
  }
}

final imageCaptureUtils = ImageCaptureUtils();
