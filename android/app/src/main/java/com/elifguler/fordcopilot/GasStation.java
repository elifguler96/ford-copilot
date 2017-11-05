package com.elifguler.fordcopilot;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GasStation implements Serializable{
    public static final long serialVersionUID = 1L;

    @SerializedName("name")
    String name;

    @SerializedName("location")
    Location location;

    @SerializedName("price")
    Double price;

    @SerializedName("fuelEfficiency")
    Double fuelEfficiency;

    @SerializedName("lastRefuel")
    String lastRefuel;
}

class Location {
    @SerializedName("lat")
    Double latitude;

    @SerializedName("lng")
    Double longitude;
}
