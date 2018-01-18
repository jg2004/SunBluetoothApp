package com.sunelectronics.sunbluetoothapp.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.fragments.MyAlertDialogFragment;
import com.sunelectronics.sunbluetoothapp.ui.DeviceListAdaptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_NOTIFICATION;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.START_DISCOVERY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.VER_LESS_KITKAT_MESSAGE;

public class BluetoothStartUp extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "BluetoothStartUp";
    public static final String MY_PREF = "myPrefs";
    public static final String BLUETOOTHDEV = "bluetoothDevice";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothReceiver mBluetoothReceiver;
    private DeviceListAdaptor mAdaptor;
    private List<BluetoothDevice> mBluetoothDeviceList;
    private ListView mDeviceListView;
    private TextView mTvDeviceList, mProgressBarTextView;
    private CardView mProgressBarContainer;
    private boolean mIsDisplayingDiscovered, mStartDiscovery;
    private BluetoothConnectionService mBluetoothConnectionService;
    private ActionBar actionbar;

    /*-----------------overriden methods of AppCompatActivity----------------------------------------*/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: started");
        Log.d(TAG, "phone version: " + Build.VERSION.CODENAME + " SDK " + Build.VERSION.SDK_INT);
        actionbar = getSupportActionBar();

        mBluetoothConnectionService = BluetoothConnectionService.getInstance();
        setContentView(R.layout.activity_bluetooth_startup);
        mDeviceListView = (ListView) findViewById(R.id.deviceListView);
        mTvDeviceList = (TextView) findViewById(R.id.textViewDeviceList);
        mProgressBarTextView = (TextView) findViewById(R.id.textViewProgressBar);
        mProgressBarContainer = (CardView) findViewById(R.id.progressBarWithText);
        mProgressBarContainer.setVisibility(View.GONE);
        mBluetoothDeviceList = new ArrayList<>();
        mAdaptor = new DeviceListAdaptor(this, R.layout.disc_bt_devices, mBluetoothDeviceList);
        mDeviceListView.setAdapter(mAdaptor);
        mDeviceListView.setOnItemClickListener(this);
        setUpReceiver();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "phone does not have bluetooth!");
            Toast.makeText(this, "Sorry, but this phone (Ver: " + Build.VERSION.CODENAME + "\n" + "SDK: " + Build.VERSION.SDK_INT + ") " +
                    "does not appear to have bluetooth ", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //get the intent from IntroActivity to see if discovery button pressed
        Intent intent = getIntent();
        mStartDiscovery = intent.getBooleanExtra(START_DISCOVERY, false);
        if (mStartDiscovery) {
            Log.d(TAG, "Discovery button from Intro activity selected, discovering devices...");
            startDiscovery();
        } else {
            Log.d(TAG, "Connect button from Intro activity selected, attempting connection...");
            attemptConnection();
        }

    }//end of OnCreate

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called");
        //check to see if discovering, if so then disable paired menu item
        MenuItem pairedMenuItem = menu.findItem(R.id.actionPairedBluetooth);
        MenuItem discoveringMenuItem = menu.findItem(R.id.actionDiscoverBluetooth);

        if (mBluetoothAdapter == null) return false;
        //set menu item's discover icon based on whether it's discovering or not
        if (mBluetoothAdapter.isDiscovering()) {
            pairedMenuItem.setEnabled(false);
            discoveringMenuItem.setIcon(R.drawable.ic_action_cancel_discovery);

        } else {
            pairedMenuItem.setEnabled(true);
            discoveringMenuItem.setIcon(R.drawable.ic_action_discover);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionsMenu: called");
        getMenuInflater().inflate(R.menu.menu_bluetooth_startup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.actionDiscoverBluetooth:

                Log.d(TAG, "onOptionsItemSelected: discovering bluetooth devices");

                if (mBluetoothAdapter.isDiscovering()) {
                    item.setIcon(R.drawable.ic_action_discover);
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }

                } else {
                    item.setIcon(R.drawable.ic_action_cancel_discovery);
                    mAdaptor.setDiscoveredDeviceList(mBluetoothDeviceList);
                    mDeviceListView.setAdapter(mAdaptor);
                    discoverDevices();
                }
                break;

            case R.id.actionPairedBluetooth:
                Log.d(TAG, "onOptionsItemSelected: displaying paired devices");
                displayPairedDevices();
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // when displaying discovered devices, try to pair with device
        Log.d(TAG, "onItemClick: you clicked on item " + position + " mIsDisplayingDiscovered: " + mIsDisplayingDiscovered);

        BluetoothDevice device = mBluetoothDeviceList.get(position);

        if (device != null && mIsDisplayingDiscovered) {
            //if device is present and list is showing discovered devices
            // note: the createbond method is only available with API's >19

            Log.d(TAG, "onItemClick: version: " + Build.VERSION.SDK_INT + "bond state: " + device.getBondState());
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2 && device.getBondState() == BluetoothDevice.BOND_NONE) {
                Log.d(TAG, " device not bonded and API >19, attempting to create Bond with " + device.getName());
                device.createBond();
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "device is already bonded, starting connection");
                startBTConnection(device);
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2 && device.getBondState() == BluetoothDevice.BOND_NONE) {
                Log.d(TAG, " device not bonded and API < 19, cannot use createBond() method. Manually pair " + device.getName());
                showManuallyPairDialog();
            }
        } else if (device != null && !mIsDisplayingDiscovered) {

            // if device is present and list is showing paired devices (Not Discovered)
            // first verify bluetooth enabled
            if (mBluetoothAdapter.isEnabled()) {
                startBTConnection(device);
            } else {
                //bluetooth has been turned off, enable it!!
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }
        }
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called, unregister receiver mBluetoothReceiver");
        unregisterReceiver(mBluetoothReceiver);
        LocalBroadcastManager.getInstance(BluetoothStartUp.this).unregisterReceiver(mBluetoothReceiver);
        super.onDestroy();
    }

    /*
    * -------------------------------------private methods------------------------------------------
    * */

    /**
     * dialog to notify user that their phone is using API less than 19 and must manually pair with
     * Laird bluetooth device.
     */
    private void showManuallyPairDialog() {

        Bundle args = new Bundle();
        args.putString(ALERT_TITLE, VER_LESS_KITKAT_MESSAGE);
        args.putString(ALERT_TYPE, ALERT_NOTIFICATION);
        args.putString(ALERT_MESSAGE, "This device is ver " + Build.VERSION.RELEASE + " and must be manually paired. To pair, go to settings -> Bluetooth -> Scan (if necessary) and select the Laird device found. Pairing code is 1234");
        args.putInt(ALERT_ICON, R.drawable.ic_warning_black_48dp);
        MyAlertDialogFragment dialog = MyAlertDialogFragment.newInstance(args);
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * set up intent filters and register receiver
     */
    private void setUpReceiver() {
        Log.d(TAG, "register mBluetoothReceiver");
        mBluetoothReceiver = new BluetoothReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);

        IntentFilter couldNotconnectBtFilter = new IntentFilter(BluetoothConnectionService.COULD_NOT_CONNECT_BLUETOOTH);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBluetoothReceiver, couldNotconnectBtFilter);
        IntentFilter successfullConnectionFilter = new IntentFilter(BluetoothConnectionService.BLUETOOTH_SUCCESSFULLY_CONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBluetoothReceiver, successfullConnectionFilter);
    }

    /**
     * display a list of all paired devices
     */
    public void displayPairedDevices() {

        Log.d(TAG, "displayPairedDevices: started");
        mProgressBarContainer.setVisibility(View.GONE);
        mBluetoothDeviceList.clear();
        actionbar.setTitle(R.string.paired_bt_devices);
        Set<BluetoothDevice> bluetoothDeviceSet = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bluetoothDeviceSet) {
            mBluetoothDeviceList.add(device);
        }
        if (mBluetoothDeviceList.isEmpty()) {
            mDeviceListView.setVisibility(View.INVISIBLE);
            mTvDeviceList.setVisibility(View.VISIBLE);
            mTvDeviceList.setText(R.string.no_paired_dev);

        } else {
            mDeviceListView.setVisibility(View.VISIBLE);
            mTvDeviceList.setVisibility(View.INVISIBLE);
        }
        mAdaptor.notifyDataSetChanged();
        mIsDisplayingDiscovered = false;
        Log.d(TAG, "displayPairedDevices: " + mBluetoothDeviceList);

    }

    /**
     * start the discovery of unpaired bluetooth devices
     */
    private void discoverDevices() {

        Log.d(TAG, "discoverDevices: looking for a list of bluetooth devices to pair with");

        //cancel discovery if in discovery mode
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        if (mBluetoothAdapter.startDiscovery()) {
            Log.d(TAG, "discoverDevices: discovery started successfully");
            actionbar.setTitle(R.string.disc_mode);

        } else {
            Toast.makeText(this, "Discovery could not be started. Verify bluetooth is enabled", Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
            Log.d(TAG, "discoverDevices: discovery could not be started");
        }
    }

    /**
     * @param bluetoothDevice bluetooth device to connect with
     */
    private void startBTConnection(BluetoothDevice bluetoothDevice) {
        //this method called from onClicklistener and Broadcast receiver after pairing with discovered
        //device
        mProgressBarContainer.setVisibility(View.VISIBLE);
        mDeviceListView.setVisibility(View.INVISIBLE);
        mProgressBarTextView.setText("Attempting to connect to " + bluetoothDevice.getName());
        actionbar.setTitle(R.string.connecting);
        mBluetoothConnectionService.startClient(bluetoothDevice, getApplicationContext());
    }

    private void initialize() {
        //attempt to connect to a device with device name saved in sharedPrefs
        //if no device name present in prefs, then display list of paired devices
        SharedPreferences prefs = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        //device's name is saved if successfully connected in BluetoothConnectionService class
        //inside the ConnectThread inner class
        String deviceName = prefs.getString(BLUETOOTHDEV, "NONE");
        Log.d(TAG, "device in prefs file is: " + deviceName);
        if (deviceName.equals("NONE")) {
            Log.d(TAG, "no devices saved in prefs");
            displayPairedDevices();
        } else {
            //find the paired device with name found in prefs file
            boolean deviceFound = false;
            Log.d(TAG, "looking for device saved in prefs file...");
            Set<BluetoothDevice> bluetoothDeviceSet = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bluetoothDeviceSet) {
                if (device.getName().equals(deviceName)) {
                    Log.d(TAG, "found device " + deviceName + ", attempting to connect");
                    deviceFound = true;
                    startBTConnection(device);
                    break;
                }
            }
            if (!deviceFound) {
                Log.d(TAG, "could not find device " + deviceName + "in paired device list");
                displayPairedDevices();
            }
        }
    }

    /**
     * method called when Connect button pressed in IntroActivity.
     */
    private void attemptConnection() {

        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "bluetooth is already on");
            initialize();
        } else {
            //turn on bluetooth
            turnOnBluetooth();

        }
    }

    /**
     * method called when Disccover button pressed in IntroActivity.
     */
    private void startDiscovery() {
        if (mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "bluetooth is already on");
            discoverDevices();
        } else {
            //turn on bluetooth
            turnOnBluetooth();

        }
    }

    private void turnOnBluetooth() {
        mTvDeviceList.setVisibility(View.VISIBLE);
        mDeviceListView.setVisibility(View.INVISIBLE);
        mTvDeviceList.setText(R.string.bluetooth_enabled_message);
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(intent);
    }


    /*--------------------------------------------------------------------------------------------------
    * THIS IS THE INNER CLASS THAT HANDLES BROADCASTS FROM ANDROID BLUETOOTH
    * */
    private class BluetoothReceiver extends BroadcastReceiver {
        private static final String TAG = "BluetoothReceiver";
        private Set<BluetoothDevice> mBluetoothDeviceSet = new HashSet<>();

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: called with action: " + intent.getAction());
            String action = intent.getAction();

            switch (action) {

                case BluetoothConnectionService.COULD_NOT_CONNECT_BLUETOOTH:

                    Log.d(TAG, "onReceive: Could not connect bluetooth");
                    mProgressBarContainer.setVisibility(View.GONE);
                    String devName = intent.getStringExtra(BluetoothConnectionService.DEV_NAME);
                    Toast.makeText(BluetoothStartUp.this, "Could not connect with " + devName, Toast.LENGTH_LONG).show();
                    displayPairedDevices();
                    break;

                case BluetoothConnectionService.BLUETOOTH_SUCCESSFULLY_CONNECTED:

                    Log.d(TAG, "onReceive: Successfully connected to bluetooth");
                    String devicename = intent.getStringExtra(BluetoothConnectionService.DEV_NAME);
                    Toast.makeText(BluetoothStartUp.this, "Successfully connected to " + devicename + " !", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(BluetoothStartUp.this, HomeActivity.class));
                    Log.d(TAG, "finishing activity");
                    finish();

                case BluetoothAdapter.ACTION_STATE_CHANGED: //when BT is enabled

                    Log.d(TAG, "onReceive: ACTION_STATE_CHANGED");
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    switch (state) {

                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "onReceive: Bluetooth is OFF");
                            actionbar.setSubtitle("Bluetooth disabled");
                            break;

                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "onReceive: Bluetooth is turning OFF");
                            actionbar.setSubtitle("Disabling Bluetooth...");
                            break;

                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "onReceive: Bluetooth is ON");

                            actionbar.setSubtitle("");
                            mTvDeviceList.setVisibility(View.INVISIBLE);
                            mDeviceListView.setVisibility(View.VISIBLE);
                            if (mStartDiscovery) {
                                //this indicates discover button pressed in IntroActivity
                                discoverDevices();
                            } else {
                                initialize();
                            }

                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "onReceive: Bluetooth is turning ON");
                            actionbar.setTitle("Enabling Bluetooth...");
                            break;
                    }//end of inner switch to determine bluetooth state
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED://when BT discovery started

                    Log.d(TAG, "onReceive: discovery started");
                    mProgressBarContainer.setVisibility(ProgressBar.VISIBLE);
                    mProgressBarTextView.setText(R.string.searching_for_bluetooth_devices);
                    actionbar.setTitle(R.string.disc_mode);
                    mTvDeviceList.setVisibility(View.INVISIBLE);
                    mDeviceListView.setVisibility(View.VISIBLE);
                    Toast.makeText(BluetoothStartUp.this, "Discovery started", Toast.LENGTH_SHORT).show();
                    mBluetoothDeviceList.clear();
                    mIsDisplayingDiscovered = true;
                    invalidateOptionsMenu();
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED://when BT discovery finished
                    Toast.makeText(BluetoothStartUp.this, "Discovery complete", Toast.LENGTH_SHORT).show();

                    if (mBluetoothDeviceList.size() == 0) {
                        mTvDeviceList.setVisibility(View.VISIBLE);
                        mDeviceListView.setVisibility(View.INVISIBLE);
                        mTvDeviceList.setText(R.string.no_bluetooth_message);
                    }

                    invalidateOptionsMenu();
                    mProgressBarContainer.setVisibility(ProgressBar.GONE);
                    Log.d(TAG, "onReceive: discovery finished");
                    break;

                case BluetoothDevice.ACTION_FOUND://when BT discovery finds device

                    Log.d(TAG, "onReceive: device found");
                    mBluetoothDeviceList.clear();
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mBluetoothDeviceSet.add(device);
                    mBluetoothDeviceList.addAll(mBluetoothDeviceSet);
                    mAdaptor.notifyDataSetChanged();
                    Log.d(TAG, "device found: " + "name: " + device.getName() + ", device address: " + device.getAddress());
                    break;

                case BluetoothDevice.ACTION_BOND_STATE_CHANGED://when bonding (pairing) with device

                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    int bondState = bluetoothDevice.getBondState();

                    switch (bondState) {

                        case BluetoothDevice.BOND_BONDED:
                            Log.d(TAG, "onReceive: BONDED with: " + bluetoothDevice.getName());
                            startBTConnection(bluetoothDevice);
                            break;

                        case BluetoothDevice.BOND_BONDING:
                            Log.d(TAG, "onReceive: attempting to BOND with: " + bluetoothDevice.getName());
                            break;

                        case BluetoothDevice.BOND_NONE:
                            Log.d(TAG, "onReceive: BOND broken with: " + bluetoothDevice.getName());
                            break;
                    }
            } //end of outer switch statement to filter out intents

        }
    }
}