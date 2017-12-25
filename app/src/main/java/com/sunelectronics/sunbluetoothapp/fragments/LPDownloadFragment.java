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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHandler;
import com.sunelectronics.sunbluetoothapp.models.ChamberStatus;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.POWER_ON_DELAY_MS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SEND_STOP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.STATUS;

public class LPDownloadFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LPDownloadFrag";
    private static final int UPLOAD_DELAY_MS = 500;
    private static final int GET_STATUS_DELAY_MS = 2000;
    private ImageButton mSaveButton, mUploadLPButton, mRunLPButton, mStopLPButton, mDownLoadLPButton;
    private EditText mLpName, mLpContent;
    private LocalProgram mLocalProgram;
    private Handler mLpUploadHandler, mLpInitializeLPHandler, mGetChamberStatusHandler;
    private Runnable mLpUploadRunnable, mGetChamberStatusRunnable;
    private Context mContext;
    private Spinner mSpinner;
    private String mSpinnerSelectionNumber, mLpname;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mLpBroadcastReceiver;
    private LPDataBaseHandler mLPDataBaseHandler;
    private ChamberStatus mChamberStatus;
    private BottomNavigationListenter mBottomNavigationListenter;
    private boolean mIsLPRunning;
    private View view;

    public interface DownloadPInterface {
        //interface implemented by HomeActivity to send LP number from LPDetailFragment to LPDownloadFragment
        void downloadLP(String lpNumber);
    }

    public interface BottomNavigationListenter {
        //interface implemented by HomeActivity to switch bottom navigation item

        void setNavigationItem(int position);
    }

    public LPDownloadFragment() {//empty constructor
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
        view = inflater.inflate(R.layout.fragment_lp_download, container, false);
        initializeViews(view);
        initializeHandlers();
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(TAG);
            supportActionBar.show();
        }
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
        menu.findItem(R.id.action_deleteLocalProgram).setVisible(false);
        menu.findItem(R.id.action_share_LP).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_showSavedLPs:
                Log.d(TAG, "onClick: show saved Lp's clicked");
                LocalProgramListFragment frag = new LocalProgramListFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.homeContainer, frag, LocalProgramListFragment.TAG_FRAG_LP_LIST).commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeHandlers() {

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

        Log.d(TAG, "initializeViews: called");
        mRunLPButton = (ImageButton) view.findViewById(R.id.startLPButton);
        mRunLPButton.setOnClickListener(this);
        mStopLPButton = (ImageButton) view.findViewById(R.id.stopLPButton);
        mStopLPButton.setOnClickListener(this);
        mSaveButton = (ImageButton) view.findViewById(R.id.saveLPButton);
        mSaveButton.setOnClickListener(this);
        mUploadLPButton = (ImageButton) view.findViewById(R.id.upLoadButton);
        mUploadLPButton.setOnClickListener(this);
        mDownLoadLPButton = (ImageButton) view.findViewById(R.id.downLoadButton);
        mDownLoadLPButton.setOnClickListener(this);
        mLpName = (EditText) view.findViewById(R.id.lpName);
        Log.d(TAG, "initializeViews: setting lp name to " + mLpname);
        mLpName.setText(mLpname);
        mLpContent = (EditText) view.findViewById(R.id.lpContent);
        Log.d(TAG, "initializeViews: clearing lp content");

        if (mLocalProgram == null) {
            Bundle args = getArguments();
            if (args != null) {
                mLocalProgram = (LocalProgram) args.getSerializable(LP);
            }
        }

        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        try {

            Log.d(TAG, "initializeViews: inside try block, setting spinner to " + mSpinnerSelectionNumber);
            mSpinner.setSelection(Integer.parseInt(mSpinnerSelectionNumber));

        } catch (NullPointerException | NumberFormatException e) {
            Log.d(TAG, "initializeViews: inside catch block, setting spinner to 0");
            mSpinner.setSelection(0);
        }
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

                if (mIsLPRunning) {
                    Log.d(TAG, "onClick: download button pressed. Cannot Download while LP is running");
                    Snackbar.make(view, "Cannot Download while LP is running", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "dowloading lp number " + lpNumber);
                //stop the getStatushandler before downloading;
                Log.d(TAG, "onClick: removing chamber status callback");
                mGetChamberStatusHandler.removeCallbacks(mGetChamberStatusRunnable);
                downloadLP(lpNumber);
                mGetChamberStatusHandler.postDelayed(mGetChamberStatusRunnable, GET_STATUS_DELAY_MS);
                break;

            case R.id.startLPButton:
                if (mIsLPRunning) {
                    Snackbar.make(view, "LP is already running", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                mGetChamberStatusHandler.removeCallbacks(mGetChamberStatusRunnable);
                BluetoothConnectionService.getInstance(mContext).write("RUN" + lpNumber);
                break;

            case R.id.stopLPButton:
                Bundle bundle = new Bundle();
                bundle.putString(ALERT_TITLE, "STOP LP");
                bundle.putString(ALERT_MESSAGE, "OK to stop local program?");
                bundle.putString(ALERT_TYPE, SEND_STOP);
                bundle.putInt(ALERT_ICON, R.drawable.ic_stop_black_24dp);
                showDialog(bundle);
                break;

            case R.id.upLoadButton:
                Log.d(TAG, "onClick: upload button pressed");
                if (mIsLPRunning) {
                    Log.d(TAG, "LP is running. Cannot upload while LP is running");
                    Snackbar.make(view, "Cannot Upload while LP is running", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "onClick: removing chamber status callback");
                mGetChamberStatusHandler.removeCallbacks(mGetChamberStatusRunnable);
                if (!mChamberStatus.isPowerOn()) {
                    //turn on chamber
                    BluetoothConnectionService.getInstance(mContext).write(ON);
                }
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

            case R.id.saveLPButton:
                addLPtoDB();
                break;
        }
    }

    private void uploadLocalProgram(final String lpNumber) {
        String lp = mLpContent.getText().toString();
        if (lp.isEmpty()) {
            Snackbar.make(view, "Enter an LP to upload", Snackbar.LENGTH_LONG).show();
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

    public void downloadLP(String lpNumber) {

        Log.d(TAG, "downloadLP: called");
        mLpname = "Local Program " + lpNumber;
        mLpName.setText(mLpname);
        mLpContent.setHint("");
        mLpContent.setText("");
        Log.d(TAG, "setting spinner to " + lpNumber);
        mSpinnerSelectionNumber = lpNumber;
        mSpinner.setSelection(Integer.parseInt(mSpinnerSelectionNumber));
        BluetoothConnectionService.getInstance(mContext).write("LIST" + lpNumber);
    }

    //------the addLptoDb method and validate input are methods used in LPCreateEditFrag------------
    //------To avoid code duplication code, consider separating these into their own class----------

    /**
     * adds local program to database
     */
    private void addLPtoDB() {

        Log.d(TAG, "addLPtoDB: called");
        if (!validateInput()) {
            return;
        }
        //if valid input create lp and store to database
        String lpName = mLpName.getText().toString();
        String lpContent = mLpContent.getText().toString();

        LocalProgram lp = new LocalProgram(lpName, lpContent);
        mLPDataBaseHandler.addLocalProgramToDB(lp);
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(mLpName.getText().toString())) {
            Snackbar.make(view, "Enter a local program name", Snackbar.LENGTH_SHORT).show();
            mLpName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mLpContent.getText().toString())) {
            Snackbar.make(view, "Enter a local program", Snackbar.LENGTH_SHORT).show();
            mLpContent.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * this is a runnable class used to upload local program to chamber
     */
    private class LpUploadRunnable implements Runnable {

        String[] lpArray;
        int counter = 0;

        LpUploadRunnable(String[] lpArray) {
            this.lpArray = lpArray;
            mProgressBar.setProgress(counter);
            mProgressBar.setMax(lpArray.length);
            mProgressBar.setVisibility(View.VISIBLE);
            //disable window
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        public void run() {
            Log.d(TAG, "run: called");
            if (counter < lpArray.length) {

                Log.d(TAG, "run: writing " + lpArray[counter]);
                BluetoothConnectionService.getInstance(mContext).write(lpArray[counter]);
                counter++;
                mProgressBar.setProgress(counter);
                mLpUploadHandler.postDelayed(this, UPLOAD_DELAY_MS);

            } else {
                /*once all contents of lpArray are uploaded, remove callback, set progressbar to
                invisible and start getStatus runnable back up*/
                mLpUploadHandler.removeCallbacks(this);
                mProgressBar.setVisibility(View.INVISIBLE);
                mGetChamberStatusHandler.post(mGetChamberStatusRunnable);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(mContext, "Upload complete", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        mContext = context;
        mLPDataBaseHandler = LPDataBaseHandler.getInstance(getContext());

        mBottomNavigationListenter = (HomeActivity) context;

    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called, creating chamber status object, starting getStatus runnable and registering broadcast receiver");

        //this kicks off the runnable to get the STATUS? string every 5 seconds. Give it a delay
        //to allow downloadLP to retrieve lp before polling with STATUS command
        mGetChamberStatusHandler.postDelayed(mGetChamberStatusRunnable, GET_STATUS_DELAY_MS);

        mChamberStatus = new ChamberStatus();
        mLpBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String commandSent = intent.getStringExtra(BluetoothConnectionService.COMMAND_SENT);
                String response = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                Log.d(TAG, "onReceive: called with action: " + intent.getAction());
                Log.d(TAG, "Command sent:  " + commandSent + ", response received was: " + response);

                if (commandSent.contains("LIST") || commandSent.contains("NO COMMAND SENT")) {

                    if (mLpContent.getText().toString().isEmpty()) {
                        mLpContent.setText(mLpContent.getText().toString() + response);

                    } else {
                        mLpContent.setText(mLpContent.getText().toString() + '\n' + response);
                    }
                } else if (commandSent.contains("RUN")) {

                    if (response.equals("OK")) {
                        Snackbar.make(view, "LP running!", Snackbar.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBottomNavigationListenter.setNavigationItem(0);//implemented in HomeActivity
                            }
                        }, 2000);
                    }
                } else if (commandSent.equals(STATUS)) {
                    Log.d(TAG, "setting status of chamber status object");
                    mChamberStatus.setStatusMessages(response);
                    //this field is checked to see if chamber in LP run mode before downloading and uploading an LP
                    mIsLPRunning = mChamberStatus.isLPRunning();
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLpBroadcastReceiver, new IntentFilter(BluetoothConnectionService.MY_INTENT_FILTER));
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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
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
}
