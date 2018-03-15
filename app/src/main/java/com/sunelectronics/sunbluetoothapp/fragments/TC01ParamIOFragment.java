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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.interfaces.IBusy;
import com.sunelectronics.sunbluetoothapp.models.TC01SerialSendAgent;
import com.sunelectronics.sunbluetoothapp.models.TemperatureController;
import com.sunelectronics.sunbluetoothapp.utilities.PreferenceSetting;

import java.util.Locale;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_385_RTD;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_392_RTD;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_CMD_ERROR;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_J_TC;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_K_TC;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_OPT_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_PID_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_PID_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_READ_INPUT_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_T_TC;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_UTL_QUERY;

public class TC01ParamIOFragment extends Fragment implements View.OnTouchListener, CompoundButton.OnCheckedChangeListener, IBusy {
    private static final String TAG = "TC01ParamFragment";
    public static final int DELAY = 1000;
    private EditText mUtl;
    private ProgressBar mProgressBar;
    private TextView mOptionTextView, mInputTextView;
    private Handler mHandler = new Handler();
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private View view;
    private boolean isBusyDownLoading;
    private String mControllerType, mCommandSent;
    private TemperatureController mTemperatureController;
    private Spinner mProportionalSpinner, mIntegralSpinner, mDerivativeSpinner;
    private RadioButton m392Rtd, m385Rtd, mTthermocouple, mJthermocouple, mKthermocouple;
    private TC01SerialSendAgent mSerialSendAgent;

    public static TC01ParamIOFragment newInstance(TemperatureController controller) {

        Bundle args = new Bundle();
        args.putSerializable(CONTROLLER, controller);
        TC01ParamIOFragment fragment = new TC01ParamIOFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mControllerType = PreferenceSetting.getControllerType(getContext());
        mTemperatureController = (TemperatureController) getArguments().getSerializable(CONTROLLER);
        mSerialSendAgent = TC01SerialSendAgent.getInstance(mHandler);
        mCommandSent = "NONE";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(R.layout.fragment_tc01_parameters, container, false);
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(String.format("%s PARAMETERS", TemperatureController.getName(mControllerType)));
            supportActionBar.show();
        }
        initializeViews(view);
        setHasOptionsMenu(true);
        return view;
    }

    private void initializeViews(final View view) {

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        mOptionTextView = (TextView) view.findViewById(R.id.optionTv);
        mInputTextView = (TextView) view.findViewById(R.id.inputTextViewLabel);
        m392Rtd = (RadioButton) view.findViewById(R.id.radioButton392Rtd);
        m385Rtd = (RadioButton) view.findViewById(R.id.radioButton385Rtd);
        mTthermocouple = (RadioButton) view.findViewById(R.id.radioButtonTthermocouple);
        mJthermocouple = (RadioButton) view.findViewById(R.id.radioButtonJthermocouple);
        mKthermocouple = (RadioButton) view.findViewById(R.id.radioButtonKthermocouple);
        mUtl = (EditText) view.findViewById(R.id.utlEditText);

        mUtl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mUtl.setText("");
                }
            }
        });
        mProportionalSpinner = (Spinner) view.findViewById(R.id.proportionalSpinner);
        mIntegralSpinner = (Spinner) view.findViewById(R.id.integralSpinner);
        mDerivativeSpinner = (Spinner) view.findViewById(R.id.derivativeSpinner);

        Button getButton = (Button) view.findViewById(R.id.buttonGet);
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(v, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                } else if (isBusy()) {
                    Snackbar.make(view, R.string.download_parameters_message, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "GET clicked, getting parameters");

                getControllerParameters();
            }
        });
        Button setButton = (Button) view.findViewById(R.id.buttonSet);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                } else if (isBusy()) {
                    Snackbar.make(view, R.string.download_parameters_message, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "SET clicked, sending commands");
                setParameters();
            }
        });

        ToggleButton toggleButtonOutput1 = (ToggleButton) view.findViewById(R.id.buttonOutput1);
        toggleButtonOutput1.setOnCheckedChangeListener(this);
        toggleButtonOutput1.setOnTouchListener(this);

        ToggleButton toggleButtonOutput2 = (ToggleButton) view.findViewById(R.id.buttonOutput2);
        toggleButtonOutput2.setOnCheckedChangeListener(this);
        toggleButtonOutput2.setOnTouchListener(this);
        Button readInputButton = (Button) view.findViewById(R.id.buttonInput);
        readInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBusy()) {
                    mCommandSent = TC01_READ_INPUT_COMMAND;
                    mSerialSendAgent.sendCommand(mCommandSent);
                } else
                    Snackbar.make(view, R.string.download_parameters_message, Snackbar.LENGTH_SHORT).show();
            }
        });
        loadPIDDefaults();
    }

    private void getControllerParameters() {

        isBusyDownLoading = true; //HomeActivity checks this on onBackPressed to prevent leaving fragment until complete
        mProgressBar.setVisibility(View.VISIBLE);
        sendUtlQuery();
        //when UTL response is recieved by broadcast receiver, next query is done
        //in case no response, handler below will turn off progress bar and set download flag back to false
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: called");
                if (isBusyDownLoading) {
                    //after 4 seconds, should be done downloading parameters
                    //this just covers case where power is removed while downloading
                    //and prevents hanging up with progress bar showing indefinitely
                    Log.d(TAG, "taking too long, turning off progress bar and resetting busy download flag");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    isBusyDownLoading = false;
                }
            }
        }, 4000);
    }

    private void sendOptQuery() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending OPT query");
                mCommandSent = TC01_OPT_QUERY;
                mSerialSendAgent.sendCommand(mCommandSent);
            }
        });
    }

    private void sendUtlQuery() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending UTL query");
                mCommandSent = mTemperatureController.getUtlQueryCommand();
                mSerialSendAgent.sendCommand(mCommandSent);
            }
        });
    }

    private void sendPidQuery() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending PID query");
                mCommandSent = mTemperatureController.getPidCQueryCommand();
                mSerialSendAgent.sendCommand(mCommandSent);
            }
        });
    }

    @Override
    public boolean isBusy() {
        return isBusyDownLoading;
    }
    /*------------------------set parameters----------------------------------------------------------------*/

    private void setParameters() {

        String utl = mUtl.getText().toString();

        if (utl.isEmpty()) {
            isBusyDownLoading = true;
            mProgressBar.setVisibility(View.VISIBLE);
            sendPidCommand();
            sendInitCommand();
            finishUp();
        } else {

            if (isValidNumber(utl) && isValidUtl(utl)) {
                isBusyDownLoading = true;
                mProgressBar.setVisibility(View.VISIBLE);
                sendUtlCommand();
                sendPidCommand();
                sendInitCommand();
                finishUp();
            }
        }
    }

    private boolean isValidUtl(String number) {
        try {
            float utlAsFloat = Float.parseFloat(number);
            Log.d(TAG, "isUtlValid: called utlAsfloat is: " + utlAsFloat);
            if (utlAsFloat > 315) {
                Snackbar.make(view, "UTL must be <= 315C", Snackbar.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidNumber(String number) {

        if (number.isEmpty()) return false;
        try {
            float utlAsFloat = Float.parseFloat(number);
            Log.d(TAG, "isUtlValid: called utlAsfloat is: " + utlAsFloat);
            return true;
        } catch (NumberFormatException e) {
            Snackbar.make(view, "Invalid UTL", Snackbar.LENGTH_LONG).show();
            return false;
        }

    }

    private void sendUtlCommand() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCommandSent = String.format("%s%s", mUtl.getText().toString(), mTemperatureController.getUtlQueryCommand());
                Log.d(TAG, "run: sending UTL command " + mCommandSent);
                mSerialSendAgent.sendCommand(mCommandSent);
            }
        });
    }

    private void sendPidCommand() {
        final String pidCommand = getPidCommand();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending PID command " + pidCommand);
                mCommandSent = pidCommand;
                mSerialSendAgent.sendCommand(pidCommand);
            }
        });
    }

    private void sendInitCommand() {
        /*The INIT command is only valid on versions F and higher. Format is:
        INITn,p,i,d,M,C
        where n is probe type as follows:
        1=385RTD, 2=392RTD, 3= J t/c, 4=K t/c, 5= T t/c
        note: H  (hours) is valid to sub for M but is not implemented yet
         */

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "sendInitCommand: called");
                StringBuilder sb;
                sb = new StringBuilder("INIT");
                sb.append(String.valueOf(getProbeType())).append(",").append(mProportionalSpinner.getSelectedItem().toString())
                        .append(",").append(mIntegralSpinner.getSelectedItem().toString()).append(",")
                        .append(mDerivativeSpinner.getSelectedItem().toString()).append(",")
                        .append("M").append(",").append("C");
                mCommandSent = sb.toString();
                mSerialSendAgent.sendCommand(mCommandSent);
            }
        });
    }

    private char getProbeType() {
        char n = 0;
        if (m385Rtd.isChecked()) n = '1';
        if (m392Rtd.isChecked()) n = '2';
        if (mJthermocouple.isChecked()) n = '3';
        if (mKthermocouple.isChecked()) n = '4';
        if (mTthermocouple.isChecked()) n = '5';
        return n;
    }

    private void finishUp() {

        //just hide progress bar and set busy flag to false
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "finish up called, turning off progress bar");
                mProgressBar.setVisibility(View.INVISIBLE);
                isBusyDownLoading = false;
            }
        }, DELAY * 4);
    }


    private String getPidCommand() {
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append(TC01_PID_COMMAND).append(mProportionalSpinner.getSelectedItem().toString()).append(",")
                .append(mIntegralSpinner.getSelectedItem().toString()).append(",")
                .append(mDerivativeSpinner.getSelectedItem().toString());
        return sb.toString();
    }


    /*------------------------set parameters end----------------------------------------------------------------*/

    private void loadControllerParameterDefaults() {
        mUtl.setText(R.string.utl_default);
        loadPIDDefaults();
        m392Rtd.setChecked(true);
    }

    private void loadPIDDefaults() {
        mProportionalSpinner.setSelection(8);
        mIntegralSpinner.setSelection(7);
        mDerivativeSpinner.setSelection(8);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //if bluetooth not connected, then display snackbar and consume event (don't execute onCheckedChanged Listener)
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                Snackbar.make(v, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                return true; // this consumes click event
            }

            if (isBusy()) {
                Snackbar.make(view, R.string.download_parameters_message, Snackbar.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {

            mCommandSent = buttonView.getTag().toString() + "ON";
            mSerialSendAgent.sendCommand(mCommandSent);
            Log.d(TAG, "onCheckedChanged: sending command to bluetooth " + mCommandSent);

        } else {
            mCommandSent = buttonView.getTag().toString() + "OFF";
            mSerialSendAgent.sendCommand(mCommandSent);
            Log.d(TAG, "onCheckedChanged: sending command to bluetooth " + mCommandSent);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mBroadcastReceiver = new BroadcastReceiver() {

            int pidCount = 0;

            @Override
            public void onReceive(Context context, Intent intent) {

                String response = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                Log.d(TAG, "onReceive: called, command sent: " + mCommandSent + " response received: " + response);

                if (mCommandSent.contains("INIT") && response.equals(TC01_CMD_ERROR)) {
                    Snackbar.make(view, "COMMAND ERROR! (INIT command not supported)", Snackbar.LENGTH_LONG).show();
                    mCommandSent = "NONE";
                    return;
                }
                switch (mCommandSent) {

                    case TC01_READ_INPUT_COMMAND:
                        int intResponse = (int) Float.parseFloat(response);
                        mInputTextView.setText(String.format(Locale.ENGLISH, "%s %d", getString(R.string.input), intResponse));
                        break;
                    case TC01_OPT_QUERY:
                        mOptionTextView.setText(response);
                        setProbeType(response);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        isBusyDownLoading = false;
                        break;

                    case TC01_UTL_QUERY:
                        if (response.contains("O")) {
                            Snackbar.make(view, "UTL interrupt character received. Check for correct probe type", Snackbar.LENGTH_LONG).show();
                            sendOptQuery();
                            finishUp();
                        } else {
                            if (isValidNumber(response)) {
                                mUtl.setText(response);
                                sendPidQuery();
                            } else {
                                finishUp();
                            }
                        }
                        break;

                    case TC01_PID_QUERY:
                        pidCount++;
                        try {

                            if (pidCount == 1) {
                                mProportionalSpinner.setSelection(9 + (int) Float.parseFloat(response));
                            } else if (pidCount == 2) {
                                mIntegralSpinner.setSelection(9 + (int) Float.parseFloat(response));

                            } else {
                                mDerivativeSpinner.setSelection(9 + (int) Float.parseFloat(response));
                                pidCount = 0;
                                sendOptQuery();
                            }

                        } catch (NumberFormatException e) {
                            Snackbar.make(view, "invalid PID response", Snackbar.LENGTH_SHORT).show();
                        }

                        break;
                }
            }

            private void setProbeType(String response) {

                if (response != null && response.length() > 5) {
                    if (response.contains(TC01_392_RTD)) m392Rtd.setChecked(true);
                    else if (response.contains(TC01_385_RTD)) m385Rtd.setChecked(true);
                    else if (response.charAt(5) == TC01_T_TC) mTthermocouple.setChecked(true);
                    else if (response.charAt(5) == TC01_J_TC) mJthermocouple.setChecked(true);
                    else if (response.charAt(5) == TC01_K_TC) mKthermocouple.setChecked(true);
                }

            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothConnectionService.MY_INTENT_FILTER));
    }

    @Override
    public void onDetach() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_parameter_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (isBusyDownLoading) {
            Snackbar.make(view, R.string.download_parameters_message, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {

            case R.id.loadDefaults:
                loadControllerParameterDefaults();
                return true;
        }
        return false;
    }


}