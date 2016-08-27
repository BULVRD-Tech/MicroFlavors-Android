package com.zeen.reststopper.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.airbnb.android.airmapview.AirMapInterface;
import com.airbnb.android.airmapview.AirMapMarker;
import com.airbnb.android.airmapview.AirMapView;
import com.airbnb.android.airmapview.AirMapViewTypes;
import com.airbnb.android.airmapview.DefaultAirMapViewBuilder;
import com.airbnb.android.airmapview.MapType;
import com.airbnb.android.airmapview.listeners.OnInfoWindowClickListener;
import com.airbnb.android.airmapview.listeners.OnMapClickListener;
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener;
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.kobakei.ratethisapp.RateThisApp;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.melnykov.fab.FloatingActionButton;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.parse.ParseObject;
import com.zeen.reststopper.MainApp;
import com.zeen.reststopper.R;
import com.zeen.reststopper.adapter.RestStopsAdapter;
import com.zeen.reststopper.api.Api;
import com.zeen.reststopper.api.LocalJsonClient;
import com.zeen.reststopper.models.CustomLoc;
import com.zeen.reststopper.models.RestStop;
import com.zeen.reststopper.models.ip.IpResponse;
import com.zeen.reststopper.utils.Constants;
import com.zeen.reststopper.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;
import permissions.dispatcher.DeniedPermissions;
import permissions.dispatcher.NeedsPermissions;
import permissions.dispatcher.RuntimePermissions;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

@RuntimePermissions
public class MainActivity extends BaseActivity implements OnMapInitializedListener, OnMapClickListener, OnMapMarkerClickListener, OnInfoWindowClickListener {

    @Bind(R.id.map)
    AirMapView mapView;
    @Bind(R.id.loc_list)
    ListView locList;
    @Bind(R.id.zeen_holder)
    LinearLayout zeenHolder;
    @Bind(R.id.float_list)
    FloatingActionButton floatList;
    @Bind(R.id.loading)
    CircularProgressBar loading;

    Context mContext;
    Double lat, lon;
    RestAdapter restAdapter;
    LocalJsonClient localJsonClient;
    private Api api;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    List<RestStop> restStops;
    List<RestStop> restStopList = new ArrayList<>();
    AlertDialog restStopsDialog;
    RestStopsAdapter restStopsAdapter;

    boolean locationEnabled = true;
    boolean useMetric = false;
    boolean showAll = false;
    boolean useCustomLoc = false;
    boolean zeenInstalled = false;
    boolean useSatMaps = false;
    boolean networkAvailable = true;

    public static String PACKAGE_NAME;
    AddToMapAsyncTask addToMapAsyncTask;

    Gson mGson;
    CustomLoc customLoc;

    int searchDis, searchDisMeters;
    int status;

    Tracker t;

    AirMapInterface airMapInterface;
    DefaultAirMapViewBuilder mapViewBuilder;

    String android_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = sharedPreferences.edit();
        t = ((MainApp) getApplication()).getTracker(MainApp.TrackerName.APP_TRACKER);

        useMetric = sharedPreferences.getBoolean("mes_speed", false);
        useSatMaps = sharedPreferences.getBoolean("sat_map", false);
        showAll = sharedPreferences.getBoolean("show_all", false);
        useCustomLoc = sharedPreferences.getBoolean("cust_loc", false);
        searchDis = sharedPreferences.getInt("search_dis", 100);
        searchDisMeters = (int) (searchDis / 0.000621);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        mGson = new Gson();

        localJsonClient = new LocalJsonClient(mContext);

        localJsonClient.setScenario("/values");
        restAdapter = new RestAdapter.Builder()
                .setClient(localJsonClient)
                .setEndpoint(Constants.BASE_URL)
                .build();
        api = restAdapter.create(Api.class);

        status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        editor.putString("id", android_id);
        editor.apply();

        loading.setVisibility(View.VISIBLE);
        mapView.setOnMapInitializedListener(this);
        mapView.setOnMapClickListener(this);
        mapView.setOnInfoWindowClickListener(this);
        mapView.setOnMarkerClickListener(this);

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction("View Main").setCategory(getString(R.string.app_name)).setLabel("Main");
        t.send(eventBuilder.build());

        if (useCustomLoc) {
            if (sharedPreferences.getString("loc_data", null) != null) {
                useCustomLocation();
            } else {
                MainActivityPermissionsDispatcher.getUserLocationWithCheck(MainActivity.this);
            }
        } else {
            MainActivityPermissionsDispatcher.getUserLocationWithCheck(MainActivity.this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);

        // Monitor launch times and interval from installation
        RateThisApp.onStart(this);
        // If the criteria is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settings = new Intent(mContext, SettingsActivity.class);
            if (Build.VERSION.SDK_INT >= 21) {
                startActivity(settings, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            } else {
                startActivity(settings);
            }
        } else if (id == R.id.action_about) {
            Intent about = new Intent(mContext, AboutActivity.class);
            if (Build.VERSION.SDK_INT >= 21) {
                startActivity(about, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            } else {
                startActivity(about);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        networkAvailable = Utils.isNetworkConnectionAvailable(mContext);

        if (networkAvailable){
            getIpData();
        }

        zeenInstalled = Utils.appInstalled(mContext, Constants.ZEEN_PACKAGE);
        if (zeenInstalled) {
            zeenHolder.setVisibility(View.GONE);
        } else {
            zeenHolder.setVisibility(View.VISIBLE);
            zeenHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (status == ConnectionResult.SUCCESS) {
                        Answers.getInstance().logCustom(new CustomEvent("Zeen").putCustomAttribute("store", "Play"));
                        Utils.urlIntent(mContext, Constants.ZEEN_APP_URL);
                    } else {
                        Answers.getInstance().logCustom(new CustomEvent("Zeen").putCustomAttribute("store", "Amazon"));
                        Utils.urlIntent(mContext, Constants.ZEEN_APP_URL_AMAZON);
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SmartLocation.with(mContext).location().stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SmartLocation.with(mContext).location().stop();
        if (addToMapAsyncTask != null) {
            addToMapAsyncTask.cancel(true);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapInitialized() {


        if (TextUtils.equals(PACKAGE_NAME, Constants.FESTIVAL_PACKAGE)) {

        } else {
            if (useSatMaps) {
                mapView.setMapType(MapType.MAP_TYPE_SATELLITE);
            } else {
                mapView.setMapType(MapType.MAP_TYPE_NORMAL);
            }
        }

        final LatLng uLatLng = new LatLng(lat, lon);

        mapView.setMyLocationEnabled(false);
        mapView.animateCenterZoom(uLatLng, 10);

        mapView.addMarker(new AirMapMarker.Builder().id(001).position(uLatLng).title(getString(R.string.map_cust_loc)).iconId(R.drawable.ic_users_loc_g).build());
        queryValues();
    }

    private void useCustomLocation() {

        customLoc = mGson.fromJson(sharedPreferences.getString("loc_data", null), CustomLoc.class);
        lat = customLoc.lat;
        lon = customLoc.lon;

        if (TextUtils.equals(PACKAGE_NAME, Constants.FESTIVAL_PACKAGE)) {
            mapViewBuilder = new DefaultAirMapViewBuilder(this);
            airMapInterface = mapViewBuilder.builder(AirMapViewTypes.WEB).build();
            mapView.initialize(getSupportFragmentManager(), airMapInterface);
        } else {
            mapView.initialize(getSupportFragmentManager());
        }

    }

    @NeedsPermissions({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void getUserLocation() {
        SmartLocation.with(mContext)
                .location(new LocationGooglePlayServicesWithFallbackProvider(mContext))
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();

                        if (TextUtils.equals(PACKAGE_NAME, Constants.FESTIVAL_PACKAGE)) {
                            mapViewBuilder = new DefaultAirMapViewBuilder(MainActivity.this);
                            airMapInterface = mapViewBuilder.builder(AirMapViewTypes.WEB).build();
                            mapView.initialize(getSupportFragmentManager(), airMapInterface);
                        } else {
                            mapView.initialize(getSupportFragmentManager());
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.
                onRequestPermissionsResult(MainActivity.this, requestCode, grantResults);
    }

    @DeniedPermissions({Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    void showDeniedForLocationSensors() {
        Toast.makeText(mContext, getString(R.string.app_name) + " requires Location and storage permissions to deliver the best experience! Enable location permissions for an improved experience!", Toast.LENGTH_LONG).show();
        if (networkAvailable) {
            getIpDataLocation();
        }
    }

    private void queryValues() {
        api.getData(new Callback<List<RestStop>>() {
            @Override
            public void success(List<RestStop> restStopsresponse, Response response) {
                    int size = restStopsresponse.size();
                    if (size > 0) {
                        restStops = restStopsresponse;
                        addToMapAsyncTask = new AddToMapAsyncTask();
                        addToMapAsyncTask.execute();
                    } else {
                        showErrorSnack(getString(R.string.snack_nothing_found), getString(R.string.snack_error_okay), Snackbar.SnackbarDuration.LENGTH_LONG);
                    }
            }

            @Override
            public void failure(RetrofitError error) {
                if (error != null) {
                    Log.e("error", error.toString());
                    showErrorSnack(getString(R.string.snack_error_load), getString(R.string.snack_error_okay), Snackbar.SnackbarDuration.LENGTH_LONG);
                }
                loading.setVisibility(View.GONE);
            }
        });


        ParseObject parseObject = new ParseObject("extra_locs");
        parseObject.put("lat", lat);
        parseObject.put("lon", lon);
        parseObject.put("app_package", PACKAGE_NAME);
        parseObject.put("app_name", getString(R.string.app_name));
        if (status == ConnectionResult.SUCCESS) {
            parseObject.put("store", "Play");
        } else {
            parseObject.put("store", "Amazon");
        }
        parseObject.put("device", Build.DEVICE);
        parseObject.put("manufacturer", Build.MANUFACTURER);
        parseObject.put("model", Build.MODEL);
        parseObject.put("os", Build.VERSION.SDK_INT);
        parseObject.put("platform", Constants.PLATFORM);

        parseObject.put("zeenInstalled", zeenInstalled);
        parseObject.put("deviceId", android_id);
        parseObject.put("useMetric", useMetric);
        parseObject.saveEventually();

    }

    private String setUpMeasure(float distance) {
        String distanceText = "";
        if (useMetric) {
            distanceText = new StringBuilder().append(Math.round(distance * 0.001)).append(" km away").toString();
        } else {
            distanceText = new StringBuilder().append(Math.round(distance * 0.000621371)).append(" mi away").toString();
        }
        return distanceText;
    }

    private void addToMap(LatLng latLng, int value, String distance, String extra) {
        if (TextUtils.equals(PACKAGE_NAME, Constants.HISTORY_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).snippet(extra).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.THEME_PARK_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).snippet(extra).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.BREWS_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).snippet(extra).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.MURICA_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).snippet(extra).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.CASTLE_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).snippet(extra).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.WINE_PACKAGE)) {
            if (!TextUtils.equals("Winery", extra)) {
                mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).snippet(extra).build());

            } else {
                mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).build());

            }
        } else if (TextUtils.equals(PACKAGE_NAME, Constants.ZIPCODE_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).snippet(extra).bitmap(textAsBitmap(extra, 30, Color.BLACK)).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.FESTIVAL_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).snippet(extra).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.HORSE_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(extra).snippet(distance).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.WATERFALL_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(extra).snippet(distance).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.CAVES_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(extra).snippet(distance).build());

        } else if (TextUtils.equals(PACKAGE_NAME, Constants.FISHING_PIERS_PACKAGE)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(extra).snippet(distance).build());

        }else if (TextUtils.equals(PACKAGE_NAME, Constants.PIZZA_JOINTS)) {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(extra).snippet(distance).build());

        } else {
            mapView.addMarker(new AirMapMarker.Builder().id(value).position(latLng).title(distance).build());

        }
    }

    private void showErrorSnack(String errorText, String btnText, Snackbar.SnackbarDuration duration) {
        SnackbarManager.show(
                Snackbar.with(mContext)
                        .type(SnackbarType.MULTI_LINE)
                        .color(Color.RED)
                        .textColor(Color.WHITE)
                        .actionColor(Color.WHITE)
                        .text(errorText)
                        .actionLabel(btnText)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                snackbar.dismiss();
                            }
                        })
                        .duration(duration)
                        .animation(true));
    }

    @Override
    public void onMapMarkerClick(long l) {
        //required, but not needed/used
        if (l != 001) {

            ParseObject parseObject1 = new ParseObject("marker_loc");
            parseObject1.put("deviceId", android_id);
            parseObject1.put("package", PACKAGE_NAME);
            parseObject1.put("app_name", getString(R.string.app_name));
            parseObject1.put("user_loc_lat", lat);
            parseObject1.put("user_loc_lon", lon);
            parseObject1.put("data_loc_lat", restStopList.get((int) l).lat);
            parseObject1.put("data_loc_lon", restStopList.get((int) l).lon);

            parseObject1.put("device", Build.DEVICE);
            parseObject1.put("manufacturer", Build.MANUFACTURER);
            parseObject1.put("model", Build.MODEL);
            parseObject1.put("os", Build.VERSION.SDK_INT);
            parseObject1.put("platform", Constants.PLATFORM);

            if (status == ConnectionResult.SUCCESS) {
                parseObject1.put("store", "Play");
            } else {
                parseObject1.put("store", "Amazon");
            }

            parseObject1.saveEventually();
        }
    }

    @Override
    public void onMapMarkerClick(Marker marker) {
        try {
            if (!TextUtils.equals("001", marker.getId())) {

                String digits = marker.getId().replaceAll("[^0-9.]", "");
                int index = (Integer.parseInt(digits) - 1);

                ParseObject parseObject1 = new ParseObject("marker_loc");
                parseObject1.put("deviceId", android_id);
                parseObject1.put("package", PACKAGE_NAME);
                parseObject1.put("app_name", getString(R.string.app_name));
                parseObject1.put("user_loc_lat", lat);
                parseObject1.put("user_loc_lon", lon);
                parseObject1.put("data_loc_lat", restStopList.get(index).lat);
                parseObject1.put("data_loc_lon", restStopList.get(index).lon);

                parseObject1.put("device", Build.DEVICE);
                parseObject1.put("manufacturer", Build.MANUFACTURER);
                parseObject1.put("model", Build.MODEL);
                parseObject1.put("os", Build.VERSION.SDK_INT);
                parseObject1.put("platform", Constants.PLATFORM);

                if (status == ConnectionResult.SUCCESS) {
                    parseObject1.put("store", "Play");
                } else {
                    parseObject1.put("store", "Amazon");
                }

                parseObject1.saveEventually();
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    @Override
    public void onInfoWindowClick(long l) {
        if (l != 001) {

            ParseObject parseObject1 = new ParseObject("window_loc");
            parseObject1.put("deviceId", android_id);
            parseObject1.put("package", PACKAGE_NAME);
            parseObject1.put("app_name", getString(R.string.app_name));
            parseObject1.put("user_loc_lat", lat);
            parseObject1.put("user_loc_lon", lon);
            parseObject1.put("data_loc_lat", restStopList.get((int) l).lat);
            parseObject1.put("data_loc_lon", restStopList.get((int) l).lon);

            parseObject1.put("device", Build.DEVICE);
            parseObject1.put("manufacturer", Build.MANUFACTURER);
            parseObject1.put("model", Build.MODEL);
            parseObject1.put("os", Build.VERSION.SDK_INT);
            parseObject1.put("platform", Constants.PLATFORM);

            if (status == ConnectionResult.SUCCESS) {
                parseObject1.put("store", "Play");
            } else {
                parseObject1.put("store", "Amazon");
            }

            parseObject1.saveEventually();

            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
            eventBuilder.setAction("" + restStopList.get((int) l).lat + " : " + restStopList.get((int) l).lon).setCategory(getString(R.string.app_name)).setLabel("Marker Click");
            t.send(eventBuilder.build());

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + restStopList.get((int) l).lat + "," + restStopList.get((int) l).lon));
                mContext.startActivity(intent);
            } catch (Exception e) {
                if (status == ConnectionResult.SUCCESS) {
                    Utils.urlIntent(mContext, Constants.ZEEN_APP_URL);
                } else {
                    Utils.urlIntent(mContext, Constants.ZEEN_APP_URL_AMAZON);
                }
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        try {
            if (!TextUtils.equals("001", marker.getId())) {

                String digits = marker.getId().replaceAll("[^0-9.]", "");
                int index = (Integer.parseInt(digits) - 1);

                ParseObject parseObject1 = new ParseObject("window_loc");
                parseObject1.put("deviceId", android_id);
                parseObject1.put("package", PACKAGE_NAME);
                parseObject1.put("app_name", getString(R.string.app_name));
                parseObject1.put("user_loc_lat", lat);
                parseObject1.put("user_loc_lon", lon);
                parseObject1.put("data_loc_lat", restStopList.get(index).lat);
                parseObject1.put("data_loc_lon", restStopList.get(index).lon);

                parseObject1.put("device", Build.DEVICE);
                parseObject1.put("manufacturer", Build.MANUFACTURER);
                parseObject1.put("model", Build.MODEL);
                parseObject1.put("os", Build.VERSION.SDK_INT);
                parseObject1.put("platform", Constants.PLATFORM);

                if (status == ConnectionResult.SUCCESS) {
                    parseObject1.put("store", "Play");
                } else {
                    parseObject1.put("store", "Amazon");
                }

                parseObject1.saveEventually();

                HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
                eventBuilder.setAction("" + marker.getPosition().latitude + " : " + marker.getPosition().longitude).setCategory(getString(R.string.app_name)).setLabel("Marker Click");
                t.send(eventBuilder.build());

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + marker.getPosition().latitude + "," + marker.getPosition().longitude));
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    if (status == ConnectionResult.SUCCESS) {
                        Utils.urlIntent(mContext, Constants.ZEEN_APP_URL);
                    } else {
                        Utils.urlIntent(mContext, Constants.ZEEN_APP_URL_AMAZON);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    private void showRestStopsDialog() {
        floatList.setVisibility(View.GONE);
        restStopsDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.main_dialog_title))
                .setAdapter(restStopsAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        floatList.setVisibility(View.VISIBLE);
                        mapView.animateCenterZoom(new LatLng(restStopList.get(which).lat, restStopList.get(which).lon), 13);
                    }
                }).setPositiveButton(getString(R.string.main_dialog_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        floatList.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                }).show();

    }

    public class AddToMapAsyncTask extends AsyncTask<String, String, String> {

        public AddToMapAsyncTask() {

        }

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            int size = restStops.size();
            for (int i = 0; i < size; i++) {
                if ((restStops.get(i).lat != null) && (restStops.get(i).lon != null)) {
                    float[] dist = new float[1];
                    Location.distanceBetween(lat, lon, restStops.get(i).lat, restStops.get(i).lon, dist);
                    restStops.get(i).setDistance(dist[0]);

                    if (showAll) {
                        restStopList.add(restStops.get(i));
                    } else {
                        if (restStops.get(i).getDistance() < searchDisMeters) {
                            restStopList.add(restStops.get(i));
                        }
                    }
                }
            }

            Collections.sort(restStopList, new Comparator<RestStop>() {
                @Override
                public int compare(RestStop lhs, RestStop rhs) {
                    return Float.compare(lhs.distance, rhs.distance);
                }
            });

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            int size = restStopList.size();

            if (size == 0) {
                showErrorSnack(getString(R.string.snack_nothing_found), getString(R.string.snack_error_okay), Snackbar.SnackbarDuration.LENGTH_LONG);
            } else {
                for (int i = 0; i < size; i++) {
                    LatLng latLng = new LatLng(restStopList.get(i).lat, restStopList.get(i).lon);
                    if (TextUtils.equals(PACKAGE_NAME, Constants.HORSE_PACKAGE)) {
                        addToMap(latLng, i, setUpMeasure(restStopList.get(i).distance), restStopList.get(i).location);
                    }else{
                        addToMap(latLng, i, setUpMeasure(restStopList.get(i).distance), restStopList.get(i).info);
                    }
                }

                restStopsAdapter = new RestStopsAdapter(mContext, restStopList, useMetric, PACKAGE_NAME);
                locList.setAdapter(restStopsAdapter);

                locList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mapView.animateCenterZoom(new LatLng(restStopList.get(position).lat, restStopList.get(position).lon), 13);
                    }
                });

                floatList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (restStopsDialog != null) {
                            if (!restStopsDialog.isShowing()) {
                                showRestStopsDialog();
                            }
                        } else {
                            showRestStopsDialog();
                        }
                    }
                });
            }

            loading.setVisibility(View.GONE);
        }
    }

    public Bitmap stringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public void getIpData() {
        String url = Constants.IP_BASE;

        Ion.with(mContext)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String jsonContent) {
                        if (e != null) {
                            Log.e("geo", e.toString());
                        } else {
                            try {
                                IpResponse ipResponse = new Gson().fromJson(jsonContent, IpResponse.class);
                                ParseObject parseObject = new ParseObject("ip_locs");
                                parseObject.put("status", ipResponse.getStatus());
                                parseObject.put("country", ipResponse.getCountry());
                                parseObject.put("country_code", ipResponse.getCountryCode());
                                parseObject.put("region", ipResponse.getRegion());
                                parseObject.put("region_name", ipResponse.getRegionName());
                                parseObject.put("city", ipResponse.getCity());
                                parseObject.put("zip", ipResponse.getZip());
                                parseObject.put("lat", ipResponse.getLat());
                                parseObject.put("lon", ipResponse.getLon());
                                parseObject.put("timezone", ipResponse.getTimezone());
                                parseObject.put("isp", ipResponse.getIsp());
                                parseObject.put("org", ipResponse.getOrg());
                                parseObject.put("as", ipResponse.getAs());
                                parseObject.put("query", ipResponse.getQuery());
                                parseObject.put("app_package", PACKAGE_NAME);
                                parseObject.put("app_name", getString(R.string.app_name));
                                parseObject.put("zeen_installed", zeenInstalled);
                                if (status == ConnectionResult.SUCCESS) {
                                    parseObject.put("store", "Play");
                                } else {
                                    parseObject.put("store", "Amazon");
                                }
                                float[] dist = new float[1];
                                Location.distanceBetween(lat, lon, Double.parseDouble(ipResponse.getLat()), Double.parseDouble(ipResponse.getLon()), dist);
                                parseObject.put("loc_diff", dist[0]);
                                parseObject.put("loc_lat", lat);
                                parseObject.put("loc_lon", lon);
                                parseObject.put("device_id", android_id);
                                parseObject.put("device", Build.DEVICE);
                                parseObject.put("manufacturer", Build.MANUFACTURER);
                                parseObject.put("model", Build.MODEL);
                                parseObject.put("os", Build.VERSION.SDK_INT);
                                parseObject.saveEventually();

                            } catch (Exception e1) {
                                Log.e("geo", e1.toString());
                            }
                        }
                    }
                });
    }

    public void getIpDataLocation() {
        String url = Constants.IP_BASE;

        Ion.with(mContext)
                .load(url)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String jsonContent) {
                        if (e != null) {
                            Log.e("geo", e.toString());
                            showErrorSnack(getString(R.string.snack_error_loc), getString(R.string.snack_error_okay), Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
                            finish();
                        } else {
                            try {
                                IpResponse ipResponse = new Gson().fromJson(jsonContent, IpResponse.class);

                                lat = Double.parseDouble(ipResponse.getLat());
                                lon = Double.parseDouble(ipResponse.getLon());

                                if (TextUtils.equals(PACKAGE_NAME, Constants.FESTIVAL_PACKAGE)) {
                                    mapViewBuilder = new DefaultAirMapViewBuilder(MainActivity.this);
                                    airMapInterface = mapViewBuilder.builder(AirMapViewTypes.WEB).build();
                                    mapView.initialize(getSupportFragmentManager(), airMapInterface);
                                } else {
                                    mapView.initialize(getSupportFragmentManager());
                                }

                                Location location = new Location("");
                                location.setLatitude(lat);
                                location.setLongitude(lon);
                                location.setBearing(0);
                                location.setProvider("IP");
                                location.setAccuracy(100);
                                location.setAltitude(0);
                                location.setSpeed(0);

                            } catch (Exception e1) {
                                Log.e("geo", e1.toString());
                                showErrorSnack(getString(R.string.snack_error_loc), getString(R.string.snack_error_okay), Snackbar.SnackbarDuration.LENGTH_INDEFINITE);
                                finish();
                            }
                        }
                    }
                });
    }
}
