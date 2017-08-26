package com.sunelectronics.sunbluetoothapp.utilities;

/**
 * Created by Jerry on 8/19/2017.
 */

public class Constants {

    public static final String LP_DB_NAME = "localPrograms.db";
    public static final String LP_TABLE = "lp_table";
    public static final int LP_DB_VER = 1;

    public static final String SAMPLE_LP_NAME = "LP1";
    public static final String SAMPLE_LP = "FOR I0 = 0,10\nRATE=20\nWAIT=00:30:00\n" +
            "SET=-50\nWAIT=00:30:00\nSET=125\n" +
            "NEXT I0\nWAIT=5\nSET=25\nEND";


    // Columns:

    public static final String LP_ID = "_id";
    public static final String LP_NAME = "lp_name";
    public static final String LP_CONTENT = "lp_content";
}
