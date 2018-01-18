package com.sunelectronics.sunbluetoothapp.models;

import android.util.Log;

/**
 * Class that stores the chamber status info contained in the response from the STATUS? command.
 * This command contains a string of Yes and No characters (i.e YNNNYYY...etc) that provide the user
 * status information about the chamber such as whether the chamber is powered on, running a local
 * program, has a valid set point etc...
 */

public class ChamberStatus {

    private String mTimeoutStatusMessage;
    private String mWaitingForTimeOutStatusMessage;
    private String mValidSetStatusMessage;
    private String mCurrentlyRampingStatusMessage;
    private String mLpRunningStatusMessage;
    private String mWaitingAtBreakPointStatusMessage;
    private String mChamberIsOnMessage;
    private boolean mIsLPRunning, mHeatEnableOn, mCoolEnableOn, mWaitingAtBreakPoint, mPowerIsOn;
    private static final String TAG = "ChamberStatus";

    public ChamberStatus() {
        Log.d(TAG, "ChamberStatus empty constructor called");
    }


    public void setStatusMessages(String statusString) {
        Log.d(TAG, "setStatusMessages: called");
        setState(statusString);
    }

    private void setState(String statusString) {
        Log.d(TAG, "setState: called");

        if (statusString.length() < 12) {
            Log.d(TAG, "setState: possible invalid status string: " + statusString);
            return;
        }
        char timeOutLedOn = statusString.charAt(2);
        char waitingForTimeOut = statusString.charAt(3);
        char validSet = statusString.charAt(6);
        char currentlyRamping = statusString.charAt(8);
        char lpRunning = statusString.charAt(12);
        char waitingAtBreakPoint = statusString.charAt(11);
        char powerIsOn = statusString.charAt(0);
        char coolEnableOn = statusString.charAt(5);
        char heatEnableOn = statusString.charAt(4);

        if (powerIsOn == 'Y') {
            mChamberIsOnMessage = "POWER IS ON";
            mPowerIsOn = true;
        } else {
            mChamberIsOnMessage = "POWER IS OFF";
            mPowerIsOn = false;
        }
        mCoolEnableOn = coolEnableOn == 'Y';
        mHeatEnableOn = heatEnableOn == 'Y';

        if (timeOutLedOn == 'Y') {
            mTimeoutStatusMessage = "TIME OUT LED IS ON";
        } else {
            mTimeoutStatusMessage = "TIME OUT LED IS OFF";
        }

        if (waitingForTimeOut == 'Y') {
            mWaitingForTimeOutStatusMessage = "WAIT TIME COUNTING DOWN";
        } else {
            mWaitingForTimeOutStatusMessage = "NOT WAITING FOR TIME OUT";
        }

        if (validSet == 'Y') {
            mValidSetStatusMessage = "VALID SET POINT";
        } else {
            mValidSetStatusMessage = "NO SET POINT";
        }
        if (currentlyRamping == 'Y') {
            mCurrentlyRampingStatusMessage = "RAMPING TO SET";
        } else {
            mCurrentlyRampingStatusMessage = "NOT RAMPING";
        }
        if (lpRunning == 'Y') {
            mLpRunningStatusMessage = "LP RUNNING";
            mIsLPRunning = true;
        } else {
            mLpRunningStatusMessage = "LP NOT RUNNING";
            mIsLPRunning = false;
        }
        if (waitingAtBreakPoint == 'Y') {

            mWaitingAtBreakPointStatusMessage = "LP PAUSED AT BREAKPOINT";
            mWaitingAtBreakPoint = true;
        } else {
            mWaitingAtBreakPointStatusMessage = "LP NOT WAITING AT BREAKPOINT";
            mWaitingAtBreakPoint = false;
        }
    }

    /*---------------------------------setters----------------------------------------------------*/
    public void setPowerIsOn(boolean powerIsOn) {
        mPowerIsOn = powerIsOn;
    }

    /*----------------------------------getters---------------------------------------------------*/
    public boolean isLPRunning() {
        return mIsLPRunning;
    }

    public boolean isHeatEnableOn() {
        return mHeatEnableOn;
    }

    public boolean isCoolEnableOn() {
        return mCoolEnableOn;
    }

    public String getWaitingAtBreakPointStatusMessage() {
        return mWaitingAtBreakPointStatusMessage;
    }

    public boolean isPowerOn() {
        return mPowerIsOn;
    }

    public boolean isWaitingAtBreakPoint() {
        return mWaitingAtBreakPoint;
    }

    public String getTimeoutStatusMessage() {
        return mTimeoutStatusMessage;
    }

    public String getWaitingForTimeOutStatusMessage() {
        return mWaitingForTimeOutStatusMessage;
    }

    public String getValidSetStatusMessage() {
        return mValidSetStatusMessage;
    }

    public String getCurrentlyRampingStatusMessage() {
        return mCurrentlyRampingStatusMessage;
    }

    public String getLpRunningStatusMessage() {
        return mLpRunningStatusMessage;
    }

    public String getChamberIsOnMessage() {
        return mChamberIsOnMessage;
    }

}