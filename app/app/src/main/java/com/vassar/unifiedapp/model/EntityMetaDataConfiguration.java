package com.vassar.unifiedapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityMetaDataConfiguration {

    @JsonProperty("entities")
    List<EntityMetaData> entityMetaDataArrayList;

    public EntityMetaDataConfiguration(List<EntityMetaData> entityMetaDataArrayList) {
        this.entityMetaDataArrayList = entityMetaDataArrayList;
    }
    public EntityMetaDataConfiguration() {

    }

    public List<EntityMetaData> getEntityMetaDataArrayList() {
        return entityMetaDataArrayList;
    }

    public void setEntityMetaDataArrayList(List<EntityMetaData> entityMetaDataArrayList) {
        this.entityMetaDataArrayList = entityMetaDataArrayList;
    }
}
