package com.example.junyu.sensorreadings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
    private static final String LOG_TAG = "SensorMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

    public void startLoggingActivity5P(View view) {
        Intent intent = new Intent(this, LoggingActivity5P.class);
        startActivity(intent);
    }

    public void startLoggingActivity2P(View view) {
        Intent intent = new Intent(this, LoggingActivity2P.class);
        startActivity(intent);
    }
}
