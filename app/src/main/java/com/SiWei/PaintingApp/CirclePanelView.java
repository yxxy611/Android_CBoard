package com.SiWei.PaintingApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by yxxy6 on 10/29/2017.
 */

public class CirclePanelView extends View {
    private float scrollSpeed;
    private int mScrollValue, mOpacityValue, mColorValue, mBrushValue;
    private int mOpacityCount, mColorCount, mBrushCount;
    private Bitmap mMainBmp, mOpacBmp, mMainSelectBmp, mOpacSelectBmp, mBrushSelectBmp;
    private Matrix mMatrix;
    public MenuMode menuMode, tmpMenuMode;
    private Paint mPaint;
    private int[] palette;
    private float centerX, centerY;
    private float[] mOffset;

    public CirclePanelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mMainBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_main, null);
        centerX = mMainBmp.getWidth() / 2;
        centerY = mMainBmp.getHeight() / 2 + 1;
        mOffset =new float[] {-mMainBmp.getWidth()*1.5f,-mMainBmp.getHeight()/2};
        mMainSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_main_select, null);
        mOpacBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_opacity, null);
        mOpacSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_opacity_select, null);
        mBrushSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_brush_select, null).extractAlpha();
        //mBrushUnselectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_brush_unselect, null);
        menuMode = MenuMode.DEFAULT;
        mMatrix = new Matrix();
        mScrollValue = 0;
        mOpacityValue = 0;
        mColorValue = 0;
        mBrushValue = 0;
        mOpacityCount = 20;
        mBrushCount = 12;
        palette = new int[]{0xff86B32C,
                0xff0C8858,
                0xff188EB0,
                0xff2B6BA5,
                0xff404A90,
                0xff673683,
                0xffB61074,
                0xffD52824,
                0xffDD6023,
                0xffE68A23,
                0xffF2BF1D,
                0xffE9DC23};
        mColorCount = palette.length;
        Log.d("bbbbb", "densityDpi: " + getResources().getDisplayMetrics().densityDpi);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (menuMode) {
            case MAIN:
                tmpMenuMode = drawMainMenu(canvas);
                break;
            case OPACITY:
                drawOpacMenu(canvas);
                break;
            case COLOR:
                drawColorMenu(canvas);
                break;
            case BRUSH:
                drawBrushMenu(canvas);
                break;
        }


    }

    public void confirm() {
        if (menuMode == MenuMode.MAIN) {
            setMode();
        } else {
            menuMode = MenuMode.DEFAULT;
        }
        invalidate();
    }

    private void setMode() {
        menuMode = tmpMenuMode;
    }

    public void menuScrollNext() {
        switch (menuMode) {
            case MAIN:
                mScrollValue += 1;
                break;
            case OPACITY:
                mOpacityValue = mOpacityValue < mOpacityCount ? mOpacityValue + 1 : mOpacityValue;
                break;
            case COLOR:
                mColorValue = mColorValue < mColorCount - 1 ? mColorValue + 1 : mColorValue;
                break;
            case BRUSH:
                mBrushValue = mBrushValue < mBrushCount - 1 ? mBrushValue + 1 : mBrushValue;//do not need reach 360 deg
                break;
        }
        invalidate();
    }

    public void menuScrollPrev() {
        switch (menuMode) {
            case MAIN:
                mScrollValue -= 1;
                break;
            case OPACITY:
                mOpacityValue = mOpacityValue > 0 ? mOpacityValue - 1 : 0;
                break;
            case COLOR:
                mColorValue = mColorValue > 0 ? mColorValue - 1 : 0;
                break;
            case BRUSH:
                mBrushValue = mBrushValue > 0 ? mBrushValue - 1 : 0;
                break;
        }
        invalidate();
    }

    public void awake() {
        mScrollValue = 0;
        menuMode = MenuMode.MAIN;
        invalidate();
    }

    public void dismiss() {
        menuMode = MenuMode.DEFAULT;
        invalidate();
    }

    public int getmOpacityValue() {
        ;
        return mOpacityValue;
    }

    public int getmColorValue() {
        return mColorValue;
    }

    public int getmBrushValue() {
        return mBrushValue;
    }

    public void setPosition(float x, float y) {
        this.setX(x+mOffset[0]);
        this.setY(y+mOffset[1]);
    }

    private MenuMode drawMainMenu(Canvas canvas) {
        //mScrollValue = 0;
        canvas.drawBitmap(mMainBmp, 0, 0, mPaint);
        int i = mScrollValue % 3;
        MenuMode selectedMode = MenuMode.MAIN;
        mMatrix.setRotate(-120 * i, centerX, centerY);
        canvas.drawBitmap(mMainSelectBmp, mMatrix, mPaint);
        switch (i) {
            case 0:
                selectedMode = MenuMode.OPACITY;
                break;
            case 1:
                selectedMode = MenuMode.COLOR;
                break;

            case 2:
                selectedMode = MenuMode.BRUSH;
                break;
            case -1:
                selectedMode = MenuMode.BRUSH;
                break;
            case -2:
                selectedMode = MenuMode.COLOR;
                break;
        }
        return selectedMode;
    }

    private void drawOpacMenu(Canvas canvas) {
        canvas.drawBitmap(mOpacBmp, 0, 0, mPaint);
        int i = mOpacityValue;
        mMatrix.setRotate(-i * 360 / mOpacityCount, centerX, centerY);
        canvas.drawBitmap(mOpacSelectBmp, mMatrix, mPaint);
    }

    private void drawBrushMenu(Canvas canvas) {
        canvas.drawBitmap(mMainBmp, 0, 0, mPaint);
        //mPaint.setColor(0xff221814);
        int i = mBrushValue;
        mPaint.setColor(0xff000000);
        for (int n = 0; n <= i; n++) {
            mMatrix.setRotate(-n * 360 / mBrushCount, centerX, centerY);
            canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
            //Log.i("fda", "n: " + n + "    " + "brushvalue: " + mBrushValue);
        }
        mPaint.setColor(0xffC9C9CA);
        for (int t = 1; t < mBrushCount - i; t++) {
            mMatrix.setRotate(t * 360 / mBrushCount, centerX, centerY);
            canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
        }

    }

    private void drawColorMenu(Canvas canvas) {
        canvas.drawBitmap(mMainBmp, 0, 0, mPaint);
        int i = mColorValue;
        for (int n = 0; n < mColorCount; n++) {
            mPaint.setColor(palette[n]);
            mMatrix.setRotate(-n * 360 / mColorCount, centerX, centerY);
            canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
        }

        mMatrix.setScale(1.1f, 1.1f, centerX, centerY);
        mMatrix.postRotate(-i * 360 / mColorCount, centerX, centerY);
        mPaint.setColor(palette[i]);
        canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
        //Log.i("fda", "colorvalue: " + mColorValue);
    }

    public enum MenuMode {
        DEFAULT, MAIN, BRUSH, OPACITY, COLOR
    }
}
