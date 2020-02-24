import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:permission_handler/permission_handler.dart';
import 'dart:io';

import '../global/empty_container.dart';
import '../../helpers/ask_for_permissions.dart';
import '../../helpers/form_values.dart';
import '../../screens/camera_screen.dart';
import '../../screens/image_preview_screen.dart';
import '../../../db/models/form_media_table.dart';
import '../../../models/app_meta_data_config.dart';
import '../../../ua_app_context.dart';
import '../../../utils/common_constants.dart';
import '../../../utils/image_capture_utils.dart';
import '../../../utils/media_action_type.dart';
import '../../../utils/media_subtype.dart';
import '../../../utils/media_type.dart';
import '../../../utils/media_upload_status.dart';
import '../../../models/gps_validation.dart';

import '../../themes/uniapp_css.dart';
import '../../themes/color_theme.dart';
import '../../themes/text_theme.dart';

class FormCameraWidget extends StatefulWidget {
  final String id;
  final String label;
  final String appId;
  final String projectId;
  final String userId;
  final String externalProjectId;
  final Position currentPosition;
  final int maxValue;
  final String projectLat;
  final String projectLon;
  final GpsValidation gpsValidation;

  FormCameraWidget({
    @required this.id,
    @required this.label,
    @required this.appId,
    @required this.projectId,
    @required this.userId,
    @required this.externalProjectId,
    @required this.currentPosition,
    @required this.maxValue,
    @required this.projectLat,
    @required this.projectLon,
    @required this.gpsValidation,
  });

  @override
  FormCameraWidgetState createState() =>
      FormCameraWidgetState(currentPos: currentPosition);
}

class FormCameraWidgetState extends State<FormCameraWidget> {
  Position currentPos;
  AskForPermission askForPermission = new AskForPermission();

  FormCameraWidgetState({
    @required this.currentPos,
  });

  Map _results = new Map();

  _thumbnails() {
    if (_results != null &&
        _results.containsKey("images") &&
        _results["images"] != null) {
      return Container(
        width: double.maxFinite,
        height: 80.0,
        padding: EdgeInsets.fromLTRB(0, 8, 0, 0),
        child: ListView.builder(
          scrollDirection: Axis.horizontal,
          itemBuilder: (BuildContext context, int index) {
            return _createListThumbnailItem(index);
          },
          itemCount: _results["images"].length,
        ),
      );
    }

    return EmptyContainer();
  }

  _createListThumbnailItem(int index) {
    return Container(
      width: 64.0,
      height: 64.0,
      margin: EdgeInsets.fromLTRB(4, 8, 8, 4),
      child: FlatButton(
        color: Colors.black,
        onPressed: () {
          _navigateToPreviewScreen(_results["images"].values.elementAt(index));
        },
        padding: EdgeInsets.all(0.0),
        child: Image.file(
          File(_results["images"].keys.elementAt(index)),
        ),
      ),
    );
  }

  _navigateToPreviewScreen(String name) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => ImagePreviewScreen(
          imageName: name,
          appId: widget.appId,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: UniappCSS.smallHorizontalPadding,
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
                    padding: UniappCSS.smallHorizontalAndVerticalPadding,
                    child: Text(
                      widget.label,
                      style: UniappTextTheme.defaultWidgetStyle,
                    ),
                  ),
                ),
                IconButton(
                  icon: Icon(Icons.camera_alt),
                  iconSize: UniappCSS.largeIconSize,
                  tooltip: "Camera",
                  color: UniappColorTheme.widgetColor,
                  onPressed: () {
                    List<PermissionGroup> permissions =
                        new List<PermissionGroup>();
                    permissions.add(PermissionGroup.camera);
                    if (Platform.isAndroid) {
                      permissions.add(PermissionGroup.storage);
                    }
                    askForPermission
                        .checkAndValidatePermission(permissions)
                        .then((List<PermissionGroup> permissionList) async {
                      if (permissionList.isEmpty) {
                        _results = await Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (context) => CameraScreen(
                              currentPosition: currentPos,
                              retainedImages: _results["images"],
                              retainedImagesGeolocationInfo:
                                  _results["imageGeolocationInfo"],
                              maxImages: widget.maxValue,
                              projLat: widget.projectLat,
                              projLon: widget.projectLon,
                              gpsValidation: widget.gpsValidation,
                            ),
                          ),
                        );
                        print("Results: $_results");

                        if (_results != null &&
                            _results.containsKey("images") &&
                            _results.containsKey("imageGeolocationInfo")) {
                          Map<String, String> images = _results["images"];
                          Map<String, String> imageGeolocationInfo =
                              _results["imageGeolocationInfo"];
                          formMap.formValues[widget.id] = new List<String>();
                          images.keys.forEach((path) async {
                            String imageName = images[path];

                            String imageGeolocation =
                                imageGeolocationInfo[path];
                            double lat = CommonConstants.DEFAULT_LATITUDE;
                            double lon = CommonConstants.DEFAULT_LONGITUDE;
                            double accuracy = CommonConstants.DEFAULT_ACCURACY;
                            double bearing = CommonConstants.DEFAULT_BEARING;

                            if (imageGeolocation != null &&
                                imageGeolocation.isNotEmpty) {
                              // lat:lon:accuracy:bearing
                              List<String> infoList =
                                  imageGeolocation.split(":");
                              if (infoList != null && infoList.isNotEmpty) {
                                lat = double.parse(infoList.elementAt(0));
                                lon = double.parse(infoList.elementAt(1));
                                accuracy = double.parse(infoList.elementAt(2));
                                bearing = double.parse(infoList.elementAt(3));
                              }
                            }

                            formMap.cameraUuids.add(imageName);
                            formMap.formValues[widget.id]
                                .add("$imageName##$lat##$lon");

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
                            if (widget.externalProjectId != null &&
                                widget.externalProjectId.isNotEmpty) {
                              additionalProperties[
                                      CommonConstants.EXTERNAL_PROJECT_ID] =
                                  widget.externalProjectId == null ||
                                          widget.externalProjectId.isEmpty
                                      ? null
                                      : widget.externalProjectId;
                            }

                            FormMediaTable formMedia = new FormMediaTable(
                              widget.appId,
                              UAAppContext.getInstance().userID,
                              imageName,
                              0,
                              widget.projectId,
                              path,
                              null,
                              false,
                              lat == null
                                  ? CommonConstants.DEFAULT_LATITUDE
                                  : lat,
                              lon == null
                                  ? CommonConstants.DEFAULT_LONGITUDE
                                  : lon,
                              accuracy == null
                                  ? CommonConstants.DEFAULT_ACCURACY
                                  : accuracy,
                              MediaTypeHelper.getValue(MediaType.IMAGE),
                              MediaSubTypeHelper.getValue(MediaSubType.FULL),
                              "png",
                              0,
                              0,
                              MediaActionTypeHelper.getValue(
                                  MediaActionType.UPLOAD),
                              retries,
                              MediaUploadStatusHelper.getValue(
                                  MediaUploadStatus.NEW),
                              additionalProperties,
                            );

                            imageCaptureUtils.addToMap(imageName, formMedia);

                            await UAAppContext.getInstance()
                                .unifiedAppDBHelper
                                .insertFormMedia(formMedia);
                          });
                        } else {
                          // TODO : Show no images toast
                        }
                      } else {
                        AskForPermission().getPermissions(permissionList);
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
