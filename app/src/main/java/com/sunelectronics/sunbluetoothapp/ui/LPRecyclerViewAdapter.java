package com.sunelectronics.sunbluetoothapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.LPDetailFragment;
import com.sunelectronics.sunbluetoothapp.activities.LocalProgramActivity;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;

import java.util.List;

/**
 * Created by Jerry on 8/19/2017.
 */

public class LPRecyclerViewAdapter extends RecyclerView.Adapter<LPRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private List<LocalProgram> mLocalProgramList;


    public LPRecyclerViewAdapter(Context context, List<LocalProgram> lpList) {

        mContext = context;
        mLocalProgramList = lpList;
    }

    @Override
    public LPRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lp_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LPRecyclerViewAdapter.ViewHolder holder, int position) {

        holder.lpNameTv.setText(mLocalProgramList.get(position).getName());
        holder.lpContentTv.setText((mLocalProgramList.get(position).getContent()));
    }

    @Override
    public int getItemCount() {
        return mLocalProgramList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView lpNameTv, lpContentTv;

        public ViewHolder(View view) {
            super(view);
            lpNameTv = (TextView) view.findViewById(R.id.lpNameTextView);
            lpContentTv = (TextView) view.findViewById(R.id.lpContentTextView);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LocalProgram localProgram = mLocalProgramList.get(getAdapterPosition());

                    Bundle args = new Bundle();
                    args.putSerializable("lp", localProgram);
                    LocalProgramActivity activity = (LocalProgramActivity) mContext;

                    LPDetailFragment lpDetailFragment = new LPDetailFragment();
                    lpDetailFragment.setArguments(args);
                    FragmentManager fm  = activity.getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.localProgramContainer, lpDetailFragment).addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }
    }
}
