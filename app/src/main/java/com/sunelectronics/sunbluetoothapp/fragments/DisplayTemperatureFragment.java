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
import android.widget.ToggleButton;

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
import com.sunelectronics.sunbluetoothapp.models.ControllerStatus;
import com.sunelectronics.sunbluetoothapp.models.DualChannelTemperatureController;
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

import static android.view.View.VISIBLE;
import static com.sunelectronics.sunbluetoothapp.R.id.buttonSingleSegment;
import static com.sunelectronics.sunbluetoothapp.R.id.buttonStatus;
import static com.sunelectronics.sunbluetoothapp.R.id.lineChart;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH1_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CH2_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHART_VISIBLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER_ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC127;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EC1X_CH2_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGER_START_TIME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGING_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC100;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_RATE_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_SET_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PC_WAIT_QUERY_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.RATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SET_TEMP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.START_TIME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.STATUS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_CHAMBER_STATUS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_OUTPUT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PARAMETER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PIDA_MODE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC02;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TEMP_CONTROLLER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TEMP_FILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TERMINATE_LOGGING_SESSION;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TURN_OFF_CHAMBER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.WAIT_TIME;

public class DisplayTemperatureFragment extends Fragment implements IChamberOffSwitch, ILogger, ChartDataCallback {
    private static final String TAG = "DisplayTemperatureFragm";
    private static final int COMMAND_SEND_DELAY_MS = 1000;
    private static final int COMMAND_SEND_DELAY_LONG_MS = 1000;
    private static final int LOGGER_DELAY_MS = 15000;
    private static final int LIVE_CHART_DELAY_MS = 20000;
    private static final int LIVE_CHART_INIT_DELAY_MS = 3000;
    private Handler mHandler;
    private Runnable mDisplayUpdateRunnable, mLoggerRunnable, mLiveChartRunnable;
    private TextView mTextViewCH1Temp, mTextViewCH2Temp, mTextViewWaitTime, mTextViewSetTemp, mTextViewRate;
    private ToggleButton mHeatEnableToggleButton, mCoolEnableToggleButton;
    private Switch mSwitchOnOff;
    private Context mContext;
    private BroadcastReceiver mDispTempBroadcastReceiver;
    private TemperatureController mTemperatureController;
    private ControllerStatus mControllerStatus;
    private TemperatureLogWriter mTemperatureLogWriter;
    private boolean mIsLoggingData, mIsLandscape, mControllerOn, mChartVisible, mControllerResponding;
    private View view;
    private String mControllerType;
    private long mLoggerStartTime, mLiveChartStartTime;
    private LineChart mLineChart;
    private LineData mLineData;
    private Description mChartDescription;
    private LineDataSet mLineDataSetTemps, mLineDataSetCh2Temps, mLineDataSetSetPoints;
    private ProgressBar progressBar;
    private ActionBar mSupportActionBar;
    private Button mButtonSingleSegment, mButtonStatus;
    private boolean taskBusy;

    public interface DisplayTemperatureFragmentCallBacks {

        //implemented by HomeActivity
        void closeActivity();

        void turnOffChamber();

        void stopLoggingSession();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        // if rotated then get state from savedInstanceState
        // if started for first time variables are set to intitial values

        Log.d(TAG, "onCreate: called, creating DisplayTempFrag");
        super.onCreate(savedInstanceState);
        mControllerType = PreferenceSetting.getControllerType(getContext());
        mIsLandscape = (getResources().getConfiguration().orientation) == Configuration.ORIENTATION_LANDSCAPE;

        if (savedInstanceState != null) {
            restoreStateFromSavedInstanceState(savedInstanceState);
        } else {
            if (mTemperatureController == null) {
                mTemperatureController = TemperatureController.createController(mControllerType);
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
            Log.d(TAG, "onCreateView: temp file exists, loading file from asyncTask");
            LoadChartTask task = new LoadChartTask(this);
            task.execute(reader.getFileContents(TEMP_FILE));

        } else {
            initializeChart();
        }
        setHasOptionsMenu(true);
        checkStatus();
        return view;
    }

    private void initializeChart() {
        initializeChartLabels();
        if (mLineData == null) {
            Log.d(TAG, "Line data was NULL, initialize charts");
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
        Log.d(TAG, "initializeChartData: called");
        List<Entry> tempEntries = new ArrayList<>();
        List<Entry> setPointEntries = new ArrayList<>();
        mLineData = new LineData();
        mLineDataSetTemps = new LineDataSet(tempEntries, mTemperatureController.getCh1Label());
        mLineDataSetTemps.setDrawCircles(false);
        mLineDataSetTemps.setDrawValues(false);
        mLineDataSetTemps.setColor(Color.BLUE);
        mLineData.addDataSet(mLineDataSetTemps);
        if (mTemperatureController instanceof DualChannelTemperatureController) {
            List<Entry> ch2Entries = new ArrayList<>();
            mLineDataSetCh2Temps = new LineDataSet(ch2Entries, ((DualChannelTemperatureController) mTemperatureController).getCh2Label());
            mLineDataSetCh2Temps.setDrawCircles(false);
            mLineDataSetCh2Temps.setDrawValues(false);
            mLineDataSetCh2Temps.setColor(Color.RED);
            mLineData.addDataSet(mLineDataSetCh2Temps);
        }
        mLineDataSetSetPoints = new LineDataSet(setPointEntries, "SET");
        mLineDataSetSetPoints.setDrawCircles(false);
        mLineDataSetSetPoints.setDrawValues(false);
        mLineDataSetSetPoints.setColor(Color.BLACK);
        mLineData.addDataSet(mLineDataSetSetPoints);

    }

    private void checkStatus() {
        Log.d(TAG, "checkStatus: checking the status");
        BluetoothConnectionService.getInstance().write(STATUS);
    }

    /**
     * sets all the views invisible other than the line chart sets linechart to visible
     *
     * @param view The view of the fragment creates
     */

    private void makeViewsInvisible(View view) {
        Log.d(TAG, "makeViewsInvisible: making chart visible");
        view.findViewById(R.id.textViewLabelCh1).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.textViewRateLabel).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.textViewWaitLabel).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.textViewSetLabel).setVisibility(View.INVISIBLE);
        mTextViewRate.setVisibility(View.INVISIBLE);
        mTextViewSetTemp.setVisibility(View.INVISIBLE);
        mTextViewCH1Temp.setVisibility(View.INVISIBLE);
        mTextViewWaitTime.setVisibility(View.INVISIBLE);
        mSwitchOnOff.setEnabled(false);
        mSwitchOnOff.setVisibility(View.INVISIBLE);
        mCoolEnableToggleButton.setEnabled(false);
        mCoolEnableToggleButton.setVisibility(View.INVISIBLE);
        mHeatEnableToggleButton.setEnabled(false);
        mHeatEnableToggleButton.setVisibility(View.INVISIBLE);
        mButtonSingleSegment.setVisibility(View.INVISIBLE);
        mButtonStatus.setVisibility(View.INVISIBLE);
        if (mIsLandscape) mSupportActionBar.hide();
        mLineChart.setVisibility(VISIBLE);
        mChartVisible = mLineChart.getVisibility() == VISIBLE;
        if (mIsLandscape) ((HomeActivity) getActivity()).hideBottomNavigationView();
        if (mTemperatureController instanceof DualChannelTemperatureController) {
            view.findViewById(R.id.textViewCh2Label).setVisibility(View.INVISIBLE);
            mTextViewCH2Temp.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * sets all the view other than the line chart to visible and sets linechart to invisible
     *
     * @param view The view of the fragment creates
     */
    private void makeViewsVisible(View view) {
        Log.d(TAG, "makeViewsVisible: making chart invisible");
        mLineChart.setVisibility(View.INVISIBLE);
        mChartVisible = mLineChart.getVisibility() == VISIBLE;
        view.findViewById(R.id.textViewLabelCh1).setVisibility(VISIBLE);
        view.findViewById(R.id.textViewRateLabel).setVisibility(VISIBLE);
        view.findViewById(R.id.textViewWaitLabel).setVisibility(VISIBLE);
        view.findViewById(R.id.textViewSetLabel).setVisibility(VISIBLE);
        mTextViewRate.setVisibility(VISIBLE);
        mTextViewSetTemp.setVisibility(VISIBLE);
        mTextViewCH1Temp.setVisibility(VISIBLE);
        mTextViewWaitTime.setVisibility(VISIBLE);
        mCoolEnableToggleButton.setEnabled(true);
        mCoolEnableToggleButton.setVisibility(VISIBLE);
        mHeatEnableToggleButton.setEnabled(true);
        mHeatEnableToggleButton.setVisibility(VISIBLE);
        mSwitchOnOff.setEnabled(true);
        mSwitchOnOff.setVisibility(VISIBLE);
        mButtonSingleSegment.setVisibility(VISIBLE);
        mButtonStatus.setVisibility(VISIBLE);
        mSupportActionBar.show();
        ((HomeActivity) getActivity()).showBottomNavigationView();
        if (mTemperatureController instanceof DualChannelTemperatureController) {
            view.findViewById(R.id.textViewCh2Label).setVisibility(View.VISIBLE);
            mTextViewCH2Temp.setVisibility(View.VISIBLE);
        }
    }

    private void initializeRunnables() {

        mHandler = new Handler();

        mLiveChartRunnable = new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "run: inside chart runnable");
                if (mLineChart.getLineData() != null) {
                    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
                    String date = df.format(new Date(mLiveChartStartTime));
                    mChartDescription.setText(String.format(Locale.getDefault(), "%s Points: %d", date, mLineDataSetTemps.getEntryCount()));
                }

                if (mTemperatureController.hasNonNullCh1AndSet()) {

                    if (!taskBusy) {
                        addDataToChart();
                    }
                }

                mHandler.postDelayed(this, LIVE_CHART_DELAY_MS);
            }
        };
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
                // this checks if no response to the initial STATUS? command is sent, as in the case where controller has no
                // power or faulty RS232 setting such as baud rate not 9600 or Handshaking turne on.
                // It then turns off switch off and displays message. It's initally false, then
                // set to true in broadcast receive method. Check is done JUST ONCE.
                if (!mControllerResponding) {
                    mSwitchOnOff.setChecked(false);
                    Snackbar.make(view, R.string.no_resp_message, Snackbar.LENGTH_INDEFINITE).show();
                    return;
                }

                String command = mTemperatureController.getNextPollingCommand();
                BluetoothConnectionService.getInstance().write(command);
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
        float setPoint;
        try {
            //catch exception caused by set = "NONE"
            setPoint = Float.parseFloat(mTemperatureController.getCurrentSetPoint());
        } catch (NumberFormatException e) {
            setPoint = 0;
        }
        mLineDataSetSetPoints.addEntry(new Entry(xValue, setPoint));
        if (mTemperatureController instanceof DualChannelTemperatureController) {
            float ch2Reading = Float.parseFloat(((DualChannelTemperatureController) mTemperatureController).getCh2Reading());
            mLineDataSetCh2Temps.addEntry(new Entry(xValue, ch2Reading));
        }
        refreshChart();

    }

    private void refreshChart() {
        //notify LineData and LineChart data has changed
        mLineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        mLineChart.invalidate();
        mLineChart.setData(mLineData);
    }

    private void initializeViews(final View view) {

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mLineChart = (LineChart) view.findViewById(lineChart);
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


        mButtonStatus = (Button) view.findViewById(buttonStatus);
        mButtonStatus.setEnabled(false);
        mButtonStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatusFragment();
            }
        });
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
                    mHandler.postDelayed(mLiveChartRunnable, LIVE_CHART_INIT_DELAY_MS);
                    if (!mControllerOn) {
                        mControllerOn = true;
                        mLiveChartStartTime = System.currentTimeMillis();
                        Log.d(TAG, "onCheckedChanged: controller was off, initializing start time");
                    }
                    mButtonSingleSegment.setEnabled(true);
                    mButtonStatus.setEnabled(true);
                    Log.d(TAG, "onCheckedChanged: invalidating options menu");
                    getActivity().invalidateOptionsMenu();
                    if (mIsLoggingData) {
                        Log.d(TAG, "onCheckedChanged: starting logger, mIsLoggingData was true");
                        startLogger();
                    }

                } else {
                    Log.d(TAG, "onCheckedChanged: REMOVING display runnable because switch is off");
                    mHandler.removeCallbacks(mDisplayUpdateRunnable);
                    mHandler.removeCallbacks(mLiveChartRunnable);
                    mButtonSingleSegment.setEnabled(false);
                    mButtonStatus.setEnabled(false);
                    mControllerOn = false;
                    clearAllChartData();
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

    private void clearAllChartData() {

        if (!mLineChart.isEmpty()) {

            Log.d(TAG, "clearAllChartData: clearing all chart data");
            mLineChart.clearValues();
            initializeChartData();
        }
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
        SingleSegDialogFragment fragment = SingleSegDialogFragment.newInstance(getString(R.string.enter_segment), mTemperatureController);
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
        MenuItem liveChartmenuItem = menu.findItem(R.id.displayLiveChart);
        MenuItem saveLiveChartMenuItem = menu.findItem(R.id.saveLiveChart);
        MenuItem pidAMenuItem = menu.findItem(R.id.pidAMode);
        MenuItem outputsMenuItem = menu.findItem(R.id.outputs);
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


            case R.id.saveLiveChart:

                if (mLineChart.getLineData() != null)
                    ChartUtilityHelperClass.saveLiveChartToFile(mLineChart, mContext, mLiveChartStartTime, null, true);
                else Toast.makeText(mContext, "No chart data to save!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.displayLiveChart:

                if (mLineChart.getVisibility() == VISIBLE) {
                    makeViewsVisible(view);
                } else {
                    makeViewsInvisible(view);
                }
                getActivity().invalidateOptionsMenu();
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
        mHandler.removeCallbacks(mLiveChartRunnable);
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
                mControllerResponding = true;
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
            case PC_SET_QUERY_COMMAND:
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
            case PC_WAIT_QUERY_COMMAND:

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
            case PC_RATE_QUERY_COMMAND:

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

                if (responseToCommandSent.contains(STATUS)) {
                    //then RS232 echo is on. display message to disable echo.
                    Snackbar snackbar = Snackbar.make(view, mTemperatureController.getRs232EchoMessage(), Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                    mSwitchOnOff.setChecked(false);
                }

                // TODO: 1/2/2018 temporary code!!

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
        Log.d(TAG, "onStart: called, deleting TEMP FILE and checking if chart is visible");
        ChartUtilityHelperClass.deleteFile(getContext(), TEMP_FILE);
        if (mChartVisible) makeViewsInvisible(view);
        else makeViewsVisible(view);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: called, storing boolean value mIsoggingData: " + mIsLoggingData);
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
        mLineDataSetTemps = (LineDataSet) mLineData.getDataSetByLabel(mTemperatureController.getCh1Label(), true);
        mLineDataSetSetPoints = (LineDataSet) mLineData.getDataSetByLabel("SET", true);
        if (mTemperatureController instanceof DualChannelTemperatureController) {
            mLineDataSetCh2Temps = (LineDataSet) mLineData.getDataSetByLabel(((DualChannelTemperatureController) mTemperatureController).getCh2Label(), true);
        }
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