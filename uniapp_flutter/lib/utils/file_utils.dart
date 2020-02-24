import 'package:path_provider/path_provider.dart';

class FileUtils {

  Future<String> getApplicationStoragePathForImages() async {
    final directory = await getApplicationDocumentsDirectory();
    return directory.path + "images";
  }

  Future<String> getApplicationStoragePathForDownloads() async {
    final directory = await getApplicationDocumentsDirectory();
    return directory.path + "downloads";
  }
}

final fileUtils = FileUtils();
