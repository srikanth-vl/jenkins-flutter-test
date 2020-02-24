import 'dart:async';
import 'dart:io';

import '../../blocs/map_config_bloc.dart';
import '../../models/map_config.dart';
import '../../models/offline_map_file.dart';
import 'package:flutter/material.dart';

import '../../ua_app_context.dart';
import 'file_download_card.dart';

class DownloadListViewWidget extends StatelessWidget{

  MapConfigBloc mapConfigBloc = MapConfigBloc();

  @override
  void dispose() {
    mapConfigBloc.dispose();
  }

  @override
  Widget build(BuildContext context) {
    mapConfigBloc.fetchMapConfig();
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
          final offlineMapFile = offlineMapFiles[index];
          Directory dir = UAAppContext.getInstance().appDir;
          if (dir != null) {
            print("directory path ::${dir.path}");
          }
          File file = new File(dir.path +
              offlineMapFile.fileStoragePath +
              "/" +
              offlineMapFile.fileName);
          return FutureBuilder<bool>(
              future: ifFilePresent(offlineMapFile),
              builder: (BuildContext context, AsyncSnapshot<bool> fileSize) {
                if (fileSize.hasData) {
                  if (fileSize.data) {
                    return FileDownloadCard(offlineMapFile: offlineMapFile);
                  } else if (fileSize.hasError) {
                    print('fileSize.hasData ::${fileSize.error}');
                    return FileDownloadCard(offlineMapFile: offlineMapFile);
                  }
                }
                return FileDownloadCard(offlineMapFile: offlineMapFile);
              }
          );
        },
      ),
    );
  }

  Future<bool> ifFilePresent(OfflineMapFile offlineMapFile) async {
        if(UAAppContext.getInstance()!= null && UAAppContext.getInstance().downloadingFiles != null && UAAppContext.getInstance().downloadingFiles.contains(offlineMapFile.fileName)) {

      offlineMapFile.isDownloading = true;
      offlineMapFile.isDownloaded = false;
      return true;
    }
    print("offlinemapfileobject :: ${offlineMapFile.toJson()}");
    if (offlineMapFile.isDownloaded) {
      print("FILE IS DOWNLOADED ");
      return true;
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
      return false;
    } else {

      int size = await file.length();
      double fileSize = double.parse(offlineMapFile.fileSize);
      double offlinefileSize = (fileSize * 1000);
      if(offlinefileSize <= size) {
        offlineMapFile.isDownloading = false;
        offlineMapFile.isDownloaded = true;
        print("FILE IS Downloaded");
        return true;
      }
    }
  }
}
