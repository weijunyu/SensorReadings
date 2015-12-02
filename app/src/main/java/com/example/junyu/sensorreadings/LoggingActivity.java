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
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
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
import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class LoggingActivity extends AppCompatActivity {
    private static String LOG_TAG = "LoggingActivity(Base)";
    protected static final String LEFT_HAND = "left_hand";
    protected static final String RIGHT_HAND = "right_hand";
    protected String selectedHand;
//    private Intent aware;

    protected File linAccLogDir;
    protected File gyroLogDir;
    protected String mainLogDir = "/SensorReadings/logs";
    protected String linAccDirName = "/lin_acc";
    protected String gyroDirName = "/gyro";
    protected String linAccLogFileName;
    protected String gyroLogFilename;

    protected SensorManager sensorManager;
    protected SensorEventListener sensorListener;

    protected int indicatorNum;

    /**
     * Sets the name of the log file, runs in onStart()
     */
    abstract void setLogNumber();

    /**
     * Checks if hand is selected, then erases the views and starts TapView, which draws tap points
     */

    abstract void beginLogging(View view);

    /**
     * Creates sensor listeners that write to the log file upon changes in sensor values
     */
//    abstract void startListening();

    public void selectHand(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_left_hand:
                if (checked) {
                    selectedHand = LEFT_HAND;
                }
                break;
            case R.id.radio_right_hand:
                if (checked) {
                    selectedHand = RIGHT_HAND;
                }
                break;
            default:
                break;
        }
    }

    protected boolean checkHandSelected() {
        // Check if hand is selected
        RadioButton radioLeftHand = (RadioButton) findViewById(R.id.radio_left_hand);
        RadioButton radioRightHand = (RadioButton) findViewById(R.id.radio_right_hand);
        return (radioLeftHand.isChecked() || radioRightHand.isChecked());
    }

    protected void hideHandSelection() {
        // Remove the entire hand selection layout
        LinearLayout selectHandLayout = (LinearLayout) findViewById(R.id.select_hand_layout);
        selectHandLayout.setVisibility(View.GONE);
    }

    protected void hideExplanationAndButton() {
        // Remove text and button
        TextView loggingExplanation = (TextView) findViewById(R.id.logging_explanation);
        Button beginLogging = (Button) findViewById(R.id.begin_logging);
        loggingExplanation.setVisibility(View.GONE);
        beginLogging.setVisibility(View.GONE);
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
                    String hand = selectedHand;
                    double xValue = event.values[0];
                    double yValue = event.values[1];
                    double zValue = event.values[2];
                    Calendar calendar = new GregorianCalendar();
                    long timeStamp = calendar.getTimeInMillis();
                    String logLine = hand + "," + timeStamp + "," + indicatorNum + "," +
                            xValue + "," + yValue + "," + zValue;
                    appendLog(logLine, Sensor.TYPE_LINEAR_ACCELERATION);
                } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    String hand = selectedHand;
                    double xValue = event.values[0];
                    double yValue = event.values[1];
                    double zValue = event.values[2];
                    Calendar calendar = new GregorianCalendar();
                    long timeStamp = calendar.getTimeInMillis();
                    String logLine = hand + "," + timeStamp + "," + indicatorNum + "," +
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

    protected void appendLog(String logLine, int sensorType) {
        if (isExternalStorageWritable()) {
            if (sensorType == Sensor.TYPE_LINEAR_ACCELERATION) {
                File logFile = new File(linAccLogDir, linAccLogFileName);
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            new FileWriter(logFile, true)); // true set to append to log
                    bufferedWriter.append(logLine);
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                    Log.d(LOG_TAG, "Wrote log to: " + logFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
                File logFile = new File(gyroLogDir, gyroLogFilename);
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            new FileWriter(logFile, true)); // true set to append to log
                    bufferedWriter.append(logLine);
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                    Log.d(LOG_TAG, "Wrote log to: " + logFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "Could not write to log file", Toast.LENGTH_SHORT).show();
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    public void setIndicatorNum(int indicatorNum) {
        this.indicatorNum = indicatorNum;
    }

    /**
     * Unregisters sensors when logging is complete, or activity ends.
     */
    public void stopLogging() {
        if (sensorListener != null) {
            sensorManager.unregisterListener(sensorListener);
        }
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

    public void showEndDialog() {
        // Remove progress bar and text
        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.logging_progress);
        TextView loggingText = (TextView) this.findViewById(R.id.logging_text);
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

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    public void backToMain(View view) {
        NavUtils.navigateUpFromSameTask(this);
    }

    /////////////////////////////////////////AWARE Things///////////////////////////////////////////////
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
        LinAccReceiver linAccReceiver = new LinAccReceiver();
        IntentFilter linAccBroadcastFilter = new IntentFilter();
        // When new data is recorded in provider, grab it
        linAccBroadcastFilter.addAction(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);
        registerReceiver(linAccReceiver, linAccBroadcastFilter);
    }

    private void registerGyroReceiver() {
        // Create and register a sensorBroadcastReceiver
        GyroReceiver gyroReceiver  = new GyroReceiver();
        IntentFilter gyroBroadcastFilter = new IntentFilter();
        // When new data is recorded in provider, grab it
        gyroBroadcastFilter.addAction(Gyroscope.ACTION_AWARE_GYROSCOPE);
        registerReceiver(gyroReceiver, gyroBroadcastFilter);
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
                // TextView sensorValues = (TextView) LoggingActivity5p.this.findViewById(R.id.sensor_values);
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
                // TextView sensorValues = (TextView) LoggingActivity5p.this.findViewById(R.id.gyro_values);
                // sensorValues.setText(xValue + ", " + yValue + ", " + zValue);

                String logLine = label + "," + timeStamp + ","  +
                        xValue + "," + yValue + "," + zValue;
                appendLog(logLine, Sensor.TYPE_GYROSCOPE);
            }
        }
    }
}
