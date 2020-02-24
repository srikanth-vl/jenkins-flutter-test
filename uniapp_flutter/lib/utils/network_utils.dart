import 'package:connectivity/connectivity.dart';

class NetworkUtils {

  Future<bool> hasActiveInternet() async {
    var connectivityResult = await (Connectivity().checkConnectivity());
    if (connectivityResult == ConnectivityResult.mobile
        || connectivityResult == ConnectivityResult.wifi) {
      // Connected to a mobile network or wifi network
      return true;
    } else {
      // No active internet connection
      return false;
    }
  }
}

final networkUtils = NetworkUtils();