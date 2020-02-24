import 'package:geolocator/geolocator.dart';
import 'package:rxdart/rxdart.dart';

class CurrentLocationBloc {

  final _currentPositionController = PublishSubject<Position>();

  Stream<Position> get currentPos =>
      _currentPositionController.stream;

  getCurrentPosition() async {
    final Geolocator geolocation = Geolocator()..forceAndroidLocationManager;
    geolocation
        .getCurrentPosition(desiredAccuracy: LocationAccuracy.best)
        .then((Position position) {
      _currentPositionController.add(position);
    }).catchError((e) {
      print(e);
    });
  }

  dispose() {
    _currentPositionController.close();
  }
}

//final currentLocBloc = CurrentLocationBloc();
