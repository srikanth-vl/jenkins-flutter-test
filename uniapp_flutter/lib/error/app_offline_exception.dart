import 'ua_exception.dart';
 class AppOfflineException extends UAException {

  AppOfflineException(String errorCode, String message ,{ String source}) : super(errorCode, message, source:source);

}
