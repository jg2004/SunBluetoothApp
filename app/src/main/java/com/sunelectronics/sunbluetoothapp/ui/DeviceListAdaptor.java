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

        BluetoothDevice device = discoveredDeviceList.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(layoutResource, null);

        }

        if (device != null) {

            TextView btName = (TextView) convertView.findViewById(R.id.tvDevName);
            TextView btAddress = (TextView) convertView.findViewById(R.id.tvDevAddress);
            String name = device.getName();
            String address = device.getAddress();
            if (name == null) {
                name = "Name not supplied by vendor";
            }
            btName.setText(name);
            btAddress.setText(address);
        }
        return convertView;
    }

}