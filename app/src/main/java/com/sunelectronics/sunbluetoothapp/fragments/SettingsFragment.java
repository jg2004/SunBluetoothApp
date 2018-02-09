package com.sunelectronics.sunbluetoothapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.sunelectronics.sunbluetoothapp.R;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";
    private static final String PREF_CONT_TYPE = "pref_controller_type";
    private ActionBar mSupportActionBar;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.d(TAG, "onCreatePreferences: rootKey is: " + rootKey);
        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.show();
            mSupportActionBar.setTitle("SETTINGS");
        }

        addPreferencesFromResource(R.xml.preferences);
        Preference preference = findPreference(PREF_CONT_TYPE);

        if (preference instanceof ListPreference) {
            CharSequence entry = ((ListPreference) preference).getEntry();
            if (entry != null) {

                String value = ((ListPreference) preference).getEntry().toString();
                preference.setSummary(value);
            } else {
                ((ListPreference) preference).setValueIndex(0);
            }
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PREF_CONT_TYPE)) {
            Log.d(TAG, "onSharedPreferenceChanged: the controller type was changed");

            Preference preference = findPreference(key);
            if (preference instanceof ListPreference) {
                String value = ((ListPreference) preference).getEntry().toString();
                preference.setSummary(value);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSupportActionBar.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
    }
}
