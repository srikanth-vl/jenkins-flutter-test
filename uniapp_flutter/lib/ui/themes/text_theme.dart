import 'package:flutter/material.dart';

import './color_theme.dart';

/// Available Text themes:
///
/// 1. Error (default/inverted).
/// 2. Large Header (default/inverted).
/// 3. Small Header (default/inverted).
/// 4.  (default/inverted).
///

class UniappTextTheme {
  static final TextStyle defaultErrorTextStyle = TextStyle(
    fontWeight: FontWeight.w500,
    color: UniappColorTheme.defaultColor,
    fontFamily: 'Montserrat',
    fontSize: 18.0,
  );

  static final TextStyle invertedErrorTextStyle = TextStyle(
    fontWeight: FontWeight.w500,
    fontFamily: 'Montserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 18.0,
  );

  static final TextStyle largeHeader = TextStyle(
    fontFamily: 'Monteserrat',
    color: UniappColorTheme.defaultTheme.accentColor,
    fontSize: 32.0,
  );

  static final TextStyle largeInvertedHeader = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.alternateTheme.accentColor,
    fontSize: 32.0,
  );

  static final TextStyle smallHeader = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.defaultColor,
    fontSize: 16.0,
  );

  static final TextStyle smallInvertedHeader = TextStyle(
    fontFamily: 'Monteserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 16.0,
  );

  static final TextStyle defaultTextStyle = TextStyle(
      fontFamily: 'Monteserrat',
      color: UniappColorTheme.defaultColor,
      fontSize: 14.0);

  static final TextStyle invertedTextStyle = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 12.0,
  );

  static final TextStyle defaultWidgetStyle = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.defaultColor,
    fontSize: 16.0,
  );

  static final TextStyle invertedWidgetStyle = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 16.0,
  );
}
