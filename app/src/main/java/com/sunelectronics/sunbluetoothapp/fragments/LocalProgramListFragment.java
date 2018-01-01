package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHandler;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;
import com.sunelectronics.sunbluetoothapp.ui.LPRecyclerViewAdapter;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOCAL_PROGRAM_LIST_FRAG_TITLE;

public class LocalProgramListFragment extends Fragment {
    private static final String TAG = "LPListFragment";
    public static final String TAG_FRAG_LP_LIST = "LP_LIST_FRAG";
    private LPRecyclerViewAdapter mLPRecyclerViewAdapter;
    private List<LocalProgram> lpList;
    private LPDataBaseHandler mLPDataBaseHandler;

    public LocalProgramListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");

        View view = inflater.inflate(R.layout.fragment_lp_list, container, false);

        lpList = mLPDataBaseHandler.getLocalPrograms();
        mLPRecyclerViewAdapter = new LPRecyclerViewAdapter(getContext(), lpList);
        setUpRecyclerView(view);

        //to see # of lp's use: setTitle("Local Programs (" + lpList.size() + ")")
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(LOCAL_PROGRAM_LIST_FRAG_TITLE);
            supportActionBar.show();
        }

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                ft.replace(R.id.homeContainer, new LPCreateEditFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return view;
    }

    private void setUpRecyclerView(View view) {

        RecyclerView lpRecyclerView = (RecyclerView) view.findViewById(R.id.lpRecyclerView);
        mLPDataBaseHandler.setRecyclerView(lpRecyclerView);
        lpRecyclerView.setHasFixedSize(true);
        lpRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lpRecyclerView.setAdapter(mLPRecyclerViewAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(TAG, "onCreateOptionsMenu: called");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_local_program, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called");
        menu.findItem(R.id.action_deleteLocalProgram).setVisible(false);
        menu.findItem(R.id.action_share_LP).setVisible(false);
        menu.findItem(R.id.action_showSavedLPs).setVisible(false);
        if (lpList.size() > 0) {

            menu.findItem(R.id.action_loadSampleLP).setVisible(false);
            menu.findItem(R.id.action_deleteAllLocalPrograms).setVisible(true);

        } else {
            menu.findItem(R.id.action_deleteAllLocalPrograms).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_deleteAllLocalPrograms:

                Bundle args = new Bundle();
                args.putString(ALERT_TITLE, DELETE_MESSAGE);
                args.putString(ALERT_MESSAGE, "Ok to delete all local programs from this device?");
                args.putString(ALERT_TYPE, DELETE_ALL_LP);
                args.putInt(ALERT_ICON, R.drawable.ic_delete_black_48dp);
                showDialog(args);
                return true;

            case R.id.action_loadSampleLP:

                LocalProgram lp = new LocalProgram("sample local program", null);
                lp.setContent(Constants.SAMPLE_LP);
                mLPDataBaseHandler.addLocalProgramToDB(lp);
                lpList = mLPDataBaseHandler.getLocalPrograms();
                mLPRecyclerViewAdapter.setLocalProgramList(lpList);
                mLPRecyclerViewAdapter.notifyDataSetChanged();
                getActivity().invalidateOptionsMenu();
                //// TODO: 12/26/2017  refresh list
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialog(Bundle bundle) {

        MyAlertDialogFragment myAlertdialogFragment = MyAlertDialogFragment.newInstance(bundle);
        myAlertdialogFragment.show(getFragmentManager(), null);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: called, context: " + context.getClass().getName());
        mLPDataBaseHandler = LPDataBaseHandler.getInstance(context);
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
