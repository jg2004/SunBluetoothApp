package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.BluetoothStartUp;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.utilities.Constants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.START_DISCOVERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_HELP_DIALOG;


public class IntroFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "IntroFragment";
    private ActionBar mSupportActionBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_intro, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

        Button buttonConnect = (Button) view.findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(this);
        Button buttonLogViewer = (Button) view.findViewById(R.id.buttonViewLog);
        buttonLogViewer.setOnClickListener(this);
        Button buttonDiscover = (Button) view.findViewById(R.id.buttonDiscover);
        buttonDiscover.setOnClickListener(this);
        Button buttonHelp = (Button) view.findViewById(R.id.buttonHelp);
        buttonHelp.setOnClickListener(this);
        Button buttonSettings = (Button) view.findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonHelp:

                String helpFileContents = getFileContents();
                Log.d(TAG, "onClick: file contents: " + "\n" + helpFileContents);
                showHelpDialog(helpFileContents);
                break;

            case R.id.buttonConnect:

                if (BluetoothConnectionService.getInstance().getCurrentState() == BluetoothConnectionService.STATE_NONE) {
                    Log.d(TAG, "onClick: not connected, starting BluetoothStartup class");
                    Intent intent = new Intent(getContext(), BluetoothStartUp.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "onClick: bluetooth already connected, starting HomeActivity!");
                    Intent intent = new Intent(getContext(), HomeActivity.class);
                    startActivity(intent);
                }

                break;
            case R.id.buttonSettings:
                performTransaction(new SettingsFragment(), "tag_frag_settings");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String test = preferences.getString("pref_controller_type", "ec1x");
                Log.d(TAG, "onClick: pref value is: " + test);
                break;

            case R.id.buttonViewLog:

                performTransaction(new LogFileListFragment(), Constants.TAG_FRAGMENT_LOGGER);

                break;

            case R.id.buttonDiscover:

                Intent intentDiscoverBluetooth = new Intent(getContext(), BluetoothStartUp.class);
                intentDiscoverBluetooth.putExtra(START_DISCOVERY, true);
                startActivity(intentDiscoverBluetooth);
                break;
        }
    }

    private void showHelpDialog(String helpFileContents) {

        HelpDialogFragment frag = HelpDialogFragment.newInstance(helpFileContents);
        frag.show(getFragmentManager(), TAG_FRAGMENT_HELP_DIALOG);

    }

    private String getFileContents() {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        AssetManager assetManager = getActivity().getAssets();
        try {
            InputStream inputStream = assetManager.open("help.txt");
            br = new BufferedReader(new InputStreamReader(inputStream));
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp).append("\n");
            }

        } catch (FileNotFoundException f) {
            Log.d(TAG, "getFileContents: error opening help.txt file");
            Toast.makeText(getContext(), "File not found: " + "\n" + f.getMessage(), Toast.LENGTH_LONG).show();
            f.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "getFileContents: error opening help.txt file");
            Toast.makeText(getContext(), "Error opening text file" + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();

            } catch (IOException ex) {
                Log.d(TAG, "getFileContents: error closing help.txt file");
                ex.printStackTrace();
            }
        }
        return sb.toString();
    }

    public void performTransaction(Fragment fragment, String tag) {

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.homeContainer, fragment, tag).commit();
    }
}
