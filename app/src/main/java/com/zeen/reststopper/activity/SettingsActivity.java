package com.zeen.reststopper.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.zeen.reststopper.MainApp;
import com.zeen.reststopper.R;
import com.zeen.reststopper.models.CustomLoc;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by davidhodge on 8/9/15.
 */
public class SettingsActivity extends BaseActivity {

    @Bind(R.id.settings_show_all)
    CheckBox showAll;
    @Bind(R.id.settings_mes)
    CheckBox useMes;
    @Bind(R.id.settings_distance_enter)
    EditText distanceEnter;
    @Bind(R.id.settings_distance_test)
    TextView distanceText;

    @Bind(R.id.settings_custom_loc)
    CheckBox useCustomLoc;
    @Bind(R.id.settings_custom_loc_text)
    TextView customLocText;
    @Bind(R.id.settings_custom_loc_enter)
    EditText customLocEnter;

    @Bind(R.id.settings_sat)
    CheckBox settingSat;

    Context mContext;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    ActionBar actionBar;
    CustomLoc customLoc;
    Gson mGson;

    Tracker t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        mContext = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = sharedPreferences.edit();
        t = ((MainApp) getApplication()).getTracker(MainApp.TrackerName.APP_TRACKER);
        mGson = new Gson();

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.ab_settings));

        //show all locations on map logic
        showAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    distanceEnter.setEnabled(false);

                    distanceText.setTextColor(getResources().getColor(R.color.black_overlay));
                    distanceText.setTextColor(getResources().getColor(R.color.black_overlay));

                } else {
                    distanceEnter.setEnabled(true);

                    distanceEnter.setTextColor(getResources().getColor(android.R.color.black));
                    distanceText.setTextColor(getResources().getColor(android.R.color.black));

                }
                editor.putBoolean("show_all", isChecked);
                editor.apply();
            }
        });
        showAll.setChecked(sharedPreferences.getBoolean("show_all", false));
        if (!sharedPreferences.getBoolean("show_all", false)) {
            distanceEnter.setEnabled(true);
            distanceEnter.setFocusable(true);

            distanceEnter.setTextColor(getResources().getColor(android.R.color.black));
            distanceText.setTextColor(getResources().getColor(android.R.color.black));
        }

        //use custom location logic
        useCustomLoc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    customLocEnter.setEnabled(true);
                    customLocEnter.setFocusable(true);

                    customLocEnter.setTextColor(getResources().getColor(android.R.color.black));
                    customLocText.setTextColor(getResources().getColor(android.R.color.black));

                } else {

                    customLocEnter.setEnabled(false);
                    customLocEnter.setFocusable(false);

                    customLocEnter.setTextColor(getResources().getColor(R.color.black_overlay));
                    customLocText.setTextColor(getResources().getColor(R.color.black_overlay));
                }

                editor.putBoolean("cust_loc", isChecked);
                editor.apply();
            }
        });

        useCustomLoc.setChecked(sharedPreferences.getBoolean("cust_loc", false));
        if (sharedPreferences.getBoolean("cust_loc", false)) {
            customLocEnter.setEnabled(true);
            customLocEnter.setFocusable(true);

            customLocEnter.setTextColor(getResources().getColor(android.R.color.black));
            customLocText.setTextColor(getResources().getColor(android.R.color.black));
            try {
                customLoc = mGson.fromJson(sharedPreferences.getString("loc_data", null), CustomLoc.class);
                customLocEnter.setText("" + customLoc.lat + ", " + customLoc.lon);
            } catch (Exception e) {

            }
        } else {
            customLocEnter.setEnabled(false);
            customLocEnter.setFocusable(false);

            customLocEnter.setTextColor(getResources().getColor(R.color.black_overlay));
            customLocText.setTextColor(getResources().getColor(R.color.black_overlay));
        }

        customLocEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pick = new Intent(mContext, PickCustomLocActivity.class);
                startActivityForResult(pick, 888);
            }
        });

        //use metric measurements logic
        useMes.setChecked(sharedPreferences.getBoolean("mes_speed", false));
        useMes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("mes_speed", isChecked);
                editor.apply();
            }
        });

        //use metric measurements logic
        settingSat.setChecked(sharedPreferences.getBoolean("sat_map", false));
        settingSat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("sat_map", isChecked);
                editor.apply();
            }
        });

        //custom distance visibility logic
        distanceEnter.setText(Integer.toString(sharedPreferences.getInt("search_dis", 100)));
        distanceEnter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    editor.putInt("search_dis", Integer.parseInt(s.toString()));
                    editor.apply();
                } else {
                    editor.putInt("search_dis", 100);
                    editor.apply();
                }
            }
        });

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
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (Build.VERSION.SDK_INT >= 21) {
            startActivity(i, ActivityOptionsCompat.makeSceneTransitionAnimation(SettingsActivity.this).toBundle());
        } else {
            startActivity(i);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 888) {
            if (resultCode == RESULT_OK) {
                CustomLoc customLoc = new CustomLoc();
                customLoc.lat = data.getDoubleExtra("lat", 0);
                customLoc.lon = data.getDoubleExtra("lon", 0);

                Gson mGson = new Gson();
                editor.putString("loc_data", mGson.toJson(customLoc));
                editor.apply();
                customLocEnter.setText("" + customLoc.lat + ", " + customLoc.lon);
            } else {
                //Do nothing, dur
                Toast.makeText(mContext, "Canceled Search", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
