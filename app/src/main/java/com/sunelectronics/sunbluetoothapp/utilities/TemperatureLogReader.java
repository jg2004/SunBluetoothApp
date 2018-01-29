package com.sunelectronics.sunbluetoothapp.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sunelectronics.sunbluetoothapp.models.LogFileObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILES_DIRECTORY;

public class TemperatureLogReader {

    private static final String TAG = "TemperatureLogReader";
    private Context mContext;

    public TemperatureLogReader(Context context) {
        mContext = context;
    }

    public boolean deleteFile(String fileName) {

        File file = new File(mContext.getFilesDir() + File.separator + LOG_FILES_DIRECTORY, fileName);

        if (file.exists()) {
            return file.delete();
        } else {
            Toast.makeText(mContext, "File does not exist", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public boolean deleteAllFilesInLogFilesDirectory() {

        File logFilesDirectory = new File(mContext.getFilesDir() + File.separator + LOG_FILES_DIRECTORY);
        boolean allFilesDeleted = true;
        for (File file : logFilesDirectory.listFiles()) {

            Log.d(TAG, "deleteAllFilesInLogFilesDirectory: deleting all files!");
            if (!file.delete()) {
                Log.d(TAG, "deleteAllFilesInLogFilesDirectory: file " + file.getName() + " could not be deleted");
                allFilesDeleted = false;
            }
        }
        return allFilesDeleted;
    }

    public List<LogFileObject> getLogFileObjects() {

        ArrayList<LogFileObject> fileObjectList = new ArrayList<>();
        File logFilesDirectory = new File(mContext.getFilesDir() + File.separator + LOG_FILES_DIRECTORY);
        Log.d(TAG, "getLogFileObjects: created file: " + logFilesDirectory.getName());
        File[] files = logFilesDirectory.listFiles();
        if (files != null) {
            for (File f : files) {
                fileObjectList.add(new LogFileObject(f.getName(), f.length()));
                Log.d(TAG, "getLogFileObjects: added " + f.getName() + " to list");
            }
        }
        return fileObjectList;
    }

    public String getFileContents(String fileName) {

        String fileContents;
        File file = new File(mContext.getFilesDir() + File.separator + LOG_FILES_DIRECTORY + File.separator + fileName);

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String tempString;
            StringBuilder stringBuilder = new StringBuilder();

            while ((tempString = br.readLine()) != null) {

                stringBuilder.append(tempString).append("\n");
            }
            br.close();
            fileContents = stringBuilder.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "getFileContents: file: " + file.getName() + " not found");
            fileContents = "FILE NOT FOUND";

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "getFileContents: I/O exception");
            fileContents = "ERROR OPENING FILE";
        }
        return fileContents;
    }
}
