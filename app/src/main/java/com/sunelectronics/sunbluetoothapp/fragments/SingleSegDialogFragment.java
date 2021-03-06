package com.sunelectronics.sunbluetoothapp.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.models.TemperatureController;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DIALOG_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC10_STOP_COMMAND;


public class SingleSegDialogFragment extends DialogFragment {
    private static final String TAG = "SingleSegDialogFragment";
    public static final int DELAY = 1000;
    private TextInputEditText mTextInputEditTextRate, mTextInputEditTextWait, mTextInputEditTextSet;
    private Handler mHandler = new Handler();
    private TemperatureController mTemperatureController;


    public SingleSegDialogFragment() {
        Log.d(TAG, "SingleSegDialogFragment: empty constructor called");
        //required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        }
    }

    public static SingleSegDialogFragment newInstance(String title, TemperatureController controller) {

        Log.d(TAG, "newInstance: creating instance of SignleSegDialogFragment");
        SingleSegDialogFragment fragment = new SingleSegDialogFragment();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putSerializable(CONTROLLER, controller);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        return inflater.inflate(R.layout.dialog_frag_single_seg, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String title = args.getString(DIALOG_TITLE, "DEFAULT TITLE");
        getDialog().setTitle(title);
        mTemperatureController = (TemperatureController) args.getSerializable(CONTROLLER);
        initializeViews(view);
    }

    private void initializeViews(final View view) {
        mTextInputEditTextWait = (TextInputEditText) view.findViewById(R.id.editTextWait);
        mTextInputEditTextWait.requestFocus();
        mTextInputEditTextRate = (TextInputEditText) view.findViewById(R.id.editTextRate);
        mTextInputEditTextSet = (TextInputEditText) view.findViewById(R.id.editTextSetPoint);
        Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ImageButton buttonStop = (ImageButton) view.findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "sending STOP command");
                        BluetoothConnectionService.getInstance().write(TC10_STOP_COMMAND);
                    }
                }, DELAY);
                dismiss();
            }
        });
        Button buttonOK = (Button) view.findViewById(R.id.buttonOk);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: OK was pressed, sending single segment commands...");

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTextInputEditTextRate.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the rate...");
                            String rateCommand = mTemperatureController.getRateCommand() + mTextInputEditTextRate.getText().toString();
                            BluetoothConnectionService.getInstance().write(rateCommand);
                        }
                    }
                }, DELAY);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!mTextInputEditTextWait.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the wait...");
                            String waitCommand = mTemperatureController.getWaitCommand() + mTextInputEditTextWait.getText().toString();
                            BluetoothConnectionService.getInstance().write(waitCommand);
                        }

                    }
                }, 2 * DELAY);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTextInputEditTextSet.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the set...");
                            String setCommand = mTemperatureController.getSetCommand() + mTextInputEditTextSet.getText().toString();
                            BluetoothConnectionService.getInstance().write(setCommand);
                        }

                    }
                }, 3 * DELAY);
                dismiss();
            }
        });
    }
}