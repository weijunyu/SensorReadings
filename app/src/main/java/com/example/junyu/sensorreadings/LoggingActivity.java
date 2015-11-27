package com.example.junyu.sensorreadings;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Gyroscope;
import com.aware.LinearAccelerometer;
import com.aware.providers.Gyroscope_Provider;
import com.aware.providers.Linear_Accelerometer_Provider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class LoggingActivity extends AppCompatActivity {
    private static final String LOG_TAG = "LoggingActivity";
    private static String selectedHand = "";
    private LinAccReceiver linAccReceiver;
    private GyroReceiver gyroReceiver;
    private Intent aware;

    File linAccLogDir;
    File gyroLogDir;
    private String logDirName = "/SensorReadings/logs";
    private String linAccDirName = "/lin_acc";
    private String gyroDirName = "/gyro";
    private String linAccLogFileName = "";
    private String gyroLogFilename = "";

    private SensorManager sensorManager;
    private SensorEventListener sensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        selectedHand = intent.getStringExtra(MainActivity.HAND_SELECT_EXTRA);

        setContentView(R.layout.activity_logging);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setLogNumber();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLogging();
    }

    private void setLogNumber() {
        // Find the linear accelerometer directory
        if (isExternalStorageWritable()) {
            int logNum;
            File logDir = new File(
                    Environment.getExternalStorageDirectory() + logDirName);
            // '/storage/emulated/0' + '/SensorReadings/logs'
            linAccLogDir = new File(logDir + linAccDirName);
            gyroLogDir =new File(logDir + gyroDirName);
            // '/storage/emulated/0' + '/SensorReadings/logs' + '/gyro'

            boolean[] success = {
                    logDir.mkdirs(),
                    linAccLogDir.mkdirs(),
                    gyroLogDir.mkdirs()
            }; // create directors if they don't exist
            if (success[0] || success[1] || success[2]) {
                Log.d(LOG_TAG, "Log directories created.");
            } else {
                Log.d(LOG_TAG, "Log directories already exist.");
            }

            // Check files in linear accelerometer directory
            File[] logFiles = linAccLogDir.listFiles();
            if (logFiles.length == 0) {
                Log.d(LOG_TAG, "No files found in directory, setting logNum to 1");
                logNum = 1;
                linAccLogFileName = Integer.toString(logNum);
                gyroLogFilename = linAccLogFileName;
            } else {
                // Get the last log number
                List<Integer> logNames = new ArrayList<>();
                for (int i = 0; i < logFiles.length; i++) {
                    String fileName = logFiles[i].getName();
                    logNames.add(Integer.valueOf(fileName));
                }
                logNum = Collections.max(logNames) + 1;
                linAccLogFileName = Integer.toString(logNum);
                gyroLogFilename = linAccLogFileName;
            }
        } else {
            Toast.makeText(this, "Could not open log file for writing", Toast.LENGTH_SHORT).show();
            NavUtils.navigateUpFromSameTask(this);
        }
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

        // Initialise AWARE
//        aware = new Intent(this, Aware.class);
//        startService(aware);

        // Starts the logging
//        startLinAccBroadcast();
//        registerLinAccReceiver();
//        startGyroBroadcast();
//        registerGyroReceiver();
        startListening();

        // Add TapView to layout
        ViewGroup loggingLayout = (ViewGroup) this.findViewById(R.id.logging_activity);
        TapView tapView = new TapView(this, selectedHand);
        loggingLayout.addView(tapView);
    }

    // Callback when logging is complete.
    public void stopLogging() {
        Log.d(LOG_TAG, "Done logging; unregistering linAccReceiver, " +
                "stopping aware service and sensors");
        sensorManager.unregisterListener(sensorListener);
//        try {
//            Aware.stopSensor(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER);
//            Aware.stopSensor(this, Aware_Preferences.STATUS_GYROSCOPE);
//            stopService(aware);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (linAccReceiver != null) {unregisterReceiver(linAccReceiver);}
//        if (gyroReceiver != null) {unregisterReceiver(gyroReceiver);}
    }

    private void startListening() {
        // Initialise sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                    String label = "linear_accelerometer";
                    double xValue = event.values[0];
                    double yValue = event.values[1];
                    double zValue = event.values[2];
                    Calendar calendar = new GregorianCalendar();
                    long timeStamp = calendar.getTimeInMillis();
                    String logLine = label + "," + timeStamp + "," +
                            xValue + "," + yValue + "," + zValue;
                    appendLog(logLine, Sensor.TYPE_LINEAR_ACCELERATION);
                } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    String label = "gyroscope";
                    double xValue = event.values[0];
                    double yValue = event.values[1];
                    double zValue = event.values[2];
                    Calendar calendar = new GregorianCalendar();
                    long timeStamp = calendar.getTimeInMillis();
                    String logLine = label + "," + timeStamp + "," +
                            xValue + "," + yValue + "," + zValue;
                    appendLog(logLine, Sensor.TYPE_GYROSCOPE);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(sensorListener, linearAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorListener, gyroscope, SensorManager.SENSOR_DELAY_GAME);
    }

    private void appendLog(String logLine, int type) {

        if (isExternalStorageWritable()) {
            if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
                File logFile = new File(linAccLogDir, linAccLogFileName);

                try {
                    Log.d(LOG_TAG, logFile.getAbsolutePath());
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            new FileWriter(logFile, true)); // true set to append to log
                    bufferedWriter.append(logLine);
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                    Log.d(LOG_TAG, "Wrote to bufferedWriter");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (type == Sensor.TYPE_GYROSCOPE) {
                File logFile = new File(gyroLogDir, gyroLogFilename);

                try {
                    Log.d(LOG_TAG, logFile.getAbsolutePath());
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            new FileWriter(logFile, true)); // true set to append to log
                    bufferedWriter.append(logLine);
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                    Log.d(LOG_TAG, "Wrote to bufferedWriter");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "Could not write to log file", Toast.LENGTH_SHORT).show();
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public void backToMain(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }

    private void startLinAccBroadcast() {
        // Activate sensors, set settings.
        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_NORMAL);
        Aware.startSensor(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER);
    }

    private void startGyroBroadcast() {
        Aware.setSetting(this, Aware_Preferences.STATUS_GYROSCOPE, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_GYROSCOPE,
                SensorManager.SENSOR_DELAY_NORMAL);
        Aware.startSensor(this, Aware_Preferences.STATUS_GYROSCOPE);
    }

    private void registerLinAccReceiver() {
        // Create and register a sensorBroadcastReceiver
        linAccReceiver = new LinAccReceiver();
        IntentFilter linAccBroadcastFilter = new IntentFilter();
        // When new data is recorded in provider, grab it
        linAccBroadcastFilter.addAction(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);
        registerReceiver(linAccReceiver, linAccBroadcastFilter);
    }

    private void registerGyroReceiver() {
        // Create and register a sensorBroadcastReceiver
        gyroReceiver = new GyroReceiver();
        IntentFilter gyroBroadcastFilter = new IntentFilter();
        // When new data is recorded in provider, grab it
        gyroBroadcastFilter.addAction(Gyroscope.ACTION_AWARE_GYROSCOPE);
        registerReceiver(gyroReceiver, gyroBroadcastFilter);
    }

    public void showEndDialog() {
        // Remove progress bar and text
        final ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.logging_progress);
        final TextView loggingText = (TextView) this.findViewById(R.id.logging_text);
        progressBar.setVisibility(View.GONE);
        loggingText.setVisibility(View.GONE);

        // show the done button
        TextView doneText = (TextView) this.findViewById(R.id.done_text);
        Button doneButton = (Button) this.findViewById(R.id.done_button);
        doneText.bringToFront();
        doneText.setVisibility(View.VISIBLE);
        doneButton.bringToFront();
        doneButton.setVisibility(View.VISIBLE);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    public class LinAccReceiver extends BroadcastReceiver {
        private final static String LOG_TAG = "LinAccReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Object accData = intent.getExtras().get(LinearAccelerometer.EXTRA_DATA);
            ContentValues content = (ContentValues) accData;
            if (content != null) {
                String label = content.getAsString(Gyroscope_Provider.Gyroscope_Data.LABEL);
                int timeStamp = content.getAsInteger(
                        Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP);
                double xValue = content.getAsDouble(
                        Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0);
                double yValue = content.getAsDouble(
                        Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1);
                double zValue = content.getAsDouble(
                        Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2);
                Log.d(LOG_TAG, "Logging linear accelerometer content!");
                // This should be correct:
                // TextView sensorValues = (TextView) LoggingActivity.this.findViewById(R.id.sensor_values);
                // sensorValues.setText(xValue + ", " + yValue + ", " + zValue);

                String logLine = label + "," + timeStamp + "," +
                        xValue + "," + yValue + "," + zValue;
                appendLog(logLine, Sensor.TYPE_LINEAR_ACCELERATION);
            }
        }
    }

    public class GyroReceiver extends BroadcastReceiver {
        private final static String LOG_TAG = "GyroReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Object gyroData = intent.getExtras().get(Gyroscope.EXTRA_DATA);
            ContentValues content = (ContentValues) gyroData;
            if (content != null) {
                String label = content.getAsString(Gyroscope_Provider.Gyroscope_Data.LABEL);
                int timeStamp = content.getAsInteger(Gyroscope_Provider.Gyroscope_Data.TIMESTAMP);
                double xValue = content.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_0);
                double yValue = content.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_1);
                double zValue = content.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_2);
                Log.d(LOG_TAG, "Logging gyroscope content!");
                // This should be correct:
                // TextView sensorValues = (TextView) LoggingActivity.this.findViewById(R.id.gyro_values);
                // sensorValues.setText(xValue + ", " + yValue + ", " + zValue);

                String logLine = label + "," + timeStamp + ","  +
                        xValue + "," + yValue + "," + zValue;
                appendLog(logLine, Sensor.TYPE_GYROSCOPE);
            }
        }
    }


}
