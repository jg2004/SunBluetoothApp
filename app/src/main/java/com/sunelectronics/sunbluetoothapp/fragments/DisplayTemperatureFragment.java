package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.sunelectronics.sunbluetoothapp.models.ChamberModel;
import com.sunelectronics.sunbluetoothapp.models.ChamberStatus;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogWriter;

import java.util.ArrayList;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHAM_TEMP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.COFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DISPLAY_TEMP_FRAG_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.HOFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.HON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGING_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.RATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SET_TEMP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.STATUS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_CHAMBER_STATUS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_OUTPUT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PARAMETER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PIDA_MODE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TERMINATE_LOGGING_SESSION;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TURN_OFF_CHAMBER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.USER_TEMP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.WAIT_TIME;

public class DisplayTemperatureFragment extends Fragment {
    private static final String TAG = "DisplayTemperatureFragm";
    private static final int COMMAND_SEND_DELAY_MS = 1000;
    private static final int COMMAND_SEND_DELAY_LONG_MS = 1000;
    private static final int LOGGER_DELAY_MS = 15000;
    private Handler mHandler;
    private Runnable mDisplayUpdateRunnable, mLoggerRunnable;
    private TextView mTextViewChamberTemp, mTextViewUserTemp, mTextViewWaitTime, mTextViewSetTemp;
    private TextView mTextViewRate;
    private ToggleButton mHeatEnableToggleButton, mCoolEnableToggleButton;
    private Switch mSwitchOnOff;
    private Context mContext;
    private ArrayList<String> mCommands = new ArrayList<>();
    private int mCommandCounter;
    private BroadcastReceiver mDispTempBroadcastReceiver;
    private ChamberModel mChamberModel;
    private ChamberStatus mChamberStatus;
    private TemperatureLogWriter mTemperatureLogWriter;
    private boolean mIsLoggingData;
    private View view;

    public interface DisplayTemperatureFragmentCallBacks {

        //implemented by HomeActivity
        void closeActivity();

        void turnOffChamber();

        void stopLoggingSession();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        // onSavedInstanceSate is not called if frag put on backstack, all state is retained!

        Log.d(TAG, "onCreate called: mIsLoggingData is: " + mIsLoggingData);
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mIsLoggingData = savedInstanceState.getBoolean(LOGGING_STATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(R.layout.fragment_display_temps, container, false);
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(DISPLAY_TEMP_FRAG_TITLE);
            supportActionBar.show();
        }
        initializeCommandArrayList();
        initializeRunnables();
        initializeViews(view);
        setHasOptionsMenu(true);
        checkStatus();
        return view;
    }

    private void checkStatus() {
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
                String command = mCommands.get(mCommandCounter);

                BluetoothConnectionService.getInstance().write(command);
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

    private void initializeViews(final View view) {
        mTextViewChamberTemp = (TextView) view.findViewById(R.id.textViewChamTemp);
        mTextViewUserTemp = (TextView) view.findViewById(R.id.textViewUserTemp);
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
                    BluetoothConnectionService.getInstance().write(HON);

                } else {
                    BluetoothConnectionService.getInstance().write(HOFF);
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
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(v, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mCoolEnableToggleButton.isChecked()) {
                    BluetoothConnectionService.getInstance().write(CON);
                } else {
                    BluetoothConnectionService.getInstance().write(COFF);
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
                    if (!mChamberStatus.isPowerOn()) {
                        Log.d(TAG, "onCheckedChanged: chamber power not on, sending ON command");
                        BluetoothConnectionService.getInstance().write(ON);
                    } else {
                        // TODO: 11/25/2017 debugging code, ok to remove
                        Log.d(TAG, "onCheckedChanged: chamber is powered on, NOT sending ON command");
                    }
                    mHandler.postDelayed(mDisplayUpdateRunnable, COMMAND_SEND_DELAY_LONG_MS);
                    //mStartLoggingMenuItem.setEnabled(true);
                    Log.d(TAG, "onCheckedChanged: invalidating options menu");
                    getActivity().invalidateOptionsMenu();
                    if (mIsLoggingData) {
                        Log.d(TAG, "onCheckedChanged: starting logger, mIsLoggingData was true");
                        startLogger();
                    }

                } else {
                    mHandler.removeCallbacks(mDisplayUpdateRunnable);
                    BluetoothConnectionService.getInstance().write(OFF);
                    //manually setting power on to false since status not updated fast enough
                    mChamberStatus.setPowerIsOn(false);
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
        ft.replace(R.id.homeContainer, new ChamberStatusFragment(), TAG_FRAGMENT_CHAMBER_STATUS).commit();
    }

    private void showSingleSegmentDialog() {
        SingleSegDialogFragment fragment = SingleSegDialogFragment.newInstance("Enter segment");
        Log.d(TAG, "showSingleSegmentDialog: showing dialog");
        fragment.show(getFragmentManager(), "single_seg_frag");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called");
        menu.setGroupEnabled(R.id.displayTempFragMenuGroup, mSwitchOnOff.isChecked());
        MenuItem startLoggingMenuItem = menu.findItem(R.id.startLogging);
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

                    mTemperatureLogWriter = new TemperatureLogWriter(getContext(), mChamberModel);
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

    private void showPidAModeFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homeContainer, new PidAModeFragment(), TAG_FRAGMENT_PIDA_MODE).commit();
    }


    private void showParametersFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.homeContainer, new ParameterFragment(), TAG_FRAGMENT_PARAMETER).commit();
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

        Log.d(TAG, "startLogger: called, starting loggerRunnable");
        mIsLoggingData = true;
        Log.d(TAG, "startLogger: invalidate options menu called");
        getActivity().invalidateOptionsMenu();
        mHandler.postDelayed(mLoggerRunnable, LOGGER_DELAY_MS);
    }

    private void pauseLogger() {

        Log.d(TAG, "pauseLogger: called, removing loggerRunnable callback");
        mHandler.removeCallbacks(mLoggerRunnable);
    }

    @Override
    public void onDetach() {

        Log.d(TAG, "onDetach: called, removing displayUpdate callback from mHandler");
        Toast.makeText(mContext, "Logging paused", Toast.LENGTH_SHORT).show();
        mHandler.removeCallbacks(mDisplayUpdateRunnable);
        pauseLogger();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDispTempBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        mContext = context;
        if (mChamberModel == null) {
            mChamberModel = new ChamberModel();
        }
        if (mChamberStatus == null) {
            mChamberStatus = new ChamberStatus();
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
                    mTextViewWaitTime.setText(R.string.forever);
                } else {
                    mTextViewWaitTime.setText(responseToCommandSent);
                }
                break;

            case RATE:

                mTextViewRate.setText(responseToCommandSent);
                break;

            case STATUS:

                // TODO: 11/25/2017 take following Toast command out?
                if (responseToCommandSent.length() < 12) {
                    Log.d(TAG, "response " + responseToCommandSent + " from status was less than 12");
                    Toast.makeText(getContext(), "Status length less than 12", Toast.LENGTH_SHORT).show();
                    return;
                }
                mChamberStatus.setStatusMessages(responseToCommandSent);
                mHeatEnableToggleButton.setChecked(mChamberStatus.isHeatEnableOn());
                mCoolEnableToggleButton.setChecked(mChamberStatus.isCoolEnableOn());

                if (mChamberStatus.isPowerOn() && !mSwitchOnOff.isChecked()) {

                    mSwitchOnOff.setChecked(mChamberStatus.isPowerOn());

                } else if (!mChamberStatus.isPowerOn() && mSwitchOnOff.isChecked()) {

                    mSwitchOnOff.setChecked(mChamberStatus.isPowerOn());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: called");
        outState.putBoolean(LOGGING_STATE, mIsLoggingData);
        super.onSaveInstanceState(outState);
    }
}