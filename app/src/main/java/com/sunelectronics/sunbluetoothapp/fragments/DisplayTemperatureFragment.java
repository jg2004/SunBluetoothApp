package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.models.ControllerStatus;
import com.sunelectronics.sunbluetoothapp.models.DualChannelTemperatureController;
import com.sunelectronics.sunbluetoothapp.models.TemperatureController;
import com.sunelectronics.sunbluetoothapp.utilities.PreferenceSetting;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogWriter;

import static android.content.Context.MODE_PRIVATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH1_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH2_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC127;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X_CH2_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGING_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.RATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SET_TEMP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.STATUS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_CHAMBER_STATUS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_OUTPUT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PARAMETER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PIDA_MODE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC02;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TEMP_CONTROLLER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TERMINATE_LOGGING_SESSION;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TURN_OFF_CHAMBER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.WAIT_TIME;

public class DisplayTemperatureFragment extends Fragment {
    private static final String TAG = "DisplayTemperatureFragm";
    private static final int COMMAND_SEND_DELAY_MS = 1000;
    private static final int COMMAND_SEND_DELAY_LONG_MS = 1000;
    private static final int LOGGER_DELAY_MS = 15000;
    private Handler mHandler;
    private Runnable mDisplayUpdateRunnable, mLoggerRunnable;
    private TextView mTextViewCH1Temp, mTextViewCH2Temp, mTextViewWaitTime, mTextViewSetTemp, mTextViewRate;
    private ToggleButton mHeatEnableToggleButton, mCoolEnableToggleButton;
    private Switch mSwitchOnOff;
    private Context mContext;
    private BroadcastReceiver mDispTempBroadcastReceiver;
    private TemperatureController mTemperatureController;
    private ControllerStatus mControllerStatus;
    private TemperatureLogWriter mTemperatureLogWriter;
    private boolean mIsLoggingData;
    private View view;
    private String mControllerType;

    public interface DisplayTemperatureFragmentCallBacks {

        //implemented by HomeActivity
        void closeActivity();

        void turnOffChamber();

        void stopLoggingSession();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        // onSavedInstanceSate is not called if frag put on backstack, all state is retained!

        Log.d(TAG, "onCreate: CREATING A DISPLAYTEMPERATURE FRAGMENT!!!");
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), MODE_PRIVATE);
        mControllerType = PreferenceSetting.getControllerType(getContext());
        Log.d(TAG, "onCreate: mControllerType is " + mControllerType);

        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState of DISPTEMPFRAG not null, restoring chamberModel!");
            mIsLoggingData = savedInstanceState.getBoolean(LOGGING_STATE);
            mTemperatureController = (TemperatureController) savedInstanceState.getSerializable(TEMP_CONTROLLER);

            String fileName = savedInstanceState.getString(FILE_NAME);
            if (mIsLoggingData) {
                mTemperatureLogWriter = new TemperatureLogWriter(getContext(), mTemperatureController, fileName);
            }
        } else {
            Log.d(TAG, "onCreate: savedInstanceState is NULL");

            if (mTemperatureController == null) {
                Log.d(TAG, "CHAMBER MODEL WAS  NULL, CREATED NEW ONE");
                mTemperatureController = TemperatureController.createController(mControllerType);
            } else {
                Log.d(TAG, "CHAMBER MODEL WAS NOT NULL");
            }

            mIsLoggingData = prefs.getBoolean(LOGGING_STATE, false);
            if (mIsLoggingData) {
                String fileName = prefs.getString(FILE_NAME, null);
                if (fileName != null) {
                    mTemperatureLogWriter = new TemperatureLogWriter(getContext(), mTemperatureController, fileName);
                }
            }
        }
        Log.d(TAG, "onCreate called: mIsLoggingData is: " + mIsLoggingData);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(mTemperatureController.getResourceLayout(), container, false);
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(String.format("%s MONITOR", mTemperatureController.getName()));
            supportActionBar.show();
        }
        initializeRunnables();
        initializeViews(view);
        setHasOptionsMenu(true);
        checkStatus();
        return view;
    }

    private void checkStatus() {
        Log.d(TAG, "checkStatus: checking the status");
        BluetoothConnectionService.getInstance().write(STATUS);
    }

    private void initializeRunnables() {

        mHandler = new Handler();
        //runnable to send chamber commands every half second
        mDisplayUpdateRunnable = new Runnable() {
            @Override
            public void run() {

                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {

                    Log.d(TAG, "removing displayUpdate runnable from nHandler and stopping temperature LOGGER as well");
                    mHandler.removeCallbacks(this);
                    stopLogger();
                    return;
                }
                Log.d(TAG, "run: inside displayupdate runnable");
                String command = mTemperatureController.getNextPollingCommand();
                BluetoothConnectionService.getInstance().write(command);
                mHandler.postDelayed(this, COMMAND_SEND_DELAY_MS);
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

    private void initializeViews(final View view) {
        TextView textViewCh1Label = (TextView) view.findViewById(R.id.textViewLabelCh1);
        textViewCh1Label.setText(String.format("%s:", mTemperatureController.getCh1Label()));
        mTextViewCH1Temp = (TextView) view.findViewById(R.id.textViewCh1Temp);

        if (mTemperatureController instanceof DualChannelTemperatureController) {
            TextView textViewCh2Label = (TextView) view.findViewById(R.id.textViewCh2Label);
            textViewCh2Label.setText(String.format("%s:", ((DualChannelTemperatureController) mTemperatureController).getCh2Label()));
            mTextViewCH2Temp = (TextView) view.findViewById(R.id.textViewCh2Temp);
        }

        mTextViewRate = (TextView) view.findViewById(R.id.textViewRate);
        mTextViewWaitTime = (TextView) view.findViewById(R.id.textViewWait);
        mTextViewSetTemp = (TextView) view.findViewById(R.id.textViewSet);


        Button buttonStatus = (Button) view.findViewById(R.id.buttonStatus);
        buttonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatusFragment();
            }
        });
        Button buttonSingleSegment = (Button) view.findViewById(R.id.buttonSingleSegment);
        buttonSingleSegment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                showSingleSegmentDialog();
            }
        });
        mHeatEnableToggleButton = (ToggleButton) view.findViewById(R.id.toggleButtonHeatEnable);
        mHeatEnableToggleButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                        Snackbar.make(v, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });
        mHeatEnableToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mHeatEnableToggleButton.isChecked()) {
                    BluetoothConnectionService.getInstance().write(mTemperatureController.getHeatEnableCommand());

                } else {
                    BluetoothConnectionService.getInstance().write(mTemperatureController.getHeatDisableCommand());
                }
            }
        });
        mCoolEnableToggleButton = (ToggleButton) view.findViewById(R.id.toggleButtonCoolEnable);
        mCoolEnableToggleButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                        Snackbar.make(v, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });
        mCoolEnableToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCoolEnableToggleButton.isChecked()) {
                    BluetoothConnectionService.getInstance().write(mTemperatureController.getCoolEnableCommand());
                } else {
                    BluetoothConnectionService.getInstance().write(mTemperatureController.getCoolDisableCommand());
                }
            }
        });
        mSwitchOnOff = (Switch) view.findViewById(R.id.switchOnOff);

        //add onTouchListener to show dialog to see if user wants to proceed with shutting down
        //logger if active. Based on dialog response, proceed with turning off switch
        mSwitchOnOff.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: event occured");

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    //check if bluetooth connected. if not, show snackbar and do nothing
                    if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                        Snackbar.make(v, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                        return true;//this consumes event and prevents switch from changing state
                    }

                    if (mIsLoggingData) {
                        //show alert dialog to confirm
                        showAlertDialog(TURN_OFF_CHAMBER);
                        return true; //this consumes event and prevents switch from changing state
                    }
                }
                //return true if to consume event and not have onClick and stateChangeEvent occur
                return false;
            }
        });

        mSwitchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: switch was changed to: " + isChecked);
                if (isChecked) {

                    BluetoothConnectionService.getInstance().clearCommandsWrittenList();
                    if (!mControllerStatus.isPowerOn()) {
                        Log.d(TAG, "onCheckedChanged: chamber power not on, sending ON command");
                        BluetoothConnectionService.getInstance().write(ON);
                    }
                    Log.d(TAG, "onCheckedChanged: starting display update runnable");
                    mHandler.postDelayed(mDisplayUpdateRunnable, COMMAND_SEND_DELAY_LONG_MS);
                    Log.d(TAG, "onCheckedChanged: invalidating options menu");
                    getActivity().invalidateOptionsMenu();
                    if (mIsLoggingData) {
                        Log.d(TAG, "onCheckedChanged: starting logger, mIsLoggingData was true");
                        startLogger();
                    }

                } else {
                    Log.d(TAG, "onCheckedChanged: REMOVING display runnable because switch is off");
                    mHandler.removeCallbacks(mDisplayUpdateRunnable);
                    BluetoothConnectionService.getInstance().write(OFF);
                    //manually setting power on to false since status not updated fast enough
                    mControllerStatus.setPowerIsOn(false);
                    mHeatEnableToggleButton.setChecked(false);
                    mCoolEnableToggleButton.setChecked(false);
                    Log.d(TAG, "onCheckedChanged: invalidating options menu");
                    getActivity().invalidateOptionsMenu();
                    if (mIsLoggingData) {
                        stopLogger();
                    }
                }
            }
        });
    }

    private void showAlertDialog(String alertType) {
        Bundle args = new Bundle();
        args.putString(ALERT_TYPE, alertType);
        args.putString(ALERT_TITLE, alertType);
        args.putString(ALERT_MESSAGE, "This will terminate Logging session, proceed?");
        args.putInt(ALERT_ICON, R.drawable.ic_stop_black_24dp);
        MyAlertDialogFragment dialog = MyAlertDialogFragment.newInstance(args);
        dialog.show(getFragmentManager(), null);
    }

    public void turnOffChamberSwitch() {
        //used by HomeActivity to turn off chamber after user is shown alertDialog and presses Yes
        //to confirm shut off
        mSwitchOnOff.setChecked(false);
    }

    private void showStatusFragment() {
        Log.d(TAG, "showStatusFragment: called");
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.homeContainer, new ControllerStatusFragment(), TAG_FRAGMENT_CHAMBER_STATUS).commit();
    }

    private void showSingleSegmentDialog() {
        SingleSegDialogFragment fragment = SingleSegDialogFragment.newInstance("Enter segment");
        Log.d(TAG, "showSingleSegmentDialog: showing dialog");
        fragment.show(getFragmentManager(), "single_seg_frag");
    }

    private void showPidAModeFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homeContainer, new PidAModeFragment(), TAG_FRAGMENT_PIDA_MODE).commit();
    }

    private void showParametersFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homeContainer, ParameterFragment.newInstance(mTemperatureController), TAG_FRAGMENT_PARAMETER).commit();
    }

    private void showOutputFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homeContainer, new OutputFragment(), TAG_FRAGMENT_OUTPUT).commit();
    }

    public boolean isLoggingData() {
        return mIsLoggingData;
    }

    /**
     * used by this fragment and HomeActivity to stop logging session.
     */
    public void stopLogger() {

        Log.d(TAG, "stopLogger: called");
        mIsLoggingData = false;
        Log.d(TAG, "stopLogger: invalidate options menu called");
        getActivity().invalidateOptionsMenu();
        mHandler.removeCallbacks(mLoggerRunnable);
        closeLoggingFile();
    }

    public void closeLoggingFile() {

        if (mTemperatureLogWriter != null) {
            Log.d(TAG, "closeLoggingFile: closing OutputStreamWriter");
            mTemperatureLogWriter.closeFile();
        }
    }

    private void startLogger() {

        Log.d(TAG, "startLogger: called, logging temperature data");
        Toast.makeText(getContext(), "LOGGER STARTED", Toast.LENGTH_SHORT).show();
        mIsLoggingData = true;
        getActivity().invalidateOptionsMenu();
        mHandler.postDelayed(mLoggerRunnable, LOGGER_DELAY_MS);
    }

    private void pauseLogger() {

        Log.d(TAG, "pauseLogger: called, removing loggerRunnable callback");
        Toast.makeText(mContext, "LOGGING PAUSED", Toast.LENGTH_SHORT).show();
        mHandler.removeCallbacks(mLoggerRunnable);
        mTemperatureLogWriter.flush();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called");
        menu.setGroupEnabled(R.id.displayTempFragMenuGroup, mSwitchOnOff.isChecked());
        MenuItem startLoggingMenuItem = menu.findItem(R.id.startLogging);
        MenuItem pidAMenuItem = menu.findItem(R.id.pidAMode);
        MenuItem outputsMenuItem = menu.findItem(R.id.outputs);

        if (mIsLoggingData) {
            startLoggingMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            startLoggingMenuItem.setIcon(R.drawable.ic_action_stop_logger_red);
            startLoggingMenuItem.setEnabled(true);

        } else {
            //not logging
            startLoggingMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            startLoggingMenuItem.setIcon(null);
        }

        if (mControllerType.equals(TC02) || mControllerType.equals(PC100)) {

            pidAMenuItem.setEnabled(false);

        }
        if (!mControllerType.equals(EC1X) && !mControllerType.equals(EC127)) {
            outputsMenuItem.setTitle("Controller Outputs");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: called");
        inflater.inflate(R.menu.menu_disp_temp_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.startLogging:
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                if (item.getIcon() == null) {

                    mTemperatureLogWriter = new TemperatureLogWriter(getContext(), mTemperatureController);
                    startLogger();
                } else {
                    // stop recording icon is showing so show confirmation dialog. If user presses yes
                    // then stopLogger will be called by HomeActivity via MyAlertDialogFragment
                    showAlertDialog(TERMINATE_LOGGING_SESSION);
                }
                return true;

            case R.id.outputs:
                showOutputFragment();
                return true;

            case R.id.parameters:
                showParametersFragment();
                return true;
            case R.id.pidAMode:
                showPidAModeFragment();
                return true;
        }
        return false;
    }

    @Override
    public void onDetach() {

        Log.d(TAG, "onDetach: called, removing displayUpdate callback from mHandler");
        mHandler.removeCallbacks(mDisplayUpdateRunnable);
        if (mIsLoggingData) {
            pauseLogger();
        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDispTempBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        mContext = context;
        if (mControllerStatus == null) {
            mControllerStatus = ControllerStatus.getInstance(getContext());
        }

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
     * @param commandSent           command that was sent to bluetooth
     * @param responseToCommandSent the response to the command sent
     */
    private void updateView(String commandSent, String responseToCommandSent) {

        switch (commandSent) {

            case CH1_QUERY_COMMAND:
            case PC_QUERY_COMMAND:

                //verify that response is numeric
                if (!isNumeric(responseToCommandSent)) {
                    //if it's not, then display is out of sync with controller responses
                    //re-sync by clearing out array of commands written list
                    BluetoothConnectionService.getInstance().clearCommandsWrittenList();
                    // TODO: 1/2/2018 temporary code!!
                    Toast.makeText(mContext, "chamber temp was not numeric, clear command list to re-sync", Toast.LENGTH_LONG).show();
                    break;
                }
                mTemperatureController.setTimeStampOfReading(System.currentTimeMillis());
                mTemperatureController.setCh1Reading(responseToCommandSent);
                mTextViewCH1Temp.setText(mTemperatureController.getCh1Reading());
                break;

            case CH2_QUERY_COMMAND:
            case EC1X_CH2_QUERY_COMMAND:

                if (!isNumeric(responseToCommandSent)) {
                    //if it's not, then display is out of sync with controller responses
                    //re-sync by clearing out array of commands written list
                    BluetoothConnectionService.getInstance().clearCommandsWrittenList();
                    break;
                }
                if (mTemperatureController instanceof DualChannelTemperatureController) {
                    ((DualChannelTemperatureController) mTemperatureController).setCh2Reading(responseToCommandSent);
                    mTextViewCH2Temp.setText(((DualChannelTemperatureController) mTemperatureController).getCh2Reading());
                }

                break;

            case SET_TEMP:
                if (!isNumeric(responseToCommandSent) && !responseToCommandSent.contains("NONE")) {
                    BluetoothConnectionService.getInstance().clearCommandsWrittenList();
                    // TODO: 1/2/2018 temporary code!!
                    Toast.makeText(mContext, "set temp was not numeric and not NONE", Toast.LENGTH_LONG).show();
                    break;
                }
                if (responseToCommandSent.contains("NONE")) {
                    mTemperatureController.setCurrentSetPoint("NONE");
                    mTextViewSetTemp.setText(mTemperatureController.getCurrentSetPoint());
                } else {
                    mTemperatureController.setCurrentSetPoint(responseToCommandSent);
                    mTextViewSetTemp.setText(mTemperatureController.getCurrentSetPoint());
                }
                break;

            case WAIT_TIME:

                if (!responseToCommandSent.contains(":") && !responseToCommandSent.contains("FOREVER")) {
                    BluetoothConnectionService.getInstance().clearCommandsWrittenList();
                    // TODO: 1/2/2018 temporary code!!
                    Toast.makeText(mContext, "Wait time did not contain colon symbol", Toast.LENGTH_LONG).show();
                    break;
                }
                if (responseToCommandSent.contains("FOREVER")) {
                    mTextViewWaitTime.setText(R.string.forever);
                } else {
                    mTextViewWaitTime.setText(responseToCommandSent);
                }
                break;

            case RATE:

                if (!isNumeric(responseToCommandSent)) {
                    //if it's not, then display is out of sync with controller responses
                    //re-sync by clearing out array of commands written list
                    BluetoothConnectionService.getInstance().clearCommandsWrittenList();
                    // TODO: 1/2/2018 temporary code!!
                    Toast.makeText(mContext, "rate was not numeric, clear command list to re-sync", Toast.LENGTH_LONG).show();
                    break;
                }
                mTextViewRate.setText(responseToCommandSent);
                break;

            case STATUS:

                // TODO: 11/25/2017 take following Toast command out?
                if (responseToCommandSent.length() < 12) {
                    BluetoothConnectionService.getInstance().clearCommandsWrittenList();
                    Toast.makeText(getContext(), "Status length less than 12, re-syncing", Toast.LENGTH_SHORT).show();
                    return;
                }
                mControllerStatus.setStatusMessages(responseToCommandSent);
                mHeatEnableToggleButton.setChecked(mControllerStatus.isHeatEnableOn());
                mCoolEnableToggleButton.setChecked(mControllerStatus.isCoolEnableOn());

                if (mControllerStatus.isPowerOn() && !mSwitchOnOff.isChecked()) {
                    Log.d(TAG, "updateView: chamber status is on, and switch is off, turning on switch");

                    mSwitchOnOff.setChecked(mControllerStatus.isPowerOn());

                } else if (!mControllerStatus.isPowerOn() && mSwitchOnOff.isChecked()) {
                    Log.d(TAG, "updateView: chamber status is OFF so turning off switch");

                    mSwitchOnOff.setChecked(mControllerStatus.isPowerOn());
                }

                break;

            default:
                Log.d(TAG, "Unknown command sent: " + commandSent + " response: " + responseToCommandSent);
                break;
        }//end of switch statement
    }


    /**
     * This verifies that commands sent such as cham?, rate?, user? have a valid numeric response
     * if not, then controller sent back an invalid response that will cause display to be out of
     * sync with controller responses i.e CHAM:YYYYNNNNNYY
     *
     * @param responseToCommandSent this is the response to the command sent to controller
     * @return true if valid double, return false otherwise
     */
    private boolean isNumeric(String responseToCommandSent) {

        try {
            Double test = Double.parseDouble(responseToCommandSent);
            return !test.isNaN();
        } catch (Exception e) {
            return false;
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), MODE_PRIVATE);
        prefs.edit().putBoolean(LOGGING_STATE, mIsLoggingData).apply();
        Log.d(TAG, "storing saved preference in: " + getActivity().getPackageName());
        Log.d(TAG, "mIsLogginData value is " + mIsLoggingData);
        if (mIsLoggingData && mTemperatureLogWriter != null) {
            prefs.edit().putString(FILE_NAME, mTemperatureLogWriter.getFileName()).apply();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: called, storing boolean value mIsoggingData: " + mIsLoggingData);
        outState.putBoolean(LOGGING_STATE, mIsLoggingData);
        outState.putSerializable(TEMP_CONTROLLER, mTemperatureController);
        if (mIsLoggingData) {
            outState.putString(FILE_NAME, mTemperatureLogWriter.getFileName());
        }
        super.onSaveInstanceState(outState);
    }
}