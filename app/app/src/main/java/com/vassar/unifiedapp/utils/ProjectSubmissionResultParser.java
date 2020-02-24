package com.vassar.unifiedapp.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassar.unifiedapp.log.LogTags;
import com.vassar.unifiedapp.model.ProjectSubmissionResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProjectSubmissionResultParser {
    /**
     * If validation error -> convert message to map{key -> {list of error messages}},
     * else parse the message as string message
     * @param projectSubmissionResult
     */
    public String parseProjectSubmissionResult(ProjectSubmissionResult projectSubmissionResult) {
        if(projectSubmissionResult == null) {
            Utils.getInstance().showLog("PARSE SUBMISSION RESULT", "ProjectSubmissionResult is NULL");
            return null;
        }
        String errorMessage = projectSubmissionResult.getMessage();
        if(projectSubmissionResult.getStatusCode() == ProjectSubmissionConstants.SUBMISSION_VALIDATION_ERROR) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Map<String, List<String>> keyToErrorMessages = objectMapper.readValue(errorMessage, new TypeReference<Map<String, List<String>>>() {
                });
                StringBuilder message = new StringBuilder();
                for (String key : keyToErrorMessages.keySet()) {
                    message.append(key + ": " + StringUtils.getconcatenatedStringFromStringList(",", keyToErrorMessages.get(key)));
                    message.append("\n");
                }
            } catch (IOException e) {
                Utils.logError(LogTags.PROJECT_SUBMISSION_SERVICE, "Failed to parse Json :: " + errorMessage);
                e.printStackTrace();
            }
            return errorMessage;
        }else {
            return errorMessage;
        }
    }
}
