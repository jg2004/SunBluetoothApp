package com.sunelectronics.sunbluetoothapp.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.fragments.DisplayTemperatureFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPCreateEditFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPDetailFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LPDownloadFragment;
import com.sunelectronics.sunbluetoothapp.fragments.LogFileListFragment;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;

import java.util.ArrayList;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_LP_DETAIL_FRAG;

public class HomeActivity extends AppCompatActivity implements LogFileListFragment.DeleteLogFileListener, LPDownloadFragment.BottomNavigationListenter, LPCreateEditFragment.RefreshFragment, LPDownloadFragment.DownloadPInterface {

    private static final String TAG = "HomeActivity";
    private List<Fragment> mFragmentList = new ArrayList<>();
    public static final String TAG_FRAGMENT_MONITOR = "tag_frag_monitor";
    public static final String TAG_FRAGMENT_LOCAL_PROGRAM = "tag_frag_lp";
    public static final String TAG_FRAGMENT_COMMUNICATION = "tag_frag_comm";
    public static final String TAG_FRAGMENT_LOGGER = "tag_frag_logger";

    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        buildFragmentList();
        setUpViews();
        // switchFragment(0, TAG_FRAGMENT_MONITOR);
        //switchFragment(1, TAG_FRAGMENT_LOCAL_PROGRAM);
        switchFragment(2, TAG_FRAGMENT_LOGGER);

    }

    //-------------------------------private methods----------------------------------------------

    /**
     * initialize all widgets
     */
    private void setUpViews() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

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

    /**
     * add fragments to fragment array list
     */
    private void buildFragmentList() {

        mFragmentList.add(new DisplayTemperatureFragment());
        // TODO: 10/25/2017 create fragments below:
        mFragmentList.add(new LPDownloadFragment());
        mFragmentList.add(new LogFileListFragment());
    }

    /**
     * perform the fragment transaction
     *
     * @param position
     * @param tag
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

    /*------------interface implemented methods--------------------------------------------------*/
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
        BluetoothConnectionService.getInstance(HomeActivity.this).cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");

        DisplayTemperatureFragment tempMonitorFrag = (DisplayTemperatureFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MONITOR);
        if (tempMonitorFrag == null) {
            tempMonitorFrag = new DisplayTemperatureFragment();
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && !tempMonitorFrag.isVisible()) {
            Log.d(TAG, "onBackPressed: backstack entry is 0 and tempMonitor is not visible");
            mBottomNavigationView.setSelectedItemId(R.id.bottom_bar_monitor);

        } else {
            // TODO: 11/7/2017 show dialog confirming exit
            super.onBackPressed();
        }
    }


}