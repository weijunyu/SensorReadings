package com.example.junyu.sensorreadings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

public class TapView extends View {
    private Paint TapIndicator;
    private Context context;

    public TapView(Context context) {
        super(context);
        this.context = context;
        init();
    }
    public TapView(Context context, AttributeSet attrs) {
        super(context,attrs);
        this.context = context;
        init();
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
        // 7 Points to draw around the screen
        int radius = 60;
        int separation = 100;
        canvas.drawCircle(separation, separation, radius, TapIndicator); // 1: Top left
        int x2 = getWidth() / 2;
        canvas.drawCircle(x2, separation, radius, TapIndicator); // 2: Top middle
        int x3 = getWidth() - separation;
        canvas.drawCircle(x3, separation, radius, TapIndicator); // 3: Top right
        int y4 = getHeight() / 2;
        canvas.drawCircle(separation, y4, radius, TapIndicator); // 4: Middle left
        int x5 = getWidth() - separation;
        int y5 = getHeight() / 2;
        canvas.drawCircle(x5, y5, radius, TapIndicator); // 5: Middle right
        int y6 = getHeight() - separation;
        canvas.drawCircle(separation, y6, radius, TapIndicator); // 6: Bottom left
        int x7 = getWidth() - separation;
        int y7 = getHeight() - separation;
        canvas.drawCircle(x7, y7, radius, TapIndicator); // 7: Bottom right

    }

    private void init() {
        TapIndicator = new Paint(Paint.ANTI_ALIAS_FLAG);
        TapIndicator.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        TapIndicator.setStyle(Paint.Style.FILL);
    }

    // Have Stimulus or Target (basically drawn objects), implementing Drawables
    // Have list of Drawables, which can be inserted or deleted from the View
}
