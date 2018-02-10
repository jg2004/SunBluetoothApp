package com.sunelectronics.sunbluetoothapp.models;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH1_LABEL;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH1_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER_RS_ECHO_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.COOL_DISABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.COOL_ENABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.HEAT_DISABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.HEAT_ENABLE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LTL_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LTL_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDC_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDC_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDH_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDH_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.RATE_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SET_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC02;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC02_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC_RATE_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC_SET_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC_WAIT_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.UTL_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.UTL_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.WAIT_QUERY_COMMAND;

public class SingleChannelTemperatureController extends TemperatureController {

    private String rateQueryCommand;

    SingleChannelTemperatureController(String type) {

        mResourceLayout = R.layout.fragment_single_channel_display;

        switch (type) {

            case PC100:
                name = PC100_NAME;
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
                pidHCommand = PIDH_COMMAND;
                ltlqueryCommand = LTL_QUERY;
                utlQueryCommand = UTL_QUERY;
                ltlCommand = LTL_COMMAND;
                utlCommand = UTL_COMMAND;
                rs232EchoMessage = CONTROLLER_RS_ECHO_MESSAGE;
                rateCommand=TC_RATE_COMMAND;
                waitCommand=TC_WAIT_COMMAND;
                setCommand=TC_SET_COMMAND;
                break;
            case TC02:
                name = TC02_NAME;
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
                pidHCommand = PIDH_COMMAND;
                ltlqueryCommand = LTL_QUERY;
                utlQueryCommand = UTL_QUERY;
                ltlCommand = LTL_COMMAND;
                utlCommand = UTL_COMMAND;
                rs232EchoMessage = CONTROLLER_RS_ECHO_MESSAGE;
                rateCommand=TC_RATE_COMMAND;
                waitCommand=TC_WAIT_COMMAND;
                setCommand=TC_SET_COMMAND;
                break;
        }
        pollingCommands.add(ch1QueryCommand);
        pollingCommands.add(rateQueryCommand);
        pollingCommands.add(waitQueryCommand);
        pollingCommands.add(setQueryCommand);
        pollingCommands.add(Constants.STATUS);
    }
}