import '../models/project_submission_result.dart';
import '../utils/project_submission_constants.dart';
import 'dart:convert';
import '../utils/string_utils.dart';

class ProjectSubmissionResultParser {

  /**
   * If validation error -> convert message to map{key -> {list of error messages}},
   * else parse the message as string message
   * @param projectSubmissionResult
   */
  String parseProjectSubmissionResult(ProjectSubmissionResult projectSubmissionResult) {
    if(projectSubmissionResult == null) {
      return null;
    }
    String errorMessage = projectSubmissionResult.message;
    String message = "";
    if(projectSubmissionResult.statusCode == ProjectSubmissionConstants.SUBMISSION_VALIDATION_ERROR) {
      Map<String, List<String>> errorMessgMap = new Map();
      jsonDecode(errorMessage).forEach((key, value){
        List<String> errors = List();
        if(value != null && value != ''){
          value.forEach((a) => errors.add(a));
          errorMessgMap[key] = errors;
        }
      });
      for (String key in errorMessgMap.keys) {
        message += (key + ": " +
            StringUtils.getconcatenatedStringFromStringList(
                ",", errorMessgMap[key]));
        message += "\n";
      }
      return message;
    }else {
      return errorMessage;
    }
  }
}
