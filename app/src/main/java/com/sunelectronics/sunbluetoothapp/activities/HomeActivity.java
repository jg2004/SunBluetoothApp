package com.sunelectronics.sunbluetoothapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteOpenHelper;
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
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHelper;
import com.sunelectronics.sunbluetoothapp.database.Tc01ProfDataBaseHelper;
import com.sunelectronics.sunbluetoothapp.fragments.DisplayTemperatureFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPCreateEditFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPDetailFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPDownloadFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LogFileListFragment;
import com.sunelectronics.sunbluetoothapp.fragments.MyAlertDialogFragment;
import com.sunelectronics.sunbluetoothapp.fragments.TC01DispTempFragment;
import com.sunelectronics.sunbluetoothapp.fragments.TC01ProfileDetailFragment;
import com.sunelectronics.sunbluetoothapp.interfaces.IBottomNavigationListener;
import com.sunelectronics.sunbluetoothapp.interfaces.IBusy;
import com.sunelectronics.sunbluetoothapp.interfaces.IChamberOffSwitch;
import com.sunelectronics.sunbluetoothapp.interfaces.ILogger;
import com.sunelectronics.sunbluetoothapp.interfaces.IStop;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;
import com.sunelectronics.sunbluetoothapp.utilities.PreferenceSetting;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService.STATE_CONNECTED;
import static com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService.STATE_CONNECTING;
import static com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService.STATE_NONE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CONNECTION_LOST;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EXIT_APP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_LOGGER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_MONITOR;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_PARAMETER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_TEMP_PROF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_LP_DETAIL_FRAG;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.UPDATE_BT_STATE;

public class HomeActivity extends AppCompatActivity implements LogFileListFragment.DeleteLogFileListener,
        IBottomNavigationListener, LPCreateEditFragment.RefreshFragment,
        LPDownloadFragment.DownloadPInterface, DisplayTemperatureFragment.DisplayTemperatureFragmentCallBacks,
        IStop {

    private static final String TAG = "HomeActivity";
    private List<Fragment> mFragmentList = new ArrayList<>();
    private View view;
    private SQLiteOpenHelper mDataBaseHelper;
    private BottomNavigationView mBottomNavigationView;
    private BroadcastReceiver mReceiver;
    private Toolbar mToolbar;
    private Handler mHandler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreate: called");
        mDataBaseHelper = getDBHelper();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        view = findViewById(R.id.activityLayout);
        setUpViews();
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: savedInstanceState was NULL");
            buildFragmentList(PreferenceSetting.getControllerType(getApplicationContext()));
            switchFragment(0, TAG_FRAGMENT_MONITOR);

        } else {
            //if not null, then don't recreate all fragments since they will be restored automatically
            Log.d(TAG, "onCreate: savedInstanceState was NOT NULL");
            rebuildFragmentList(PreferenceSetting.getControllerType(getApplicationContext()));
        }
    }

    public void hideBottomNavigationView() {

        mBottomNavigationView.setVisibility(INVISIBLE);
    }

    public void showBottomNavigationView() {
        mBottomNavigationView.setVisibility(View.VISIBLE);
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
        if (PreferenceSetting.getControllerType(HomeActivity.this).equals(TC01)) {
            //change title from Local Program to Profiles if TC01
            MenuItem profileMenuItem = mBottomNavigationView.getMenu().findItem(R.id.bottom_bar_local_program);
            profileMenuItem.setTitle(R.string.profile);

        }
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

                        switchFragment(1, TAG_FRAGMENT_TEMP_PROF);
                        return true;


                    case R.id.bottom_bar_communicate:
                        switchFragment(2, TAG_FRAGMENT_LOGGER);
                        return true;
                }
                return false;
            }
        });
    }

    public SQLiteOpenHelper getDataBaseHelper() {
        return mDataBaseHelper;
    }

    private void setToolbarSubTitle() {

        //set subtitle based on bluetooth connection state
        Log.d(TAG, "setToolbarSubTitle: state of bluetooth is: " + BluetoothConnectionService.getInstance().getCurrentState());
        switch (BluetoothConnectionService.getInstance().getCurrentState()) {
            case STATE_CONNECTED:
                mToolbar.setSubtitle("Connected: " +
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
    private void buildFragmentList(String controllerType) {

        if (controllerType.equals(TC01)) {

            mFragmentList.add(new TC01DispTempFragment());
            mFragmentList.add(new TC01ProfileDetailFragment());
            mFragmentList.add(new LogFileListFragment());

        } else {

            mFragmentList.add(new DisplayTemperatureFragment());
            mFragmentList.add(new LPDownloadFragment());
            mFragmentList.add(new LogFileListFragment());
        }

        Log.d(TAG, "buildFragmentList: number of fragments in list: " + mFragmentList.size());
    }

    /**
     * get fragments that have already been created and add to fragment list so that fragment state
     * is restored (instead of recreating 3 new fragments
     */
    private void rebuildFragmentList(String controllerType) {

        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MONITOR);
        if (fragment1 != null) {
            mFragmentList.add(fragment1);
            Log.d(TAG, "buildFragmentList: added fragment: " + fragment1.getTag());
        } else {
            if (controllerType.equals(TC01)) {
                mFragmentList.add(new TC01DispTempFragment());

            } else {
                mFragmentList.add(new DisplayTemperatureFragment());
            }
        }

        Fragment fragment2 = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_TEMP_PROF);
        if (fragment2 != null) {
            mFragmentList.add(fragment2);
            Log.d(TAG, "buildFragmentList: added fragment: " + fragment2.getTag());
        } else {
            if (controllerType.equals(TC01)) {
                mFragmentList.add(new TC01ProfileDetailFragment());

            } else {
                mFragmentList.add(new LPDownloadFragment());
            }
        }

        Fragment fragment3 = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_LOGGER);
        if (fragment3 != null) {
            mFragmentList.add(fragment3);
            Log.d(TAG, "buildFragmentList: added fragment: " + fragment3.getTag());
        } else {
            mFragmentList.add(new LogFileListFragment());
        }

        Log.d(TAG, "buildFragmentList: mFragmentListSize: " + mFragmentList.size());

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

    private SQLiteOpenHelper getDBHelper() {

        String controllerType = PreferenceSetting.getControllerType(HomeActivity.this);
        if (controllerType.equals(TC01)) {
            return new Tc01ProfDataBaseHelper(HomeActivity.this);

        } else {
            return new LPDataBaseHelper(HomeActivity.this);
        }
    }

    /*------------interface implemented methods--------------------------------------------------*/

    @Override
    public void stopLoggingSession() {
        //called by MyAlertDialogFragment if user presses Yes button on dialog
        Log.d(TAG, "stopLoggingSession: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager
                .findFragmentByTag(TAG_FRAGMENT_MONITOR);

        if (fragment instanceof ILogger) {
            ILogger logger = (ILogger) fragment;
            logger.stopLogger();
        }
    }

    @Override
    public void turnOffChamber() {
        //called by MyAlertDialogFragment if user presses Yes button on dialog
        Log.d(TAG, "turnOffChamber: called");
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager
                .findFragmentByTag(TAG_FRAGMENT_MONITOR);

        if (fragment instanceof IChamberOffSwitch) {
            IChamberOffSwitch chamberOffSwitch = (IChamberOffSwitch) fragment;
            chamberOffSwitch.turnOffChamberSwitch();
        }
    }

    @Override
    public void closeActivity() {
        //called by MyAlertDialogFragment if user presses Yes button on dialog
        Log.d(TAG, "closing HomeActivity");
        stopLoggingSession();
        finish();
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

        LPDownloadFragment lpDownloadFrag = (LPDownloadFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_TEMP_PROF);
        lpDownloadFrag.downloadLP(lpNumber);
    }

    @Override
    public void downloadLP() {
        LPDownloadFragment lpDownloadFrag = (LPDownloadFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_TEMP_PROF);
        lpDownloadFrag.downloadLP();
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
            Log.d(TAG, "setNavigationItem: to: " + position);
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
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called CLOSING DB");
        mDataBaseHelper.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");

        //make sure no lp's are being uploaded or parameters being downloades
        if (checkIfBusy()) {
            return;
        }

        Fragment tempMonitorFrag = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MONITOR);

        if (tempMonitorFrag == null && PreferenceSetting.getControllerType(HomeActivity.this).equals(TC01)) {
            tempMonitorFrag = new TC01DispTempFragment();
        } else if (tempMonitorFrag == null && !PreferenceSetting.getControllerType(HomeActivity.this).equals(TC01)) {
            tempMonitorFrag = new DisplayTemperatureFragment();
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && !tempMonitorFrag.isVisible()) {
            Log.d(TAG, "onBackPressed: backstack entry is 0 and tempMonitor is not visible");
            mBottomNavigationView.setSelectedItemId(R.id.bottom_bar_monitor);

        } else {
            //if it is visible, then cast to logger and check that it's logging
            ILogger logger = (ILogger) tempMonitorFrag;

            if (logger.isLoggingData()) {
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

        IBusy paramfrag = (IBusy) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_PARAMETER);
        if (paramfrag != null) {
            if (paramfrag.isBusy()) {
                Snackbar.make(view, R.string.download_parameters_message, Snackbar.LENGTH_SHORT).show();
                return true; //prevents backing out of parameter fragment until download is complete
            }
        }
        // TODO: 3/14/2018  get rid of

//        LPDownloadFragment lpDownloadFragment = (LPDownloadFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_TEMP_PROF);
//        if (lpDownloadFragment != null) {
//            if (lpDownloadFragment.isBusyUploadingLp()) {
//                Snackbar.make(view, R.string.lp_busy_uploading, Snackbar.LENGTH_SHORT).show();
//                return true; //prevents backing out of download fragment until download is complete
//            }
//        }

        IBusy fragment = (IBusy) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_TEMP_PROF);

        if (fragment != null) {
            if (fragment.isBusy()) {
                Snackbar.make(view, R.string.controller_busy, Snackbar.LENGTH_SHORT).show();
                return true; //prevents backing out of fragment thats busy uploading or downloading lp or tc01 profile
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
                        Snackbar snackbar = Snackbar.make(view, R.string.bluetooth_connection_lost, Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.reconnect, new View.OnClickListener() {
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
        Log.d(TAG, "onStop: called, unregister reciever that listens for connection lost");
        LocalBroadcastManager.getInstance(HomeActivity.this).unregisterReceiver(mReceiver);
        super.onStop();
    }

    @Override
    public void sendStop() {

        IStop fragment = (IStop) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_TEMP_PROF);

        if (fragment != null) {

            fragment.sendStop();

        }

    }


}