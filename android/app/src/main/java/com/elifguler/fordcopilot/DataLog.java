package com.elifguler.fordcopilot;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DataLog implements Serializable {
    public static final long serialVersionUID = 1L;

    @SerializedName("type")
    String type;

    @SerializedName("data")
    Serializable data;
}
