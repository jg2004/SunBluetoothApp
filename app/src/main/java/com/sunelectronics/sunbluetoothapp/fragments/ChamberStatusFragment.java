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
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.models.ChamberStatus;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.BKPNT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.BKPNTC;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDA_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.STATUS;

/**
 * Created by Jerry on 11/11/2017.
 */

public class ChamberStatusFragment extends Fragment {

    private TextView mLprunningTv, mWaitingAtBreakPointTv, mCycleNumberTv, mTimeOutLedTv;
    private TextView mChamberWaitingToTimeOutTv, mChamberNotRampingTv, mChamberValidSetPointTv;
    private TextView mChamberPoweredOnTv, mPidAModeTv;
    private ImageButton mBreakPointContinueImageButton;
    private Spinner mSpinner;
    private LinearLayout mCycleLayout;
    private Handler mHandler;
    private Context mContext;
    private Runnable mStatusRunnable, mGetCycleRunnable, mGetBreakPointRunnable, mPidARunnable;
    private BroadcastReceiver mStatusBroadcastReceiver;
    private ChamberStatus mChamberStatus;
    private String mCycleVariable;
    private StringBuilder stringBuilder;
    private static final int GET_STATUS_MESSAGE_DELAY = 3000;
    private boolean mSentBKPNTCommand;
    private static final String TAG = "ChamberStatusFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");

        View view = inflater.inflate(R.layout.fragment_chamber_status, container, false);
        initializeViews(view);
        return view;
    }

    private void initializeViews(View view) {
        mCycleLayout = (LinearLayout) view.findViewById(R.id.lpCycleLayout);
        mBreakPointContinueImageButton = (ImageButton) view.findViewById(R.id.imageButtonBreakPointContinue);
        mBreakPointContinueImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: breakPoint image button was clicked");
                BluetoothConnectionService.getInstance(mContext).write(BKPNTC);
            }
        });
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCycleVariable = mSpinner.getSelectedItem().toString() + "?";
                Log.d(TAG, "onItemSelected: setting mCycleVariable to " + mCycleVariable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mLprunningTv = (TextView) view.findViewById(R.id.textViewLpRunningStatus);
        mWaitingAtBreakPointTv = (TextView) view.findViewById(R.id.textViewWaitingAtBreakPointStatus);
        mTimeOutLedTv = (TextView) view.findViewById(R.id.textViewTimeOutLEDStatus);
        mChamberWaitingToTimeOutTv = (TextView) view.findViewById(R.id.textViewWaitingForTimeOutStatus);
        mChamberNotRampingTv = (TextView) view.findViewById(R.id.textViewCurrentlyRampingStatus);
        mChamberValidSetPointTv = (TextView) view.findViewById(R.id.textViewValidSetPointtatus);
        mChamberPoweredOnTv = (TextView) view.findViewById(R.id.textViewChamberPoweredOnStatus);
        mCycleNumberTv = (TextView) view.findViewById(R.id.textViewCycleNumber);
        mCycleVariable = mSpinner.getSelectedItem().toString() + "?";
        mPidAModeTv = (TextView) view.findViewById(R.id.textViewPidaMode);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        stringBuilder = new StringBuilder();
        Log.d(TAG, "onAttach: called, creating a runnable and chamberStatus object");
        mChamberStatus = new ChamberStatus();
        mStatusRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "inside mStatusrunnable, writing STATUS? to bluetooth");
                BluetoothConnectionService.getInstance(mContext).write(STATUS);
                mHandler.postDelayed(this, GET_STATUS_MESSAGE_DELAY);
                mHandler.postDelayed(mPidARunnable, 1000);
            }
        };
        mGetCycleRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "inside mGetCycleRunnable, writing " + mCycleVariable + " to bluetooth");
                BluetoothConnectionService.getInstance(mContext).write(mCycleVariable);
            }
        };
        mGetBreakPointRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "inside breakPointRunnable, writing  BKPNT? to bluetooth");
                BluetoothConnectionService.getInstance(mContext).write(BKPNT);
            }
        };
        mPidARunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "inside writing PIDA? to bluetooth");
                BluetoothConnectionService.getInstance(mContext).write(PIDA_QUERY);
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called, creating and starting handler");
        mStatusBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: called of statusBroadcastReceiver");
                String action = intent.getAction();
                if (!action.equals(BluetoothConnectionService.MY_INTENT_FILTER)) {
                    return;
                }
                //if action is MY_INTENT_FILTER then, proceed:
                String response = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                String commandSent = intent.getStringExtra(BluetoothConnectionService.COMMAND_SENT);

                if (commandSent.equals(STATUS)) {
                    Log.d(TAG, "STATUS command sent. Setting status inside chamberStatus object to: " + response);
                    mChamberStatus.setStatusMessages(response);

                    if (mChamberStatus.isLPRunning()) {
                        mCycleLayout.setVisibility(View.VISIBLE);
                        mHandler.post(mGetCycleRunnable);
                    } else {
                        mCycleLayout.setVisibility(View.INVISIBLE);
                        mHandler.removeCallbacks(mGetCycleRunnable);
                    }
                    if (mChamberStatus.isWaitingAtBreakPoint() && !mSentBKPNTCommand) {
                        //the mSentBKPNTCommand is a flag used to send BKPNT? command once as there
                        //is a bug that clears this value to 0 after first read. So when subsequent
                        // reads are performed, the value 0 is read. So send just once!!!
                        mHandler.postDelayed(mGetBreakPointRunnable, 1000);
                        mSentBKPNTCommand = true;
                    } else if (mChamberStatus.isWaitingAtBreakPoint() && mSentBKPNTCommand) {
                        //do nothing at all if BKPNT? was sent once

                    } else {
                        //otherwise if not waiting at breakpoint, set BKPNTC button to invisible
                        // and reset the text view and the mSentBKPNTflag
                        mBreakPointContinueImageButton.setVisibility(View.INVISIBLE);
                        mWaitingAtBreakPointTv.setText(mChamberStatus.getWaitingAtBreakPointStatusMessage());
                        mSentBKPNTCommand = false;
                    }
                    //get status messages from status object mChamberStatus and set to appropriate textView
                    mLprunningTv.setText(mChamberStatus.getLpRunningStatusMessage());
                    mTimeOutLedTv.setText(mChamberStatus.getTimeoutStatusMessage());
                    mChamberWaitingToTimeOutTv.setText(mChamberStatus.getWaitingForTimeOutStatusMessage());
                    mChamberNotRampingTv.setText(mChamberStatus.getCurrentlyRampingStatusMessage());
                    mChamberValidSetPointTv.setText(mChamberStatus.getValidSetStatusMessage());
                    mChamberPoweredOnTv.setText(mChamberStatus.getChamberIsOnMessage());

                } else if (commandSent.contains("I"))

                {
                    mCycleNumberTv.setText(response);
                } else if (commandSent.equals(BKPNT))

                {
                    Log.d(TAG, "onReceive: command sent was BKPNT?, response was " + response);
                    mWaitingAtBreakPointTv.setText(mChamberStatus.getWaitingAtBreakPointStatusMessage() + " " + response);
                    mBreakPointContinueImageButton.setVisibility(View.VISIBLE);
                }
                 else if (commandSent.equals(PIDA_QUERY)){

                    // TODO: 11/30/2017 display PIDA mode
                    mChamberStatus.setPidMode(response);
                    mPidAModeTv.setText(mChamberStatus.getPidMode());
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mStatusBroadcastReceiver,
                new IntentFilter(BluetoothConnectionService.MY_INTENT_FILTER));
        mHandler = new Handler();
        mHandler.post(mStatusRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called");
        mHandler.removeCallbacks(mStatusRunnable);
        mHandler.removeCallbacks(mGetCycleRunnable);
        mHandler.removeCallbacks(mGetBreakPointRunnable);
        mHandler.removeCallbacks(mPidARunnable);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mStatusBroadcastReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: called");
    }

}
