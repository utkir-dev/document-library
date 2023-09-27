package com.tiptop.app.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tiptop.R;

public class DrawLineCanvas extends View {

    private Canvas c;

    private Paint pLine, pBg;
    private Path touchPath;

    private Bitmap b;
    float endX;
    float endY;
    float touchX;
    float touchY;
    public DrawLineCanvas(Context context) {
        super(context);
    }

    public DrawLineCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);

        pBg = new Paint();
        pBg.setColor(context.getResources().getColor(R.color.green_clear, null));
        pBg.setStrokeCap(Paint.Cap.ROUND);
        pLine = new Paint();
        pLine.setColor(context.getResources().getColor(R.color.green_clear, null));
        pLine.setAntiAlias(true);
        pLine.setStyle(Paint.Style.STROKE);
        pLine.setStrokeWidth(60);

        touchPath = new Path();


    }

    public DrawLineCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
       b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
       c = new Canvas(b);
    //    c = new Canvas();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

//        touchX = event.getX();
//        touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                touchX = event.getX();
                touchY = event.getY();
                endX = event.getX();
                endY = event.getY();
            }// touchPath.moveTo(touchX, touchY);
            case MotionEvent.ACTION_MOVE -> {
                endX = event.getX();
                endY = event.getY();

               c.drawRect(touchX, touchY, endX, endY, pBg);
            }// touchPath.lineTo(touchX, touchY);
            case MotionEvent.ACTION_UP -> {
                c.drawRect(touchX, touchY, endX, endY, pBg);
//                touchPath.lineTo(touchX, touchY);
//                c.drawPath(touchPath, pLine);
//                touchPath = new Path();
            }
            default -> {
                return false;
            }
        }

       invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

       // canvas.drawBitmap(b, 0, 0, pBg);
       // canvas.drawPath(touchPath, pLine);
            canvas.drawRect(touchX, touchY, endX, endY, pBg);

    }
}
