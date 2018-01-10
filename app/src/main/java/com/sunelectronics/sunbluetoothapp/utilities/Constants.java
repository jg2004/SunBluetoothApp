package com.sunelectronics.sunbluetoothapp.utilities;


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

    //Titles for fragments/activities
    public static final String DISPLAY_TEMP_FRAG_TITLE = "CHAMBER MONITOR";
    public static final String LOCAL_PROGRAM_LIST_FRAG_TITLE = "STORED LP's";
    public static final String LOG_FILE_LIST_FRAG_TITLE = "LOG FILES";
    public static final String CHAMBER_STATUS_FRAG_TITLE = "CHAMBER STATUS";
    public static final String LP_DETAIL_FRAG_TITLE = "LP DETAIL";
    public static final String OUTPUT_FRAG_TITLE = "CHAMBER OUTPUTS";
    public static final String PIDA_FRAG_TITLE = "ADVANCED PID MODE";
    public static final String PARAMETER_FRAG_TITLE = "CONTROLLER PARAMETERS";
    public static final String LP_DOWNLOAD_FRAG_TITLE = "LP MODE";
    public static final String LOG_FILE_VIEWER_TITLE = "FILE VIEWER";
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
    public static final String ALERT_NOTIFICATION = "alert_notification";
    public static final String LP = "lp";
    public static final int POWER_ON_DELAY_MS = 2000;
    public static final String LOG_FILE_CONTENTS = "log_file_contents";
    public static final String FILE_CONTENTS = "file_contents";
    public static final String EMPTY_LOG_FILE_CONTENTS = "Log file empty";
    public static final String LOG_FILE_NAME = "log_file_name";
    public static final String LOG_FILES_DIRECTORY = "logFiles";
    public static final String DELETE_LOG_FILE = "delete_log_file";
    public static final String DELETE_ALL_LOG_FILES = "delete_all_log_files";
    public static final String LOGGING_STATE = "logging_state";
    public static final String TURN_OFF_CHAMBER = "TURN OFF CHAMBER";
    public static final String TERMINATE_LOGGING_SESSION = "TERMINATE LOGGING SESSION";
    public static final String EXIT_APP = "EXIT APPLICATION";
    public static final String START_DISCOVERY = "start_discovery";
    public static final String CONNECTION_LOST = "connection_lost";
    public static final String UPDATE_BT_STATE = "update_bt_state";
    public static final String VER_LESS_KITKAT_MESSAGE = "VER 4.3 OR BELOW";

    //Fragment TAGS
    public static final String TAG_LP_DETAIL_FRAG = "lp_detail_frag";
    public static final String TAG_FRAGMENT_CHAMBER_STATUS = "chambers_status_fragment";
    public static final String TAG_FRAGMENT_OUTPUT = "output_fragment";
    public static final String TAG_FRAGMENT_PARAMETER = "parameter_fragment";
    public static final String TAG_FRAGMENT_PIDA_MODE = "pidA_mode_fragment";
    public static final String TAG_FRAGMENT_HELP_DIALOG = "help_dialog_fragment";

    //chamber commands
    public static final String PIDH_COMMAND = "PIDH=";
    public static final String PIDC_COMMAND = "PIDC=";
    public static final String PWMP_COMMAND = "PWMP=";
    public static final String UTL_COMMAND = "UTL=";
    public static final String LTL_COMMAND = "LTL=";
    public static final String PIDH_QUERY = "PIDH?";
    public static final String PIDC_QUERY = "PIDC?";
    public static final String PWMP_QUERY = "PWMP?";
    public static final String UTL_QUERY = "UTL?";
    public static final String LTL_QUERY = "LTL?";
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
    public static final String OUT0_COMMAND_PREFIX = "OUT0:";
    public static final String OUT_COMMAND_ON = ",1";
    public static final String OUT_COMMAND_OFF = ",0";
    public static final String OUT3_COMMAND_PREFIX = "OUT3:";

    //command descriptions
    public static final String PIDA_MODE_0 = "CONTROL TO CHAMBER PROBE";
    public static final String PIDA_MODE_1 = "CONTROL TO AVG OF CHAMBER, USER PROBE";
    public static final String PIDA_MODE_2 = "CONTROL TO CHAM, THEN SLOWLY FORCE USER TO SET";
    public static final String PIDA_MODE_3 = "CONTROL TO USER PROBE";
    public static final String PIDA_MODE_4 = "CONTROL TO AVG, THEN SLOWLY FORCE USER TO SET";
    public static final String ANALOG_0 = "0";
    public static final String ANALOG_1 = "1";
    public static final String ANALOG_2 = "2";
    public static final String ANALOG_3 = "3";

}
