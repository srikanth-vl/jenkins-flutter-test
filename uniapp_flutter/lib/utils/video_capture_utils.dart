import 'package:camera/camera.dart';

class VideoCaptureUtils {
  List<CameraDescription> _cameras;

  List<CameraDescription> get cameras => _cameras;

  set cameras(List<CameraDescription> cameras) {
    _cameras = cameras;
  }
}

final videoCaptureUtils = VideoCaptureUtils();
