package com.sunelectronics.sunbluetoothapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;

import static android.content.ContentValues.TAG;
import static com.sunelectronics.sunbluetoothapp.R.id.nextButton;
import static com.sunelectronics.sunbluetoothapp.R.id.prevButton;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.FILE_CONTENTS;


public class HelpDialogFragment extends DialogFragment implements View.OnClickListener {

    private String mFileContents;
    private String[] mSections;
    private int mSectionsIndex;
    private TextView mFileContentsTextView;
    private Button mPrevButton, mNextButton;

    public static HelpDialogFragment newInstance(String fileContents) {

        Bundle args = new Bundle();
        args.putString(FILE_CONTENTS, fileContents);
        HelpDialogFragment f = new HelpDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
        //setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Dialog);


        mFileContents = getArguments().getString(FILE_CONTENTS, "FILE CONTENTS MISSING");
        mSections = mFileContents.split("-----");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_help_dialog, container, false);
        mFileContentsTextView = (TextView) view.findViewById(R.id.tvFileContents);
        mFileContentsTextView.setText(mSections[mSectionsIndex]);
        mPrevButton = (Button) view.findViewById(R.id.prevButton);
        mPrevButton.setOnClickListener(this);
        mPrevButton.setEnabled(false);
        mNextButton = (Button) view.findViewById(nextButton);
        mNextButton.setOnClickListener(this);
        ImageView exit = (ImageView) view.findViewById(R.id.exitImageView);
        exit.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case prevButton:

                if (mSectionsIndex > 0) {
                    mNextButton.setEnabled(true);
                    mNextButton.setBackgroundResource(R.drawable.ic_action_next_enabled);
                    mSectionsIndex--;
                    mFileContentsTextView.setText(mSections[mSectionsIndex]);

                    if (mSectionsIndex == 0) {
                        mPrevButton.setEnabled(false);
                        mPrevButton.setBackgroundResource(R.drawable.ic_action_prev);
                    }
                }
                break;

            case nextButton:

                if (mSectionsIndex < mSections.length - 1) {
                    mPrevButton.setEnabled(true);
                    mPrevButton.setBackgroundResource(R.drawable.ic_action_prev_enabled);
                    mSectionsIndex++;
                    mFileContentsTextView.setText(mSections[mSectionsIndex]);

                    if (mSectionsIndex == mSections.length - 1) {
                        mNextButton.setEnabled(false);
                        mNextButton.setBackgroundResource(R.drawable.ic_action_next);
                    }
                }
                break;
            case R.id.exitImageView:
                dismiss();
                break;
        }
    }
}