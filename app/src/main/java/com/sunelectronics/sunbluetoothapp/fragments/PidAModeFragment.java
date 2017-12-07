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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDA_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDA_MODE_0;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDA_MODE_1;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDA_MODE_2;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDA_MODE_3;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDA_MODE_4;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDA_QUERY;

public class PidAModeFragment extends Fragment {
    private static final String TAG = "PidAModeFragment";
    private RadioButton mRadioButtonChecked, mRadioButton_mode0, mRadioButton_mode1, mRadioButton_mode2;
    private RadioButton mRadioButton_mode3, mRadioButton_mode4;
    private RadioGroup mRadioGroup;
    private EditText mDampingCoefficient;
    private Button mSetButton, mGetButton;
    private Handler mHandler = new Handler();
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_pida_mode, container, false);
        initializeViews(view);
        return view;
    }

    private void initializeViews(final View view) {

        mDampingCoefficient = (EditText) view.findViewById(R.id.editTextDampingCoefficient);
        mRadioButton_mode0 = (RadioButton) view.findViewById(R.id.chamberRadioButton);
        mRadioButton_mode1 = (RadioButton) view.findViewById(R.id.chamberUserAverageRadioButton);
        mRadioButton_mode2 = (RadioButton) view.findViewById(R.id.slowlyForceUserRadioButton);
        mRadioButton_mode3 = (RadioButton) view.findViewById(R.id.userRadioButton);
        mRadioButton_mode4 = (RadioButton) view.findViewById(R.id.averageSlowlyForceUserRadioButton);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        mGetButton = (Button) view.findViewById(R.id.buttonGet);
        mGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "GET clicked, getting parameters");
                sendPidAQuery();
            }
        });
        mSetButton = (Button) view.findViewById(R.id.buttonSet);
        mSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "SET clicked, sending commands");
                mRadioButtonChecked = (RadioButton) view.findViewById(mRadioGroup.getCheckedRadioButtonId());
                if (isDampingCoefficientValid()) {
                    sendPidAcommand();
                }
            }//end of OnClick event
        });
    }

    private boolean isDampingCoefficientValid() {

        if (mDampingCoefficient.getText().toString().isEmpty()) {
            return true;
        }
        try {
            int dampingCoefficient = Integer.parseInt(mDampingCoefficient.getText().toString());

            if (dampingCoefficient > 1000 || dampingCoefficient < 0) {

                Snackbar.make(getView(), "Damping Coefficient must be between 0 and 1000", Snackbar.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Log.d(TAG, "getPidAcommand: exception: " + e.getMessage());
            return false;

        }
        return true;
    }


    private void sendPidAQuery() {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending PidAQuery");
                BluetoothConnectionService.getInstance(getContext()).write(PIDA_QUERY);
            }
        });
    }

    private void sendPidAcommand() {
        final String pidAcommand = getPidAcommand();


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending pida command: " + pidAcommand);
                BluetoothConnectionService.getInstance(getContext()).write(pidAcommand);
            }
        });
    }

    private String getPidAcommand() {
        String tag = (String) mRadioButtonChecked.getTag();
        String dampingCoefficient = mDampingCoefficient.getText().toString();
        if (dampingCoefficient.isEmpty()) {
            dampingCoefficient = "400";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(PIDA_COMMAND);

        switch (tag) {
            case "0":
                sb.append(tag);
                break;
            case "1":
                sb.append(tag);
                break;
            case "2":
            case "4":
                sb.append(tag);
                sb.append(",").append(dampingCoefficient);
                break;
            case "3":
                sb.append(tag);
                break;
        }
        return sb.toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {


                StringBuilder sb = new StringBuilder();
                sb.append("Problem retrieving PIDA mode");
                String commandSent = intent.getStringExtra(BluetoothConnectionService.COMMAND_SENT);
                String response = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                Log.d(TAG, "onReceive: called, command sent: " + commandSent + " response received: " + response);

                if (commandSent.contains(PIDA_COMMAND)) {

                    StringBuilder sb1 = new StringBuilder();
                    if (response.contains("OK")) {

                        if (commandSent.contains("PIDA=0")) {
                            sb1.append("PIDA mode set to: " + PIDA_MODE_0);
                        } else if (commandSent.contains("PIDA=1")) {
                            sb1.append("PIDA mode set to: " + PIDA_MODE_1);
                        } else if (commandSent.contains("PIDA=2")) {
                            sb1.append("PIDA mode set to: " + PIDA_MODE_2);
                        } else if (commandSent.contains("PIDA=3")) {
                            sb1.append("PIDA mode set to: " + PIDA_MODE_3);
                        } else if (commandSent.contains("PIDA=4")) {
                            sb1.append("PIDA mode set to: " + PIDA_MODE_4);
                        } else {
                            sb1.append("INVALID PIDA COMMAND!");
                        }

                        Snackbar.make(getView(), sb1.toString(), Snackbar.LENGTH_SHORT).show();
                    }
                }

                switch (commandSent) {

                    case PIDA_QUERY:

                        if (response.equals("0")) {
                            mRadioButton_mode0.setChecked(true);
                            sb.delete(0, sb.length());
                            sb.append("Chamber set to: " + PIDA_MODE_0);
                        } else if (response.equals("1")) {
                            mRadioButton_mode1.setChecked(true);
                            sb.delete(0, sb.length());
                            sb.append("Chamber set to: " + PIDA_MODE_1);
                        } else if (response.equals("2")) {
                            mRadioButton_mode2.setChecked(true);
                            sb.delete(0, sb.length());
                            sb.append("Chamber set to: " + PIDA_MODE_2);
                        } else if (response.equals("3")) {
                            mRadioButton_mode3.setChecked(true);
                            sb.delete(0, sb.length());
                            sb.append("Chamber set to: " + PIDA_MODE_3);
                        } else if (response.equals("4")) {
                            mRadioButton_mode4.setChecked(true);
                            sb.delete(0, sb.length());
                            sb.append("Chamber set to: " + PIDA_MODE_4);
                        }
                        Snackbar.make(getView(), sb.toString(), Snackbar.LENGTH_LONG).show();
                        break;

                    default:

                        Log.d(TAG, "inside the default case, response is: " + response);
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
}