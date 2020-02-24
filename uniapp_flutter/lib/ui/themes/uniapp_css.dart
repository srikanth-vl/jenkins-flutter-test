import 'package:flutter/material.dart';

import './color_theme.dart';

class UniappCSS {
  static final EdgeInsets smallHorizontalAndVerticalPadding =
      const EdgeInsets.all(8.0);
  static final EdgeInsets largeHorizontalAndVerticalPadding =
      const EdgeInsets.all(16.0);

  static final EdgeInsets smallHorizontalPadding =
      const EdgeInsets.symmetric(horizontal: 8.0);
  static final EdgeInsets largeHorizontalPadding =
      const EdgeInsets.symmetric(horizontal: 16.0);

  static final EdgeInsets smallVerticalPadding =
      const EdgeInsets.symmetric(vertical: 8.0);
  static final EdgeInsets largeVerticalPadding =
      const EdgeInsets.symmetric(vertical: 16.0);

  static final TextStyle largeHeader = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.defaultColor,
    fontSize: 18.0,
  );

  static final TextStyle largeInvertedHeader = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 18.0,
  );

  static final TextStyle mediumHeader = TextStyle(
    fontFamily: 'Monteserrat',
    color: UniappColorTheme.defaultColor,
    fontSize: 16.0,
  );

  static final TextStyle mediumInvertedHeader = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 16.0,
  );

  static final TextStyle smallHeader = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.invertedTextColor,
    //color: UniappColorTheme.defaultColor,
    fontSize: 14.0,
  );

  static final TextStyle smallInvertedHeader = TextStyle(
    fontFamily: 'Monteserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 14.0,
  );

  static final TextStyle text = TextStyle(
      fontFamily: 'Monteserrat',
      color: UniappColorTheme.defaultColor,
      fontSize: 12.0);

  static final TextStyle invertedText = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 12.0,
  );

  static final TextStyle boldLargeHeader = TextStyle(
    fontWeight: FontWeight.w800,
    color: UniappColorTheme.defaultColor,
    fontFamily: 'Montserrat',
    fontSize: 18.0,
  );

  static final TextStyle boldLargeInvertedHeader = TextStyle(
    fontWeight: FontWeight.w800,
    fontFamily: 'Montserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 18.0,
  );

  static final TextStyle boldMediumHeader = TextStyle(
    fontWeight: FontWeight.w800,
    color: UniappColorTheme.defaultColor,
    fontFamily: 'Monteserrat',
    fontSize: 16.0,
  );

  static final TextStyle boldMediumInvertedHeader = TextStyle(
    fontWeight: FontWeight.w800,
    fontFamily: 'Montserrat',
    //color: UniappColorTheme.textColor,
    color: UniappColorTheme.invertedColor,
    fontSize: 16.0,
  );

  static final TextStyle boldSmallHeader = TextStyle(
    fontWeight: FontWeight.w800,
    color: UniappColorTheme.defaultColor,
    fontFamily: 'Montserrat',
    fontSize: 14.0,
  );

  static final TextStyle boldSmallInvertedHeader = TextStyle(
    fontWeight: FontWeight.w800,
    fontFamily: 'Monteserrat',
    color: UniappColorTheme.invertedColor,
    fontSize: 14.0,
  );

  static final TextStyle titleStyle = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.defaultColor,
    fontSize: 16.0,
  );

  static final TextStyle widgetStyle = TextStyle(
    fontFamily: 'Montserrat',
    color: UniappColorTheme.defaultColor,
    fontSize: 12.0,
  );

  static final double smallIconSize = 16.0;
  static final double largeIconSize = 32.0;
  static final double defaultIconSize = 24.0;

  static final double smallCardElevation = 4.0;
  static final double largeCardElevation = 8.0;

  // Form Button bar default height.
  static final double buttonBarButtonHeight = 48.0;

  static final BorderRadius widgetBorderRadius = BorderRadius.circular(4.0);
  static final Border widgetBorder = Border.all(
    width: 1.0,
    color: UniappColorTheme.widgetColor,
  );

  static final double haloSize = 48.0;

  // @Kanishk
  // TODO: Make a new widget for sizedbox spaces (Use form-divider class?).
}
