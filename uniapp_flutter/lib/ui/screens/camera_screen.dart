import '../../ui/helpers/appbar_helper.dart';
import '../../utils/geotag_validation_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter_compass/flutter_compass.dart';
import 'package:geolocator/geolocator.dart';
import 'package:path_provider/path_provider.dart';
import 'package:camera/camera.dart';
import 'package:toast/toast.dart';
import 'package:uuid/uuid.dart';

import 'dart:async';
import 'dart:io';

import '../themes/uniapp_css.dart';
import '../themes/color_theme.dart';
import '../widgets/global/empty_container.dart';
import '../widgets/preview_widget.dart';
import '../widgets/global/uniapp_appbar.dart';
import '../../ua_app_context.dart';
import '../../models/gps_validation.dart';
import '../../utils/common_utils.dart';
import '../../localization/app_translations.dart';
import '../../utils/common_constants.dart';
import 'image_preview_screen.dart';

class CameraScreen extends StatefulWidget {
  final Position currentPosition;
  final Map<String, String> retainedImages;
  final Map<String, String> retainedImagesGeolocationInfo;
  final int maxImages;
  final String projLat;
  final String projLon;
  final GpsValidation gpsValidation;

  CameraScreen({
    @required this.currentPosition,
    @required this.retainedImages,
    @required this.retainedImagesGeolocationInfo,
    @required this.maxImages,
    @required this.projLat,
    @required this.projLon,
    @required this.gpsValidation,
  });

  @override
  CameraScreenState createState() {
    return CameraScreenState(
      currentPosition: currentPosition,
      retainedImages: retainedImages,
      retainedImagesGeolocationInfo: retainedImagesGeolocationInfo,
    );
  }
}

class CameraScreenState extends State<CameraScreen> {
  CameraController controller;
  List cameras;
  int selectedCameraIdx;
  String imagePath;
  BuildContext context;
  Position currentPosition;
  Map<String, String> retainedImages;
  Map<String, String> retainedImagesGeolocationInfo;
  double _direction;
  StreamSubscription<Position> positionStream;
  StreamSubscription<double> bearingStream;

  List<String> _imagePaths = new List();
  Map<String, String> _imageList = new Map();
  Map<String, String> _imageGeolocationInfo = new Map();
  bool isVisible = true;

  CameraScreenState({
    @required this.currentPosition,
    @required this.retainedImages,
    @required this.retainedImagesGeolocationInfo,
  });

  Future<bool> _onWillPop() async {
    Navigator.of(context).pop({'images': null});
    return Future.value(false);
  }

  Future _initCameraController(CameraDescription cameraDescription) async {
    if (controller != null) {
      await controller.dispose();
    }

    controller = CameraController(cameraDescription, ResolutionPreset.high);

    // If the controller is updated then update the UI.
    controller.addListener(() {
      if (mounted) {
        setState(() {});
      }

      if (controller.value.hasError) {
        print('Camera error ${controller.value.errorDescription}');
      }
    });

    try {
      await controller.initialize();
    } on CameraException catch (e) {
      _showCameraException(e);
    }

    if (mounted) {
      setState(() {});
    }
  }

  @override
  void initState() {
    super.initState();

    UAAppContext.getInstance().context = context as BuildContext;

    var geolocator = Geolocator();
    var locationOptions =
        LocationOptions(accuracy: LocationAccuracy.high, distanceFilter: 10);

    positionStream = geolocator
        .getPositionStream(locationOptions)
        .listen((Position position) {
      setState(() {
        currentPosition = position;
      });
    });

    bearingStream = FlutterCompass.events.listen((double direction) {
      setState(() {
        _direction = direction;
      });
    });

    if (retainedImages != null) {
      _imagePaths = retainedImages.keys.toList();
      _imageList = retainedImages;
      _imageGeolocationInfo = retainedImagesGeolocationInfo;
    }

    availableCameras().then((availableCameras) {
      cameras = availableCameras;

      if (cameras.length > 0) {
        setState(() {
          selectedCameraIdx = 0;
        });

        _initCameraController(cameras[selectedCameraIdx]).then((void v) {});
      } else {
        print("No camera available");
      }
    }).catchError((err) {
      print('Error: $err.code\nError Message: $err.message');
    });
  }

  @override
  Widget build(BuildContext context) {
    this.context = context;
    return new WillPopScope(
      onWillPop: _onWillPop,
      child: SafeArea(
        child: Scaffold(
          backgroundColor: Colors.black12,
          appBar: UniAppBar(
            appbarParams:
                AppbarParameterMapping().getAppbarMapping("CameraScreen"),
            showMultilineAppbar:
                AppbarParameterMapping().getActionCount("CameraScreen"),
            showProjectListView: false,
            active: false,
            appId: null,
          ),
          body: Container(
            child: Column(
              mainAxisSize: MainAxisSize.max,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: <Widget>[
                Expanded(
                  child: Stack(
                    children: <Widget>[
                      Stack(
                        children: <Widget>[
                          Container(
                            width: MediaQuery.of(context).size.width,
                            child: _cameraPreviewWidget(),
                          ),
                          _callLocation(),
                        ],
                      ),
                      Align(
                        alignment: Alignment.bottomCenter,
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: <Widget>[
                            _thumbnails(),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                SizedBox(height: 8.0),
                _cameraButtonBar(),
                SizedBox(height: 8.0),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  dispose() {
    super.dispose();
    if (positionStream != null) {
      positionStream.cancel();
    }
    if (bearingStream != null) {
      bearingStream.cancel();
    }
  }

  /// Display Camera preview.
  Widget _cameraPreviewWidget() {
    if (controller == null || !controller.value.isInitialized) {
      return Center(
        child: CircularProgressIndicator(),
      );
    }

    return AspectRatio(
      aspectRatio: controller.value.aspectRatio,
      child: CameraPreview(controller),
    );
  }

  /// Display the control bar with buttons to take pictures
  Widget _captureControlRowWidget(context) {
    return Align(
      alignment: Alignment.center,
      child: FloatingActionButton(
          backgroundColor: UniappColorTheme.fabAlternateButtonColor,
          child: Icon(
            Icons.camera,
            size: UniappCSS.largeIconSize,
            color: UniappColorTheme.fabAlternateTextColor,
          ),
          onPressed: () {
            _onCapturePressed(context);
          }),
    );
  }

  /// Display a row of toggle to select the camera (or a message if no camera is available).
  Widget _cameraTogglesRowWidget() {
    if (cameras == null || cameras.isEmpty) {
      return EmptyContainer();
    }

    CameraDescription selectedCamera = cameras[selectedCameraIdx];
    CameraLensDirection lensDirection = selectedCamera.lensDirection;

    return Align(
      alignment: Alignment.centerLeft,
      child: FlatButton.icon(
        onPressed: _onSwitchCamera,
        icon: Icon(
          _getCameraLensIcon(lensDirection),
          color: Colors.white,
        ),
        // icon: Image.asset(
        //   'assets/images/switch-white.png',
        //   width: 36.0,
        //   color: Colors.white,
        // ),
        label: Text(
          "${lensDirection.toString().substring(lensDirection.toString().indexOf('.') + 1)}",
          style: UniappCSS.invertedText,
        ),
      ),
    );
  }

  Widget _cameraButtonBar() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        Expanded(
          child: _cameraTogglesRowWidget(),
        ),
        Expanded(
          child: _captureControlRowWidget(context),
        ),
        Expanded(
          child: Align(
            alignment: Alignment.centerRight,
            child: IconButton(
              icon: Icon(
                Icons.save,
                color: Colors.white,
              ),
              iconSize: 32.0,
              tooltip: 'Submit Images',
              onPressed: () {
                _submitImages();
              },
            ),
          ),
        ),
      ],
    );
  }

  Widget _callLocation() {
    if (currentPosition != null && currentPosition.accuracy != null) {
      String direction = '-';
      if (_direction != null) direction = _direction.toStringAsFixed(2);
      if (isVisible == true) {
        return Align(
          alignment: Alignment.topRight,
          child: Container(
            padding: UniappCSS.smallHorizontalAndVerticalPadding,
            width: MediaQuery.of(context).size.width / 2,
            child: Card(
              elevation: 4.0,
              color: Colors.black12,
              child: Stack(
                children: <Widget>[
                  Container(
                    padding: UniappCSS.largeHorizontalPadding,
                    height: 96.0,
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      mainAxisSize: MainAxisSize.min,
                      children: <Widget>[
                        Row(
                          children: <Widget>[
                            Expanded(
                              child: Text(
                                "${AppTranslations.of(context).text("Accuracy")}: ",
                                style: UniappCSS.invertedText,
                              ),
                            ),
                            Expanded(
                              child: Align(
                                alignment: Alignment.center,
                                child: Text(
                                  currentPosition.accuracy.toStringAsFixed(2) ??
                                      "-",
                                  style: UniappCSS.invertedText,
                                ),
                              ),
                            ),
                          ],
                        ),
                        Row(
                          children: <Widget>[
                            Expanded(
                              child: Text(
                                "${AppTranslations.of(context).text("Bearing")}: ",
                                style: UniappCSS.invertedText,
                              ),
                            ),
                            Expanded(
                              child: Align(
                                alignment: Alignment.center,
                                child: Text(
                                  direction ?? "-",
                                  style: UniappCSS.invertedText,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  Positioned(
                    top: -12.0,
                    right: -12.0,
                    child: IconButton(
                      icon: Icon(Icons.info),
                      iconSize: 24.0,
                      color: Colors.white,
                      onPressed: () {
                        setState(() {
                          isVisible = !isVisible;
                        });
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      } else {
        return Align(
          alignment: Alignment.topRight,
          child: IconButton(
            icon: Icon(Icons.info),
            iconSize: 24.0,
            color: Colors.white,
            onPressed: () {
              setState(() {
                isVisible = !isVisible;
              });
            },
          ),
        );
      }
    } else {
      return Container(
        height: 96.0,
        child: Center(
          child: CircularProgressIndicator(),
        ),
      );
    }
  }

  IconData _getCameraLensIcon(CameraLensDirection direction) {
    switch (direction) {
      case CameraLensDirection.back:
        return Icons.camera_rear;
      case CameraLensDirection.front:
        return Icons.camera_front;
      case CameraLensDirection.external:
        return Icons.camera;
      default:
        return Icons.device_unknown;
    }
  }

  void _onSwitchCamera() {
    selectedCameraIdx =
        selectedCameraIdx < cameras.length - 1 ? selectedCameraIdx + 1 : 0;
    CameraDescription selectedCamera = cameras[selectedCameraIdx];
    _initCameraController(selectedCamera);
  }

  void _onCapturePressed(context) async {
    if (_imagePaths != null && _imagePaths.length >= widget.maxImages) {
      CommonUtils.showToast(CommonConstants.MAX_IMAGES_REACHED, context);
      return;
    }
    // Take the Picture in a try / catch block. If anything goes wrong,
    // catch the error.
    String imageName = '${Uuid().v1().toString()}.png';
    double lat = currentPosition.latitude;
    double lon = currentPosition.longitude;
    double accuracy = currentPosition.accuracy;
    double bearing = _direction;

    try {
      final Directory extDir = UAAppContext.getInstance().appDir;
      final String dirPath =
          '${extDir.path}/Images/${UAAppContext.getInstance().userID}';
      Directory mediaDir = await Directory(dirPath).create(recursive: true);
      final String filePath = '${mediaDir.path}/$imageName';
      print("Filepath --> $filePath");
      await controller.takePicture(filePath);
      Map results = await Navigator.of(context).push(
        /// @ROHAN.
        /// TODO: Use this preview screen for preview.
        // MaterialPageRoute(
        //   builder: (context) => ImagePreviewScreen(
        //     imageName: name,
        //     appId: widget.appId,
        //   ),
        // ),

        MaterialPageRoute(
          builder: (context) => PreviewImageScreen(
            imagePath: filePath,
          ),
        ),
      );

      if (results != null &&
          results.containsKey('saved') &&
          results['saved'] == true) {
        print("IMAGE STATUS : Image clicked $filePath");

        /**
         * 1. Add the image to the list of image UUIDs
         * 2. Build the widget to reflect this with a new image thumbnail
         * */
        bool isValid = await geotagValidationUtils.validateGeotagImage(
            currentPosition,
            widget.gpsValidation,
            widget.projLat,
            widget.projLon);

        if (isValid) {
          setState(() {
            _imagePaths.add(filePath);
            _imageList[filePath] =
                imageName.substring(0, imageName.lastIndexOf('.'));
            print("_imageGeolocationInfo[path] ::$lat");
            print("_imageGeolocationInfo[path] ::$lon");
            _imageGeolocationInfo[filePath] =
                "${lat == null ? 0.0 : lat}:${lon == null ? 0.0 : lon}:${accuracy == null ? CommonConstants.DEFAULT_ACCURACY : bearing}:${bearing == null ? CommonConstants.DEFAULT_BEARING : bearing}";
          });
        } else {
          Toast.show(
            "Not a valid image according to location!",
            context,
            duration: Toast.LENGTH_LONG,
            gravity: Toast.BOTTOM,
            backgroundColor: Colors.white,
            textColor: Colors.black,
          );
          File imgFile = new File(filePath);
          imgFile.delete();
        }
      } else if (results != null &&
          results.containsKey('saved') &&
          results['saved'] == false) {
        // Do nothing, let the user click and save an image
        print("IMAGE STATUS : Image not clicked");
      }
    } catch (e) {
      // If an error occurs, log the error to the console.
      print(e);
    }
  }

  _createListThumbnailItem(int index) {
    return Container(
      padding: EdgeInsets.fromLTRB(4, 4, 4, 0),
      width: 80.0,
      height: 80.0,
      margin: EdgeInsets.fromLTRB(0, 8, 8, 4),
      child: FlatButton(
        color: Colors.black,
        onPressed: () {},
        padding: EdgeInsets.all(0.0),
        child: Image.file(
          File(_imagePaths[index]),
        ),
      ),
    );
  }

  _deleteCapturedImage(int index) {
    /** Do not have to delete image from DB, has not been entered yet.
     * Will only be entered if the user saves images and moves back to
     * the form (form_camera) */
    // 1. TODO : Delete image from file system
    // 2. Delete image from the list
    // 3. Redraw widget to reflect image deletion

    setState(() {
      _imageList.remove(_imagePaths[index]);
      _imageGeolocationInfo.remove(_imagePaths[index]);
      _imagePaths.removeAt(index);
    });
  }

  _thumbnails() {
    return Container(
      color: Colors.black26,
      width: double.maxFinite,
      height: 120.0,
      padding: EdgeInsets.fromLTRB(0, 8, 0, 0),
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        itemBuilder: (BuildContext context, int index) {
          return Stack(
            children: <Widget>[
              _createListThumbnailItem(index),
              Positioned(
                top: -0.0,
                right: -0.0,
                child: Container(
                  width: 24.0,
                  height: 24.0,
                  child: FloatingActionButton(
                    heroTag: index,
                    backgroundColor: Colors.white,
                    child: Container(
                      child: Icon(
                        Icons.close,
                        color: Colors.red,
                        size: 12.0,
                        semanticLabel: "Delete Image",
                      ),
                    ),
                    onPressed: () {
                      _deleteCapturedImage(index);
                    },
                  ),
                ),
              ),
            ],
          );
        },
        itemCount: _imagePaths.length,
      ),
    );
  }

  _submitImages() {
    Navigator.of(context).pop({
      'images': _imageList,
      'imageGeolocationInfo': _imageGeolocationInfo,
    });
  }

  void _showCameraException(CameraException e) {
    String errorText = 'Error: ${e.code}\nError Message: ${e.description}';
    print(errorText);
    print('Error: ${e.code}\n${e.description}');
  }
}
