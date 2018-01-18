package com.sunelectronics.sunbluetoothapp.ui;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;

import java.util.List;


public class DeviceListAdaptor extends ArrayAdapter<BluetoothDevice> {
    private List<BluetoothDevice> discoveredDeviceList;
    private int layoutResource;
    private Context mContext;

    public DeviceListAdaptor(@NonNull Context context, @LayoutRes int resource, @NonNull List<BluetoothDevice> btDeviceList) {
        super(context, resource, btDeviceList);
        mContext = context;
        layoutResource = resource;
        discoveredDeviceList = btDeviceList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(layoutResource, null);

        BluetoothDevice device = discoveredDeviceList.get(position);
        if (device != null) {

            TextView btName = (TextView) view.findViewById(R.id.tvDevName);
            TextView btAddress = (TextView) view.findViewById(R.id.tvDevAddress);
            String name = device.getName();
            String address = device.getAddress();
            if (name == null) {
                name = "Name not supplied by vendor";
            }
            btName.setText(name);
            btAddress.setText(address);
        }
        return view;
    }

    public void setDiscoveredDeviceList(List<BluetoothDevice> discoveredDeviceList) {
        this.discoveredDeviceList = discoveredDeviceList;
    }

}
