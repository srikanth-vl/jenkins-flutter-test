package com.vassar.unifiedapp.model;

public class ConfigFile {

    private String mUserId;
    private String mConfigName;
    private String mConfigContent;
    private Integer mConfigVersion;
    private Long mConfigLastSyncTs;

    public ConfigFile(String userId, String configName, String configContent,
                      Integer configVersion, Long configLastSyncTs) {
        this.mUserId = userId;
        this.mConfigName = configName;
        this.mConfigContent = configContent;
        this.mConfigVersion = configVersion;
        this.mConfigLastSyncTs = configLastSyncTs;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }

    public String getConfigName() {
        return mConfigName;
    }

    public void setConfigName(String configName) {
        this.mConfigName = configName;
    }

    public String getConfigContent() {
        return mConfigContent;
    }

    public void setConfigContent(String configContent) {
        this.mConfigContent = configContent;
    }

    public Integer getConfigVersion() {
        return mConfigVersion;
    }

    public void setConfigVersion(Integer configVersion) {
        this.mConfigVersion = configVersion;
    }

    public Long getConfigLastSyncTs() {
        return mConfigLastSyncTs;
    }

    public void setConfigLastSyncTs(Long configLastSyncTs) {
        this.mConfigLastSyncTs = configLastSyncTs;
    }
}
