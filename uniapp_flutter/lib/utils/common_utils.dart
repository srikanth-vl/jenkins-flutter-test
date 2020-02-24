import '../models/map_config.dart';
import '../models/offline_map_file.dart';

import '../utils/screen_navigate_utils.dart';

import '../models/root_config.dart';
import '../models/sub_app.dart';
import 'package:intl/intl.dart';

import '../models/project_specific_form.dart';
import '../models/project_type_configuartion.dart';
import '../resources/project_type_configuration_service.dart';

import '../db/models/form_media_table.dart';
import '../utils/media_upload_status.dart';
import '../utils/project_submission_upload_status.dart';
import 'package:flutter/material.dart';
import 'package:toast/toast.dart';
import '../ua_app_context.dart';
import 'common_constants.dart';
import 'dart:io';

import 'package:archive/archive.dart';
import 'package:archive/archive_io.dart';

class CommonUtils {
  static handleTokenExpiry(BuildContext context) {
//    SharedPreferenceUtil().setPreferenceValue(CommonConstants.USER_IS_LOGGED_IN_PREFERENCE_KEY,
//        false, CommonConstants.PREFERENCE_TYPE_BOOL);
//    SharedPreferenceUtil().setPreferenceValue(CommonConstants.USER_ID_PREFERENCE_KEY,
//        CommonConstants.USER_ID_PREFERENCE_DEFAULT, CommonConstants.PREFERENCE_TYPE_STRING);
    Navigator.pushNamed(context, '/login');
  }

  static showToast(String message, BuildContext context) {
    Toast.show(message, context,
        duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
  }

  static Future<File> getIconFromStorage(String iconUrl) async {
    if (iconUrl != null && iconUrl.isNotEmpty) {
      String iconName =
          iconUrl.substring(iconUrl.lastIndexOf("/") + 1).replaceAll(" ", "");
      File iconFile = new File(UAAppContext.getInstance().appDir.path +
          CommonConstants.ICON_PATH +
          "/" +
          iconName);
      bool isPresent = await iconFile.exists();
      if (isPresent) {
        return iconFile;
      } else {
        return null;
      }
    }
    return null;
  }

  static Future<int> getUnsyncedMediaCount(String mAppId) async {
    List<int> statusList = new List();
    statusList.add(MediaUploadStatusHelper.getValue(MediaUploadStatus.NEW));
    statusList.add(MediaUploadStatusHelper.getValue(MediaUploadStatus.PENDING));
    int unsyncedMediaCount = await UAAppContext.getInstance()
        .unifiedAppDBHelper
        .getFormMediaCountForApp(
            mAppId, UAAppContext.getInstance().userID, statusList);
    return unsyncedMediaCount;
  }

  static Future<int> getUnsyncedProjectCount(String mAppId) async {
    List<int> projectStatusList = new List();
    projectStatusList.add(ProjectSubmissionUploadStatusHelper.getValue(
        ProjectSubmissionUploadStatus.SERVER_ERROR));
    projectStatusList.add(ProjectSubmissionUploadStatusHelper.getValue(
        ProjectSubmissionUploadStatus.UNSYNCED));
    int unsyncedProjectCount = await UAAppContext.getInstance()
        .unifiedAppDBHelper
        .getProjectSubmissionCountForApp(
            mAppId, UAAppContext.getInstance().userID, projectStatusList);

    return unsyncedProjectCount;
  }

  static Future<File> getFileFromDB(String appId, String uuid) async {
    File file;
    if (uuid != null && uuid.isNotEmpty && appId != null && appId.isNotEmpty) {
      FormMediaTable formMedia = await UAAppContext.getInstance()
          .unifiedAppDBHelper
          .getFormMedia(uuid, appId, UAAppContext.getInstance().userID);
      file = new File(formMedia.mediaLocalPath);
    }
    return file;
  }

  static Future<ProjectTypeConfiguration> getProjectTypeConfig(
      String appId) async {
    ProjectTypeConfiguration projectTypeConfig =
        await ProjectTypeService().fetchProjectTypeConfigurationFromDb(
      UAAppContext.getInstance().userID,
      appId,
    );
    return projectTypeConfig;
  }

  static Future<bool> checkAccessForAssetCreation(String appId) async {
    ProjectTypeConfiguration projectTypeConfig =
        await getProjectTypeConfig(appId);
    if (projectTypeConfig != null) {
      Map<String, ProjectSpecificForm> forms = null;
      forms = projectTypeConfig.content[CommonConstants.DEFAULT_PROJECT_ID];

      if (forms != null) {
        if (forms[CommonConstants.INSERT_FORM_KEY] != null) {
          return true;
        }
      }
    }
    return false;
  }

  static Future<String> unzipFile(String filePath, String dirPath) async {
    // Read the Zip file from disk.
    List<int> bytes = new File(filePath).readAsBytesSync();

    // Decode the Zip file
    Archive archive = new ZipDecoder().decodeBytes(bytes);

    // Extract path
    var path = dirPath;
    // Extract the contents of the Zip archive to disk.
    for (ArchiveFile file in archive) {
      String filename = file.name;
      if (file.isFile) {
        List<int> data = file.content;
        new File(path + filename)
          ..createSync(recursive: true)
          ..writeAsBytesSync(data);
      } else {
        new Directory(path + filename)..create(recursive: true);
      }
    }
    return path;
  }

  static getDate(int ts) {
    DateFormat dateFormat = new DateFormat('dd/MM/yyyy').add_Hm();
    return dateFormat.format(DateTime.fromMillisecondsSinceEpoch(ts));
  }

  static SubApp getSubAppFromConfig(String appId) {
    RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
    if (rootConfig != null) {
      for (SubApp app in rootConfig.config) {
        if (appId.compareTo(app.appId) == 0) {
          return app;
        }
      }
    }
    return null;
  }

  static bool checkForSingleSubApp(){
    RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
    if(rootConfig != null && rootConfig.config != null && rootConfig.config.isNotEmpty){
      if(rootConfig.config.length == 1){
        return true;
      }
    }
    return false;
  }

  static showMapDownloadDialog(BuildContext context) async {
    MapConfig mapConfig = UAAppContext.getInstance().mapConfig;
    if(mapConfig != null && mapConfig.offlineMapFiles != null && mapConfig.offlineMapFiles.isNotEmpty) {
      bool showPopup = await _checkIfAnyFileIsNotDownloaded(mapConfig.offlineMapFiles);

      if(!showPopup) {
        // set up the buttons
        Widget cancelButton = FlatButton(
          child: Text("Cancel"),
          onPressed: () {
            Navigator.of(context, rootNavigator: true).pop();
          },
        );
        Widget continueButton = FlatButton(
          child: Text("Ok"),
          onPressed: () async {
            Navigator.of(context, rootNavigator: true).pop();
            ScreenNavigateUtils().navigateToDownloadMapScreen(context);
          },
        );
        // set up the AlertDialog
        AlertDialog alert = AlertDialog(
          content: Text("Map files available. Download?"),
          actions: [
            cancelButton,
            continueButton,
          ],
        );

        // show the dialog
        await Future.delayed(Duration(milliseconds: 50));
        showDialog(
          context: context,
          builder: (BuildContext context) {
            return alert;
          },
        );
      }
    }
  }

  static Future<bool> _checkIfAnyFileIsNotDownloaded(List<OfflineMapFile> offlineMaps) async {
    for(OfflineMapFile mapFile in offlineMaps){
      File file = new File(UAAppContext.getInstance().appDir.path +
          mapFile.fileStoragePath +
          "/" +
          mapFile.fileName);
      bool isPresent = await file.exists();
      if(!isPresent){
        return false;
      }
    }
    return true;
  }
}
