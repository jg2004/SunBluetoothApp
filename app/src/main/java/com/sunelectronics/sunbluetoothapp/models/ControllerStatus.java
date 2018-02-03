package com.sunelectronics.sunbluetoothapp.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER_TYPE;

/**
 * Class that stores the chamber status info contained in the response from the STATUS? command.
 * This command contains a string of Yes and No characters (i.e YNNNYYY...etc) that provide the user
 * status information about the chamber such as whether the chamber is powered on, running a local
 * program, has a valid set point etc...
 */

public class ControllerStatus {

    private String mTimeoutStatusMessage, mWaitingForTimeOutStatusMessage, mValidSetStatusMessage;
    private String mCurrentlyRampingStatusMessage, mLpRunningStatusMessage, mWaitingAtBreakPointStatusMessage;
    private String mChamberIsOnMessage, mControllerType;
    private boolean mIsLPRunning, mHeatEnableOn, mCoolEnableOn, mWaitingAtBreakPoint, mPowerIsOn;

    private static final String TAG = "ControllerStatus";

    public ControllerStatus(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        mControllerType = prefs.getString(CONTROLLER_TYPE, "EC1X");
        Log.d(TAG, "ControllerStatus: controller is type: " + mControllerType);
    }

    public void setStatusMessages(String statusString) {
        Log.d(TAG, "setStatusMessages: called");
        setState(statusString);
    }

    private void setState(String statusString) {
        Log.d(TAG, "setState: called");

        char currentlyRamping = 'N', waitingAtBreakPoint = 'N', lpRunning = 'N';

        if (statusString.length() < 12) {
            Log.d(TAG, "setState: possible invalid status string: " + statusString);
            return;
        }

        char powerIsOn = statusString.charAt(0);
        char timeOutLedOn = statusString.charAt(2);
        char waitingForTimeOut = statusString.charAt(3);
        char heatEnableOn = statusString.charAt(4);
        char coolEnableOn = statusString.charAt(5);
        char validSet = statusString.charAt(6);

        switch (mControllerType) {

            case "EC1X":
            case "PC100-2":
            case "TC02":
            case "PC100":
                currentlyRamping = statusString.charAt(8);
                waitingAtBreakPoint = statusString.charAt(11);
                lpRunning = statusString.charAt(12);
                break;

            case "PC1000":
            case "EC127":
                Log.d(TAG, "setState: controller is type pc1000 or ec127");
                currentlyRamping = statusString.charAt(12);
                waitingAtBreakPoint = statusString.charAt(19);
                lpRunning = statusString.charAt(20);
                break;
        }
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