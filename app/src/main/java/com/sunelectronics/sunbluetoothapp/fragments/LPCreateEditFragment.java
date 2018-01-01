package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHandler;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LP;

public class LPCreateEditFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LPCreateEditFragment";
    private LPDataBaseHandler mLPDataBaseHandler;
    private EditText mLpName, mLpContent;
    private boolean isEdit;
    private LocalProgram localProgram;
    private RefreshFragment mRefreshFragment;
    private View view;

    public interface RefreshFragment {

        //this interface is implemented by homeactivity to allow sending local program
        //from this fragment to the LPDetailFragment
        void refreshLPDetailFragment(LocalProgram localProgram);
    }

    public LPCreateEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //to hide menu items from fragment set HasOptions menu to true and in onPrepareOptionsMenu hide item
        setHasOptionsMenu(true);

        // ((LocalProgramActivity) getActivity()).setShowConfirmDialog(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");

        view = inflater.inflate(R.layout.fragment_lp_create, container, false);

        mLpName = (EditText) view.findViewById(R.id.lpName);
        mLpContent = (EditText) view.findViewById(R.id.lpContent);

        //set title of action bar to create lp or edit lp depending on getarguments null or not
        String actionBarTitle = getResources().getString(R.string.create_lp);
        if (getArguments() != null) {

            //then this is an edit lp called from LPDetailFragment otherwise create a new LP
            actionBarTitle = getString(R.string.edit_lp);
            Bundle args = getArguments();
            localProgram = (LocalProgram) args.getSerializable(LP);
            if (localProgram != null) {
                mLpName.setText(localProgram.getName());
                mLpContent.setText(localProgram.getContent());
            }
            isEdit = true; //lp is being edited not creating a new one
        }

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(actionBarTitle);
            supportActionBar.show();
        }

        ImageButton lpClearButton = (ImageButton) view.findViewById(R.id.clearLPButton);
        lpClearButton.setOnClickListener(this);

        ImageButton loadSampleButton = (ImageButton) view.findViewById(R.id.loadSampleButton);
        loadSampleButton.setOnClickListener(this);

        ImageButton saveButton = (ImageButton) view.findViewById(R.id.saveLPButton);
        saveButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.saveLPButton:

                // TODO: 10/25/2017 what does the following statement do???
                //((LocalProgramActivity) getActivity()).setShowConfirmDialog(false);
                if (isEdit) {

                    editLP();

                } else {

                    addLPtoDB();
                }
                break;

            case R.id.loadSampleButton:
                mLpName.setText(Constants.SAMPLE_LP_NAME);
                mLpContent.setText(Constants.SAMPLE_LP);
                break;

            case R.id.clearLPButton:
                mLpName.setText("");
                mLpContent.setText("");
                break;
        }
    }

    private void editLP() {

        if (!validateInput()) {
            return;
        }
        //if valid input create lp and update record in db
        Log.d(TAG, "editLP: editing LP");
        String lpName = mLpName.getText().toString();
        String lpContent = mLpContent.getText().toString();

        LocalProgram lp = new LocalProgram(lpName, lpContent);
        lp.setId(localProgram.getId());
        mLPDataBaseHandler.upDateExistingLP(lp);

        //update the LPDetail fragment with the edited LP
        mRefreshFragment.refreshLPDetailFragment(lp);

        //go to previous fragment
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
    }

    /**
     * adds local program to database
     */
    private void addLPtoDB() {

        Log.d(TAG, "addLPtoDB: called");
        if (!validateInput()) {
            return;
        }
        //if valid input create lp and store to database
        String lpName = mLpName.getText().toString();
        String lpContent = mLpContent.getText().toString();

        LocalProgram lp = new LocalProgram(lpName, lpContent);
        mLPDataBaseHandler.addLocalProgramToDB(lp);
        //return to previous fragment
        Log.d(TAG, "addLPtoDB: popBackStack called");
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();// go back to LPListFragment
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(mLpName.getText().toString())) {
            Snackbar.make(view, "Enter a local program name", Snackbar.LENGTH_SHORT).show();
            mLpName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(mLpContent.getText().toString())) {
            Snackbar.make(view, "Enter a local program", Snackbar.LENGTH_SHORT).show();
            mLpContent.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: called");
        mLPDataBaseHandler = LPDataBaseHandler.getInstance(getContext());
        //RefreshFragment is an interface implemented by HomeActivity that is used to send
        //bundle local program to LPDetailFragment after local program is edited
        mRefreshFragment = (RefreshFragment) getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: called");
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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called");
    }
}