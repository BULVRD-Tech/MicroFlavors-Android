package com.zeen.reststopper.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.zeen.reststopper.R;

/**
 * Created by davidhodge on 8/9/15.
 */
public class Utils {

    public static boolean isNetworkConnectionAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    public static void urlIntent(Context mContext, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        mContext.startActivity(i);
    }

    public static boolean appInstalled(Context mContext, String uri) {
        PackageManager pm = mContext.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static String getFullVersion(Context context) {
        return getVersionFormat(context, R.string.full_version_text);
    }

    public static String getVersionFormat(Context context, int stringResId) {
        String versionString = "";
        int versionCode = 0;
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        try {
            versionString = pm.getPackageInfo(packageName, 0).versionName;
            versionCode = pm.getPackageInfo(packageName, 0).versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            //Failed
        }
        return String.format(context.getResources().getString(stringResId), versionString,
                versionCode);
    }
}
