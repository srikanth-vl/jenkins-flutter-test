import 'dart:async';

import '../helpers/form_values.dart';

import '../themes/text_theme.dart';

import '../helpers/home_screen_helper.dart';
import '../../utils/screen_navigate_utils.dart';
import '../../localization/app_translations.dart';
import '../../resources/localization_config_provider.dart';
import '../../utils/network_utils.dart';
import '../../event/event_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:toast/toast.dart';
import '../../blocs/localization_config_bloc.dart';
import '../../blocs/map_config_bloc.dart';
import '../../blocs/root_config_bloc.dart';
import '../../models/localization_config.dart';
import '../../models/root_config.dart';
import '../../ua_app_context.dart';
import '../../utils/common_constants.dart';
import '../../utils/shared_preference_util.dart';
import '../widgets/global/empty_container.dart';
import '../../blocs/app_meta_config_bloc.dart';
import '../widgets/global/image_banner.dart';
import '../../log/uniapp_logger.dart';
import '../../resources/root_config_provider.dart';
import '../../resources/map_config_provider.dart';

class SplashScreen extends StatefulWidget {
  @override
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  bool _isLoading = true;
  bool _isLoggedIn = false;
  bool _isAvailableToRedirect = false;
  bool _isAppMetaConfigDataAvailable = false;
  Logger logger = getLogger("SplashScreen");
  StreamSubscription<CheckInternetEvent> internetSubscription;

  RootConfigBloc rootConfigBloc = RootConfigBloc();
  LocalizationBloc localizationBloc = LocalizationBloc();
  MapConfigBloc mapConfigBloc = MapConfigBloc();
  AppMetaConfigBloc appMetaConfigBloc = AppMetaConfigBloc();

  HomeScreenHelper homeScreenHelper = HomeScreenHelper();

  redirectFromSplashScreen() async {
    print(
        "$_isLoggedIn && $_isAvailableToRedirect && $_isAppMetaConfigDataAvailable");

    if (_isLoggedIn) {
      if (UAAppContext.getInstance().userID == CommonConstants.GUEST_USER_ID &&
          _isAvailableToRedirect) {
        _navigateToLoginScreen();
      } else if (_isAvailableToRedirect && _isAppMetaConfigDataAvailable) {
        _setLoginPreferences();
      }
    } else if (_isAvailableToRedirect && _isAppMetaConfigDataAvailable) {
      _navigateToLoginScreen();
    } else if (!_isAppMetaConfigDataAvailable) {
      appMetaConfigRequest();
    }
  }

  /*
    TODO: Initialize UAAppContext
    1. Initialize context username
    2. Initialize context token
    3. Initialize context map config
    4. Initialize context localization config
    5. Initialize context root config
  */
  appMetaConfigRequest() async {
    if (_isAppMetaConfigDataAvailable) return;
    appMetaConfigBloc.fetchAppMetaConfig();

    appMetaConfigBloc.appMetaConfig.listen(
      (appMetaConfig) async {
        if (appMetaConfig != null) {
          _isAppMetaConfigDataAvailable = true;
          await redirectFromSplashScreen();
        } else {
          logger
              .e("could not fetch appMetaconfig :: checkInternetConnectivity");
          await _checkInternetConnectivity();
          bool hasInternet = await NetworkUtils().hasActiveInternet();
          if (!hasInternet) {
            Future.delayed(const Duration(milliseconds: 5000), () {
              SystemChannels.platform.invokeMethod('SystemNavigator.pop');
            });
          }
        }
      },
    );
  }

  _fetchLocalizationConfig() {
    localizationBloc.fetchLocalizationConfig();
    localizationBloc.localizationConfigStream
        .listen((LocalizationConfig localizationConfig) {
      if (localizationConfig != null) {
        // TODO : Save to DB
        _fetchRootConfig();
      }
//      else {
//        // TODO : Log error
//        Toast.show("Something went wrong. Please try again!", context,
//            duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
//      }
    });
  }

  _fetchRootConfig() {
    rootConfigBloc.fetchRootConfig();
    rootConfigBloc.rootConfigStream.listen((RootConfig rootConfig) {
      if (rootConfig != null) {
        // TODO : Save to DB
        _fetchMapConfig();
      }
    });
  }

  _fetchMapConfig() {
    mapConfigBloc.fetchMapConfig();
    mapConfigBloc.mapConfigStream.listen((mapConfig) {
      if (mapConfig != null) {
        homeScreenHelper.skipHomeScreen(context);
      }
    });
  }

  _setLoginPreferences() async {
    String userName = await sharedPreferenceUtil
        .getStringPreference(CommonConstants.USERNAME_SHARED_PREFERENCE);
    UAAppContext.getInstance().userID = userName;

    String token = await sharedPreferenceUtil
        .getStringPreference(CommonConstants.TOKEN_SHARED_PREFERENCE);

    UAAppContext.getInstance().token = token;
    UAAppContext.getInstance().isLoggedIn = true;
    try {
      LocalizationConfigProvider localizationConfigProvider =
          LocalizationConfigProvider.getInstance();
      await localizationConfigProvider.initLocalizationConfig();
      RootConfigProvider rootConfigProvider = RootConfigProvider.getInstance();
      await rootConfigProvider.initRootConfig();
      MapConfigProvider mapConfigProvider = MapConfigProvider.getInstance();
      await mapConfigProvider.initMapConfig();
      homeScreenHelper.skipHomeScreen(context);
    } catch (e) {
      logger.e("Some exception occured while fetching data");
    }
  }

  _navigateToLoginScreen() {
    ScreenNavigateUtils().navigateToLoginScreen(context, true, false);
  }

  _checkInternetConnectivity() async {
    internetSubscription =
        eventBus.on<CheckInternetEvent>().listen((event) async {
      if (event.isOnline) {
        // Connected to internet
        appMetaConfigRequest();
//        await redirectFromSplashScreen();
      } else {
        // No active internet connection
        Toast.show(CommonConstants.CHECK_INTERNET_CONNECTION, context,
            duration: 5, gravity: Toast.BOTTOM);
        Future.delayed(const Duration(milliseconds: 5000), () {
          SystemChannels.platform.invokeMethod('SystemNavigator.pop');
        });
      }
    });
  }

  @override
  void dispose() {
    super.dispose();
    appMetaConfigBloc.dispose();
    rootConfigBloc.dispose();
    localizationBloc.dispose();
    mapConfigBloc.dispose();
    if (internetSubscription != null) {
      internetSubscription.cancel();
    }
  }

  @override
  void initState() {
    super.initState();
    UAAppContext.getInstance().context = context;

    Timer(Duration(seconds: 3), () {
      _isAvailableToRedirect = true;
      redirectFromSplashScreen();
    });

    sharedPreferenceUtil
        .getBoolPreference(CommonConstants.IS_LOGGED_IN_SHARED_PREFERENCE)
        .then((isLoggedIn) {
      _isLoggedIn = isLoggedIn;
      appMetaConfigRequest();
    });
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Scaffold(
        body: Center(
          child: Column(
            children: <Widget>[
              Expanded(
                flex: 2,
                child: Text(""),
              ),
              ImageBanner(
                imagePath: (CommonConstants.DEFAULT_THEME == false)
                    ? CommonConstants.APP_INVERTED_LOGO
                    : CommonConstants.APP_LOGO,
                imageHeight: 300.0,
              ),
              Expanded(
                flex: 2,
                child: Column(
                  children: <Widget>[
                    Text(
                      AppTranslations.of(context).text("app_name"),
                      style: CommonConstants.DEFAULT_THEME == true
                          ? UniappTextTheme.largeInvertedHeader
                          : UniappTextTheme.largeHeader,
                      textScaleFactor: 1.25,
                    ),
                    _isLoading
                        ? Center(
                            child: Padding(
                              padding: const EdgeInsets.all(16.0),
                              child: CircularProgressIndicator(),
                            ),
                          )
                        : EmptyContainer(),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
