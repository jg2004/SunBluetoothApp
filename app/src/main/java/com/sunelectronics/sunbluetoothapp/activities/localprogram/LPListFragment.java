package com.sunelectronics.sunbluetoothapp.activities.localprogram;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHandler;
import com.sunelectronics.sunbluetoothapp.fragments.LPCreateEditFragment;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;
import com.sunelectronics.sunbluetoothapp.ui.LPRecyclerViewAdapter;

import java.util.List;

public class LPListFragment extends Fragment {
    private static final String TAG = "LPListFragment";
    private RecyclerView mLpRecyclerView;
    private LPRecyclerViewAdapter mLPRecyclerViewAdapter;
    private List<LocalProgram> lpList;
    private LPDataBaseHandler mLPDataBaseHandler;

    public LPListFragment() {
    }
    // TODO: 11/12/2017 delete this fragment as it is only used in LocalProgramActivity (delete that too)

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((LocalProgramActivity) getActivity()).setShowConfirmDialog(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");

        View view = inflater.inflate(R.layout.fragment_lp_list, container, false);

        lpList = mLPDataBaseHandler.getLocalPrograms();
        mLPRecyclerViewAdapter = new LPRecyclerViewAdapter(getContext(), lpList);
        setUpRecyclerView(view);

        ((LocalProgramActivity) getActivity()).getSupportActionBar().setTitle("Local Programs (" + lpList.size() + ")");

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentManager fragmentManager = getFragmentManager();
                //FragmentTransaction ft = fragmentManager.beginTransaction().replace(R.id.localProgramContainer, new LPCreateEditFragment());
                FragmentTransaction ft = fragmentManager.beginTransaction();
                //ft.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                ft.replace(R.id.localProgramContainer, new LPCreateEditFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return view;
    }

    private void setUpRecyclerView(View view) {

        mLpRecyclerView = (RecyclerView) view.findViewById(R.id.lpRecyclerView);
        mLpRecyclerView.setHasFixedSize(true);
        mLpRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mLpRecyclerView.setAdapter(mLPRecyclerViewAdapter);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (lpList.size() > 0) {

            menu.findItem(R.id.action_loadSampleLP).setVisible(false);
            menu.findItem(R.id.action_deleteAllLocalPrograms).setVisible(true);

        } else {
            menu.findItem(R.id.action_deleteAllLocalPrograms).setVisible(false);
        }
    }

    public LPRecyclerViewAdapter getLPRecyclerViewAdapter() {
        return mLPRecyclerViewAdapter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: called, context: " + context.getClass().getName());
        mLPDataBaseHandler = LPDataBaseHandler.getInstance(getContext());
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
