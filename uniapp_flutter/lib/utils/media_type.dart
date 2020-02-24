
enum MediaType {
  IMAGE,
  VIDEO,
  AUDIO,
  TEXT,
  PDF,
  BLOB,
  OTHERS
}

class MediaTypeHelper {
  static int getValue(MediaType status){
    switch(status){
      case MediaType.IMAGE:
        return 1;
      case MediaType.VIDEO:
        return 2;
      case MediaType.AUDIO:
        return 3;
      case MediaType.TEXT:
        return 4;
      case MediaType.PDF:
        return 5;
      case MediaType.BLOB:
        return 6;
      case MediaType.OTHERS:
        return 7;
      default:
        return 0;
    }
  }
  static final Map<int, MediaType> valueToStateNameMap = new Map();
  final int value = 0;
  static createMap() {

    for (MediaType myEnum in MediaType.values) {
      valueToStateNameMap[MediaTypeHelper.getValue(myEnum)]= myEnum;
    }
  }

  static MediaType getStateByValue(int value) {
    if(valueToStateNameMap.isEmpty) {
      createMap();
    }
    return valueToStateNameMap[value];
  }
}


