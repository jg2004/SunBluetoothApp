package com.sunelectronics.sunbluetoothapp.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.sunelectronics.sunbluetoothapp.R;


public class PassiveDialogFragment extends DialogFragment {
    private static final String TAG = "PassiveDialogFragment";

    public interface OnPassiveDialogFragmentListener {

        void dialogDismissed();
    }

    public static final String MESSAGE = "message";
    private OnPassiveDialogFragmentListener mPassiveDialogFragmentListener;

    public static PassiveDialogFragment newInstance(String message) {

        Log.d(TAG, "newInstance: called");
        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        PassiveDialogFragment f = new PassiveDialogFragment();
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Log.d(TAG, "onCreateDialog: called");
        String message = getArguments().getString(MESSAGE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(message).setIcon(R.drawable.ic_action_alert).setTitle("Location permission required");
        builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                mPassiveDialogFragmentListener.dialogDismissed();
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: called");

        if (context instanceof OnPassiveDialogFragmentListener) {
            mPassiveDialogFragmentListener = (OnPassiveDialogFragmentListener) context;
        }

    }
}