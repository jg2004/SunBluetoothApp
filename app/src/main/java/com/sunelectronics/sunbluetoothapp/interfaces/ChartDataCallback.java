package com.sunelectronics.sunbluetoothapp.interfaces;

import com.github.mikephil.charting.data.LineData;

/**
 * implemented by fragments that use the LoadChartTask async task. The async task passes it's
 * data back through the fragments that implement this interface
 */

public interface ChartDataCallback {

    void initialize();
    void setLineData(LineData lineData);
}
