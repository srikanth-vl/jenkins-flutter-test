import '../../models/sub_app.dart';
import '../../utils/common_utils.dart';
import './form_values.dart';

class AppbarParameters {
  // Outside Actions:
  bool showSync = false;
  bool showSearchView = false;
  bool showFilter = false;

  // Vertical ellipsis:
  bool showPreferences = false;
  bool showDownloads = false;
  bool showLogout = false;

  AppbarParameters({
    this.showSync,
    this.showFilter,
    this.showSearchView,
    this.showPreferences,
    this.showDownloads,
    this.showLogout,
  });
}

class AppbarParameterMapping {
  bool isFilterEnabled() {
    String appId = formMap.appId;
    if (appId != null && appId.isNotEmpty) {
      SubApp subapp = CommonUtils.getSubAppFromConfig(appId);

      if (subapp != null &&
          subapp.filterEnabled != null &&
          subapp.filterEnabled &&
          subapp.filteringForm != null) {
        return true;
      }
    }

    return false;
  }

  bool isSearchEnabled() {
    String appId = formMap.appId;
    if (appId != null && appId.isNotEmpty) {
      SubApp subapp = CommonUtils.getSubAppFromConfig(appId);

      if (subapp != null &&
          subapp.searchEnabled != null &&
          subapp.searchEnabled == true) {
        return true;
      }
    }

    return false;
  }

  getAppbarMapping(String screenName) {
    if (screenName != null &&
        screenName.isNotEmpty &&
        appBarMapping[screenName] != null) {
      if (screenName == 'ProjectListScreen') {
        appBarMapping[screenName].showSearchView = isSearchEnabled();
        appBarMapping[screenName].showFilter = isFilterEnabled();
      }

      return appBarMapping[screenName];
    } else {
      return AppbarParameters(
        showSync: false,
        showFilter: false,
        showSearchView: false,
        showPreferences: false,
        showDownloads: false,
        showLogout: false,
      );
    }
  }

  bool getActionCount(String screenName) {
    int count = 1; //Initialised as 1, because of vertical ellipsis.
    AppbarParameters appParams = getAppbarMapping(screenName);

    /// TODO: remove this hardcoding!
    /// Need to migrate the Map/List action logic here.
    if (screenName == "ProjectListScreen") count++;

    if (appParams.showSync == true) count++;
    if (appParams.showFilter == true) count++;
    if (appParams.showSearchView == true) count++;

    if (count > 3) return true;
    return false;
  }

  Map<String, AppbarParameters> appBarMapping = {
    "CameraScreen": AppbarParameters(
      showSync: false,
      showFilter: false,
      showSearchView: false,
      showPreferences: false,
      showDownloads: false,
      showLogout: true,
    ),
    "DownloadScreen": AppbarParameters(
      showSync: false,
      showFilter: false,
      showSearchView: false,
      showPreferences: true,
      showDownloads: false,
      showLogout: true,
    ),
    "FilterScreen": AppbarParameters(
      showSync: false,
      showFilter: false,
      showSearchView: false,
      showPreferences: true,
      showDownloads: false,
      showLogout: true,
    ),
    "HomeScreen": AppbarParameters(
      showSync: true,
      showFilter: false,
      showSearchView: false,
      showPreferences: true,
      showDownloads: true,
      showLogout: true,
    ),
    "ImagePreviewScreen": AppbarParameters(
      showSync: false,
      showFilter: false,
      showSearchView: false,
      showPreferences: false,
      showDownloads: false,
      showLogout: true,
    ),
    "NavigationScreen": AppbarParameters(
      showSync: false,
      showFilter: false,
      showSearchView: false,
      showPreferences: false,
      showDownloads: false,
      showLogout: true,
    ),
    "ProjectGroupingScreen": AppbarParameters(
      showSync: true,
      showFilter: false,
      showSearchView: false,
      showPreferences: true,
      showDownloads: true,
      showLogout: true,
    ),
    "ProjectListScreen": AppbarParameters(
      showSync: true,
      showSearchView: false,
      showPreferences: true,
      showDownloads: true,
      showLogout: true,
    ),
    "ProjectFormScreen": AppbarParameters(
      showSync: false,
      showFilter: false,
      showSearchView: false,
      showPreferences: true,
      showDownloads: false,
      showLogout: true,
    ),
    "VideoPreviewScreen": AppbarParameters(
      showSync: false,
      showFilter: false,
      showSearchView: false,
      showPreferences: false,
      showDownloads: false,
      showLogout: true,
    ),
  };
}
