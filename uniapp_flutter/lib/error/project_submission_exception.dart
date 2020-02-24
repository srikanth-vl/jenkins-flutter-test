import 'ua_exception.dart';
 class ProjectSubmissionException extends UAException {

   ProjectSubmissionException(String errorCode, String message,
       { String source}) : super(errorCode, message, source: source);
 }