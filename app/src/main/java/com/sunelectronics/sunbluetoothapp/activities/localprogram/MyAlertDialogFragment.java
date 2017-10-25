package com.sunelectronics.sunbluetoothapp.activities.localprogram;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.sunelectronics.sunbluetoothapp.R;

public class MyAlertDialogFragment extends DialogFragment {
    private static final String TAG = "MyAlertDialogFragment";

    interface OnPositiveDialogClick {

        void onPositiveClick(Bundle args);
    }

    //create an instance of MyAlertDialogFragment with bundle containing title, icon, objects passed in as argument


    public static MyAlertDialogFragment newInstance(Bundle args) {
        Log.d(TAG, "newInstance: called");
        MyAlertDialogFragment dialogFragment = new MyAlertDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: called");

        String title = getArguments().getString(LocalProgramActivity.ALERT_TITLE);
        int iconId = getArguments().getInt(LocalProgramActivity.ALERT_ICON);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setIcon(iconId)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ((OnPositiveDialogClick) getActivity()).onPositiveClick(getArguments());

                        if (getFragmentManager().getBackStackEntryCount() > 0) {
                            getFragmentManager().popBackStack();
                        } else {
                            getFragmentManager().beginTransaction().replace(R.id.localProgramContainer, new LPListFragment()).commit();
                        }
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
