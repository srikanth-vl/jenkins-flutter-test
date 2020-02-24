package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vassar.unifiedapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppMetaData {
    @JsonProperty("splashscreenproperties")
    public SplashScreenProperties mSplashScreenProperties;
    @JsonProperty("colorscheme")
    public AppColorScheme mAppColorScheme;
    @JsonProperty("gridcolumns")
    public String mGridColumns;
    @JsonProperty("version")
    public String mSplashConfigVersion;
    @JsonProperty("user-name-type")
    public String mUserNameType;
    @JsonProperty("sub-title")
    public String mSubTitle;
    @JsonProperty("syncvisible")
    public boolean mSyncVisible;
    @JsonProperty("syncinterval")
    public Integer mSyncInterval;
    @JsonProperty("title")
    public String mTitle;
    @JsonProperty("urlendpoints")
    public URLEndpoints mUrlEndpoints;
    @JsonProperty("sort_type")
    public String mSortType;
    @JsonProperty("service_frequency")
    public Map<String, Long> mServiceFrequency; // Frequency at which each service is called in the background sync
    public Long mServerSessionExpiry = Constants.SERVER_SESSION_EXPIRY; // By default set to 30 days
    @JsonProperty("currenttime")
    public long mCurrentServerTime;
    @JsonProperty("retries")
    public int mRetries;
    @JsonProperty("mediaretries")
    public int mMediaRetries;
    @JsonProperty("media_in_parts")
    public Boolean mMediaInParts;
    @JsonProperty("media_packet_size")
    public Integer mMediaPacketSize;
    @JsonProperty("media_max_file_size")
    public Integer mMediaMaxFileSize;
    @JsonProperty("onstartup")
    public List<OnStartupAction> onStartUp;
    @JsonProperty("force_update")
    public Boolean mEnableForceUpdate = false;
    @JsonProperty("map_download_url")
    public String mMapDownloadBaseUrl;
    @JsonProperty("accept_data_submission_from_older_app")
    public Boolean acceptOlderAppData;
    @JsonProperty("current_playstore_app_version")
    public String currentVersion;
    @JsonProperty("enabled_languages")
    public List<LanguageInfo> enabledLanguages;
    @JsonProperty("profile_config")
    public List<Header> mProfileConfig;
    @JsonProperty("settings_page_enabled")
    public Boolean mSettingsPageEnabled;
    @JsonProperty("map_enabled")
    public Boolean mMapEnabled;
    @JsonProperty("filter_enabled")
    public Boolean mFilterEnabled;

    public long getServerFrequency(String key) {
        if (mServiceFrequency == null || mServiceFrequency.isEmpty()) {
            return Constants.DEFAULT_SERVICE_FREQUENCY; // in ms
        }
        if(mServiceFrequency.get(key) ==  null) {
            return Constants.DEFAULT_SERVICE_FREQUENCY;
        }
        return mServiceFrequency.get(key);
    }

    public long getMediaSyncFrequency(String key) {
        if (mServiceFrequency == null || mServiceFrequency.isEmpty() || mServiceFrequency.get(key) == null) {
            return Constants.MEDIA_SYNC_INTERVAL; // in ms
        }
        return mServiceFrequency.get(key);
    }

    public int getmMediaRetries() {

        if (mMediaRetries <= 0) {
            return Constants.DEFAULT_RETRIES;
        }
        return mMediaRetries;
    }

    @Override
    public String toString() {
        return "AppMetaData{" +
                "mSplashScreenProperties=" + mSplashScreenProperties +
                ", mAppColorScheme=" + mAppColorScheme +
                ", mGridColumns='" + mGridColumns + '\'' +
                ", mSplashConfigVersion='" + mSplashConfigVersion + '\'' +
                ", mUserNameType='" + mUserNameType + '\'' +
                ", mSubTitle='" + mSubTitle + '\'' +
                ", mSyncVisible=" + mSyncVisible +
                ", mSyncInterval=" + mSyncInterval +
                ", mTitle='" + mTitle + '\'' +
                ", mUrlEndpoints=" + mUrlEndpoints +
                ", mSortType='" + mSortType + '\'' +
                ", mServiceFrequency=" + mServiceFrequency +
                ", mServerSessionExpiry=" + mServerSessionExpiry +
                ", mCurrentServerTime=" + mCurrentServerTime +
                ", mRetries=" + mRetries +
                ", mMediaRetries=" + mMediaRetries +
                ", mMediaInParts=" + mMediaInParts +
                ", mMediaPacketSize=" + mMediaPacketSize +
                ", mMediaMaxFileSize=" + mMediaMaxFileSize +
                ", onStartUp=" + onStartUp +
                ", mEnableForceUpdate=" + mEnableForceUpdate +
                ", mMapDownloadBaseUrl='" + mMapDownloadBaseUrl + '\'' +
                ", acceptOlderAppData=" + acceptOlderAppData +
                ", currentVersion='" + currentVersion + '\'' +
                ", enabledLangauges=" + enabledLanguages +
                ", profileConfig=" + mProfileConfig +
                ", settingsPageEnabled=" + mSettingsPageEnabled +
                ", mapEnabled=" +  mMapEnabled +
                ", filterEnabled=" +  mFilterEnabled +
                 '}';
    }

    public List<LanguageInfo> getEnabledLanguages() {
        if(enabledLanguages == null) {
            List<LanguageInfo> languageInfos = new ArrayList<>();
            LanguageInfo info =  new LanguageInfo();
            info.setLocale("en");
            info.setName("English");
            languageInfos.add(info);
            enabledLanguages = languageInfos;
        }
        return enabledLanguages;
    }

    public void setEnabledLanguages(List<LanguageInfo> enabledLanguages) {
        this.enabledLanguages = enabledLanguages;
    }

    public void setProfileConfig(List<Header> mProfileConfig){
        this.mProfileConfig=mProfileConfig;
    }
    public List<Header> getProfileConfig(){
        if(this.mProfileConfig == null) {
            return  new ArrayList<>();
        }
        return this.mProfileConfig;
    }
}
