package com.zeen.reststopper.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.listeners.OnInfoWindowClickListener;
import com.airbnb.android.airmapview.listeners.OnMapClickListener;
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.zeen.reststopper.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by davidhodge on 8/19/15.
 */
public class PickCustomLocActivity extends BaseActivity implements OnMapInitializedListener, OnMapClickListener, OnMapMarkerClickListener, OnInfoWindowClickListener {

    @Bind(R.id.map)
    AirMapView mapView;
    Context mContext;
    SharedPreferences sharedPreferences;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_loc);
        ButterKnife.bind(this);
        mContext = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.ab_pick_loc));

        mapView.setOnMapInitializedListener(this);
        mapView.setOnMapClickListener(this);
        mapView.setOnInfoWindowClickListener(this);
        mapView.setOnMarkerClickListener(this);
        mapView.initialize(getSupportFragmentManager());
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Intent none = new Intent();
        setResult(RESULT_CANCELED, none);
        finish();
    }

    @Override
    public void onInfoWindowClick(long l) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Intent none = new Intent();
        none.putExtra("lat", latLng.latitude);
        none.putExtra("lon", latLng.longitude);
        setResult(RESULT_OK, none);
        finish();
    }

    @Override
    public void onMapInitialized() {
        final LatLng uLatLng = new LatLng(40.181936, -99.246543);

        mapView.setMyLocationEnabled(false);
        mapView.animateCenterZoom(uLatLng, 4);
    }

    @Override
    public void onMapMarkerClick(long l) {

    }

    @Override
    public void onMapMarkerClick(Marker marker) {

    }
}
