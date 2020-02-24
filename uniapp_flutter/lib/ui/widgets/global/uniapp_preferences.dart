import 'package:flutter/material.dart';

import './image_avatar.dart';
// import '../../helpers/form_values.dart';
import '../../themes/color_theme.dart';
import '../../themes/text_theme.dart';
import '../../themes/uniapp_css.dart';
import '../../../models/enabled_language.dart';
import '../../../ua_app_context.dart';
import '../../../utils/common_constants.dart';

class UniappPreferences {
  // Widget _toggleThemeWidget() {
  //   if (CommonConstants.DEFAULT_THEME == true) {
  //     return Icon(
  //       Icons.brightness_low,
  //       color: Colors.yellow[800],
  //       size: UniappCSS.defaultIconSize,
  //     );
  //   }
  //   return Icon(
  //     Icons.brightness_3,
  //     size: UniappCSS.defaultIconSize,
  //   );
  // }

  List<EnabledLanguage> _getLanguages() {
    return UAAppContext.getInstance().getEnabledLangauge();
  }

  preferenceBottomSheet(context) {
    List<EnabledLanguage> languages = _getLanguages();
    showModalBottomSheet(
        isScrollControlled: true,
        context: context,
        builder: (BuildContext context) {
          return Container(
            color: Colors.black12,
            padding: UniappCSS.smallHorizontalAndVerticalPadding,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                // Manage Profile Card
                Card(
                  color: UniappColorTheme.userProfileColor,
                  elevation: UniappCSS.smallCardElevation,
                  child: ListTile(
                    leading: Container(
                      width: 42.0,
                      height: 42.0,
                      child: ImageAvatar(
                        imagePath: CommonConstants.PREFERENCE_USER_LOGO,
                        radius: double.infinity,
                      ),
                    ),
                    title: Text(
                      'User Profile',
                      style: UniappTextTheme.smallInvertedHeader,
                    ),
                    subtitle: Text("Manage your profile",
                        style: UniappTextTheme.invertedTextStyle),
                    trailing: Icon(
                      Icons.chevron_right,
                      color: UniappColorTheme.invertedColor,
                    ),
                    onTap: () {
                      // TODO:
                      // Navigate to Project Form Screen with the user form.
                    },
                  ),
                ),

                // Language Setting Expandable Card
                Card(
                  child: ExpansionTile(
                    initiallyExpanded: true,
                    leading: Container(
                      width: 42.0,
                      height: 42.0,
                      child: Icon(
                        Icons.language,
                        size: UniappCSS.largeIconSize,
                        color: UniappColorTheme.widgetColor,
                      ),
                    ),
                    title: Text(
                      'Language',
                      style: UniappCSS.titleStyle,
                    ),
                    children: <Widget>[
                      Divider(
                        color: UniappColorTheme.orangeLight,
                        height: 2.0,
                        indent: 8.0,
                        endIndent: 8.0,
                      ),
                      ListView.builder(
                        shrinkWrap: true,
                        itemCount: languages.length,
                        itemBuilder: (BuildContext context, int index) {
                          var eachLanguageData = languages.elementAt(index);
                          return ListTile(
                              trailing: Image.asset(
                                'assets/images/${languages.elementAt(index).locale}.png',
                                height: 32.0,
                              ),
                              title: Text('${languages.elementAt(index).name}'),
                              onTap: () => {
                                    UAAppContext.getInstance().onLocaleChanged(
                                        Locale(
                                            languages.elementAt(index).locale))
                                  });
                        },
                      ),
                    ],
                  ),
                ),

                // Card(
                //   child: ListTile(
                //     leading: Container(
                //       child: Icon(
                //         Icons.lightbulb_outline,
                //         size: UniappCSS.largeIconSize,
                //         color: UniappColorTheme.widgetColor,
                //       ),
                //     ),
                //     title: Text(
                //       'Theme',
                //       style: UniappTextTheme.smallHeader,
                //     ),
                //     trailing: _toggleThemeWidget(),
                //     onTap: () {
                //       CommonConstants.DEFAULT_THEME =
                //           !CommonConstants.DEFAULT_THEME;
                //     },
                //   ),
                // ),
              ],
            ),
          );
        });
  }
}
