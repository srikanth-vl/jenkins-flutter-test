package com.vassar.unifiedapp.model;

public class ProjectSubmissionResult {

    int statusCode;
    boolean isSuccessful;
    String message;

    public ProjectSubmissionResult() {
    }

    public ProjectSubmissionResult(int statusCode, boolean isSuccessful, String message) {
        this.statusCode = statusCode;
        this.isSuccessful = isSuccessful;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ProjectSubmissionResult{" +
                "statusCode=" + statusCode +
                ", isSuccessful=" + isSuccessful +
                ", message='" + message + '\'' +
                '}';
    }
}
