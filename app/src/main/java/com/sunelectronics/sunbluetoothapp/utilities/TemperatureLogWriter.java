package com.sunelectronics.sunbluetoothapp.utilities;

import android.content.Context;
import android.util.Log;

import com.sunelectronics.sunbluetoothapp.models.ChamberModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TemperatureLogWriter {

    private static final String TAG = "TemperatureLogWriter";
    private static final String FORMATTER = "%02d";
    public static final String NEW_LINE = "\r\n";
    public static final String SPACE = " ";
    private OutputStreamWriter mOutputStreamWriter;
    private Context mContext;
    private ChamberModel mChamberModel;

    public TemperatureLogWriter(Context context, ChamberModel chamberModel) {

        mContext = context;
        mChamberModel = chamberModel;
        mOutputStreamWriter = getOutputStreamWriter();

    }

    /**
     * Builds a file name based on the date and time such as 11_18_2017_143503.txt to ensure a unique
     * file name. Creates a logFiles folder in internal storage, creates an Output stream writer
     * and writes a file header (this is just date and time file created). logger will write temperature
     * data to this text file.
     *
     * @return OutputStreamWriter
     */
    private OutputStreamWriter getOutputStreamWriter() {

        Calendar calendar = Calendar.getInstance();
        String month = String.format(FORMATTER, calendar.get(Calendar.MONTH) + 1);
        String dayOfMonth = String.format(FORMATTER, calendar.get(Calendar.DAY_OF_MONTH));
        String year = String.format(FORMATTER, calendar.get(Calendar.YEAR));
        String hour = String.format(FORMATTER, calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.format(FORMATTER, calendar.get(Calendar.MINUTE));
        String second = String.format(FORMATTER, calendar.get(Calendar.SECOND));

        StringBuilder stringBuilderForFileHeader = new StringBuilder();
        stringBuilderForFileHeader.append(month)
                .append("/").append(dayOfMonth).append("/").append(year).append(",")
                .append(hour).append(":").append(minute).append(":").append(second).append(NEW_LINE)
                .append(mChamberModel.getCh1Command()).append(",").append(mChamberModel.getCh2Command()).append(SPACE)
                .append(mChamberModel.getSetCommand()).append(",").append("TIME").append(NEW_LINE);
        String fileHeader = stringBuilderForFileHeader.toString();

        StringBuilder stringBuilderForFileName = new StringBuilder();
        stringBuilderForFileName.append(month)
                .append("_").append(dayOfMonth).append("_").append(year)
                .append("_").append(hour).append(minute).append(second).append(".txt");

        String fileName = stringBuilderForFileName.toString();
        // File file = new File(mContext.getFilesDir() + File.separator + "logFiles", fileName);
        File directory = new File(mContext.getFilesDir() + File.separator + "logFiles");
        directory.delete();
        if (!directory.exists()) {
            //create a logFiles directory if it hasn't been created to store the log files
            Log.d(TAG, "creating logFiles directory");
            directory.mkdir();
        }

        File file = new File(mContext.getFilesDir() + File.separator + "logFiles", fileName);
        Log.d(TAG, "TemperatureLogWriter: creating file: " + file.getPath());

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);//true to append data!
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(fileHeader);
            return outputStreamWriter;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "TemperatureLogWriter: file not found: " + file.getName());
        } catch (IOException e) {
            Log.d(TAG, "TemperatureLogWriter: I/O exception writing to file: " + file.getName());
            e.printStackTrace();
        }

        return null;
    }

    public void log() {

        Log.d(TAG, "log: writing to file");
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

        sb.append(mChamberModel.getCh1Reading()).append(",").append(mChamberModel.getCh2Reading()).append(",")
                .append(mChamberModel.getSetReading()).append(",").append(sdf.format(new Date(mChamberModel.getTimeStamp())))
                .append(NEW_LINE);
        try {
            mOutputStreamWriter.write(sb.toString());

        } catch (IOException e) {
            Log.d(TAG, "log: error writing to file");
            e.printStackTrace();
        }
    }

    public void closeFile() {
        Log.d(TAG, "closeFile: closing file");
        try {
            mOutputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "closeFile: error closing file");
        }
    }
}
