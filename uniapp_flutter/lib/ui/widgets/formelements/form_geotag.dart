import '../../../models/gps_validation.dart';

import '../../../utils/screen_navigate_utils.dart';

import '../../../utils/string_utils.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../helpers/form_values.dart';
import '../../helpers/ask_for_permissions.dart';
import '../../themes/uniapp_css.dart';
import '../../themes/color_theme.dart';
import '../../themes/text_theme.dart';

class FormGeotag extends StatefulWidget {
  final String id;
  GpsValidation gpsValidation;
  final String projLat;
  final String projLon;
  FormGeotag({this.id, this.gpsValidation, this.projLat, this.projLon,});

  @override
  _FormGeotagState createState() => _FormGeotagState();
}

class _FormGeotagState extends State<FormGeotag> {
  final controller = TextEditingController();
  AskForPermission askForPermission = new AskForPermission();
  Map _results = new Map();

  Icon geoTagIcon = Icon(Icons.location_on);

  @override
  void initState() {
    super.initState();
    controller.addListener(() {
      formMap.formValues[widget.id] = controller.text;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: UniappCSS.smallHorizontalPadding,
      child: Container(
        decoration: BoxDecoration(
          borderRadius: UniappCSS.widgetBorderRadius,
          border: UniappCSS.widgetBorder,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Row(
              mainAxisSize: MainAxisSize.max,
              children: <Widget>[
                Expanded(
                  child: Container(
                    padding: UniappCSS.smallHorizontalAndVerticalPadding,
                    child: Text(
                      StringUtils.getTranslatedString("Geo-Tag"),
                      style: UniappTextTheme.defaultWidgetStyle,
                    ),
                  ),
                ),
                IconButton(
                  icon: geoTagIcon,
                  iconSize: UniappCSS.largeIconSize,
                  tooltip: "Geo-Tag",
                  color: UniappColorTheme.widgetColor,
                  onPressed: () {
                    List<PermissionGroup> permissions =
                        new List<PermissionGroup>();
                    permissions.add(PermissionGroup.location);
                    askForPermission
                        .checkAndValidatePermission(permissions)
                        .then((List<PermissionGroup> permissionList) async {
                      if (permissionList.isEmpty) {
                        _results = await ScreenNavigateUtils()
                            .navigateToGeoTaggingScreen(context, widget.id, widget.gpsValidation, widget.projLat, widget.projLon,);
                        if (_results != null &&
                            _results.containsKey("lat") &&
                            _results.containsKey("lon")) {
                          // TODO : Set these coordinates to
                          double lat = _results["lat"];
                          double lon = _results["lon"];
                          formMap.formValues[widget.id] = "$lat,$lon";

                          setState(() {
                            geoTagIcon = Icon(Icons.check);
                          });
                        } else {
                          // TODO : @AITHA
                        }
                      } else {
                        AskForPermission().getPermissions(permissionList);
                      }
                    });
                  },
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
