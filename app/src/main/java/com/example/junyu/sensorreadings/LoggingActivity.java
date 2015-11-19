package com.example.junyu.sensorreadings;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.os.Process;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    private static String selectedHand = "";
    private LinAccReceiver linAccReceiver;
    private Intent aware;
    private String linAccLogFileName = "lin_acc_log";
    private String logDirName = "/SensorReadings/logs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        selectedHand = intent.getStringExtra(MainActivity.HAND_SELECT_EXTRA);

        setContentView(R.layout.activity_logging);

        // TODO: Should check the file directory and set linAccLogFile to a new value
    }

    public void beginLogging(View view) {
        /**
         * 1. Adds TapView to layout
         * 2. TapView starts the animations/drawings
         */
        // Remove text and button
        TextView loggingExplanation = (TextView) this.findViewById(R.id.logging_explanation);
        Button beginLogging = (Button) this.findViewById(R.id.begin_logging);
        loggingExplanation.setVisibility(View.GONE);
        beginLogging.setVisibility(View.GONE);

        // Starts the logging
        startLinAccBroadcast();
        registerLinAccReceiver();

        // Add TapView to layout
        ViewGroup loggingLayout = (ViewGroup) this.findViewById(R.id.logging_activity);
        TapView tapView = new TapView(this, selectedHand);
        loggingLayout.addView(tapView);
    }

    private void startLinAccBroadcast() {
        // Initialise AWARE
        aware = new Intent(this, Aware.class);
        startService(aware);
        // Activate sensors, set settings.
        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 200000);
        Aware.startSensor(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER);
    }

    private void registerLinAccReceiver() {
        HandlerThread handlerThread = new HandlerThread(
                "LogWritingThread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler loggingHandler = new Handler(looper);

        // Create and register a sensorBroadcastReceiver
        linAccReceiver = new LinAccReceiver();
        IntentFilter linAccBroadcastFilter = new IntentFilter();
        // When new data is recorded in provider, grab it
        linAccBroadcastFilter.addAction(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);
        registerReceiver(linAccReceiver, linAccBroadcastFilter, null, loggingHandler);
    }

    // Callback when logging is complete.
    public void doneLogging(View view) {
        Log.d(LOG_TAG, "Done logging; unregistering linAccReceiver, stopping aware service");
        unregisterReceiver(linAccReceiver);
        stopService(aware);
        NavUtils.navigateUpFromSameTask(this);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }


    public class LinAccReceiver extends BroadcastReceiver {
        private final static String LOG_TAG = "LinAccReceiver";
        private int timeStamp;
        private double xValue, yValue, zValue;
        String logLine;

        @Override
        public void onReceive(Context context, Intent intent) {
            Object accData = intent.getExtras().get(LinearAccelerometer.EXTRA_DATA);
            ContentValues content = (ContentValues) accData;
            if (content != null) {
                timeStamp = content.getAsInteger(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP);
                xValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0);
                yValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1);
                zValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2);
                Log.d(LOG_TAG, "Logging content!");
                // This should be correct:
                // TextView sensorValues = (TextView) LoggingActivity.this.findViewById(R.id.sensor_values);
                // sensorValues.setText(xValue + ", " + yValue + ", " + zValue);

                logLine = timeStamp + "," + xValue + "," + yValue + "," + zValue;
                appendLog(linAccLogFileName, logLine);
            }
        }
    }

    private void appendLog(String logFileName, String logLine) {
        if (isExternalStorageWritable()) {
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
        } else {
            Toast.makeText(this, "Could not write to log file", Toast.LENGTH_SHORT).show();
            NavUtils.navigateUpFromSameTask(this);
        }
    }
}
