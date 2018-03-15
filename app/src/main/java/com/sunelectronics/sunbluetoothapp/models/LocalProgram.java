package com.sunelectronics.sunbluetoothapp.models;

import com.sunelectronics.sunbluetoothapp.interfaces.Iid;

import java.io.Serializable;

/**
 * Simple POJO to store local program name, content and id assigned by database
 */

public class LocalProgram implements Serializable, Iid {

    private int id;
    private String name;
    private String content;

    public LocalProgram() {
    }

    public LocalProgram(String lpName, String lpContent) {

        this.name = lpName;
        this.content = lpContent;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
