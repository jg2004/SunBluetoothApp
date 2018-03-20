package com.sunelectronics.sunbluetoothapp.utilities;


public class Constants {

    public static final String LP_DB_NAME = "localPrograms.db";
    public static final String TC01_PROF_DB_NAME = "tc01Profiles.db";
    public static final String TC01_PROF_TABLE = "PROFILE";
    public static final String LP_TABLE = "lp_table";
    public static final int LP_DB_VER = 1;
    public static final int TC01_DB_VER = 1;
    public static final String SAMPLE_LP_NAME = "LP1";
    public static final String SAMPLE_LP = "FOR I0 = 0,10\nRATE=20\nWAIT=00:30:00\n" +
            "SET=-50\nWAIT=00:30:00\nSET=125\n" +
            "NEXT I0\nWAIT=5\nSET=25\nEND";

    // Columns:

    public static final String PROFILE_ID = "_id";
    public static final String LP_NAME = "lp_name";
    public static final String LP_CONTENT = "lp_content";

    //Titles for fragments/activities
    public static final String LOCAL_PROGRAM_LIST_FRAG_TITLE = "STORED LP's";
    public static final String PROFILE_LIST_FRAG_TITLE = "PROFILES";
    public static final String LOG_FILE_LIST_FRAG_TITLE = "LOG FILES";
    public static final String CHAMBER_STATUS_FRAG_TITLE = "CHAMBER STATUS";
    public static final String LP_DETAIL_FRAG_TITLE = "LP DETAIL";
    public static final String PIDA_FRAG_TITLE = "ADVANCED PID MODE";
    public static final String LP_DOWNLOAD_FRAG_TITLE = "LP MODE";
    public static final String LOG_FILE_VIEWER_TITLE = "FILE VIEWER";
    public static final String SCAN_MODE_TITLE = "SCAN MODE";
    //constants used
    public static final String TC01_NO_SET_POINT = "NONE";
    public static final String TC01_INFINITY_WAIT_TIME = "FOREV";
    public static final String TC01_INFINITY = "1999.9";
    public static final String CONTROLLER_TYPE = "controller_type";
    public static final String SWITCH_STATE = "switch_state";
    public static final String SEND_STOP = "SEND STOP COMMAND";
    public static final String CHART_TITLE = "TEMPERATURE CHART";
    public static final String FILE_NAME = "file_name";
    public static final String CHART_DATA = "chart_data";
    public static final String TEMP_CONTROLLER = "chamber_model";
    public static final String DELETE_MESSAGE = "DELETE";
    public static final String DELETE_LP = "DELETE LP";
    public static final String DELETE_PROFILE = "DELETE PROFILE";
    public static final String DELETE_ALL_PROFILES = "DELETE ALL PROFILES";
    public static final String ALERT_TYPE = "type";
    public static final String ALERT_TITLE = "title";
    public static final String DIALOG_TITLE = "title";
    public static final String ALERT_MESSAGE = "message";
    public static final String ALERT_CONFIRM_EXIT = "exit";
    public static final String ALERT_ICON = "alert";
    public static final String ALERT_NOTIFICATION = "alert_notification";
    public static final String LP = "lp";
    public static final String PROFILE = "profile";
    public static final int POWER_ON_DELAY_MS = 2000;
    public static final int DELAY_2000MS = 2000;
    public static final String FILE_CONTENTS = "file_contents";
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
    public static final String TC01 = "tc01";
    public static final String EC1X = "ec1x";
    public static final String PC1000 = "pc1000";
    public static final String PC100 = "pc100";
    public static final String PC100_2 = "pc100_2";
    public static final String TC02 = "tc02";
    public static final String EC127 = "ec127";
    public static final String TC01_NAME = "TC01";
    public static final String PC100_NAME = "PC100";
    public static final String TC02_NAME = "TC02";
    public static final String EC1X_NAME = "EC1x";
    public static final String PC1000_NAME = "PC1000";
    public static final String EC127_NAME = "EC127";
    public static final String PC100_2_NAME = "PC100-2";
    public static final String CONTROLLER = "controller";
    public static final String EC1X_RS_ECHO_MESSAGE = "RS232 ECHO ON! Go to SDEF MENU and set RS Char Echo to N";
    public static final String CONTROLLER_RS_ECHO_MESSAGE = "RS232 ECHO ON! Go to MENU and set RS Char Echo to N";


    //Fragment TAGS
    public static final String TAG_LP_DETAIL_FRAG = "lp_detail_frag";
    public static final String TAG_FRAGMENT_CHAMBER_STATUS = "chambers_status_fragment";
    public static final String TAG_FRAGMENT_OUTPUT = "output_fragment";
    public static final String TAG_FRAGMENT_PARAMETER = "parameter_fragment";
    public static final String TAG_FRAGMENT_PIDA_MODE = "pidA_mode_fragment";
    public static final String TAG_FRAGMENT_HELP_DIALOG = "help_dialog_fragment";
    public static final String TAG_FRAGMENT_TEMP_CHART = "temp_chart_fragment";
    public static final String TAG_FRAGMENT_LOF_FILE_VIEWER = "log_file_viewer_fragment";
    public static final String TAG_FRAGMENT_INTRO_FRAGMENT = "intro_fragment";
    public static final String TAG_FRAGMENT_PROFILE_LIST = "profile_list_fragment";
    public static final String TAG_FRAGMENT_MONITOR = "tag_frag_monitor";
    public static final String TAG_FRAGMENT_TEMP_PROF = "tag_frag_prof";
    public static final String TAG_FRAGMENT_LOGGER = "tag_frag_logger";


    //controller commands
    public static final String TC10_STOP_COMMAND = "STOP";
    public static final String TC_RATE_COMMAND = "RATE=";
    public static final String TC_WAIT_COMMAND = "WAIT=";
    public static final String TC_SET_COMMAND = "SET=";
    public static final String PC_RATE_COMMAND = "RATE1=";
    public static final String PC_WAIT_COMMAND = "WAIT1=";
    public static final String PC_SET_COMMAND = "SET1=";

    public static final String PIDH_COMMAND = "PIDH=";
    public static final String PIDC_COMMAND = "PIDC=";
    public static final String PWMP_COMMAND = "PWMP=";
    public static final String UTL_COMMAND = "UTL=";
    public static final String LTL_COMMAND = "LTL=";
    public static final String PC1000_UTL_COMMAND = "UTL1=";
    public static final String PC1000_LTL_COMMAND = "LTL1=";
    public static final String PIDH_QUERY = "PIDH?";
    public static final String PIDC_QUERY = "PIDC?";
    public static final String PC1000_PIDH_QUERY = "PID1+?";
    public static final String PC1000_PIDC_QUERY = "PID1-?";
    public static final String PC1000_PIDH_COMMAND = "PID1+=";
    public static final String PC1000_PIDC_COMMAND = "PID1-=";
    public static final String PC1000_COOL_ENABLE_COMMAND = " C1ON-";
    public static final String PC1000_HEAT_ENABLE_COMMAND = " C1ON+";
    public static final String PC1000_COOL_DISABLE_COMMAND = " C1OFF-";
    public static final String PC1000_HEAT_DISABLE_COMMAND = " C1OFF+";
    public static final String TEMP_QUERY_COMMAND = "TEMP?";
    public static final String EC1X_CH2_QUERY_COMMAND = "UCHAN?";
    public static final String TC10_WAIT_QUERY_COMMAND = "WAIT?";
    public static final String TC10_RATE_QUERY_COMMAND = "RATE?";
    public static final String TC10_SET_QUERY_COMMAND = "SET?";
    public static final String CHAMBER_READING_LABEL = "CHAM";
    public static final String USER_READING_LABEL = "USER";
    public static final String PC_QUERY_COMMAND = "C1?";
    public static final String CH2_QUERY_COMMAND = "C2?";
    public static final String PC_WAIT_QUERY_COMMAND = "WAIT1?";
    public static final String PC_RATE_QUERY_COMMAND = "RATE1?";
    public static final String PC_SET_QUERY_COMMAND = "SET1?";
    public static final String CH1_READING_LABEL = "CH1";
    public static final String CH2_READING_LABEL = "CH2";
    public static final String CH1_QUERY_COMMAND = "TEMP?";
    public static final String RATE_QUERY_COMMAND = "RATE?";
    public static final String WAIT_QUERY_COMMAND = "WAIT?";
    public static final String SET_QUERY_COMMAND = "SET?";
    public static final String CH1_LABEL = "TEMP";
    public static final String COOL_ENABLE_COMMAND = "CON";
    public static final String HEAT_ENABLE_COMMAND = "HON";
    public static final String COOL_DISABLE_COMMAND = "COFF";
    public static final String HEAT_DISABLE_COMMAND = "HOFF";
    public static final String PWMP_QUERY = "PWMP?";
    public static final String UTL_QUERY = "UTL?";
    public static final String LTL_QUERY = "LTL?";
    public static final String PC1000_UTL_QUERY = "UTL1?";
    public static final String PC1000_LTL_QUERY = "LTL1?";

    public static final String BKPNTC = "BKPNTC";
    public static final String STATUS = "STATUS?";
    public static final String ON = "ON";
    public static final String OFF = "OFF";
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

    //TC01 commands
    public static final String TC01_UTL_INT = "O";
    public static final String TC01_RESET_COMMAND = "R";
    public static final String TC01_TEMP_QUERY = "T";
    public static final String TC01_WAIT_QUERY = "M";
    public static final String TC01_SET_QUERY = "C";
    public static final String TC01_CYCLE_QUERY = "B-";
    public static final String TC01_STOP_SCANMODE = "BA";
    public static final String TC01_START_SCANMODE = "AB";
    public static final String TC01_CMD_ERROR = "CMD ERROR";
    public static final String TC01_PID_QUERY = "PID?";
    public static final String TC01_PID_COMMAND = "PID=";
    public static final String TC01_UTL_QUERY = "UTL";
    public static final String TC01_OPT_QUERY = "OPT";
    public static final String TC01_READ_INPUT_COMMAND = "IN1";
    public static final String TC01_385_RTD = ".385RTD";
    public static final String TC01_392_RTD = ".392RTD";
    public static final char TC01_T_TC = 'T';
    public static final char TC01_J_TC = 'J';
    public static final char TC01_K_TC = 'K';

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