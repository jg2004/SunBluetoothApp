package com.sunelectronics.sunbluetoothapp.activities.localprogram;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;

public class LPDetailFragment extends Fragment  implements View.OnClickListener{

    private static final String TAG = "LPDetailFragment";
    private TextView lpDetailName, lpDetailContent;
    private ImageButton deleteButton, editButton, uploadLPButton;
    private LocalProgram localProgram;

    public LPDetailFragment() {//empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_lp_detail,container,false);

        deleteButton = (ImageButton) view.findViewById(R.id.deleteLPButton);
        editButton = (ImageButton) view.findViewById(R.id.editButton);
        uploadLPButton = (ImageButton) view.findViewById(R.id.upLoadButton);
        deleteButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        uploadLPButton.setOnClickListener(this);
        lpDetailName = (TextView)view.findViewById(R.id.detailLpName);
        lpDetailContent = (TextView) view.findViewById(R.id.detailLpContent);
        localProgram = (LocalProgram) getArguments().getSerializable("lp");
        lpDetailName.setText(localProgram.getName());
        lpDetailContent.setText(localProgram.getContent());

        ((LocalProgramActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.local_program )+ " id: " +localProgram.getId());

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.deleteLPButton:
                Log.d(TAG, "onClick: delete pressed");
                Bundle lpArgs = new Bundle();
                lpArgs.putSerializable("lp", localProgram);
                lpArgs.putString(LocalProgramActivity.ALERT_TITLE, "OK to Delete LP " + localProgram.getName() + " ?");
                lpArgs.putString(LocalProgramActivity.ALERT_TYPE, LocalProgramActivity.DELETE_LP);
                lpArgs.putInt(LocalProgramActivity.ALERT_ICON,R.drawable.ic_delete_black_48dp);
                ((LocalProgramActivity)getActivity()).showDialog(lpArgs);

                break;

            case R.id.upLoadButton:
                Toast.makeText(getContext(), "upload button pressed", Toast.LENGTH_LONG).show();
                break;

            case R.id.editButton:
                LPCreateEditFragment lpcreateEditFragment = new LPCreateEditFragment();
                Bundle args = new Bundle();
                args.putSerializable("lp", localProgram);
                lpcreateEditFragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.localProgramContainer, lpcreateEditFragment).commit();
                break;
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_deleteAllLocalPrograms).setVisible(false);
        menu.findItem(R.id.action_loadSampleLP).setVisible(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: called");

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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: called");
        super.onStop();

    }


}
