package com.sunelectronics.sunbluetoothapp.activities.localprogram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHandler;
import com.sunelectronics.sunbluetoothapp.fragments.MyAlertDialogFragment;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_CONFIRM_EXIT;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_LP;

public class LocalProgramActivity extends AppCompatActivity implements MyAlertDialogFragment.OnPositiveDialogClick {


    private boolean showConfirmDialog;
    private LPDataBaseHandler mLPDataBaseHandler;
    private static final String TAG = "LocalProgramActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_program);
        mLPDataBaseHandler = LPDataBaseHandler.getInstance(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().beginTransaction().replace(R.id.localProgramContainer, new LPListFragment()).commit();
    }

    //setters

    public void setShowConfirmDialog(boolean showConfirmDialog) {
        this.showConfirmDialog = showConfirmDialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_local_program, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_deleteAllLocalPrograms:

                Bundle args = new Bundle();
                args.putString(ALERT_TITLE, "OK to Delete All Local Programs?");
                args.putString(ALERT_TYPE, DELETE_ALL_LP);
                args.putInt(ALERT_ICON, R.drawable.ic_delete_black_48dp);
                showDialog(args);
                break;

            case R.id.action_loadSampleLP:

                LocalProgram lp = new LocalProgram("sample local program", null);
                lp.setContent(Constants.SAMPLE_LP);
                mLPDataBaseHandler.addLocalProgramToDB(lp);
                getSupportFragmentManager().beginTransaction().replace(R.id.localProgramContainer, new LPListFragment()).commit();
        }

        return true;
    }

    public void showDialog(Bundle bundle) {

        MyAlertDialogFragment myAlertdialogFragment = MyAlertDialogFragment.newInstance(bundle);
        myAlertdialogFragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onBackPressed() {

        Log.d(TAG, "onBackPressed: called");

        if (showConfirmDialog) {

            Bundle args = new Bundle();
            args.putString(ALERT_TITLE, "Data not saved, OK to exit?");
            args.putString(ALERT_TYPE, ALERT_CONFIRM_EXIT);
            args.putInt(ALERT_ICON, R.drawable.ic_warning_black_48dp);
            showDialog(args);
            return;
        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            Log.d(TAG, "onBackPressed: entry count >0");
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    /* this is method from DialogFragment interface */
    public void onPositiveClick(Bundle args) {

        String type = args.getString(ALERT_TYPE);
        if (type == null) {
            type = "Type Not Found"; //this just prevents switch (null) from causing app to crash
        }

        switch (type) {

            case DELETE_LP:

                LocalProgram lp = (LocalProgram) args.getSerializable("lp");
                if (lp != null) {
                    mLPDataBaseHandler.deleteLPFromDB(lp.getId());
                } else {
                    Toast.makeText(this, "Local Program was null", Toast.LENGTH_SHORT).show();
                }
                break;

            case DELETE_ALL_LP:

                mLPDataBaseHandler.deleteAllLocalPrograms();
                break;

            case ALERT_CONFIRM_EXIT:

                showConfirmDialog = false;
                onBackPressed();
                break;

            default:
                Toast.makeText(this, "dialog type not found", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
        mLPDataBaseHandler.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: called");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called");
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
    }

}