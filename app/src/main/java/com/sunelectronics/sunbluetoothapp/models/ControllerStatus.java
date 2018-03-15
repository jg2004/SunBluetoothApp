package com.sunelectronics.sunbluetoothapp.models;

import android.content.Context;
import android.util.Log;

import com.sunelectronics.sunbluetoothapp.utilities.PreferenceSetting;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC127;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC1000;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100_2;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC02;

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
    private static ControllerStatus mControllerStatus;

    private static final String TAG = "ControllerStatus";

    private ControllerStatus(Context context) {

        mControllerType = PreferenceSetting.getControllerType(context);

        Log.d(TAG, "ControllerStatus: controller is type: " + mControllerType);
    }

    public static synchronized ControllerStatus getInstance(Context context) {

        if (mControllerStatus == null) {
            mControllerStatus = new ControllerStatus(context);
            return mControllerStatus;
        } else {

            return mControllerStatus;
        }
    }

    public static synchronized void init() {
        //used by Preference Frag to initialize to null when changing controller types
        Log.d(TAG, "init: setting controller status to NULL");
        mControllerStatus = null;
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

            case EC1X:
            case PC100_2:
            case TC02:
            case PC100:
                currentlyRamping = statusString.charAt(8);
                waitingAtBreakPoint = statusString.charAt(11);
                lpRunning = statusString.charAt(12);
                break;

            case PC1000:
            case EC127:
                Log.d(TAG, "setState: controller is type pc1000 or ec127");
                currentlyRamping = statusString.charAt(12);
                if (statusString.length() > 21) {
                    waitingAtBreakPoint = statusString.charAt(19);
                    lpRunning = statusString.charAt(20);
                }
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
            Log.d(TAG, "setState: set LP running to true");
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