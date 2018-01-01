package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;

import java.io.File;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.EMPTY_LOG_FILE_CONTENTS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILES_DIRECTORY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_CONTENTS;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_VIEWER_TITLE;


public class LogFileViewerFragment extends Fragment {

    private static final String TAG = "LogFileViewerFragment";
    private String mLogFileContents;
    private String mFileName;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_log_file_viewer, container, false);
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.show();
            supportActionBar.setTitle(LOG_FILE_VIEWER_TITLE);
        }

        setHasOptionsMenu(true);
        TextView logFileTextView = (TextView) view.findViewById(R.id.textViewLogFile);
        if (mLogFileContents.isEmpty()) {

            logFileTextView.setText(EMPTY_LOG_FILE_CONTENTS);
        } else {

            logFileTextView.setText(mLogFileContents);
        }
        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            mLogFileContents = args.getString(LOG_FILE_CONTENTS);
            mFileName = args.getString(LOG_FILE_NAME);
        } else {
            mLogFileContents = getString(R.string.log_file_null_message);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_logger, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_share_log:

                String subject = "Temperature Log File";
                File file = new File(getContext().getFilesDir() + File.separator + LOG_FILES_DIRECTORY + File.separator + mFileName);
                //Uri filePath = Uri.fromFile(file);
                // TODO: 11/23/2017 send attachment not working
                //Uri uri = Uri.parse("file://" + file.getAbsolutePath());
                //Log.d(TAG, "onOptionsItemSelected:  Uri: " + uri.toString());
                shareLog(subject, mLogFileContents);
                return true;
        }
        return false;
    }

    private void shareLog(String subject, String message) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        // sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("text/plain");
        //sendIntent.setType("message/rfc822");

        if (sendIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(sendIntent, "Share temperature logging file..."));
        }

    }
}
