package com.example.junyu.sensorreadings;

import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggingActivity2P extends LoggingActivity {
    private static final String LOG_TAG ="LoggingActivity(2P)";
    private static final String noOfPointsDir = "/2_points";

    protected void setLogNumber() {
        // Find the linear accelerometer directory
        if (isExternalStorageWritable()) {
            int logNum;
            File logDir = new File(
                    Environment.getExternalStorageDirectory() +
                            mainLogDir + noOfPointsDir + handDir);
            // eg: '/storage/emulated/0' + '/SensorReadings/logs' + '/2_points' + '/left_hand'
            linAccLogDir = new File(logDir + linAccDirName);
            gyroLogDir = new File(logDir + gyroDirName);
            // '/storage/emulated/0' + '/SensorReadings/logs' + '/gyro'

            boolean[] success = {
                    logDir.mkdirs(),
                    linAccLogDir.mkdirs(),
                    gyroLogDir.mkdirs()
            }; // create directories if they don't exist
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
            setLogNumber();
            startListening();

            // Add TapView to layout
            ViewGroup loggingLayout = (ViewGroup) this.findViewById(R.id.logging_activity);
            TapView2P tapView = new TapView2P(this, selectedHand);
            loggingLayout.addView(tapView);
        } else {
            Toast.makeText(this, "Please select which hand you are using to hold the phone first",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
