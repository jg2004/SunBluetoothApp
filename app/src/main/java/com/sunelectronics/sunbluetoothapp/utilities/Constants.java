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
    public static final String LP_LOCATION = "lp_location";

    //constants used
    public static final String SEND_STOP = "SEND STOP COMMAND";
    public static final String DELETE_MESSAGE = "DELETE";
    public static final String DELETE_LP = "DELETE LP";
    public static final String DELETE_ALL_LP = "DELETE ALL LP";
    public static final String ALERT_TYPE = "type";
    public static final String ALERT_TITLE = "title";
    public static final String ALERT_MESSAGE = "message";
    public static final String ALERT_CONFIRM_EXIT = "exit";
    public static final String ALERT_ICON = "alert";
    public static final String LP = "lp";
    public static final int POWER_ON_DELAY_MS = 2000;
    public static final String LOG_FILE_CONTENTS = "log_file_contents";
    public static final String EMPTY_LOG_FILE_CONTENTS = "Log file empty";
    public static final String LOG_FILE_NAME = "log_file_name";
    public static final String LOG_FILES_DIRECTORY = "logFiles";
    public static final String DELETE_LOG_FILE = "delete_log_file";
    public static final String DELETE_ALL_LOG_FILES = "delete_all_log_files";
    public static final String LOGGING_STATE = "logging_state";
    public static final String TURN_OFF_CHAMBER = "TURN OFF CHAMBER";
    public static final String TERMINATE_LOGGING_SESSION = "TERMINATE LOGGING SESSION";
    public static final String EXIT_APP = "EXIT APPLICATION";

    //Fragment TAGS
    public static final String TAG_LP_DETAIL_FRAG = "lp_detail_frag";
    public static final String TAG_FRAGMENT_CHAMBER_STATUS = "chambers_status_fragment";

    //chamber commands
    public static final String BKPNTC = "BKPNTC";
    public static final String STATUS = "STATUS?";
    public static final String HON = "HON";
    public static final String HOFF = "HOFF";
    public static final String CON = "CON";
    public static final String COFF = "COFF";
    public static final String CHAM_TEMP = "CHAM?";
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String USER_TEMP = "USER?";
    public static final String SET_TEMP = "SET?";
    public static final String WAIT_TIME = "WAIT?";
    public static final String RATE = "RATE?";
    public static final String BKPNT = "BKPNT?";
    public static final String PIDA_COMMAND = "PIDA=";
    public static final String PIDA_QUERY = "PIDA?";

    //command descriptions
    public static final String PIDA_MODE_0 = "Control to chamber probe";
    public static final String PIDA_MODE_1 = "Control to average of chamber and user probe";
    public static final String PIDA_MODE_2 = "Control to chamber, then slowly force user to set";
    public static final String PIDA_MODE_3 = "Control to user probe";
    public static final String PIDA_MODE_4 = "Control to average, then slowly force user to set";

}
