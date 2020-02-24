import 'ua_exception.dart';
 class ServerFetchException extends UAException {
  ServerFetchException(String errorCode, String message ,{ String source}) : super(errorCode, message, source:source);

}
