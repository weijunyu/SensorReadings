package com.example.junyu.sensorreadings;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class LoggingActivity5P extends LoggingActivity {
    private static final String LOG_TAG = "LoggingActivity(5P)";
//    private Intent aware;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    protected void setLogNumber() {
        // Find the linear accelerometer directory
        if (isExternalStorageWritable()) {
            int logNum;
            File logDir = new File(
                    Environment.getExternalStorageDirectory() + mainLogDir + "/5_points");
            // '/storage/emulated/0' + '/SensorReadings/logs'
            linAccLogDir = new File(logDir + linAccDirName);
            gyroLogDir = new File(logDir + gyroDirName);
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
                List<Integer> fileNames = new ArrayList<>();
                for (File file : logFiles) {
                    // for each
                    String fileName = file.getName();
                    fileNames.add(Integer.valueOf(fileName));
                }
                logNum = Collections.max(fileNames) + 1;
                linAccLogFileName = Integer.toString(logNum);
                gyroLogFilename = linAccLogFileName;
            }
        } else {
            Toast.makeText(this, "Could not open log file for writing", Toast.LENGTH_SHORT).show();
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    public void beginLogging(View view) {

        if (checkHandSelected()) {
            hideHandSelection();
            hideExplanationAndButton();

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
            TapView5P tapView = new TapView5P(this, selectedHand);
            loggingLayout.addView(tapView);
        } else {
            Toast.makeText(this, "Please select which hand you are using to hold the phone first",
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void startListening() {
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
}
