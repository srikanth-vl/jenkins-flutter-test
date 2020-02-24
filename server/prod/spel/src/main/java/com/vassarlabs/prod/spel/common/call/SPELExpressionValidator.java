package com.vassarlabs.prod.spel.common.call;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.VariableReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import com.vassarlabs.common.utils.err.ValidationException;
import com.vassarlabs.prod.common.utils.DateUtils;

@Service
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
	public static final String DELIMITER = "##";
	
	public Boolean validateSPELExpression(String script, Map<String, String> keyToDataValue, String dataType, Properties additionalProperties) {
		EvaluationContext context = new StandardEvaluationContext(); 
		ExpressionParser parser = new SpelExpressionParser();
		SpelExpression exp = (SpelExpression) parser.parseExpression(script);
		Set<String> variablesForEvaluation = getVars(exp.getAST());
		List<String> errorMsg = new ArrayList<>();
		for(String variable : variablesForEvaluation) {
			if(keyToDataValue.containsKey(variable)) {
				errorMsg = setContextVariable(variable, keyToDataValue.get(variable), dataType, context, additionalProperties);
				if(!errorMsg.isEmpty())
					return false;
			} else 
				return true;
		}
		Boolean result = (Boolean) exp.getValue(context);
		return result;
	}
	
	public Boolean validateSPELExpression(String script, Map<String, String> keyToToDataValue,Map<String, String> keyToToDataType, Properties additionalProperties) {
		EvaluationContext context = new StandardEvaluationContext(); 
		ExpressionParser parser = new SpelExpressionParser();
		SpelExpression exp = (SpelExpression) parser.parseExpression(script);
		Set<String> variablesForEvaluation = getVars(exp.getAST());
		List<String> errorMsg = new ArrayList<>();
		for(String variable : variablesForEvaluation) {
			if(keyToToDataValue.containsKey(variable)
					&& keyToToDataType.containsKey(variable)) {
				errorMsg = setContextVariable(variable, keyToToDataValue.get(variable), keyToToDataType.get(variable), context, additionalProperties);
				if(!errorMsg.isEmpty())
					return false;
			} else 
				return true;
		}
		Boolean result = (Boolean) exp.getValue(context);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> generateParamsMap(String script, Map<String, String> keyToDataValue, String dataType, Properties additionalProperties) {
		EvaluationContext context = new StandardEvaluationContext(); 
		ExpressionParser parser = new SpelExpressionParser();
		SpelExpression exp = (SpelExpression) parser.parseExpression(script);
		Set<String> variablesForEvaluation = getVars(exp.getAST());
		for(String variable : variablesForEvaluation) {
			setContextVariable(variable, keyToDataValue.get(variable), dataType, context, additionalProperties);
		}
		Object paramsObject =  exp.getValue(context);
		Map<String, Object> params = null;
		if(paramsObject != null) {
			params = (Map<String, Object>) paramsObject;
		}
		return params;
	}

	public boolean validateSPELExpression(String script, Map<String, ?> variables) throws ValidationException {
		
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
	
	public List<String> setContextVariable(String key, String value, String dataType, EvaluationContext context, Properties additionalProperties)  {
		String format = additionalProperties.getProperty(dataType);
		List<String> errorMsg = new ArrayList<>();
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
		} else if(dataType.equalsIgnoreCase(DATATYPE_DATE)) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
				LocalDate date = LocalDate.parse(value, formatter);
				context.setVariable(key, date);
				// Used for evaluating against current date
				context.setVariable(CURRENT_DATE_STR, LocalDate.now());
			} catch(DateTimeException e) {
				errorMsg.add("For key : " + key + " expecting : " + dataType + " value, but found : " + value);
			}
		} else if(dataType.equalsIgnoreCase(DATATYPE_TIMESTAMP)) {
			if(value.matches("[0-9]+")) {
				context.setVariable(key, Long.parseLong(value));
			} else {
				errorMsg.add("For key : " + key + " expecting : " + dataType + " value, but found : " + value);
			}
		} else if(dataType.equalsIgnoreCase(DATATYPE_TIME)) {
			value = value != null ? String.valueOf(DateUtils.changeHHMMSSIntoSecond(value)) : value;
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
	
	
	public String evaluateSpelExpression(String script , Map<String, Map<String, String>> keyToValues)  {
		ExpressionParser expressionParser = new SpelExpressionParser();
		SpelExpression expression = (SpelExpression) expressionParser.parseExpression(script);
		EvaluationContext context = new StandardEvaluationContext(keyToValues);
		Set<String> variablesForEvaluation = getVars(expression.getAST());
		for(String variable : variablesForEvaluation) {
			if(keyToValues.containsKey(variable))
				context.setVariable(variable, keyToValues.get(variable));;
		}
		String result = (String) expression.getValue(context);
		return result;
	}

}
