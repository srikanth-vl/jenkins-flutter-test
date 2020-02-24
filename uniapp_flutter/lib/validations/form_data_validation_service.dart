import 'package:expressions/expressions.dart';

import 'client_validation_response.dart';
import '../log/uniapp_logger.dart';
import '../models/multiple_value.dart';
import '../models/uniapp_form.dart';
import '../models/uniapp_form_field.dart';
import '../models/validation_expression.dart';
import '../ui/helpers/form_values.dart';
class FormDataValidationService {
  Logger logger = getLogger("FormDataValidationService");
  /*
   * This method is called for validating the fields of the current subform
   */
  /* 
   1. Cast field Values as per data type in KeyToDataType Map
   2. Remove all # in Validation Expression (Were used for SpellExpression Validation In Java for Native Android Framework)
   3.  Find all ContextVariables in Given Validation Expression
   4. Return true if any context Variable is missing
   5. Evaluate expression
   */


  Future<ClientValidationResponse> validateFormSubmission( List<UniappFormField> formFields, Map<String, dynamic> superFieldMap, Map<String, dynamic> submittedFieldToValue,
      Map<String, String> keyToDataTypeMap) async {
    if(keyToDataTypeMap == null) {
      keyToDataTypeMap = Map();
    }
    ClientValidationResponse isValid = new ClientValidationResponse();
    isValid.mIsValid = true;

    if (formFields == null) {
      return isValid;
    }

    Map<String, dynamic> keyToSubmittedDataMap = new Map();
    for(String key in submittedFieldToValue.keys) {
      keyToSubmittedDataMap[key] =  submittedFieldToValue[key];
    }

    for (UniappFormField formField in formFields) {

      if (formField.validations != null) {
//        // checking for mandatory fields
        if (formField.uitype != null && formField.validations.mandatory!= null && formField.validations.mandatory) {
          if (!keyToSubmittedDataMap.containsKey(formField.key)) {
            isValid.mIsValid = false;
            isValid.mMessage = formField.label + " is a mandatory field. " + "Cannot be blank!";
            return isValid;
          } else {
            String value = keyToSubmittedDataMap[formField.key];
            if(value == null || value.isEmpty) {
              isValid.mIsValid = false;
              isValid.mMessage = formField.label + " is a mandatory field. " + "Cannot be blank!";
              return isValid;
            }
          }
        }

        switch (formField.uitype) {
          case "textview":
            if (formField.expandable != null && formField.expandable.subform != null) {
              UniappForm form = formMap.formIdToFormMap[formField.expandable.subform];
              if (form != null && form.fields != null) {
                ClientValidationResponse isSubFormValid = await validateFormSubmission( form.fields, superFieldMap, submittedFieldToValue, keyToDataTypeMap);
                if (!isSubFormValid.mIsValid) {
                  return isSubFormValid;
                }
              }
            }
            break;

          case "toggle":
          case "dropdown":
          case "checkbox":
          case "radio":
            List<MultipleValue> multipleValues = new List();
            multipleValues.addAll(formField.multipleValues);

            await keyToSubmittedDataMap.forEach((key, jsonObject) async {

              String value = jsonObject;

              if (key != null && key == formField.key) {
                for (MultipleValue multipleValue in multipleValues) {
                  if (value != null && value == multipleValue.value) {
                    if (multipleValue.expandable != null && multipleValue.expandable.subform!= null) {
                      UniappForm form = formMap.formIdToFormMap[multipleValue.expandable.subform];
                      if (form!= null && form.fields != null) {
                        ClientValidationResponse isSubFormValid = await validateFormSubmission( form.fields,
                            superFieldMap, submittedFieldToValue, keyToDataTypeMap);
                        if (!isSubFormValid.mIsValid) {
                          return isSubFormValid;
                        }
                      }
                    } else {
                      keyToDataTypeMap[formField.key]=formField.datatype;
                      performSPELValidation(formField.validations.validationExpression, keyToDataTypeMap, superFieldMap, isValid, formField.datatype);
                      if(!isValid.mIsValid) return isValid;
                    }
                  }
                }
              }
            });
            break;

          case "image":
          case "geotagimage":
          case "geotag":
          case "geotagimagefused":
          case "video":
            break;

          case "edittext":
          case "textbox":
          case "date":
          case "timepicker":
            keyToDataTypeMap[formField.key] = formField.datatype;

            performSPELValidation(formField.validations.validationExpression, keyToDataTypeMap, superFieldMap, isValid, formField.datatype);
            if(!isValid.mIsValid) return isValid;
        }
      }
    }
    return isValid;
  }

//     void performSPELValidation(ArrayList<ValidationExpression> expressionList, String dataType, Map<tring, String> superFieldMap, ClientValidationResponse isValid) {
//
//        if (expressionList == null || expressionList.isEmpty()) {
//            return;
//        }
//        for (ValidationExpression validationExpression : expressionList) {
//            SPELExpressionValidator spelExpressionValidator = new SPELExpressionValidator();
//            if (validationExpression.mExpression != null && !validationExpression.mExpression.isEmpty()) {
//                bool isExpressionValid = spelExpressionValidator
//                        .validateSPELExpression(validationExpression.mExpression,
//                                superFieldMap,
//                                dataType);
//                if (isExpressionValid != null && !isExpressionValid) {
//                    isValid.mIsValid = false;
//                    isValid.mMessage = validationExpression.mErrorMessage;
//                }
//            }
//        }
//    }

  void performSPELValidation(List<ValidationExpression> expressionList, Map<String, dynamic> keyToDataType, Map<String, dynamic> superFieldMap, ClientValidationResponse isValid, String dataType) {

    if (expressionList == null || expressionList.isEmpty) {
      isValid.mIsValid = true;
      return;
    }
    Map<String, dynamic> superFieldMapTemp = Map();

    for (ValidationExpression validationExpression in expressionList) {
      ExpressionEvaluator spelExpressionValidator = new ExpressionEvaluator();
      if (validationExpression.expr != null && validationExpression.expr.isNotEmpty) {
        String expressionString =
            validationExpression.expr;
        expressionString = expressionString.replaceAll(" or ", " || ");
        expressionString = expressionString.replaceAll(" and ", " && ");

        int indexf = expressionString.indexOf("#");
        String expressionKey  ="";
        List<String> keys = List();
        for (int i=indexf; i< expressionString.length ; i++) {
          if(expressionString[i] == "#" && expressionKey.isEmpty ) {
            expressionKey = expressionString[i+1];
            i= i+1;
          } else if((expressionString[i] == " " || expressionString[i] == "." || expressionString[i] ==')'
              || expressionString[i] == "*" || expressionString[i] == "/" || expressionString[i] == "-" || expressionString[i] == "+")&&expressionKey.isNotEmpty) {
            if(!keys.contains(expressionKey)) {
              keys.add(expressionKey);
            }
            expressionKey = "";
          } else if(expressionKey.isNotEmpty) {
            expressionKey += expressionString[i];
          }
        }
        for(String validationExpressionKey  in keys) {
          if(superFieldMap[validationExpressionKey] == null || superFieldMap[validationExpressionKey].toString().isEmpty ) {
            isValid.mIsValid = true;
            return ;
          } else {
            String keyValue = superFieldMap[validationExpressionKey];
            superFieldMapTemp[validationExpressionKey] = getFormattedValue(keyValue, keyToDataType[validationExpressionKey]);


        }

        expressionString = expressionString.replaceAll("#", "");
        logger.i('Expression to validated :: $expressionString');
        Expression expression =Expression.parse(expressionString);

        bool isExpressionValid = spelExpressionValidator
            .eval(expression,
            superFieldMapTemp);
        if (isExpressionValid != null && !isExpressionValid) {
          isValid.mIsValid = false;
          isValid.mMessage = validationExpression.errorMsg;
        }
      }
    }
  }

  }
  dynamic getFormattedValue(String value,String dataType) {
    switch (dataType){
      case "double":return double.parse(value);
    }
    return value;
  }
  bool validateMandatoryFields(Map<String, dynamic> submittedFieldsMap, List<String> mandatoryFields) {
    try {
      List<String> submittedFields = new List();
      for (String key in submittedFieldsMap.keys) {
        int lastHashIndex = key.lastIndexOf("#");
        String finalKey;
        if (lastHashIndex == -1) {
          finalKey = key;
        } else {
          finalKey = key.substring(lastHashIndex + 1);
        }
        String value = submittedFieldsMap[key];
        if (value != null && value.isNotEmpty) {
          submittedFields.add(finalKey);
        }
      }


      if (mandatoryFields.isEmpty) {
        return true;
      }
    } catch (e) {
      e.printStackTrace();
    }
    return false;
  }

}