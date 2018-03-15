package com.sunelectronics.sunbluetoothapp.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.models.TC01Controller;
import com.sunelectronics.sunbluetoothapp.models.TC01SerialSendAgent;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONTROLLER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DIALOG_TITLE;


public class TC01SingleSegDialogFragment extends DialogFragment {
    private static final String TAG = "TC01SingleSegDialogFrag";
    public static final int DELAY = 1000;
    private TextInputEditText mTextInputEditTextWait, mTextInputEditTextSet;
    private Handler mHandler = new Handler();
    private TC01Controller mTC01Controller;
    private TC01SerialSendAgent mSendAgent;


    public TC01SingleSegDialogFragment() {
        Log.d(TAG, "SingleSegDialogFragment: empty constructor called");
        //required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        }
        mSendAgent = TC01SerialSendAgent.getInstance(mHandler);
    }

    public static TC01SingleSegDialogFragment newInstance(String title, TC01Controller controller) {

        Log.d(TAG, "newInstance: creating instance of SignleSegDialogFragment");
        TC01SingleSegDialogFragment fragment = new TC01SingleSegDialogFragment();
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
        return inflater.inflate(R.layout.dialog_frag_tc01_segment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String title = args.getString(DIALOG_TITLE, "DEFAULT TITLE");
        getDialog().setTitle(title);
        mTC01Controller = (TC01Controller) args.getSerializable(CONTROLLER);
        initializeViews(view);
    }

    private void initializeViews(final View view) {
        mTextInputEditTextWait = (TextInputEditText) view.findViewById(R.id.editTextWait);
        mTextInputEditTextWait.requestFocus();
        mTextInputEditTextSet = (TextInputEditText) view.findViewById(R.id.editTextSetPoint);
        Button buttonCancel = (Button) view.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Button buttonOK = (Button) view.findViewById(R.id.buttonOk);
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: OK was pressed, sending single segment commands...");

                if (!validWaitTime()) return;

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!mTextInputEditTextWait.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the wait...");
                            String waitCommand = mTC01Controller.getWaitCommand(mTextInputEditTextWait.getText().toString());
                            mSendAgent.sendCommand(waitCommand);
                        }
                    }
                }, DELAY);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTextInputEditTextSet.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the set...");
                            String setCommand = mTC01Controller.getSetCommand(mTextInputEditTextSet.getText().toString());
                            mSendAgent.sendCommand(setCommand);
                        }
                    }
                }, 2 * DELAY);
                dismiss();
            }
        });
    }

    private boolean validWaitTime() {

        if (mTextInputEditTextWait.getText().toString().isEmpty()) return true;
        try {

            float waitTime = Float.parseFloat(mTextInputEditTextWait.getText().toString());
            if (waitTime <= 0 || waitTime > 1999) {
                Snackbar.make(getView(), "Invalid wait time. Enter a wait time > 0 and less than 2000", Snackbar.LENGTH_LONG).show();
                return false;
            }

        } catch (NumberFormatException e) {

            Log.d(TAG, "validWaitTime: number format exception");
            Snackbar.make(getView(), "Invalid wait time", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;

    }
}