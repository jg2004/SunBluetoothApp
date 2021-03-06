package com.sunelectronics.sunbluetoothapp.models;


import android.support.annotation.NonNull;

public class LogFileObject implements Comparable<LogFileObject> {

    private String fileName;
    private long fileSize;

    public LogFileObject(String fileName, long fileSize) {

        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSizeAsString() {
        double num;

        if (fileSize < 1000) {
            return String.valueOf(fileSize) + " B";
        } else if (fileSize < 10000000) {
            num = fileSize / 1000d;
            num = Math.round(num * 10.0) / 10.0;
            return String.valueOf(num) + " KB";

        } else if (fileSize < 1000000000) {
            num = fileSize / 1000000d;
            num = Math.round(num * 10.0) / 10.0;
            return String.valueOf(num) + " MB";
        } else {
            num = fileSize / 1000000000d;
            num = Math.round(num * 10.0) / 10.0;
            return String.valueOf(num) + " GB";
        }
    }

    @Override
    public int compareTo(@NonNull LogFileObject o) {

        String year = o.getFileName().substring(6, 10);
        String day = o.getFileName().substring(3, 5);
        String month = o.getFileName().substring(0, 2);
        String time = o.getFileName().substring(11, 17);
        StringBuilder sb;
        sb = new StringBuilder();
        String oName = sb.append(year).append(month).append(day).append(time).toString();
        year = getFileName().substring(6, 10);
        day = getFileName().substring(3, 5);
        month = getFileName().substring(0, 2);
        time = getFileName().substring(11, 17);
        StringBuilder sb2;
        sb2 = new StringBuilder();
        String thisName = sb2.append(year).append(month).append(day).append(time).toString();
        return thisName.compareTo(oName);
    }
}
