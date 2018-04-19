package com.sunelectronics.sunbluetoothapp.utilities;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.sunelectronics.sunbluetoothapp.utilities.Constants.FORMATTER;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.LOG_FILES_DIRECTORY;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.NEW_LINE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.SPACE;
import static com.sunelectronics.sunbluetoothapp.utilities.Constants.TEMP_FILE;

public class ChartUtilityHelperClass {

    private static final String TAG = "ChartUtilityHelperClass";
    private static final int CHART_FONT_SIZE = 12;

    public static void saveLiveChartToFile(LineChart lineChart, Context context, long startTime, String fileName, boolean notify) {

        LineData lineData = lineChart.getLineData();

        if (lineData == null || lineData.getDataSets().isEmpty()) {

            Log.d(TAG, "saveLiveChartToFile: NO CHART DATA TO SAVE!!!");

            return;
        }
        StringBuilder sb = new StringBuilder();
        List<String> dataSetLabels = new ArrayList<>();

        for (ILineDataSet dataSet : lineData.getDataSets()) {
            dataSetLabels.add(dataSet.getLabel());
        }

        deleteFile(context, fileName);

        BufferedWriter writer = getBufferedWriter(context, dataSetLabels, startTime, fileName);

        for (int i = 0; i < lineData.getDataSets().get(0).getEntryCount(); i++) {

            sb.delete(0, sb.length());
            sb.append(String.format(Locale.ENGLISH, "%.1f", lineData.getDataSets().get(0).getEntryForIndex(i).getX()));

            for (ILineDataSet dataSet : lineData.getDataSets()) {
                dataSetLabels.add(dataSet.getLabel());
                sb.append(",");
                sb.append(dataSet.getEntryForIndex(i).getY());
            }
            sb.append(NEW_LINE);
            try {
                if (writer == null) throw new AssertionError("writer was null");
                writer.write(sb.toString());
            } catch (IOException e) {
                Toast.makeText(context, "Error saving live chart to file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
                break;
            }
        }
        //notify user that data saved to file
        if (notify) Toast.makeText(context, "Chart Saved to File!", Toast.LENGTH_SHORT).show();

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(Context context, String fileName) {
        //this deletes a temp file if it exists
        if (fileName != null && fileName.equals(TEMP_FILE)) {

            File file = new File(context.getFilesDir() + File.separator + LOG_FILES_DIRECTORY, fileName);
            if (file.exists()) {
                Log.d(TAG, "deleted temp file");
                file.delete();
            }
        }
    }

    private static BufferedWriter getBufferedWriter(Context context, List<String> labelList, Long startTime, String fileName) {

        Calendar calendarForFileName = Calendar.getInstance();
        Calendar calendarForFileHeader = Calendar.getInstance();
        calendarForFileHeader.setTimeInMillis(startTime);
        String month = String.format(Locale.ENGLISH, FORMATTER, calendarForFileHeader.get(Calendar.MONTH) + 1);
        String dayOfMonth = String.format(Locale.ENGLISH, FORMATTER, calendarForFileHeader.get(Calendar.DAY_OF_MONTH));
        String year = String.format(Locale.ENGLISH, FORMATTER, calendarForFileHeader.get(Calendar.YEAR));
        String hour = String.format(Locale.ENGLISH, FORMATTER, calendarForFileHeader.get(Calendar.HOUR_OF_DAY));
        String minute = String.format(Locale.ENGLISH, FORMATTER, calendarForFileHeader.get(Calendar.MINUTE));
        String second = String.format(Locale.ENGLISH, FORMATTER, calendarForFileHeader.get(Calendar.SECOND));

        StringBuilder stringBuilderForFileHeader;
        stringBuilderForFileHeader = new StringBuilder();
        stringBuilderForFileHeader.append(month)
                .append("/").append(dayOfMonth).append("/").append(year).append(SPACE)
                .append(hour).append(":").append(minute).append(":").append(second).append(NEW_LINE)
                .append("TIME");

        for (String label : labelList) {
            stringBuilderForFileHeader.append(",").append(label);
        }
        stringBuilderForFileHeader.append(NEW_LINE);
        String fileHeader = stringBuilderForFileHeader.toString();

        //if fileName is 'null', then build a file name from date
        if (fileName == null) {
            StringBuilder stringBuilderForFileName;
            stringBuilderForFileName = new StringBuilder();
            String monthForFileName = String.format(Locale.ENGLISH, FORMATTER, calendarForFileName.get(Calendar.MONTH) + 1);
            String dayOfMonthForFileName = String.format(Locale.ENGLISH, FORMATTER, calendarForFileName.get(Calendar.DAY_OF_MONTH));
            String yearForFileName = String.format(Locale.ENGLISH, FORMATTER, calendarForFileName.get(Calendar.YEAR));
            String hourForFileName = String.format(Locale.ENGLISH, FORMATTER, calendarForFileName.get(Calendar.HOUR_OF_DAY));
            String minutForFileName = String.format(Locale.ENGLISH, FORMATTER, calendarForFileName.get(Calendar.MINUTE));
            String secondForFileName = String.format(Locale.ENGLISH, FORMATTER, calendarForFileName.get(Calendar.SECOND));
            stringBuilderForFileName.append(monthForFileName)
                    .append("_").append(dayOfMonthForFileName).append("_").append(yearForFileName)
                    .append("_").append(hourForFileName).append(minutForFileName).append(secondForFileName).append(".txt");
            fileName = stringBuilderForFileName.toString();
        }


        File directory = new File(context.getFilesDir() + File.separator + LOG_FILES_DIRECTORY);
        if (!directory.exists()) {
            //create a logFiles directory if it hasn't been created to store the log files
            Log.d(TAG, "creating logFiles directory");
            if (!directory.mkdir()) {
                //if directory cannot be created with mkdir() then log error and display with toast
                Log.d(TAG, "getBufferedWriter: could not make directory: " + directory.getName());
                Toast.makeText(context, "Could not make directory: " + directory.getName(), Toast.LENGTH_LONG).show();
            }
        }

        File file = new File(context.getFilesDir() + File.separator + LOG_FILES_DIRECTORY, fileName);
        Log.d(TAG, "TemperatureLogWriter: creating file: " + file.getPath());

        try {

            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
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

    public static String getChartTitleFromFileContents(String fileContents) {

        String[] mLogFileContentsArray = fileContents.split("\n");
        return mLogFileContentsArray[0];
    }

    public static void formatChart(LineChart lineChart) {
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                if (value < 1) {
                    return String.format(Locale.ENGLISH, "%.2f m", value);

                } else if (value >= 1 && value < 60) {
                    return String.format(Locale.ENGLISH, "%.1f m", value);

                } else {
                    value /= 60;
                    return String.format(Locale.ENGLISH, "%.1f h", value);
                }
            }
        });
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
    }

    public static Description getFormattedDescription() {
        Description description = new Description();
        description.setTextSize(CHART_FONT_SIZE);
        return description;
    }

    public static LineData getLineDataFromFileContents(String fileContents) {

        int[] colorArray = {Color.BLUE, Color.MAGENTA, Color.BLACK, Color.CYAN, Color.DKGRAY, Color.GREEN};
        String[] mLogFileContentsArray = fileContents.split("\n");
        String[] headers = mLogFileContentsArray[1].split(",");
        int yValuesToPlot = headers.length - 1;
        yValuesToPlot = yValuesToPlot > 6 ? 6 : yValuesToPlot;//plot no more than 6 values since color array has only 6 values
        final String[] xValues = new String[mLogFileContentsArray.length - 2];

        // an arraylist of arraylist<Entry>
        ArrayList<ArrayList<Entry>> yValuesArrayList = new ArrayList<>();

        for (int i = 0; i < yValuesToPlot; i++) {
            yValuesArrayList.add(new ArrayList<Entry>());
        }

        for (int i = 0; i < mLogFileContentsArray.length - 2; i++) {

            String[] row = mLogFileContentsArray[i + 2].split(",");
            xValues[i] = row[0];
            for (int j = 0; j < yValuesToPlot; j++) {

                float temp;
                //try catch is used to catch non -numeric values such as SET=NONE
                try {
                    temp = Float.parseFloat(row[j + 1]);
                } catch (NumberFormatException e) {
                    temp = 0f;
                }
                yValuesArrayList.get(j).add(new Entry(Float.parseFloat(xValues[i]), temp));
            }
        }

        LineDataSet[] lineDataSetArray = new LineDataSet[yValuesToPlot];
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        for (int i = 0; i < yValuesToPlot; i++) {
            lineDataSetArray[i] = new LineDataSet(yValuesArrayList.get(i), headers[i + 1]);
            lineDataSetArray[i].setDrawCircles(false);
            lineDataSetArray[i].setDrawValues(false);
            lineDataSetArray[i].setColor(colorArray[i]);
            dataSets.add(lineDataSetArray[i]);
        }

        return new LineData((dataSets));
    }
}