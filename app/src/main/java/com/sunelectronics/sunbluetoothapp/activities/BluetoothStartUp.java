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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.bluetooth.BluetoothConnectionService;
import com.sunelectronics.sunbluetoothapp.ui.DeviceListAdaptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private LinearLayout mProgressBarContainer;
    private boolean isDiscovering, isDisplayingDiscovered;
    private MenuItem mDiscoveringMenutItem;
    private BluetoothConnectionService mBluetoothConnectionService;

    /*-----------------overriden methods of AppCompatActivity----------------------------------------*/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: started");
        Log.d(TAG, "phone version: " + Build.VERSION.CODENAME + " SDK " + Build.VERSION.SDK_INT);
        mBluetoothConnectionService = BluetoothConnectionService.getInstance(BluetoothStartUp.this);
        setContentView(R.layout.activity_bluetooth_startup);
        mDeviceListView = (ListView) findViewById(R.id.deviceListView);
        mTvDeviceList = (TextView) findViewById(R.id.textViewDeviceList);
        mProgressBarTextView = (TextView) findViewById(R.id.textViewProgressBar);
        mProgressBarContainer = (LinearLayout) findViewById(R.id.progressBarWithText);
        mProgressBarContainer.setVisibility(View.GONE);
        mBluetoothDeviceList = new ArrayList<>();
        mAdaptor = new DeviceListAdaptor(this, R.layout.disc_bt_devices, mBluetoothDeviceList);
        mDeviceListView.setAdapter(mAdaptor);
        mDeviceListView.setOnItemClickListener(this);
        setUpReceiver();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "phone does not have bluetooth!");
            Toast.makeText(this, "phone " + Build.VERSION.CODENAME + " does not have bluetooth", Toast.LENGTH_LONG).show();
            // TODO: 10/1/2017 cannot use app without bluetooth
        } else {

            if (mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "bluetooth is already on");
                initialize();

            } else {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }
        }

    }//end of OnCreate


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called");
        //check to see if discovering, if so then disable paired menu item
        MenuItem paired = menu.findItem(R.id.actionPairedBluetooth);
        MenuItem discovering = menu.findItem(R.id.actionDiscoverBluetooth);
        //set as a field for global access
        mDiscoveringMenutItem = discovering;

        if (isDiscovering) {
            paired.setEnabled(false);

        } else {
            paired.setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_startup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.actionDiscoverBluetooth:

                Log.d(TAG, "onOptionsItemSelected: discovering bluetooth devices");

                if (isDiscovering) {
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
        Log.d(TAG, "onItemClick: you clicked on item " + position + " isDisplayingDiscovered: " + isDisplayingDiscovered);

        BluetoothDevice device = mBluetoothDeviceList.get(position);

        if (device != null && isDisplayingDiscovered) {
            //if device is present and list is showing discovered devices
            // note: the createbond method is only available with API's >19
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Log.d(TAG, "attempting to create Bond with " + device.getName());
                device.createBond();
            }
        } else if (device != null && !isDisplayingDiscovered) {

            // if device is present and list is showing paired devices (Not Discovered)

            startBTConnection(device);
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

//        registerReceiver(mBluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
//        IntentFilter discoveryIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        registerReceiver(mBluetoothReceiver, discoveryIntentFilter);
//        IntentFilter bondingFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        registerReceiver(mBluetoothReceiver, bondingFilter);
        IntentFilter couldNotconnectBtFilter = new IntentFilter(BluetoothConnectionService.COULD_NOT_CONNECT_BLUETOOTH);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBluetoothReceiver, couldNotconnectBtFilter);
        IntentFilter successfullConnectionFilter = new IntentFilter(BluetoothConnectionService.BLUETOOTH_SUCCESSFULLY_CONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBluetoothReceiver, successfullConnectionFilter);

    }

    /**
     * display a list of all paired devides
     */
    public void displayPairedDevices() {

        Log.d(TAG, "displayPairedDevices: started");
        mProgressBarContainer.setVisibility(View.GONE);
        mDeviceListView.setVisibility(View.VISIBLE);
        mBluetoothDeviceList.clear();
        mTvDeviceList.setText(R.string.paired_bt_devices);
        Set bluetoothDeviceSet = mBluetoothAdapter.getBondedDevices();

        Iterator<BluetoothDevice> iterator = bluetoothDeviceSet.iterator();
        while (iterator.hasNext()) {
            BluetoothDevice device = iterator.next();
            mBluetoothDeviceList.add(device);
        }
        mAdaptor.notifyDataSetChanged();
        isDisplayingDiscovered = false;
        Log.d(TAG, "displayPairedDevices: " + mBluetoothDeviceList);
    }

    /**
     * start the discovery of unpaired bluetooth devices
     */
    private void discoverDevices() {
        Log.d(TAG, "discoverDevices: looking for a list of bluetooth devices to pair with");
        mTvDeviceList.setText(R.string.disc_bt_devices);

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

        } else {
            Toast.makeText(this, "Discovery could not be started. Verify bluetooth is enabled", Toast.LENGTH_SHORT).show();
            mDiscoveringMenutItem.setIcon(R.drawable.ic_action_discover);
            Log.d(TAG, "discoverDevices: discovery could not be started");
        }
    }

    /**
     * Method called to start the Bluetooth connection service
     *
     * @param bluetoothDevice
     */
    private void startBTConnection(BluetoothDevice bluetoothDevice) {
        //this method called from onClicklistener and Broadcase receiver after pairing with discovered

        //device
        mProgressBarContainer.setVisibility(View.VISIBLE);
        mDeviceListView.setVisibility(View.GONE);
        mProgressBarTextView.setText("Attempting to connect with " + bluetoothDevice.getName() + "...");
        mBluetoothConnectionService.startClient(bluetoothDevice);
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
            Set bluetoothDeviceSet = mBluetoothAdapter.getBondedDevices();

            Iterator<BluetoothDevice> iterator = bluetoothDeviceSet.iterator();
            while (iterator.hasNext()) {
                BluetoothDevice device = iterator.next();
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

    /*--------------------------------------------------------------------------------------------------
    * THIS IS THE INNER CLASS THAT HANDLES BROADCASTS FROM ANDROID BLUETOOTH
    * */
    private class BluetoothReceiver extends BroadcastReceiver {
        private static final String TAG = "BluetoothReceiver";

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

                            break;

                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "onReceive: Bluetooth is turning OFF");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "onReceive: Bluetooth is ON");
                            initialize();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "onReceive: Bluetooth is turning ON");
                            break;
                    }//end of inner switch to determine bluetooth state
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_STARTED://when BT discovery started

                    Log.d(TAG, "onReceive: discovery started");
                    mProgressBarContainer.setVisibility(ProgressBar.VISIBLE);
                    mProgressBarTextView.setText(R.string.searching_for_bluetooth_devices);
                    mTvDeviceList.setText(getString(R.string.disc_bt_devices));
                    Toast.makeText(BluetoothStartUp.this, "Discovery started", Toast.LENGTH_SHORT).show();
                    mBluetoothDeviceList.clear();
                    isDiscovering = true;
                    isDisplayingDiscovered = true;

                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED://when BT discovery finished
                    Toast.makeText(BluetoothStartUp.this, "Discovery complete", Toast.LENGTH_SHORT).show();
                    isDiscovering = false;

                    if (mBluetoothDeviceList.size() == 0) {
                        mTvDeviceList.setText("No bluetooth devices found");
                    }

                    //set menu item's icon from cancel icon to search icon
                    mDiscoveringMenutItem.setIcon(R.drawable.ic_action_discover);
                    mProgressBarContainer.setVisibility(ProgressBar.GONE);
                    mAdaptor.notifyDataSetChanged();
                    Log.d(TAG, "onReceive: discovery finished");
                    break;

                case BluetoothDevice.ACTION_FOUND://when BT discovery finds device

                    Log.d(TAG, "onReceive: device found");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mBluetoothDeviceList.add(device);
                    mAdaptor.notifyDataSetChanged();

                    //mBluetoothDeviceSet.add(device);

                    // mBluetoothDevices.add(device);
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



