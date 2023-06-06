package com.creativeapps.schoolbustracker.data.network.services;


import com.creativeapps.schoolbustracker.data.Util;
import com.creativeapps.schoolbustracker.data.network.models.ChildResponse;
import com.creativeapps.schoolbustracker.data.network.models.EventLogResponse;
import com.creativeapps.schoolbustracker.data.network.models.ParentResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;


public class ParentApiService {

    //URL of the backend for APIs
    private static final String URL = Util.WEB_SERVER_URL + "/api/parents/";
    //Parent API instance
    private ParentApi parentApi;
    //Parent API service instance
    private static ParentApiService instance;

    /*get parent API instance*/
    public ParentApi getParentApi() {
        return parentApi;
    }

    /*get parent API service instance*/
    public static ParentApiService getInstance() {
        if (instance == null) {
            //if the parent API service is not already instantiated, create new one
            instance = new ParentApiService();
        }
        //return the created instance
        return instance;
    }

    /*constructor for parent API service*/
    private ParentApiService() {
        //create a Retrofit object with json enabled
        Retrofit mRetrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(URL).build();
        //initialize the parent API instance
        parentApi = mRetrofit.create(ParentApi.class);
    }

    /*define the interface with required methods from the API*/
    public interface ParentApi {
        //method that get the details of a parent (including school and bus data) using a telephone number
        @GET("getParentTelNumber")
        Call<ParentResponse> getParentTelNumber(@Query("country_code") String countryCode,
                                                         @Query("tel_number") String tel_number,
                                                         @Query("secretKey") String secretKey);

        //method that verify the telephone number of a parent after sign in using a verification code
        @POST("verifyParentTelNumber")
        Call<ParentResponse> verifyParentTelNumber(@Query("fcm_token") String token,
                                                   @Query("country_code") String countryCode,
                                                   @Query("tel_number") String tel_number,
                                                   @Query("v_code") String v_code);

        //method that update the position of the parent
        @PUT("updatePosition")
        Call<ResponseBody> updatePosition(
                                          @Query("secretKey") String secretKey,
                                          @Query("address_latitude") Double last_latitude,
                                          @Query("address_longitude") Double last_longitude);

        //method that update the position of the parent
        @PUT("updateChildAbsent")
        Call<ChildResponse> updateChildAbsent(@Query("id") Integer id,
                                              @Query("tomorrow_date") String tomorrow_date);

        //method that set the alert distance of the parent
        @PUT("setZoneAlertDistance")
        Call<ResponseBody> setParentZoneDistance(
                                           @Query("secretKey") String secretKey,
                                           @Query("zoneAlertDistance") Integer zoneAlertDistance);

        //method that get driver log of the parent (sign in)
        @POST("getDriverLog")
        Call<EventLogResponse> getDriverLog(
                                            @Query("page") Integer page,
                                            @Query("secretKey") String secretKey);


        //method that get driver log of the parent (sign in)
        @POST("getChildLog")
        Call<EventLogResponse> getChildLog(
                                           @Query("page") Integer page,
                                           @Query("secretKey") String secretKey);

    }



}


