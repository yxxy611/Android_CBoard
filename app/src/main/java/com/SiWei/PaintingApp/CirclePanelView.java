package com.SiWei.PaintingApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by yxxy6 on 10/29/2017.
 */

public class CirclePanelView extends View {
    private int mScrollValue, mColorValue,mOpacityValue,mOpacityRange,mOpacityValueTmpCal;
    private float mBrushValue;
    private float mBrushRange;
    private int mOpacityValueTmp, mColorValueTmp, mBrushValueTmp;
    private float mBrushValueTmpCal ;
    private int mColorValueTmpCal;
    private int mOpacityCount, mColorCount, mBrushCount;
    private Bitmap mMainBmp, mOpacBmp, mMainSelectBmp, mOpacSelectBmp, mBrushSelectBmp;
    private Matrix mMatrix;
    private MenuMode menuMode, tmpMenuMode;
    private Paint mPaint, mPaintTest;
    private int[] palette;
    private float centerX, centerY,posX,posY;
    private float[] mOffset;
    private boolean isAwake;
    private float[] mGestureCenter, mVectorMoved;
    private float mAng,mAngIni;
    private boolean isAngleInitialized;

    public CirclePanelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaintTest = new Paint();
        mPaintTest.setColor(Color.BLACK);                    //设置画笔颜色
        mPaintTest.setStrokeWidth(1.0f);              //线宽
        mPaintTest.setStyle(Paint.Style.STROKE);
        mPaintTest.setAntiAlias(true);

        mMainBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_main, null);
        centerX = mMainBmp.getWidth() / 2;
        centerY = mMainBmp.getHeight() / 2 + 1;
        mOffset = new float[]{-mMainBmp.getWidth() * 1.5f, -mMainBmp.getHeight()/2};
        mMainSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_main_select, null);
        mOpacBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_opacity, null);
        mOpacSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_opacity_select, null);
        mBrushSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_brush_select, null).extractAlpha();
        //mBrushUnselectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_brush_unselect, null);
        menuMode = MenuMode.DEFAULT;
        mMatrix = new Matrix();
        mScrollValue = 0;
        mOpacityValueTmp = 0;
        mColorValueTmp = 0;
        mBrushValueTmp = 0;
        mOpacityCount = 20;
        mBrushCount = 12;
        isAwake = false;
        mGestureCenter = null;
        isAngleInitialized = false;
        mVectorMoved = new float[2];
        mOpacityRange = 255;
        mBrushRange = 6;
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

        mOpacityValue = 255;
        mBrushValue = 1.5f;
        mColorValue = 0xffaaaaaa;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mGestureCenter != null) {
            Log.d("bbbbb", "Center: " + mGestureCenter[0] + "  " + mGestureCenter[1]);
            canvas.drawText(String.valueOf(mAng),1500,100,mPaintTest);
            canvas.drawCircle(mGestureCenter[0], mGestureCenter[1], 20, mPaintTest);
        }
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

            switch (menuMode) {
                case OPACITY:
                    mOpacityValue = mOpacityValueTmpCal;
                    break;
                case COLOR:
                    mColorValue = mColorValueTmpCal;
                    break;
                case BRUSH:
                    mBrushValue = mBrushValueTmpCal;
                    break;
            }
            menuMode = MenuMode.DEFAULT;
        }
        isAngleInitialized = false;
        invalidate();
    }

    private void setMode() {
        menuMode = tmpMenuMode;
        switch (menuMode) {
            case OPACITY:
                //mOpacityValueTmp = mOpacityValue;
                break;
            case COLOR:
                //mColorValueTmp = mColorValue;
                break;
            case BRUSH:
                //mBrushValueTmp = mBrushValue;
                break;
        }
    }

    public void menuScrollNext() {
        switch (menuMode) {
            case MAIN:
                mScrollValue += 1;
                break;
            case OPACITY:
                mOpacityValueTmp = mOpacityValueTmp < mOpacityCount ? mOpacityValueTmp + 1 : mOpacityValueTmp;
                break;
            case COLOR:
                mColorValueTmp = mColorValueTmp < mColorCount - 1 ? mColorValueTmp + 1 : mColorValueTmp;
                break;
            case BRUSH:
                mBrushValueTmp = mBrushValueTmp < mBrushCount - 1 ? mBrushValueTmp + 1 : mBrushValueTmp;//do not need reach 360 deg
                break;
        }
        invalidate();
    }

    //根据当前手势初始化二级圆盘
    public void menuScroll() {
        float f;
        if (!isAngleInitialized) {
            setInitialAngle( AngleBetween());
            isAngleInitialized = true;
        }
        mAng = AngleBetween()-mAngIni;
        if(mAng>180){
            mAng = 360-mAng;
        }else if(mAng<=-180){
            mAng = 360+mAng;
        }
        Log.i("angleeee", String.valueOf(mAng));
        f=-mAng;

        switch (menuMode) {
            case MAIN:
                mScrollValue = (int) f / 25;
                break;
            case OPACITY:
                if (f < 0) {
                    mOpacityValueTmp = 0;
                } else {
                    mOpacityValueTmp = (int) (f / (60 / mOpacityCount)) < mOpacityCount ? (int) (f / (60 / mOpacityCount)) : mOpacityCount;
                }
                break;
            case COLOR:
                if (f < 0) {
                    mColorValueTmp = mColorCount-1 - (-(int) (f / (60 / mColorCount)) < mColorCount-1 ? -(int) (f / (60 / mColorCount)) : mColorCount-1);
                } else {
                    mColorValueTmp = (int) (f / (60 / mColorCount)) < mColorCount-1 ? (int) (f / (60 / mColorCount)) : mColorCount-1;
                }
                break;
            case BRUSH:
                if (f < 0) {
                    mBrushValueTmp = 0;
                } else {
                    mBrushValueTmp = (int) (f / (60 / mBrushCount)) < mBrushCount ? (int) (f / (60 / mBrushCount)) : mBrushCount;
                }
                break;
        }
        invalidate();
    }

    //使用暂存数据初始化二级圆盘
    public void menuScrollT(float f) {
        switch (menuMode) {
            case MAIN:
                mScrollValue = (int) f / 15;
                break;
            //            case OPACITY:
            //                mOpacityValueTmp = mOpacityValueTmp + (int) (f / mColorCount) < mOpacityCount ? mOpacityValueTmp + i : mOpacityCount;
            //                break;
            //            case COLOR:
            //                mColorValueTmp = mColorValueTmp + i < mColorCount - 1 ? mColorValueTmp + i : mColorCount - 1;
            //                break;
            //            case BRUSH:
            //                mBrushValueTmp = mBrushValueTmp + i < mBrushCount - 1 ? mBrushValueTmp + i : mBrushCount - 1;//do not need reach 360 deg
            //                break;
        }
        invalidate();
    }

    public void menuScrollPrev() {
        switch (menuMode) {
            case MAIN:
                mScrollValue -= 1;
                break;
            case OPACITY:
                mOpacityValueTmp = mOpacityValueTmp > 0 ? mOpacityValueTmp - 1 : 0;
                break;
            case COLOR:
                mColorValueTmp = mColorValueTmp > 0 ? mColorValueTmp - 1 : 0;
                break;
            case BRUSH:
                mBrushValueTmp = mBrushValueTmp > 0 ? mBrushValueTmp - 1 : 0;
                break;
        }
        invalidate();
    }

    public void awake() {
        setPosition();
        mScrollValue = 0;
        menuMode = MenuMode.MAIN;
        isAwake = true;
        invalidate();
    }

    public void dismiss() {
        menuMode = MenuMode.DEFAULT;
        setGestureCenter();
        isAwake = false;
        isAngleInitialized = false;
        invalidate();
    }

    public void setGestureCenter(float x, float y)

    {
        mGestureCenter = new float[2];
        mGestureCenter[0] = x;
        mGestureCenter[1] = y+20;

    }

    public void setGestureCenter() {
        mGestureCenter = null;
    }

    public float[] getGestureCenter() {
        return mGestureCenter;
    }

    public boolean isAwake() {
        return isAwake;
    }

    public int getmOpacityValue() {
        return mOpacityValue;
    }

    public int getmColorValue() {
        return mColorValue;
    }

    public float getmBrushValue() {
        return mBrushValue;
    }

    public void setInitialAngle(float d) {
        mAngIni = d;
    }

    public double getInitialAngle() {
        return mAngIni;
    }

    /**
     * 设置圆盘在手势左边
     */
    public void setPosition() {
        posX=mGestureCenter[0] + mOffset[0];
        posY=mGestureCenter[1] + mOffset[1];
    }

    private MenuMode drawMainMenu(Canvas canvas) {
        //mScrollValue = 0;
        canvas.drawBitmap(mMainBmp, posX, posY, mPaint);
        int i = mScrollValue % 3;
        MenuMode selectedMode = MenuMode.MAIN;
        mMatrix.setRotate(-120 * i, centerX, centerY);
        mMatrix.postTranslate(posX,posY);
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
        canvas.drawBitmap(mOpacBmp, posX, posY, mPaint);
        int i = mOpacityValueTmp;
        mMatrix.setRotate(-i * 360 / mOpacityCount, centerX, centerY);
        mMatrix.postTranslate(posX,posY);
        mOpacityValueTmpCal = mOpacityRange-mOpacityRange/mOpacityCount*mOpacityValueTmp;
        canvas.drawBitmap(mOpacSelectBmp, mMatrix, mPaint);
    }

    private void drawBrushMenu(Canvas canvas) {
        canvas.drawBitmap(mMainBmp, posX,posY, mPaint);
        int i = mBrushValueTmp;
        mPaint.setColor(0xff000000);
        for (int n = 0; n <= i; n++) {
            mMatrix.setRotate(-n * 360 / mBrushCount, centerX, centerY);
            mMatrix.postTranslate(posX,posY);
            canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
        }
        mPaint.setColor(0xffC9C9CA);
        for (int t = 1; t < mBrushCount - i; t++) {
            mMatrix.setRotate(t * 360 / mBrushCount, centerX, centerY);
            mMatrix.postTranslate(posX,posY);
            canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
        }
        mBrushValueTmpCal = mBrushRange/mBrushCount*mBrushValueTmp;

    }

    private void drawColorMenu(Canvas canvas) {
        canvas.drawBitmap(mMainBmp, posX, posY, mPaint);
        int i = mColorValueTmp;
        for (int n = 0; n < mColorCount; n++) {
            mPaint.setColor(palette[n]);
            mMatrix.setRotate(-n * 360 / mColorCount, centerX, centerY);
            mMatrix.postTranslate(posX,posY);
            canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
        }

        mMatrix.setScale(1.1f, 1.1f, centerX, centerY);
        mMatrix.postRotate(-i * 360 / mColorCount, centerX, centerY);
        mMatrix.postTranslate(posX,posY);
        mPaint.setColor(palette[i]);
        canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
        mColorValueTmpCal = palette[mColorValueTmp];
    }

    public void setVectorMoved(float x, float y) {
        mVectorMoved[0] = x;
        mVectorMoved[1] = y;
    }

    public float AngleBetween() {
        float detaX = mVectorMoved[0] - mGestureCenter[0];
        float detaY = mVectorMoved[1] - mGestureCenter[1];
        double d;
        //坐标在四个象限里
        if (detaX != 0) {
            float tan = Math.abs(detaY / detaX);

            if (detaX > 0) {

                //第一象限
                if (detaY >= 0) {
                    d = Math.atan(tan);

                } else {
                    //第四象限
                    d = 2 * Math.PI - Math.atan(tan);
                }

            } else {
                if (detaY >= 0) {
                    //第二象限
                    d = Math.PI - Math.atan(tan);
                } else {
                    //第三象限
                    d = Math.PI + Math.atan(tan);
                }
            }

        } else {
            //坐标在y轴上
            if (detaY > 0) {
                //坐标在y>0上
                d = Math.PI / 2;
            } else {
                //坐标在y<0上
                d = -Math.PI / 2;
            }
        }

        return (float) ((d * 180) / Math.PI);

    }


    private enum MenuMode {
        DEFAULT, MAIN, BRUSH, OPACITY, COLOR
    }
}
