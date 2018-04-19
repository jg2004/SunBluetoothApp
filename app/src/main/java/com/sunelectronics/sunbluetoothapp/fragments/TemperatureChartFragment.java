package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.LineData;
import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.interfaces.ChartDataCallback;
import com.sunelectronics.sunbluetoothapp.utilities.ChartUtilityHelperClass;
import com.sunelectronics.sunbluetoothapp.utilities.LoadChartTask;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHART_DATA;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHART_TITLE;

public class TemperatureChartFragment extends Fragment implements ChartDataCallback {
    private static final String TAG = "TemperatureChartFragmen";
    private String mLogFileContents;
    private LineChart mLineChart;
    private ActionBar mSupportActionBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        mLogFileContents = getArguments().getString(CHART_DATA);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        mLineChart = (LineChart) view.findViewById(R.id.lineChart);
        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        Log.d(TAG, "onCreateView: orientation is: " + getActivity().getResources().getConfiguration().orientation);

        if (mSupportActionBar != null && getActivity().getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mSupportActionBar.show();
            mSupportActionBar.setTitle(CHART_TITLE);
        }
        return view;
    }

    private void populateChart() {

        LoadChartTask task = new LoadChartTask(this);
        task.execute(mLogFileContents);
    }

    @Override
    public void onStart() {
        super.onStart();
        populateChart();
    }

    @Override
    public void onStop() {
        mSupportActionBar.hide();
        super.onStop();
    }

    /*******************************called from AsynTask pre,post execute methods**********************/

    @Override
    public void initialize() {
        Log.d(TAG, "initialize: called");
        //can put a progress bar in layout and set to visible here if desired
    }

    @Override
    public void setLineData(LineData lineData) {

        Log.d(TAG, "setLineData: called");
        int entryCount = lineData.getDataSets().get(0).getEntryCount();
        mLineChart.setData(lineData);
        ChartUtilityHelperClass.formatChart(mLineChart);
        Description description = ChartUtilityHelperClass.getFormattedDescription();
        String chartTitle = ChartUtilityHelperClass.getChartTitleFromFileContents(mLogFileContents);
        description.setText(chartTitle + " Points: " + entryCount);
        mLineChart.setDescription(description);
        refreshChart();
    }

    /**********************************************************************************************/

    private void refreshChart() {
        //notify  LineChart data has changed
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
    }

}