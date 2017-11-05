package com.elifguler.fordcopilot;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;


public interface RestInterfaceController {

    @POST("login")
    Call<User> login(@Body User user);

    @GET("speed-limit")
    Call<SpeedLimit> speedLimit(@QueryMap Map<String, Double> body);

    @POST("log-data")
    Call<Void> logData(@Body DataLog dataLog);

    @GET("fuel-station")
    Call<List<GasStation>> gasStations(@QueryMap Map<String, Double> body);

    @GET("refuel-time")
    Call<Double> refuelTime();

    @POST("emergency-contact")
    Call<Emergency> emergency(@Body Emergency emergency);

    @GET("send-emergency-text")
    Call<Integer> sendEmergencyText();
}
