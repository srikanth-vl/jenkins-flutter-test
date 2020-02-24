package services.com.vassarlabs.play.services;

import java.rmi.server.UID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;

import models.com.vassarlabs.play.models.ApiResponse;

@Service
public class ApiResponseFactory {

    private ApiResponseFactory () {
        //don't instantiate this class
    }

    @JsonIgnore
    public static ApiResponse createResponse(Object object) {
        ApiResponse response = new ApiResponse();
        response.setId((new UID()).toString());
        response.setResult(true, 200, object, "");
        return response;
    }

    @JsonIgnore
    public static ApiResponse createErrorResponse() {
        ApiResponse response = new ApiResponse();
        response.setId((new UID()).toString());
        response.setResult(false, 500, "NA", "Error");
        return response;
    }

    @JsonIgnore
    public static ApiResponse createErrorResponse(int errorCode, String errMessage) {
        ApiResponse response = new ApiResponse();
        response.setId((new UID()).toString());
        response.setResult(false, errorCode, errMessage, errMessage);
        return response;
    }
}