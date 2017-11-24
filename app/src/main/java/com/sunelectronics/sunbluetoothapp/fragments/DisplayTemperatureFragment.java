package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.models.ChamberModel;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogWriter;

import java.util.ArrayList;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHAM_TEMP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.COFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.HOFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.HON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.RATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SET_TEMP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.STATUS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_CHAMBER_STATUS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.USER_TEMP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.WAIT_TIME;

public class DisplayTemperatureFragment extends Fragment {
    private static final String TAG = "DisplayTemperatureFragm";
    private static final int COMMAND_SEND_DELAY_MS = 500;
    private static final int COMMAND_SEND_DELAY_LONG_MS = 1000;
    private static final int LOGGER_DELAY_MS = 3000;
    private Handler mHandler;
    private Runnable mDisplayUpdateRunnable, mLoggerRunnable;
    private Button mButtonSingleSegment, mButtonStatus;
    private TextView mTextViewChamberTemp, mTextViewUserTemp, mTextViewWaitTime, mTextViewSetTemp;
    private TextView mTextViewRate;
    private ToggleButton mHeatEnableToggleButton, mCoolEnableToggleButton;
    private Switch mSwitchOnOff;
    private Context mContext;
    private ArrayList<String> mCommands = new ArrayList<>();
    private int mCommandCounter;
    private BroadcastReceiver mDispTempBroadcastReceiver;
    private ChamberModel mChamberModel;
    private MenuItem mViewLogMenuItem;
    private TemperatureLogWriter mTemperatureLogWriter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_display_temps, container, false);
        ((HomeActivity) getActivity()).getSupportActionBar().setTitle("DispTempFrag");
        initializeCommandArrayList();
        initializeRunnables();
        initializeViews(view);
        setHasOptionsMenu(true);
        checkStatus();
        return view;
    }

    private void checkStatus() {
        BluetoothConnectionService.getInstance(getContext()).write(STATUS);
    }

    private void initializeRunnables() {

        mHandler = new Handler();
        //runnable to send chamber commands every half second
        mDisplayUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                String command = mCommands.get(mCommandCounter);
                BluetoothConnectionService.getInstance(mContext).write(command);
                mHandler.postDelayed(this, COMMAND_SEND_DELAY_MS);
                // reset mCommandCounter to 0 when it reaches the end of list, otherwise increment
                if (mCommandCounter >= mCommands.size() - 1) {
                    mCommandCounter = 0;
                } else {
                    mCommandCounter++;
                }
            }
        };
        //runnable to write temperature data to a text file
        mLoggerRunnable = new Runnable() {
            @Override
            public void run() {
                mTemperatureLogWriter.log();
                mHandler.postDelayed(this, LOGGER_DELAY_MS);
            }
        };
    }

    private void initializeCommandArrayList() {
        //this is the list of commands that are iterated through every COMMAND_SEND_DELAY_MS ms
        //by the mHandler

        mCommands.add(CHAM_TEMP);
        mCommands.add(USER_TEMP);
        mCommands.add(SET_TEMP);
        mCommands.add(RATE);
        mCommands.add(WAIT_TIME);
        mCommands.add(STATUS);
    }

    private void initializeViews(View view) {
        mTextViewChamberTemp = (TextView) view.findViewById(R.id.textViewChamTemp);
        mTextViewUserTemp = (TextView) view.findViewById(R.id.textViewUserTemp);
        mTextViewRate = (TextView) view.findViewById(R.id.textViewRate);
        mTextViewWaitTime = (TextView) view.findViewById(R.id.textViewWait);
        mTextViewSetTemp = (TextView) view.findViewById(R.id.textViewSet);
        mButtonStatus = (Button) view.findViewById(R.id.buttonStatus);
        mButtonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatusFragment();
            }
        });
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
                Log.d(TAG, "onClick: switch was clicked");
                if (mSwitchOnOff.isChecked()) {
                    BluetoothConnectionService.getInstance(mContext).clearCommandsWrittenList();
                    BluetoothConnectionService.getInstance(mContext).write(ON);
                } else {
                    BluetoothConnectionService.getInstance(mContext).write(OFF);
                    BluetoothConnectionService.getInstance(mContext).clearCommandsWrittenList();
                    mHeatEnableToggleButton.setChecked(false);
                    mCoolEnableToggleButton.setChecked(false);
                }
            }
        });

        mSwitchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: switch was changed to: " + isChecked);
                if (isChecked) {
                    BluetoothConnectionService.getInstance(mContext).clearCommandsWrittenList();
                    mHandler.postDelayed(mDisplayUpdateRunnable, COMMAND_SEND_DELAY_LONG_MS);
                } else {
                    mHandler.removeCallbacks(mDisplayUpdateRunnable);
                }
            }
        });
    }

    private void showStatusFragment() {
        Log.d(TAG, "showStatusFragment: called");
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.homeContainer, new ChamberStatusFragment(), TAG_FRAGMENT_CHAMBER_STATUS).commit();
    }

    private void showSingleSegmentDialog() {
        SingleSegDialogFragment fragment = SingleSegDialogFragment.newInstance("Enter segment");
        Log.d(TAG, "showSingleSegmentDialog: showing dialog");
        fragment.show(getFragmentManager(), "single_seg_frag");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mViewLogMenuItem = menu.findItem(R.id.viewLogs);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_disp_temp_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.startLogging:

                if (item.getIcon() == null) {
                    //mViewLogMenuItem.setVisible(false);
                    mViewLogMenuItem.setEnabled(false);
                    item.setIcon(R.drawable.ic_action_stop_logger_red);
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    mTemperatureLogWriter = new TemperatureLogWriter(getContext(), mChamberModel);
                    mHandler.post(mLoggerRunnable);

                } else {
                    // stop recording icon is showing
                    item.setIcon(null);
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                    mViewLogMenuItem.setVisible(true);
                    mHandler.removeCallbacks(mLoggerRunnable);
                    mTemperatureLogWriter.closeFile();
                }
                break;
        }
        return true;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: called");
        Toast.makeText(mContext, "Logging paused", Toast.LENGTH_SHORT).show();
        mHandler.removeCallbacks(mDisplayUpdateRunnable);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDispTempBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        mContext = context;
        mChamberModel = new ChamberModel();
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
    }

    /**
     * method used to update the ChamberModel object and textViews according to response from bluetooth
     *
     * @param commandSent
     * @param responseToCommandSent
     */
    private void updateView(String commandSent, String responseToCommandSent) {

        switch (commandSent) {

            case CHAM_TEMP:

                mChamberModel.setTimeStamp(System.currentTimeMillis());
                mChamberModel.setCh1Reading(responseToCommandSent);
                mTextViewChamberTemp.setText(mChamberModel.getCh1Reading());
                break;

            case USER_TEMP:

                mChamberModel.setCh2Reading(responseToCommandSent);
                mTextViewUserTemp.setText(mChamberModel.getCh2Reading());
                break;

            case SET_TEMP:
                if (responseToCommandSent.contains("NONE")) {
                    mChamberModel.setSetReading("NONE");
                    mTextViewSetTemp.setText(mChamberModel.getSetReading());
                } else {
                    mChamberModel.setSetReading(responseToCommandSent);
                    mTextViewSetTemp.setText(mChamberModel.getSetReading());
                }
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

                char powerOnChar;
                powerOnChar = responseToCommandSent.charAt(0);
                Log.d(TAG, "powerOnChar is: " + powerOnChar);
                if (powerOnChar == 'Y' && !mSwitchOnOff.isChecked()) {
                    mSwitchOnOff.setChecked(true);
                } else if (powerOnChar == 'N' && mSwitchOnOff.isChecked()) {

                    mSwitchOnOff.setChecked(false);
                }
                break;
            default:
                Log.d(TAG, "Unknown command sent: " + commandSent + " response: " + responseToCommandSent);
                break;
        }//end of switch statement
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
    }
}