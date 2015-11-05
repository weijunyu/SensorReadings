package com.example.junyu.sensorreadings;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import com.aware.providers.Linear_Accelerometer_Provider;

public class SensorObserver extends ContentObserver{
    private final String LOG_TAG = "SensorObserver";
    private Context context;
    private double xValue, yValue, zValue;

    public SensorObserver(Context context, Handler handler) {
        super(handler);
        this.context = context;
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(LOG_TAG, "Change observerd!!!");
        // Get latest recorded value
        Cursor linAccMeter = this.context.getContentResolver().query(
                Linear_Accelerometer_Provider.Linear_Accelerometer_Data.CONTENT_URI,
                null,
                null,
                null,
                Linear_Accelerometer_Provider.Linear_Accelerometer_Data.TIMESTAMP + " DESC LIMIT 1"
        );
        try {
            if (linAccMeter != null && linAccMeter.moveToFirst()) {
                xValue = linAccMeter.getDouble(
                        linAccMeter.getColumnIndex(
                                Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_0
                        ));
                yValue = linAccMeter.getDouble(
                        linAccMeter.getColumnIndex(
                                Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_1
                        ));
                zValue = linAccMeter.getDouble(
                        linAccMeter.getColumnIndex(
                                Linear_Accelerometer_Provider.Linear_Accelerometer_Data.VALUES_2
                        ));
                Log.d(LOG_TAG, "xValue: " + xValue + ", yValue: " + yValue + ", zValue: " + zValue);
            } else {
                Log.e(LOG_TAG, "Didn't manage to get values");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            linAccMeter.close();
        }
    }

    public double[] getLinAccValues() {
        return new double[] {xValue, yValue, zValue};
    }
}