package com.sunelectronics.sunbluetoothapp.models;

import com.sunelectronics.sunbluetoothapp.R;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH1_LABEL;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_CYCLE_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_PID_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_SET_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_TEMP_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_UTL_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_WAIT_QUERY;

public class TC01Controller extends TemperatureController {

    private String timeUnits;

    TC01Controller() {

        ch1Label = CH1_LABEL;
        ch1QueryCommand = TC01_TEMP_QUERY;
        waitQueryCommand = TC01_WAIT_QUERY;
        setQueryCommand = TC01_SET_QUERY;
        cycleNumberQuery = TC01_CYCLE_QUERY;
        pollingCommands.add(ch1QueryCommand);
        pollingCommands.add(setQueryCommand);
        pollingCommands.add(waitQueryCommand);
        pollingCommands.add(cycleNumberQuery);
        mResourceLayout = R.layout.fragment_tc01_display;
        utlQueryCommand = TC01_UTL_QUERY;
        pidCQueryCommand = TC01_PID_QUERY;
        pidHQueryCommand = TC01_PID_QUERY;
        name = TC01_NAME;
        timeUnits = "MIN";
    }

    public String getTimeUnits() {
        return timeUnits;
    }

    public void setTimeUnits(String timeUnits) {
        this.timeUnits = timeUnits;
    }

    public String getSetCommand(String setTemp) {
        return setTemp + "C";
    }

    public String getWaitCommand(String waitTime) {
        return waitTime + "M";
    }

}
