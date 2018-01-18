package com.sunelectronics.sunbluetoothapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sunelectronics.sunbluetoothapp.R;
import com.sunelectronics.sunbluetoothapp.activities.HomeActivity;
import com.sunelectronics.sunbluetoothapp.fragments.LPDetailFragment;
import com.sunelectronics.sunbluetoothapp.models.LocalProgram;

import java.io.Serializable;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LP;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_LP_DETAIL_FRAG;

public class LPRecyclerViewAdapter extends RecyclerView.Adapter<LPRecyclerViewAdapter.ViewHolder> implements Serializable {

    private Context mContext;
    private List<LocalProgram> mLocalProgramList;


    public void setLocalProgramList(List<LocalProgram> localProgramList) {
        mLocalProgramList = localProgramList;
    }


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

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView lpNameTv, lpContentTv;

        ViewHolder(View view) {
            super(view);
            lpNameTv = (TextView) view.findViewById(R.id.lpNameTextView);
            lpContentTv = (TextView) view.findViewById(R.id.lpContentTextView);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LocalProgram localProgram = mLocalProgramList.get(getAdapterPosition());
                    Bundle args = new Bundle();
                    args.putSerializable(LP, localProgram);
                    HomeActivity homeActivity = (HomeActivity) mContext;
                    LPDetailFragment lpDetailFragment = new LPDetailFragment();
                    lpDetailFragment.setArguments(args);
                    homeActivity.getSupportFragmentManager().beginTransaction().replace(R.id.homeContainer, lpDetailFragment, TAG_LP_DETAIL_FRAG)
                            .addToBackStack(null).commit();
                }
            });
        }
    }
}
