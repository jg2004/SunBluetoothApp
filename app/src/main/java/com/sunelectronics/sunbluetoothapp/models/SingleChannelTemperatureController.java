package com.sunelectronics.sunbluetoothapp.models;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDC_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDC_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDH_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDH_QUERY;

public class SingleChannelTemperatureController extends TemperatureController {

    public static final String CH1_QUERY_COMMAND = "TEMP?";
    private static final String RATE_QUERY_COMMAND = "RATE?";
    private static final String WAIT_QUERY_COMMAND = "WAIT?";
    private static final String SET_QUERY_COMMAND = "SET?";
    private static final String CH1_LABEL = "TEMP";
    public static final String COOL_ENABLE_COMMAND = "CON";
    public static final String HEAT_ENABLE_COMMAND = "HON";
    public static final String COOL_DISABLE_COMMAND = "COFF";
    public static final String HEAT_DISABLE_COMMAND = "HOFF";


    private String rateQueryCommand;

    SingleChannelTemperatureController(String type) {

        mResourceLayout = R.layout.fragment_single_channel_display;

        switch (type.toUpperCase()) {

            case "PC100":
            case "TC02":
                ch1Label = CH1_LABEL;
                ch1QueryCommand = CH1_QUERY_COMMAND;
                waitQueryCommand = WAIT_QUERY_COMMAND;
                setQueryCommand = SET_QUERY_COMMAND;
                rateQueryCommand = RATE_QUERY_COMMAND;
                coolEnableCommand = COOL_ENABLE_COMMAND;
                heatEnableCommand = HEAT_ENABLE_COMMAND;
                heatDisableCommand = HEAT_DISABLE_COMMAND;
                coolDisableCommand = COOL_DISABLE_COMMAND;
                pidHQueryCommand = PIDH_QUERY;
                pidCQueryCommand = PIDC_QUERY;
                pidCCommand = PIDC_COMMAND;
                pidHCommand= PIDH_COMMAND;
                break;
        }
        pollingCommands.add(ch1QueryCommand);
        pollingCommands.add(rateQueryCommand);
        pollingCommands.add(waitQueryCommand);
        pollingCommands.add(setQueryCommand);
        pollingCommands.add(Constants.STATUS);
    }
}