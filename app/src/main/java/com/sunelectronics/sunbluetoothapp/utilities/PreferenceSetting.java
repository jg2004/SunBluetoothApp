package com.sunelectronics.sunbluetoothapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

public class PreferenceSetting {
    private static final String PREF_CONT_TYPE = "pref_controller_type";

    public static String getControllerType(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PREF_CONT_TYPE, "");
    }
}
