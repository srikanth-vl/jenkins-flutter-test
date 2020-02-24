package com.vassar.unifiedapp.utils;

import android.content.res.AssetManager;

import com.vassar.unifiedapp.context.UAAppContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    protected static Properties properties;

    public static String getProperty(String propertyName) {
        try {
            if(properties == null) {
                properties = new Properties();
                AssetManager assetManager = UAAppContext.getInstance().getContext().getAssets();
                InputStream inputStream = assetManager.open("config.properties");
                properties.load(inputStream);
            }
            return properties.getProperty(propertyName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Reads the property from config.properties and convert it into boolean
     * @param propertyName
     * @return
     */
    public static boolean getBooleanProperty(String propertyName) {
        return Boolean.valueOf(getProperty(propertyName));
    }

    /**
     * Reads the property from config.properties and convert it into integer
     * @param propertyName
     * @return
     */
    public static Integer getIntegerProperty(String propertyName) {
        return Integer.valueOf(getProperty(propertyName));
    }

    /**
     * Reads the property from config.properties and convert it into double
     * @param propertyName
     * @return
     */
    public static Double getDoubleProperty(String propertyName) {
        return Double.valueOf(getProperty(propertyName));
    }
}