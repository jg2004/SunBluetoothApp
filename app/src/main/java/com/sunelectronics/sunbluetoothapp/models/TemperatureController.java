package com.sunelectronics.sunbluetoothapp.models;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class TemperatureController implements Serializable {

    int numberOfChannels = 1;
    String ch1QueryCommand, waitQueryCommand, setQueryCommand, ch1Label, heatEnableCommand;
    String pidHQueryCommand, pidCQueryCommand;
    String coolEnableCommand, heatDisableCommand, coolDisableCommand;
    String pidHCommand, pidCCommand;
    private String ch1Reading, currentSetPoint;
    ArrayList<String> pollingCommands;
    ArrayList<String> chartCommands;
    private int mPollCommandIterator;
    int mResourceLayout;
    private long timeStampOfReading;
    private static final String TAG = "TemperatureController";

    TemperatureController() {

        Log.d(TAG, "TemperatureController: was constructed");
        pollingCommands = new ArrayList<>();
    }

    public String getCurrentSetPoint() {
        return currentSetPoint;
    }

    public void setCurrentSetPoint(String currentSetPoint) {
        this.currentSetPoint = currentSetPoint;
    }

    public String getCh1Label() {
        return ch1Label;
    }

    public int getResourceLayout() {
        return mResourceLayout;
    }

    public static TemperatureController createController(String type) {


        switch (type.toUpperCase()) {

            case "EC1X":
            case "EC127":
            case "PC1000":
            case "PC100-2":
                return new DualChannelTemperatureController(type);

            case "PC100":
            case "TC02":
                return new SingleChannelTemperatureController(type);

            default:
                return null;
        }
    }

    public String getNextPollingCommand() {

        String command = pollingCommands.get(mPollCommandIterator);
        mPollCommandIterator++;
        mPollCommandIterator = mPollCommandIterator >= pollingCommands.size() ? 0 : mPollCommandIterator;
        return command;
    }

    public String getCh1Reading() {
        return ch1Reading;
    }

    public void setCh1Reading(String ch1Reading) {
        this.ch1Reading = ch1Reading;
    }

    public long getTimeStampOfReading() {
        return timeStampOfReading;
    }

    public void setTimeStampOfReading(long timeStampOfReading) {
        this.timeStampOfReading = timeStampOfReading;
    }

    public String getHeatEnableCommand() {
        return heatEnableCommand;
    }

    public String getCoolEnableCommand() {
        return coolEnableCommand;
    }

    public String getHeatDisableCommand() {
        return heatDisableCommand;
    }

    public String getCoolDisableCommand() {
        return coolDisableCommand;
    }
}