package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LTL_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LTL_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PARAMETER_FRAG_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDC_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDC_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDH_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PIDH_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PWMP_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PWMP_QUERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.UTL_COMMAND;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.UTL_QUERY;

public class ParameterFragment extends Fragment {
    private static final String TAG = "ParameterFragment";
    public static final int DELAY = 1000;
    private EditText mPwmp, mUtl, mLtl, mPH, mIH, mDH, mPC, mIC, mDC;
    private LinearLayout mPidLayout, mPwmpLayout, mTempLimitLayout, mButtonLayout, mProgressBarLayout;
    private TextView mProgressBarTextView;
    private Handler mHandler = new Handler();
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private View view;
    private boolean isBusyDownLoading;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(R.layout.fragment_parameters, container, false);
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(PARAMETER_FRAG_TITLE);
            supportActionBar.show();
        }
        initializeViews(view);
        setHasOptionsMenu(true);
        if (BluetoothConnectionService.getInstance().getCurrentState()== BluetoothConnectionService.STATE_CONNECTED){
            //only download parameters if connected
            getControllerParameters();
        }
        return view;
    }

    private void initializeViews(final View view) {
        mProgressBarLayout = (LinearLayout) view.findViewById(R.id.progressBarLayout);
        mProgressBarTextView = (TextView) view.findViewById(R.id.textViewProgressBar);
        mPidLayout = (LinearLayout) view.findViewById(R.id.pidMainLayout);
        mPwmpLayout = (LinearLayout) view.findViewById(R.id.pwmpLayout);
        mTempLimitLayout = (LinearLayout) view.findViewById(R.id.UtlLtlLayout);
        mButtonLayout = (LinearLayout) view.findViewById(R.id.buttonLayout);
        mPwmp = (EditText) view.findViewById(R.id.pwmpEditText);
        mUtl = (EditText) view.findViewById(R.id.utlEditText);
        mLtl = (EditText) view.findViewById(R.id.ltlEditText);
        mPH = (EditText) view.findViewById(R.id.pHeatEditText);
        mIH = (EditText) view.findViewById(R.id.iHeatEditText);
        mDH = (EditText) view.findViewById(R.id.dHeatEditText);
        mPC = (EditText) view.findViewById(R.id.pCoolEditText);
        mIC = (EditText) view.findViewById(R.id.iCoolEditText);
        mDC = (EditText) view.findViewById(R.id.dCoolHeatEditText);
        Button getButton = (Button) view.findViewById(R.id.buttonGet);
        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(v, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "GET clicked, getting parameters");
                getControllerParameters();
            }
        });
        Button setButton = (Button) view.findViewById(R.id.buttonSet);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (BluetoothConnectionService.getInstance().getCurrentState() != BluetoothConnectionService.STATE_CONNECTED) {
                    Snackbar.make(view, R.string.bluetooth_not_connected, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "SET clicked, sending commands");
                setParameters();
            }
        });
    }
    public boolean isBusyDownLoading() {
        return isBusyDownLoading;
    }

    private void setParameters() {
        mProgressBarLayout.setVisibility(View.VISIBLE);
        mProgressBarTextView.setText(R.string.set_param_message);
        makeLayoutsInvisible();

        if (!isInputValid()) {
            mProgressBarLayout.setVisibility(View.INVISIBLE);
            makeLayoutsVisible();
            return;
        }


        if (!mPwmp.getText().toString().isEmpty()) {
            sendPwmpCommand();
        }

        if (!mUtl.getText().toString().isEmpty()) {
            sendUtlCommand();
        }

        if (!mLtl.getText().toString().isEmpty()) {
            sendLtlCommand();
        }
        sendPidHCommand();
        sendPidCCommand();
    }

    private boolean isInputValid() {

        return isValidPWMP(mPwmp.getText().toString());
    }

    private boolean isValidPWMP(String s) {

        if (s.isEmpty()) {
            return true;
        }
        double pwmp = Double.parseDouble(s);
        if (pwmp >= 2 && pwmp <= 30) {
            return true;

        } else {

            Log.d(TAG, "isValidPWMP: pwmp is out of range: " + pwmp);
            Snackbar.make(view, "PWMP must be >= 2 and <= 30", Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }

    private void getControllerParameters() {
        //disable window
//        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        isBusyDownLoading = true; //HomeActivity checks this on onBackPressed to prevent leaving fragment until complete
        mProgressBarLayout.setVisibility(View.VISIBLE);
        mProgressBarTextView.setText(R.string.get_param_message);
        makeLayoutsInvisible();
        sendPwmpQuery();
        sendUtlQuery();
        sendLtlQuery();
        sendPidHQuery();
        sendPidCQuery();
    }

    private void makeLayoutsInvisible() {

        mButtonLayout.setVisibility(View.INVISIBLE);
        mPwmpLayout.setVisibility(View.INVISIBLE);
        mPidLayout.setVisibility(View.INVISIBLE);
        mTempLimitLayout.setVisibility(View.INVISIBLE);
    }

    private void makeLayoutsVisible() {
        mButtonLayout.setVisibility(View.VISIBLE);
        mPwmpLayout.setVisibility(View.VISIBLE);
        mPidLayout.setVisibility(View.VISIBLE);
        mTempLimitLayout.setVisibility(View.VISIBLE);
    }

    private void loadControllerParameterDefaults() {

        mPH.setText(R.string.p_default);
        mIH.setText(R.string.i_default);
        mDH.setText(R.string.d_default);
        mPC.setText(R.string.p_default);
        mIC.setText(R.string.i_default);
        mDC.setText(R.string.d_default);
        mPwmp.setText(R.string.pwmp_default);
        mUtl.setText(R.string.utl_default);
        mLtl.setText(R.string.ltl_default);
    }

    private void sendPidCQuery() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending PIDC query");
                BluetoothConnectionService.getInstance().write(PIDC_QUERY);
            }
        }, DELAY * 8);
    }

    private void sendPidHQuery() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending PIDH query");
                BluetoothConnectionService.getInstance().write(PIDH_QUERY);
            }
        }, DELAY * 4);
    }

    private void sendLtlQuery() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending LTL query");
                BluetoothConnectionService.getInstance().write(LTL_QUERY);
            }
        }, DELAY * 3);
    }

    private void sendUtlQuery() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending UTL query");
                BluetoothConnectionService.getInstance().write(UTL_QUERY);
            }
        }, DELAY * 2);
    }

    private void sendPwmpQuery() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending PWMP query");
                BluetoothConnectionService.getInstance().write(PWMP_QUERY);
            }
        }, DELAY);
    }

    private void sendPidCCommand() {
        final String pidCCommand = getPidCCommand();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending PIDH command " + pidCCommand);
                BluetoothConnectionService.getInstance().write(pidCCommand);
            }
        }, DELAY * 5);
    }

    private void sendPidHCommand() {
        final String pidHCommand = getPidHCommand();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending PIDH command " + pidHCommand);
                BluetoothConnectionService.getInstance().write(pidHCommand);
            }
        }, DELAY * 4);
    }

    private void sendLtlCommand() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending UTL command " + LTL_COMMAND + mLtl.getText().toString());
                BluetoothConnectionService.getInstance().write(LTL_COMMAND + mLtl.getText().toString());
            }
        }, DELAY * 3);
    }

    private void sendUtlCommand() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending UTL command " + UTL_COMMAND + mUtl.getText().toString());
                BluetoothConnectionService.getInstance().write(UTL_COMMAND + mUtl.getText().toString());
            }
        }, DELAY * 2);
    }

    private void sendPwmpCommand() {
        final String pwmpCommand = getPwmpCommand();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: sending pwmp command " + pwmpCommand);
                BluetoothConnectionService.getInstance().write(pwmpCommand);
            }
        }, DELAY);
    }

    private String getPidHCommand() {
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append(PIDH_COMMAND).append(mPH.getText().toString()).append(",").append(mIH.getText().toString())
                .append(",").append(mDH.getText().toString());
        return sb.toString();
    }

    private String getPidCCommand() {
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append(PIDC_COMMAND).append(mPC.getText().toString()).append(",").append(mIC.getText().toString())
                .append(",").append(mDC.getText().toString());
        return sb.toString();
    }

    private String getPwmpCommand() {

        return PWMP_COMMAND + mPwmp.getText().toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mBroadcastReceiver = new BroadcastReceiver() {
            int pidHCount = 0;
            int pidCCount = 0;

            @Override
            public void onReceive(Context context, Intent intent) {


                String commandSent = intent.getStringExtra(BluetoothConnectionService.COMMAND_SENT);
                String response = intent.getStringExtra(BluetoothConnectionService.BLUETOOTH_RESPONSE);
                Log.d(TAG, "onReceive: called, command sent: " + commandSent + " response received: " + response);

                if (commandSent.contains(PIDC_COMMAND)) {
                    Log.d(TAG, "last set command was sent");
                    mProgressBarLayout.setVisibility(View.INVISIBLE);
                    makeLayoutsVisible();
                }
                switch (commandSent) {

                    case PIDH_QUERY:

                        Log.d(TAG, "Inside PIDH_QUERY case, response: " + response);
                        mPH.setText(response);
                        pidHCount++;
                        break;

                    case PIDC_QUERY:

                        Log.d(TAG, "Inside PIDC_QUERY case, response: " + response);
                        mPC.setText(response);
                        pidCCount++;
                        break;

                    case UTL_QUERY:
                        mUtl.setText(response);
                        break;

                    case LTL_QUERY:
                        mLtl.setText(response);
                        break;

                    case PWMP_QUERY:
                        mPwmp.setText(response);
                        break;
                    default:
                        if (pidHCount == 1) {
                            pidHCount++;
                            mIH.setText(response);

                        } else if (pidHCount == 2) {
                            pidHCount = 0; //reset to 0
                            mDH.setText(response);

                        } else if (pidCCount == 1) {
                            pidCCount++;
                            mIC.setText(response);

                        } else if (pidCCount == 2) {
                            pidCCount = 0;
                            mDC.setText(response);
                            mProgressBarLayout.setVisibility(View.INVISIBLE);
                            //re-enable window and set isBusyDownloading to false
//                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            isBusyDownLoading=false;
                            makeLayoutsVisible();
                        }
                        Log.d(TAG, "inside the default case, response is: " + response);
                }
            }
        };
        LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothConnectionService.MY_INTENT_FILTER));
    }

    @Override
    public void onDetach() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_parameter_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (isBusyDownLoading){
            Snackbar.make(view, R.string.download_parameters_message, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {

            case R.id.loadDefaults:
                loadControllerParameterDefaults();
                return true;
        }
        return false;
    }
}