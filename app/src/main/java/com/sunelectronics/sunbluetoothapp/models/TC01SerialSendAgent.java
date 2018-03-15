package com.sunelectronics.sunbluetoothapp.models;

import android.os.Handler;
import android.util.Log;

import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;

import java.util.ArrayList;
import java.util.List;


public class TC01SerialSendAgent {

    private Handler mHandler;
    private static final int DELAY_100_MS = 100;
    private static final String TAG = "TC01SerialSendAgent";
    private List<Runnable> mRunnableList;
    private boolean busy;
    private static TC01SerialSendAgent mSerialSendAgent;

    private TC01SerialSendAgent(Handler handler) {

        mHandler = handler;
        mRunnableList = new ArrayList<>();
    }

    public static synchronized TC01SerialSendAgent getInstance(Handler handler) {
        if (mSerialSendAgent == null) {
            mSerialSendAgent = new TC01SerialSendAgent(handler);
        }
        return mSerialSendAgent;
    }

    public void sendCommand(String stringToSend) {
        Log.d(TAG, "sendCommand: is " + stringToSend);
        SendCharRunnable runnable = new SendCharRunnable(stringToSend);
        if (busy) {
            mRunnableList.add(runnable);
            Log.d(TAG, "sendCommand: is busy, adding runnable to list. List size is: " + mRunnableList.size());
        } else {
            Log.d(TAG, "sendCommand: is NOT busy, sending runnable with command: " + stringToSend);
            mHandler.postDelayed(runnable, DELAY_100_MS);
            busy = true;
        }
    }

    private class SendCharRunnable implements Runnable {
        String mStringToSend;
        int mCharCounter;

        SendCharRunnable(String stringToSend) {
            mStringToSend = stringToSend;
        }

        @Override
        public void run() {
            Log.d(TAG, "run: sending character (" + mCharCounter + ") " + mStringToSend.charAt(mCharCounter));
            BluetoothConnectionService.getInstance().writeNoCr(String.valueOf(mStringToSend.charAt(mCharCounter)));
            if (mCharCounter < mStringToSend.length() - 1) {
                mCharCounter++;
                mHandler.postDelayed(this, DELAY_100_MS);
            } else {
                Log.d(TAG, "run: sending command is complete");
                if (!mRunnableList.isEmpty()) {
                    Log.d(TAG, "run: getting the next runnable");
                    mHandler.postDelayed(mRunnableList.get(0), DELAY_100_MS);
                    mRunnableList.remove(0);
                } else {
                    Log.d(TAG, "run: list is empty!");
                    busy = false;
                }
            }
        }
    }
}