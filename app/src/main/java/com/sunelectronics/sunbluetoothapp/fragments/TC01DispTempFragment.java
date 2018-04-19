package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.interfaces.ChartDataCallback;
import com.sunelectronics.sunbluetoothapp.interfaces.IChamberOffSwitch;
import com.sunelectronics.sunbluetoothapp.interfaces.ILogger;
import com.sunelectronics.sunbluetoothapp.models.TC01Controller;
import com.sunelectronics.sunbluetoothapp.models.TC01SerialSendAgent;
import com.sunelectronics.sunbluetoothapp.models.TemperatureController;
import com.sunelectronics.sunbluetoothapp.utilities.ChartUtilityHelperClass;
import com.sunelectronics.sunbluetoothapp.utilities.LoadChartTask;
import com.sunelectronics.sunbluetoothapp.utilities.PreferenceSetting;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogReader;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.util.Log.d;
import static android.view.View.VISIBLE;
import static com.sunelectronics.sunbluetoothapp.R.id.buttonSingleSegment;
import static com.sunelectronics.sunbluetoothapp.R.id.lineChart;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHART_VISIBLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER_ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGER_START_TIME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGING_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.START_TIME;
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
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TEMP_FILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TERMINATE_LOGGING_SESSION;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TURN_OFF_CHAMBER;

public class TC01DispTempFragment extends Fragment implements IChamberOffSwitch, ILogger, ChartDataCallback {
    private static final String TAG = "TC01DispTempFrag";
    private static final int COMMAND_SEND_DELAY_MS = 1000;
    private static final int LOGGER_DELAY_MS = 20000;
    private static final int LIVE_CHART_DELAY_MS = 20000;
    private static final int LIVE_CHART_INIT_DELAY_MS = 3000;
    private Handler mHandler;
    private Runnable mDisplayUpdateRunnable, mLoggerRunnable, mLiveChartRunnable;
    private TextView mTextViewTemp, mTextViewWaitTime, mTextViewSetTemp, mTextViewCycleNumber;
    private Switch mSwitchOnOff;
    private Context mContext;
    private BroadcastReceiver mDispTempBroadcastReceiver;
    private TemperatureController mTemperatureController;
    private TemperatureLogWriter mTemperatureLogWriter;
    private boolean mIsLoggingData, mIsLandscape, mControllerOn, mChartVisible;
    private boolean mResponseReceived = true;
    private View view;
    private String mCommandSent = "NO COMMAND SENT";
    private TC01SerialSendAgent mSerialSendAgent;
    private int mMissedResponses;
    private Button mButtonSingleSegment;
    private LineChart mLineChart;
    private LineDataSet mLineDataSetTemps, mLineDataSetSetPoints;
    private LineData mLineData;
    private long mLiveChartStartTime, mLoggerStartTime;
    private ActionBar mSupportActionBar;
    private Description mChartDescription;
    private ProgressBar progressBar;
    private boolean taskBusy;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        // onSavedInstanceSate is not called if frag put on backstack, all state is retained!

        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        String controllerType = PreferenceSetting.getControllerType(getContext());
        mIsLandscape = (getResources().getConfiguration().orientation) == Configuration.ORIENTATION_LANDSCAPE;

        if (savedInstanceState != null) {
            restoreStateFromSavedInstanceState(savedInstanceState);

        } else {

            if (mTemperatureController == null) {
                mTemperatureController = TemperatureController.createController(controllerType);
                restorStateFromPrefs();
            }
        }
    }

    private void restoreStateFromSavedInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "restoreStateFromSavedInstanceState called as savedInstanceState was not NULL");
        mIsLoggingData = savedInstanceState.getBoolean(LOGGING_STATE);
        mControllerOn = savedInstanceState.getBoolean(CONTROLLER_ON);
        mLiveChartStartTime = savedInstanceState.getLong(START_TIME);
        mLoggerStartTime = savedInstanceState.getLong(LOGGER_START_TIME);
        mChartVisible = savedInstanceState.getBoolean(CHART_VISIBLE);
        mTemperatureController = (TemperatureController) savedInstanceState.getSerializable(TEMP_CONTROLLER);
        String fileName = savedInstanceState.getString(FILE_NAME);

        if (mIsLoggingData) {
            mTemperatureLogWriter = new TemperatureLogWriter(getContext(), mTemperatureController, fileName);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(mTemperatureController.getResourceLayout(), container, false);
        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setTitle(String.format("%s MONITOR", mTemperatureController.getName()));
            mSupportActionBar.show();
        }

        initializeRunnables();
        initializeViews(view);
        if (mIsLandscape) makeViewsInvisible(view);

        TemperatureLogReader reader = new TemperatureLogReader(getContext());

        if (reader.fileExists(TEMP_FILE)) {
            d(TAG, "onCreateView: temp file exists, loading file from asyncTask");
            LoadChartTask task = new LoadChartTask(this);
            task.execute(reader.getFileContents(TEMP_FILE));

        } else {
            initializeChart();
        }

        setHasOptionsMenu(true);
        return view;
    }

    private void initializeChart() {
        initializeChartLabels();
        if (mLineData == null) {
            d(TAG, "Line data was NULL, initialize charts");
            initializeChartData();
        }
    }

    private void initializeChartLabels() {
        Log.d(TAG, "initializeChartLabels: called");
        ChartUtilityHelperClass.formatChart(mLineChart);
        mChartDescription = ChartUtilityHelperClass.getFormattedDescription();
        mLineChart.setDescription(mChartDescription);
    }

    private void initializeChartData() {
        d(TAG, "initializeChartData: called");
        List<Entry> tempEntries = new ArrayList<>();
        List<Entry> setPointEntries = new ArrayList<>();
        mLineDataSetTemps = new LineDataSet(tempEntries, "TEMP");
        mLineDataSetTemps.setDrawCircles(false);
        mLineDataSetTemps.setDrawValues(false);
        mLineDataSetTemps.setColor(Color.BLUE);
        mLineDataSetSetPoints = new LineDataSet(setPointEntries, "SET");
        mLineDataSetSetPoints.setDrawCircles(false);
        mLineDataSetSetPoints.setDrawValues(false);
        mLineDataSetSetPoints.setColor(Color.BLACK);
        mLineData = new LineData(mLineDataSetTemps, mLineDataSetSetPoints);
    }

    private void initializeViews(final View view) {
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        TextView textViewTempLabel = (TextView) view.findViewById(R.id.textViewLabelCh1);
        textViewTempLabel.setText(String.format("%s:", mTemperatureController.getCh1Label()));
        mLineChart = (LineChart) view.findViewById(lineChart);
        mTextViewTemp = (TextView) view.findViewById(R.id.textViewCh1Temp);
        mTextViewWaitTime = (TextView) view.findViewById(R.id.textViewWait);
        mTextViewSetTemp = (TextView) view.findViewById(R.id.textViewSet);
        mTextViewCycleNumber = (TextView) view.findViewById(R.id.textViewCycleNumber);

        mButtonSingleSegment = (Button) view.findViewById(buttonSingleSegment);
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
                d(TAG, "onTouch: event occured");

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
                d(TAG, "onCheckedChanged: switch was changed to: " + isChecked);
                if (isChecked) {

                    mMissedResponses = 0;
                    d(TAG, "onCheckedChanged: starting display update runnable");
                    mHandler.postDelayed(mDisplayUpdateRunnable, COMMAND_SEND_DELAY_MS);
                    mHandler.postDelayed(mLiveChartRunnable, LIVE_CHART_INIT_DELAY_MS);
                    if (!mControllerOn) {
                        mControllerOn = true;
                        mLiveChartStartTime = System.currentTimeMillis();
                        d(TAG, "onCheckedChanged: controller was off, initializing start time");
                    }
                    mButtonSingleSegment.setEnabled(true);
                    d(TAG, "onCheckedChanged: invalidating options menu");
                    getActivity().invalidateOptionsMenu();
                    if (mIsLoggingData) {
                        d(TAG, "onCheckedChanged: starting logger, mIsLoggingData was true");
                        startLogger();
                    }

                } else {
                    d(TAG, "onCheckedChanged: REMOVING display runnable because switch is off");
                    mHandler.removeCallbacks(mDisplayUpdateRunnable);
                    mHandler.removeCallbacks(mLiveChartRunnable);
                    mButtonSingleSegment.setEnabled(false);
                    mControllerOn = false;
                    clearAllChartData();
                    d(TAG, "onCheckedChanged: invalidating options menu");
                    getActivity().invalidateOptionsMenu();
                    if (mIsLoggingData) {
                        stopLogger();
                    }
                }
            }
        });
        mSwitchOnOff.setChecked(PreferenceSetting.getSwitchState(getContext()));
    }

    private void clearAllChartData() {

        if (!mLineChart.isEmpty()) {

            Log.d(TAG, "clearAllChartData: clearing all chart data");
            mLineChart.clearValues();
            initializeChartData();
        }
    }

    /**
     * sets all the views invisible other than the line chart sets linechart to visible
     *
     * @param view The view of the fragment creates
     */

    private void makeViewsInvisible(View view) {
        d(TAG, "makeViewsInvisible: making chart visible");
        view.findViewById(R.id.textViewLabelCh1).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.textViewCycleLabel).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.textViewWaitLabel).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.textViewSetLabel).setVisibility(View.INVISIBLE);
        mTextViewCycleNumber.setVisibility(View.INVISIBLE);
        mTextViewSetTemp.setVisibility(View.INVISIBLE);
        mTextViewTemp.setVisibility(View.INVISIBLE);
        mTextViewWaitTime.setVisibility(View.INVISIBLE);
        mSwitchOnOff.setEnabled(false);
        mSwitchOnOff.setVisibility(View.INVISIBLE);
        mButtonSingleSegment.setVisibility(View.INVISIBLE);
        if (mIsLandscape) mSupportActionBar.hide();
        mLineChart.setVisibility(VISIBLE);
        mChartVisible = mLineChart.getVisibility() == VISIBLE;
        if (mIsLandscape) ((HomeActivity) getActivity()).hideBottomNavigationView();
    }

    /**
     * sets all the view other than the line chart to visible and sets linechart to invisible
     *
     * @param view The view of the fragment creates
     */
    private void makeViewsVisible(View view) {
        d(TAG, "makeViewsVisible: making chart invisible");
        mLineChart.setVisibility(View.INVISIBLE);
        mChartVisible = mLineChart.getVisibility() == VISIBLE;
        view.findViewById(R.id.textViewLabelCh1).setVisibility(VISIBLE);
        view.findViewById(R.id.textViewCycleLabel).setVisibility(VISIBLE);
        view.findViewById(R.id.textViewWaitLabel).setVisibility(VISIBLE);
        view.findViewById(R.id.textViewSetLabel).setVisibility(VISIBLE);
        mTextViewCycleNumber.setVisibility(VISIBLE);
        mTextViewSetTemp.setVisibility(VISIBLE);
        mTextViewTemp.setVisibility(VISIBLE);
        mTextViewWaitTime.setVisibility(VISIBLE);
        mSwitchOnOff.setEnabled(true);
        mSwitchOnOff.setVisibility(VISIBLE);
        mButtonSingleSegment.setVisibility(VISIBLE);
        mSupportActionBar.show();
        ((HomeActivity) getActivity()).showBottomNavigationView();
    }

    private void initializeRunnables() {

        mHandler = new Handler();
        mSerialSendAgent = TC01SerialSendAgent.getInstance(mHandler);

        mLiveChartRunnable = new Runnable() {
            @Override
            public void run() {

                if (mLineChart.getLineData() != null) {
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                    String date = df.format(new Date(mLiveChartStartTime));
                    mChartDescription.setText(String.format(Locale.getDefault(), "%s Points: %d", date, mLineDataSetTemps.getEntryCount()));
                }

                if (mTemperatureController.hasNonNullCh1AndSet()) {
                    if (!taskBusy){
                        addDataToChart();
                    }
                }

                mHandler.postDelayed(this, LIVE_CHART_DELAY_MS);
            }
        };

        //runnable to send TC01 commands every  second
        mDisplayUpdateRunnable = new Runnable() {
            @Override
            public void run() {

                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {

                    d(TAG, "removing displayUpdate runnable from nHandler and stopping temperature LOGGER as well");
                    Toast.makeText(getContext(), "Connection Lost, turning off switch", Toast.LENGTH_LONG).show();
                    turnOffChamberSwitch();
                    return;
                }
                mMissedResponses = !mResponseReceived ? mMissedResponses + 1 : 0;

                //if no response after 3 consecutive writes, turn off switch

                if (mMissedResponses > 2) {
                    Snackbar.make(view, "Controller Not Responding", Snackbar.LENGTH_INDEFINITE).show();
                    turnOffChamberSwitch();
                    return;
                }
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
                mTemperatureLogWriter.log(mLoggerStartTime);
                mHandler.postDelayed(this, LOGGER_DELAY_MS);
            }
        };
    }

    private void addDataToChart() {

        float xValue = (mTemperatureController.getTimeStampOfReading() - mLiveChartStartTime) / 60000f;
        float tempReading = Float.parseFloat(mTemperatureController.getCh1Reading());
        mLineDataSetTemps.addEntry(new Entry(xValue, tempReading));
        float setPoint = Float.parseFloat(mTemperatureController.getCurrentSetPoint());
        mLineDataSetSetPoints.addEntry(new Entry(xValue, setPoint));
        refreshChart();

    }

    private void refreshChart() {
        //notify LineData and LineChart data has changed
        mLineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
        mLineChart.setData(mLineData);
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
        d(TAG, "turnOffChamberSwitch: called");
        mSwitchOnOff.setChecked(false);
    }

    private void showSingleSegmentDialog() {

        TC01SingleSegDialogFragment fragment = TC01SingleSegDialogFragment.newInstance(getString(R.string.enter_segment), (TC01Controller) mTemperatureController);
        d(TAG, "showSingleSegmentDialog: showing dialog");
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

        d(TAG, "stopLogger: called");
        mIsLoggingData = false;
        d(TAG, "stopLogger: invalidate options menu called");
        getActivity().invalidateOptionsMenu();
        mHandler.removeCallbacks(mLoggerRunnable);
        closeLoggingFile();
    }

    public void closeLoggingFile() {

        if (mTemperatureLogWriter != null) {
            d(TAG, "closeLoggingFile: closing OutputStreamWriter");
            mTemperatureLogWriter.closeFile();
        }
    }

    private void startLogger() {

        d(TAG, "startLogger: called, logging temperature data");
        Toast.makeText(getContext(), "LOGGER STARTED", Toast.LENGTH_SHORT).show();
        mIsLoggingData = true;
        getActivity().invalidateOptionsMenu();
        mHandler.postDelayed(mLoggerRunnable, LOGGER_DELAY_MS);
    }

    private void pauseLogger() {

        d(TAG, "pauseLogger: called, removing loggerRunnable callback");
        Toast.makeText(mContext, "LOGGING PAUSED", Toast.LENGTH_SHORT).show();
        mHandler.removeCallbacks(mLoggerRunnable);
        mTemperatureLogWriter.flush();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        d(TAG, "onPrepareOptionsMenu: called");
        menu.setGroupEnabled(R.id.displayTempFragMenuGroup, true);
        MenuItem startLoggingMenuItem = menu.findItem(R.id.startLogging);
        startLoggingMenuItem.setEnabled(mSwitchOnOff.isChecked());
        MenuItem liveChartmenuItem = menu.findItem(R.id.displayLiveChart);
        MenuItem saveLiveChartMenuItem = menu.findItem(R.id.saveLiveChart);
        if (!mSwitchOnOff.isChecked()) {
            makeViewsVisible(view);
            liveChartmenuItem.setEnabled(false);
        }

        if (mIsLoggingData) {
            startLoggingMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            startLoggingMenuItem.setIcon(R.drawable.ic_action_stop_logger_red);
            startLoggingMenuItem.setEnabled(true);

        } else {
            //not logging
            startLoggingMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            startLoggingMenuItem.setIcon(null);
        }

        if (mLineChart.getVisibility() == VISIBLE) {

            liveChartmenuItem.setTitle(R.string.monitor_view);
            saveLiveChartMenuItem.setVisible(true);

        } else {
            liveChartmenuItem.setTitle(R.string.live_chart_view);
            saveLiveChartMenuItem.setVisible(false);
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

            case R.id.saveLiveChart:

                if (mLineChart.getLineData() != null)
                    ChartUtilityHelperClass.saveLiveChartToFile(mLineChart, mContext, mLiveChartStartTime, null, true);
                else Toast.makeText(mContext, "No chart data to save!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.startLogging:
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                if (item.getIcon() == null) {
                    mTemperatureLogWriter = new TemperatureLogWriter(getContext(), mTemperatureController);
                    mLoggerStartTime = System.currentTimeMillis();
                    startLogger();
                } else {
                    // stop recording icon is showing so show confirmation dialog. If user presses yes
                    // then stopLogger will be called by HomeActivity via MyAlertDialogFragment
                    showAlertDialog(TERMINATE_LOGGING_SESSION);
                }
                return true;

            case R.id.displayLiveChart:

                if (mLineChart.getVisibility() == VISIBLE) {
                    makeViewsVisible(view);
                } else {
                    makeViewsInvisible(view);
                }

                getActivity().invalidateOptionsMenu();
                break;

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

        d(TAG, "onDetach: called, removing displayUpdate callback from mHandler");
        mHandler.removeCallbacks(mDisplayUpdateRunnable);
        mHandler.removeCallbacks(mLiveChartRunnable);
        if (mIsLoggingData) {
            pauseLogger();
        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDispTempBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        d(TAG, "onAttach: called");
        super.onAttach(context);
        mContext = context;

        mDispTempBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String responseToCommandSent = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                mResponseReceived = true;
                if ((mCommandSent.equals(TC01_TEMP_QUERY) || mCommandSent.equals(TC01_SET_QUERY)) && responseToCommandSent.equals(TC01_INFINITY)) {
                    // invalid response from T or C
                    Log.d(TAG, "onReceive: received 1999.9, command: " + mCommandSent);
                    return;
                }
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
                    d(TAG, "onReceive: UTL interrupt, turning off switch");
                    turnOffChamberSwitch();
                    return;
                }

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
            Log.d(TAG, "updateView: response was not numeric, ignoring data");
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
                d(TAG, "Unknown command sent: " + commandSent + " response: " + responseToCommandSent);
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
            d(TAG, "isWaitForever: numberformat exception");
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
        d(TAG, "onStop: called");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called, deleting TEMP FILE and checking if chart is visible");
        ChartUtilityHelperClass.deleteFile(getContext(), TEMP_FILE);
        if (mChartVisible) makeViewsInvisible(view);
        else makeViewsVisible(view);
    }

    @Override
    public void onPause() {
        super.onPause();
        d(TAG, "onPause: called");
    }

    @Override
    public void onResume() {
        super.onResume();
        d(TAG, "onResume: called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        d(TAG, "onDestroy: called, storing state to prefs, saving chart");
        ChartUtilityHelperClass.saveLiveChartToFile(mLineChart, getContext(), mLiveChartStartTime, TEMP_FILE, false);
        storeStateToPrefs();
    }

    private void storeStateToPrefs() {
        //this is required when DispTempFrag is destroyed and new one created. This can happen when
        //another fragment is visible when phone is rotated to landscape. Only visible fragment is retained
        //The state is restored to false in IntroActivity's onDestroy. Don't put in HomeActivity as it's onDestroy
        //is called when screen is rotate

        Log.d(TAG, "storeStateToPrefs: called");
        PreferenceSetting.storeLiveChartVisibility(getContext(), mChartVisible);
        PreferenceSetting.storeLoggingState(getContext(), mIsLoggingData);
        PreferenceSetting.storeSwitchState(getContext(), mSwitchOnOff.isChecked());
        PreferenceSetting.storeStartTime(getContext(), mLiveChartStartTime);
        PreferenceSetting.storeLoggerStartTime(getContext(), mLoggerStartTime);
        if (mIsLoggingData && mTemperatureLogWriter != null) {
            PreferenceSetting.storeFileName(getContext(), mTemperatureLogWriter.getFileName());
        }
        PreferenceSetting.storeControllerOn(getContext(), mControllerOn);
    }

    private void restorStateFromPrefs() {
        Log.d(TAG, "restorStateFromPrefs: called since controller was NULL and state not retained");
        mChartVisible = PreferenceSetting.getLiveChartVisibility(getContext());
        mControllerOn = PreferenceSetting.getControllerOn(getContext());
        mLiveChartStartTime = PreferenceSetting.getStartTime(getContext());
        mIsLoggingData = PreferenceSetting.getLoggingState(getContext());
        mLoggerStartTime = PreferenceSetting.getLoggerStartTime(getContext());
        if (mIsLoggingData) {
            String fileName = PreferenceSetting.getFileName(getContext());
            if (fileName != null) {
                mTemperatureLogWriter = new TemperatureLogWriter(getContext(), mTemperatureController, fileName);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        d(TAG, "onSaveInstanceState: called, storing state variable to outState bundle");
        outState.putBoolean(LOGGING_STATE, mIsLoggingData);
        outState.putSerializable(TEMP_CONTROLLER, mTemperatureController);
        outState.putBoolean(CONTROLLER_ON, mControllerOn);
        outState.putLong(START_TIME, mLiveChartStartTime);
        outState.putLong(LOGGER_START_TIME, mLoggerStartTime);
        outState.putBoolean(CHART_VISIBLE, mChartVisible);
        if (mIsLoggingData) {
            outState.putString(FILE_NAME, mTemperatureLogWriter.getFileName());
        }

        super.onSaveInstanceState(outState);
    }

    /*******************************called from AsynTask pre,post execute methods********************
     /**
     * called from Async task LoadChartTask in postexecute method
     * @param lineData data from to populate chart
     */
    @Override
    public void setLineData(LineData lineData) {

        Log.d(TAG, "setLineData: called, populating chart data and restarting livechart runnable");
        progressBar.setVisibility(View.INVISIBLE);
        mLineData = lineData;
        mLineDataSetTemps = (LineDataSet) mLineData.getDataSetByLabel("TEMP", true);
        mLineDataSetSetPoints = (LineDataSet) mLineData.getDataSetByLabel("SET", true);
        initializeChartLabels();
        refreshChart();
        taskBusy = false;
        mHandler.postDelayed(mLiveChartRunnable, LIVE_CHART_INIT_DELAY_MS);
    }

    /**
     * called from LoadChartTask's pre execute method
     */
    @Override
    public void initialize() {

        Log.d(TAG, "initialize: called, removing liveChart runnable");
        mHandler.removeCallbacks(mLiveChartRunnable);
        taskBusy = true;
        progressBar.setVisibility(VISIBLE);
    }
    //*********************************************************************************************/
}