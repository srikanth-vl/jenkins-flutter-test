import 'dart:async';

import '../../ui/themes/color_theme.dart';
import '../../ui/themes/uniapp_css.dart';

import '../../models/gps_validation.dart';
import '../../utils/geotag_validation_utils.dart';

import '../../localization/app_translations.dart';
import '../../utils/network_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:geolocator/geolocator.dart';
import 'package:latlong/latlong.dart';
import 'package:toast/toast.dart';

import '../../ua_app_context.dart';
import 'global/empty_container.dart';

class GeotaggingWidget extends StatefulWidget {
  final String id;
  BuildContext ctxt;
  GpsValidation gpsValidation;
  String projLat;
  String projLon;

  GeotaggingWidget({
    this.id,
    this.ctxt,
    this.gpsValidation,
    this.projLat,
    this.projLon,
  });

  @override
  State<StatefulWidget> createState() => GeotaggingWidgetState();
}

class GeotaggingWidgetState extends State<GeotaggingWidget> {
  String pServerURL = '';
  TextStyle style = TextStyle(fontFamily: 'Montserrat', fontSize: 20.0);
  Position _currentPosition = Position();
  List<LayerOptions> mapLayer = new List();
  Map<String, String> propertiesMap = {'transparent': 'true'};
  LatLng droppedLocation = null;
  bool isOnline = false;
  Marker droppedMarker;
  List<Marker> mapMarkers = new List();

  StreamSubscription<Position> positionStream;
  var _documentsDirectory;
  Position _droppedPosition = null;

  MapController mapController = new MapController();
  bool isBaseLayerActive = true;
  LatLng mapCenter = new LatLng(0.0, 0.0);

  @override
  void initState() {
    super.initState();

    if (UAAppContext.getInstance().positionStream != null) {
      UAAppContext.getInstance().positionStream.cancel();
    }

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
        _showMarkers(_currentPosition, _droppedPosition);
        mapCenter = new LatLng(position.latitude, position.longitude);
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
        body: Stack(
            children: <Widget>[
              isOnline == false
                  ? FutureBuilder(
                  future: _downloadMapFile(),
                  builder:
                      (BuildContext context, AsyncSnapshot<String> snapshot) {
                    if (snapshot.hasData) {
                      return FlutterMap(
                        options: new MapOptions(
                          center: new LatLng(
//                            20.35815, 85.80751),
//                              17.40086, 78.34050),
                              _currentPosition.latitude, _currentPosition.longitude),
                          zoom: 13.0,
                          minZoom: 5.0,
                          maxZoom: 25.0,
                          onLongPress: (latlng) {
                            Toast.show(
                                "Selected Location ${latlng.latitude}  ::${latlng.longitude}",
                                context);
                            _droppedPosition = new Position(
                                latitude: latlng.latitude,
                                longitude: latlng.longitude);
                            _showMarkers(_currentPosition, _droppedPosition);
                          },
                        ),
                        layers: mapLayer,
                        mapController: mapController,);
                    } else {
                      return CircularProgressIndicator();
                    }
                  })
                  : FlutterMap(
                options: new MapOptions(
                  center: new LatLng(
//                    20.35815, 85.80751),
//                      17.40086, 78.34050),
                      _currentPosition.latitude, _currentPosition.longitude),
                  zoom: 13.0,
                  minZoom: 5.0,
                  maxZoom: 25.0,
                  onLongPress: (LatLng latlng) {
                    Toast.show(
                        "Selected Location ${latlng.latitude}  ::${latlng.longitude}",
                        context);
                    _droppedPosition = new Position(
                        latitude: latlng.latitude, longitude: latlng.longitude);
                    _showMarkers(_currentPosition, _droppedPosition);
                  },
                ),
                layers: mapLayer,
                mapController: mapController,
              ),
              _getFloatingActionButtons(),
            ]),
        bottomNavigationBar: _createSubmitButton(context),
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

  _showMarkers(Position currentLocation, Position droppedPosition) {
    setState(() {
      if (mapMarkers.isNotEmpty) {
        mapMarkers.clear();
      }

      if (currentLocation != null) {
        mapMarkers.add(_showCurrentLocationMarker(currentLocation));
      }

      if (droppedPosition != null) {
        LatLng droppedLocation =
            LatLng(droppedPosition.latitude, droppedPosition.longitude);
        mapMarkers.add(_showDroppedMarker(droppedLocation));
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
    droppedLocation = latLng;
    droppedMarker = new Marker(
      width: 50.0,
      height: 50.0,
      point: new LatLng(latLng.latitude, latLng.longitude),
      builder: (context) => new Container(
        child: IconButton(
          icon: Icon(Icons.add_location),
          color: Colors.blueAccent,
          onPressed: () {},
          iconSize: 45.0,
        ),
      ),
    );
    return droppedMarker;
  }

  _createSubmitButton(BuildContext context) {
    return Material(
      elevation: 4.0,
      borderRadius: BorderRadius.circular(4.0),
      color: Theme.of(context).accentColor,
      child: MaterialButton(
        minWidth: MediaQuery.of(context).size.width,
        padding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
        onPressed: () async {
          if (droppedLocation != null) {
            Position droppedLoc = new Position(longitude: droppedLocation.longitude, latitude: droppedLocation.latitude);
            bool isValid = await geotagValidationUtils.validateGeotagImage(droppedLoc, widget.gpsValidation, widget.projLat, widget.projLon);
            if(isValid) {
              _navigateToForm();
            } else{
              Toast.show("Please select a valid location.", context,
                  duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
            }
          } else {
            // TODO : Log error
            Toast.show("No valid location dropped.", context,
                duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
          }
        },
        child: Text(
          AppTranslations.of(context).text("submit"),
          textAlign: TextAlign.center,
          style:
              style.copyWith(color: Colors.white, fontWeight: FontWeight.bold),
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
  }

  _onWillPop() {
    _navigateToForm();
  }

  _navigateToForm() {
    double lat = 0.0;
    double lon = 0.0;

    if (droppedLocation != null) {
      lat = droppedLocation.latitude;
      lon = droppedLocation.longitude;
    }

    Navigator.of(context).pop({
      'lat': lat,
      'lon': lon,
    });
  }

  Widget _getFloatingActionButtons() {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: <Widget>[
            Align(
              alignment: Alignment.topRight,
              child: _mapLayerFloatingActionButton(),
            ),
            Align(
              alignment: Alignment.bottomRight,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  SizedBox(
                    height: 8.0,
                  ),
                  _currentLocationFloatingActionButton(),
                ],
              ),
            ),
          ]),
    );
  }

  Widget _mapLayerFloatingActionButton() {
    if (isOnline) {
      return FloatingActionButton(
        backgroundColor: UniappColorTheme.fabPrimaryButtonColor,
        child: Icon(
          Icons.layers,
          size: UniappCSS.defaultIconSize,
          color: UniappColorTheme.fabPrimaryTextColor,
        ),
        onPressed: () {
          showDialog(
              context: context,
              builder: (BuildContext context) {
                double outerContainerHeight = 160.0;
                double innerContainerHeight = 120.0;

                return AlertDialog(
                  titlePadding: EdgeInsets.all(8.0),
                  contentPadding: EdgeInsets.all(8.0),
                  title: Row(
                    children: <Widget>[
                      Expanded(
                        child: Text(AppTranslations.of(context)
                            .text("select_map_view")),
                      ),
                    ],
                  ),
                  content: Container(
                    height: outerContainerHeight,
                    child: Row(
                      children: <Widget>[
                        Expanded(
                          child: Container(
                            decoration: BoxDecoration(
                              borderRadius: UniappCSS.widgetBorderRadius,
                              border: UniappCSS.widgetBorder,
                            ),
                            child: Column(
                              children: <Widget>[
                                Container(
                                  height: innerContainerHeight,
                                  width: MediaQuery.of(context).size.width,
                                  decoration: BoxDecoration(
                                    color: Colors.amber,
                                    image: DecorationImage(
                                      fit: BoxFit.fitHeight,
                                      image: AssetImage(
                                          'assets/images/default.jpeg'),
                                    ),
                                  ),
                                  child: new FlatButton(
                                      padding: EdgeInsets.all(0.0),
                                      onPressed: () => _onDefaultMap(),
                                      child: null),
                                ),
                                SizedBox(
                                  height: 8.0,
                                ),
                                Expanded(
                                  child: Text(AppTranslations.of(context)
                                      .text("default_view")),
                                ),
                              ],
                            ),
                          ),
                        ),
                        SizedBox(
                          width: 8.0,
                        ),
                        Expanded(
                          child: Container(
                            decoration: BoxDecoration(
                              borderRadius: UniappCSS.widgetBorderRadius,
                              border: UniappCSS.widgetBorder,
                            ),
                            child: Column(
                              children: <Widget>[
                                Container(
                                  height: innerContainerHeight,
                                  width: MediaQuery.of(context).size.width,
                                  decoration: BoxDecoration(
                                    image: DecorationImage(
                                      fit: BoxFit.fitHeight,
                                      image: AssetImage(
                                          'assets/images/satellite.jpeg'),
                                    ),
                                  ),
                                  child: new FlatButton(
                                      padding: EdgeInsets.all(0.0),
                                      onPressed: () => _onSatelliteMap(),
                                      // onPressed: _onSatelliteMap(),
                                      child: null),
                                ),
                                SizedBox(
                                  height: 8.0,
                                ),
                                Expanded(
                                  child: Text(AppTranslations.of(context)
                                      .text("satelite_view")),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              });
        },
      );
    }
    return EmptyContainer();
  }

  Widget _currentLocationFloatingActionButton() {
    return FloatingActionButton(
      backgroundColor: UniappColorTheme.fabAlternateButtonColor,
      child: Icon(
        Icons.my_location,
        size: UniappCSS.defaultIconSize,
        color: UniappColorTheme.fabAlternateTextColor,
      ),
      onPressed: () {
        mapCenter = new LatLng(
          _currentPosition.latitude,
          _currentPosition.longitude,
        );
        mapController.move(
          mapCenter,
          13.0,
        );
      },
    );
  }

  _onDefaultMap() {
    setState(() {
      isBaseLayerActive = true;
      LayerOptions baseLayer = mapLayer.removeAt(0);
      mapLayer = new List<LayerOptions>();
      mapLayer.add(baseLayer);
      mapLayer
          .add(new MarkerLayerOptions(markers: mapMarkers));
    });
  }

  _onSatelliteMap() {
    setState(() {
      isBaseLayerActive = false;
      LayerOptions satelliteLayer = new TileLayerOptions(
        urlTemplate:
        'http://52.250.0.64:8080/geoserver/gwc/service/wmts?layer=BLUIS:20191104_4326np&transparent=true&style=&tilematrixset=EPSG:900913&Service=WMTS&Request=GetTile&Version=1.0.0&Format=image/png&TileMatrix=EPSG:900913:{z}&TileCol={x}&TileRow={y}',
        additionalOptions: propertiesMap,
        backgroundColor: new Color.fromRGBO(0, 0, 0, 0),
      );

      int index = _checkIfMarkerLayerIsPresent();
      if (index >= 0) {
        mapLayer.add(satelliteLayer);
        mapLayer
            .add(new MarkerLayerOptions(markers: mapMarkers));
      }
    });
  }

  int _checkIfMarkerLayerIsPresent() {
    int index = -1;
    for (int i = 0; i < mapLayer.length; i++) {
      if (mapLayer[i] is MarkerLayerOptions) {
        mapLayer.removeAt(i);
        return i;
      }
    }
    return index;
  }
}
