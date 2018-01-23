package com.sunelectronics.sunbluetoothapp.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;

import java.util.List;

import static android.content.ContentValues.TAG;

public class LogFileContentRecyclerViewAdapter extends RecyclerView.Adapter<LogFileContentRecyclerViewAdapter.ViewHolder> {

    private List<String> mFileContentList;
    private LayoutInflater mLayoutInflater;

    public LogFileContentRecyclerViewAdapter(Context context, List<String> fileContentList) {
        mLayoutInflater = LayoutInflater.from(context);
        mFileContentList = fileContentList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mLayoutInflater.inflate(R.layout.log_content_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String fileContent = mFileContentList.get(position);
        holder.lineTextView.setText(fileContent);
    }

    @Override
    public int getItemCount() {
        return mFileContentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView lineTextView;

        ViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder: new view created");
            lineTextView = (TextView) itemView.findViewById(R.id.tvRow);
        }
    }
}
