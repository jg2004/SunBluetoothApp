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

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.interfaces.IChamberOffSwitch;
import com.sunelectronics.sunbluetoothapp.interfaces.ILogger;
import com.sunelectronics.sunbluetoothapp.models.TC01Controller;
import com.sunelectronics.sunbluetoothapp.models.TC01SerialSendAgent;
import com.sunelectronics.sunbluetoothapp.models.TemperatureController;
import com.sunelectronics.sunbluetoothapp.utilities.PreferenceSetting;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogWriter;

import static android.content.Context.MODE_PRIVATE;
import static com.sunelectronics.sunbluetoothapp.R.id.buttonSingleSegment;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGING_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SWITCH_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PARAMETER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_CMD_ERROR;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_CYCLE_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_INFINITY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_RESET_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_SET_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_TEMP_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_UTL_INT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_WAIT_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TEMP_CONTROLLER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TERMINATE_LOGGING_SESSION;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TURN_OFF_CHAMBER;

public class TC01DispTempFragment extends Fragment implements IChamberOffSwitch, ILogger {
    private static final String TAG = "TC01DispTempFrag";
    private static final int COMMAND_SEND_DELAY_MS = 1000;
    private static final int LOGGER_DELAY_MS = 15000;
    private Handler mHandler;
    private Runnable mDisplayUpdateRunnable, mLoggerRunnable;
    private TextView mTextViewTemp, mTextViewWaitTime, mTextViewSetTemp, mTextViewCycleNumber;
    private Switch mSwitchOnOff;
    private Context mContext;
    private BroadcastReceiver mDispTempBroadcastReceiver;
    private TemperatureController mTemperatureController;
    private TemperatureLogWriter mTemperatureLogWriter;
    private boolean mIsLoggingData;
    private boolean mResponseReceived = true;
    private View view;
    private String mControllerType;
    private String mCommandSent = "NO COMMAND SENT";
    private TC01SerialSendAgent mSerialSendAgent;
    private int mMissedResponses;
    private Button mButtonSingleSegment;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        // onSavedInstanceSate is not called if frag put on backstack, all state is retained!

        Log.d(TAG, "onCreate: CREATING A DISPLAYTEMPERATURE FRAGMENT!!!");
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), MODE_PRIVATE);
        mControllerType = PreferenceSetting.getControllerType(getContext());

        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState of TC01DispTempFrag not null, restoring chamberModel!");
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
        return view;
    }

    private void initializeRunnables() {

        mHandler = new Handler();
        mSerialSendAgent = TC01SerialSendAgent.getInstance(mHandler);
        //runnable to send TC01 commands every half second
        mDisplayUpdateRunnable = new Runnable() {
            @Override
            public void run() {

                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {

                    Log.d(TAG, "removing displayUpdate runnable from nHandler and stopping temperature LOGGER as well");
                    Toast.makeText(getContext(), "Connection Lost, turning off switch", Toast.LENGTH_LONG).show();
                    turnOffChamberSwitch();
                    //mHandler.removeCallbacks(this);
                    //stopLogger();
                    return;
                }
                mMissedResponses = !mResponseReceived ? mMissedResponses + 1 : 0;
                Log.d(TAG, "run: mMissedResponses: " + mMissedResponses);

                //if no response after 3 consecutive writes, turn off switch

                if (mMissedResponses > 2) {
                    Snackbar.make(view, "Controller Not Responding - check controller power", Snackbar.LENGTH_INDEFINITE).show();
                    turnOffChamberSwitch();
                    return;
                }
                Log.d(TAG, "run: inside display update runnable");
                mCommandSent = mTemperatureController.getNextPollingCommand();
                mSerialSendAgent.sendCommand(mCommandSent);
                mResponseReceived = false;
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
        TextView textViewTempLabel = (TextView) view.findViewById(R.id.textViewLabelCh1);
        textViewTempLabel.setText(String.format("%s:", mTemperatureController.getCh1Label()));
        mTextViewTemp = (TextView) view.findViewById(R.id.textViewCh1Temp);
        mTextViewWaitTime = (TextView) view.findViewById(R.id.textViewWait);
        mTextViewSetTemp = (TextView) view.findViewById(R.id.textViewSet);
        mTextViewCycleNumber = (TextView) view.findViewById(R.id.textViewCycleNumber);

        mButtonSingleSegment= (Button) view.findViewById(buttonSingleSegment);
        mButtonSingleSegment.setEnabled(false);
        mButtonSingleSegment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                showSingleSegmentDialog();
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
                    mMissedResponses = 0;
                    Log.d(TAG, "onCheckedChanged: starting display update runnable");
                    mHandler.postDelayed(mDisplayUpdateRunnable, COMMAND_SEND_DELAY_MS);
                    mButtonSingleSegment.setEnabled(true);
                    Log.d(TAG, "onCheckedChanged: invalidating options menu");
                    getActivity().invalidateOptionsMenu();
                    if (mIsLoggingData) {
                        Log.d(TAG, "onCheckedChanged: starting logger, mIsLoggingData was true");
                        startLogger();
                    }

                } else {
                    Log.d(TAG, "onCheckedChanged: REMOVING display runnable because switch is off");
                    mHandler.removeCallbacks(mDisplayUpdateRunnable);
                    Log.d(TAG, "onCheckedChanged: invalidating options menu");
                    getActivity().invalidateOptionsMenu();
                    if (mIsLoggingData) {
                        stopLogger();
                    }
                }
            }
        });

        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), MODE_PRIVATE);
        mSwitchOnOff.setChecked(prefs.getBoolean(SWITCH_STATE,false));
        Log.d(TAG, "initializeViews: set switch state from prefs to: " + prefs.getBoolean(SWITCH_STATE,false));
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
        Log.d(TAG, "turnOffChamberSwitch: called");
        mSwitchOnOff.setChecked(false);
    }

    private void showSingleSegmentDialog() {

        TC01SingleSegDialogFragment fragment = TC01SingleSegDialogFragment.newInstance(getString(R.string.enter_segment), (TC01Controller) mTemperatureController);
        Log.d(TAG, "showSingleSegmentDialog: showing dialog");
        fragment.show(getFragmentManager(), "single_seg_frag");
    }

    private void showParametersFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homeContainer, TC01ParamIOFragment.newInstance(mTemperatureController), TAG_FRAGMENT_PARAMETER).commit();
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
        //menu.setGroupEnabled(R.id.displayTempFragMenuGroup, mSwitchOnOff.isChecked());
        menu.setGroupEnabled(R.id.displayTempFragMenuGroup,true);
        MenuItem startLoggingMenuItem = menu.findItem(R.id.startLogging);
        startLoggingMenuItem.setEnabled(mSwitchOnOff.isChecked());

        if (mIsLoggingData) {
            startLoggingMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            startLoggingMenuItem.setIcon(R.drawable.ic_action_stop_logger_red);
            startLoggingMenuItem.setEnabled(true);

        } else {
            //not logging
            startLoggingMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            startLoggingMenuItem.setIcon(null);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: called");
        inflater.inflate(R.menu.menu_tc01_disp_temp_frag, menu);
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

            case R.id.controllerReset:
                mSerialSendAgent.sendCommand(TC01_RESET_COMMAND);
                return true;

            case R.id.parameters:
                showParametersFragment();
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

        mDispTempBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String responseToCommandSent = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                mResponseReceived = true;
                if (String.valueOf(responseToCommandSent.charAt(0)).equals("I")) {
                    //timeout interrupt char received
                    showSnackBar();
                    return;
                }
                if (responseToCommandSent.equals(TC01_CMD_ERROR)) {
                    Snackbar.make(view, "COMMAND ERROR", Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (responseToCommandSent.contains(TC01_UTL_INT)) {
                    Snackbar.make(view, "UTL interrupt received. Controller temperature exceeded UTL", Snackbar.LENGTH_INDEFINITE).show();
                    Log.d(TAG, "onReceive: UTL interrupt, turning off switch");
                    turnOffChamberSwitch();
                    return;
                }
                String action = intent.getAction();
                Log.d(TAG, "Broadcast received: \n action: " + action + "\n" + "Command Sent: " +
                        mCommandSent + "\n" + "Response: " + responseToCommandSent);
                updateView(mCommandSent, responseToCommandSent);
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


        //verify that response is numeric
        if (!isNumeric(responseToCommandSent)) {
            //if it's not, then display is out of sync with controller responses
            //re-sync by clearing out array of commands written list
            //BluetoothConnectionService.getInstance().clearCommandsWrittenList();
            // TODO: 1/2/2018 temporary code!!
            Toast.makeText(mContext, "response was not numeric, clear command list to re-sync", Toast.LENGTH_LONG).show();
            return;
        }
        switch (commandSent) {

            case TC01_TEMP_QUERY:

                mTemperatureController.setTimeStampOfReading(System.currentTimeMillis());
                mTemperatureController.setCh1Reading(responseToCommandSent);
                mTextViewTemp.setText(String.format("%s C", mTemperatureController.getCh1Reading()));
                break;

            case TC01_SET_QUERY:

                mTemperatureController.setCurrentSetPoint(responseToCommandSent);
                mTextViewSetTemp.setText(String.format("%s C", mTemperatureController.getCurrentSetPoint()));
                break;

            case TC01_WAIT_QUERY:

                if (isWaitForever(responseToCommandSent)) {
                    mTextViewWaitTime.setText(R.string.forever);

                } else {
                    mTextViewWaitTime.setText(responseToCommandSent + " " + ((TC01Controller) mTemperatureController).getTimeUnits());
                }

                break;

            case TC01_CYCLE_QUERY:

                if (responseToCommandSent.equals(TC01_INFINITY)) {
                    mTextViewCycleNumber.setText(R.string.none);
                } else {
                    mTextViewCycleNumber.setText(responseToCommandSent);
                }
                break;

            default:
                Log.d(TAG, "Unknown command sent: " + commandSent + " response: " + responseToCommandSent);
                break;
        }//end of switch statement
    }

    private void showSnackBar() {

        final Snackbar snackbar = Snackbar.make(view, R.string.timeout_message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.reset, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSerialSendAgent.sendCommand("R");
            }
        });
        snackbar.show();
    }


    private boolean isWaitForever(String responseToCommandSent) {
        try {
            Double test = Double.parseDouble(responseToCommandSent);
            return (test > 1800);

        } catch (NumberFormatException e) {
            Log.d(TAG, "isWaitForever: numberformat exception");
            return false;
        }

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
        prefs.edit().putBoolean(SWITCH_STATE, mSwitchOnOff.isChecked()).apply();
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