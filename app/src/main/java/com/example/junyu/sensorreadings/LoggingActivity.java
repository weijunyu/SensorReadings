package com.example.junyu.sensorreadings;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.LinearAccelerometer;
import com.aware.providers.Linear_Accelerometer_Provider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LoggingActivity extends AppCompatActivity {
    private static final String LOG_TAG = "LoggingActivity";
    private LinAccReceiver linAccReceiver;
    private Intent aware;
    private String linAccLogFileName = "lin_acc_log";
    private String logDirName = "/SensorReadings/logs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        Log.d(LOG_TAG, "Started logging activity!");

        // Should check the file directory and set linAccLogFile to a new value
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume called, registered linAccReceiver");
        super.onResume();
        // Register the sensor BroadcastReceiver
        IntentFilter linAccBroadcastFilter = new IntentFilter();
        // When new data is recorded in provider, grab it
        linAccBroadcastFilter.addAction(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);
        registerReceiver(linAccReceiver, linAccBroadcastFilter);
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause called, unregistered linAccReceiver");
        unregisterReceiver(linAccReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy called, aware service stopped");
        stopService(aware);
        super.onDestroy();
    }

    public void beginLogging(View view) {
        /**
         * 1. Add TapView to layout
         * 2. TapView starts the animations/drawings
         * 3. endLogging() called, goes back to main activity.
         */
        // Remove text and button
        TextView loggingExplanation = (TextView) this.findViewById(R.id.logging_explanation);
        Button beginLogging = (Button) this.findViewById(R.id.begin_logging);
        ((ViewManager) loggingExplanation.getParent()).removeView(loggingExplanation);
        ((ViewManager) beginLogging.getParent()).removeView(beginLogging);

        // Starts the logging
        startLinAccBroadcast();
        registerLinAccReceiver();

        ViewGroup loggingLayout = (ViewGroup) this.findViewById(R.id.logging_activity);
        TapView tapView = new TapView(this);

        loggingLayout.addView(tapView);
    }

    private void appendLog(String logFileName, String logLine) {
        // !!!Implement checks for ability to write to external storage!
        // Write data to file
        final File logDir = new File(Environment.getExternalStorageDirectory() + logDirName);
        boolean success = logDir.mkdirs();
        final File logFile = new File(logDir, logFileName);
        // logFile.getAbsolutePath() returns /storage/emulated/0/SensorReadings/logs/lin_acc_log

        if (success) {
            Log.d(LOG_TAG, "New logfile created.");
        } else {
            Log.d(LOG_TAG, "Writing to existing log file.");
        }
        try {
            Log.d(LOG_TAG, logFile.getAbsolutePath());
            // true set to append to log
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
            bufferedWriter.append(logLine);
            bufferedWriter.newLine();
            bufferedWriter.close();
            Log.d(LOG_TAG, "Wrote to bufferedWriter");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class LinAccReceiver extends BroadcastReceiver {
        private final static String LOG_TAG = "LinAccReceiver";
        private int timeStamp;
        private double xValue, yValue, zValue;
        String logLine;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Sensor broadcast received!!!");

            Object accData = intent.getExtras().get(LinearAccelerometer.EXTRA_DATA);
            ContentValues content = (ContentValues) accData;
            if (content != null) {
                timeStamp = content.getAsInteger(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP);
                xValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0);
                yValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1);
                zValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2);
                Log.d(LOG_TAG, "Logging content!");

                logLine = timeStamp + "," + xValue + "," + yValue + "," + zValue;
                appendLog(linAccLogFileName, logLine);
            }
        }
    }


    private void startLinAccBroadcast() {
        // Initialise AWARE
        aware = new Intent(this, Aware.class);
        startService(aware);
        // Activate sensors, set settings.
        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_NORMAL);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    private void registerLinAccReceiver() {
        // Create and register a sensorBroadcastReceiver
        linAccReceiver = new LinAccReceiver();
        IntentFilter linAccBroadcastFilter = new IntentFilter();
        // When new data is recorded in provider, grab it
        linAccBroadcastFilter.addAction(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);
        registerReceiver(linAccReceiver, linAccBroadcastFilter);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
