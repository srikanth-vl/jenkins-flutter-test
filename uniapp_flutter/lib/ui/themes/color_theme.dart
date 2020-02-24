import 'package:flutter/material.dart';

import '../../utils/common_constants.dart';

class UniappColorTheme {
  static final Color defaultColor = Colors.black; // #000000
  static final Color invertedColor = Colors.white; // #FFFFFF
  static final Color alternateColor = orangeLight; // #FF9800

  // Change according to the Theme applied.

  static final Color greyLight = Color.fromRGBO(109, 109, 109, 1); // #6D6D6D
  static final Color greyDark = Color.fromRGBO(46, 46, 46, 1); // #2E2E2E

  static final Color greenLight = Color.fromRGBO(0, 150, 136, 1); // #009688
  static final Color greenDark = Color.fromRGBO(0, 137, 123, 1); // #00897B
  static final Color orangeLight = Color.fromRGBO(255, 152, 0, 1); // FF9800

  static final Color blueLight = Color.fromRGBO(79, 195, 247, 1); // #4FC3F7
  static final Color blueDark = Colors.blue[800];

  static final Color whiteDark = Color.fromRGBO(236, 236, 236, 0.95); // #ECECEC

  static final Color textColor = Colors.black;
  static final Color invertedTextColor = Colors.white;

  static final Color listHaloColor = Colors.white ?? whiteDark;
  static final Color clickableLinkColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : Colors.white;

  static final Color headerColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : greyDark;
  static final Color alternateHeaderColor =
      (CommonConstants.DEFAULT_THEME == true) ? Colors.black12 : greenDark;
  static final Color formFieldsColor = invertedColor;
  static final Color widgetColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : greenDark;

  static final Color cancelButtonColor = invertedColor;
  static final Color previewButtonColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : orangeLight;
  static final Color submitButtonColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : orangeLight;

  static final Color showDialogBackgroundColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : greenLight;
  static final Color userProfileColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : greenDark;

  static final Color fabPrimaryButtonColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : greenDark;
  static final Color fabPrimaryTextColor = Colors.white;

  static final Color fabSecondaryButtonColor = orangeLight;
  static final Color fabSecondaryTextColor = Colors.white;

  static final Color fabAlternateButtonColor = Colors.white;
  static final Color fabAlternateTextColor =
      (CommonConstants.DEFAULT_THEME == true) ? blueDark : greenDark;

  static final ThemeData defaultTheme = ThemeData(
    appBarTheme: AppBarTheme(
      color: greyDark,
    ),
    bottomAppBarTheme: BottomAppBarTheme(
      color: greyDark,
    ),
    scaffoldBackgroundColor: greenLight,
    primaryColor: greyDark,
    accentColor: orangeLight,
    fontFamily: 'Montserrat',
  );

  static final ThemeData alternateTheme = ThemeData(
    appBarTheme: AppBarTheme(
      color: blueDark,
    ),
    bottomAppBarTheme: BottomAppBarTheme(
      color: blueDark,
    ),
    scaffoldBackgroundColor: whiteDark,
    primaryColor: greyDark,
    accentColor: blueDark,
    fontFamily: 'Montserrat',
  );

  static ThemeData getTheme() {
    if (CommonConstants.DEFAULT_THEME == true) {
      return alternateTheme;
    }
    return defaultTheme;
  }
}
