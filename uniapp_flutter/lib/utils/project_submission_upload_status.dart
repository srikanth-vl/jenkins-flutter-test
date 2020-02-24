enum ProjectSubmissionUploadStatus {
  UNSYNCED,
  SYNCED,
  SYNCED_WITH_MEDIA,
  VALIDATION_ERROR,
  SERVER_ERROR,
  FAILED,
  DELETED,
  APP_VERSION_MISMATCH_ERROR
}
class ProjectSubmissionUploadStatusHelper {
  static int getValue(ProjectSubmissionUploadStatus status){
    switch(status){
      case ProjectSubmissionUploadStatus.UNSYNCED:
        return 0;
      case ProjectSubmissionUploadStatus.SYNCED:
        return 1;
      case ProjectSubmissionUploadStatus.SYNCED_WITH_MEDIA:
        return 2;
      case ProjectSubmissionUploadStatus.SERVER_ERROR:
        return -2;
      case ProjectSubmissionUploadStatus.FAILED:
        return -3;
      case ProjectSubmissionUploadStatus.DELETED:
        return -4;
      case ProjectSubmissionUploadStatus.APP_VERSION_MISMATCH_ERROR:
        return -5;
      default:
        return 5;
    }
  }
  static final Map<int, ProjectSubmissionUploadStatus> valueToStateNameMap = new Map();
  final int value = 0;
  static createMap() {

    for (ProjectSubmissionUploadStatus myEnum in ProjectSubmissionUploadStatus.values) {
      valueToStateNameMap[ProjectSubmissionUploadStatusHelper.getValue(myEnum)]= myEnum;
    }
  }

  static ProjectSubmissionUploadStatus getStateByValue(int value) {
    if(valueToStateNameMap.isEmpty) {
      createMap();
    }
    return valueToStateNameMap[value];
  }
}


