package com.sunelectronics.sunbluetoothapp.models;


import java.io.Serializable;

public class ChamberModel implements Serializable {

    private String ch1Command;
    private String ch2Command;
    private String ch1Reading;
    private String ch2Reading;
    private String setCommand;
    private String setReading;
    private long timeStamp;

    public ChamberModel() {
        ch1Command = "CHAM";
        ch2Command = "USER";
        setCommand = "SET";
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCh1Reading() {
        return ch1Reading;
    }

    public String getCh1Command() {
        return ch1Command;
    }

    public String getCh2Command() {
        return ch2Command;
    }

    public String getSetCommand() {
        return setCommand;
    }

    public void setCh1Reading(String ch1Reading) {
        this.ch1Reading = ch1Reading;
    }

    public String getCh2Reading() {
        return ch2Reading;
    }

    public void setCh2Reading(String ch2Reading) {
        this.ch2Reading = ch2Reading;
    }

    public String getSetReading() {
        return setReading;
    }

    public void setSetReading(String setReading) {
        this.setReading = setReading;
    }
}
