import 'package:geolocator/geolocator.dart';
import 'dart:async';

import '../ua_app_context.dart';

class GeolocatorUtil {
  Position _currentPosition;

  Future<Position> getCurrentPosition() async {
    if (UAAppContext.getInstance().positionStream != null) {
      UAAppContext.getInstance().positionStream.cancel();
    }
    final Geolocator geolocation = Geolocator()..forceAndroidLocationManager;

    Position position = await geolocation.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.best);
    return position;
  }
}

final locationUtil = GeolocatorUtil();
