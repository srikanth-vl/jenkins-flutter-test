import 'dart:io';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:toast/toast.dart';

import '../../helpers/ask_for_permissions.dart';
import '../global/empty_container.dart';
import '../../helpers/form_values.dart';
import '../../screens/video_camera_screen.dart';
import '../../screens/video_preview_screen.dart';
import '../../themes/color_theme.dart';
import '../../themes/text_theme.dart';
import '../../themes/uniapp_css.dart';
import '../../../ua_app_context.dart';
import '../../../db/models/form_media_table.dart';
import '../../../models/app_meta_data_config.dart';
import '../../../utils/common_constants.dart';
import '../../../utils/image_capture_utils.dart';
import '../../../utils/media_action_type.dart';
import '../../../utils/media_subtype.dart';
import '../../../utils/media_type.dart';
import '../../../utils/media_upload_status.dart';
import '../../../utils/video_capture_utils.dart';

class FormVideoWidget extends StatefulWidget {
  final String id;
  final String label;
  final String appId;
  final String projectId;
  final String userId;
  final String externalProjectId;
  final Position currentPosition;
  final int maxValue;

  FormVideoWidget({
    @required this.id,
    @required this.label,
    @required this.appId,
    @required this.projectId,
    @required this.userId,
    @required this.externalProjectId,
    @required this.currentPosition,
    @required this.maxValue,
  });

  @override
  FormVideoWidgetState createState() =>
      FormVideoWidgetState(currentPos: currentPosition);
}

class FormVideoWidgetState extends State<FormVideoWidget> {
  Position currentPos;

  FormVideoWidgetState({
    @required this.currentPos,
  });

  Map results = new Map();

  _thumbnails() {
    if (results["videoPath"] != null) {
      return Container(
        width: double.maxFinite,
        child: SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            mainAxisSize: MainAxisSize.max,
            children: <Widget>[
              InkWell(
                onTap: () => _navigateToPreviewScreen(results["videoPath"]),
                child: Stack(
                  children: <Widget>[
                    Container(
                      padding: EdgeInsets.all(4.0),
                      width: 64.0,
                      height: 64.0,
                      child: FlatButton(
                        color: Colors.black,
                        onPressed: () {},
                        child: null,
                      ),
                    ),
                    Positioned(
                      top: 16.0,
                      left: 16.0,
                      child: Icon(
                        Icons.play_circle_filled,
                        color: Colors.white,
                        size: 32.0,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      );
    }
    return EmptyContainer();
  }

  _navigateToPreviewScreen(String path) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => VideoPreviewScreen(
          videoPath: path,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(8.0, 0.0, 8.0, 0.0),
      child: Container(
        decoration: BoxDecoration(
          borderRadius: UniappCSS.widgetBorderRadius,
          border: UniappCSS.widgetBorder,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Row(
              mainAxisSize: MainAxisSize.max,
              children: <Widget>[
                Expanded(
                  child: Container(
                    padding: UniappCSS.smallHorizontalPadding,
                    child: Text(
                      widget.label,
                      style: UniappTextTheme.defaultWidgetStyle,
                    ),
                  ),
                ),
                IconButton(
                  icon: Icon(Icons.videocam),
                  iconSize: UniappCSS.largeIconSize,
                  tooltip: "Capture Video",
                  color: UniappColorTheme.widgetColor,
                  onPressed: () {
                    List<PermissionGroup> permissions =
                        new List<PermissionGroup>();
                    permissions.add(PermissionGroup.camera);
                    if (Platform.isAndroid) {
                      permissions.add(PermissionGroup.storage);
                    }
                    AskForPermission()
                        .checkAndValidatePermission(permissions)
                        .then((List<PermissionGroup> permissionList) async {
                      if (permissionList.isEmpty) {
                        // Checking for available cameras
                        try {
                          WidgetsFlutterBinding.ensureInitialized();
                          cameras = await availableCameras();

                          videoCaptureUtils.cameras = cameras;

                          results = await Navigator.of(context).push(
                            MaterialPageRoute(builder: (context) {
                              return VideoCameraScreen();
                            }),
                          );

                          if (results != null &&
                              results.containsKey("videoName") &&
                              results.containsKey("videoPath")) {
                            // TODO: Implement .add function instead of overwriting the UUIDs.
                            // Will be needed when multiple videos are required.

                            if (results["videoName"] != null &&
                                results["videoPath"] != null) {
                              formMap.cameraUuids
                                  .add(results["videoName"].toString());
                              formMap.formValues[widget.id] =
                                  results["videoName"].toString();
                              AppMetaDataConfig appMetaConfig =
                                  UAAppContext.getInstance().appMDConfig;
                              int retries =
                                  CommonConstants.MEDIA_IMAGE_DEFAULT_RETRIES;

                              if (appMetaConfig != null &&
                                  appMetaConfig.mediaretries != null &&
                                  appMetaConfig.mediaretries != 0) {
                                retries = appMetaConfig.mediaretries;
                              }

                              Map<String, String> additionalProperties =
                                  new Map();
                              additionalProperties[
                                      CommonConstants.EXTERNAL_PROJECT_ID] =
                                  widget.externalProjectId;

                              FormMediaTable formMedia = new FormMediaTable(
                                widget.appId,
                                UAAppContext.getInstance().userID,
                                results["videoName"],
                                0,
                                widget.projectId,
                                results["videoPath"],
                                null,
                                false,
                                CommonConstants.DEFAULT_LATITUDE,
                                CommonConstants.DEFAULT_LONGITUDE,
                                CommonConstants.DEFAULT_ACCURACY,
                                MediaTypeHelper.getValue(MediaType.VIDEO),
                                MediaSubTypeHelper.getValue(MediaSubType.FULL),
                                "mp4",
                                0,
                                0,
                                MediaActionTypeHelper.getValue(
                                    MediaActionType.UPLOAD),
                                retries,
                                MediaUploadStatusHelper.getValue(
                                    MediaUploadStatus.NEW),
                                additionalProperties,
                              );

                              imageCaptureUtils.addToMap(
                                  results["videoName"], formMedia);

                              await UAAppContext.getInstance()
                                  .unifiedAppDBHelper
                                  .insertFormMedia(formMedia);
                            } else {
                              // No valid video received
                              Toast.show("Video not captured!", context,
                                  duration: 2);
                            }
                          } else {
                            AskForPermission().getPermissions(permissionList);
                          }
                        } on CameraException catch (e) {
                          // TODO : Show toast message
                          logError(e.code, e.description);
                        }
                      }
                    });
                  },
                ),
              ],
            ),
            _thumbnails(),
          ],
        ),
      ),
    );
  }
}
