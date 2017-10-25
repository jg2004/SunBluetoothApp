package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;

import java.util.ArrayList;

public class DisplayTemperatureFragment extends Fragment {
    private static final String TAG = "DisplayTemperatureFragm";
    private static final int COMMAND_SEND_DELAY_MS = 500;
    private Handler mTimerHandler;
    private Runnable mDisplayUpdateRunnable;
    private Button mButtonSingleSegment;
    private TextView mTextViewChamberTemp, mTextViewUserTemp, mTextViewWaitTime, mTextViewSetTemp;
    private TextView mTextViewRate;
    private ToggleButton mHeatEnableToggleButton, mCoolEnableToggleButton;
    private Switch mSwitchOnOff;
    private Context mContext;
    private static final String HON = "HON";
    private static final String HOFF = "HOFF";
    private static final String CON = "CON";
    private static final String COFF = "COFF";
    private static final String CHAM_TEMP = "CHAM?";
    private static final String ON = "ON";
    private static final String OFF = "OFF";
    private static final String USER_TEMP = "USER?";
    private static final String SET_TEMP = "SET?";
    private static final String WAIT_TIME = "WAIT?";
    private static final String STATUS = "STATUS?";
    private static final String RATE = "RATE?";
    private ArrayList<String> mCommands = new ArrayList<>();
    private int mCommandCounter;
    private BroadcastReceiver mDispTempBroadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_display_temps, container, false);
        initializeCommandArrayList();
        initializeHandler();
        initializeViews(view);
        return view;
    }

    private void initializeHandler() {

        mTimerHandler = new Handler();
        mDisplayUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                String command = mCommands.get(mCommandCounter);
                BluetoothConnectionService.getInstance(mContext).write(command);
                mTimerHandler.postDelayed(this, COMMAND_SEND_DELAY_MS);
                // reset mCommandCounter to 0 when it reaches 3, otherwise increment
                if (mCommandCounter >= mCommands.size() - 1) {
                    mCommandCounter = 0;
                } else {
                    mCommandCounter++;
                }
            }
        };
    }

    private void initializeCommandArrayList() {
        //this is the list of commands that are iterated through every COMMAND_SEND_DELAY_MS ms
        //by the mTimerHandler

        mCommands.add(CHAM_TEMP);
        mCommands.add(USER_TEMP);
        mCommands.add(RATE);
        mCommands.add(WAIT_TIME);
        mCommands.add(SET_TEMP);
        mCommands.add(STATUS);
    }

    private void initializeViews(View view) {
        mTextViewChamberTemp = (TextView) view.findViewById(R.id.textViewChamTemp);
        mTextViewUserTemp = (TextView) view.findViewById(R.id.textViewUserTemp);
        mTextViewRate = (TextView) view.findViewById(R.id.textViewRate);
        mTextViewWaitTime = (TextView) view.findViewById(R.id.textViewWait);
        mTextViewSetTemp = (TextView) view.findViewById(R.id.textViewSet);
        mButtonSingleSegment = (Button) view.findViewById(R.id.buttonSingleSegment);
        mButtonSingleSegment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleSegmentDialog();
            }
        });
        mHeatEnableToggleButton = (ToggleButton) view.findViewById(R.id.toggleButtonHeatEnable);
        mHeatEnableToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHeatEnableToggleButton.isChecked()) {
                    BluetoothConnectionService.getInstance(mContext).write(HON);

                } else {
                    BluetoothConnectionService.getInstance(mContext).write(HOFF);

                }
            }
        });
        mCoolEnableToggleButton = (ToggleButton) view.findViewById(R.id.toggleButtonCoolEnable);
        mCoolEnableToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCoolEnableToggleButton.isChecked()) {
                    BluetoothConnectionService.getInstance(mContext).write(CON);
                } else {
                    BluetoothConnectionService.getInstance(mContext).write(COFF);
                }
            }
        });

        mSwitchOnOff = (Switch) view.findViewById(R.id.switchOnOff);
        mSwitchOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwitchOnOff.isChecked()) {
                    BluetoothConnectionService.getInstance(mContext).clearCommandsWrittenList();
                    BluetoothConnectionService.getInstance(mContext).write(ON);
                    mTimerHandler.postDelayed(mDisplayUpdateRunnable, COMMAND_SEND_DELAY_MS);
                } else {
                    mTimerHandler.removeCallbacks(mDisplayUpdateRunnable);
                    BluetoothConnectionService.getInstance(mContext).write(OFF);
                    BluetoothConnectionService.getInstance(mContext).clearCommandsWrittenList();
                }
            }
        });
    }

    private void showSingleSegmentDialog() {
        SingleSegDialogFragment fragment = SingleSegDialogFragment.newInstance("Enter segment");
        fragment.show(getFragmentManager(), "single_seg_frag");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: called");
        mTimerHandler.removeCallbacks(mDisplayUpdateRunnable);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDispTempBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: called");
        mContext = context;
        mDispTempBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String commandSent = intent.getStringExtra(BluetoothConnectionService.COMMAND_SENT);
                String responseToCommandSent = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                String action = intent.getAction();
                Log.d(TAG, "Broadcast received: \n action: " + action + "\n" + "Command Sent: " +
                        commandSent + "\n" + "Response: " + responseToCommandSent);
                updateView(commandSent, responseToCommandSent);
            }
        };

        LocalBroadcastManager.getInstance(context).registerReceiver(mDispTempBroadcastReceiver, new IntentFilter(BluetoothConnectionService.MY_INTENT_FILTER));
        super.onAttach(context);
    }

    /**
     * method used to update the textViews according to response from bluetooth
     *
     * @param commandSent
     * @param responseToCommandSent
     */
    private void updateView(String commandSent, String responseToCommandSent) {

        switch (commandSent) {

            case CHAM_TEMP:

                mTextViewChamberTemp.setText(responseToCommandSent);
                break;

            case USER_TEMP:

                mTextViewUserTemp.setText(responseToCommandSent);
                break;

            case WAIT_TIME:

                if (responseToCommandSent.contains("FOREVER")) {
                    mTextViewWaitTime.setText("FOREVER");
                } else {
                    mTextViewWaitTime.setText(responseToCommandSent);
                }
                break;

            case RATE:

                mTextViewRate.setText(responseToCommandSent);
                break;

            case SET_TEMP:
                if (responseToCommandSent.contains("NONE")) {
                    mTextViewSetTemp.setText("NONE");
                } else {
                    mTextViewSetTemp.setText(responseToCommandSent);
                }
                break;
            case STATUS:

                char heatEnableChar = 'N'; //initialize to 'N'
                //check first if size of string is at least 5 to avoid outOfBound Exception
                if (responseToCommandSent.length() >= 5) {
                    heatEnableChar = responseToCommandSent.charAt(4);
                }

                if (heatEnableChar == 'Y') {
                    mHeatEnableToggleButton.setChecked(true);
                } else {
                    mHeatEnableToggleButton.setChecked(false);
                }
                char coolEnableChar = 'N'; // initialize to 'N"
                //check first if size of string is at least 6 to avoid outOfBound Exception

                if (responseToCommandSent.length() >= 6) {
                    coolEnableChar = responseToCommandSent.charAt(5);
                }

                if (coolEnableChar == 'Y') {
                    mCoolEnableToggleButton.setChecked(true);

                } else {
                    mCoolEnableToggleButton.setChecked(false);
                }
                Log.d(TAG, "heat enable char is: " + heatEnableChar);
                Log.d(TAG, "cool enable char is: " + coolEnableChar);

            default:
                Log.d(TAG, "Unknown command sent: " + commandSent + " response: " + responseToCommandSent);
                break;
        }//end of switch statement
    }
}
