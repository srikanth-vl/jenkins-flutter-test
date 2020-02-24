import 'dart:convert';
import 'dart:io';

import '../ua_app_context.dart';
import 'package:path_provider/path_provider.dart';

class LocalizationUtils {


//  Future<File> get _localFile async {
//    final path = UAAppContext.getInstance().appDir.path;
//    return File('$path/counter.txt');
//  }
//
//  Future<String> readCounter() async {
//    try {
//      final file = await _localFile;
//
//      // Read the file
//      String contents = await file.readAsString();
//
//      return contents;
//    } catch (e) {
//      // If encountering an error, return 0
//      return null;
//    }
//  }
//
//  Future<File> writeCounter(int counter) async {
//    final file = await _localFile;
//
//    // Write the file
//    return file.writeAsString('$counter');
//  }
  void createFile(String content, Directory dir, String fileName) {
    print("Creating file!");
    File file = new File(dir.path + "/" + fileName);
    file.createSync();
//    file.writeAsStringSync(jsonEncode(content));
//    file.writeAsStringSync(jsonEncode(content));
  }

  void writeToFile( String content)  {
    File jsonFile = new File(UAAppContext.getInstance().appDir.path + "/" + "localization.json");
    bool fileExists = jsonFile.existsSync();
    if (!fileExists)  {
      createFile(content, UAAppContext.getInstance().appDir,"localization.json");
    }
    jsonFile.writeAsStringSync(content);
  }
  Map<String,dynamic> getData() {
    File jsonFile = new File(UAAppContext.getInstance().appDir.path + "/" + "localization.json");
    bool fileExists = jsonFile.existsSync();
    if (fileExists)  {
      return jsonDecode(jsonFile.readAsStringSync());
    }
    return null;

  }
}