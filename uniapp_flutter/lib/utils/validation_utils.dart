import '../models/uniapp_form.dart';

class ValidationUtils {

  bool checkForMandatory(UniappForm form, Map<String, String> userEnteredValues) {

    if(form != null && form.fields != null && form.fields.isNotEmpty &&
        userEnteredValues != null && userEnteredValues.isNotEmpty) {
      for(int i = 0; i < form.fields.length; i++) {
        if(form.fields[i].validations != null &&
            form.fields[i].validations.mandatory &&
            !userEnteredValues.containsKey(form.fields[i].key)) {
          return false;
        }
      }
    } else {
      // TODO : Log error and return true
      return true;
    }

    return true;
  }
}

final validationUtils = ValidationUtils();