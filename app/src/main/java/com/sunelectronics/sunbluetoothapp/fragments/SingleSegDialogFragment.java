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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;


public class SingleSegDialogFragment extends DialogFragment {
    private static final String TAG = "SingleSegDialogFragment";
    public static final int DELAY = 1000;
    private Button mButtonOK, mButtonCancel;
    private RadioButton mRadioButton;
    private RadioGroup mRadioGroup;
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

    private void initializeViews(final View view) {
        final EditText dampingCoefficientEditText = (EditText) view.findViewById(R.id.editTextDampingCoefficient);
        mTextInputEditTextWait = (TextInputEditText) view.findViewById(R.id.editTextWait);
        mTextInputEditTextWait.requestFocus();
        mTextInputEditTextRate = (TextInputEditText) view.findViewById(R.id.editTextRate);
        mTextInputEditTextSet = (TextInputEditText) view.findViewById(R.id.editTextSetPoint);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
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

                String dampingCoefficient = dampingCoefficientEditText.getText().toString();
                if (Integer.parseInt(dampingCoefficient) > 1000 || Integer.parseInt(dampingCoefficient)<0){
                    
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRadioButton = (RadioButton) view.findViewById(mRadioGroup.getCheckedRadioButtonId());
                        String pidACommand = "PIDA=" + mRadioButton.getTag();
                        Log.d(TAG, "run: sending pida command: " + pidACommand);


                    }
                });
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTextInputEditTextRate.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the rate...");
                            String rateCommand = "RATE=" + mTextInputEditTextRate.getText().toString();
                            BluetoothConnectionService.getInstance(getContext()).write(rateCommand);
                        }
                    }
                }, DELAY);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!mTextInputEditTextWait.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the wait...");
                            String waitCommand = "WAIT=" + mTextInputEditTextWait.getText().toString();
                            BluetoothConnectionService.getInstance(getContext()).write(waitCommand);
                        }

                    }
                }, 2 * DELAY);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTextInputEditTextSet.getText().toString().isEmpty()) {
                            Log.d(TAG, "setting the set...");
                            String setCommand = "SET=" + mTextInputEditTextSet.getText().toString();
                            BluetoothConnectionService.getInstance(getContext()).write(setCommand);
                        }

                    }
                }, 3 * DELAY);

                dismiss();

            }
        });
    }


}
