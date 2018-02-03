package com.sunelectronics.sunbluetoothapp.models;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

public class SingleChannelTemperatureController extends TemperatureController {

    public static final String CH1_QUERY_COMMAND = "TEMP?";
    private static final String RATE_QUERY_COMMAND = "RATE?";
    private static final String WAIT_QUERY_COMMAND = "WAIT?";
    private static final String SET_QUERY_COMMAND = "SET?";
    private static final String CH1_LABEL = "TEMP";

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
                break;
        }
        pollingCommands.add(ch1QueryCommand);
        pollingCommands.add(rateQueryCommand);
        pollingCommands.add(waitQueryCommand);
        pollingCommands.add(setQueryCommand);
        pollingCommands.add(Constants.STATUS);
    }
}