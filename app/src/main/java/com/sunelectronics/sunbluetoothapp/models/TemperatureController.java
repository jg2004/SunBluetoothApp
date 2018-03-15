package com.sunelectronics.sunbluetoothapp.models;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC127;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC127_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100_2;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100_2_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC02;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC02_NAME;

public abstract class TemperatureController implements Serializable {

    int numberOfChannels = 1;
    String ch1QueryCommand, waitQueryCommand, setQueryCommand, ch1Label, heatEnableCommand;
    String pidHQueryCommand, pidCQueryCommand, rs232EchoMessage;
    String coolEnableCommand, heatDisableCommand, coolDisableCommand;
    String pidHCommand, pidCCommand, utlCommand, cycleNumberQuery;
    String ltlCommand, rateCommand, waitCommand, setCommand;
    String name, utlQueryCommand, ltlqueryCommand;
    private String ch1Reading, currentSetPoint;
    ArrayList<String> pollingCommands;
    ArrayList<String> chartCommands;
    private int mPollCommandIterator;
    int mResourceLayout;
    private long timeStampOfReading;
    private static final String TAG = "TemperatureController";

    public String getUtlCommand() {
        return utlCommand;
    }

    public String getLtlCommand() {
        return ltlCommand;
    }

    public String getRs232EchoMessage() {
        return rs232EchoMessage;
    }

    public String getRateCommand() {
        return rateCommand;
    }

    public String getWaitCommand() {
        return waitCommand;
    }

    public String getSetCommand() {
        return setCommand;
    }

    public String getPidHQueryCommand() {
        return pidHQueryCommand;
    }

    public String getPidCQueryCommand() {
        return pidCQueryCommand;
    }

    public String getPidHCommand() {
        return pidHCommand;
    }

    public String getPidCCommand() {
        return pidCCommand;
    }

    public String getUtlQueryCommand() {
        return utlQueryCommand;
    }

    public String getLtlqueryCommand() {
        return ltlqueryCommand;
    }


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


        switch (type) {

            case EC1X:
            case EC127:
            case PC1000:
            case PC100_2:
                return new DualChannelTemperatureController(type);

            case PC100:
            case TC02:
                return new SingleChannelTemperatureController(type);
            case TC01:
                return new TC01Controller();

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

    public String getName() {
        return name;
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



    public static String getName(String controllerType) {
        switch (controllerType) {

            case EC1X:
                return EC1X_NAME;
            case PC100:
                return PC100_NAME;
            case EC127:
                return EC127_NAME;
            case PC1000:
                return PC1000_NAME;
            case PC100_2:
                return PC100_2_NAME;
            case TC02:
                return TC02_NAME;
            case TC01:
                return TC01_NAME;
            default:
                return "N/A";
        }
    }
}