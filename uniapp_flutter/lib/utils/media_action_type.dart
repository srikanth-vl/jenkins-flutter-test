enum MediaActionType {
  DOWNLOAD,
  UPLOAD
}

class MediaActionTypeHelper {
  static int getValue(MediaActionType status){
    switch(status){
      case MediaActionType.DOWNLOAD:
        return 1;
      case MediaActionType.UPLOAD:
        return 2;
      default:
        return 0;
    }
  }
  static final Map<int, MediaActionType> valueToStateNameMap = new Map();
  final int value = 0;
  static createMap() {

    for (MediaActionType myEnum in MediaActionType.values) {
      valueToStateNameMap[MediaActionTypeHelper.getValue(myEnum)]= myEnum;
    }
  }

  static MediaActionType getStateByValue(int value) {
    if(valueToStateNameMap.isEmpty) {
      createMap();
    }
    return valueToStateNameMap[value];
  }
}


