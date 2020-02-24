package com.vassar.unifiedapp.utils;

import com.vassar.unifiedapp.context.UAAppContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {

    public static String getconcatenatedStringFromStringList(String delimitter, List<String> stringList) {
        String concatenatedString = "";
        if (stringList == null || stringList.isEmpty())
            return "";
        else if(stringList.size() == 1)  {
            return stringList.get(0);
        } else {
            for (int i = 0; i < stringList.size()-1; i++) {
                concatenatedString = concatenatedString + stringList.get(i) + delimitter;
            }
            concatenatedString += stringList.get(stringList.size()-1);
        }
        return concatenatedString;
    }
    public static List<String> getStringListFromDelimiter(String delimiter, String concatenatedString) {
        List<String> stringList = new ArrayList<>();
        if (concatenatedString == null || concatenatedString.isEmpty())
            return stringList;
        return Arrays.asList(concatenatedString.split(delimiter));
    }
    public static String getFormattedText(String value) {
        if(value == null)
            return  null;
        value = value.trim();
        value = value.toLowerCase();
        value = value.replace(" ","_");
        return value;
    }
    public static String getTranslatedString(String text) {
        String translatedText = text;
        JSONObject json =  UAAppContext.getInstance().getLocalization();
        if(json != null && json.has(text)) {
            try {
                translatedText = json.getString(text);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return translatedText;
    }
}
