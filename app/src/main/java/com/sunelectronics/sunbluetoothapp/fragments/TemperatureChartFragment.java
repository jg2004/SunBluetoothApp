package com.sunelectronics.sunbluetoothapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunelectronics.sunbluetoothapp.R;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHART_DATA;


public class TemperatureChartFragment extends Fragment {
    private static final String TAG = "TemperatureChartFragmen";
    private String mLogFileContents;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        return inflater.inflate(R.layout.fragment_chart, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        mLogFileContents = getArguments().getString(CHART_DATA);
        Log.d(TAG, "File contents: " + mLogFileContents);

    }
}
