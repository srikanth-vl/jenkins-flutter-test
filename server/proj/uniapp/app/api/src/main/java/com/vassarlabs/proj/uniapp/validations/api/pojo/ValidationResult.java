package com.vassarlabs.proj.uniapp.validations.api.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResult {
	
	boolean valid;
	Map<String, List<String>> keyToErrorMessages;
	
	public static ValidationResult ok() {
        return new ValidationResult(true, new HashMap<>());
    }
    private ValidationResult(boolean valid) {
        this.valid = valid;
    }
	public static ValidationResult fail(Map<String, List<String>> keyToErrorMessages) {
		return new ValidationResult(false , keyToErrorMessages);
    }
	public static ValidationResult fail() {
		return new ValidationResult(false);
    }
}
