package com.example.junyu.sensorreadings;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "SensorMainActivity";
    public static final String HAND_SELECT_EXTRA = "com.example.junyu.sensorreadings.HAND_SELECT_EXTRA";
    public static final String LEFT_HAND = "left hand";
    public static final String RIGHT_HAND = "right hand";
    private static String SELECTED_HAND = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.appbar_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startLoggingActivity(View view) {
        // Check if button was pressed first
        RadioButton radioLeftHand = (RadioButton) findViewById(R.id.radio_left_hand);
        RadioButton radioRightHand = (RadioButton) findViewById(R.id.radio_right_hand);
        if (radioLeftHand.isChecked() || radioRightHand.isChecked()) {
            Intent intent = new Intent(this, LoggingActivity.class);
            intent.putExtra(HAND_SELECT_EXTRA, SELECTED_HAND);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please select which hand you are using to hold the phone first",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void selectHand(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_left_hand:
                if (checked) {
                    SELECTED_HAND = LEFT_HAND;
                }
                break;
            case R.id.radio_right_hand:
                if (checked) {
                    SELECTED_HAND = RIGHT_HAND;
                }
                break;
            default:
                break;
        }
    }
}
