package com.elifguler.fordcopilot;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    public static final long serialVersionUID = 1L;

    @SerializedName("email")
    String email;

    @SerializedName("password")
    String password;

    @SerializedName("passwordResetToken")
    String passwordResetToken;

    @SerializedName("facebook")
    String facebook;

    @SerializedName("twitter")
    String twitter;

    @SerializedName("google")
    String google;

    @SerializedName("tokens")
    List<String> tokens;

    @SerializedName("profile")
    Profile profile;

    @SerializedName("cars")
    List<String> cars;

    @SerializedName("_id")
    String id;

    @SerializedName("createdAt")
    String createdAt;

    @SerializedName("updatedAt")
    String updatedAt;

    @SerializedName("__v")
    String __v;
}

class Profile {
    @SerializedName("name")
    String name;

    @SerializedName("gender")
    String gender;

    @SerializedName("location")
    String location;

    @SerializedName("website")
    String website;

    @SerializedName("picture")
    String picture;
}
