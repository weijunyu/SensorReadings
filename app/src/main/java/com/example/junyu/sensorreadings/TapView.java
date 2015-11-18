package com.example.junyu.sensorreadings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TapView extends View {
    private final String LOG_TAG = "TapView, Canvas";
    private Paint TapIndicator;
    private Context context;
    private int indicatorRadius = 60;
    private int separation = 70;
    private int sampleTime = 3000; // 3000ms, or 3s.
    private int sampleInterval = sampleTime / 100;

    public TapView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public TapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        TapIndicator = new Paint(Paint.ANTI_ALIAS_FLAG);
        TapIndicator.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        TapIndicator.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPoints(canvas);
    }

    private void drawPoints(Canvas canvas) {
        /**
         * Draws the 7 tap points on the display. Points are numbered 1-7, and go left-right,
         * top-down.
         */
        flashTopLeftIndicator(canvas);

//        // 7 Points to draw around the screen
//        int radius = 60;
//        int separation = 100;
//        canvas.drawCircle(separation, separation, radius, TapIndicator); // 1: Top left
//        int x2 = getWidth() / 2;
//        canvas.drawCircle(x2, separation, radius, TapIndicator); // 2: Top middle
//        int x3 = getWidth() - separation;
//        canvas.drawCircle(x3, separation, radius, TapIndicator); // 3: Top right
//        int y4 = getHeight() / 2;
//        canvas.drawCircle(separation, y4, radius, TapIndicator); // 4: Middle left
//        int x5 = getWidth() - separation;
//        int y5 = getHeight() / 2;
//        canvas.drawCircle(x5, y5, radius, TapIndicator); // 5: Middle right
//        int y6 = getHeight() - separation;
//        canvas.drawCircle(separation, y6, radius, TapIndicator); // 6: Bottom left
//        int x7 = getWidth() - separation;
//        int y7 = getHeight() - separation;
//        canvas.drawCircle(x7, y7, radius, TapIndicator); // 7: Bottom right

    }


    private void flashTopLeftIndicator(Canvas canvas) {
        canvas.drawCircle(separation, separation, indicatorRadius, TapIndicator); // 1: Top left

        View parent = (View) getParent();
        final ProgressBar progressBar = (ProgressBar) parent.findViewById(R.id.logging_progress);
        progressBar.setVisibility(View.VISIBLE);
        final TextView loggingText = (TextView) parent.findViewById(R.id.hidden_text);
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
                Log.d(LOG_TAG, "onFinish called!");
                progressBar.setVisibility(View.INVISIBLE);
                loggingText.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private void flashTopMiddleIndicator(Canvas canvas) {
        int x = getWidth() / 2;
        canvas.drawCircle(x, separation, indicatorRadius, TapIndicator); // 2: Top middle
    }

    // Have Stimulus or Target (basically drawn objects), implementing Drawables
    // Have list of Drawables, which can be inserted or deleted from the View
}
