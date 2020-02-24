import 'ui/helpers/ask_for_permissions.dart';
import 'utils/location_util.dart';
import 'package:permission_handler/permission_handler.dart';

import 'db/models/project_master_data_table.dart';
import 'package:geolocator/geolocator.dart';

import 'event/event_utils.dart';
import 'localization/localization_utils.dart';
import 'models/enabled_language.dart';
import 'utils/common_constants.dart';
import 'utils/network_utils.dart';
import 'package:connectivity/connectivity.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:io';

import 'db/databaseHelper.dart';
import 'models/app_meta_data_config.dart';
import 'dart:async';
import 'models/map_config.dart';
import 'models/root_config.dart';
import 'utils/shared_preference_util.dart';

class UAAppContext {
  static final UAAppContext ourInstance = new UAAppContext();

  static UAAppContext getInstance() {
    return ourInstance;
  }

  var sharedPreferences;
  DatabaseHelper _unifiedAppDBHelper;
  AppMetaDataConfig _appMDConfig;
  RootConfig _rootConfig;
  String _userID;
  String _token;
  List<String> _downloadingFiles = List();
  MapConfig _mapConfig;
  BuildContext _context;
  Directory _appDir;
  Connectivity _connectivity = new Connectivity();
  StreamSubscription<ConnectivityResult> _subscription;
  static bool isOnline;
  bool isLoggedIn = false;
  Position _currentLoc = null;
  List<ProjectMasterDataTable> _projectList = List();
  Map<String, String> _filterSelectedValueMap = Map();
  String _selectedAppId;

  BuildContext get context => _context;

  set context(BuildContext value) {
    _context = value;
  }

  MapConfig get mapConfig => _mapConfig;

  set mapConfig(MapConfig value) {
    _mapConfig = value;
  }

  StreamSubscription positionStream;
  UAAppContext() {
    initUAContext();

    // getApplicationDir
    if (Platform.isAndroid) {
      getExternalStorageDirectory().then((Directory directory) {
        _appDir = directory;
      });
    } else if (Platform.isIOS) {
      getApplicationDocumentsDirectory().then((Directory dir) {
        _appDir = dir;
      });
    }
    NetworkUtils().hasActiveInternet().then((hasInternet) {
      isOnline = hasInternet;
      checkInternetConnection();
    });

    // Do Nothing here for now
    initDBHelper();
  }

  void initUAContext() {
    if (Platform.isAndroid) {
      justWait(numberOfSeconds: 0);
    } else {
      justWait(numberOfSeconds: 1);
    }
  }

  void justWait({@required int numberOfSeconds}) async {
    await Future.delayed(Duration(seconds: numberOfSeconds));
    List<PermissionGroup> permissionGroups = new List<PermissionGroup>();

    if (Platform.isAndroid) {
      permissionGroups.add(PermissionGroup.storage);
    }
    permissionGroups.add(PermissionGroup.locationWhenInUse);
    permissionGroups.add(PermissionGroup.camera);
    await AskForPermission().checkAndGetPermissions(permissionGroups);
    LocationUtil().getLocationPermission(context);
    positionStreamMethod();
    return;
  }

  void positionStreamMethod() {
    var geolocator = Geolocator();
    var locationOptions =
        LocationOptions(accuracy: LocationAccuracy.high, distanceFilter: 10);
    positionStream = geolocator
        .getPositionStream(locationOptions)
        .listen((Position position) {
      _currentLoc = position;
    });
  }

  void initDBHelper() {
    this._unifiedAppDBHelper = DatabaseHelper();
    // print("XXX - Initialized DB Helper");
  }

  DatabaseHelper get unifiedAppDBHelper => _unifiedAppDBHelper;

  AppMetaDataConfig get appMDConfig => _appMDConfig;

  RootConfig get rootConfig => _rootConfig;

  String get userID => _userID;

  String get token => _token;

  List<String> get downloadingFiles => _downloadingFiles;

  Position get currentLoc => _currentLoc;

  List<ProjectMasterDataTable> get projectList => _projectList;

  Map<String, String> get filterSelectedValueMap => _filterSelectedValueMap;

  set token(String value) {
    _token = value;
  }

  set userID(String value) {
    _userID = value;
  }

  set downloadingFiles(List<String> value) {
    _downloadingFiles = value;
  }

  set rootConfig(RootConfig value) {
    _rootConfig = value;
  }

  set appMDConfig(AppMetaDataConfig value) {
    _appMDConfig = value;
  }

  set unifiedAppDBHelper(DatabaseHelper value) {
    _unifiedAppDBHelper = value;
  }

  Directory get appDir => _appDir;

  set appDir(Directory value) {
    _appDir = value;
  }

  set currentLoc(Position loc) {
    _currentLoc = loc;
  }

  set projectList(List<ProjectMasterDataTable> projList) {
    _projectList = projList;
  }

  set filterSelectedValueMap(Map<String, String> filterMap) {
    _filterSelectedValueMap = filterMap;
  }

  String get selectedAppId => _selectedAppId;

  set selectedAppId(String value) {
    _selectedAppId = value;
  }

  checkInternetConnection() {
    _subscription = _connectivity.onConnectivityChanged.listen((result) {
      if (result == ConnectivityResult.mobile ||
          result == ConnectivityResult.wifi) {
        //     Connected to a mobile network or wifi network

        if (isOnline == false) {
          isOnline = true;
          eventBus.fire(CheckInternetEvent(isOnline));
        }
      } else {
        // No active internet connection
        if (isOnline == true) {
          isOnline = false;
          eventBus.fire(CheckInternetEvent(isOnline));
        }
      }
    });
  }

  final List<String> supportedLanguages = [
    "English",
    "Hindi",
  ];

  final List<String> supportedLanguagesCodes = [
    "en",
    "hi",
  ];

  //returns the list of supported Locales
//  Iterable<Locale> supportedLocales() =>
//      supportedLanguagesCodes.map<Locale>((language) => Locale(language, ""));

  //function to be invoked when changing the language
  LocaleChangeCallback onLocaleChanged;

  void setLocalizationJson(Map localizationConfigJSON) {
    if (localizationConfigJSON == null) {
      mLocalizationJson = LocalizationUtils().getData();
    } else {
      mLocalizationJson = localizationConfigJSON;
    }
  }

  Map<String, dynamic> mLocalizationJson = new Map<String, dynamic>();

  Map<String, dynamic> getLocalization() {
    String locale = _locale;
    Map<String, dynamic> localizationJson = new Map<String, dynamic>();
    if (mLocalizationJson != null && mLocalizationJson.containsKey(locale)) {
      localizationJson = mLocalizationJson[locale];
    }
    return localizationJson;
  }

  void changeLanguage(String key) {
    if (key == null || key.isEmpty) {
      _locale = "en";
    } else {
      _locale = key;
    }
    addLocale(key);
  }

  String _locale = 'en';

  String get locale => _locale;

  set locale(String value) {
    _locale = value;
  }

  void addLocale(String locale) {
    sharedPreferenceUtil.setPreferenceValue(
        CommonConstants.LOCALE_IN_PREFERENCE_KEY,
        locale == null || locale.isEmpty ? "en" : locale,
        CommonConstants.PREFERENCE_TYPE_STRING);
  }

  Future<String> getLocale() async {
    String locale = 'en';
    locale = await sharedPreferenceUtil
        .getStringPreference(CommonConstants.LOCALE_IN_PREFERENCE_KEY);
    locale = locale == null || locale.isEmpty ? "en" : locale;
    _locale = locale;
    return locale;
  }

//  void  changeStaticLocalizationConfiguration(String languageToLoad) {
//    Resources res = context.getResources(); // Change locale settings in the app.
//    DisplayMetrics dm = res.getDisplayMetrics();
//    android.content.res.Configuration conf = res.getConfiguration();
//    Locale locale = new Locale(languageToLoad);
//    Locale.setDefault(locale);
//    conf.setLocale(locale);
//    res.updateConfiguration(conf, dm);
//  }
  List<EnabledLanguage> getEnabledLangauge() {
    List<EnabledLanguage> languages = List();
    if (appMDConfig == null ||
        appMDConfig.enabledLanguages == null ||
        appMDConfig.enabledLanguages.isEmpty) {
      languages.add(EnabledLanguage(name: "English", locale: "en"));
      return languages;
    }
    return appMDConfig.enabledLanguages;
  }
}

typedef void LocaleChangeCallback(Locale locale);
