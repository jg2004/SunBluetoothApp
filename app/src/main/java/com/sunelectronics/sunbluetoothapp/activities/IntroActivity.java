package com.sunelectronics.sunbluetoothapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.fragments.IntroFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LogFileListFragment;

import static android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGING_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_INTRO_FRAGMENT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_LOGGER;

public class IntroActivity extends AppCompatActivity implements LogFileListFragment.DeleteLogFileListener {
    private static final String TAG = "IntroActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d(TAG, "PIXEL WIDTH: " + getResources().getDisplayMetrics().widthPixels);
        Log.d(TAG, "PIXEL HEIGHT: " + getResources().getDisplayMetrics().heightPixels);
        Log.d(TAG, "PIXEL DENSITY: " + getResources().getDisplayMetrics().density);
        Log.d(TAG, "SCREEN DENSITY (dpi): " + getResources().getDisplayMetrics().densityDpi);
        Log.d(TAG, "resetting SharedPrefs " + getPackageName());
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        prefs.edit().putBoolean(LOGGING_STATE, false).apply();

        if (savedInstanceState == null) {
            performTransaction(new IntroFragment(), TAG_FRAGMENT_INTRO_FRAGMENT, false);
        }
    }

    public void performTransaction(Fragment fragment, String tag, boolean addTobackStack) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (addTobackStack) fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.homeContainer, fragment, tag).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {

        Log.d(TAG, "onSupportNavigateUp: was called bro");
        getSupportFragmentManager().popBackStack(null, POP_BACK_STACK_INCLUSIVE);
        return true;
    }

    @Override
    public void deleteLogFile(String fileName) {
        Log.d(TAG, "deleteLogFile: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        LogFileListFragment fragment = (LogFileListFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_LOGGER);
        //LogFileListFragment has deleteLogFileMethod
        fragment.deleteLogFile(fileName);
    }

    @Override
    public void deleteAllLogFiles() {
        Log.d(TAG, "deleteAllLogFiles: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        LogFileListFragment fragment = (LogFileListFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_LOGGER);
        //LogFileListFragment has deleteLogFilesMethod
        fragment.deleteAllLogFiles();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called, closing bluetooth connection");
        //clean up: cancel bluetooth thread
        BluetoothConnectionService.getInstance().stop();

        Log.d(TAG, "storing saved preference in: " + getPackageName());
        super.onDestroy();
    }


}
