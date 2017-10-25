package com.sunelectronics.sunbluetoothapp.activities.localprogram;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.database.LPDataBaseHandler;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

public class LPCreateEditFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "LPCreateEditFragment";
    private LPDataBaseHandler mLPDataBaseHandler;
    private ImageButton saveButton, cancelButton, lpClearButton, loadSampleButton;
    private EditText lpNameEditText, lpContentEditText;
    private boolean isEdit;
    private LocalProgram localProgram;

    public LPCreateEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //to hide menu items from fragment set HasOptions menu to true and in onPrepareOptionsMenu hide item
        setHasOptionsMenu(true);

        ((LocalProgramActivity) getActivity()).setShowConfirmDialog(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");

        View view = inflater.inflate(R.layout.fragment_lp_create, container, false);

        lpNameEditText = (EditText) view.findViewById(R.id.lpName);
        lpContentEditText = (EditText) view.findViewById(R.id.lpContent);

        String actionBarTitle = getString(R.string.create_lp);


        if (getArguments() != null) {

            //then this is an edit lp called from LPDetailFragment otherwise create a new LP
            actionBarTitle = getString(R.string.edit_lp);
            Bundle args = getArguments();
            localProgram = (LocalProgram) args.getSerializable("lp");
            lpNameEditText.setText(localProgram.getName());
            lpContentEditText.setText(localProgram.getContent());
            isEdit = true; //lp is being edited not creating a new one
        }

        //set title of action bar to create lp or edit lp depending on getarguments null or not
        LocalProgramActivity localProgramActivity = (LocalProgramActivity) getActivity();
        localProgramActivity.getSupportActionBar().setTitle(actionBarTitle);

        lpClearButton = (ImageButton) view.findViewById(R.id.clearLPButton);
        lpClearButton.setOnClickListener(this);

        loadSampleButton = (ImageButton) view.findViewById(R.id.loadSampleButton);
        loadSampleButton.setOnClickListener(this);

        saveButton = (ImageButton) view.findViewById(R.id.saveLPButton);
        saveButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.saveLPButton:

                ((LocalProgramActivity) getActivity()).setShowConfirmDialog(false);
                if (isEdit) {

                    editLP();

                } else {

                    addLPtoDB();
                }
                break;

            case R.id.loadSampleButton:
                lpNameEditText.setText(Constants.SAMPLE_LP_NAME);
                lpContentEditText.setText(Constants.SAMPLE_LP);
                break;

            case R.id.clearLPButton:
                lpNameEditText.setText("");
                lpContentEditText.setText("");
                break;
        }
    }

    private void editLP() {

        if (!validateInput()) {
            return;
        }
        //if valid input create lp and update record in db
        Log.d(TAG, "editLP: editing LP");
        String lpName = lpNameEditText.getText().toString();
        String lpContent = lpContentEditText.getText().toString();

        LocalProgram lp = new LocalProgram(lpName, lpContent);
        lp.setId(localProgram.getId());
        mLPDataBaseHandler.upDateExistingLP(lp);
        //return back to lp list
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();// go back to LPListFragment
        }
    }

    /**
     * adds local program to database
     */
    private void addLPtoDB() {

        if (!validateInput()) {
            return;
        }
        //if valid input create lp and store to database
        String lpName = lpNameEditText.getText().toString();
        String lpContent = lpContentEditText.getText().toString();

        LocalProgram lp = new LocalProgram(lpName, lpContent);
        mLPDataBaseHandler.addLocalProgramToDB(lp);
        //return back to lp list
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();// go back to LPListFragment
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(lpNameEditText.getText().toString())) {
            Toast.makeText(getContext(), "Enter a local program name", Toast.LENGTH_SHORT).show();
            lpNameEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(lpContentEditText.getText().toString())) {
            Toast.makeText(getContext(), "Enter a local program", Toast.LENGTH_SHORT).show();
            lpContentEditText.requestFocus();
            return false;
        }
        return true;
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
        mLPDataBaseHandler = new LPDataBaseHandler(context);
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
