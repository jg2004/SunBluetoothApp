package com.sunelectronics.sunbluetoothapp.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.models.ChamberModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILES_DIRECTORY;

public class TemperatureLogWriter {

    private static final String TAG = "TemperatureLogWriter";
    private static final String FORMATTER = "%02d";
    private static final String NEW_LINE = "\r\n";
    private static final String SPACE = " ";
    private BufferedWriter mBufferedWriter;
    private Context mContext;
    private ChamberModel mChamberModel;

    public TemperatureLogWriter(Context context, ChamberModel chamberModel) {

        Log.d(TAG, "TemperatureLogWriter: creating a new TemperatureLogWriter");
        mContext = context;
        mChamberModel = chamberModel;
        mBufferedWriter = getBufferedWriter();

    }

    /**
     * Builds a file name based on the date and time such as 11_18_2017_143503.txt to ensure a unique
     * file name. Creates a logFiles folder in internal storage, creates an Output stream writer
     * and writes a file header (this is just date and time file created). logger will write temperature
     * data to this text file.
     *
     * @return BufferedWriter
     */
    private BufferedWriter getBufferedWriter() {

        Calendar calendar = Calendar.getInstance();
        String month = String.format(Locale.ENGLISH, FORMATTER, calendar.get(Calendar.MONTH) + 1);
        String dayOfMonth = String.format(Locale.ENGLISH, FORMATTER, calendar.get(Calendar.DAY_OF_MONTH));
        String year = String.format(Locale.ENGLISH, FORMATTER, calendar.get(Calendar.YEAR));
        String hour = String.format(Locale.ENGLISH, FORMATTER, calendar.get(Calendar.HOUR_OF_DAY));
        String minute = String.format(Locale.ENGLISH, FORMATTER, calendar.get(Calendar.MINUTE));
        String second = String.format(Locale.ENGLISH, FORMATTER, calendar.get(Calendar.SECOND));

        StringBuilder stringBuilderForFileHeader;
        stringBuilderForFileHeader = new StringBuilder();
        stringBuilderForFileHeader.append(month)
                .append("/").append(dayOfMonth).append("/").append(year).append(SPACE)
                .append(hour).append(":").append(minute).append(":").append(second).append(NEW_LINE)
                .append("TIME").append(",").append(mChamberModel.getCh1Command()).append(",")
                .append(mChamberModel.getCh2Command()).append(",")
                .append(mChamberModel.getSetCommand()).append(NEW_LINE);
        String fileHeader = stringBuilderForFileHeader.toString();

        StringBuilder stringBuilderForFileName;
        stringBuilderForFileName = new StringBuilder();
        stringBuilderForFileName.append(month)
                .append("_").append(dayOfMonth).append("_").append(year)
                .append("_").append(hour).append(minute).append(second).append(".txt");

        String fileName = stringBuilderForFileName.toString();
        File directory = new File(mContext.getFilesDir() + File.separator + LOG_FILES_DIRECTORY);
        if (!directory.exists()) {
            //create a logFiles directory if it hasn't been created to store the log files
            Log.d(TAG, "creating logFiles directory");
            if (!directory.mkdir()) {
                //if directory cannot be created with mkdir() then log error and display with toast
                Log.d(TAG, "getBufferedWriter: could not make directory: " + directory.getName());
                Toast.makeText(mContext, "Could not make directory: " + directory.getName(), Toast.LENGTH_LONG).show();
            }
        }

        File file = new File(mContext.getFilesDir() + File.separator + LOG_FILES_DIRECTORY, fileName);
        Log.d(TAG, "TemperatureLogWriter: creating file: " + file.getPath());

        try {

            FileWriter fileWriter = new FileWriter(file,true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//            FileOutputStream fileOutputStream = new FileOutputStream(file, true);//true to append data!
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
//            outputStreamWriter.write(fileHeader);
            bufferedWriter.write(fileHeader);
            return bufferedWriter;

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

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);//HH is military time (no AM/PM)

        sb.append(sdf.format(new Date(mChamberModel.getTimeStamp()))).append(",").append(mChamberModel.getCh1Reading())
                .append(",").append(mChamberModel.getCh2Reading()).append(",")
                .append(mChamberModel.getSetReading()).append(NEW_LINE);
        try {
            Log.d(TAG, "log: writing " + sb.toString() + "to file");
            mBufferedWriter.write(sb.toString());

        } catch (IOException e) {
            Log.d(TAG, "log: error writing to file");
            e.printStackTrace();
        }
    }

    public void closeFile() {
        Log.d(TAG, "closeFile: closing file");
        try {
            mBufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "closeFile: error closing file");
        }
    }
}
