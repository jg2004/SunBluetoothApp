package com.sunelectronics.sunbluetoothapp.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHandler;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;
import com.sunelectronics.sunbluetoothapp.ui.LPRecyclerViewAdapter;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_CONFIRM_EXIT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_LOG_FILES;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_LOG_FILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SEND_STOP;

public class MyAlertDialogFragment extends DialogFragment {
    private static final String TAG = "MyAlertDialogFragment";
    private LPRecyclerViewAdapter mAdpaptor;
    private LogFileListFragment.DeleteLogFileListener mDeleteLogFileListener;

    public interface OnPositiveDialogClick {

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

        String title = getArguments().getString(ALERT_TITLE);
        int iconId = getArguments().getInt(ALERT_ICON);
        String message = getArguments().getString(ALERT_MESSAGE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(iconId)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //((OnPositiveDialogClick) getActivity()).onPositiveClick(getArguments());
                        Log.d(TAG, "onClick: ok was pressed");
                        performAction();
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

    @Override
    public void onAttach(Context context) {

        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        //HomeActivity implements the DeleteLogFileListener
        mDeleteLogFileListener = (HomeActivity) context;
    }

    private void performAction() {

        String type = getArguments().getString(ALERT_TYPE);
        if (type == null) {
            type = "Type Not Found"; //this just prevents switch (null) from causing app to crash
        }

        switch (type) {

            case DELETE_LOG_FILE:

                Bundle args = getArguments();
                String fileName = args.getString(LOG_FILE_NAME);
                //HomeActivity contains deleteLogFile method
                mDeleteLogFileListener.deleteLogFile(fileName);
                break;

            case DELETE_ALL_LOG_FILES:
                //HomeActivity contains deleteAllLogFiles() method
                mDeleteLogFileListener.deleteAllLogFiles();
                break;

            case DELETE_LP:

                LocalProgram lp = (LocalProgram) getArguments().getSerializable(LP);
                if (lp != null) {
                    LPDataBaseHandler.getInstance(getContext()).deleteLPFromDB(lp.getId());
                    if (getFragmentManager().getBackStackEntryCount() > 0) {
                        getFragmentManager().popBackStack();
                    } else {
                        getFragmentManager().beginTransaction().replace(R.id.homeContainer, new LocalProgramListFragment()).commit();
                    }
                } else {
                    Toast.makeText(getContext(), "Local Program was null", Toast.LENGTH_SHORT).show();
                }
                break;

            case DELETE_ALL_LP:

                LPDataBaseHandler.getInstance(getContext()).deleteAllLocalPrograms();

                // TODO: 11/2/2017 should the frag trans code above be added to this case as well?

                break;

            case SEND_STOP:

                BluetoothConnectionService.getInstance(getContext()).write("STOP");
                break;

            case ALERT_CONFIRM_EXIT:

                Log.d(TAG, "performAction: need to implement alert confirm exit");
                // TODO: 10/30/2017
                // showConfirmDialog = false;
                //onBackPressed();
                break;

            default:
                Toast.makeText(getContext(), "dialog type not found", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}