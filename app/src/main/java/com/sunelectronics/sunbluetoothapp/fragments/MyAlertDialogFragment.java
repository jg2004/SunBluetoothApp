package com.sunelectronics.sunbluetoothapp.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.activities.IntroActivity;
import com.sunelectronics.sunbluetoothapp.interfaces.IDelete;
import com.sunelectronics.sunbluetoothapp.interfaces.IStop;
import com.sunelectronics.sunbluetoothapp.interfaces.Iid;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_CONFIRM_EXIT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_NOTIFICATION;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_LOG_FILES;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_PROFILES;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_LOG_FILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_PROFILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EXIT_APP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PROFILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SEND_STOP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TERMINATE_LOGGING_SESSION;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TURN_OFF_CHAMBER;

public class MyAlertDialogFragment extends DialogFragment {
    private static final String TAG = "MyAlertDialogFragment";
    private LogFileListFragment.DeleteLogFileListener mDeleteLogFileListener;
    private DisplayTemperatureFragment.DisplayTemperatureFragmentCallBacks mDisplayTemperatureFragmentCallBacks;
    private IDelete mIDelete;
    private IStop mIstop;

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
        String type = getArguments().getString(ALERT_TYPE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(iconId)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.d(TAG, "onClick: ok was pressed");
                        performAction();
                    }
                });
        // only set a negative button if not a notification type of alert
        if (type != null && !type.equals(ALERT_NOTIFICATION)) {

            builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });

        }

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {

        Log.d(TAG, "onAttach: called");
        super.onAttach(context);

        //HomeActivity implements the DeleteLogFileListener

        if (context instanceof HomeActivity) {
            Log.d(TAG, "context is instance of HomeActivity");
            mDeleteLogFileListener = (HomeActivity) context;
            mDisplayTemperatureFragmentCallBacks = (HomeActivity) context;
            SQLiteOpenHelper helper = ((HomeActivity) context).getDataBaseHelper();
            mIDelete = (IDelete) helper;
            mIstop = (HomeActivity) context;
        } else if (context instanceof IntroActivity) {

            Log.d(TAG, "context is instance of IntroActivity");
            mDeleteLogFileListener = (IntroActivity) context;
        }
    }

    private void performAction() {

        String type = getArguments().getString(ALERT_TYPE);
        Log.d(TAG, "performAction: called, type is: " + type);

        if (type == null) {
            type = "Type Not Found"; //this just prevents switch (null) from causing app to crash
        }

        switch (type) {

            case ALERT_NOTIFICATION:
                dismiss();
                break;
            case EXIT_APP:
                //HomeActivity has closeActivity method
                mDisplayTemperatureFragmentCallBacks.closeActivity();
                break;

            case TERMINATE_LOGGING_SESSION:
                //HomeActivity has stopLoggingSession() method
                mDisplayTemperatureFragmentCallBacks.stopLoggingSession();
                break;

            case TURN_OFF_CHAMBER:
                //HomeActivity contains the turnOffChamber() method
                mDisplayTemperatureFragmentCallBacks.turnOffChamber();
                break;

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

            case DELETE_PROFILE:

                Iid profile = (Iid) getArguments().getSerializable(PROFILE);
                if (profile != null) {
                    mIDelete.deleteProfile(profile.getId());

                    if (profile instanceof LocalProgram) {

                        if (getFragmentManager().getBackStackEntryCount() > 0) {
                            getFragmentManager().popBackStack();
                        } else {
                            getFragmentManager().beginTransaction().replace(R.id.homeContainer, new LocalProgramListFragment()).commit();
                        }
                    }
                }
                break;


            case DELETE_ALL_PROFILES:
                mIDelete.deleteAllProfiles();
                // TODO: 11/2/2017 should the frag trans code above be added to this case as well?

                break;

            case SEND_STOP:
                mIstop.sendStop();
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