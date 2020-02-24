import 'dart:async';

import 'package:geolocator/geolocator.dart';

import '../../../utils/map_utils.dart';

import '../../../ui/screens/navigation_screen.dart';
import '../../../utils/network_utils.dart';
import 'package:flutter/material.dart';

import '../../themes/uniapp_css.dart';
import '../../themes/color_theme.dart';

class FormGetDirections extends StatelessWidget {
  final String latitude;
  final String longitude;
  StreamSubscription<Position> positionStream;

  FormGetDirections({
    @required this.latitude,
    @required this.longitude,
    this.positionStream,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width:  36.0,
      height: 36.0,
      child: FloatingActionButton(
        tooltip: "Get Directions to this site",
        backgroundColor: UniappColorTheme.fabAlternateButtonColor,
        child: Container(
          child: Icon(
            Icons.directions,
            color: UniappColorTheme.fabAlternateTextColor,
            size: UniappCSS.defaultIconSize,
          ),
        ),
        onPressed: () async {
          if(positionStream != null) {
            positionStream.cancel();
          }
          bool isOnline = await NetworkUtils().hasActiveInternet();
          if(isOnline) {
            MapUtils().openMap(
              double.parse(latitude),
              double.parse(longitude),
            );
          } else{
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => NavigationScreen(
                  projLat: double.parse(latitude),
                  projLon: double.parse(longitude),
                ),
              ),
            );
          }
        },
      ),
    );
  }
}
