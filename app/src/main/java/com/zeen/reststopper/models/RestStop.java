package com.zeen.reststopper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by davidhodge on 8/9/15.
 */
public class RestStop {

    @Expose
    @SerializedName("FIELD1")
    public Double lon;
    @Expose
    @SerializedName("FIELD2")
    public Double lat;
    @Expose
    @SerializedName("FIELD3")
    public String location;
    @Expose
    @SerializedName("FIELD4")
    public String info;
    @SerializedName("distance")
    public float distance;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

}
