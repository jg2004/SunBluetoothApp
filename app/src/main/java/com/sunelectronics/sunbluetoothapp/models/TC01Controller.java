package com.sunelectronics.sunbluetoothapp.models;

/**
 * Created by Jerry on 2/2/2018.
 */

public class TC01Controller extends TemperatureController {
    private static final String CH1_COMMAND = "T";
    private static final String WAIT_COMMAND = "M";


    public TC01Controller() {
        super(1);
        displayCommands.add(CH1_COMMAND);
        displayCommands.add(WAIT_COMMAND);
    }
}
