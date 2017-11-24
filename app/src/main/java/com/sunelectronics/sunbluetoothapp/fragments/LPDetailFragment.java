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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.models.ChamberStatus;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.POWER_ON_DELAY_MS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.STATUS;

public class LPDetailFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LPDetailFragment";
    private static final int UPLOAD_DELAY_MS = 500;
    private static final int GET_STATUS_DELAY_MS = 3000;
    private TextView mLpdetailname, mLpdetailcontent;
    private ImageButton mEditButton, mUploadLPButton, mDownLoadLPButton;
    private LocalProgram mLocalProgram;
    private Handler mLpUploadHandler, mLpInitializeLPHandler, mGetChamberStatusHandler;
    private Runnable mLpUploadRunnable, mGetChamberStatusRunnable;
    private Context mContext;
    private Spinner mSpinner;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mLpBroadcastReceiver;
    private LPDownloadFragment.DownloadPInterface mDownloadPInterface;
    private ChamberStatus mChamberStatus;

    public LPDetailFragment() {//empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_lp_detail, container, false);
        initializeViews(view);
        initializeHandler();

        // TODO: 10/25/2017 fix this shit below!!
        //((LocalProgramActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.local_program )+ " id: " +mLocalProgram.getId());

        ((HomeActivity) getActivity()).getSupportActionBar().setTitle(TAG);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_local_program, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.action_deleteAllLocalPrograms).setVisible(false);
        menu.findItem(R.id.action_loadSampleLP).setVisible(false);
        menu.findItem(R.id.action_showSavedLPs).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_deleteLocalProgram:
                Log.d(TAG, "onClick: delete pressed");
                Bundle lpArgs = new Bundle();
                lpArgs.putSerializable(LP, mLocalProgram);
                lpArgs.putString(ALERT_TITLE, DELETE_MESSAGE);
                lpArgs.putString(ALERT_MESSAGE, "OK to Delete LP " + mLocalProgram.getName() + "?");
                lpArgs.putString(ALERT_TYPE, DELETE_LP);
                lpArgs.putInt(ALERT_ICON, R.drawable.ic_delete_black_48dp);
                showDialog(lpArgs);
                return true;

            case R.id.action_share_LP:
                String subject = "Local Program";
                composeEmail(subject);

        }
        return super.onOptionsItemSelected(item);
    }

    private void composeEmail(String subject) {
        //intent.setType("message/rfc822");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mLocalProgram.getContent());
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("text/plain");

        if (sendIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(sendIntent, "Share local program..."));
        }
    }

    public void setLocalProgram(LocalProgram localProgram) {
        mLocalProgram = localProgram;
    }

    private void initializeHandler() {

        mLpUploadHandler = new Handler();
        mLpInitializeLPHandler = new Handler();
        mGetChamberStatusHandler = new Handler();
        mGetChamberStatusRunnable = new Runnable() {
            @Override
            public void run() {
                BluetoothConnectionService.getInstance(mContext).write(STATUS);
                mGetChamberStatusHandler.postDelayed(this, GET_STATUS_DELAY_MS);
            }
        };

    }

    private void initializeViews(View view) {


        mDownLoadLPButton = (ImageButton) view.findViewById(R.id.downLoadButton);
        mDownLoadLPButton.setOnClickListener(this);

        mEditButton = (ImageButton) view.findViewById(R.id.editButton);
        mEditButton.setOnClickListener(this);

        mUploadLPButton = (ImageButton) view.findViewById(R.id.upLoadButton);
        mUploadLPButton.setOnClickListener(this);

        mLpdetailname = (TextView) view.findViewById(R.id.detailLpName);
        mLpdetailcontent = (TextView) view.findViewById(R.id.detailLpContent);

        if (mLocalProgram == null) {
            mLocalProgram = (LocalProgram) getArguments().getSerializable(LP);
        }
        mLpdetailname.setText(mLocalProgram.getName());
        mLpdetailcontent.setText(mLocalProgram.getContent());
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mSpinner.setSelection(0, true);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBarLp);
        mProgressBar.setVisibility(View.INVISIBLE);

    }

    public void showDialog(Bundle bundle) {

        MyAlertDialogFragment myAlertdialogFragment = MyAlertDialogFragment.newInstance(bundle);
        myAlertdialogFragment.show(getFragmentManager(), null);
    }

    @Override
    public void onClick(View v) {

        final String lpNumber = String.valueOf(mSpinner.getSelectedItemPosition());

        switch (v.getId()) {

            case R.id.downLoadButton:
                Log.d(TAG, "onClick: download pressed");
                if (mChamberStatus.isLPRunning()) {
                    Log.d(TAG, "Cannot Download while LP is running");
                    Snackbar.make(getView(), "Cannot Download while LP is running", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "onClick: removing chamber status callback");
                mGetChamberStatusHandler.removeCallbacks(mGetChamberStatusRunnable);
                if (getFragmentManager().getBackStackEntryCount() > 0) {

                    //remove all fragment transactions from backstack
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                LPDownloadFragment fragment = (LPDownloadFragment) getFragmentManager().findFragmentByTag(HomeActivity.TAG_FRAGMENT_LOCAL_PROGRAM);
                //send lp number to LPDownloadFragment so that it can perform download of LP at that location

                mDownloadPInterface.downloadLP(lpNumber);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.homeContainer, fragment).commit();


                break;

            case R.id.upLoadButton:
                Log.d(TAG, "onClick: upload button pressed");
                if (mChamberStatus.isLPRunning()) {
                    Log.d(TAG, "LP is running. Cannot upload while LP is running");
                    Snackbar.make(getView(), "Cannot Upload while LP is running", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (!mChamberStatus.isPowerIsOn()) {
                    //turn on chamber
                    BluetoothConnectionService.getInstance(mContext).write(ON);
                }


                Log.d(TAG, "onClick: removing chamber status callback");
                mGetChamberStatusHandler.removeCallbacks(mGetChamberStatusRunnable);
                uploadLocalProgram(lpNumber);

                break;

            case R.id.editButton:
                LPCreateEditFragment lpcreateEditFragment = new LPCreateEditFragment();
                Bundle args = new Bundle();
                args.putSerializable(LP, mLocalProgram);
                lpcreateEditFragment.setArguments(args);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.addToBackStack(null);
                ft.replace(R.id.homeContainer, lpcreateEditFragment).commit();
                break;
        }
    }

    private void uploadLocalProgram(final String lpNumber) {

        String lp = mLpdetailcontent.getText().toString();
        if (lp.isEmpty()) {
            Snackbar.make(getView(), "Enter an LP to upload", Snackbar.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "lp content is: " + lp);
        String[] lpArray = lp.split("\n");
        Log.d(TAG, "lpArray size is " + lpArray.length);

        mLpInitializeLPHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: deleting LP " + lpNumber);
                BluetoothConnectionService.getInstance(mContext).write("DELP" + lpNumber);
            }
        }, POWER_ON_DELAY_MS);
        mLpInitializeLPHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                BluetoothConnectionService.getInstance(mContext).write("STORE#" + lpNumber);

            }
        }, POWER_ON_DELAY_MS + 1000);

        mLpUploadRunnable = new LpUploadRunnable(lpArray);
        mLpUploadHandler.postDelayed(mLpUploadRunnable, POWER_ON_DELAY_MS + 2000);
    }

    private class LpUploadRunnable implements Runnable {

        String[] lpArray;
        int counter = 0;
        boolean endCommandSent;

        public LpUploadRunnable(String[] lpArray) {
            this.lpArray = lpArray;
            mProgressBar.setProgress(counter);
            mProgressBar.setMax(lpArray.length);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void run() {
            Log.d(TAG, "run: called");
            if (counter < lpArray.length) {

                //keep sending commands until all have been sent (all items in lpArray)
                Log.d(TAG, "run: writing " + lpArray[counter]);
                if (lpArray[counter].trim().equals("END")) {
                    Log.d(TAG, "the END command was sent");
                    endCommandSent = true;
                }
                BluetoothConnectionService.getInstance(mContext).write(lpArray[counter]);
                counter++;
                mProgressBar.setProgress(counter);
                mLpUploadHandler.postDelayed(this, UPLOAD_DELAY_MS);

            } else {
                //this is done after all commands in LP have been sent
                if (!endCommandSent) {
                    //send the END command to exit EDIT mode if not in LP
                    Log.d(TAG, "no END command in LP, sending END command to exit EDIT mode");
                    BluetoothConnectionService.getInstance(mContext).write("END");
                }
                endCommandSent = false;
                mLpUploadHandler.removeCallbacks(this);
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(mContext, "Upload complete", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mDownloadPInterface = (HomeActivity) context;
        Log.d(TAG, "onAttach: called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: called");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called, creating chamber status object and registering broadcast receiver");
        //kick off chamber status handler
        mGetChamberStatusHandler.post(mGetChamberStatusRunnable);
        mChamberStatus = new ChamberStatus();
        mLpBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String commandSent = intent.getStringExtra(BluetoothConnectionService.COMMAND_SENT);
                String response = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                Log.d(TAG, "onReceive: called with action: " + intent.getAction());
                Log.d(TAG, "Command sent:  " + commandSent + ", response received was: " + response);
                //this code checks for invalid command in local program was sent
                if (response.equals("?")) {
                    Log.d(TAG, "onReceive: received a command error when " + commandSent + " was sent");
                    Snackbar.make(getView(), "INVALID COMMAND SENT: " + commandSent, Snackbar.LENGTH_LONG).show();
                    //exit EDIT MODE by sending the  "STOP" commmand
                    BluetoothConnectionService.getInstance(mContext).write("STOP");
                    mLpUploadHandler.removeCallbacks(mLpUploadRunnable);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(mContext, "Upload cancelled", Toast.LENGTH_LONG).show();
                } else if (commandSent.equals(STATUS)) {
                    Log.d(TAG, "setting status of chamber status object");
                    mChamberStatus.setStatusMessages(response);
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLpBroadcastReceiver, new IntentFilter(BluetoothConnectionService.MY_INTENT_FILTER));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called, removing lpUploadHandler and getChamberStatusHandler callbacks and unregistering " +
                "broadcast receiver");
        mLpUploadHandler.removeCallbacks(mLpUploadRunnable);
        mGetChamberStatusHandler.removeCallbacks(mGetChamberStatusRunnable);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLpBroadcastReceiver);
        super.onStop();
    }
}
