package com.sunelectronics.sunbluetoothapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.fragments.DisplayTemperatureFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    List<Fragment> mFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mFragmentList.add(new DisplayTemperatureFragment());
        switchFragment(0);
    }

    private void switchFragment(int position) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.homeContainer, mFragmentList.get(position)).commit();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: closing bluetooth connection");
        BluetoothConnectionService.getInstance(HomeActivity.this).cancel();
        super.onDestroy();
    }
}
