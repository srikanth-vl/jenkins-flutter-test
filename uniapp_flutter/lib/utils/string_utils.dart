import '../ua_app_context.dart';

class StringUtils {

  static String getconcatenatedStringFromStringList(String delimitter, List<String> stringList) {
    String concatenatedString = "";
    if (stringList == null || stringList.isEmpty)
      return "";
    else if(stringList.length == 1)  {
      return stringList[0];
    } else {
      for (int i = 0; i < stringList.length-1; i++) {
        concatenatedString = concatenatedString + stringList[i] + delimitter;
      }
      concatenatedString += stringList[stringList.length-1];
    }
    return concatenatedString;
  }
  static List<String> getStringListFromDelimiter(String delimiter, String concatenatedString) {
    List<String> stringList = new List();
    if (concatenatedString == null || concatenatedString.isEmpty)
      return stringList;
    return concatenatedString.split(delimiter);
  }
  static String getFormattedText(String value) {
    if(value == null)
      return  null;
    value = value.trim();
    value = value.toLowerCase();
//    value = value.replace(" ","_");
    return value;
  }
  static String getTranslatedString(String text) {
    String translatedText = text;
    Map<String, dynamic> json =  UAAppContext.getInstance().getLocalization();
    if(json != null && (json.containsKey(text) ||json.containsKey(text.toLowerCase())) ) {
      translatedText = json[text] == null ?json[text.toLowerCase()]: json[text] ;
    }
    return translatedText;
  }

  static List<String> getImageUUIDList(List<String> uuidsWithLongLat) {
    List<String>uuids = List();
    for(String uuidWithLatLong in uuidsWithLongLat) {
      List<String> imageUUIDInfo = getStringListFromDelimiter(
          "##", uuidWithLatLong);
      if (imageUUIDInfo.length > 0) {
        uuids.add(imageUUIDInfo[0]);
      }
    }
    return uuids;
  }
  static String getKey(String key) {
    List<String> subkeys = StringUtils.getStringListFromDelimiter("\$\$", key);
    return subkeys.elementAt(subkeys.length - 1);
  }
}
