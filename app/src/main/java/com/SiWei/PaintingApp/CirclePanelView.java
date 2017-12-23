package com.SiWei.PaintingApp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by yxxy6 on 10/29/2017.
 */

public class CirclePanelView extends View {
    private int mScrollValue, mColorValue, mOpacityValue, mOpacityRange, mOpacityValueTmpCal;
    private float mBrushValue;
    private float mBrushRange;
    private int mOpacityValueTmp, mColorValueTmp, mBrushValueTmp;
    private int mOpacityValueTmpRec, mColorValueTmpRec, mBrushValueTmpRec;
    private float mBrushValueTmpCal;
    private int mColorValueTmpCal;
    private int mOpacityCount, mColorCount, mBrushCount;
    private Bitmap mMainBmp,mMainBmp02, mOpacBmp, mOpacSelectBmp;
    private Bitmap mMainSelectBmp, mMainSelectBmp02, mMainSelectBmp03;
    private Bitmap mColorSelectBmp, mColorSelectBmp02, mColorSelectBmp03, mColorSelectBmp04, mColorSelectBmp05, mColorSelectBmp06;
    private Bitmap mBrushSelectBmp, mBrushSelectBmp02;
    private Matrix mMatrix;
    private MenuMode menuMode, tmpMenuMode;
    private Paint mPaint, mPaintTest,mPaintPath,mPaintColor;
    private int[] palette;
    private float centerX, centerY, posX, posY;
    private float[] mOffset, mOffsetCenter;
    private boolean isAwake;
    private float[] mGestureCenter, mVectorMoved;
    private float mAng, mAngIni;
    private boolean isAngleInitialized;
    public boolean isColorChanged;//画笔颜色有没有由圆盘控件改变
    private Path textPath;
    private String opacName,brushName,colorName;

    public CirclePanelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        //mPaint.setDither(true);
        mPaintTest = new Paint();
        mPaintTest.setColor(Color.WHITE);                    //设置画笔颜色
        mPaintTest.setStrokeWidth(1.0f);              //线宽
        mPaintTest.setStyle(Paint.Style.FILL);
        mPaintTest.setTextAlign(Paint.Align.CENTER);
        mPaintTest.setTextSize(20);
        mPaintTest.setAntiAlias(true);

        mPaintPath = new Paint();
        mPaintPath.setAntiAlias(true);
        mPaintPath.setColor(Color.WHITE);                    //设置画笔颜色
        mPaintPath.setStrokeWidth(1.0f);              //线宽
        mPaintPath.setStyle(Paint.Style.FILL);
        mPaintPath.setTextAlign(Paint.Align.CENTER);
        mPaintPath.setTextSize(12f);

        mPaintColor = new Paint();
        mPaintColor.setAntiAlias(true);


        mMainBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_main_02, null);
        mMainBmp02 =BitmapFactory.decodeResource(getResources(), R.drawable.cir_main_02, null);
        centerX = mMainBmp.getWidth() / 2;
        centerY = mMainBmp.getHeight() / 2 + 1;
        mOffset = new float[]{-mMainBmp.getWidth() * 1.5f, -mMainBmp.getHeight() / 2};
        mOffsetCenter = new float[]{mMainBmp.getWidth() * 0.5f, mMainBmp.getHeight() / 2};

        mMainSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_main_select, null);
        mMainSelectBmp02 = BitmapFactory.decodeResource(getResources(), R.drawable.cir_main_select_02, null);
        mMainSelectBmp03 = BitmapFactory.decodeResource(getResources(), R.drawable.cir_main_select_03, null);

        mOpacBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_opacity, null);
        mOpacSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_opacity_select, null);

        mColorSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_color_select, null).extractAlpha();
        mColorSelectBmp02 = BitmapFactory.decodeResource(getResources(), R.drawable.cir_color_select_02, null).extractAlpha();
        mColorSelectBmp03 = BitmapFactory.decodeResource(getResources(), R.drawable.cir_color_select_03, null).extractAlpha();
        mColorSelectBmp04 = BitmapFactory.decodeResource(getResources(), R.drawable.cir_color_select_04, null).extractAlpha();
        mColorSelectBmp05 = BitmapFactory.decodeResource(getResources(), R.drawable.cir_color_select_05, null).extractAlpha();
        mColorSelectBmp06 = BitmapFactory.decodeResource(getResources(), R.drawable.cir_color_select_06, null).extractAlpha();

        mBrushSelectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_brush_select, null).extractAlpha();
        mBrushSelectBmp02 = BitmapFactory.decodeResource(getResources(), R.drawable.cir_brush_select_02, null).extractAlpha();
        //mBrushUnselectBmp = BitmapFactory.decodeResource(getResources(), R.drawable.cir_brush_unselect, null);
        menuMode = MenuMode.DEFAULT;
        mMatrix = new Matrix();
        mScrollValue = 0;
        mOpacityValueTmp = 0;
        mColorValueTmp = 0;
        mBrushValueTmp = 0;
        mOpacityCount = 20;
        mBrushCount = 8;
        isAwake = false;
        mGestureCenter = null;
        isAngleInitialized = false;
        mVectorMoved = new float[2];
        mOpacityRange = 255;
        mBrushRange = 8.75f;
        palette = new int[]{
                0xff86B32C,
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
        isColorChanged = false;
        textPath = new Path();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CirclePanelView);
        opacName = ta.getString(R.styleable.CirclePanelView_opacity_name);
        brushName = ta.getString(R.styleable.CirclePanelView_brush_name);
        colorName = ta.getString(R.styleable.CirclePanelView_color_name);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //        if (mGestureCenter != null) {
        //            Log.d("bbbbb", "Center: " + mGestureCenter[0] + "  " + mGestureCenter[1]);
        //
        //            canvas.drawCircle(mGestureCenter[0], mGestureCenter[1], 20, mPaintTest);
        //        }
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
                    mOpacityValueTmpRec = mOpacityValueTmp;
                    break;
                case COLOR:
                    mColorValue = mColorValueTmpCal;
                    mColorValueTmpRec = mColorValueTmp;
                    isColorChanged = true;
                    break;
                case BRUSH:
                    mBrushValue = mBrushValueTmpCal;
                    mBrushValueTmpRec = mBrushValueTmp;
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

    /**
     * 根据当前手势初始化二级圆盘
     */
    public void menuScroll() {
        float f;
        if (!isAngleInitialized) {
            setInitialAngle(AngleBetween());
            isAngleInitialized = true;
        }
        mAng = AngleBetween() - mAngIni;
        if (mAng > 180) {
            mAng = 360 - mAng;
        } else if (mAng <= -180) {
            mAng = 360 + mAng;
        }
        Log.i("angleeee", String.valueOf(mAng));
        f = -mAng;

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
                    mColorValueTmp = mColorCount - 1 - (-(int) (f / (60 / mColorCount)) < mColorCount - 1 ? -(int) (f / (60 / mColorCount)) : mColorCount - 1);
                } else {
                    mColorValueTmp = (int) (f / (60 / mColorCount)) < mColorCount - 1 ? (int) (f / (60 / mColorCount)) : mColorCount - 1;
                }
                break;
            case BRUSH:
                if (f < 0) {
                    mBrushValueTmp = 0;
                } else {
                    mBrushValueTmp = (int) (f / (60 / mBrushCount)) < mBrushCount - 1 ? (int) (f / (60 / mBrushCount)) : mBrushCount - 1;
                }
                break;
        }
        invalidate();
    }

    /**
     * 使用暂存数据初始化二级圆盘
     */
    public void menuScrollT() {
        float f;
        if (!isAngleInitialized) {
            setInitialAngle(AngleBetween());
            isAngleInitialized = true;
        }
        mAng = AngleBetween() - mAngIni;
        if (mAng > 180) {
            mAng = 360 - mAng;
        } else if (mAng <= -180) {
            mAng = 360 + mAng;
        }
        Log.i("angleeee", String.valueOf(mAng));
        f = -mAng;

        switch (menuMode) {
            case MAIN:
                mScrollValue = (int) f / 25;
                break;
            case OPACITY:
                if (f < 0) {
                    mOpacityValueTmp = (mOpacityValueTmpRec + (int) (f / (60 / mOpacityCount))) > 0 ? mOpacityValueTmpRec + (int) (f / (60 / mOpacityCount)) : 0;
                } else {
                    mOpacityValueTmp = (mOpacityValueTmpRec + (int) (f / (60 / mOpacityCount))) < mOpacityCount ? mOpacityValueTmpRec + (int) (f / (60 / mOpacityCount)) : mOpacityCount;
                }
                break;
            case COLOR:
                mColorValueTmp = mColorValueTmpRec + (int) (f / (60 / mColorCount));
                break;
            case BRUSH:
                if (f < 0) {
                    mBrushValueTmp = (mBrushValueTmpRec + (int) (f / (60 / mBrushCount))) > 0 ? mBrushValueTmpRec + (int) (f / (60 / mBrushCount)) : 0;
                } else {
                    mBrushValueTmp = (mBrushValueTmpRec + (int) (f / (60 / mBrushCount))) < mBrushCount - 1 ? mBrushValueTmpRec + (int) (f / (60 / mBrushCount)) : mBrushCount - 1;
                }
                break;
        }
        invalidate();
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

        if (mGestureCenter == null) {
            mGestureCenter = new float[2];
            mGestureCenter[0] = 600;
            mGestureCenter[1] = 200;
        }
        isColorChanged = false;
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
        mGestureCenter[1] = y + 20;

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
     * 控件外设置颜色时，存入圆盘暂存数值
     */
    public void setColorValueTmpRec(int v) {
        mColorValueTmpRec = v;
    }

    public int[] getPaletteRequired(int[] index) {
        int[] paletteReq = new int[index.length];
        for (int i = 0; i < index.length; i++) {
            paletteReq[i] = palette[index[i]];
        }
        return paletteReq;
    }

    /**
     * 设置圆盘在手势左边
     */
    public void setPosition() {
        posX = mGestureCenter[0] + mOffset[0];
        posY = mGestureCenter[1] + mOffset[1];
    }

    private MenuMode drawMainMenu(Canvas canvas) {
        //mScrollValue = 0;
        canvas.drawBitmap(mMainBmp02, posX, posY, mPaint);
        int i = mScrollValue % 3;
//        canvas.drawText("opacity", posX + mOffsetCenter[0], posY + mOffsetCenter[1] -12, mPaintTest);
//        canvas.drawText("color", posX + mOffsetCenter[0]-10, posY + mOffsetCenter[1]+12, mPaintTest);
//        canvas.drawText("brush", posX + mOffsetCenter[0]+10, posY + mOffsetCenter[1]+12, mPaintTest);
       // textPath.addCircle(posX + mOffsetCenter[0], posY + mOffsetCenter[1], 30, Path.Direction.CW);
        textPath.addArc(new RectF(posX + mOffsetCenter[0]-30,
                posY + mOffsetCenter[1]-30,
                posX + mOffsetCenter[0]+30,
                posY + mOffsetCenter[1]+30),
                210,120);
        canvas.drawTextOnPath(opacName,textPath,0,12,mPaintPath);
        textPath.reset();

        textPath.addArc(new RectF(posX + mOffsetCenter[0]-30,
                        posY + mOffsetCenter[1]-30,
                        posX + mOffsetCenter[0]+30,
                        posY + mOffsetCenter[1]+30),
                330,120);
        canvas.drawTextOnPath(brushName,textPath,0,12,mPaintPath);
        textPath.reset();

        textPath.addArc(new RectF(posX + mOffsetCenter[0]-30,
                        posY + mOffsetCenter[1]-30,
                        posX + mOffsetCenter[0]+30,
                        posY + mOffsetCenter[1]+30),
                90,120);
        canvas.drawTextOnPath(colorName,textPath,0,12,mPaintPath);
        textPath.reset();

        MenuMode selectedMode = MenuMode.MAIN;
        //        mMatrix.setRotate(-120 * i, centerX, centerY);
        //        mMatrix.postTranslate(posX,posY);

        switch (i) {
            case 0:
                selectedMode = MenuMode.OPACITY;
                canvas.drawBitmap(mMainSelectBmp, posX, posY, mPaint);
                break;
            case 1:
                selectedMode = MenuMode.COLOR;
                canvas.drawBitmap(mMainSelectBmp02, posX, posY, mPaint);
                break;
            case 2:
                selectedMode = MenuMode.BRUSH;
                canvas.drawBitmap(mMainSelectBmp03, posX, posY, mPaint);
                break;
            case -1:
                selectedMode = MenuMode.BRUSH;
                canvas.drawBitmap(mMainSelectBmp03, posX, posY, mPaint);
                break;
            case -2:
                selectedMode = MenuMode.COLOR;
                canvas.drawBitmap(mMainSelectBmp02, posX, posY, mPaint);
                break;
        }
        return selectedMode;
    }

    private void drawOpacMenu(Canvas canvas) {
        canvas.drawBitmap(mMainBmp, posX, posY, mPaint);
        canvas.drawBitmap(mOpacBmp, posX, posY, mPaint);
        int i = mOpacityValueTmp;
        mMatrix.setRotate(-i * 360 / mOpacityCount, centerX, centerY);
        mMatrix.postTranslate(posX, posY);
        mOpacityValueTmpCal = mOpacityRange - mOpacityRange / mOpacityCount * mOpacityValueTmp;
        canvas.drawBitmap(mOpacSelectBmp, mMatrix, mPaint);
        canvas.drawText(String.valueOf((mOpacityCount - mOpacityValueTmp) * 5) + "%", posX + mOffsetCenter[0], posY + mOffsetCenter[1] + 10, mPaintTest);
    }

    private void drawBrushMenu(Canvas canvas) {
        canvas.drawBitmap(mMainBmp, posX, posY, mPaint);
        int i = mBrushValueTmp;
        mPaint.setColor(0xff000000);
        int p, q;
        for (int n = 0; n <= i; n++) {
            p = (int) Math.floor(n / 2);
            q = n % 2;
            mMatrix.setRotate(-p * 360 / mBrushCount * 2, centerX, centerY);
            mMatrix.postTranslate(posX, posY);
            switch (q) {
                case 0:
                    canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
                    break;
                case 1:
                    canvas.drawBitmap(mBrushSelectBmp02, mMatrix, mPaint);
                    break;
            }

        }
        mPaint.setColor(0xffC9C9CA);

        for (int t = i + 1; t < mBrushCount; t++) {
            q = t % 2;
            p = (int) Math.floor(t / 2);
            mMatrix.setRotate(-p * 360 / mBrushCount * 2, centerX, centerY);
            mMatrix.postTranslate(posX, posY);
            switch (q) {
                case 0:
                    canvas.drawBitmap(mBrushSelectBmp, mMatrix, mPaint);
                    break;
                case 1:
                    canvas.drawBitmap(mBrushSelectBmp02, mMatrix, mPaint);
                    break;
            }
        }

        mBrushValueTmpCal = mBrushRange / (mBrushCount - 1) * mBrushValueTmp + 0.25f;
        canvas.drawText(String.valueOf(mBrushValueTmpCal), posX + mOffsetCenter[0], posY + mOffsetCenter[1] + 10, mPaintTest);


    }

    private void drawColorMenu(Canvas canvas) {
        canvas.drawBitmap(mMainBmp, posX, posY, mPaint);
        int i;
        if (mColorValueTmp >= 0) {
            i = mColorValueTmp % 12;
        } else if (mColorValueTmp % 12 != 0) {
            i = 12 + mColorValueTmp % 12;
        } else {
            i = 0;
        }
        Log.i("ceshi", "drawColorMenu: " + i);
        int p, q;
        for (int n = 0; n < mColorCount; n++) {
            p = (int) Math.floor(n / 3);
            q = n % 3;
            mPaint.setColor(palette[n]);
            mMatrix.setRotate(-p * 360 / mColorCount * 3, centerX, centerY);
            mMatrix.postTranslate(posX, posY);
            switch (q) {
                case 0:
                    canvas.drawBitmap(mColorSelectBmp, mMatrix, mPaint);
                    break;
                case 1:
                    canvas.drawBitmap(mColorSelectBmp02, mMatrix, mPaint);
                    break;
                case 2:
                    canvas.drawBitmap(mColorSelectBmp03, mMatrix, mPaint);
                    break;
            }
        }

        //mMatrix.setScale(1.0f, 1.0f, centerX, centerY);
        p = (int) Math.floor(i / 3);
        mMatrix.setRotate(-p * 360 / mColorCount * 3, centerX, centerY);
        mMatrix.postTranslate(posX, posY);
        mPaint.setColor(palette[i]);
        q = i % 3;
        switch (q) {
            case 0:
                canvas.drawBitmap(mColorSelectBmp04, mMatrix, mPaint);
                break;
            case 1:
                canvas.drawBitmap(mColorSelectBmp05, mMatrix, mPaint);
                break;
            case 2:
                canvas.drawBitmap(mColorSelectBmp06, mMatrix, mPaint);
                break;
        }

        mColorValueTmpCal = palette[i];
        mPaintColor.setColor(palette[i]);
        canvas.drawCircle(posX+mOffsetCenter[0],posY+mOffsetCenter[1],25,mPaintColor);
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
