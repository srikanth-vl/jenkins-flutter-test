import '../models/sub_app.dart';
import '../utils/common_constants.dart';


class SubAppSortingService {

  static List<SubApp> mList;
  static String mSortType;

  static void sort() {
    switch (mSortType) {
      case CommonConstants.PROJECT_TYPE_ALPHABETICAL_SORTING:
      // The list is shown in alphabetical order
        if (mList.length > 0) {
          mList.sort((a, b) => a.name.compareTo(b.name));
        }
        break;

      case CommonConstants.PROJECT_TYPE_CUSTOM_SORTING:
        if (mList.length > 0) {
          mList.sort((a, b) => a.order.compareTo(b.order));
        }
        break;
    }
  }
}
