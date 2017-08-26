package com.sunelectronics.sunbluetoothapp.models;

import java.io.Serializable;

/**
 * Created by Jerry on 8/19/2017.
 */

public class LocalProgram implements Serializable {

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
