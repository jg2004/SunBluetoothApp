package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sunelectronics.sunbluetoothapp.R;

import java.util.ArrayList;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHART_DATA;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHART_TITLE;


public class TemperatureChartFragment extends Fragment {
    private static final String TAG = "TemperatureChartFragmen";
    private String mLogFileContents;

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
        LineChart lineChart = (LineChart) view.findViewById(R.id.lineChart);
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        Log.d(TAG, "onCreateView: orientation is: " + getActivity().getResources().getConfiguration().orientation);

        if (supportActionBar != null && getActivity().getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            supportActionBar.show();
            supportActionBar.setTitle(CHART_TITLE);
        }
        populateChart(lineChart);
        return view;
    }

    private void populateChart(final LineChart lineChart) {

        String[] mLogFileContentsArray = mLogFileContents.split("\n");
        Log.d(TAG, "onCreateView: mLogFileContents split into: " + mLogFileContentsArray.length + " lines");
        String chartTitle = mLogFileContentsArray[0];
        String[] headers = mLogFileContentsArray[1].split(",");
        final String[] xValues = new String[mLogFileContentsArray.length - 2];
        ArrayList<Entry> yChamberValues = new ArrayList<>();
        ArrayList<Entry> yUserValues = new ArrayList<>();
        ArrayList<Entry> ySetValues = new ArrayList<>();
        for (int i = 0; i < mLogFileContentsArray.length - 2; i++) {

            String[] row = mLogFileContentsArray[i + 2].split(",");
            xValues[i] = row[0];
            float chamberValue = Float.parseFloat(row[1]);
            float userValue = Float.parseFloat(row[2]);
            float setValue;

            //to handle set = "none" string value, convert to 0 if string
            try {
                setValue = Float.parseFloat(row[3]);
            } catch (NumberFormatException e) {
                setValue = 0f;
            }
            yChamberValues.add(new Entry(i, chamberValue));
            yUserValues.add(new Entry(i, userValue));
            ySetValues.add(new Entry(i, setValue));

        }
        Log.d(TAG, "onCreateView: size of xValues: " + xValues.length);

        LineDataSet lineDataSet1 = new LineDataSet(yChamberValues, headers[1]);
        lineDataSet1.setDrawCircles(false);
        lineDataSet1.setColor(Color.BLUE);
        lineDataSet1.setDrawValues(false);

        LineDataSet lineDataSet2 = new LineDataSet(yUserValues, headers[2]);
        lineDataSet2.setDrawCircles(false);
        lineDataSet2.setColor(Color.GREEN);
        lineDataSet2.setDrawValues(false);


        LineDataSet lineDataSet3 = new LineDataSet(ySetValues, headers[3]);
        lineDataSet3.setDrawCircles(false);
        lineDataSet3.setColor(Color.BLACK);
        lineDataSet3.setDrawValues(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);
        dataSets.add(lineDataSet2);
        dataSets.add(lineDataSet3);

        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xValues[(int) value];
            }
        });

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Description description = new Description();
        description.setText(chartTitle + " Points: " + xValues.length);
        description.setTextSize(12);

        lineChart.setDescription(description);
        lineChart.setData(new LineData(dataSets));

    }
}
