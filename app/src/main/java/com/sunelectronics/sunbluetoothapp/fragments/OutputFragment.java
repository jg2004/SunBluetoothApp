package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ANALOG_0;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ANALOG_1;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ANALOG_2;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ANALOG_3;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OUT0_COMMAND_PREFIX;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OUT3_COMMAND_PREFIX;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OUT_COMMAND_OFF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.OUT_COMMAND_ON;

public class OutputFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "OutputFragment";
    private ToggleButton mToggleButtonOutput2, mToggleButtonOutput3, mToggleButtonOutput4, mToggleButtonOutput5, mToggleButtonOutput6;
    private SeekBar mSeekBar0, mSeekBar1, mSeekBar2, mSeekBar3;
    private TextView mAnalogChannel0TextView, mAnalogChannel1TextView, mAnalogChannel2TextView, mAnalogChannel3TextView;
    private BroadcastReceiver mBroadCastReceiver;
    private Context mContext;
    private StringBuilder mStringBuilderMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        mStringBuilderMessage = new StringBuilder();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_outputs, container, false);
        ((HomeActivity) getActivity()).getSupportActionBar().setTitle("CHAMBER OUTPUTS");

        intializeViews(view);
        return view;
    }

    private void intializeViews(View view) {

        mAnalogChannel0TextView = (TextView) view.findViewById(R.id.channel_0_textView);
        mAnalogChannel1TextView = (TextView) view.findViewById(R.id.channel_1_textView);
        mAnalogChannel2TextView = (TextView) view.findViewById(R.id.channel_2_textView);
        mAnalogChannel3TextView = (TextView) view.findViewById(R.id.channel_3_textView);

        mToggleButtonOutput2 = (ToggleButton) view.findViewById(R.id.buttonOutput2);
        mToggleButtonOutput2.setOnCheckedChangeListener(this);

        mToggleButtonOutput3 = (ToggleButton) view.findViewById(R.id.buttonOutput3);
        mToggleButtonOutput3.setOnCheckedChangeListener(this);

        mToggleButtonOutput4 = (ToggleButton) view.findViewById(R.id.buttonOutput4);
        mToggleButtonOutput4.setOnCheckedChangeListener(this);

        mToggleButtonOutput5 = (ToggleButton) view.findViewById(R.id.buttonOutput5);
        mToggleButtonOutput5.setOnCheckedChangeListener(this);

        mToggleButtonOutput6 = (ToggleButton) view.findViewById(R.id.buttonOutput6);
        mToggleButtonOutput6.setOnCheckedChangeListener(this);

        mSeekBar0 = (SeekBar) view.findViewById(R.id.seekbarChannel0);
        mSeekBar0.setOnSeekBarChangeListener(this);
        mAnalogChannel0TextView.setText(String.valueOf(mSeekBar0.getProgress()));

        mSeekBar1 = (SeekBar) view.findViewById(R.id.seekbarChannel1);
        mSeekBar1.setOnSeekBarChangeListener(this);
        mAnalogChannel1TextView.setText(String.valueOf(mSeekBar1.getProgress()));

        mSeekBar2 = (SeekBar) view.findViewById(R.id.seekbarChannel2);
        mSeekBar2.setOnSeekBarChangeListener(this);
        mAnalogChannel2TextView.setText(String.valueOf(mSeekBar2.getProgress()));

        mSeekBar3 = (SeekBar) view.findViewById(R.id.seekbarChannel3);
        mSeekBar3.setOnSeekBarChangeListener(this);
        mAnalogChannel3TextView.setText(String.valueOf(mSeekBar3.getProgress()));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            String outCommandToSend = OUT0_COMMAND_PREFIX + buttonView.getTag() + OUT_COMMAND_ON;
            Log.d(TAG, "onCheckedChanged: sending command to bluetooth " + outCommandToSend);
            mStringBuilderMessage.append("Output ").append(buttonView.getTag()).append(" turned ON");
            BluetoothConnectionService.getInstance(getContext()).write(outCommandToSend);

        } else {
            String outCommandToSend = OUT0_COMMAND_PREFIX + buttonView.getTag() + OUT_COMMAND_OFF;
            Log.d(TAG, "onCheckedChanged: sending command to bluetooth " + outCommandToSend);
            mStringBuilderMessage.append("Output ").append(buttonView.getTag()).append(" turned OFF");
            BluetoothConnectionService.getInstance(getContext()).write(outCommandToSend);
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
        String commmandToSend = OUT3_COMMAND_PREFIX + seekBar.getTag() + "," + seekBar.getProgress();
        double approxVoltage = (seekBar.getProgress()/255.0) * 5.0;
        mStringBuilderMessage.append("Analog Output ").append(seekBar.getTag()).append(" set to ~").append(String.format("%.1f",approxVoltage)).append(" Vdc");
        Log.d(TAG, "sending command to bluetooth: " + commmandToSend);
        BluetoothConnectionService.getInstance(getContext()).write(commmandToSend);
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
                    Snackbar.make(getView(), mStringBuilderMessage.toString(), Snackbar.LENGTH_SHORT).show();
                } else if (response.equals("?")) {
                    Snackbar.make(getView(), "COMMAND ERROR!", Snackbar.LENGTH_SHORT).show();

                }
                mStringBuilderMessage.delete(0,mStringBuilderMessage.length());
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



