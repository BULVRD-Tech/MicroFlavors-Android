package com.zeen.reststopper.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.zeen.reststopper.MainApp;
import com.zeen.reststopper.R;
import com.zeen.reststopper.utils.Constants;
import com.zeen.reststopper.utils.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by davidhodge on 8/9/15.
 */
public class AboutActivity extends BaseActivity {

    @Bind(R.id.about_version_text)
    TextView aboutVersionText;
    @Bind(R.id.about_zeen_remove)
    TextView aboutZeenRemove;
    @Bind(R.id.about_zeen_divider)
    View aboutZeenDivier;
    @Bind(R.id.about_zeen_dev)
    TextView aboutZeenDev;
    @Bind(R.id.about_email)
    TextView aboutEmail;
    @Bind(R.id.about_twitter)
    TextView aboutTwitter;
    @Bind(R.id.about_carto)
    ImageView aboutCarto;

    Context mContext;
    ActionBar actionBar;
    int status;

    Tracker t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        mContext = this;

        t = ((MainApp) getApplication()).getTracker(MainApp.TrackerName.APP_TRACKER);

        actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.ab_about));

        status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        aboutVersionText.setText(Utils.getFullVersion(mContext));
        aboutVersionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (Utils.appInstalled(mContext, Constants.ZEEN_PACKAGE)) {
            aboutZeenRemove.setVisibility(View.GONE);
            aboutZeenDivier.setVisibility(View.GONE);
        } else {
            aboutZeenRemove.setVisibility(View.VISIBLE);
            aboutZeenDivier.setVisibility(View.VISIBLE);
            aboutZeenRemove.setOnClickListener(new View.OnClickListener() {
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

        aboutZeenDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == ConnectionResult.SUCCESS) {
                    Utils.urlIntent(mContext, Constants.ZEEN_DEV_PLAY);
                } else {
                    Utils.urlIntent(mContext, Constants.ZEEN_DEV_AMAZON);
                }
            }
        });

        aboutEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", Constants.CONTACT_EMAIL, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        aboutTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urlIntent(Constants.CONTACT_TWITTER);
            }
        });

        aboutCarto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.urlIntent(mContext, Constants.SITE_CARTO);
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
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void urlIntent(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
