package com.sunelectronics.sunbluetoothapp.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.models.LogFileObject;

import java.util.List;

import static android.content.ContentValues.TAG;

public class LogFileListAdaptor extends ArrayAdapter<LogFileObject> {

    private Context mContext;
    private int mLayoutResource;
    private List<LogFileObject> mLogFileObjectList;

    public LogFileListAdaptor(@NonNull Context context, @LayoutRes int resource, @NonNull List<LogFileObject> logFileObjectList) {
        super(context, resource, logFileObjectList);
        mContext = context;
        mLayoutResource = resource;
        mLogFileObjectList = logFileObjectList;
    }

    public void remove(String fileName) {

        for (LogFileObject fileObject : mLogFileObjectList) {

            if (fileObject.getFileName().equals(fileName)) {
                remove(fileObject);
                Log.d(TAG, "removed: file: " + fileObject.getFileName());
                break;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LogFileObject logFileObject = mLogFileObjectList.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, null);
        }

        TextView fileNameTv = (TextView) convertView.findViewById(R.id.textViewFileName);
        TextView fileSizeTv = (TextView) convertView.findViewById(R.id.textViewFileSize);
        String name = logFileObject.getFileName();
        String size = logFileObject.getFileSizeAsString();
        fileNameTv.setText(name);
        fileSizeTv.setText(size);

        return convertView;
    }
}
