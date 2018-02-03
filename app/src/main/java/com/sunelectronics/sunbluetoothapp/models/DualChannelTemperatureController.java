package com.sunelectronics.sunbluetoothapp.models;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;


public class DualChannelTemperatureController extends TemperatureController {

    private static final String TEMP_QUERY_COMMAND = "TEMP?";
    public static final String EC1X_CH2_QUERY_COMMAND = "UCHAN?";
    private static final String TC10_WAIT_QUERY_COMMAND = "WAIT?";
    private static final String TC10_RATE_QUERY_COMMAND = "RATE?";
    private static final String TC10_SET_QUERY_COMMAND = "SET?";
    private static final String CHAMBER_READING_LABEL = "CHAM";
    private static final String USER_READING_LABEL = "USER";
    public static final String PC_QUERY_COMMAND = "C1?";
    public static final String CH2_QUERY_COMMAND = "C2?";
    private static final String PC_WAIT_QUERY_COMMAND = "WAIT1?";
    private static final String PC_RATE_QUERY_COMMAND = "RATE1?";
    private static final String PC_SET_QUERY_COMMAND = "SET1?";
    private static final String CH1_READING_LABEL = "CH1";
    private static final String CH2_READING_LABEL = "CH2";

    private String ch2QueryCommand, ch2Label, rateQueryCommand, ch2Reading;

    private DualChannelTemperatureController() {

        super();
        numberOfChannels = 2;
        mResourceLayout = R.layout.fragment_dual_channel_display;
    }

    public String getCh2Label() {
        return ch2Label;
    }

    DualChannelTemperatureController(String type) {

        this();
        switch (type.toUpperCase()) {

            case "EC1X":
                setQueryCommand = TC10_SET_QUERY_COMMAND;
                ch1QueryCommand = TEMP_QUERY_COMMAND;
                ch2QueryCommand = EC1X_CH2_QUERY_COMMAND;
                ch1Label = CHAMBER_READING_LABEL;
                ch2Label = USER_READING_LABEL;
                waitQueryCommand = TC10_WAIT_QUERY_COMMAND;
                rateQueryCommand = TC10_RATE_QUERY_COMMAND;
                break;

            case "EC127":
                setQueryCommand = PC_SET_QUERY_COMMAND;
                ch1QueryCommand = PC_QUERY_COMMAND;
                ch2QueryCommand = CH2_QUERY_COMMAND;
                ch1Label = CHAMBER_READING_LABEL;
                ch2Label = USER_READING_LABEL;
                waitQueryCommand = PC_WAIT_QUERY_COMMAND;
                rateQueryCommand = PC_RATE_QUERY_COMMAND;
                break;

            case "PC100-2":
            case "PC1000":
                setQueryCommand = PC_SET_QUERY_COMMAND;
                ch1QueryCommand = PC_QUERY_COMMAND;
                ch2QueryCommand = CH2_QUERY_COMMAND;
                ch1Label = CH1_READING_LABEL;
                ch2Label = CH2_READING_LABEL;
                waitQueryCommand = TC10_WAIT_QUERY_COMMAND;
                rateQueryCommand = TC10_RATE_QUERY_COMMAND;
                break;
        }
        pollingCommands.add(ch1QueryCommand);
        pollingCommands.add(ch2QueryCommand);
        pollingCommands.add(rateQueryCommand);
        pollingCommands.add(waitQueryCommand);
        pollingCommands.add(setQueryCommand);
        pollingCommands.add(Constants.STATUS);
    }

    public String getCh2Reading() {
        return ch2Reading;
    }

    public void setCh2Reading(String ch2Reading) {
        this.ch2Reading = ch2Reading;
    }
}