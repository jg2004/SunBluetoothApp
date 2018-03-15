package com.sunelectronics.sunbluetoothapp.models;

import android.util.Log;

import com.sunelectronics.sunbluetoothapp.interfaces.Iid;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a TC01 profile with up to 10 scan temps and 10 scan times
 */

public class Tc01Profile implements Serializable, Iid {

    private int id;
    private String name;
    private Map<String, String> scanTempTimes;
    private static final String TAG = "Tc01Profile";
    private String cycles;

    public Tc01Profile(String name) {
        this.name = name;
        scanTempTimes = new HashMap<>(20); //10 scan temps, 10 scan times;
    }

    public boolean addSegment(int loc, String temp, String wait) {

        if (loc >= 0 && loc <= 9) {
            String scanTempLocation = "A" + loc;
            String scanTimeLocation = "B" + loc;
            scanTempTimes.put(scanTempLocation, temp);
            scanTempTimes.put(scanTimeLocation, wait);
            Log.d(TAG, "addSegment: added scan temp " + scanTempLocation + ", " + temp +
                    ", scan time: " + scanTimeLocation + ", " + wait + " to hashMap");
            return true;
        } else {
            Log.d(TAG, "addSegment: invalid location: " + loc + " segment not added to map");
            return false;
        }
    }

    public String getCycles() {
        return cycles;
    }

    public void setCycles(String cycles) {
        this.cycles = cycles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getScanTempTimes() {
        return scanTempTimes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Profile: ");
        sb.append("\n");
        for (String s : scanTempTimes.keySet()) {

            sb.append(s);
            sb.append(": ");
            sb.append(scanTempTimes.get(s));
            sb.append("\n");
        }
        return sb.toString();
    }
}
