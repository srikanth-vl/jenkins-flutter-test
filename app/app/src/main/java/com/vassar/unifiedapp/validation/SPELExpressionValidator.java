package com.vassar.unifiedapp.validation;

import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ClientValidationResponse;
import com.vassar.unifiedapp.utils.Constants;
import com.vassar.unifiedapp.utils.Utils;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.VariableReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SPELExpressionValidator {

    public static final String DATATYPE_INTEGER = "integer";
    public static final String DATATYPE_DOUBLE = "double";
    public static final String DATATYPE_STRING = "string";
    public static final String DATATYPE_LONG = "long";
    public static final String DATATYPE_DATE = "date";
    public static final String DATATYPE_IMAGE = "image";
    public static final String DATATYPE_GEOTAG = "geotag";
    public static final String CURRENT_DATE_STR = "current";
    public static final String DATATYPE_TIMESTAMP = "timestamp";
    public static final String DATATYPE_TIME = "time";

    public Boolean validateSPELExpression(String script, Map<String, String> keyToDataValue, String dataType) {

        try {
            EvaluationContext context = new StandardEvaluationContext();
            ExpressionParser parser = new SpelExpressionParser();
            SpelExpression exp = (SpelExpression) parser.parseExpression(script);
            Set<String> variablesForEvaluation = getVars(exp.getAST());
            for (String variable : variablesForEvaluation) {
                if (keyToDataValue.containsKey(variable)) {
                    if (keyToDataValue.get(variable) == null)
                        return true;
                    else
                        setContextVariable(variable, keyToDataValue.get(variable), dataType, context);
                } else
                    return true;
            }
            Boolean result = (Boolean) exp.getValue(context);
            return result;
        } catch(Exception e) {
            Utils.logError(LogTags.SPEL_EXPRESSION_VALIDATION, "Could not evaluate expression :: " + script);
            e.printStackTrace();
            return true;
        } catch(Error e) {
            Utils.logError(LogTags.SPEL_EXPRESSION_VALIDATION, "Could not evaluate expression :: " + script);
            e.printStackTrace();
            return true;
        }
    }

    public Boolean validateSPELExpression(String script, Map<String, String> keyToDataValue, String dataType, ClientValidationResponse clientValidationResponse) {
        EvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser();
        SpelExpression exp = (SpelExpression) parser.parseExpression(script);
        Set<String> variablesForEvaluation = getVars(exp.getAST());
        for(String variable : variablesForEvaluation) {
            if(keyToDataValue.containsKey(variable)) {
                if(keyToDataValue.get(variable) == null)
                    return true;
                else {
                    List<String> errorMessage = setContextVariable(variable, keyToDataValue.get(variable), dataType, context);
                    if(errorMessage != null && !errorMessage.isEmpty()) {
                        clientValidationResponse.mKeyToErrorMessage.put(variable, errorMessage.get(0));
                        clientValidationResponse.mIsValid = false;
                        return false;
                    }
                }
            }
            else
                return true;
        }
        try {
            Boolean result = (Boolean) exp.getValue(context);
            return result;
        }
        catch(Exception e) {
            Utils.logError(LogTags.SPEL_EXPRESSION_VALIDATION, "Could not evaluate expression :: " + script);
        e.printStackTrace();
        return true;
    } catch(Error e) {
            Utils.logError(LogTags.SPEL_EXPRESSION_VALIDATION, "Could not evaluate expression :: " + script);
            e.printStackTrace();
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> generateParamsMap(String script, Map<String, String> keyToDataValue, String dataType) {
        EvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser();
        SpelExpression exp = (SpelExpression) parser.parseExpression(script);
        Set<String> variablesForEvaluation = getVars(exp.getAST());
        for(String variable : variablesForEvaluation) {
            setContextVariable(variable, keyToDataValue.get(variable), dataType, context);
        }
        Object paramsObject =  exp.getValue(context);
        Map<String, Object> params = null;
        if(paramsObject != null) {
            params = (Map<String, Object>) paramsObject;
        }
        return params;
    }

    public boolean validateSPELExpression(String script, Map<String, ?> variables) {

        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        SpelExpression exp = (SpelExpression) parser.parseExpression(script);
        for(String variable : variables.keySet()) {
            context.setVariable(variable, variables.get(variable));
        }
        Boolean result = (Boolean) exp.getValue(context);
        return result;
    }

    /**
     * Gets all the variables present in the expression
     * Used to validate if all the variables defined in expression are set.
     * @param node
     * @return
     */
    public Set<String> getVars(SpelNode node) {
        Set<String> vars = new LinkedHashSet<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            SpelNode child = node.getChild(i);
            if (child.getChildCount() > 0) {
                vars.addAll(getVars(child));
            }
            else {
                if (child instanceof VariableReference) {
                    vars.add(child.toStringAST().replaceAll("#", ""));
                }
            }
        }
        return vars;
    }

    public List<String> setContextVariable(String key, String value, String dataType, EvaluationContext context)  {

        List<String> errorMsg = new ArrayList<>();
        //TODO: Copy this before modifying this function
        if(value.contains(Constants.NA_STRING)) {
            return errorMsg;
        }

        if(dataType.equalsIgnoreCase(DATATYPE_INTEGER)) {
            if(value.matches("[+-]?[0-9]+")) {
                context.setVariable(key, Integer.parseInt(value));
            } else {
                errorMsg.add("For key : " + key + " expecting : " + dataType + " value, but found : " + value);
            }
        } else if(dataType.equalsIgnoreCase(DATATYPE_DOUBLE)) {
            if(value.matches("[+-]?\\d*\\.?\\d+")) {
                context.setVariable(key, Double.parseDouble(value));
            } else {
                errorMsg.add("For key : " + key + " expecting : " + dataType + " value, but found : " + value);
            }
        } else if(dataType.equalsIgnoreCase(DATATYPE_LONG)) {
            if(value.matches("[+-]?[0-9]+")) {
                context.setVariable(key, Long.parseLong(value));
            } else {
                errorMsg.add("For key : " + key + " expecting : " + dataType + " value, but found : " + value);
            }
        } else if(dataType.equalsIgnoreCase(DATATYPE_TIMESTAMP)) {
            if(value.matches("[0-9]+")) {
                context.setVariable(key, Long.parseLong(value));
            } else {
                errorMsg.add("For key : " + key + " expecting : " + dataType + " value, but found : " + value);
            }
        } else if(dataType.equalsIgnoreCase(DATATYPE_TIME)) {

            value = value != null ? String.valueOf(changeHHMMSSIntoSecond(value)) : value;
            if(value.matches("[0-9]+") && Long.parseLong(value) <= 86400){
                context.setVariable(key, Long.parseLong(value));
            } else {
                errorMsg.add("For key : " + key + " expecting : " + dataType + " value, but found : " + value);
            }
        } else {
            if(value != null) {
                context.setVariable(key, value);
            } else {
                errorMsg.add("For key : " + key + " expecting : " + dataType + " value, but found : " + value);
            }
        }
        return errorMsg;
    }

    public static long getStartOfDay(long referenceTimeMillis){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(referenceTimeMillis);
        //cal.set(Calendar.DATE,-1);  //previous day
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Converts HH:MM:SS or HH:MM to long time
     * @param time
     * @return
     */
    public static long changeHHMMSSIntoSecond(String time) {
        int timeInSeconds = 0;
        int hh = 0;
        int mm = 0;
        int ss = 0;
        List<String>  hhmmss = getStringListFromDelimitter(":", time);
        if(hhmmss.size() == 1) {
            timeInSeconds = Integer.parseInt(hhmmss.get(0));
            return timeInSeconds;
        }
        else if(hhmmss.size() == 2) {
            hh  = Integer.parseInt(hhmmss.get(0));
            mm  = Integer.parseInt(hhmmss.get(1));
        }
        else if(hhmmss.size() == 3) {
            hh  = Integer.parseInt(hhmmss.get(0));
            mm  = Integer.parseInt(hhmmss.get(1));
            ss = Integer.parseInt(hhmmss.get(2));
        }
        timeInSeconds  = hh*60*60 + mm*60+ ss;
        return timeInSeconds;

    }

    public static List<String> getStringListFromDelimitter(String delimitter, String concatenatedString) {
        List<String> stringList = new ArrayList<>();
        if (concatenatedString == null || concatenatedString.isEmpty())
            return stringList;
        return Arrays.asList(concatenatedString.split(delimitter));
    }
}