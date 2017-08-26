package com.sunelectronics.sunbluetoothapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHandler;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_lp_list, container, false);

        mLpRecyclerView = (RecyclerView) view.findViewById(R.id.lpRecyclerView);
        mLpRecyclerView.setHasFixedSize(true);
        mLpRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lpList = mLPDataBaseHandler.getLocalPrograms();

        mLPRecyclerViewAdapter = new LPRecyclerViewAdapter(getContext(),lpList);


        LocalProgramActivity localProgramActivity = (LocalProgramActivity)getActivity();
        localProgramActivity.getSupportActionBar().setTitle("Local Programs (" + lpList.size() + ")");
        mLpRecyclerView.setAdapter(mLPRecyclerViewAdapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment lpCreateFragment = new LPCreateEditFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction().replace(R.id.localProgramContainer, lpCreateFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return view;
    }


    public LPRecyclerViewAdapter getLPRecyclerViewAdapter() {
        return mLPRecyclerViewAdapter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: called, context: " + context.getClass().getName());
        mLPDataBaseHandler = new LPDataBaseHandler(context);

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
