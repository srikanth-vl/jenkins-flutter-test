class UserLocation {
  final double latitude;
  final double longitude;
  final DateTime time;
  final double altitude;
  final double accuracy;
  final double bearing;
//  final double verticalAccuracy;
//  final double bearingAccuracy;
  final double speedAccuracy;
  final String provider;
  UserLocation({this.longitude,
                this.latitude,
                this.time, // in DateTime
                this.altitude, // in meters
                this.bearing, // in degree
                this.accuracy, // in meters
//                this.verticalAccuracy, // in meters
//                this.bearingAccuracy, // in meters
                this.speedAccuracy, // in meters/s
                this.provider, // as String,either gps/ network/ fused
                });
}