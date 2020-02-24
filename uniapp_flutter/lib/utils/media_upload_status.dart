enum MediaUploadStatus {
  NEW,
  SYNCED,
  FAILED,
  PENDING,
  DELETED
}

class MediaUploadStatusHelper {
  static int getValue(MediaUploadStatus status){
    switch(status){
      case MediaUploadStatus.NEW:
        return 1;
      case MediaUploadStatus.SYNCED:
        return 2;
      case MediaUploadStatus.FAILED:
        return 3;
      case MediaUploadStatus.PENDING:
        return 4;
      case MediaUploadStatus.DELETED:
        return 5;
      default:
        return 0;
    }
  }
  static final Map<int, MediaUploadStatus> valueToStateNameMap = new Map();
  final int value = 0;
  static createMap() {

    for (MediaUploadStatus myEnum in MediaUploadStatus.values) {
      valueToStateNameMap[MediaUploadStatusHelper.getValue(myEnum)]= myEnum;
    }
  }

  static MediaUploadStatus getStateByValue(int value) {
    if(valueToStateNameMap.isEmpty) {
      createMap();
    }
    return valueToStateNameMap[value];
  }
}


