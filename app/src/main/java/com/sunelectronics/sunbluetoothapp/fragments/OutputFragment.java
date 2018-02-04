package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;

import java.util.Locale;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ANALOG_0;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ANALOG_1;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ANALOG_2;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ANALOG_3;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OUT0_COMMAND_PREFIX;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OUT3_COMMAND_PREFIX;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OUT_COMMAND_OFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OUT_COMMAND_ON;

public class OutputFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener,
        View.OnTouchListener {

    private static final String TAG = "OutputFragment";
    private TextView mAnalogChannel0TextView, mAnalogChannel1TextView, mAnalogChannel2TextView, mAnalogChannel3TextView;
    private BroadcastReceiver mBroadCastReceiver;
    private Context mContext;
    private StringBuilder mStringBuilderMessage;
    private View view;
    private String mOutput2Text, mOutput4Text, mControllerType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        mStringBuilderMessage = new StringBuilder();
        SharedPreferences prefs = getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
        mControllerType = prefs.getString(CONTROLLER_TYPE, "EC1X");
        initializeButtonText(mControllerType);
    }

    private void initializeButtonText(String controllerType) {

        if (controllerType.equals("EC1X")) {
            mOutput2Text = "OUTPUT 2 (AB) ON";
            mOutput4Text = "OUTPUT 4 (N2 GP) ON";
        } else {

            mOutput2Text = "OUTPUT 2 ON";
            mOutput4Text = "OUTPUT 4 ON";
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(R.layout.fragment_outputs, container, false);
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(String.format("%s OUTPUTS", mControllerType));
            supportActionBar.show();
        }
        intializeViews(view);
        return view;
    }

    private void intializeViews(View view) {

        mAnalogChannel0TextView = (TextView) view.findViewById(R.id.channel_0_textView);
        mAnalogChannel1TextView = (TextView) view.findViewById(R.id.channel_1_textView);
        mAnalogChannel2TextView = (TextView) view.findViewById(R.id.channel_2_textView);
        mAnalogChannel3TextView = (TextView) view.findViewById(R.id.channel_3_textView);

        ToggleButton toggleButtonOutput2 = (ToggleButton) view.findViewById(R.id.buttonOutput2);
        toggleButtonOutput2.setOnCheckedChangeListener(this);
        toggleButtonOutput2.setOnTouchListener(this);
        toggleButtonOutput2.setText(mOutput2Text);

        ToggleButton toggleButtonOutput3 = (ToggleButton) view.findViewById(R.id.buttonOutput3);
        toggleButtonOutput3.setOnCheckedChangeListener(this);
        toggleButtonOutput3.setOnTouchListener(this);

        ToggleButton toggleButtonOutput4 = (ToggleButton) view.findViewById(R.id.buttonOutput4);
        toggleButtonOutput4.setOnCheckedChangeListener(this);
        toggleButtonOutput4.setOnTouchListener(this);
        toggleButtonOutput4.setText(mOutput4Text);


        ToggleButton toggleButtonOutput5 = (ToggleButton) view.findViewById(R.id.buttonOutput5);
        toggleButtonOutput5.setOnCheckedChangeListener(this);
        toggleButtonOutput5.setOnTouchListener(this);

        ToggleButton toggleButtonOutput6 = (ToggleButton) view.findViewById(R.id.buttonOutput6);
        toggleButtonOutput6.setOnCheckedChangeListener(this);
        toggleButtonOutput6.setOnTouchListener(this);

        SeekBar seekBar0 = (SeekBar) view.findViewById(R.id.seekbarChannel0);
        seekBar0.setOnSeekBarChangeListener(this);
        mAnalogChannel0TextView.setText(String.valueOf(seekBar0.getProgress()));

        SeekBar seekBar1 = (SeekBar) view.findViewById(R.id.seekbarChannel1);
        seekBar1.setOnSeekBarChangeListener(this);
        mAnalogChannel1TextView.setText(String.valueOf(seekBar1.getProgress()));

        SeekBar seekBar2 = (SeekBar) view.findViewById(R.id.seekbarChannel2);
        seekBar2.setOnSeekBarChangeListener(this);
        mAnalogChannel2TextView.setText(String.valueOf(seekBar2.getProgress()));

        SeekBar seekBar3 = (SeekBar) view.findViewById(R.id.seekbarChannel3);
        seekBar3.setOnSeekBarChangeListener(this);
        mAnalogChannel3TextView.setText(String.valueOf(seekBar3.getProgress()));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //if bluetooth not connected, then display snackbar and consume event (don't execute onCheckedChanged Listener)
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                Snackbar.make(v, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                return true; // this consumes click event
            }
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


        if (isChecked) {
            String outCommandToSend = OUT0_COMMAND_PREFIX + buttonView.getTag() + OUT_COMMAND_ON;
            Log.d(TAG, "onCheckedChanged: sending command to bluetooth " + outCommandToSend);
            mStringBuilderMessage.append("Output ").append(buttonView.getTag()).append(" turned ON");
            BluetoothConnectionService.getInstance().write(outCommandToSend);

        } else {
            String outCommandToSend = OUT0_COMMAND_PREFIX + buttonView.getTag() + OUT_COMMAND_OFF;
            Log.d(TAG, "onCheckedChanged: sending command to bluetooth " + outCommandToSend);
            mStringBuilderMessage.append("Output ").append(buttonView.getTag()).append(" turned OFF");
            BluetoothConnectionService.getInstance().write(outCommandToSend);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "onProgressChanged: called with progress: " + progress + " fromUser is: " + fromUser);

        String analogOutput = (String) seekBar.getTag();

        switch (analogOutput) {

            case ANALOG_0:
                mAnalogChannel0TextView.setText(String.valueOf(progress));
                break;
            case ANALOG_1:
                mAnalogChannel1TextView.setText(String.valueOf(progress));
                break;
            case ANALOG_2:
                mAnalogChannel2TextView.setText(String.valueOf(progress));
                break;
            case ANALOG_3:
                mAnalogChannel3TextView.setText(String.valueOf(progress));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStartTrackingTouch: called");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStopTrackingTouch: called");
        if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
            Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
            return;
        }
        String commmandToSend = OUT3_COMMAND_PREFIX + seekBar.getTag() + "," + seekBar.getProgress();
        double approxVoltage = (seekBar.getProgress() / 255.0) * 5.0;
        mStringBuilderMessage.append("Analog Output ").append(seekBar.getTag()).append(" set to ~").append(String.format(Locale.ENGLISH, "%.1f", approxVoltage)).append(" Vdc");
        Log.d(TAG, "sending command to bluetooth: " + commmandToSend);
        BluetoothConnectionService.getInstance().write(commmandToSend);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mBroadCastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String response = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                Log.d(TAG, "onReceive: called, action is: " + intent.getAction());
                String commandSent = intent.getStringExtra(BluetoothConnectionService.COMMAND_SENT);
                Log.d(TAG, "onReceive: called, command sent is: " + commandSent + " response is: " + response);

                if (response.equals("OK")) {
                    Snackbar.make(view, mStringBuilderMessage.toString(), Snackbar.LENGTH_SHORT).show();
                } else if (response.equals("?")) {
                    Snackbar.make(view, "COMMAND ERROR!", Snackbar.LENGTH_SHORT).show();

                }
                mStringBuilderMessage.delete(0, mStringBuilderMessage.length());
            }
        };

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadCastReceiver, new IntentFilter(BluetoothConnectionService.MY_INTENT_FILTER));
    }

    @Override
    public void onDetach() {

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadCastReceiver);
        super.onDetach();
    }


}



