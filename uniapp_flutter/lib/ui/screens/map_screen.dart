import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:geolocator/geolocator.dart';
import 'package:latlong/latlong.dart';
import 'dart:math' as math;
import 'dart:async';
import 'dart:io';

import '../helpers/recase_text.dart';
import '../themes/color_theme.dart';
import '../helpers/map_screen_helper.dart';
import '../themes/text_theme.dart';
import '../themes/uniapp_css.dart';

import '../../localization/app_translations.dart';
import '../../utils/screen_navigate_utils.dart';
import '../../utils/string_utils.dart';
import '../../utils/common_utils.dart';
import '../widgets/global/empty_container.dart';
import '../../models/legend_attribute.dart';
import '../../models/map_project_info.dart';
import '../../ua_app_context.dart';
import '../../models/map_config.dart';
import '../../utils/common_constants.dart';
import '../../utils/map_utils.dart';
import '../../models/project_icon_info.dart';
import '../../models/root_config.dart';
import '../../models/sub_app.dart';
import '../widgets/formelements/form_get_directions.dart';

class MapScreen extends StatefulWidget {
  static const String route = '/';
  BuildContext ctxt;
  List<MapProjectInfo> projectInfoList;
  String appId;
  bool isOnline;

  MapScreen({
    this.ctxt,
    this.projectInfoList,
    this.appId,
    this.isOnline,
  });

  @override
  State<StatefulWidget> createState() => MapScreenState(projectInfoList, appId);
}

class MapScreenState extends State<MapScreen> with TickerProviderStateMixin {
  bool isBaseLayerActive = true;
  List<MapProjectInfo> projectInfoList;

  String pServerURL = '';
  Position _currentPosition = Position();
  List<LayerOptions> mapLayer = new List();
  Map<String, String> propertiesMap = {'transparent': 'true'};
  String appId;

  Map<String, String> markerLayerInfoMap = Map();
  ProjectIconInfo projectIconInfoForApp;
  StreamSubscription<Position> positionStream;

  MapConfig mapConfig;
  Map<LegendAttribute, bool> legendMarkerToggleInfo = new Map();
  Map<LegendAttribute, int> legendMarkerCount = new Map();
  Map<LegendAttribute, int> initialLegendMarkerCount = new Map();
  AnimationController _controller;
  LatLng mapCenter = new LatLng(17.40086, 78.34050);
  MapController mapController = new MapController();

  MapScreenState(List<MapProjectInfo> projList, String appId) {
    this.projectInfoList = projList;
    this.appId = appId;
    RootConfig rootConfig = UAAppContext.getInstance().rootConfig;
    SubApp appConfig = rootConfig != null && rootConfig.config != null
        ? rootConfig.config.firstWhere((a) => a.appId == appId)
        : null;
    if (appConfig != null) {
      markerLayerInfoMap =
          appConfig.mapOverLayInfo == null ? Map() : appConfig.mapOverLayInfo;
      projectIconInfoForApp = appConfig.projectIconInfo;
    }
    if (UAAppContext.getInstance().mapConfig != null) {
      mapConfig = UAAppContext.getInstance().mapConfig;
    }
  }

  @override
  void initState() {
    super.initState();

    _controller = new AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 500),
    );

    if (UAAppContext.getInstance().positionStream != null) {
      UAAppContext.getInstance().positionStream.cancel();
    }
    var geolocator = Geolocator();
    var locationOptions =
        LocationOptions(accuracy: LocationAccuracy.high, distanceFilter: 10);

    positionStream = geolocator
        .getPositionStream(locationOptions)
        .listen((Position position) {
      setState(() {
        _currentPosition = position;
        _checkIfMarkerLayerIsPresent();
        mapLayer
            .add(new MarkerLayerOptions(markers: getMarkers(_currentPosition)));
      });
    });

    if (widget.isOnline) {
      LayerOptions layer1 = new TileLayerOptions(
        urlTemplate: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        subdomains: ['a', 'b', 'c'],
      );
      mapLayer.add(layer1);
    }
    legendMarkerToggleInfo =
        MapScreenHelper().initializeLegendMarkerToggleInfo(mapConfig, appId);
    projectInfoList = MapUtils().updateProjectIconInfoLegendAttributes(
        projectInfoList, legendMarkerToggleInfo);
    initialLegendMarkerCount = MapScreenHelper().initializeLegendMarkerCount(
        projectInfoList, projectIconInfoForApp, legendMarkerToggleInfo);

    for (LegendAttribute attribute in initialLegendMarkerCount.keys) {
      if (!MapScreenHelper().checkIfLegendAttributeIsPresent(
          attribute, legendMarkerCount.keys.toList())) {
        legendMarkerCount[attribute] = initialLegendMarkerCount[attribute];
      }
    }
    mapCenter = MapScreenHelper().initializeMapCenter(projectInfoList);
  }

  @override
  Widget build(BuildContext context) {
    if (_currentPosition.latitude == null) {
      return Center(child: CircularProgressIndicator());
    }
    String filePath = _getOfflineMapFilePath();
    if (widget.isOnline == false && filePath != null && filePath.isNotEmpty) {
      // return Text("Offline map");
      return Stack(
        children: <Widget>[
          FlutterMap(
            options: new MapOptions(
              // center: new LatLng(
//          20.35815, 85.80751),
              // 17.40086,78.34050),
              center: mapCenter,
              zoom: 12.0,
              minZoom: 5.0,
              maxZoom: 17.0,
            ),
            layers: mapLayer,
            mapController: mapController,
          ),
          _getFloatingActionButtons(),
          Align(
            alignment: Alignment.bottomCenter,
            child: _getBottomNavigationBar(),
          ),
        ],
      );
    } else {
      return Stack(
        children: <Widget>[
          FlutterMap(
            options: new MapOptions(
              // center: new LatLng(
//          20.35815, 85.80751),
              // 17.40086,
              // 78.34050),
              center: mapCenter,
              zoom: 16.0,
              minZoom: 5.0,
              maxZoom: 20.0,
            ),
            layers: mapLayer,
            mapController: mapController,
          ),
          _getFloatingActionButtons(),
          Align(
            alignment: Alignment.bottomCenter,
            child: _getBottomNavigationBar(),
          ),
        ],
      );
    }

    // return SafeArea(
    //   child: Scaffold(
    //     body:
    //         widget.isOnline == false && filePath != null && filePath.isNotEmpty
    // ?
    // : ,
    //     floatingActionButton: Stack(children: _getFloatingButtons()),
    //     bottomNavigationBar: _getBottomNavigationBar(),
    //   ),
    // );
  }

  Widget _currentLocationFloatingActionButton() {
    return FloatingActionButton(
      // heroTag: "Current Location FAB",
      heroTag: null,
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

  Widget _legendFloatingActionButton() {
    List<String> attrKeys =
        MapScreenHelper().getLegendAttributeKeys(mapConfig, appId);
    List<LegendAttribute> legendBarItems = new List();
    for (LegendAttribute attr in legendMarkerToggleInfo.keys) {
      if (attr.key == attrKeys[1]) {
        legendBarItems.add(attr);
      }
    }
    Color backgroundColor = Theme.of(context).cardColor;
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: new List.generate(legendBarItems.length, (int index) {
        Widget child = new Container(
          height: 70.0,
          width: 56.0,
          alignment: FractionalOffset.center,
          child: new ScaleTransition(
            scale: new CurvedAnimation(
              parent: _controller,
              curve: new Interval(
                  0.0, 1.0 - index / legendBarItems.length / 2.0,
                  curve: Curves.easeOut),
            ),
            child: FloatingActionButton(
              // heroTag: "Legend FAB",
              heroTag: null,
              backgroundColor: backgroundColor,
              mini: true,
              child: Stack(children: <Widget>[
                FutureBuilder(
                  future:
                      CommonUtils.getIconFromStorage(legendBarItems[index].url),
                  builder:
                      (BuildContext context, AsyncSnapshot<File> snapshot) {
                    File imgFile;
                    if (snapshot.hasData) {
                      imgFile = snapshot.data;
                      return IconButton(
                        tooltip: StringUtils.getTranslatedString(
                            legendBarItems[index].label),
                        icon: imgFile == null
                            ? Image.network(legendBarItems[index].url)
                            : Image.file(imgFile),
                        onPressed: () {
                          onToggleLegendMarker(legendBarItems[index],
                              !legendMarkerToggleInfo[legendBarItems[index]]);
                        },
                      );
                    } else if (snapshot.connectionState ==
                        ConnectionState.waiting) {
                      return CircularProgressIndicator();
                    }
                    return EmptyContainer();
                  },
                ),
//                    Positioned(
//                      right: 0,
//                      child: new Container(
//                        padding: EdgeInsets.all(1),
//                        decoration: new BoxDecoration(
//                          color: Colors.red,
//                          borderRadius: BorderRadius.circular(14),
//                        ),
//                        constraints: BoxConstraints(
//                          minWidth: 15,
//                          minHeight: 15,
//                        ),
//                        child: new Text(legendMarkerCount[legendBarItems[index]] == null ? "0" : legendMarkerCount[legendBarItems[index]].toString(),
//                          style: new TextStyle(
//                            color: Colors.white,
//                            fontSize: 16,
//                          ),
//                          textAlign: TextAlign.center,
//                        ),
//                      ),
//                    ),
              ]),
              onPressed: () {
                onToggleLegendMarker(legendBarItems[index],
                    !legendMarkerToggleInfo[legendBarItems[index]]);
              },
            ),
          ),
        );
        return child;
      }).toList()
        ..add(
          new FloatingActionButton(
            backgroundColor: UniappColorTheme.fabSecondaryButtonColor,
            // heroTag: "IDK what FAB is this",
            heroTag: null,
            child: new AnimatedBuilder(
              animation: _controller,
              builder: (BuildContext context, Widget child) {
                return new Transform(
                  transform:
                      new Matrix4.rotationZ(_controller.value * 0.5 * math.pi),
                  alignment: FractionalOffset.center,
                  child: new Icon(
                    _controller.isDismissed ? Icons.location_on : Icons.close,
                    color: UniappColorTheme.fabSecondaryTextColor,
                  ),
                );
              },
            ),
            onPressed: () {
              _controller.isDismissed == true
                  ? _controller.forward()
                  : _controller.reverse();
            },
          ),
        ),
    );
  }

  Widget _mapLayerFloatingActionButton() {
    if (widget.isOnline == true) {
      return FloatingActionButton(
        // heroTag: "Map Layer",
        heroTag: null,
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
                  _legendFloatingActionButton(),
                  SizedBox(
                    height: 8.0,
                  ),
                  _currentLocationFloatingActionButton(),
                  SizedBox(
                    height: 80.0,
                  ),
                ],
              ),
            ),
          ]),
    );
  }

  _onDefaultMap() {
    setState(() {
      isBaseLayerActive = true;
      LayerOptions baseLayer = mapLayer.removeAt(0);
      mapLayer = new List<LayerOptions>();
      mapLayer.add(baseLayer);
      mapLayer
          .add(new MarkerLayerOptions(markers: getMarkers(_currentPosition)));
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
            .add(new MarkerLayerOptions(markers: getMarkers(_currentPosition)));
      }
    });
  }

  @override
  dispose() {
    super.dispose();
    if (positionStream != null) {
      positionStream.cancel();
    }
  }

  Widget _getProjectMetaData(MapProjectInfo project) {
    Map<String, String> keyToValue = project.projectInfo;
    return Container(
      color: UniappColorTheme.alternateHeaderColor,
      child: ListView.separated(
        physics: NeverScrollableScrollPhysics(),
        shrinkWrap: true,
        separatorBuilder: (BuildContext context, int index) {
          return Divider(color: UniappColorTheme.invertedColor);
        },
        itemCount: markerLayerInfoMap.keys.length,
        itemBuilder: (BuildContext context, index) {
          String key = markerLayerInfoMap.keys.elementAt(index);
          return Row(
            mainAxisSize: MainAxisSize.max,
            children: <Widget>[
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(12.0),
                  child: Text(
                    StringUtils.getTranslatedString(
                            markerLayerInfoMap.values.elementAt(index)) ??
                        "-",
                    style: UniappTextTheme.smallInvertedHeader,
                  ),
                ),
              ),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(12.0),
                  child: Text(
                    keyToValue[key] ?? "-",
                    style: UniappTextTheme.smallInvertedHeader,
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  List<Marker> getMarkers(Position position) {
    List<Marker> markers = List();
    for (MapProjectInfo project in projectInfoList) {
      if (project.showProjectMarker != null && project.showProjectMarker) {
        Marker marker = new Marker(
          width: 50.0,
          height: 50.0,
          point: new LatLng(
              project.lat == null || project.lat.isEmpty
                  ? 0.0
                  : double.parse(project.lat),
              project.long == null || project.long.isEmpty
                  ? 0.0
                  : double.parse(project.long)),
          builder: (context) => new Container(
            child: FutureBuilder<File>(
                future: CommonUtils.getIconFromStorage(MapUtils()
                    .getProjectIcon(
                        project.iconInfo == null
                            ? projectIconInfoForApp
                            : project.iconInfo,
                        project.projectInfo)),
                builder: (BuildContext context, AsyncSnapshot<File> snapshot) {
                  File imgFile;
                  if (snapshot.hasData && snapshot.data != null) {
                    imgFile = snapshot.data;
                    return IconButton(
                      icon: (imgFile == null)
                          ? Image.network(MapUtils().getProjectIcon(
                              project.iconInfo == null
                                  ? projectIconInfoForApp
                                  : project.iconInfo,
                              project.projectInfo))
                          : Image.file(imgFile),
                      color: Colors.red,
                      iconSize: 45.0,
                      onPressed: () {
                        showDialog(
                            context: context,
                            builder: (BuildContext context) {
                              return AlertDialog(
                                backgroundColor:
                                    UniappColorTheme.showDialogBackgroundColor,
                                title: Row(
                                  children: <Widget>[
                                    Expanded(
                                      child: Text(
                                          RecaseText().reCase(
                                              "titleCase", project.projectname),
                                          style: UniappTextTheme
                                              .smallInvertedHeader),
                                    ),
                                    FormGetDirections(
                                      latitude: project.lat,
                                      longitude: project.long,
                                      positionStream: positionStream,
                                    ),
                                  ],
                                ),
                                content: Container(
                                  width: MediaQuery.of(context).size.width,
                                  child: _getProjectMetaData(project),
                                ),
                                actions: <Widget>[
                                  Container(
                                    padding: EdgeInsets.fromLTRB(
                                        16.0, 0.0, 16.0, 0.0),
                                    width: MediaQuery.of(context).size.width,
                                    child: FlatButton(
                                      color: (CommonConstants.DEFAULT_THEME ==
                                              true)
                                          ? UniappColorTheme.cancelButtonColor
                                          : UniappColorTheme.submitButtonColor,
                                      child: Text(
                                          StringUtils.getTranslatedString(
                                              "Update Data"),
                                          style:
                                              (CommonConstants.DEFAULT_THEME ==
                                                      true)
                                                  ? UniappTextTheme.smallHeader
                                                  : UniappTextTheme
                                                      .smallInvertedHeader),
                                      onPressed: () {
                                        if (positionStream != null) {
                                          positionStream.cancel();
                                        }
                                        _navigateToProjectForm(
                                            appId,
                                            project.projectid,
                                            CommonConstants.UPDATE_FORM_KEY);
                                      },
                                    ),
                                  ),
                                ],
                              );
                            });
                      },
                    );
                  } else if (snapshot.connectionState ==
                      ConnectionState.waiting) {
                    return CircularProgressIndicator();
                  }
                  return EmptyContainer();
                }),
          ),
        );
        markers.add(marker);
      }
    }
    Marker currentLocMarker = new Marker(
      width: 50.0,
      height: 50.0,
      point: new LatLng(position.latitude, position.longitude),
      builder: (context) => new Container(
        child: IconButton(
          icon: Image.asset(
            "assets/images/icon_my_location.png",
          ),
          color: Colors.red,
          iconSize: 45.0,
          onPressed: () {},
        ),
      ),
    );

    markers.add(currentLocMarker);
    return markers;
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

  String _getOfflineMapFilePath() {
    try {
      Directory _documentsDirectory = UAAppContext.getInstance().appDir;
      var filePath = _documentsDirectory.path +
          "/osmdroid/Offline/vassarMaps/{z}/{x}/{y}.png";

      _checkIfMarkerLayerIsPresent();
      LayerOptions layer1 = new TileLayerOptions(
        tileProvider: FileTileProvider(),
        urlTemplate: filePath,
        additionalOptions: propertiesMap,
        backgroundColor: new Color.fromRGBO(0, 0, 0, 0),
      );
      mapLayer.add(layer1);
      mapLayer
          .add(new MarkerLayerOptions(markers: getMarkers(_currentPosition)));
      return filePath;
    } catch (e) {
      return e.toString();
    }
  }

  List<Widget> legendListWidget(List<LegendAttribute> legendBarItems) {
    List<Widget> legendList = [];

    for (int index = 0; index < legendBarItems.length; index++) {
      int markerCount = MapScreenHelper().getMarkerCountForLegend(
          legendMarkerCount, legendBarItems[index].label);
      legendList.add(
        Expanded(
          child: eachLegendWidget(
            legendBarItems[index],
            markerCount.toString(),
            legendMarkerToggleInfo[legendBarItems[index]],
          ),
        ),
      );
    }
    return legendList;
  }

  Widget eachLegend(List<LegendAttribute> legendBarItems) {
    final legendsBarHeight = 64.0;
    if (legendBarItems.length > 0) {
      return Container(
        height: legendsBarHeight,
        margin: EdgeInsets.symmetric(vertical: 6.0),
        child: SingleChildScrollView(
          child: Row(
            children: legendListWidget(legendBarItems),
          ),
        ),
      );
      // return Container(
      //   height: legendsBarHeight,
      //   margin: EdgeInsets.symmetric(vertical: 6.0),
      //   child: ListView.builder(
      //     scrollDirection: Axis.horizontal,
      //     itemCount: legendBarItems.length,
      //     itemBuilder: (BuildContext context, index) {
      //       return eachLegendWidget(legendBarItems[index], legendMarkerCount[legendBarItems[index]].toString()
      //           , legendMarkerToggleInfo[legendBarItems[index]]);
      //     },
      //   ),
      // );
    }
    return EmptyContainer();
  }

  eachLegendWidget(
      LegendAttribute legendAttribute, String mapMarkerCount, bool visibility) {
    final iconSize = 40.0;
    bool _isVisible = visibility;

    return InkWell(
      onTap: () {
        onToggleLegendMarker(legendAttribute, !_isVisible);
      },
      child: Container(
        padding: EdgeInsets.all(8.0),
        child: Opacity(
          opacity: _isVisible ? 1.0 : 0.4,
          child: Stack(
            children: <Widget>[
              FutureBuilder<File>(
                  future: CommonUtils.getIconFromStorage(legendAttribute.url),
                  builder:
                      (BuildContext context, AsyncSnapshot<File> snapshot) {
                    File imgFile;
                    if (snapshot.hasData && snapshot.data != null) {
                      imgFile = snapshot.data;
                      return IconButton(
                        tooltip: StringUtils.getTranslatedString(
                            legendAttribute.label),
                        icon: (imgFile == null)
                            ? Image.network(legendAttribute.url)
                            : Image.file(imgFile),
                        iconSize: iconSize,
                        color: Colors.white,
                        onPressed: () {
                          onToggleLegendMarker(legendAttribute, !_isVisible);
                        },
                      );
                    } else if (snapshot.connectionState ==
                        ConnectionState.waiting) {
                      return CircularProgressIndicator();
                    }
                    return EmptyContainer();
                  }),
              Positioned(
                // TODO: Undo this hardcoding and figure out dynamic values
                right: 12,
                child: new Container(
                  padding: EdgeInsets.all(1),
                  decoration: new BoxDecoration(
                    color: Colors.red,
                    borderRadius: BorderRadius.circular(15),
                  ),
                  constraints: BoxConstraints(
                    minWidth: 24,
                    minHeight: 24,
                  ),
                  child: new Text(
                    mapMarkerCount == null || mapMarkerCount == "null"
                        ? "0"
                        : mapMarkerCount,
                    style: new TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _getBottomNavigationBar() {
    List<String> attrKeys =
        MapScreenHelper().getLegendAttributeKeys(mapConfig, appId);
    List<LegendAttribute> legendBarItems = new List();
    for (LegendAttribute attr in legendMarkerToggleInfo.keys) {
      if (attr.key == attrKeys[0]) {
        legendBarItems.add(attr);
      }
    }
    return BottomAppBar(child: eachLegend(legendBarItems));
  }

  void onToggleLegendMarker(LegendAttribute legendAttribute, bool isVisible) {
    for (LegendAttribute attribute in legendMarkerToggleInfo.keys) {
      if (attribute == legendAttribute) {
        legendMarkerToggleInfo[attribute] = isVisible;
      }
    }
    updateMapMarkerOnToggle(legendAttribute, isVisible);
  }

  void updateMapMarkerOnToggle(
      LegendAttribute legendAttribute, bool isVisible) {
    setState(() {
      int count = 0;
      for (MapProjectInfo projectInfo in projectInfoList) {
        bool showMarker = true;
        ProjectIconInfo projectIconInfo = new ProjectIconInfo();
        if (projectInfo.iconInfo != null) {
          projectIconInfo = projectInfo.iconInfo;
        } else if (projectIconInfoForApp != null) {
          projectIconInfo.defaultMarker =
              projectIconInfoForApp.defaultMarker == null
                  ? ""
                  : projectIconInfoForApp.defaultMarker;
          projectIconInfo.staticUrl = projectIconInfoForApp.staticUrl;
          projectIconInfo.dynamicKeyName = projectIconInfoForApp.dynamicKeyName;
          List<LegendAttribute> legendAttrs = new List();
          List<String> dynamicUrlValues = MapUtils().getProjectIconInfoValues(
              projectIconInfo, projectInfo.projectInfo);
          for (String value in dynamicUrlValues) {
            for (LegendAttribute attrKey in legendMarkerToggleInfo.keys) {
              if (value.toLowerCase() == attrKey.label.toLowerCase()) {
                legendAttrs.add(attrKey);
              }
            }
          }
          if (legendAttrs.isEmpty) {
            legendAttrs.add(new LegendAttribute(
                key: CommonConstants.SOURCE_KEY,
                label: CommonConstants.DEFAULT_LABEL,
                url: projectIconInfo.defaultMarker));
          }
          projectIconInfo.markerAttributes = legendAttrs;
          projectInfo.iconInfo = projectIconInfo;
        }
        List<LegendAttribute> attributes =
            projectInfo.iconInfo.markerAttributes;
        for (LegendAttribute attribute in attributes) {
          if (MapScreenHelper().checkIfLegendAttributeIsPresent(
              attribute, legendMarkerToggleInfo.keys.toList())) {
            bool check = MapScreenHelper()
                .getMarkerToggleInfo(legendMarkerToggleInfo, attribute.label);
            if (!check) {
              showMarker = check;
              break;
            }
          }
        }
        projectInfo.showProjectMarker = showMarker;
        if (projectInfo.iconInfo.markerAttributes != null &&
            projectInfo.iconInfo.markerAttributes.isNotEmpty) {
          if (MapScreenHelper().checkIfLegendAttributeIsPresent(
              legendAttribute, projectInfo.iconInfo.markerAttributes)) {
            if (showMarker) {
              count++;
            }
            for (LegendAttribute attribute
                in projectInfo.iconInfo.markerAttributes) {
              int markerCount = MapScreenHelper()
                  .getMarkerCountForLegend(legendMarkerCount, attribute.label);
              int maxCount = MapScreenHelper().getMarkerCountForLegend(
                  initialLegendMarkerCount, attribute.label);
              if (!isVisible) {
                if (markerCount > 0) {
                  legendMarkerCount[attribute] = markerCount - 1;
                }
              } else {
                if (maxCount > markerCount) {
                  legendMarkerCount[attribute] =
                      legendMarkerCount[attribute] + 1;
                }
              }
            }
          }
        }
      }
      if (isVisible) {
        legendMarkerCount[legendAttribute] = count;
      } else {
        legendMarkerCount[legendAttribute] = 0;
      }
      int index = _checkIfMarkerLayerIsPresent();
      if (index >= 0) {
        mapLayer
            .add(new MarkerLayerOptions(markers: getMarkers(_currentPosition)));
      }
    });
  }

  _navigateToProjectForm(appId, projectId, formActionType) {
    ScreenNavigateUtils()
        .navigateToProjectFormScreen(context, appId, projectId, formActionType);
  }
}
