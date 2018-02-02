package com.sunelectronics.sunbluetoothapp.models;

import java.util.ArrayList;

/**
 * Created by Jerry on 2/2/2018.
 */

public abstract class TemperatureController {

    protected int numberOfChannels;
    protected ArrayList<String> displayCommands;
    protected ArrayList<String> chartCommands;
    protected int viewResourceID;

    public TemperatureController(int numberOfChannels, int viewResourceID){

        this.numberOfChannels=numberOfChannels;
        this.viewResourceID = viewResourceID;
    }





}
