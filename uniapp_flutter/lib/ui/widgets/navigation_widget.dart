import 'dart:async';
import 'dart:math';
import '../../utils/network_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:geolocator/geolocator.dart';
import 'package:latlong/latlong.dart';

import '../../ua_app_context.dart';

class NavigationWidget extends StatefulWidget {
  BuildContext ctxt;
  double projLat;
  double projLon;

  NavigationWidget({
    this.ctxt,
    this.projLat,
    this.projLon,
  });

  @override
  State<StatefulWidget> createState() => NavigationWidgetState(projLat, projLon);
}

class NavigationWidgetState extends State<NavigationWidget> {
  String pServerURL = '';
  TextStyle style = TextStyle(fontFamily: 'Montserrat', fontSize: 20.0);
  Position _currentPosition = Position();
  List<LayerOptions> mapLayer = new List();
  Map<String, String> propertiesMap = {'transparent': 'true'};
  LatLng destinationLatLng = null;
  bool isOnline = false;
  Marker destinationMarker;
  List<Marker> mapMarkers = new List();

  StreamSubscription<Position> positionStream;
  var _documentsDirectory;
  LatLng _destinationLoc;

  NavigationWidgetState(double projLat, double projLon){
    _destinationLoc = new LatLng(projLat, projLon);
  }

  @override
  void initState() {
    super.initState();

    if (UAAppContext.getInstance().positionStream != null) {
      UAAppContext.getInstance().positionStream.cancel();
    }
//    TODO Cancel position stream of MAP screen

    var geolocator = Geolocator();
    var locationOptions =
    LocationOptions(accuracy: LocationAccuracy.high, distanceFilter: 10);

    networkUtils.hasActiveInternet().then((isOnline) {
      if (isOnline) {
        this.isOnline = isOnline;
        LayerOptions layer1 = new TileLayerOptions(
          urlTemplate: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
          subdomains: ['a', 'b', 'c'],
        );
        mapLayer.add(layer1);
      }
    });

    positionStream = geolocator
        .getPositionStream(locationOptions)
        .listen((Position position) {
      setState(() {
        _currentPosition = position;
        _showMarkers(_currentPosition, _destinationLoc);
        mapLayer.add(new MarkerLayerOptions(markers: mapMarkers));
      });
    });

  }

  @override
  Widget build(BuildContext context) {
    if (_currentPosition.latitude == null) {
      return Center(child: CircularProgressIndicator());
    }
    return SafeArea(
      child: Scaffold(
        body: isOnline == false
            ? FutureBuilder(
            future: _downloadMapFile(),
            builder:
                (BuildContext context, AsyncSnapshot<String> snapshot) {
              if (snapshot.hasData) {
                return FlutterMap(
                    options: new MapOptions(
                      center: new LatLng(
//                            20.35815, 85.80751),
//                          17.40086, 78.34050),
                            _currentPosition.latitude, _currentPosition.longitude),
                      zoom: 13.0,
                      minZoom: 5.0,
                      maxZoom: 25.0,
                    ),
                    layers: mapLayer);
              } else {
                return CircularProgressIndicator();
              }
            })
            : FlutterMap(
          options: new MapOptions(
            center: new LatLng(
//                    20.35815, 85.80751),
//                17.40086, 78.34050),
                    _currentPosition.latitude, _currentPosition.longitude),
            zoom: 13.0,
            minZoom: 5.0,
            maxZoom: 25.0,
          ),
          layers: mapLayer,
        ),
      ),
    );
  }

  Future<String> _downloadMapFile() async {
    try {
      _documentsDirectory = UAAppContext.getInstance().appDir;
      var filePath = _documentsDirectory.path +
          "/osmdroid/Offline/vassarMaps/{z}/{x}/{y}.png";

      LayerOptions layer1 = new TileLayerOptions(
        tileProvider: FileTileProvider(),
        urlTemplate: filePath,
        additionalOptions: propertiesMap,
        backgroundColor: new Color.fromRGBO(0, 0, 0, 0),
      );
      mapLayer.add(layer1);
      mapLayer.add(new MarkerLayerOptions(markers: mapMarkers));
      return filePath;
    } catch (e) {
      print("exception : " + e.toString());
      return "exc";
    }
  }

  _showMarkers(Position currentLocation, LatLng destinationLoc) {
    setState(() {
      if (mapMarkers.isNotEmpty) {
        mapMarkers.clear();
      }

     /* if (currentLocation != null) {
        mapMarkers.add(_showCurrentLocationMarker(currentLocation));
      }*/

      if (destinationLoc != null) {
        mapMarkers.add(_showDroppedMarker(destinationLoc));
      }

      if(currentLocation != null && destinationLoc != null){
        double latDiff = destinationLoc.latitude - currentLocation.latitude;
        double lonDiff = destinationLoc.longitude - currentLocation.longitude;
        double angle = atan2(latDiff, lonDiff);
        Marker currentLocMarker = new Marker(
            width: 50.0,
            height: 50.0,
            point: new LatLng(currentLocation.latitude, currentLocation.longitude),
            builder: (context) => new Container(
              child: new GestureDetector(
                behavior:  HitTestBehavior.opaque,
                child: new Transform.rotate(
                  origin: const Offset(0.0, 0.0),
                  angle: (( angle ?? 0) * -1) - 1.5708,
                  child: new Icon(Icons.arrow_downward, size: 30.0,
                      color: Colors.indigoAccent),
                ),
              ),
            )
        );
        mapMarkers.add(currentLocMarker);
      }
    });
  }

  Marker _showCurrentLocationMarker(Position position) {
    Marker marker = new Marker(
      width: 50.0,
      height: 50.0,
      point: new LatLng(position.latitude, position.longitude),
      builder: (context) => new Container(
        child: IconButton(
          icon: Image.asset("assets/images/icon_my_location.png"),
          color: Colors.blue,
          iconSize: 45.0,
          onPressed: () {},
        ),
      ),
    );
    return marker;
  }

  Marker _showDroppedMarker(LatLng latLng) {
    destinationLatLng = latLng;
    destinationMarker = new Marker(
      width: 50.0,
      height: 50.0,
      point: new LatLng(latLng.latitude, latLng.longitude),
      builder: (context) => new Container(
        child: IconButton(
          icon: Icon(Icons.location_on),
          color: Colors.redAccent,
          onPressed: () {},
          iconSize: 45.0,
        ),
      ),
    );
    return destinationMarker;
  }

  @override
  dispose() {
    super.dispose();
    if (positionStream != null) {
      positionStream.cancel();
    }
  }
}
