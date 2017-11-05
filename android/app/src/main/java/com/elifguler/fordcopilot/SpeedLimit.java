package com.elifguler.fordcopilot;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpeedLimit implements Serializable {
    public static final long serialVersionUID = 1L;

    @SerializedName("speedLimit")
    int speedLimit;

    @SerializedName("roadType")
    String roadType;

    @SerializedName("name")
    String roadName;

    @SerializedName("oneway")
    boolean oneWay;

    @SerializedName("lanes")
    int laneCount;

    static class SpeedLimitBody {
        @SerializedName("lat")
        double lat;

        @SerializedName("lng")
        double lng;
    }
}