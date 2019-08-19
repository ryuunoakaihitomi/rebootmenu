package com.ryuunoakaihitomi.rebootmenu.csc_compat;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by ZQY on 2019/8/18.
 */

public class GmsApiWrapper {

    /**
     * 取Google Play Service可用性状态
     *
     * @param context context
     * @return String
     */
    public static String fetchStateString(Context context) {
        switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context.getApplicationContext())) {
            case ConnectionResult.SUCCESS:
                return "SUCCESS";
            case ConnectionResult.SERVICE_MISSING:
                return "SERVICE_MISSING";
            case ConnectionResult.SERVICE_UPDATING:
                return "SERVICE_UPDATING";
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                return "SERVICE_VERSION_UPDATE_REQUIRED";
            case ConnectionResult.SERVICE_DISABLED:
                return "SERVICE_DISABLED";
            case ConnectionResult.SERVICE_INVALID:
                return "SERVICE_INVALID";
        }
        return null;
    }
}
