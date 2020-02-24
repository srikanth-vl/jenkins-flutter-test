import 'dart:math';
import '../utils/common_constants.dart';
import '../db/models/project_master_data_table.dart';
import 'dart:io';


class ProjectListSortingService {

  static List<ProjectMasterDataTable> mList;
  static String mSortType;
  static double mUserLat;
  static double mUserLong;

  static void sort() {

    switch (mSortType) {
      case CommonConstants.PROJECT_LIST_ALPHABETICAL_SORTING :
      // The list is shown in alphabetical order
        if (mList.length > 0) {
          mList.sort((a, b) {
            return a.projectName.compareTo(b.projectName);
          });
        }
        break;

      case CommonConstants.PROJECT_LIST_LAST_UPDATED_ASCENDING_SORTING:
      // The list is shown in alphabetical order
        if (mList.length > 0) {
          mList.sort((a, b) => a.projectLastUpdatedTs.compareTo(b.projectLastUpdatedTs) != 0 ?
          a.projectLastUpdatedTs.compareTo(b.projectLastUpdatedTs) : a.projectName.compareTo(b.projectName));
        }
        break;

      case CommonConstants.PROJECT_LIST_LAST_UPDATED_DESCENDING_SORTING:
      // The list is shown in alphabetical order
        if (mList.length > 0) {
          mList.sort((a, b) => b.projectLastUpdatedTs.compareTo(a.projectLastUpdatedTs) != 0 ?
          b.projectLastUpdatedTs.compareTo(a.projectLastUpdatedTs) : b.projectName.compareTo(a.projectName));

        }
        break;

      case CommonConstants.PROJECT_LIST_NEAREST_PROJECTS_FIRST:
        if (mUserLat != null && mUserLat != 0.0 &&
            mUserLong != null && mUserLong != 0.0) {

          double distanceToPlace1 = 0.0;
          double distanceToPlace2 = 0.0;

          if (mList.length > 0) {
            mList.sort((o1, o2) {
              if (o1.projectLat == null || o1.projectLon == null || o1.projectLat.isEmpty || o1.projectLon.isEmpty) {
                distanceToPlace1 = distance(mUserLat, mUserLong, 0.0, 0.0);
              } else if (o1.projectLat != null && !o1.projectLat.isEmpty && o1.projectLon != null && !o1.projectLon.isEmpty) {
                distanceToPlace1 = distance(mUserLat, mUserLong, double.parse(o1.projectLat), double.parse(o1.projectLon));
              }
              if (o2.projectLat == null || o2.projectLon == null || o2.projectLat.isEmpty || o2.projectLon.isEmpty) {
                distanceToPlace2 = distance(mUserLat, mUserLong, 0.0, 0.0);
              } else if (o2.projectLat != null && !o2.projectLat.isEmpty && o2.projectLon != null && !o2.projectLon.isEmpty) {
                distanceToPlace2 = distance(mUserLat, mUserLong, double.parse(o2.projectLat), double.parse(o2.projectLon));
              }
              if (distanceToPlace1 == distanceToPlace2)
                return 0;
              else if (distanceToPlace1 > distanceToPlace2)
                return 1;
              else
                return -1;
            });
          }
        }
        break;
    }
  }

  static double distance(double fromLat, double fromLon, double toLat, double toLon) {
    double deltaLat = toLat - fromLat;
    double deltaLon = toLon - fromLon;
    return sqrt(pow(deltaLat, 2)) + sqrt(pow(deltaLon, 2));
  }
}
