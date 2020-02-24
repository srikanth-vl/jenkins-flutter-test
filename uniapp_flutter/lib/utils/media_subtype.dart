enum MediaSubType {
  FULL,
  PREVIEW,
  THUMBNAIL
}

class MediaSubTypeHelper {
  static int getValue(MediaSubType status){
    switch(status){
      case MediaSubType.FULL:
        return 1;
      case MediaSubType.PREVIEW:
        return 2;
      case MediaSubType.THUMBNAIL:
        return 3;
      default:
        return 0;
    }
  }
  static final Map<int, MediaSubType> valueToStateNameMap = new Map();
  final int value = 0;
  static createMap() {

    for (MediaSubType myEnum in MediaSubType.values) {
      valueToStateNameMap[MediaSubTypeHelper.getValue(myEnum)]= myEnum;
    }
  }

  static MediaSubType getStateByValue(int value) {
    if(valueToStateNameMap.isEmpty) {
      createMap();
    }
    return valueToStateNameMap[value];
  }
}


