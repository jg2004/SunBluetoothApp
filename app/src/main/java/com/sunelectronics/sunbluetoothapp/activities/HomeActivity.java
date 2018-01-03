package com.sunelectronics.sunbluetoothapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.fragments.DisplayTemperatureFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPCreateEditFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPDetailFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPDownloadFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LogFileListFragment;
import com.sunelectronics.sunbluetoothapp.fragments.MyAlertDialogFragment;
import com.sunelectronics.sunbluetoothapp.fragments.ParameterFragment;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;

import java.util.ArrayList;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService.STATE_CONNECTED;
import static com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService.STATE_CONNECTING;
import static com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService.STATE_NONE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONNECTION_LOST;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EXIT_APP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PARAMETER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_LP_DETAIL_FRAG;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.UPDATE_BT_STATE;

public class HomeActivity extends AppCompatActivity implements LogFileListFragment.DeleteLogFileListener,
        LPDownloadFragment.BottomNavigationListenter, LPCreateEditFragment.RefreshFragment,
        LPDownloadFragment.DownloadPInterface, DisplayTemperatureFragment.DisplayTemperatureFragmentCallBacks {

    private static final String TAG = "HomeActivity";
    private List<Fragment> mFragmentList = new ArrayList<>();
    public static final String TAG_FRAGMENT_MONITOR = "tag_frag_monitor";
    public static final String TAG_FRAGMENT_LOCAL_PROGRAM = "tag_frag_lp";
    public static final String TAG_FRAGMENT_LOGGER = "tag_frag_logger";
    private View view;
    private BottomNavigationView mBottomNavigationView;
    private BroadcastReceiver mReceiver;
    private Toolbar mToolbar;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        view = findViewById(R.id.activityLayout);
        buildFragmentList();
        setUpViews();
        switchFragment(0, TAG_FRAGMENT_MONITOR);
    }

    //-------------------------------private methods----------------------------------------------

    /**
     * initialize all views
     */
    private void setUpViews() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setToolbarSubTitle();
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //if busy uploading lp or downloading params, ignore bottom navigation click
                if (checkIfBusy()) {
                    return false; // so menu item is NOT selected
                }


                Log.d(TAG, "onNavigationItemSelected: called");
                int itemId = item.getItemId();

                switch (itemId) {

                    case R.id.bottom_bar_monitor:
                        switchFragment(0, TAG_FRAGMENT_MONITOR);
                        return true;

                    case R.id.bottom_bar_local_program:
                        switchFragment(1, TAG_FRAGMENT_LOCAL_PROGRAM);
                        return true;


                    case R.id.bottom_bar_communicate:
                        switchFragment(2, TAG_FRAGMENT_LOGGER);
                        return true;
                }
                return false;
            }
        });
    }

    private void setToolbarSubTitle() {

        //set subtitle based on bluetooth connection state
        Log.d(TAG, "setToolbarSubTitle: state of bluetooth is: " + BluetoothConnectionService.getInstance().getCurrentState());
        switch (BluetoothConnectionService.getInstance().getCurrentState()) {
            case STATE_CONNECTED:
                mToolbar.setSubtitle("Connected to: " +
                        BluetoothConnectionService.getInstance().getDevice().getName());
                break;
            case STATE_CONNECTING:
                mToolbar.setSubtitle("Connecting to: " +
                        BluetoothConnectionService.getInstance().getDevice().getName());
                break;
            case STATE_NONE:
                mToolbar.setSubtitle("Not connected");
                break;
        }
    }

    /**
     * add fragments to fragment array list
     */
    private void buildFragmentList() {

        mFragmentList.add(new DisplayTemperatureFragment());
        mFragmentList.add(new LPDownloadFragment());
        mFragmentList.add(new LogFileListFragment());
    }

    /**
     * perform the fragment transaction
     *
     * @param position is index of array list holding fragments
     * @param tag      is the tag assigned to fragment
     */

    private void switchFragment(int position, String tag) {

        Log.d(TAG, "switchFragment: called");

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);//this clears all fragments off backstack
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.homeContainer, mFragmentList.get(position), tag).commit();
    }

    public void refreshLPDetailFragment(LocalProgram localProgram) {

        //called from LPCreateEditFragment to send lp with new data to LPDetailFragment
        Log.d(TAG, "refreshLPDetailFragment: called");
        LPDetailFragment lpDetailFragment = (LPDetailFragment) getSupportFragmentManager().findFragmentByTag(TAG_LP_DETAIL_FRAG);
        lpDetailFragment.setLocalProgram(localProgram);
    }

    private void showAlertDialog() {
        Bundle args = new Bundle();
        args.putString(ALERT_TITLE, EXIT_APP);
        args.putString(ALERT_TYPE, EXIT_APP);
        args.putString(ALERT_MESSAGE, "Logging session in progress, exit application?");
        args.putInt(ALERT_ICON, R.drawable.ic_warning_black_48dp);
        MyAlertDialogFragment myAlertDialogFragment = MyAlertDialogFragment.newInstance(args);
        myAlertDialogFragment.show(getSupportFragmentManager(), null);
    }

    /*------------interface implemented methods--------------------------------------------------*/

    @Override
    public void stopLoggingSession() {
        //called by MyAlertDialogFragment if user presses Yes button on dialog
        Log.d(TAG, "stopLoggingSession: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        DisplayTemperatureFragment displayTemperatureFragment = (DisplayTemperatureFragment) fragmentManager
                .findFragmentByTag(TAG_FRAGMENT_MONITOR);
        displayTemperatureFragment.stopLogger();
    }

    @Override
    public void closeActivity() {
        //called by MyAlertDialogFragment if user presses Yes button on dialog
        Log.d(TAG, "closing HomeActivity");
        finish();
    }

    @Override
    public void turnOffChamber() {
        //called by MyAlertDialogFragment if user presses Yes button on dialog
        Log.d(TAG, "turnOffChamber: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        DisplayTemperatureFragment displayTemperatureFragment = (DisplayTemperatureFragment) fragmentManager
                .findFragmentByTag(TAG_FRAGMENT_MONITOR);
        displayTemperatureFragment.turnOffChamberSwitch();
    }

    @Override
    public void deleteLogFile(String fileName) {

        Log.d(TAG, "deleteLogFile: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        LogFileListFragment fragment = (LogFileListFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_LOGGER);
        //LogFileListFragment has deleteLogFileMethod
        fragment.deleteLogFile(fileName);
    }

    @Override
    public void deleteAllLogFiles() {

        Log.d(TAG, "deleteAllLogFiles: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        LogFileListFragment fragment = (LogFileListFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_LOGGER);
        //LogFileListFragment has deleteLogFileMethod
        fragment.deleteAllLogFiles();
    }


    @Override
    public void downloadLP(String lpNumber) {
        //interface used to send lp number from LPDetailFrag to LPDownloadFrag

        LPDownloadFragment lpDownloadFrag = (LPDownloadFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_LOCAL_PROGRAM);
        lpDownloadFrag.downloadLP(lpNumber);
    }

    /**
     * @param position and int representing the bottom navigation view position
     *                 this method can be called from any fragment to switch to another fragment
     *                 and simulate user clicking on bottom navigation view
     *                 for example, this is called from LPDownloadfragment to switch from LPDownloadFragment to TemperatureDisplayFragment
     *                 after starting a local program
     */
    @Override
    public void setNavigationItem(int position) {

        Log.d(TAG, "setNavigationItem: called from LPDownloadFragment");

        // TODO: 11/12/2017 is following necessary? since its done in switchFragment method above
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);//this clears all fragments off backstack
        }
        switch (position) {

            case 0:
                mBottomNavigationView.setSelectedItemId(R.id.bottom_bar_monitor); // TODO: 11/12/2017 doesn't this do the same action if user presses button?
                break;
            // TODO: 11/9/2017 implement remaining frag position as necessary
        }
    }

//---------------------overriden methods from AppCompatActivity class---------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // getMenuInflater().inflate(R.menu.menu_local_program,menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: closing bluetooth connection");
        //clean up: cancel bluetooth thread and close TemperatureLogWriter text file
        BluetoothConnectionService.getInstance().stop();
        FragmentManager fragmentManager = getSupportFragmentManager();
        DisplayTemperatureFragment frag = (DisplayTemperatureFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT_MONITOR);
        frag.closeLoggingFile();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");

        //make sure no lp's are being uploaded or parameters being downloades
        if (checkIfBusy()) {
            return;
        }

        DisplayTemperatureFragment tempMonitorFrag = (DisplayTemperatureFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MONITOR);
        if (tempMonitorFrag == null) {
            tempMonitorFrag = new DisplayTemperatureFragment();
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && !tempMonitorFrag.isVisible()) {
            Log.d(TAG, "onBackPressed: backstack entry is 0 and tempMonitor is not visible");
            mBottomNavigationView.setSelectedItemId(R.id.bottom_bar_monitor);

        } else {
            if (tempMonitorFrag.isLoggingData()) {
                // if logging data, show alert dialog to confirm closing of app
                showAlertDialog();

            } else {
                //not logging data so close HomeActivity and exit the app
                Log.d(TAG, "onBackPressed: super.onBackPressed executed, backstackentrycount is: " + getSupportFragmentManager().getBackStackEntryCount());
                super.onBackPressed();//this closes HomeActivity (closes the app)
            }
        }
    }

    /**
     * This method checks 3 fragments to see if busy uploading LP, or downloading parameters before
     * allowing onBackPressed or bottom navigation view to perform fragment transactions
     *
     * @return true if busy, false if not
     */
    private boolean checkIfBusy() {

        ParameterFragment parameterFragment = (ParameterFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_PARAMETER);
        if (parameterFragment != null) {
            if (parameterFragment.isBusyDownLoading()) {
                Snackbar.make(view, R.string.download_parameters_message, Snackbar.LENGTH_SHORT).show();
                return true; //prevents backing out of parameter fragment until download is complete
            }
        }

        LPDownloadFragment lpDownloadFragment = (LPDownloadFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_LOCAL_PROGRAM);
        if (lpDownloadFragment != null) {
            if (lpDownloadFragment.isBusyUploadingLp()) {
                Snackbar.make(view, R.string.lp_busy_uploading, Snackbar.LENGTH_SHORT).show();
                return true; //prevents backing out of download fragment until download is complete
            }
        }

        LPDetailFragment lpDetailFragment = (LPDetailFragment) getSupportFragmentManager().findFragmentByTag(TAG_LP_DETAIL_FRAG);
        if (lpDetailFragment != null) {
            if (lpDetailFragment.isBusyUploadingLp()) {
                Snackbar.make(view, "Uploading LP, please wait", Snackbar.LENGTH_SHORT).show();
                return true; //prevents backing out of LPDetail fragment until download is complete
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();
        mHandler = new Handler();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                Log.d(TAG, "onReceive: called with action: " + intent.getAction());
                setToolbarSubTitle();
                switch (action) {

                    case CONNECTION_LOST:
                        //close out activity and start BluetoothStartup activity
                        mToolbar.setSubtitle(R.string.connection_lost);
                        Snackbar snackbar = Snackbar.make(view, "Bluetooth Connection Lost", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("RECONNECT", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Intent intent = new Intent(HomeActivity.this, BluetoothStartUp.class);
                                Log.d(TAG, "onClick: finishing HomeActivity");

                                finish();
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startActivity(intent);
                                        Log.d(TAG, "run: starting Bluetoothstartup activity with .25 sec delay");
                                    }
                                }, 500);


                            }
                        }).show();
                        break;

                    case UPDATE_BT_STATE:
                        setToolbarSubTitle();
                        break;
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTION_LOST);
        intentFilter.addAction(UPDATE_BT_STATE);
        LocalBroadcastManager.getInstance(HomeActivity.this).registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: called");
        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(mReceiver);
        super.onStop();
    }
}