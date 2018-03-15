package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.database.Tc01ProfDataBaseHelper;
import com.sunelectronics.sunbluetoothapp.models.Tc01Profile;
import com.sunelectronics.sunbluetoothapp.ui.ProfileRecyclerViewAdapter;

import java.util.List;

import static com.sunelectronics.sunbluetoothapp.R.id.lpRecyclerView;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_ALL_PROFILES;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PROFILE_LIST_FRAG_TITLE;

public class ProfileListFragment extends Fragment {
    private static final String TAG = "ProfileListFragment";
    private ProfileRecyclerViewAdapter mProfileRecyclerViewAdapter;
    private List<Tc01Profile> mTc01ProfileList;
    private Tc01ProfDataBaseHelper mTc01ProfDataBaseHelper;

    public ProfileListFragment() {
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

        View view = inflater.inflate(R.layout.fragment_profile_list, container, false);

        mTc01ProfileList = mTc01ProfDataBaseHelper.getProfiles();
        mProfileRecyclerViewAdapter = new ProfileRecyclerViewAdapter(getContext(), mTc01ProfileList);
        setUpRecyclerView(view);

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(PROFILE_LIST_FRAG_TITLE);
            supportActionBar.show();
        }

//        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                FragmentManager fragmentManager = getFragmentManager();
//                FragmentTransaction ft = fragmentManager.beginTransaction();
//                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
//                ft.replace(R.id.homeContainer, new LPCreateEditFragment());
//                ft.addToBackStack(null);
//                ft.commit();
//            }
//        });

        return view;
    }

    private void setUpRecyclerView(View view) {

        RecyclerView recyclerView = (RecyclerView) view.findViewById(lpRecyclerView);
        mTc01ProfDataBaseHelper.setRecyclerView(recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mProfileRecyclerViewAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(TAG, "onCreateOptionsMenu: called");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tc01_profile, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called");
        menu.findItem(R.id.action_showSavedProfiles).setVisible(false);
        menu.findItem(R.id.action_clear_fields).setVisible(false);
        if (mTc01ProfileList.size() > 0) {

            menu.findItem(R.id.action_loadSampleProfile).setVisible(false);
            menu.findItem(R.id.action_deleteAllProfiles).setVisible(true);

        } else {
            menu.findItem(R.id.action_deleteAllProfiles).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_deleteAllProfiles:

                Bundle args = new Bundle();
                args.putString(ALERT_TITLE, DELETE_MESSAGE);
                args.putString(ALERT_MESSAGE, "Ok to delete all profiles from this device?");
                args.putString(ALERT_TYPE, DELETE_ALL_PROFILES);
                args.putInt(ALERT_ICON, R.drawable.ic_delete_black_48dp);
                showDialog(args);
                return true;

            case R.id.action_loadSampleProfile:

                Tc01Profile profile = new Tc01Profile("Sample Profile");
                profile.addSegment(0, "-50", "60");
                profile.addSegment(1, "150", "60");
                profile.addSegment(2, "25", "5");
                for (int i = 3; i < 10; i++) profile.addSegment(i, "", "");
                profile.setCycles("10");
                mTc01ProfDataBaseHelper.addProfileToDataBase(profile);
                mTc01ProfileList = mTc01ProfDataBaseHelper.getProfiles();
                mProfileRecyclerViewAdapter.setProfileList(mTc01ProfileList);
                mProfileRecyclerViewAdapter.notifyDataSetChanged();
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
        SQLiteOpenHelper helper = ((HomeActivity) context).getDataBaseHelper();
        mTc01ProfDataBaseHelper = (Tc01ProfDataBaseHelper) helper;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTc01ProfDataBaseHelper.close();
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
