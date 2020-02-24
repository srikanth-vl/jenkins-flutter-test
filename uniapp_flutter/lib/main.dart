import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'localization/app_translations_delegate.dart';
import 'ua_app_context.dart';
import 'models/route_parameters.dart';
import 'ui/screens/download_screen.dart';
import 'ui/screens/change_password_screen.dart';
import 'ui/screens/get_otp_screen.dart';
import 'ui/screens/projectform_screen.dart';
import 'ui/screens/filter_screen.dart';
import 'ui/screens/project_grouping_screen.dart';
import 'ui/screens/project_list_screen.dart';
import 'ui/screens/splash_screen.dart';
import 'ui/screens/login_screen.dart';
import 'ui/screens/home_screen.dart';
import 'ui/themes/color_theme.dart';
import 'ui/widgets/geotagging_widget.dart';
import 'utils/common_constants.dart';

void main() => runApp(UniApp());

class UniApp extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _UniappMainState();
}

class _UniappMainState extends State<UniApp> {
  AppTranslationsDelegate _newLocaleDelegate;
  @override
  void initState() {
    super.initState();
    setlocaleFromSharedPreference();
    _newLocaleDelegate = AppTranslationsDelegate(newLocale: null);
    UAAppContext.getInstance().onLocaleChanged = onLocaleChange;
  }

  void onLocaleChange(Locale locale) {
    setState(() {
      UAAppContext.getInstance().changeLanguage(locale.languageCode);
      _newLocaleDelegate = AppTranslationsDelegate(newLocale: locale);
    });
  }

  setlocaleFromSharedPreference() {
    UAAppContext.getInstance().getLocale().then((locale) {
      if (locale == 'en') return;
      setState(() {
        _newLocaleDelegate = AppTranslationsDelegate(newLocale: Locale(locale));
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      initialRoute: '/',
      routes: {
        '/': (context) => SplashScreen(),
        CommonConstants.homeRoute: (context) => HomeScreen(),
        CommonConstants.loginRoute: (context) => LoginScreen(),
        CommonConstants.projectGroupRoute: (context) {
          RouteParameters r = ModalRoute.of(context).settings.arguments;
          if (r != null && r.appId != null && r.appId.isNotEmpty) {
            return ProjectGroupScreen(
              appId: r.appId,
              attributes: r.groupingAttributes,
              sortType: r.sortType,
            );
          } else
            return SplashScreen();
        },
        CommonConstants.projectListRoute: (context) {
          RouteParameters r = ModalRoute.of(context).settings.arguments;
          if (r != null && r.appId != null && r.appId.isNotEmpty) {
            return ProjectListScreen(
              appId: r.appId,
              sortType: r.sortType,
              groupingKey: r.groupingKey,
              groupingValue: r.groupingValue,
              projectMasterDataTableList: r.projectMasterDataTableList,
            );
          } else
            return SplashScreen();
        },
        CommonConstants.projectFormRoute: (context) {
          RouteParameters r = ModalRoute.of(context).settings.arguments;
          if (r != null && r.appId != null && r.appId.isNotEmpty) {
            return ProjectFormScreen(
              appId: r.appId,
              projectId: r.projectId,
              formActiontype: r.formActionType,
            );
          } else
            return SplashScreen();
        },
        CommonConstants.getOTPRoute: (context) => GetOTPScreen(),
        CommonConstants.changePasswordRoute: (context) =>
            ChangePasswordScreen(),
        CommonConstants.downloadsRoute: (context) => DownloadScreen(),
        CommonConstants.filterRoute: (context) {
          RouteParameters r = ModalRoute.of(context).settings.arguments;
          if (r != null && r.appId != null && r.appId.isNotEmpty) {
            return FilterScreen(
              appId: r.appId,
              projectList: UAAppContext.getInstance().projectList,
              filterKeyToValue:
                  UAAppContext.getInstance().filterSelectedValueMap,
            );
          } else
            return SplashScreen();
        },
        CommonConstants.geoTaggingRoute: (context) {
          RouteParameters r = ModalRoute.of(context).settings.arguments;
          if (r != null &&
              r.geoTaggingWidgetId != null &&
              r.geoTaggingWidgetId.isNotEmpty) {
            return GeotaggingWidget(
              ctxt: r.context,
              id: r.geoTaggingWidgetId,
              gpsValidation: r.gpsValidation,
              projLat: r.projLat,
              projLon: r.projLon,
            );
          } else
            return SplashScreen();
        }
      },
      debugShowCheckedModeBanner: false,
      // theme: UniappColorTheme.defaultTheme,
      theme: UniappColorTheme.getTheme(),
      localizationsDelegates: [
        _newLocaleDelegate,
        //provides localised strings
        GlobalMaterialLocalizations.delegate,
        //provides RTL support
        GlobalWidgetsLocalizations.delegate,
      ],
      supportedLocales: [
        const Locale("en", ""),
        const Locale("hi", ""),
        const Locale("or", "")
      ],
    );
  }
}
