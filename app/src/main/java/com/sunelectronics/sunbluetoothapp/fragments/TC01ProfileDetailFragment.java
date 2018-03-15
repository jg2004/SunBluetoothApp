package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.database.Tc01ProfDataBaseHelper;
import com.sunelectronics.sunbluetoothapp.interfaces.IBottomNavigationListener;
import com.sunelectronics.sunbluetoothapp.interfaces.IBusy;
import com.sunelectronics.sunbluetoothapp.interfaces.IStop;
import com.sunelectronics.sunbluetoothapp.models.TC01SerialSendAgent;
import com.sunelectronics.sunbluetoothapp.models.Tc01Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELAY_2000MS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SCAN_MODE_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SEND_STOP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PROFILE_LIST;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_CMD_ERROR;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_INFINITY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_INFINITY_WAIT_TIME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_NO_SET_POINT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_START_SCANMODE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_STOP_SCANMODE;

public class TC01ProfileDetailFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener, IBusy,
        IStop {

    private static final String TAG = "TC01ProfDetailFragment";
    private static final int DELAY_MS_500 = 500;
    private Handler mHandler;
    private Context mContext;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mLpBroadcastReceiver;
    private Tc01ProfDataBaseHelper mDataBaseHelper;
    private View view;
    private EditText mCycles, mProfileName;
    private CheckBox mInfiniteCycles;
    private EditText[] mScanTemps = new EditText[10];
    private EditText[] mScanTimes = new EditText[10];
    private TC01SerialSendAgent mSendAgent;
    private String mCommandSent;
    private boolean mIsBusy;
    private Tc01Profile mProfile;
    private IBottomNavigationListener mBottomNavigationListener;
    private boolean mCommandErrorFromStartScanMode;


    public TC01ProfileDetailFragment() {//empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(R.layout.fragment_tc01_prof_detail, container, false);
        initializeViews(view);
        initializeHandlers();
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(SCAN_MODE_TITLE);
            supportActionBar.show();
        }
//        if (savedInstanceState != null) {
//            String lpContent = savedInstanceState.getString(LP_CONTENT);
//            String lpName = savedInstanceState.getString(LP_NAME);
//
//        }
        return view;
    }

    private void initializeHandlers() {

        mHandler = new Handler();
        mSendAgent = TC01SerialSendAgent.getInstance(mHandler);
    }

    private void initializeViews(View view) {

        Log.d(TAG, "initializeViews: called");
        ImageButton startScanMode = (ImageButton) view.findViewById(R.id.startScanModeButton);
        startScanMode.setOnClickListener(this);
        ImageButton stopScanMode = (ImageButton) view.findViewById(R.id.stopScanMode);
        stopScanMode.setOnClickListener(this);
        ImageButton saveButton = (ImageButton) view.findViewById(R.id.saveScanTempsButton);
        saveButton.setOnClickListener(this);
        ImageButton uploadScanTemps = (ImageButton) view.findViewById(R.id.upLoadButton);
        uploadScanTemps.setOnClickListener(this);
        ImageButton downloadScanTemps = (ImageButton) view.findViewById(R.id.downLoadButton);
        downloadScanTemps.setOnClickListener(this);
        mProfileName = (EditText) view.findViewById(R.id.profileName);
        mProfileName.setText(String.format(Locale.ENGLISH, "Profile %d", mDataBaseHelper.getCount() + 1));
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        mScanTemps[0] = (EditText) view.findViewById(R.id.editTextSet1);
        mScanTemps[1] = (EditText) view.findViewById(R.id.editTextSet2);
        mScanTemps[2] = (EditText) view.findViewById(R.id.editTextSet3);
        mScanTemps[3] = (EditText) view.findViewById(R.id.editTextSet4);
        mScanTemps[4] = (EditText) view.findViewById(R.id.editTextSet5);
        mScanTemps[5] = (EditText) view.findViewById(R.id.editTextSet6);
        mScanTemps[6] = (EditText) view.findViewById(R.id.editTextSet7);
        mScanTemps[7] = (EditText) view.findViewById(R.id.editTextSet8);
        mScanTemps[8] = (EditText) view.findViewById(R.id.editTextSet9);
        mScanTemps[9] = (EditText) view.findViewById(R.id.editTextSet10);
        mScanTimes[0] = (EditText) view.findViewById(R.id.editTextWait1);
        mScanTimes[1] = (EditText) view.findViewById(R.id.editTextWait2);
        mScanTimes[2] = (EditText) view.findViewById(R.id.editTextWait3);
        mScanTimes[3] = (EditText) view.findViewById(R.id.editTextWait4);
        mScanTimes[4] = (EditText) view.findViewById(R.id.editTextWait5);
        mScanTimes[5] = (EditText) view.findViewById(R.id.editTextWait6);
        mScanTimes[6] = (EditText) view.findViewById(R.id.editTextWait7);
        mScanTimes[7] = (EditText) view.findViewById(R.id.editTextWait8);
        mScanTimes[8] = (EditText) view.findViewById(R.id.editTextWait9);
        mScanTimes[9] = (EditText) view.findViewById(R.id.editTextWait10);
        mCycles = (EditText) view.findViewById(R.id.editTextCycles);
        mInfiniteCycles = (CheckBox) view.findViewById(R.id.checkboxInfinity);
        for (int i = 0; i < 10; i++) {
            mScanTemps[i].setOnFocusChangeListener(this);
            mScanTimes[i].setOnFocusChangeListener(this);
        }
        mInfiniteCycles.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mCycles.setText("");
                } else {
                    mCycles.requestFocus();
                }
            }
        });
        mCycles.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mInfiniteCycles.setChecked(false);
                }
            }
        });
        mCycles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfiniteCycles.setChecked(false);
            }
        });

    }

    private void populateViewsWithProfile() {

        Log.d(TAG, "populateViewsWithProfile: called with profile: " + mProfile);
        for (int i = 0; i < 10; i++) {

            mScanTemps[i].setText(mProfile.getScanTempTimes().get("A" + i));
            mScanTimes[i].setText(mProfile.getScanTempTimes().get("B" + i));

        }
        mProfileName.setText(mProfile.getName());
        if (mProfile.getCycles().equals(TC01_INFINITY)) {
            mInfiniteCycles.setChecked(true);
            mCycles.setText("");
        } else {
            mCycles.setText(mProfile.getCycles());
            mInfiniteCycles.setChecked(false);
        }
    }

    public void showDialog(Bundle bundle) {

        MyAlertDialogFragment myAlertdialogFragment = MyAlertDialogFragment.newInstance(bundle);
        myAlertdialogFragment.show(getFragmentManager(), null);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        EditText editText = (EditText) v;
        if (hasFocus && (editText.getText().toString().equals(TC01_NO_SET_POINT)
                || editText.getText().toString().equals(TC01_INFINITY_WAIT_TIME))) {
            editText.setText("");
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.downLoadButton:

                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (mIsBusy) {
                    Snackbar.make(view, getString(R.string.controller_busy), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                downloadScanTemps();
                break;

            case R.id.startScanModeButton:
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mIsBusy) {
                    Snackbar.make(view, R.string.controller_busy, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                startScanMode();
                break;

            case R.id.stopScanMode:
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (mIsBusy) {
                    Snackbar.make(view, R.string.controller_busy, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(ALERT_TITLE, "STOP SCAN MODE");
                bundle.putString(ALERT_MESSAGE, "OK to stop SCAN mode?");
                bundle.putString(ALERT_TYPE, SEND_STOP);
                bundle.putInt(ALERT_ICON, R.drawable.ic_stop_black_24dp);
                showDialog(bundle);
                break;

            case R.id.upLoadButton:
                Log.d(TAG, "onClick: upload button pressed");
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (mIsBusy) {
                    Snackbar.make(view, R.string.controller_busy, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (noInput() || !validCyclesEntered()) return;

                if (getNumberOfValidInputs() > 0) {
                    //proceed with upload
                    uploadScanTemps();
                } else {
                    return;
                }
                break;

            case R.id.saveScanTempsButton:
                if (mIsBusy) {
                    Snackbar.make(view, R.string.controller_busy, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                addProfileToDataBase();
                break;
        }
    }

    private void startScanMode() {
        mCommandErrorFromStartScanMode = false;
        mIsBusy = true;
        mCommandSent = TC01_START_SCANMODE;
        mSendAgent.sendCommand(mCommandSent);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mCommandErrorFromStartScanMode) {
                    Snackbar.make(view, "Scan mode started", Snackbar.LENGTH_SHORT).show();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsBusy = false;
                            mBottomNavigationListener.setNavigationItem(0);
                        }
                    }, 1000);
                } else {
                    mIsBusy = false;
                }
            }
        }, DELAY_2000MS);
    }

    private boolean validCyclesEntered() {

        if (mCycles.getText().toString().isEmpty() && !mInfiniteCycles.isChecked()) {
            Snackbar.make(view, "Enter number of cycles (1-1800) or check continuous for infinite cycles", Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (mInfiniteCycles.isChecked()) {
            return true;
        }

        try {
            float cycles = Float.parseFloat(mCycles.getText().toString());
            if (cycles > 1800 || cycles < 1) {
                Snackbar.make(view, "Enter a number of cycles (1-1800), or check continuous for infinite cycles", Snackbar.LENGTH_LONG).show();
                return false;
            }
        } catch (NumberFormatException e) {

            Log.d(TAG, "validCyclesEntered: invalid cycle number");
            Snackbar.make(view, "Invalid number of cycles", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private int getNumberOfValidInputs() {

        int numOfValidInputs = 0;
        if (mProfileName.getText().toString().isEmpty()) {
            Snackbar.make(view, "Enter a profile name", Snackbar.LENGTH_LONG).show();
            return 0;
        }

        for (int i = 0; i < 10; i++) {

            if (validateSegment(mScanTemps[i], mScanTimes[i], i)) {
                numOfValidInputs++;
            } else {
                return 0; //return 0 and user should fix issue
            }
        }
        Log.d(TAG, "getNumberOfValidInputs: number of valid inputs: " + numOfValidInputs);

        return numOfValidInputs;
    }

    private boolean validateSegment(EditText temp, EditText wait, int segmentNumber) {

        String setTemp = temp.getText().toString();
        String waitTime = wait.getText().toString();

        if (!setTemp.isEmpty() && !setTemp.contains(TC01_NO_SET_POINT) && (waitTime.isEmpty() || waitTime.contains(TC01_INFINITY_WAIT_TIME))) {
            Snackbar.make(view, "Enter a wait time for segment " + (segmentNumber + 1), Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if ((setTemp.isEmpty() || setTemp.contains(TC01_NO_SET_POINT)) && !waitTime.isEmpty() && !waitTime.contains(TC01_INFINITY_WAIT_TIME)) {
            Snackbar.make(view, "Enter a set temp for segment " + (segmentNumber + 1), Snackbar.LENGTH_SHORT).show();
            return false;
        }

        if (waitTime.isEmpty() || waitTime.contains(TC01_INFINITY_WAIT_TIME)) return true;

        try {

            float waitAsInt = Float.parseFloat(waitTime);
            if (waitAsInt <= 0 || waitAsInt > 1800) {
                Snackbar.make(view, "Segment " + (segmentNumber + 1) + " wait time must be greater than 0 and less than 1800", Snackbar.LENGTH_LONG).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Snackbar.make(view, "Invalid wait time for segment: " + (segmentNumber + 1), Snackbar.LENGTH_LONG).show();
            return false;
        }
        Log.d(TAG, "validateSegment: valid segment found");

        return true;
    }


    private boolean noInput() {

        //checks if no input was entered
        for (int i = 0; i < 10; i++) {
            if (!mScanTemps[i].getText().toString().isEmpty() && !mScanTemps[i].getText().toString().contains(TC01_NO_SET_POINT))
                return false;
            if (!mScanTimes[i].getText().toString().isEmpty() && !mScanTimes[i].getText().toString().contains(TC01_INFINITY_WAIT_TIME))
                return false;
        }
        Snackbar.make(view, "Enter at least one set point and one wait time", Snackbar.LENGTH_SHORT).show();
        return true;
    }


    private void uploadScanTemps() {

        List<String> scanTemps = new ArrayList<>();
        List<String> scanTimes = new ArrayList<>();
        String mmCycles;
        mIsBusy = true;
        for (int i = 0; i < 10; i++) {

            if (!mScanTemps[i].getText().toString().isEmpty() && !mScanTemps[i].getText().toString().contains(TC01_NO_SET_POINT)) {
                scanTemps.add(mScanTemps[i].getText().toString());
                scanTimes.add(mScanTimes[i].getText().toString());
                Log.d(TAG, "uploadScanTemps: added scan temp: " + mScanTemps[i].getText().toString() + " to list");
                Log.d(TAG, "uploadScanTemps: added scan time: " + mScanTimes[i].getText().toString() + " to list");
            }
        }
        Log.d(TAG, "uploadScanTemps: scanTemp list is: " + scanTemps.size());
        initializeProgressBar(scanTemps.size() * 2 + 1 + 20);// 20 deletions + 2* number of scan temps + 1 cycle
        if (mInfiniteCycles.isChecked()) {
            mmCycles = "1999.9";
        } else {
            mmCycles = mCycles.getText().toString();
        }
        ScanUploadRunnable runnable = new ScanUploadRunnable(scanTemps, scanTimes, mmCycles);
        mHandler.post(runnable);

    }

    private void downloadScanTemps() {
        mIsBusy = true;
        clearFields();
        initializeProgressBar(21);
        Runnable scanDownloadRunnable = new ScanDownloadRunnable();
        mHandler.postDelayed(scanDownloadRunnable, DELAY_MS_500);
    }

    private void initializeProgressBar(int max) {

        mProgressBar.setMax(max);
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isBusy() {
        return mIsBusy;
    }

    @Override
    public void sendStop() {
        mSendAgent.sendCommand(TC01_STOP_SCANMODE);
        Snackbar.make(view, "Scan mode stopped", Snackbar.LENGTH_SHORT).show();
    }

    public void setProfile(Tc01Profile profile) {

        mProfile = profile;
    }


    private class ScanDownloadRunnable implements Runnable {
        int mmCounter = 0;
        int mmProgressCounter = 0;
        char scanChar = 'A';

        @Override
        public void run() {

            if (mmCounter <= 9 && scanChar == 'A') {
                mCommandSent = String.valueOf(scanChar) + mmCounter;
                mmCounter++;
            } else if (mmCounter >= 9 && scanChar == 'A') {
                scanChar++;
                mmCounter = 0;
                mCommandSent = String.valueOf(scanChar) + mmCounter;

            } else if (mmCounter <= 9 && scanChar == 'B') {
                mCommandSent = String.valueOf(scanChar) + mmCounter;
                mmCounter++;
            } else if (mmCounter >= 9 && scanChar == 'B') {
                mCommandSent = String.valueOf(scanChar) + "-";
                mSendAgent.sendCommand(mCommandSent);
                mProgressBar.setVisibility(View.INVISIBLE);
                mIsBusy = false;
                Log.d(TAG, "run: finished downloading");
                return;
            }
            Log.d(TAG, "run: sent command: " + mCommandSent);
            mSendAgent.sendCommand(mCommandSent);
            mHandler.postDelayed(this, DELAY_MS_500);
            mmProgressCounter++;
            mProgressBar.setProgress(mmProgressCounter);
        }
    }

    /**
     * this is a runnable class used to upload scan temp and scan time to EC0x chamber or TC01 controller
     */
    private class ScanUploadRunnable implements Runnable {

        int mmScanTempCounter, mmScanTimecounter, mmScanDeleteCounter, mmProgress;
        char mmScanChar = 'A';
        List<String> mmScanTemps, mmScanTimes;
        String mmCycles;
        boolean scanTempUploadsComplete, scanDeletionsComplete;

        ScanUploadRunnable(List<String> scanTemps, List<String> scanTimes, String cycles) {
            mmScanTemps = scanTemps;
            mmScanTimes = scanTimes;
            mmCycles = cycles;
            Snackbar.make(view, "Deleting previous scan temp/times...", Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public void run() {
            Log.d(TAG, "inside upload runnable scantimecounter is: " + mmScanTimecounter + " scantempcounter is: " + mmScanTempCounter);
            mmProgress++;
            mProgressBar.setProgress(mmProgress);

            if (!scanDeletionsComplete) {

                //delete all scan temps and times first
                Log.d(TAG, "run: deleting " + mmScanChar + mmScanDeleteCounter);
                mCommandSent = "-" + String.valueOf(mmScanChar) + mmScanDeleteCounter;
                mSendAgent.sendCommand(mCommandSent);
                mmScanDeleteCounter++;
                if (mmScanDeleteCounter == 10) {
                    mmScanChar++;
                    if (mmScanChar == 'C') {
                        //deletions complete
                        Log.d(TAG, "run: deletions complete, starting scantemp and time uploads...");
                        Snackbar.make(view, "Deletions complete, uploading new scan temp/times...", Snackbar.LENGTH_SHORT).show();
                        scanDeletionsComplete = true;
                    }
                    mmScanDeleteCounter = 0;
                }
                mHandler.postDelayed(this, DELAY_MS_500);
                return;
            }

            if (scanTempUploadsComplete) {
                //once scam uploads complete, send cycles command
                Log.d(TAG, "sending number of cycles");
                mCommandSent = mmCycles + "B-";
                mSendAgent.sendCommand(mCommandSent);
                mIsBusy = false;
                mProgressBar.setVisibility(View.INVISIBLE);
                return;
            }
            if (mmScanTempCounter == mmScanTimecounter) {
                mCommandSent = mmScanTemps.get(mmScanTempCounter) + "A" + mmScanTempCounter;
                mSendAgent.sendCommand(mCommandSent);
                mmScanTempCounter++;
            } else {
                mCommandSent = mmScanTimes.get(mmScanTimecounter) + "B" + mmScanTimecounter;
                mSendAgent.sendCommand(mCommandSent);
                mmScanTimecounter++;
                if (mmScanTimecounter == mmScanTemps.size()) {
                    //stop runnable
                    Log.d(TAG, "run, scan temp and time upload is complete");
                    scanTempUploadsComplete = true;
                }
            }
            mHandler.postDelayed(this, DELAY_MS_500);
        }
    }

    private void clearFields() {

        for (int i = 0; i < 10; i++) {
            mScanTimes[i].setText("");
            mScanTemps[i].setText("");
        }
        mCycles.setText("");
        mInfiniteCycles.setChecked(false);
    }

    /**
     * adds a profile to database
     */
    private void addProfileToDataBase() {

        Log.d(TAG, "addProfileToDataBase: called");

        if (noInput() || getNumberOfValidInputs() < 1 || !validCyclesEntered()) {
            return;
        }
        String profileName = mProfileName.getText().toString();
        Tc01Profile profile = new Tc01Profile(profileName);

        for (int i = 0; i < 10; i++) {

            profile.addSegment(i, mScanTemps[i].getText().toString(), mScanTimes[i].getText().toString());

        }

        if (mInfiniteCycles.isChecked()) {
            profile.setCycles(TC01_INFINITY);
        } else {
            profile.setCycles(mCycles.getText().toString());
        }
        Log.d(TAG, "addProfileToDataBase: adding profile to database");
        mDataBaseHelper.addProfileToDataBase(profile);
        clearFields();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_tc01_profile, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem deleteMenuItem = menu.findItem(R.id.action_deleteAllProfiles);
        deleteMenuItem.setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_showSavedProfiles:
                if (mIsBusy) {
                    Snackbar.make(view, R.string.controller_busy, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                Log.d(TAG, "onClick: show saved Profiles clicked");
                ProfileListFragment frag = new ProfileListFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.homeContainer, frag, TAG_FRAGMENT_PROFILE_LIST).commit();
                return true;

            case R.id.action_clear_fields:
                if (mIsBusy) {
                    Snackbar.make(view, R.string.controller_busy, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                clearFields();
                break;

            case R.id.action_loadSampleProfile:
                if (mIsBusy) {
                    Snackbar.make(view, R.string.controller_busy, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                loadSampleScanTemps();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSampleScanTemps() {
        mScanTemps[0].setText(R.string.sample_profile_low_temp);
        mScanTemps[1].setText(R.string.sample_profile_high_temp);
        mScanTemps[2].setText(R.string.sample_profile_room_temp);
        mScanTimes[0].setText(R.string.sample_profile_hour);
        mScanTimes[1].setText(R.string.sample_profile_hour);
        mScanTimes[2].setText(R.string.sample_profile_five_min);
        mCycles.setText(R.string.sample_profile_cycles);
        mInfiniteCycles.setChecked(false);
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        mContext = context;
        SQLiteOpenHelper helper = ((HomeActivity) context).getDataBaseHelper();
        mDataBaseHelper = (Tc01ProfDataBaseHelper) helper;
        mBottomNavigationListener = (HomeActivity) context;

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called, registering broadcast receiver");

        mLpBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String response = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                Log.d(TAG, "onReceive: called with action: " + intent.getAction());
                Log.d(TAG, "Command sent:  " + mCommandSent + ", response received was: " + response);
                populateView(response);
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLpBroadcastReceiver, new IntentFilter(BluetoothConnectionService.MY_INTENT_FILTER));
    }

    private void populateView(String response) {

        if (mCommandSent == null) {
            Log.d(TAG, "populateView: mCommandSent was null, response is: " + response);
            return;
        }
        if (mCommandSent.contains("A") && response.equals("1999.9")) {
            response = TC01_NO_SET_POINT;
        }
        if (mCommandSent.charAt(0) == 'B' && mCommandSent.charAt(1) != '-' && response.equals("1999.9")) {
            response = TC01_INFINITY_WAIT_TIME;
        }
        if (mCommandSent.equals(TC01_START_SCANMODE) && response.equals(TC01_CMD_ERROR)) {
            Snackbar.make(view, "No Scan Temp or Scan Times have been uploaded", Snackbar.LENGTH_INDEFINITE).show();
            mCommandErrorFromStartScanMode = true;
            return;
        }

        if (response.contains(TC01_CMD_ERROR)) {
            Snackbar.make(view, "CMD ERROR!", Snackbar.LENGTH_LONG).show();
            return;
        }


        switch (mCommandSent) {

            case "A0":
                mScanTemps[0].setText(response);
                break;
            case "A1":
                mScanTemps[1].setText(response);
                break;
            case "A2":
                mScanTemps[2].setText(response);
                break;
            case "A3":
                mScanTemps[3].setText(response);
                break;
            case "A4":
                mScanTemps[4].setText(response);
                break;
            case "A5":
                mScanTemps[5].setText(response);
                break;
            case "A6":
                mScanTemps[6].setText(response);
                break;
            case "A7":
                mScanTemps[7].setText(response);
                break;
            case "A8":
                mScanTemps[8].setText(response);
                break;
            case "A9":
                mScanTemps[9].setText(response);
                break;
            case "B0":
                mScanTimes[0].setText(response);
                break;
            case "B1":
                mScanTimes[1].setText(response);
                break;
            case "B2":
                mScanTimes[2].setText(response);
                break;
            case "B3":
                mScanTimes[3].setText(response);
                break;
            case "B4":
                mScanTimes[4].setText(response);
                break;
            case "B5":
                mScanTimes[5].setText(response);
                break;
            case "B6":
                mScanTimes[6].setText(response);
                break;
            case "B7":
                mScanTimes[7].setText(response);
                break;
            case "B8":
                mScanTimes[8].setText(response);
                break;
            case "B9":
                mScanTimes[9].setText(response);
                break;
            case "B-":
                if (response.equals(TC01_INFINITY)) {
                    mInfiniteCycles.setChecked(true);

                } else {
                    mInfiniteCycles.setChecked(false);
                    mCycles.setText(response);
                }
                break;
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called, unregistering broadcast receiver");
        //mLpUploadHandler.removeCallbacks(mLpUploadRunnable);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLpBroadcastReceiver);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
        if (mProfile != null) {
            populateViewsWithProfile();
        }
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
    public void onSaveInstanceState(Bundle outState) {
//        String lpContent = mLpContent.getText().toString();
//        String lpName = mLpName.getText().toString();
//        if (!lpContent.isEmpty()) {
//            outState.putString(LP_CONTENT, lpContent);
//            outState.putString(LP_NAME, lpName);
//        }
        super.onSaveInstanceState(outState);
    }
}
