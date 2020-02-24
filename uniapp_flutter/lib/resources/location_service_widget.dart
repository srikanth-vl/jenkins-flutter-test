import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:geolocator/geolocator.dart';
import '../models/user_location.dart';
import 'package:flutter_compass/flutter_compass.dart';
import "dart:math" show pi;

class LocationService extends StatefulWidget {
  @override
  _LocationState createState() => _LocationState();
}

class _LocationState extends State<LocationService> {
  Position _currentPosition;
  double _direction;

  Future<UserLocation> fetchLocation() async{
   await _getCurrentLocation();

   double latitude = _currentPosition.latitude;
   double longitude = _currentPosition.longitude;
   double accuracy = _currentPosition.accuracy;
   double speedAccuracy = _currentPosition.speedAccuracy;
   DateTime time = _currentPosition.timestamp;
   double altitude = _currentPosition.altitude;
   double bearing = _direction;
//    double angle = ((bearing ?? 0) * (pi / 180) * -1);

    UserLocation userLocation = new UserLocation(latitude: latitude, longitude: longitude, accuracy: accuracy,
        speedAccuracy: speedAccuracy, time: time, altitude: altitude, bearing: bearing, provider: 'gps');

    print(userLocation.bearing);
    return userLocation;
  }

  @override
  void initState() {
    super.initState();
    FlutterCompass.events.listen((double direction) {
      setState(() {
        _direction = direction;
      });
    });
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Location"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            if (_currentPosition != null)
              Text(
                  "LAT: ${_currentPosition.latitude}, LNG: ${_currentPosition.longitude}, Accuracy: ${_currentPosition.accuracy}, Bearing: ${_direction}"),
            FlatButton(
              child: Text("Get location"),
              onPressed: () {
                fetchLocation();
              },
            ),
          ],
        ),
      ),
    );
  }

  _getCurrentLocation() async {
    final Geolocator geolocation = Geolocator()..forceAndroidLocationManager;

    geolocation
        .getCurrentPosition(desiredAccuracy: LocationAccuracy.best)
        .then((Position position) {
      setState(() {
        _currentPosition = position;
      });
      displayLocation();
    }).catchError((e) {
      print(e);
    });
  }

  displayLocation() {
    double latitude = _currentPosition.latitude;
    double longitude = _currentPosition.longitude;
    double accuracy = _currentPosition.accuracy;
    double speedAccuracy = _currentPosition.speedAccuracy;
    DateTime time = _currentPosition.timestamp;
    double altitude = _currentPosition.altitude;
    double bearing = _direction;
//    double angle = ((bearing ?? 0) * (pi / 180) * -1);

    UserLocation userLocation = new UserLocation(latitude: latitude, longitude: longitude, accuracy: accuracy,
        speedAccuracy: speedAccuracy, time: time, altitude: altitude, bearing: bearing, provider: 'gps');

    print(userLocation);
    return userLocation;
  }
}

