import 'color_scheme.dart';
import 'splash_screen_properties.dart';
import 'url_end_points.dart';
import 'server_frequency.dart';
import 'enabled_language.dart';
class AppMetaDataConfig {
  String userNameType;
  String subtitle;
  bool syncvisible;
  Colorscheme colorscheme;
  SplashScreenproperties splashscreenproperties;
  int syncinterval;
  String title;
  String gridcolumns;
  String version;
  Urlendpoints urlendpoints;
  String sortType;
  ServiceFrequency serviceFrequency;
  List<EnabledLanguage> enabledLanguages;
  int retries;
  int mediaretries;
  bool forceUpdate;
  String mapDownloadUrl;
  bool mediaInParts;
  int mediaPacketSize;
  int mediaMaxFileSize;
  bool mapEnabled;
  bool settingsPageEnabled;
  bool fetchEntityMetaData;

  AppMetaDataConfig(
      {this.userNameType,
        this.subtitle,
        this.syncvisible,
        this.colorscheme,
        this.splashscreenproperties,
        this.syncinterval,
        this.title,
        this.gridcolumns,
        this.version,
        this.urlendpoints,
        this.sortType,
        this.serviceFrequency,
        this.enabledLanguages,
        this.retries,
        this.mediaretries,
        this.forceUpdate,
        this.mapDownloadUrl,
        this.mediaInParts,
        this.mediaPacketSize,
        this.mediaMaxFileSize,
        this.mapEnabled,
        this.settingsPageEnabled,
        fetchEntityMetaData});

  AppMetaDataConfig.fromJson(Map<String, dynamic> json) {
    userNameType = json['user-name-type'];
    subtitle = json['subtitle'];
    syncvisible = json['syncvisible'];
    colorscheme = json['colorscheme'] != null
        ? new Colorscheme.fromJson(json['colorscheme'])
        : null;
    splashscreenproperties = json['splashscreenproperties'] != null
        ? new SplashScreenproperties.fromJson(json['splashscreenproperties'])
        : null;
    syncinterval = json['syncinterval'];
    title = json['title'];
    gridcolumns = json['gridcolumns'];
    version = json['version'];
    urlendpoints = json['urlendpoints'] != null
        ? new Urlendpoints.fromJson(json['urlendpoints'])
        : null;
    sortType = json['sort_type'];
    serviceFrequency = json['service_frequency'] != null
        ? new ServiceFrequency.fromJson(json['service_frequency'])
        : null;
    if (json['enabled_languages'] != null) {
      enabledLanguages = new List<EnabledLanguage>();
      json['enabled_languages'].forEach((v) {
        enabledLanguages.add(new EnabledLanguage.fromJson(v));
      });
    }
    retries = json['retries'];
    mediaretries = json['mediaretries'];
    forceUpdate = json['force_update'];
    mapDownloadUrl = json['map_download_url'];
    mediaInParts = json['media_in_parts'];
    mediaPacketSize = json['media_packet_size'];
    mediaMaxFileSize = json['media_max_file_size'];
    mapEnabled = json['map_enabled'];
    settingsPageEnabled = json['settings_page_enabled'];
    fetchEntityMetaData = json['fetch_entity_meta_data'] == null ? false : json['fetch_entity_meta_data'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['user-name-type'] = this.userNameType;
    data['subtitle'] = this.subtitle;
    data['syncvisible'] = this.syncvisible;
    if (this.colorscheme != null) {
      data['colorscheme'] = this.colorscheme.toJson();
    }
    if (this.splashscreenproperties != null) {
      data['splashscreenproperties'] = this.splashscreenproperties.toJson();
    }
    data['syncinterval'] = this.syncinterval;
    data['title'] = this.title;
    data['gridcolumns'] = this.gridcolumns;
    data['version'] = this.version;
    if (this.urlendpoints != null) {
      data['urlendpoints'] = this.urlendpoints.toJson();
    }
    data['sort_type'] = this.sortType;
    if (this.serviceFrequency != null) {
      data['service_frequency'] = this.serviceFrequency.toJson();
    }
    if (this.enabledLanguages != null) {
      data['enabled_languages'] =
          this.enabledLanguages.map((v) => v.toJson()).toList();
    }
    data['retries'] = this.retries;
    data['mediaretries'] = this.mediaretries;
    data['force_update'] = this.forceUpdate;
    data['map_download_url'] = this.mapDownloadUrl;
    data['media_in_parts'] = this.mediaInParts;
    data['media_packet_size'] = this.mediaPacketSize;
    data['media_max_file_size'] = this.mediaMaxFileSize;
    data['map_enabled'] = this.mapEnabled;
    data['settings_page_enabled'] = this.settingsPageEnabled;
    data['fetch_entity_meta_data'] = this.fetchEntityMetaData;
    return data;
  }
}