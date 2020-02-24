import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:toast/toast.dart';
import 'dart:async';

import '../themes/color_theme.dart';
import '../themes/uniapp_css.dart';
import '../../localization/app_translations.dart';
import '../../db/databaseHelper.dart';
import '../../db/models/user_meta_data_table.dart';
import '../../resources/localization_config_provider.dart';
import '../../event/event_utils.dart';
import '../../blocs/localization_config_bloc.dart';
import '../../blocs/login_bloc.dart';
import '../../blocs/map_config_bloc.dart';
import '../../blocs/root_config_bloc.dart';
import '../../models/localization_config.dart';
import '../../models/map_config.dart';
import '../../models/root_config.dart';
import '../../ua_app_context.dart';
import '../../ui/helpers/home_screen_helper.dart';
import '../../utils/screen_navigate_utils.dart';
import '../../utils/common_constants.dart';
import '../../utils/shared_preference_util.dart';
import '../../utils/network_utils.dart';
import '../../resources/map_config_provider.dart';
import '../../resources/root_config_provider.dart';

class LoginWidget extends StatefulWidget {
  final String title;
  bool _obscureText = true;
  DatabaseHelper databaseHelper;

  LoginWidget({Key key, this.title}) : super(key: key);

  @override
  _LoginWidgetState createState() => _LoginWidgetState();
}

class _LoginWidgetState extends State<LoginWidget> {
  TextStyle style = TextStyle(fontFamily: 'Montserrat', fontSize: 20.0);
  bool isProgressRunning = false;
  bool _isDisposed = false;
  final usernameController = TextEditingController();
  final passwordController = TextEditingController();
  StreamSubscription<CheckInternetEvent> internetSubscription;
  final databaseHelper = DatabaseHelper();
  LoginBloc loginBloc = LoginBloc();
  RootConfigBloc rootConfigBloc = RootConfigBloc();
  LocalizationBloc localizationBloc = LocalizationBloc();
  MapConfigBloc mapConfigBloc = MapConfigBloc();
  HomeScreenHelper homeScreenHelper = HomeScreenHelper();

  @override
  void dispose() {
    // Clean up the controller when the widget is disposed.
    usernameController.dispose();
    passwordController.dispose();
    if (internetSubscription != null) {
      internetSubscription.cancel();
    }
    loginBloc.dispose();
    rootConfigBloc.dispose();
    localizationBloc.dispose();
    mapConfigBloc.dispose();
    super.dispose();
    _isDisposed = true;
  }

  @override
  Widget build(BuildContext context) {
    final emailField = TextField(
      controller: usernameController,
      decoration: new InputDecoration(
        prefixIcon: Icon(Icons.person),
        labelText: AppTranslations.of(context).text("username"),
        fillColor: Colors.white,
        filled: true,
        border: new OutlineInputBorder(
          borderRadius: new BorderRadius.circular(4.0),
          borderSide: new BorderSide(),
        ),
      ),
      keyboardType: TextInputType.emailAddress,
      style: new TextStyle(
        fontFamily: "Poppins",
      ),
    );

    final passwordField = TextField(
      obscureText: widget._obscureText,
      controller: passwordController,
      decoration: new InputDecoration(
        prefixIcon: Icon(Icons.lock),
        suffixIcon: new GestureDetector(
          onTap: () {
            setState(() {
              widget._obscureText = !widget._obscureText;
            });
          },
          child: new Icon(
              widget._obscureText ? Icons.visibility : Icons.visibility_off),
        ),
        labelText: AppTranslations.of(context).text("password"),
        fillColor: Colors.white,
        filled: true,
        border: new OutlineInputBorder(
          borderRadius: new BorderRadius.circular(4.0),
          borderSide: new BorderSide(),
        ),
      ),
      keyboardType: TextInputType.visiblePassword,
      style: new TextStyle(
        fontFamily: "Poppins",
      ),
    );

    final loginButon = Material(
      elevation: 4.0,
      borderRadius: BorderRadius.circular(4.0),
      color: Theme.of(context).accentColor,
      child: MaterialButton(
        minWidth: MediaQuery.of(context).size.width,
        padding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
        onPressed: () {
          String username = usernameController.text;
          String password = passwordController.text;

          if (username != null &&
              username.isNotEmpty &&
              password != null &&
              password.isNotEmpty) {
            // Call AuthenticationService
            _checkInternetConnectivity(username, password);
            _authenticateLogin(username, password);
          } else {
            // TODO : Log error
            Toast.show(
                AppTranslations.of(context).text("enter_user_credentials"),
                context,
                duration: Toast.LENGTH_SHORT,
                gravity: Toast.BOTTOM);
          }
        },
        child: Text(
          AppTranslations.of(context).text("login").toUpperCase(),
          textAlign: TextAlign.center,
          style: UniappCSS.largeInvertedHeader,
        ),
      ),
    );

    final loginAsGuestButton = Material(
      elevation: 4.0,
      borderRadius: BorderRadius.circular(4.0),
      color: Theme.of(context).accentColor,
      child: MaterialButton(
        minWidth: MediaQuery.of(context).size.width,
        padding: EdgeInsets.fromLTRB(20.0, 15.0, 20.0, 15.0),
        onPressed: () {
          _loginAsGuest();
        },
        child: Text(
          AppTranslations.of(context).text("Login_As_Guest").toUpperCase(),
          textAlign: TextAlign.center,
          style: UniappCSS.smallInvertedHeader,
        ),
      ),
    );

    final forgotPasswordLink = InkWell(
      child: Text(
        AppTranslations.of(context).text('forgot_password'),
        style: TextStyle(
          fontSize: 14,
          color: UniappColorTheme.clickableLinkColor,
        ),
      ),
      onTap: () {
        _navigateToGetOTPScreen();
      },
      // Text('Click Here', style: TextStyle(color: Colors.indigo, fontSize: 16),),
    );

    return Center(
      child: Container(
        child: Padding(
          padding: const EdgeInsets.all(36.0),
          child: Column(
            mainAxisSize: MainAxisSize.max,
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              SizedBox(
                height: 120.0,
                child: Image.asset(
                  (CommonConstants.DEFAULT_THEME == false)
                      ? CommonConstants.APP_INVERTED_LOGO
                      : CommonConstants.APP_LOGO,
                  fit: BoxFit.contain,
                ),
              ),
              // SizedBox(height: 24.0),
              // Text(
              //       "BLUIS",
              //       // textScaleFactor: 1.5,
              //       style: TextStyle(
              //         color: Colors.white,
              //         fontSize: 32.0,
              //         fontWeight: FontWeight.w800,
              //       ),
              //     ),
              SizedBox(height: 64.0),
              emailField,
              SizedBox(height: 16.0),
              passwordField,
              SizedBox(
                height: 48.0,
              ),
              loginButon,
              SizedBox(
                height: 16.0,
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                //   mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  // guestLoginLink,
                  forgotPasswordLink,
                ],
              ),

              SizedBox(
                height: 32.0,
              ),
              Row(
                children: <Widget>[
                  Expanded(
                    child: Divider(
                      height: 8.0,
                      color: UniappColorTheme.clickableLinkColor,
                    ),
                  ),
                  Text(
                    "    ${AppTranslations.of(context).text("Or").toUpperCase()}    ",
                    style: TextStyle(
                      color: UniappColorTheme.clickableLinkColor,
                      fontSize: 16.0,
                    ),
                  ),
                  Expanded(
                    child: Divider(
                      height: 8.0,
                      color: UniappColorTheme.clickableLinkColor,
                    ),
                  ),
                ],
              ),
              SizedBox(
                height: 32.0,
              ),

              loginAsGuestButton,

              SizedBox(
                height: 16.0,
              ),
              if (isProgressRunning)
                Center(child: new CircularProgressIndicator()),
            ],
          ),
        ),
      ),
    );
  }

  _showProgressBar() {
    if (_isDisposed) return;
    setState(() {
      isProgressRunning = true;
    });
  }

  _hideProgressBar() {
    if (_isDisposed)
      return setState(() {
        isProgressRunning = false;
      });
  }

  _loginAsGuest() {
    _authenticateLogin(
        CommonConstants.GUEST_USER_ID, CommonConstants.GUEST_PASSWORD);
  }

  _authenticateLogin(String username, String password) async {
    bool isOnline = await networkUtils.hasActiveInternet();
    if (isOnline) {
      _showProgressBar();
      Scaffold.of(context).showSnackBar(
        SnackBar(
          content: Text(AppTranslations.of(context).text("trying_to_login")),
        ),
      );
      loginBloc.authenticateLoginCredentials(username, password);
      loginBloc.loginAuthenticationStream
          .listen((UserMetaDataTable userMetaDataTable) async {
        if (userMetaDataTable != null &&
            userMetaDataTable.userId != null &&
            userMetaDataTable.userId.isNotEmpty &&
            userMetaDataTable.userId == username &&
            userMetaDataTable.token != null &&
            userMetaDataTable.token.isNotEmpty) {
          // saving login details to DB
          await _saveLoginDetailsToDB(userMetaDataTable);
          UAAppContext.getInstance().userID = userMetaDataTable.userId;
          UAAppContext.getInstance().token = userMetaDataTable.token;
          UAAppContext.getInstance().isLoggedIn = true;
          //add data in UAAppContext and set Shared Preferences
          await _setUAAContextAndSharedPrefences(userMetaDataTable);
          await _fetchAllConfig();
        }
      });
    } else {
      Toast.show("${CommonConstants.NETWORK_UNAVAILABLE}", context,
          duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
      //The user is offline, offline login
      UserMetaDataTable userMetaDataTable =
          await databaseHelper.getUserMeta(username);
      if (userMetaDataTable != null) {
        //Offline data available
        if (username.compareTo(userMetaDataTable.userId) == 0 &&
            password.compareTo(userMetaDataTable.password) == 0) {
          // Offline credentials m
          // atch
          await _setUAAContextAndSharedPrefences(userMetaDataTable);

          // Navigate to Home Screen
          _navigateToHomeScreen();
        } else {
          //Offline credentials do not match
          Toast.show(
              AppTranslations.of(context)
                  .text("offline_credentials_do_not_match"),
              context,
              duration: Toast.LENGTH_SHORT,
              gravity: Toast.BOTTOM);
        }
      } else {
        //Offline data doesn't exists for this user
        Toast.show(
            AppTranslations.of(context)
                .text("offline_data_do_not_exists_for_this_user"),
            context,
            duration: Toast.LENGTH_SHORT,
            gravity: Toast.BOTTOM);
      }
    }
  }

// TODO: Unused function. Figure out why?
  _fetchLocalizationConfig(String username, String token) {
    localizationBloc.fetchLocalizationConfig();
    localizationBloc.localizationConfigStream
        .listen((LocalizationConfig localizationConfig) {
      if (localizationConfig != null) {
        // TODO : Save to DB

        _fetchRootConfig();
      } else {
        // TODO : Log error
        Toast.show(
            AppTranslations.of(context).text("SOMETHING_WENT_WRONG"), context,
            duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
      }
    });
  }

  _fetchRootConfig() {
    rootConfigBloc.fetchRootConfig();
    rootConfigBloc.rootConfigStream.listen((RootConfig rootConfig) {
      if (rootConfig != null) {
        _fetchMapConfig();
      }
    });
  }

  _fetchMapConfig() {
    mapConfigBloc.fetchMapConfig();
    mapConfigBloc.mapConfigStream.listen((MapConfig mapConfig) {
      if (mapConfig != null) {
        if (context != null) {
          Toast.show(
              AppTranslations.of(context).text("login_complete"), context,
              duration: Toast.LENGTH_SHORT, gravity: Toast.TOP);
        }
        // Navigate to Home Screen
        _navigateToHomeScreen();
      }
//      else {
//        // TODO : Log error
//        Toast.show("Something went wrong. Please try again!", context,
//            duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
//      }
    });
  }

  _setSharedPreferencesPostLogin(String username, String token) async {
    await sharedPreferenceUtil.setPreferenceValue(
        CommonConstants.USERNAME_SHARED_PREFERENCE,
        username,
        CommonConstants.PREFERENCE_TYPE_STRING);
    await sharedPreferenceUtil.setPreferenceValue(
        CommonConstants.TOKEN_SHARED_PREFERENCE,
        token,
        CommonConstants.PREFERENCE_TYPE_STRING);
    await sharedPreferenceUtil.setPreferenceValue(
        CommonConstants.IS_LOGGED_IN_SHARED_PREFERENCE,
        true,
        CommonConstants.PREFERENCE_TYPE_BOOL);
  }

  _navigateToHomeScreen() {
    ScreenNavigateUtils().navigateToHomeScreen(context, true);
  }

  _navigateToGetOTPScreen() {
    ScreenNavigateUtils().navigateToGetOTPScreen(context);
  }

  _checkInternetConnectivity(String username, String password) async {
    internetSubscription =
        eventBus.on<CheckInternetEvent>().listen((event) async {
      if (event.isOnline) {
        // Connected to internet
        await _authenticateLogin(username, password);
      } else {
        // No active internet connection
        Toast.show(CommonConstants.CHECK_INTERNET_CONNECTION, context,
            duration: 5, gravity: Toast.BOTTOM);
        if (isProgressRunning) {
          _hideProgressBar();
        }
      }
    });
  }

  void _saveLoginDetailsToDB(UserMetaDataTable userMetaDataTable) async {
    await databaseHelper.insertUserMeta(userMetaDataTable);
    UserMetaDataTable userMetaDataTableNew =
        await databaseHelper.getUserMeta(userMetaDataTable.userId);
    print("UserMetaDatatable ${userMetaDataTableNew.toMap()}");
  }

  void _setUAAContextAndSharedPrefences(
      UserMetaDataTable userMetaDataTable) async {
    UAAppContext.getInstance().userID = userMetaDataTable.userId;
    UAAppContext.getInstance().token = userMetaDataTable.token;
    // Set Shared Preference
    await _setSharedPreferencesPostLogin(
        userMetaDataTable.userId, userMetaDataTable.token);
  }

  _fetchAllConfig() async {
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
      print("Something went wrong! $e");
      Toast.show(
          AppTranslations.of(context).text("SOMETHING_WENT_WRONG"), context,
          duration: Toast.LENGTH_SHORT, gravity: Toast.BOTTOM);
    }
  }
}
