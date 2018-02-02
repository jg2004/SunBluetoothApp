package com.sunelectronics.sunbluetoothapp.models;

import com.sunelectronics.sunbluetoothapp.R;

/**
 * Created by Jerry on 2/2/2018.
 */

public class TC10Controller extends TemperatureController {

    private static final String CH1_COMMAND = "TEMP?";
    private static final String CH2_COMMAND = "UCHAN?";
    private static final String RATE_COMMAND = "RATE?";
    private static final String WAIT_COMMAND = "WAIT?";





    public TC10Controller() {
        super(2, R.layout.fragment_display_temps);
        displayCommands.add(CH1_COMMAND);
        displayCommands.add(CH2_COMMAND);
        displayCommands.add(RATE_COMMAND);
        displayCommands.add(WAIT_COMMAND);

    }
}
