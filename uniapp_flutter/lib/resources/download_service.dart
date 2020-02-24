
import 'dart:io';

import 'package:dio/dio.dart';

class DownloadService {

  Future<bool> downloadFile(String fileUrl, String fileName, String filePath) async {
    Response response;
    try {
      Dio dio = Dio();
      Directory fileDir = await new Directory(filePath).create();
      File file = new File(filePath + "/" + fileName);
      bool filePresent = await file.exists();
      if(!filePresent) {
        if (fileUrl != null && fileUrl.isNotEmpty && fileDir.path != null &&
            fileDir.path.isNotEmpty && fileName != null && fileName.isNotEmpty) {

          response = await dio.download(fileUrl, "${fileDir.path}/$fileName",
              onReceiveProgress: (rec, total) {
//            print("ON PROGRESS : Rec: $rec , Total: $total");
              });
          if (response.statusCode == 200) {
            print("Downloaded file successfully !!!  : :  " + file.path);

          } else if (response.statusCode == 350) {
            print("Error on downloading file!!!");
          }
        }
      }
    } catch (e) {
      print(e);
    }
  }
}