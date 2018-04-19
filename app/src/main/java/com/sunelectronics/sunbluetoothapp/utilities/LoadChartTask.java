package com.sunelectronics.sunbluetoothapp.utilities;

import android.os.AsyncTask;

import com.github.mikephil.charting.data.LineData;
import com.sunelectronics.sunbluetoothapp.interfaces.ChartDataCallback;

/**
 * Task that takes in a String (from a file) and extracts the chart data. Returns a LineData
 * object that is used to populate chart
 */

public class LoadChartTask extends AsyncTask<String, Integer, LineData> {

    private static final String TAG = "LoadChartTask";
    //interface that essentially represents the fragment that runs this task
    private ChartDataCallback chartDataCallback;

    public LoadChartTask(ChartDataCallback chartDataCallback) {
        this.chartDataCallback = chartDataCallback;
    }

    @Override
    protected void onPreExecute() {
        chartDataCallback.initialize();
    }

    @Override
    protected LineData doInBackground(String... params) {
        String fileContents = params[0];
        return ChartUtilityHelperClass.getLineDataFromFileContents(fileContents);
    }

    @Override
    protected void onPostExecute(LineData lineData) {
        chartDataCallback.setLineData(lineData);
    }
}