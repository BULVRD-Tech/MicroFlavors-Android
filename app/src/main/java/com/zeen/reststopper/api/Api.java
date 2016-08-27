package com.zeen.reststopper.api;

import com.zeen.reststopper.models.RestStop;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by davidhodge on 8/9/15.
 */
public interface Api {

    @GET("/values")
    void getData(Callback<List<RestStop>> callback);

}
