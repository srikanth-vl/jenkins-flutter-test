package com.vassar.unifiedapp.network;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIServices {

    @Headers({"Content-Type:application/json"})
    @POST("appmetaconfigjson")
    Call<ResponseBody> refreshAppMetaConfig(@Body Map<String, String> versionID);

    @Headers({"Content-Type:application/json"})
    @POST("appmetaconfigjson")
    ResponseBody refreshAppMetaConfig1(@Body Map<String, String> versionID);

    @Headers({"Content-Type:application/json"})
    @POST("authenticate")
    Call<ResponseBody> authenticate(@Body Map<String, String> params);

    @Headers({"Content-Type:application/json"})
    @POST("authenticate")
    ResponseBody authenticateNew(@Body Map<String, String> params);

    @Headers({"Content-Type:application/json"})
    @POST("rootconfigdata")
    Call<ResponseBody> getRootConfig(@Body Map<String, String> params);

    @Headers({"Content-Type:application/json"})
    @POST("projecttype")
    Call<ResponseBody> getProjectTypeConfig(@Body Map<String, String> params);

    @Headers({"Content-Type:application/json"})
    @POST("projectlist")
    Call<ResponseBody> getProjectListConfig(@Body Map<String, Object> params);

    @Headers({"Content-Type:application/json"})
    @POST("{submit}")
    Call<ResponseBody> submitProjects(@Path("submit") String submitEndpoint, @Body Map<String, String> userDetails);

    @Headers({"Content-Type:application/json"})
    @POST("logout")
    Call<ResponseBody> logout(@Body Map<String, String> userDetails);

    @Headers({"Content-Type:application/json"})
    @POST("generatepasswordresetotp")
    Call<ResponseBody> getOtp(@Body Map<String, String> userDetails);

    @Headers({"Content-Type:application/json"})
    @POST("resetpassword")
    Call<ResponseBody> changePassword(@Body Map<String, String> userDetails);
}
