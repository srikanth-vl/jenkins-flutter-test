
import 'ua_exception.dart';
 class AppCriticalException
    extends UAException {
     AppCriticalException(String errorCode, String message ,{ String source}) : super(errorCode, message, source:source);
 }
