package com.sunelectronics.sunbluetoothapp.models;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH1_READING_LABEL;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH2_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH2_READING_LABEL;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHAMBER_READING_LABEL;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER_RS_ECHO_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.COOL_DISABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.COOL_ENABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC127;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC127_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X_CH2_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X_RS_ECHO_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.HEAT_DISABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.HEAT_ENABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LTL_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LTL_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_COOL_DISABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_COOL_ENABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_HEAT_DISABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_HEAT_ENABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_LTL_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_LTL_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_PIDC_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_PIDC_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_PIDH_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_PIDH_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_UTL_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_UTL_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100_2;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100_2_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_RATE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_RATE_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_SET_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_SET_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_WAIT_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_WAIT_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDC_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDC_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDH_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDH_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC10_RATE_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC10_SET_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC10_WAIT_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC_RATE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC_SET_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC_WAIT_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TEMP_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.USER_READING_LABEL;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.UTL_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.UTL_QUERY;

public class DualChannelTemperatureController extends TemperatureController {

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
        switch (type) {

            case EC1X:
                name = EC1X_NAME;
                setQueryCommand = TC10_SET_QUERY_COMMAND;
                ch1QueryCommand = TEMP_QUERY_COMMAND;
                ch2QueryCommand = EC1X_CH2_QUERY_COMMAND;
                ch1Label = CHAMBER_READING_LABEL;
                ch2Label = USER_READING_LABEL;
                waitQueryCommand = TC10_WAIT_QUERY_COMMAND;
                rateQueryCommand = TC10_RATE_QUERY_COMMAND;
                heatEnableCommand = HEAT_ENABLE_COMMAND;
                heatDisableCommand = HEAT_DISABLE_COMMAND;
                coolEnableCommand = COOL_ENABLE_COMMAND;
                coolDisableCommand = COOL_DISABLE_COMMAND;
                pidHQueryCommand = PIDH_QUERY;
                pidCQueryCommand = PIDC_QUERY;
                pidHCommand = PIDH_COMMAND;
                pidCCommand = PIDC_COMMAND;
                ltlqueryCommand = LTL_QUERY;
                utlQueryCommand = UTL_QUERY;
                ltlCommand = LTL_COMMAND;
                utlCommand = UTL_COMMAND;
                rs232EchoMessage = EC1X_RS_ECHO_MESSAGE;
                rateCommand = TC_RATE_COMMAND;
                waitCommand = TC_WAIT_COMMAND;
                setCommand = TC_SET_COMMAND;
                break;

            case EC127:
                name = EC127_NAME;
                setQueryCommand = PC_SET_QUERY_COMMAND;
                ch1QueryCommand = PC_QUERY_COMMAND;
                ch2QueryCommand = CH2_QUERY_COMMAND;
                ch1Label = CHAMBER_READING_LABEL;
                ch2Label = USER_READING_LABEL;
                waitQueryCommand = PC_WAIT_QUERY_COMMAND;
                rateQueryCommand = PC_RATE_QUERY_COMMAND;
                heatEnableCommand = PC1000_HEAT_ENABLE_COMMAND;
                heatDisableCommand = PC1000_HEAT_DISABLE_COMMAND;
                coolEnableCommand = PC1000_COOL_ENABLE_COMMAND;
                coolDisableCommand = PC1000_COOL_DISABLE_COMMAND;
                pidHQueryCommand = PC1000_PIDH_QUERY;
                pidCQueryCommand = PC1000_PIDC_QUERY;
                pidHCommand = PC1000_PIDH_COMMAND;
                pidCCommand = PC1000_PIDC_COMMAND;
                ltlqueryCommand = PC1000_LTL_QUERY;
                utlQueryCommand = PC1000_UTL_QUERY;
                ltlCommand = PC1000_LTL_COMMAND;
                utlCommand = PC1000_UTL_COMMAND;
                rs232EchoMessage = EC1X_RS_ECHO_MESSAGE;
                rateCommand = TC_RATE_COMMAND;
                waitCommand = TC_WAIT_COMMAND;
                setCommand = TC_SET_COMMAND;
                break;

            case PC100_2:
                name = PC100_2_NAME;
                setQueryCommand = PC_SET_QUERY_COMMAND;
                ch1QueryCommand = PC_QUERY_COMMAND;
                ch2QueryCommand = CH2_QUERY_COMMAND;
                ch1Label = CH1_READING_LABEL;
                ch2Label = CH2_READING_LABEL;
                waitQueryCommand = TC10_WAIT_QUERY_COMMAND;
                rateQueryCommand = TC10_RATE_QUERY_COMMAND;
                heatEnableCommand = HEAT_ENABLE_COMMAND;
                heatDisableCommand = HEAT_DISABLE_COMMAND;
                coolEnableCommand = COOL_ENABLE_COMMAND;
                coolDisableCommand = COOL_DISABLE_COMMAND;
                pidHQueryCommand = PIDH_QUERY;
                pidCQueryCommand = PIDC_QUERY;
                pidHCommand = PIDH_COMMAND;
                pidCCommand = PIDC_COMMAND;
                ltlqueryCommand = LTL_QUERY;
                utlQueryCommand = UTL_QUERY;
                ltlCommand = LTL_COMMAND;
                utlCommand = UTL_COMMAND;
                rs232EchoMessage = CONTROLLER_RS_ECHO_MESSAGE;
                rateCommand = TC_RATE_COMMAND;
                waitCommand = TC_WAIT_COMMAND;
                setCommand = TC_SET_COMMAND;
                break;
            case PC1000:
                name = PC1000_NAME;
                setQueryCommand = PC_SET_QUERY_COMMAND;
                ch1QueryCommand = PC_QUERY_COMMAND;
                ch2QueryCommand = CH2_QUERY_COMMAND;
                ch1Label = CH1_READING_LABEL;
                ch2Label = CH2_READING_LABEL;
                waitQueryCommand = PC_WAIT_QUERY_COMMAND;
                rateQueryCommand = PC_RATE_QUERY_COMMAND;
                heatEnableCommand = PC1000_HEAT_ENABLE_COMMAND;
                heatDisableCommand = PC1000_HEAT_DISABLE_COMMAND;
                coolEnableCommand = PC1000_COOL_ENABLE_COMMAND;
                coolDisableCommand = PC1000_COOL_DISABLE_COMMAND;
                pidHQueryCommand = PC1000_PIDH_QUERY;
                pidCQueryCommand = PC1000_PIDC_QUERY;
                pidHCommand = PC1000_PIDH_COMMAND;
                pidCCommand = PC1000_PIDC_COMMAND;
                ltlqueryCommand = PC1000_LTL_QUERY;
                utlQueryCommand = PC1000_UTL_QUERY;
                ltlCommand = PC1000_LTL_COMMAND;
                utlCommand = PC1000_UTL_COMMAND;
                rs232EchoMessage = CONTROLLER_RS_ECHO_MESSAGE;
                rateCommand = PC_RATE_COMMAND;
                waitCommand = PC_WAIT_COMMAND;
                setCommand = PC_SET_COMMAND;
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