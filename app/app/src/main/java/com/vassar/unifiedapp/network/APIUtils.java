package com.vassar.unifiedapp.network;

import com.vassar.unifiedapp.utils.Constants;

public class APIUtils {

    private APIUtils() {}

    public static final String BASE_URL = Constants.BASE_URL;

    public static APIServices getAPIService() {

        return RetrofitClientInstance.getClient(BASE_URL).create(APIServices.class);
    }
}