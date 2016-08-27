package com.zeen.reststopper.activity;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by davidhodge on 8/9/15.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= 21) {
            finishAfterTransition();
        }else{
            finish();
        }
    }
}
