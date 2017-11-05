package com.elifguler.fordcopilot;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Emergency implements Serializable {
    public static final long serialVersionUID = 1L;

    @SerializedName("number")
    String number;

    @SerializedName("text")
    String message;
}
