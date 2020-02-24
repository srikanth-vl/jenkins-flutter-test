package com.vassar.unifiedapp.context;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.db.UnifiedAppDBHelper;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.AppMetaData;
import com.vassar.unifiedapp.model.ConfigFile;
import com.vassar.unifiedapp.model.MapConfigurationV1;
import com.vassar.unifiedapp.model.Project;
import com.vassar.unifiedapp.model.ProjectList;
import com.vassar.unifiedapp.model.ProjectTypeModel;
import com.vassar.unifiedapp.model.RootConfig;
import com.vassar.unifiedapp.ui.SettingFragment;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.LocalizationUtils;
import com.vassar.unifiedapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class UAAppContext {

    private static final UAAppContext ourInstance = new UAAppContext();

    public static UAAppContext getInstance() {
        return ourInstance;
    }

    protected Context context;
    protected SharedPreferences sharedPreferences;
    protected UnifiedAppDBHelper unifiedAppDBHelper;
    protected AppMetaData appMDConfig;
    protected String userID;
    protected RootConfig rootConfig;
    protected MapConfigurationV1 mapConfig;

    protected ProjectList mProjectList;
    protected List<Project> mProjectListCache = new ArrayList<>();
    protected List<Project> mFilteredProjectListCache = new ArrayList<>();
    private ObjectMapper jsonObjectMapper = new ObjectMapper();

    protected JSONObject mLocalizationJson = new JSONObject();
    private UAAppContext() {
        // Do Nothing here for now
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    public void initDBHelper() {
        this.unifiedAppDBHelper = new UnifiedAppDBHelper(context);
        System.out.println("XXX - Initialized DB Helper");
    }

    public void initSharedPreferences() {
        this.sharedPreferences = context.getSharedPreferences(Constants.APP_PREFERENCES_KEY
                , MODE_PRIVATE);
        System.out.println("XXX - Initialized App Preferences");
        String locale = getLocale();
        setLocalizationJson(null);
        changeStaticLocalizationConfiguration(locale);

    }

    public UnifiedAppDBHelper getDBHelper() {
        return unifiedAppDBHelper;
    }

    public void setAppMDConfig(AppMetaData appMDConfig) {
        this.appMDConfig = appMDConfig;
    }

    public AppMetaData getAppMDConfig() {
        Utils.logInfo(LogTags.UA_APP_CONTEXT, "Get App meta config");
        if(this.appMDConfig == null ) {
            if(this.unifiedAppDBHelper == null) {
                this.initDBHelper();
            }
            Utils.logInfo(LogTags.UA_APP_CONTEXT, "Get App meta config from DB");
            ConfigFile appMetaConfigFile = this.unifiedAppDBHelper.getConfigFile(Constants.DEFAULT_USER_ID,
                    Constants.APP_META_CONFIG_DB_NAME);
            if (appMetaConfigFile != null && appMetaConfigFile.getConfigContent() != null && !appMetaConfigFile.getConfigContent().isEmpty()) {
                  AppMetaData appMetaConfigFromDB = null;
                  try {
                      appMetaConfigFromDB = jsonObjectMapper.readValue(appMetaConfigFile.getConfigContent(),AppMetaData.class);
                  }catch (IOException e) {
                      Utils.logError(LogTags.UA_APP_CONTEXT, "Could not create AppMetaData object from Json Object: "+ appMetaConfigFile.getConfigContent());
                      e.printStackTrace();
                  }
                this.appMDConfig = appMetaConfigFromDB;
            }
        }
        return this.appMDConfig;
    }

    public SharedPreferences getAppPreferences() {
        return this.sharedPreferences;
    }

    public void setAppPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        if(userID == null) {
            String userId = getAppPreferences().getString(Constants.USER_ID_PREFERENCE_KEY, Constants.USER_ID_PREFERENCE_DEFAULT);
            this.userID = userId;
        }
        return this.userID;
    }

    public String getToken() {
        String token = getAppPreferences().getString(Constants.USER_TOKEN_PREFERENCE_KEY, Constants.USER_TOKEN_PREFERENCE_DEFAULT);
        return token;
    }

    public void setRootConfig(RootConfig rootConfig) {
        this.rootConfig = rootConfig;
    }

    public void setMapConfig(MapConfigurationV1 mapConfig) {
        this.mapConfig = mapConfig;
    }

    public MapConfigurationV1 getMapConfig() {
        if(this.mapConfig == null ) {
            if(this.unifiedAppDBHelper == null) {
                this.initDBHelper();
            }
            if(userID == null) {
                String userId = getAppPreferences().getString(Constants.USER_ID_PREFERENCE_KEY, Constants.USER_ID_PREFERENCE_DEFAULT);
                this.setUserID(userId);
            }
            Utils.logInfo(LogTags.UA_APP_CONTEXT, "Get MapConfig config from DB for userId: "+ userID);
            ConfigFile mapConfigFile = this.unifiedAppDBHelper.getConfigFile(userID,
                    Constants.MAP_CONFIG_DB_NAME);
            if (mapConfigFile != null && mapConfigFile.getConfigContent() != null && !mapConfigFile.getConfigContent().isEmpty()) {
                MapConfigurationV1 mapConfigFromDB = null;
                try {
                    mapConfigFromDB = jsonObjectMapper.readValue(mapConfigFile.getConfigContent(), MapConfigurationV1.class);
                }
                catch ( IOException e) {
                    Utils.logError(LogTags.UA_APP_CONTEXT, "Could not create MapConfig object from json object :: "+ mapConfigFile.getConfigContent());
                    e.printStackTrace();
                }
                this.mapConfig = mapConfigFromDB;
            }
        }
        return this.mapConfig;
    }

    public RootConfig getRootConfig() {
        if(this.rootConfig == null ) {
            if(this.unifiedAppDBHelper == null) {
                this.initDBHelper();
            }
            if(userID == null) {
                String userId = getAppPreferences().getString(Constants.USER_ID_PREFERENCE_KEY, Constants.USER_ID_PREFERENCE_DEFAULT);
                this.setUserID(userId);
            }
            Utils.logInfo(LogTags.UA_APP_CONTEXT, "Get RootConfig config from DB for userId: "+ userID);
            ConfigFile rootConfigConfigFile = this.unifiedAppDBHelper.getConfigFile(userID,
                    Constants.ROOT_CONFIG_DB_NAME);
            if (rootConfigConfigFile != null && rootConfigConfigFile.getConfigContent() != null &&!rootConfigConfigFile.getConfigContent().isEmpty()) {
                RootConfig rootConfigFromDB = null;
                try {
                   rootConfigFromDB = jsonObjectMapper.readValue(rootConfigConfigFile.getConfigContent(), RootConfig.class);
                }
                catch ( IOException e) {
                    Utils.logError(LogTags.UA_APP_CONTEXT, "Could not create RootConfig object from json object :: "+rootConfigConfigFile.getConfigContent());
                    e.printStackTrace();
                }
                this.rootConfig = rootConfigFromDB;
            }
        }
        return this.rootConfig;
    }

    public List<Project> getProjectListCache() {
        return mProjectListCache;
    }

    public void setProjectListCache(List<Project> projectListCache) {
        this.mProjectListCache.clear();
        this.mProjectListCache.addAll(projectListCache);
    }

    public List<Project> getFilteredProjectListCache() {
        return mFilteredProjectListCache;
    }

    public void setFilteredProjectListCache(List<Project> filteredProjectListCache) {
        this.mFilteredProjectListCache.clear();
        this.mFilteredProjectListCache.addAll(filteredProjectListCache);
    }

    public ProjectList getProjectList() {
        return mProjectList;
    }

    public void setProjectList(ProjectList projectList) {
        this.mProjectList = projectList;
    }

    public void clearProjectListCaches() {
        this.mProjectList = null;
        this.mProjectListCache.clear();
        this.mFilteredProjectListCache.clear();
    }

    public Project getProjectFromProjectList(String projectId){
        if (this.mProjectList != null) {
            ArrayList<Project> projects = new ArrayList<>();
            if(mProjectList.mProjects != null && !mProjectList.mProjects.isEmpty()) {
                projects.addAll(mProjectList.mProjects);
            }
            for (int i=0; i < projects.size(); i++) {
                if (projects.get(i).mProjectId.equals(projectId)) {
                    return  projects.get(i);
                }
            }
        }
        return null;
    }

    public ProjectTypeModel getProjectTypeModel(String appId) {
        RootConfig config = getRootConfig();
        ProjectTypeModel projectType = null;
        if(config != null && config.mApplications != null) {
            for (ProjectTypeModel appConfig : config.mApplications) {
                if(appConfig.mAppId.equalsIgnoreCase(appId)) {
                    projectType = appConfig;
                    break;
                }

            }

        }
        return projectType;

    }

    public void setLocalizationJson(JSONObject localizationConfigJSON) {
        if(localizationConfigJSON == null) {
           String localizationConfig = LocalizationUtils.getInstance().getData();
            mLocalizationJson  =   jsonObjectMapper.convertValue(localizationConfig, JSONObject.class);
        } else {
        mLocalizationJson = localizationConfigJSON;
        }
    }

    public JSONObject getLocalization() {
        String locale = getLocale();
        JSONObject localizationJson = new JSONObject();
        try {
            if(mLocalizationJson != null && mLocalizationJson.has(locale)) {
                localizationJson = mLocalizationJson.getJSONObject(locale);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return localizationJson;
    }

    public void changeLanguage(String key){
        String languageToLoad = key;
        if(key == null || key.isEmpty()) {
            languageToLoad = "en";
        }

        changeStaticLocalizationConfiguration(languageToLoad);
    }

    public void addLocale(String locale) {
        if(sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Constants.APP_PREFERENCES_KEY
                    , MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.LOCALE_IN_PREFERENCE_KEY,locale == null || locale.isEmpty()? "en": locale);
        editor.apply();
    }

    public String  getLocale() {
        String locale = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingFragment.PREF_LANGUAGE,"");
        return locale;
    }

    public void  changeStaticLocalizationConfiguration(String languageToLoad) {
        Resources res = context.getResources(); // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        res.updateConfiguration(conf, dm);
    }

}
