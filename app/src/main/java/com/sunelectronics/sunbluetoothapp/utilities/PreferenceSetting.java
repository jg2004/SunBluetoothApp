package com.sunelectronics.sunbluetoothapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER_ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LINE_CHART_VISIBILITY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGER_START_TIME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGING_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.START_TIME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SWITCH_STATE;

public class PreferenceSetting {
    private static final String PREF_CONT_TYPE = "pref_controller_type";

    public static String getControllerType(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(PREF_CONT_TYPE, "");
    }

    public static void storeLiveChartVisibility(Context context, boolean isVisible) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(LINE_CHART_VISIBILITY, isVisible).apply();

    }

    public static boolean getLiveChartVisibility(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(LINE_CHART_VISIBILITY, false);
    }


    public static void storeLoggingState(Context context, boolean isLoggingData) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(LOGGING_STATE, isLoggingData).apply();
    }

    public static boolean getLoggingState(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(LOGGING_STATE, false);
    }

    public static void storeSwitchState(Context context, boolean checked) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(SWITCH_STATE, checked).apply();
    }

    public static boolean getSwitchState(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(SWITCH_STATE, false);
    }

    public static void storeFileName(Context context, String fileName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(FILE_NAME, fileName).apply();
    }

    public static String getFileName(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(FILE_NAME, null);
    }

    public static void storeControllerOn(Context context, boolean controllerOn) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(CONTROLLER_ON, controllerOn).apply();
    }

    public static boolean getControllerOn(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(CONTROLLER_ON, false);
    }

    public static void storeStartTime(Context context, long liveChartStartTime) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong(START_TIME, liveChartStartTime).apply();
    }

    public static long getStartTime(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(START_TIME, 0);
    }

    public static void storeLoggerStartTime(Context context, long loggerStartTime) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong(LOGGER_START_TIME, loggerStartTime).apply();
    }

    public static long getLoggerStartTime(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(LOGGER_START_TIME, 0);
    }
}
