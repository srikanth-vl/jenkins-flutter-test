import 'package:flutter/cupertino.dart';
import 'package:location/location.dart';
import 'package:toast/toast.dart';

class LocationUtil {

  void getLocationPermission(BuildContext context) async {
    final Location location = new Location();
    try {
//     await not required, it will be called parallely
      location.requestService(); //to launch location permission popup
    } catch (e) {
      if (e.code == 'PERMISSION_DENIED') {
        Toast.show("This App requires Location Permission", context,
            duration: 2);
        print('Permission denied');
      }
    }
  }
}