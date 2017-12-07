package com.sunelectronics.sunbluetoothapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sunelectronics.sunbluetoothapp.activities.BluetoothStartUp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnectionService implements Serializable {

    private static final String TAG = "BluetoothConnectionServ";
    //The following UUID is a common UUID for serial bluetooth devices
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String COULD_NOT_CONNECT_BLUETOOTH = "couldNotConnectBluetooth";
    public static final String DEV_NAME = "DEVICE_NAME";
    public static final String BLUETOOTH_SUCCESSFULLY_CONNECTED = "BluetoothConnected";
    private Context mContext;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BluetoothDevice mDevice;
    private UUID deviceUUID;
    private BluetoothAdapter mBluetoothAdapter;
    private static BluetoothConnectionService mBluetoothConnectionService;
    private List<String> mCommandsWrittenList = new ArrayList<>();

    // public constants
    public static final String COMMAND_SENT = "commandSent";
    public static final String MY_INTENT_FILTER = "incomingMessage";
    public static final String BLUETOOTH_RESPONSE = "response";

    private BluetoothConnectionService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceUUID = MY_UUID;
    }

    public static BluetoothConnectionService getInstance(Context context) {


        if (mBluetoothConnectionService == null) {
            Log.d(TAG, "getInstance: mBluetoothConnectionServices is null, creating singleton");
            mBluetoothConnectionService = new BluetoothConnectionService(context);
        }

        return mBluetoothConnectionService;
    }

    /*---------------------------public methods--------------------------------------------------*/

    /**
     * This method is called from activity to create a connection  with a bluetooth device.
     *
     * @param bluetoothDevice
     */
    public void startClient(BluetoothDevice bluetoothDevice) {

        Log.d(TAG, "startClient: started, mContext is: " + mContext.getClass().getName());
        mConnectThread = new ConnectThread(bluetoothDevice);
        mConnectThread.start();
    }

    /**
     * convenience method to write strings to bluetooth. Method appends carriage return then
     * converts to byte array before writing to bluetooth
     *
     * @param stringToSend
     */
    public void write(String stringToSend) {

        mCommandsWrittenList.add(stringToSend); // used to determine expected response


        // append carriage return and convert to byte array
        StringBuilder sb = new StringBuilder();
        sb.append(stringToSend);
        sb.append("\r");
        byte[] out = sb.toString().getBytes();
        mConnectedThread.write(out);
    }

    public void cancel() {

        mConnectedThread.cancel();
    }

    public void clearCommandsWrittenList() {
        Log.d(TAG, "clearCommandsWrittenList: clearing commands!");

        mCommandsWrittenList.clear();
    }

    /*---------------------------private methods--------------------------------------------------*/

    /**
     * this is the method that is executed inside the ConnectThread class once a bluetooth connection is made
     *
     * @param bluetoothSocket
     */
    private void connected(BluetoothSocket bluetoothSocket) {
        Log.d(TAG, "connected: called");
        mConnectedThread = new ConnectedThread(bluetoothSocket);
        mConnectedThread.start();

    }

 /*--------------------------------end of public/private methods----------------------------------*/

/*-------------------------------------INNER CLASSES----------------------------------------------*/

    /**
     * This class is the thread that maintains the connection. It runs continuously until an exception
     * occurs or is cancelled
     */
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmBluetoothSocket;
        private final InputStream mmInstream;
        private final OutputStream mmOutstream;
        private BufferedReader br;

        ConnectedThread(BluetoothSocket socket) {

            Log.d(TAG, "ConnectedThread: created");
            mmBluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpOut = socket.getOutputStream();
                tmpIn = socket.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInstream = tmpIn;
            mmOutstream = tmpOut;
            br = new BufferedReader(new InputStreamReader(mmInstream), 2048);
        }

        @Override
        public void run() {

            //BufferedReader br = new BufferedReader(new InputStreamReader(mmInstream), 2048);
            String bluetoothResponse;

            //keep reading from input stream until exception occurs, the read method is a blocking
            //call!!!
            while (true) {

                try {
                    //below is code to read without using buffered reader:

                    //bytes = mmInstream.read(buffer); //-----BLOCKING CALL (just waits)------------
                    // where bytes is integer of bytes read and buffer is a byte array (byte[128])
                    bluetoothResponse = br.readLine();
                    Log.d(TAG, "bufferedReader read in:  " + bluetoothResponse);

                    if (bluetoothResponse.equals(null)) {
                        Log.d(TAG, "incoming message was null, ignore");
                    }
                    if (bluetoothResponse.equals("")) {
                        Log.d(TAG, "incoming message was blank, ignore");
                    }

                    // than 0 before performing the following
                    String unHandledCommand = "NO COMMAND SENT";

                    if (mCommandsWrittenList.size() > 0) {
                        unHandledCommand = mCommandsWrittenList.get(0);
                        Log.d(TAG, " removing command from list: " + mCommandsWrittenList.get(0));
                        mCommandsWrittenList.remove(0);
                    }

                    Log.d(TAG, "command list size: " + mCommandsWrittenList.size());
                    Intent incomingMessageIntent = new Intent(MY_INTENT_FILTER);
                    incomingMessageIntent.putExtra(COMMAND_SENT, unHandledCommand);
                    incomingMessageIntent.putExtra(BLUETOOTH_RESPONSE, bluetoothResponse);
                    Log.d(TAG, "sending broadcast of message " + bluetoothResponse);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                } catch (IOException e) {
                    Log.d(TAG, "run: Error reading from input stream");
                    Log.d(TAG, "error message is: "+ e.getMessage());
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {


            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: writing data to output stream: " + text);

            try {
                mmOutstream.write(bytes);
            } catch (IOException e) {
                Log.d(TAG, "write: Error writing data to output stream: " + text);
                e.printStackTrace();
                // TODO: 12/5/2017  when connection lost this exception occurs
                // might be a good idea to broadcast this back to activity to let it know

            }
        }

        /**
         * call this from activity to shutdown the connection
         */
        public void cancel() {

            //close down streams and socket

            try {
                if (mmBluetoothSocket != null) {
                    Log.d(TAG, "cancel: closing bt socket");
                    mmBluetoothSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "cancel: could not close bt socket");
            }

            try {
                if (mmInstream != null) {
                    Log.d(TAG, "cancel: closing input stream");
                    mmInstream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "cancel: could not close input stream");
            }
            try {
                if (mmOutstream != null) {
                    Log.d(TAG, "cancel: closing output stream");
                    mmOutstream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "cancel: could not close output stream");
            }
            try {
                if (br != null) {
                    Log.d(TAG, "cancel: closing buffered reader");
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "cancel: could not close buffered reader");
            }


        }
    }//end of the ConnectedThread class
/*-------------------------------------ConnectThread class----------------------------------------*/

    /**
     * this class represents a thread that attempts to make a connection. It runs straight through
     * and either succeeds or fails
     */

    private class ConnectThread extends Thread {
        private BluetoothSocket mBluetoothSocket;

        //first step in connecting bluetooth is to get a socket
        //second step is to connect

        public ConnectThread(BluetoothDevice device) {
            Log.d(TAG, "ConnectThread: created");
            mDevice = device;
        }

        @Override
        public void run() {
            Log.d(TAG, "ConnectThread is now running");
            BluetoothSocket tmp = null;
            // attempt to create an insecure bluetooth socket
            try {
                Log.d(TAG, "trying to create insecure RF comm socket...");
                tmp = mDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);


            } catch (IOException e) {
                Log.d(TAG, "run: ConnectThread could not create insecureRFCommSocket, error: " + e.getMessage());
                e.printStackTrace();
            }
            //if here, then socket was created!
            mBluetoothSocket = tmp;
            mBluetoothAdapter.cancelDiscovery();//always cancel before connecting

            //now that we have a socket, attempt to make a connection

            try {
                //this is a blocking call that will return successful or  cause an exception
                Log.d(TAG, "Attempting to connect. with " + mDevice.getName());
                mBluetoothSocket.connect();
                Log.d(TAG, "Connection was successful!");
            } catch (IOException e) {
                Log.d(TAG, "Unable to connect to device " + mDevice.getName());
                Log.d(TAG, "Attempting to close connection...");
                Intent couldNotConnectIntent = new Intent(COULD_NOT_CONNECT_BLUETOOTH);
                couldNotConnectIntent.putExtra(DEV_NAME, mDevice.getName());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(couldNotConnectIntent);
                try {
                    mBluetoothSocket.close();
                } catch (IOException e1) {
                    Log.d(TAG, "Could not close the connection, error message: " + e1.getMessage());
                    return;
                }
                Log.d(TAG, "Connection was successfully closed");
            }

            //if it gets here, then check connection was successful, proceed to private method connected
            if (mBluetoothSocket.isConnected()) {
                Log.d(TAG, "socket is connected, starting connected thread");
                Intent intent = new Intent(BLUETOOTH_SUCCESSFULLY_CONNECTED);
                intent.putExtra(DEV_NAME, mDevice.getName());
                Log.d(TAG, "run: sending broadcast successfully connected");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                storeDevice();
                connected(mBluetoothSocket);
            }
        }

        /**
         * store the most recently connected to device to SharedPrefs and store device to file
         * to allow connection to a device that was successfully connected to. This file can then
         * be read in and displayed in a list of previously paired and successfully connected
         * devices
         */
        private void storeDevice() {
            // TODO: 10/18/2017 save device to file as well as shared prefs
            SharedPreferences blueToothDevicePreferences = mContext.getSharedPreferences(BluetoothStartUp.MY_PREF, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = blueToothDevicePreferences.edit();
            editor.putString(BluetoothStartUp.BLUETOOTHDEV, mDevice.getName());
            Log.d(TAG, "saving device " + mDevice.getName() + " to sharedPrefs");
            editor.commit();
        }


    }//end of ConnectThread class
}
