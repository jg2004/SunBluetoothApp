package com.sunelectronics.sunbluetoothapp.fragments;

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

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;


public class SingleSegDialogFragment extends DialogFragment {
    private static final String TAG = "SingleSegDialogFragment";
    private Button mButtonOK, mButtonCancel;
    private TextInputEditText mTextInputEditTextRate, mTextInputEditTextWait, mTextInputEditTextSet;
    private Handler mHandler = new Handler();


    public SingleSegDialogFragment() {
        Log.d(TAG, "SingleSegDialogFragment: empty constructor called");
        //required empty constructor
    }

    public static SingleSegDialogFragment newInstance(String title) {

        Log.d(TAG, "newInstance: creating instance of SignleSegDialogFragment");
        SingleSegDialogFragment fragment = new SingleSegDialogFragment();
        Bundle args = new Bundle();
        args.putString("TITLE", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.dialog_frag_single_seg, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        String title = args.getString("TITLE", "DEFAULT TITLE");
        getDialog().setTitle(title);
        initializeViews(view);

    }

    private void initializeViews(View view) {
        mTextInputEditTextWait = (TextInputEditText) view.findViewById(R.id.editTextWait);
        mTextInputEditTextWait.requestFocus();

        mTextInputEditTextRate = (TextInputEditText) view.findViewById(R.id.editTextRate);
        mTextInputEditTextSet = (TextInputEditText) view.findViewById(R.id.editTextSetPoint);
        mButtonCancel = (Button) view.findViewById(R.id.buttonCancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mButtonOK = (Button) view.findViewById(R.id.buttonOk);
        mButtonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: OK was pressed, sending single segment commands...");

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTextInputEditTextRate.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the rate...");
                            String rateCommand = "RATE=" + mTextInputEditTextRate.getText().toString();
                            BluetoothConnectionService.getInstance(getContext()).write(rateCommand);
                        }
                    }
                }, 0);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!mTextInputEditTextWait.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the wait...");
                            String waitCommand = "WAIT=" + mTextInputEditTextWait.getText().toString();
                            BluetoothConnectionService.getInstance(getContext()).write(waitCommand);
                        }

                    }
                }, 1000);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTextInputEditTextSet.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the set...");
                            String setCommand = "SET=" + mTextInputEditTextSet.getText().toString();
                            BluetoothConnectionService.getInstance(getContext()).write(setCommand);
                        }

                    }
                }, 2000);

                dismiss();

            }
        });
    }


}
