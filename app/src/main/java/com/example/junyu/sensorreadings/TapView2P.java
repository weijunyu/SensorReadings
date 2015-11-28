package com.example.junyu.sensorreadings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TapView2P extends View {
    private final static String LOG_TAG = "TapView2P";
    private Paint IndicatorPaint;
    private Context context;
    private int numOfIndicators = 2;
    private int indicatorRadius = 60;
    private int separation = 70;
    private int sampleTime = 2000; // 2000ms, or 2s.
    private int sampleInterval = sampleTime / 100;
    private int maxRuns = 3;
    private int[][] indicatorCoords = new int[5][2];
    private int indicatorNum = 1;
    private int run = 1;
    private boolean doneLogging = false;

    public TapView2P(Context context) {
        super(context);
//        this.context = context;
        init();
    }

    public TapView2P(Context context, AttributeSet attrs) {
        super(context, attrs);
//        this.context = context;
        init();
    }

    private void init() {
        IndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        IndicatorPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        IndicatorPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPoints(canvas);
    }

    private void drawPoints(Canvas canvas) {
        // Set coordinates of indicators, depending on hand used. Needs to be called in here!
        setCoordinates();
        // Show "logging..." text
        View parent = (View) getParent();
        TextView loggingText = (TextView) parent.findViewById(R.id.logging_text);
        loggingText.bringToFront();
        loggingText.setVisibility(View.VISIBLE);
        loggingText.setText(R.string.logging_text);

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
                    if (indicatorNum < numOfIndicators) {
                        // Updates the indicator to draw, then draw it.
                        indicatorNum += 1;
                        invalidate();
                    } else {
                        if (run < maxRuns) {
                            run += 1;
                            indicatorNum = 1;
                            invalidate();
                        } else {
                            doneLogging = true;
                            invalidate();
                        }
                    }
                }
            }.execute((Void[]) null);
        } else {
            canvas.drawColor(Color.WHITE);
            LoggingActivity parentActivity = (LoggingActivity) this.getContext();
//            LoggingActivity2P loggingActivity = (LoggingActivity2P) context;
            parentActivity.showEndDialog();
            parentActivity.stopLogging();
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

    /**
     Locations of the 7 tap points on the display. Points are numbered 1-7, and go left-right,
     top-down. First array element is x position, second element is y position.
     point 1:
     indicatorCoords[0][0] = separation;
     indicatorCoords[0][1] = separation;
     point 2:
     indicatorCoords[1][0] = getWidth() / 2;
     indicatorCoords[1][1] = separation;
     point 3:
     indicatorCoords[2][0] = getWidth() - separation;
     indicatorCoords[2][1] = separation;
     point 4:
     indicatorCoords[3][0] = separation;
     indicatorCoords[3][1] = getHeight() / 2;
     point 5:
     indicatorCoords[4][0] = getWidth() - separation;
     indicatorCoords[4][1] = getHeight() / 2;
     point 6:
     indicatorCoords[5][0] = separation;
     indicatorCoords[5][1] = getHeight() - separation;
     point 7:
     indicatorCoords[6][0] = getWidth() - separation;
     indicatorCoords[6][1] = getHeight() - separation;
     */
    private void setCoordinates() {
        // point 4
        indicatorCoords[0][0] = separation;
        indicatorCoords[0][1] = getHeight() / 2;
        // point 5
        indicatorCoords[1][0] = getWidth() - separation;
        indicatorCoords[1][1] = getHeight() / 2;
    }
}
