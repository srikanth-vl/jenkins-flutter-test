import 'ua_exception.dart';
class UAConfigException
        extends UAException {
  UAConfigException(String errorCode, String message ,{ String source}) : super(errorCode, message, source:source);
}
