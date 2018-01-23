package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.ui.LogFileContentRecyclerViewAdapter;
import com.sunelectronics.sunbluetoothapp.utilities.TemperatureLogReader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.CHART_DATA;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILES_DIRECTORY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_NAME;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILE_VIEWER_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_TEMP_CHART;

public class LogFileViewerFragment extends Fragment {

    private static final String TAG = "LogFileViewerFragment";
    private String mLogFileContents, mFileName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            mFileName = args.getString(LOG_FILE_NAME);

        } else {
            mLogFileContents = getString(R.string.log_file_null_message);
        }
    }

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
        TemperatureLogReader tempLogReader = new TemperatureLogReader(getContext());
        mLogFileContents = tempLogReader.getFileContents(mFileName);
        String[] logFileContentsArray = mLogFileContents.split("\n");
        List<String> logFileList = Arrays.asList(logFileContentsArray);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        LogFileContentRecyclerViewAdapter adapter = new LogFileContentRecyclerViewAdapter(getContext(), logFileList);
        recyclerView.setAdapter(adapter);
        return view;
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
            case R.id.action_chart:

                //only chart if more than 2 lines as first 2 lines are title and header
                if (mLogFileContents.split("\n").length > 2) {
                    goToChartFrag();
                } else {
                    Toast.makeText(getContext(), "No data to chart!", Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void goToChartFrag() {
        Fragment chartFrag = new TemperatureChartFragment();
        Bundle args = new Bundle();
        args.putString(CHART_DATA, mLogFileContents);
        chartFrag.setArguments(args);
        performTransaction(chartFrag, TAG_FRAGMENT_TEMP_CHART);
    }

    public void performTransaction(Fragment fragment, String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.homeContainer, fragment, tag).commit();
    }

    private void shareLog(String subject, String message) {

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_logging_file)));
    }
}
