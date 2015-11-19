package com.example.junyu.sensorreadings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TapView extends View {
    private final static String LOG_TAG = "TapView, Canvas";
    private Paint IndicatorPaint;
    private Context context;
    private String selectedHand;
    private int indicatorRadius = 60;
    private int separation = 70;
    private int sampleTime = 3000; // 3000ms, or 3s.
    private int sampleInterval = sampleTime / 100;
    private int indicatorNum = 1;
    private int[][] indicatorCoords = new int[5][2];
    private boolean doneLogging = false;

    public TapView(Context context, String selectedHand) {
        super(context);
        this.context = context;
        this.selectedHand = selectedHand;
        init();
    }

    public TapView(Context context, String selectedHand, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.selectedHand = selectedHand;
        init();
    }

    private void init() {
        IndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        IndicatorPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        IndicatorPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPoints(canvas);
    }

    private void drawPoints(Canvas canvas) {
        setCoordinates(selectedHand);
        if (!doneLogging) {
            flashIndicator(canvas, indicatorNum);

            new AsyncTask<Void, Void, Void>() {
                // params: 1. input, 2. progress info, 3. result
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Thread.sleep(sampleTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    if (indicatorNum < 5) {
                        indicatorNum += 1;
                        invalidate();
                    } else {
                        doneLogging = true;
                        invalidate();
                    }
                }
            }.execute((Void[]) null);;
        } else {
            canvas.drawColor(Color.WHITE);
            showEndDialog();
        }
    }
    
    private void flashIndicator(Canvas canvas, int indicatorNum) {
        int indicatorIndex = indicatorNum - 1;
        int xMargin = indicatorCoords[indicatorIndex][0];
        int yMargin = indicatorCoords[indicatorIndex][1];
        canvas.drawCircle(xMargin, yMargin, indicatorRadius, IndicatorPaint);

        View parent = (View) getParent();
        final ProgressBar progressBar = (ProgressBar) parent.findViewById(R.id.logging_progress);
        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);
        final TextView loggingText = (TextView) parent.findViewById(R.id.logging_text);
        loggingText.bringToFront();
        loggingText.setVisibility(View.VISIBLE);
        loggingText.setText(R.string.logging_text);

        // Use a timer
        new CountDownTimer(sampleTime, sampleInterval) {
            // params: 1. ms till onFinish called; 2. interval(ms) to receiver onTick callbacks
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = sampleTime / sampleInterval - (int) (millisUntilFinished / sampleInterval);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                // Log.d(LOG_TAG, "onFinish called!");
                progressBar.setProgress(0);
            }
        }.start();
    }

    private void setCoordinates(String selectedHand) {
        /*
        // Locations of the 7 tap points on the display. Points are numbered 1-7, and go left-right,
        // top-down.
        // point 1
        indicatorCoords[0][0] = separation;
        indicatorCoords[0][1] = separation;
        // point 2
        indicatorCoords[1][0] = getWidth() / 2;
        indicatorCoords[1][1] = separation;
        // point 3
        indicatorCoords[2][0] = getWidth() - separation;
        indicatorCoords[2][1] = separation;
        // point 4
        indicatorCoords[3][0] = separation;
        indicatorCoords[3][1] = getHeight() / 2;
        // point 5
        indicatorCoords[4][0] = getWidth() - separation;
        indicatorCoords[4][1] = getHeight() / 2;
        // point 6
        indicatorCoords[5][0] = separation;
        indicatorCoords[5][1] = getHeight() - separation;
        // point 7
        indicatorCoords[6][0] = getWidth() - separation;
        indicatorCoords[6][1] = getHeight() - separation;
        */
        if (selectedHand.equals(MainActivity.RIGHT_HAND)) {
            // point 3
            indicatorCoords[0][0] = getWidth() - separation;
            indicatorCoords[0][1] = separation;
            // point 2
            indicatorCoords[1][0] = getWidth() / 2;
            indicatorCoords[1][1] = separation;
            // point 1
            indicatorCoords[2][0] = separation;
            indicatorCoords[2][1] = separation;
            // point 4
            indicatorCoords[3][0] = separation;
            indicatorCoords[3][1] = getHeight() / 2;
            // point 6
            indicatorCoords[4][0] = separation;
            indicatorCoords[4][1] = getHeight() - separation;
        } else {
            // point 1
            indicatorCoords[0][0] = separation;
            indicatorCoords[0][1] = separation;
            // point 2
            indicatorCoords[1][0] = getWidth() / 2;
            indicatorCoords[1][1] = separation;
            // point 3
            indicatorCoords[2][0] = getWidth() - separation;
            indicatorCoords[2][1] = separation;
            // point 5
            indicatorCoords[3][0] = getWidth() - separation;
            indicatorCoords[3][1] = getHeight() / 2;
            // point 7
            indicatorCoords[4][0] = getWidth() - separation;
            indicatorCoords[4][1] = getHeight() - separation;
        }
    }

    private void showEndDialog() {
        View parent = (View) getParent();
        // Remove progress bar and text
        final ProgressBar progressBar = (ProgressBar) parent.findViewById(R.id.logging_progress);
        final TextView loggingText = (TextView) parent.findViewById(R.id.logging_text);
        progressBar.setVisibility(View.GONE);
        loggingText.setVisibility(View.GONE);

        // show the done button
        TextView doneText = (TextView) parent.findViewById(R.id.done_text);
        Button doneButton = (Button) parent.findViewById(R.id.done_button);
        doneText.bringToFront();
        doneText.setVisibility(View.VISIBLE);
        doneButton.bringToFront();
        doneButton.setVisibility(View.VISIBLE);
    }

    // Have Stimulus or Target (basically drawn objects), implementing Drawables
    // Have list of Drawables, which can be inserted or deleted from the View
}
