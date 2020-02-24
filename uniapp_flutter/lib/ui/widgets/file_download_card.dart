import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:toast/toast.dart';
import 'dart:async';
import 'dart:io';

import './global/image_avatar.dart';
import '../../blocs/map_config_bloc.dart';
import '../../event/event_utils.dart';
import '../../models/offline_map_file.dart';
import '../../utils/common_utils.dart';
import '../../utils/common_constants.dart';
import '../../ua_app_context.dart';

class FileDownloadCard extends StatefulWidget {
  final OfflineMapFile offlineMapFile;

  FileDownloadCard({@required this.offlineMapFile});

  _FileDownloadCardState createState() => _FileDownloadCardState();
}

class _FileDownloadCardState extends State<FileDownloadCard> {
  var downloadProgressString = "Download";
  OfflineMapFile offlineMapFile;
  MapConfigBloc mapConfigBloc = MapConfigBloc();

  StreamSubscription downloadInprogressSubscription;
  StreamSubscription downloadCompletedSubscription;

  @override
  void initState() {
    print("Initiate");

    // TODO: implement initState
    super.initState();
    //initiate downloadInprogress subscription
    downloadInprogressSubscription =
        eventBus.on<DownloadInprogressEvent>().listen((event) {
      if (mounted && event.name == offlineMapFile.fileName) {
        setState(() {
          print("Event listen file downloading");
          downloadProgressString = event.progress;
          offlineMapFile.isDownloading = true;
          offlineMapFile.isDownloaded = false;
        });
      }
    });

    //initiate downloadCompleted subscription
    downloadCompletedSubscription =
        eventBus.on<PostDownloadEvent>().listen((event) {
      if (mounted && event.name == offlineMapFile.fileName) {
        setState(() {
          print("Event listen file downloaded");
          downloadProgressString = "Downloaded";
          offlineMapFile.isDownloading = false;
          offlineMapFile.isDownloaded = true;
        });
      }
    });
    offlineMapFile = widget.offlineMapFile;
    mapConfigBloc.fetchMapConfig();
  }

  @override
  void dispose() {
    super.dispose();
    mapConfigBloc.dispose();
    downloadInprogressSubscription.cancel();
    downloadCompletedSubscription.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 4.0,
      child: ListTile(
        leading: Container(
            height: 64.0,
            width: 64.0,
            child: ImageAvatar(
              imagePath: CommonConstants.APP_LOGO,
              radius: 42.0,
            )),
        trailing: _fileDownloadStatusIcon(offlineMapFile),
        title: Text(offlineMapFile.fileName),
        subtitle: Text(
            'Status : ${offlineMapFile.isDownloaded ? "Downloaded" : "${downloadProgressString}"}'),
      ),
    );
  }

  Future<void> downloadFile(OfflineMapFile offlineMapFile) async {
    if (offlineMapFile.isDownloading) {
      return;
    }
    Directory fileDir;
    try {
      offlineMapFile.isDownloading = true;
      Dio dio = Dio();
      Directory dir = await UAAppContext.getInstance().appDir;
      //todo: need to test on ios
      fileDir = await new Directory(dir.path + offlineMapFile.fileStoragePath)
          .create();
      await dio.download(
          offlineMapFile.fileUrl, "${fileDir.path}/${offlineMapFile.fileName}",
          onReceiveProgress: (received, total) {
        if (total != -1) {
          downloadProgressString =
              ((received / total) * 100).toStringAsFixed(0) + "%";
          eventBus.fire(DownloadInprogressEvent(
              offlineMapFile.fileName, downloadProgressString));

          if (!UAAppContext.getInstance()
              .downloadingFiles
              .contains(offlineMapFile.fileName)) {
            UAAppContext.getInstance()
                .downloadingFiles
                .add(offlineMapFile.fileName);
          }
          if (mounted) {
            setState(() {
              offlineMapFile.isDownloading = true;
              offlineMapFile.isDownloaded = false;
            });
          }
        }
      });
    } catch (e) {
      print(e);
      if (mounted) {
        setState(() {
          offlineMapFile.isDownloading = false;
          offlineMapFile.isDownloaded = false;
        });
      }
    } finally {
      UAAppContext.getInstance()
          .downloadingFiles
          .remove(offlineMapFile.fileName);
      // TODO Check whether the files are downloaded or not! Validation
      // And set state accordingly.
      if (mounted) {
        setState(() {
          //TODO change boolean as per the file Validation
          offlineMapFile.isDownloading = false;
          offlineMapFile.isDownloaded = true;
          downloadProgressString = "Completed";

          if (offlineMapFile.fileName.contains(".zip") && fileDir != null) {
            CommonUtils.unzipFile(fileDir.path + "/" + offlineMapFile.fileName,
                fileDir.path + "/Offline/");
          }
        });
      }
      print("Download completed");

      eventBus.fire(PostDownloadEvent(offlineMapFile.fileName));
    }
  }

  Widget _fileDownloadStatusIcon(OfflineMapFile offlineMapFile) {
    print("_isDownloaded ${offlineMapFile.isDownloaded}");
    if (offlineMapFile.isDownloading) {
      return IconButton(
        icon: Icon(Icons.cloud_circle),
        iconSize: 32.0,
        color: Colors.blue,
        tooltip: "Inprogress",
        onPressed: () {
          //delete the fle
          // deleteFile(offlineMapFile);
          Toast.show("file download is in progress", context,
              duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
        },
      );
    }
    if (offlineMapFile.isDownloaded) {
      return IconButton(
        icon: Icon(Icons.delete_forever),
        iconSize: 32.0,
        color: Colors.blue,
        tooltip: "Completed",
        onPressed: () {
          //delete the fle
          deleteFile(offlineMapFile);
        },
      );
    } else {
      return IconButton(
          icon: Icon(Icons.cloud_download),
          iconSize: 32.0,
          color: Colors.blue,
          tooltip: "Download",
          onPressed: () {
            downloadFile(offlineMapFile);
          });
    }
  }

  deleteFile(OfflineMapFile offlineMapFile) async {
    Directory dir = UAAppContext.getInstance().appDir;
    if (dir != null) {
      print("directory path ::${dir.path}");
    }
    File file = new File(dir.path +
        offlineMapFile.fileStoragePath +
        "/" +
        offlineMapFile.fileName);
    bool fileExists = await file.exists();
    if (fileExists) {
      file.deleteSync(recursive: true);
      if (mounted) {
        setState(() {
          offlineMapFile.isDownloaded = false;
          offlineMapFile.isDownloading = false;
        });
      }
    }
  }
}
