package com.zeen.reststopper.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by davidhodge on 8/19/15.
 */
public class CustomLoc {

    @SerializedName("lat")
    public Double lat;

    @SerializedName("lon")
    public Double lon;

    @SerializedName("name")
    public String name;
}
