package com.sunelectronics.sunbluetoothapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.fragments.LogFileListFragment;

import static com.sunelectronics.sunbluetoothapp.activities.HomeActivity.TAG_FRAGMENT_LOGGER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.START_DISCOVERY;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener, LogFileListFragment.DeleteLogFileListener {
    private static final String TAG = "IntroActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.GONE);
        Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(this);
        Button buttonLogViewer = (Button) findViewById(R.id.buttonViewLog);
        buttonLogViewer.setOnClickListener(this);
        Button buttonDiscover = (Button) findViewById(R.id.buttonDiscover);
        buttonDiscover.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.buttonConnect:

                Intent intentConnectBluetooth = new Intent(IntroActivity.this,BluetoothStartUp.class);
                startActivity(intentConnectBluetooth);
                break;

            case R.id.buttonViewLog:

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.homeContainer, new LogFileListFragment(),TAG_FRAGMENT_LOGGER);
                fragmentTransaction.addToBackStack(null).commit();
                break;

            case R.id.buttonDiscover:
                Intent intentDiscoverBluetooth = new Intent (IntroActivity.this, BluetoothStartUp.class);
                intentDiscoverBluetooth.putExtra(START_DISCOVERY,true);
                startActivity(intentDiscoverBluetooth);

                break;
        }
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
}
