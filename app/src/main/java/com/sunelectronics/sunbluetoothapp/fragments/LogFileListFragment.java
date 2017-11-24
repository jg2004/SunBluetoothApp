package com.sunelectronics.sunbluetoothapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogReader;

import java.util.ArrayList;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_LOG_FILES;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_LOG_FILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_CONTENTS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_NAME;


public class LogFileListFragment extends ListFragment {

    private static final String TAG = "LogFileListFragment";
    private static final String EMPTY_LISTVIEW_MESSAGE = "No Temperature Logging Files";
    private List<String> mLogFileList = new ArrayList<>();
    private TemperatureLogReader mTemperatureLogReader;
    private ArrayAdapter mAdapter;

    public interface DeleteLogFileListener {
        void deleteLogFile(String fileName);

        void deleteAllLogFiles();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: called");
        super.onActivityCreated(savedInstanceState);

        mTemperatureLogReader = new TemperatureLogReader(getContext());
        mLogFileList = mTemperatureLogReader.getLogFiles();

        if (mLogFileList.isEmpty()) {

            mLogFileList.add(EMPTY_LISTVIEW_MESSAGE);
        }

        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mLogFileList);
        setListAdapter(mAdapter);
        setUpLongClickListener();
        setHasOptionsMenu(true);
    }

    private void setUpLongClickListener() {

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (mLogFileList.get(position).contains(EMPTY_LISTVIEW_MESSAGE)) {
                    return true;
                }

                showAlertDialog(mLogFileList.get(position));
                return true;
            }
        });
    }


    /**
     * called from HomeActivity, which is called by MyAlertDialogFragment
     *
     * @param fileName
     */
    public void deleteLogFile(String fileName) {
        if (mTemperatureLogReader.deleteFile(fileName)) {
            Snackbar.make(getView(), "File deleted!", Snackbar.LENGTH_SHORT).show();
            mAdapter.remove(fileName);

            if (mAdapter.getCount() == 0) {
                mAdapter.add(EMPTY_LISTVIEW_MESSAGE);
            }
        } else {

            Snackbar.make(getView(), "Filel could not be deleted", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void deleteAllLogFiles() {

        if (mTemperatureLogReader.deleteAllFilesInLogFilesDirectory()) {
            mAdapter.clear();
            mAdapter.add(EMPTY_LISTVIEW_MESSAGE);
        } else {

            Snackbar.make(getView(), "Some files could not be deleted", Snackbar.LENGTH_SHORT).show();

        }

    }

    /**
     * alert dialog to delete one log file
     *
     * @param fileName
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
        if (mLogFileList.get(position).equals(EMPTY_LISTVIEW_MESSAGE)) {
            return;
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        LogFileViewerFragment fragment = new LogFileViewerFragment();
        Bundle args = new Bundle();
        String fileContents = mTemperatureLogReader.getFileContents(mLogFileList.get(position));
        args.putString(LOG_FILE_CONTENTS, fileContents);
        args.putString(LOG_FILE_NAME, mLogFileList.get(position));
        fragment.setArguments(args);
        ft.addToBackStack(null);
        ft.replace(R.id.homeContainer, fragment).commit();
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
}
