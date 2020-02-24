package com.vassarlabs.prod.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class StringUtils {

	public static final String EMPTY_STRING = "";
	public static final String COMMA_STRING = ",";
	public static final String UNDERSCORE_STRING = "_";
	public static final String SLASH_STRING = "/";
	public static final String NEW_LINE_SEPARATOR = System.lineSeparator();
	public static final String SPACE_STRING = " ";
	public static final String HASH_STRING = "#";
	public static final String SINGLE_QUOTE_STRING = "'";
	public static final String HYPHEN_STRING = "-";
	public static final String NULL_STRING = "null";
	//Added for Jira-391 - send daily sms
	public static final String DOT_STRING = ".";

	public static boolean isNullOrEmpty(String value) {
		if (value == null || value.trim().length() == 0) {
			return true;
		}
		return false;
	}
	
	public static String removeQuotesFromEnds(String dataFromRTU) {
		if(dataFromRTU.startsWith("\"")){
			dataFromRTU = dataFromRTU.substring(1);
		}
		if(dataFromRTU.endsWith("\"")){
			dataFromRTU = dataFromRTU.substring(0, dataFromRTU.length()-1);
		}
		return dataFromRTU;
	}
 

	
	public static String commaSeparatedQuotedStringsForSQL (List<String> stringList) {
		if(stringList == null || stringList.isEmpty()) return null;
		StringBuilder builder = new StringBuilder();
		builder.append(SINGLE_QUOTE_STRING).append(stringList.get(0)).append(SINGLE_QUOTE_STRING);
		for(int i=1; i<stringList.size();i++){
			builder.append(COMMA_STRING).append(SINGLE_QUOTE_STRING).append(stringList.get(i)).append(SINGLE_QUOTE_STRING);
		}
		return builder.toString();
	}

	public static String commaSeparatedListForSQL (Set<Integer> set) {
		List<Integer> list = new ArrayList<>();
		list.addAll(set);
		return commaSeparatedListForSQL(list);
	}
	
	public static String commaSeparatedListForSQL (List<Integer> list) {
		if(list == null || list.isEmpty()) return null;
		StringBuilder builder = new StringBuilder();
		builder.append(list.get(0));
		for(int i=1; i<list.size();i++){
			builder.append(COMMA_STRING).append(list.get(i));
		}
		return builder.toString();
	}
	
	public static List<Long> convertIntegerListToLongList(List<Integer> locationIDs){
		List<Long> returnList = new ArrayList<>();
		for (int index = 0; index < locationIDs.size(); index++) {
			returnList.add(locationIDs.get(index).longValue());
		}
		return returnList;
	}
	
	public static boolean isVillageConcatenatedName(String fullLocName) {
		String villageFullNamePattern=".+##.+##.+";
		if (Pattern.matches(villageFullNamePattern, fullLocName)) {
			return true;
		}
		return false;
	}
	
	public static String commaSeparatedStringForSQL (List<Long> list) {
		if(list == null || list.isEmpty()) return null;
		StringBuilder builder = new StringBuilder();
		builder.append(list.get(0));
		for(int i=1; i<list.size();i++){
			builder.append(COMMA_STRING).append(list.get(i));
		}
		return builder.toString();
	}
	
	public static <T> String commaSeparatedStringForCQL(List<T> columnList) {
		if(columnList == null || columnList.isEmpty()) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(SINGLE_QUOTE_STRING).append(columnList.get(0)).append(SINGLE_QUOTE_STRING);
		for(int i=1; i< columnList.size(); i++) {
			builder.append(COMMA_STRING).append(SINGLE_QUOTE_STRING).append(columnList.get(i)).append(SINGLE_QUOTE_STRING);
		}
		return builder.toString();
		
	}
	
	public static <T> String commaSeparatedListForCQL(List<T> columnList) {
		if(columnList == null || columnList.isEmpty()) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(columnList.get(0));
		for(int i=1; i< columnList.size(); i++) {
			builder.append(COMMA_STRING).append(columnList.get(i));
		}
		return builder.toString();
		
	}
	
	public static String getFirstNotEmpty(List<String> list) {
	    if (list == null || list.isEmpty()) {
	      return null;
	    }
	    for (String item : list) {
	      if (item != null && !item.isEmpty()) {
	        return item;
	      }
	    }
	    return null;
	}
	
	public static String concatenate(String... list) {
	   return concatenate(' ', list);
	}
	
	public static String concatenate(char delimitter, String... list) {
	    String finalString = "";
		if (list == null || list.length == 0) {
	      return null;
	    }
	    for (String item : list) {
	      if (item != null && !item.isEmpty()) {
	        finalString = finalString.concat(item).concat(String.valueOf(delimitter));
	      }
	    }
	    if(finalString.length() >= 1)
	    	finalString = finalString.substring(0, finalString.length() - 1).trim();
	    return finalString;
	}
	
	public static List<String> getStringListFromDelimitter(String delimitter, String concatenatedString) {
		List<String> stringList = new ArrayList<>();
		if (concatenatedString == null || concatenatedString.isEmpty())
			return stringList;
		return Arrays.asList(concatenatedString.split(delimitter));
	}
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
}