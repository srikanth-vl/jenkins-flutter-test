import 'dart:async';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';

import '../../../blocs/map_config_bloc.dart';
import '../../../models/map_config.dart';
import '../../../models/offline_map_file.dart';
import '../../../ua_app_context.dart';
import '../../../utils/common_constants.dart';
import 'empty_container.dart';
import 'image_avatar.dart';

class DownloadWidget extends StatefulWidget {
  _DownloadListViewWidgetState createState() => _DownloadListViewWidgetState();
}

class _DownloadListViewWidgetState extends State<DownloadWidget> {
//  bool _isDownloading = false, _isDownloaded = false;
  var downloadProgressString = "Download";
  MapConfigBloc mapConfigBloc = MapConfigBloc();

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    mapConfigBloc.fetchMapConfig();
  }

  @override
  void dispose() {
    super.dispose();
    mapConfigBloc.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder(
        stream: mapConfigBloc.mapConfigStream,
        builder: (BuildContext context, AsyncSnapshot<MapConfig> snapshot) {
          if (snapshot.hasError) {
            return Text('Error: ${snapshot.error}');
          }
          switch (snapshot.connectionState) {
            case ConnectionState.waiting:
              return Center(child: const Text('Loading...'));
            default:
              if (snapshot.data == null ||
                  snapshot.data.offlineMapFiles == null ||
                  snapshot.data.offlineMapFiles.isEmpty) {
                return Text('Error: ${snapshot.error}');
              }
              return _renderMapListFromStream(snapshot.data.offlineMapFiles);
          }
        });
  }

  _renderMapListFromStream(List<OfflineMapFile> offlineMapFiles) {
    return Center(
      child: ListView.builder(
        itemCount: offlineMapFiles.length,
        itemBuilder: (context, index) {
          final item = offlineMapFiles[index].fileName;
          return Card(
            elevation: 4.0,
            child: ListTile(
              leading: Container(
                  height: 64.0,
                  width: 64.0,
                  decoration: BoxDecoration(
                    color: Colors.black12,
                    shape: BoxShape.circle,
                    image: DecorationImage(
                      image: new AssetImage(""),
                    ),
                  ),
                  child: ImageAvatar(
                    imagePath: CommonConstants.APP_LOGO,
                    radius: 42.0,
                  )),
//              trailing: Icon(Icons.cloud_download),
              trailing: _fileDownloadStatusIcon(offlineMapFiles[index]),
              title: Text(item),
              subtitle: Text('Status : $downloadProgressString'),
              onTap: () {
                //todo: make tap on icon click
//                downloadFile();
              },
            ),
          );
        },
      ),
    );
  }

  Future<void> downloadFile(OfflineMapFile offlineMapFile) async {
//      String fileUrl, String fileName, String filePath) async {
    try {
      Dio dio = Dio();
      Directory dir = await getApplicationDocumentsDirectory();
      //todo: need to test on ios
      Directory fileDir =
          await new Directory(dir.path + offlineMapFile.fileStoragePath)
              .create();

      await dio.download(
          offlineMapFile.fileUrl, "${fileDir.path}/${offlineMapFile.fileName}",
          onReceiveProgress: (received, total) {
        if (total != -1) {
          print("Rec: $received , Total: $total");
          debugPrint("Directory path : " +
              offlineMapFile.fileStoragePath +
              "/" +
              offlineMapFile.fileName);
          setState(() {
            offlineMapFile.isDownloading = true;
            offlineMapFile.isDownloaded = false;
            downloadProgressString =
                ((received / total) * 100).toStringAsFixed(0) + "%";
          });
        }
      });
    } catch (e) {
      print(e);
    } finally {
      // TODO Check whether the files are downloaded or not! Validation
      // And set state accordingly.
      setState(() {
        //TODO change boolean as per the file Validation
        offlineMapFile.isDownloading = false;
        offlineMapFile.isDownloaded = true;
        downloadProgressString = "Completed";
      });
      print("Download completed");
    }
  }

  Widget _DownloadScreenIcon(OfflineMapFile offlineMapFile) {
    Directory dir = UAAppContext.getInstance().appDir;
    if (dir != null) {
      print("directory path ::${dir.path}");
    }
    File file = new File(dir.path +
        offlineMapFile.fileStoragePath +
        "/" +
        offlineMapFile.fileName);

//    if (_isDownloaded == true) {
    return FutureBuilder<int>(
      future: file.length(),
      builder: (BuildContext context, AsyncSnapshot<int> fileSize) {
        if (fileSize.hasData) {
          print("FILE SIZE IS : " + fileSize.data.toString());
          if (fileSize.data == offlineMapFile.fileSize) {
            offlineMapFile.isDownloading = false;
            offlineMapFile.isDownloaded = true;
            return IconButton(
              icon: Icon(Icons.delete_forever),
              iconSize: 32.0,
              color: Colors.blue,
              tooltip: "Completed",
              onPressed: () {
                //TODO: delete file from storage and and change the icon status
                //delete the fle
                file.deleteSync(recursive: true);
                print("Pressed1!");
              },
            );
          } else {
            return IconButton(
                icon: Icon(Icons.cloud_download),
                iconSize: 32.0,
                color: Colors.blue,
                tooltip: "Download",
                onPressed: () {
                  file.deleteSync();
                  downloadFile(offlineMapFile);
                  print("Pressed3!");
                });
          }
        } else if (fileSize.hasError) {
          print('fileSize.hasData ::${fileSize.error}');
          return EmptyContainer();
//            return Center(
//              child: CircularProgressIndicator(),
//            );
        }
      },
    );
    /*} else if (_isDownloading == true && _isDownloaded == false) {
      return IconButton(
        icon: Icon(Icons.cloud_circle),
        iconSize: 32.0,
        color: Colors.blue,
        tooltip: "Downloading",
        onPressed: () {
          print("Pressed2!");
        },
      );
    } else if (_isDownloading == false && _isDownloaded == false) {
      return IconButton(
        icon: Icon(Icons.cloud_download),
        iconSize: 32.0,
        color: Colors.blue,
        tooltip: "Download",
        onPressed: () {
          file.exists().then((bool filePresent) {
            if (!filePresent) {
              downloadFile(offlineMapFile);
              print("Pressed3!");
            }
          });
        },
      );
    } else {
      return null;
    }*/
  }

  Widget _fileDownloadStatusIcon(OfflineMapFile offlineMapFile) {
//    Directory dir = UAAppContext.getInstance().appDir;
//    if (dir != null) {
//      print("directory path ::${dir.path}");
//    }
//    File file = new File(dir.path +
//        offlineMapFile.fileStoragePath +
//        "/" +
//        offlineMapFile.fileName);

//    if (_isDownloaded == true) {
    return FutureBuilder<int>(
      future: getfileSize(offlineMapFile),
      builder: (BuildContext context, AsyncSnapshot<int> fileSize) {
        if (fileSize.hasData) {
          print("FILE SIZE IS : " + fileSize.data.toString());
          if (fileSize.data > (double.parse(offlineMapFile.fileSize) * 1000)) {
            return IconButton(
              icon: Icon(Icons.delete_forever),
              iconSize: 32.0,
              color: Colors.blue,
              tooltip: "Completed",
              onPressed: () {
                //TODO: delete file from storage and and change the icon status
                //delete the fle
//                file.deleteSync(recursive: true);
                print("Pressed1!");
              },
            );
          } else {
            return IconButton(
                icon: Icon(Icons.cloud_download),
                iconSize: 32.0,
                color: Colors.blue,
                tooltip: "Download",
                onPressed: () {
//                  file.deleteSync();
                  downloadFile(offlineMapFile);
                  print("Pressed3!");
                });
          }
        } else {
          print('fileSize.hasData ${fileSize.hasData}');
          return EmptyContainer();
//            return Center(
//              child: CircularProgressIndicator(),
//            );
        }
      },
    );
    /*} else if (_isDownloading == true && _isDownloaded == false) {
      return IconButton(
        icon: Icon(Icons.cloud_circle),
        iconSize: 32.0,
        color: Colors.blue,
        tooltip: "Downloading",
        onPressed: () {
          print("Pressed2!");
        },
      );
    } else if (_isDownloading == false && _isDownloaded == false) {
      return IconButton(
        icon: Icon(Icons.cloud_download),
        iconSize: 32.0,
        color: Colors.blue,
        tooltip: "Download",
        onPressed: () {
          file.exists().then((bool filePresent) {
            if (!filePresent) {
              downloadFile(offlineMapFile);
              print("Pressed3!");
            }
          });
        },
      );
    } else {
      return null;
    }*/
  }

  Future<int> getfileSize(OfflineMapFile offlineMapFile) async {
    print("offlinemapfileobject :: ${offlineMapFile.toJson()}");
    if (offlineMapFile.isDownloaded) {
      print("FILE IS DOWNLOADED ");
      return int.parse(offlineMapFile.fileSize);
    }
    Directory dir = UAAppContext.getInstance().appDir;
    if (dir != null) {
      print("directory path ::${dir.path}");
    }
    File file = new File(dir.path +
        offlineMapFile.fileStoragePath +
        "/" +
        offlineMapFile.fileName);
    bool isPresent = await file.exists();
    if (!isPresent) {
      print("FILE IS NOT PRESENT ");
      return 0;
    } else {
      int size = await file.length();

      print('FIleSize ::${size}');
      return size;
    }
  }
}
