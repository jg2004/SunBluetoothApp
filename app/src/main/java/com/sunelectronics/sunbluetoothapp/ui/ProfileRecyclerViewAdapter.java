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
import com.sunelectronics.sunbluetoothapp.fragments.MyAlertDialogFragment;
import com.sunelectronics.sunbluetoothapp.fragments.TC01ProfileDetailFragment;
import com.sunelectronics.sunbluetoothapp.models.Tc01Profile;

import java.io.Serializable;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_ICON;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TITLE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.ALERT_TYPE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_MESSAGE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.DELETE_PROFILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.PROFILE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TAG_FRAGMENT_TEMP_PROF;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TC01_INFINITY;

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ViewHolder> implements Serializable {

    private Context mContext;
    private List<Tc01Profile> mTc01ProfileList;
    private StringBuilder mStringBuilder;

    public ProfileRecyclerViewAdapter(Context context, List<Tc01Profile> profileList) {

        mContext = context;
        mTc01ProfileList = profileList;
        mStringBuilder = new StringBuilder();
    }

    @Override
    public ProfileRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProfileRecyclerViewAdapter.ViewHolder holder, int position) {

        Tc01Profile profile = mTc01ProfileList.get(position);
        holder.profileNameTv.setText(profile.getName());
        if (profile.getCycles().equals(TC01_INFINITY)) {
            mStringBuilder.append("CYCLES=INFINITY");
            holder.cyclesTv.setText(mStringBuilder.toString());
        } else {

            mStringBuilder.append("CYCLES=").append(profile.getCycles());
            holder.cyclesTv.setText(mStringBuilder.toString());
        }
        mStringBuilder.delete(0, mStringBuilder.length());

        for (int i = 0; i < 10; i++) {

            if (profile.getScanTempTimes().get("A" + i) == null || profile.getScanTempTimes().get("A" + i).isEmpty()) {
                mStringBuilder.append("SET=NONE");
                holder.scanTemp[i].setText(mStringBuilder.toString());
            } else {
                mStringBuilder.append("SET= ").append(profile.getScanTempTimes().get("A" + i)).append(" C");
                holder.scanTemp[i].setText(mStringBuilder.toString());
            }
            mStringBuilder.delete(0, mStringBuilder.length());
        }
        for (int i = 0; i < 10; i++) {

            if (profile.getScanTempTimes().get("B" + i) == null || profile.getScanTempTimes().get("B" + i).isEmpty()) {
                mStringBuilder.append("WAIT=FOREV");
                holder.scanTime[i].setText(mStringBuilder.toString());
            } else {
                mStringBuilder.append("WAIT= ").append(profile.getScanTempTimes().get("B" + i)).append(" M");
                holder.scanTime[i].setText(mStringBuilder.toString());
            }
            mStringBuilder.delete(0, mStringBuilder.length());
        }
    }

    @Override
    public int getItemCount() {
        return mTc01ProfileList.size();
    }

    public void setProfileList(List<Tc01Profile> profileList) {
        mTc01ProfileList = profileList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView[] scanTemp = new TextView[10];
        TextView[] scanTime = new TextView[10];
        TextView profileNameTv, cyclesTv;

        ViewHolder(View view) {
            super(view);
            profileNameTv = (TextView) view.findViewById(R.id.profileNameTextView);
            cyclesTv = (TextView) view.findViewById(R.id.cycles);
            scanTemp[0] = (TextView) view.findViewById(R.id.scanTemp0);
            scanTemp[1] = (TextView) view.findViewById(R.id.scanTemp1);
            scanTemp[2] = (TextView) view.findViewById(R.id.scanTemp2);
            scanTemp[3] = (TextView) view.findViewById(R.id.scanTemp3);
            scanTemp[4] = (TextView) view.findViewById(R.id.scanTemp4);
            scanTemp[5] = (TextView) view.findViewById(R.id.scanTemp5);
            scanTemp[6] = (TextView) view.findViewById(R.id.scanTemp6);
            scanTemp[7] = (TextView) view.findViewById(R.id.scanTemp7);
            scanTemp[8] = (TextView) view.findViewById(R.id.scanTemp8);
            scanTemp[9] = (TextView) view.findViewById(R.id.scanTemp9);
            scanTime[0] = (TextView) view.findViewById(R.id.scanTime0);
            scanTime[1] = (TextView) view.findViewById(R.id.scanTime1);
            scanTime[2] = (TextView) view.findViewById(R.id.scanTime2);
            scanTime[3] = (TextView) view.findViewById(R.id.scanTime3);
            scanTime[4] = (TextView) view.findViewById(R.id.scanTime4);
            scanTime[5] = (TextView) view.findViewById(R.id.scanTime5);
            scanTime[6] = (TextView) view.findViewById(R.id.scanTime6);
            scanTime[7] = (TextView) view.findViewById(R.id.scanTime7);
            scanTime[8] = (TextView) view.findViewById(R.id.scanTime8);
            scanTime[9] = (TextView) view.findViewById(R.id.scanTime9);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tc01Profile profile = mTc01ProfileList.get(getAdapterPosition());
                    HomeActivity homeActivity = (HomeActivity) mContext;
                    TC01ProfileDetailFragment fragment = (TC01ProfileDetailFragment) homeActivity.getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_TEMP_PROF);
                    fragment.setProfile(profile);
                    homeActivity.getSupportFragmentManager().popBackStack();
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Tc01Profile profile = mTc01ProfileList.get(getAdapterPosition());
                    Bundle args = new Bundle();
                    args.putSerializable(PROFILE, profile);
                    args.putString(ALERT_TITLE, DELETE_MESSAGE);
                    args.putString(ALERT_MESSAGE, "Ok to delete profile?");
                    args.putString(ALERT_TYPE, DELETE_PROFILE);
                    args.putInt(ALERT_ICON, R.drawable.ic_delete_black_48dp);
                    showDialog(args);
                    return true;
                }
            });
        }
    }

    private void showDialog(Bundle bundle) {

        HomeActivity homeActivity = (HomeActivity) mContext;
        MyAlertDialogFragment myAlertdialogFragment = MyAlertDialogFragment.newInstance(bundle);
        myAlertdialogFragment.show(homeActivity.getSupportFragmentManager(), null);

    }
}
