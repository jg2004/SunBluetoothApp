package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.models.LogFileObject;
import com.sunelectronics.sunbluetoothapp.ui.LogFileListAdaptor;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_LOG_FILES;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_LOG_FILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOGGING_STATE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_LIST_FRAG_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_LOF_FILE_VIEWER;

public class LogFileListFragment extends ListFragment {

    private static final String TAG = "LogFileListFragment";
    private List<LogFileObject> mLogFileObjects = new ArrayList<>();
    private TemperatureLogReader mTemperatureLogReader;
    private LogFileListAdaptor mLogFileListAdaptor;
    private ActionBar mSupportActionBar;
    private View view;
    private boolean mIsLogging;

    //implemented by HomeActivity and IntroActivity
    public interface DeleteLogFileListener {
        void deleteLogFile(String fileName);

        void deleteAllLogFiles();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log_file_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: called");
        super.onActivityCreated(savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);
        mIsLogging = prefs.getBoolean(LOGGING_STATE, false);
        view = getView();
        mTemperatureLogReader = new TemperatureLogReader(getContext());
        mLogFileObjects = mTemperatureLogReader.getLogFileObjects();
        Collections.sort(mLogFileObjects);

        mLogFileListAdaptor = new LogFileListAdaptor(getContext(), R.layout.log_file_row, mLogFileObjects);

        setListAdapter(mLogFileListAdaptor);
        setUpLongClickListener();
        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.show();
            mSupportActionBar.setTitle(LOG_FILE_LIST_FRAG_TITLE);
        }
        setHasOptionsMenu(true);
    }

    private void setUpLongClickListener() {

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == mLogFileObjects.size() - 1 && mIsLogging) {
                    Toast.makeText(getContext(), "Logging in progress, can't delete", Toast.LENGTH_SHORT).show();
                    return true;
                }
                showAlertDialog(mLogFileObjects.get(position).getFileName());
                return true;
            }
        });
    }

    /**
     * called from HomeActivity, which is called by MyAlertDialogFragment
     *
     * @param fileName filename to be deleted
     */
    public void deleteLogFile(String fileName) {
        if (mTemperatureLogReader.deleteFile(fileName)) {
            Snackbar.make(view, "File deleted!", Snackbar.LENGTH_SHORT).show();

            mLogFileListAdaptor.remove(fileName);

        } else {
            Snackbar.make(view, "File could not be deleted", Snackbar.LENGTH_SHORT).show();
        }
        getActivity().invalidateOptionsMenu();
    }

    public void deleteAllLogFiles() {

        if (mTemperatureLogReader.deleteAllFilesInLogFilesDirectory()) {
            mLogFileListAdaptor.clear();

        } else {

            Snackbar.make(view, "Some files could not be deleted", Snackbar.LENGTH_SHORT).show();
        }
        getActivity().invalidateOptionsMenu();

    }

    /**
     * alert dialog to delete one log file
     *
     * @param fileName filename to delete
     */
    private void showAlertDialog(String fileName) {
        Bundle args = new Bundle();
        args.putString(ALERT_TITLE, DELETE_MESSAGE);
        args.putString(ALERT_MESSAGE, "Delete " + fileName + "?");
        args.putInt(ALERT_ICON, R.drawable.ic_delete_black_48dp);
        args.putString(ALERT_TYPE, DELETE_LOG_FILE);
        args.putString(LOG_FILE_NAME, fileName);
        MyAlertDialogFragment alertDialog = MyAlertDialogFragment.newInstance(args);
        alertDialog.show(getFragmentManager(), null);
    }

    /**
     * alert dialog to delete all log files
     */
    private void showAlertDialog() {
        Bundle args = new Bundle();
        args.putString(ALERT_TITLE, DELETE_MESSAGE);
        args.putString(ALERT_MESSAGE, "Delete all logging files?");
        args.putInt(ALERT_ICON, R.drawable.ic_delete_black_48dp);
        args.putString(ALERT_TYPE, DELETE_ALL_LOG_FILES);
        MyAlertDialogFragment alertDialog = MyAlertDialogFragment.newInstance(args);
        alertDialog.show(getFragmentManager(), null);
    }

    @Override

    public void onListItemClick(ListView l, View v, int position, long id) {

        Log.d(TAG, "onListItemClick: called");
        goToLogFileViewer(position);
    }

    private void goToLogFileViewer(int position) {
        LogFileViewerFragment fragment = new LogFileViewerFragment();
        Bundle args = new Bundle();
        args.putString(LOG_FILE_NAME, mLogFileObjects.get(position).getFileName());
        fragment.setArguments(args);
        performTransaction(fragment, TAG_FRAGMENT_LOF_FILE_VIEWER);

    }

    public void performTransaction(Fragment fragment, String tag) {

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.homeContainer, fragment, tag).commit();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Log.d(TAG, "onPrepareOptionsMenu: called");
        MenuItem deleteAllLogFileMenuItem = menu.findItem(R.id.action_deleteAllLogFiles);

        if (mIsLogging) {
            deleteAllLogFileMenuItem.setEnabled(false);

        } else {
            deleteAllLogFileMenuItem.setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_deleteAllLogFiles:
                showAlertDialog();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_log_file_list_view, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called");
        mSupportActionBar.hide();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mSupportActionBar.show();
    }
}