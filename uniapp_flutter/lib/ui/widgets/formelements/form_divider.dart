import 'package:flutter/material.dart';

import '../../themes/uniapp_css.dart';

class FormDivider extends StatelessWidget {
  final Color color;
  final double thickness;
  final double paddingLR;
  const FormDivider({this.color, this.thickness, this.paddingLR, });

  @override
  Widget build(BuildContext context) {
    return Container(
      child: Padding(
        padding: UniappCSS.smallHorizontalAndVerticalPadding,
        child: Divider(
          color: color,
          thickness: thickness,
          endIndent: paddingLR,
          indent: paddingLR,
        ),
      ),
    );
  }
}
