package com.example.junyu.sensorreadings;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.LinearAccelerometer;
import com.aware.providers.Linear_Accelerometer_Provider;

public class LoggingActivity extends AppCompatActivity {
    private static final String LOG_TAG = "LoggingActivity";
    private LinAccReceiver linAccReceiver;
    private Intent aware;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        Log.d(LOG_TAG, "Started logging!");

        // Initialise AWARE
        aware = new Intent(this, Aware.class);
        startService(aware);
        // Activate sensors, set settings.
        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_NORMAL);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

        // Create and register a sensorBroadcastReceiver
        linAccReceiver = new LinAccReceiver();
        IntentFilter linAccBroadcastFilter = new IntentFilter();
        // When new data is recorded in provider, grab it
        linAccBroadcastFilter.addAction(LinearAccelerometer.ACTION_AWARE_LINEAR_ACCELEROMETER);
        registerReceiver(linAccReceiver, linAccBroadcastFilter);
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
        super.onPause();
        unregisterReceiver(linAccReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy called, aware service stopped");
        super.onDestroy();
        stopService(aware);
    }

    public class LinAccReceiver extends BroadcastReceiver {
        private final static String LOG_TAG = "LinAccReceiver";
        private double xValue, yValue, zValue;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Sensor broadcast received!!!");
            TextView xLinAccValue = (TextView) findViewById(R.id.x_linacc_text);
            TextView yLinAccValue = (TextView) findViewById(R.id.y_linacc_text);
            TextView zLinAccValue = (TextView) findViewById(R.id.z_linacc_text);

            Object accData = intent.getExtras().get(LinearAccelerometer.EXTRA_DATA);
            ContentValues content = (ContentValues) accData;
            if (content != null) {
                xValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0);
                yValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1);
                zValue = content.getAsDouble(Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2);
                xLinAccValue.setText(String.valueOf(xValue));
                yLinAccValue.setText(String.valueOf(yValue));
                zLinAccValue.setText(String.valueOf(zValue));
            }
        }
    }
}
