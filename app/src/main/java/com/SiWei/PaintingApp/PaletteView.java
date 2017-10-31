package com.SiWei.PaintingApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PaletteView extends View {

    private Paint mPaint;
    private Path mPath;
    private float mLastX;
    private float mLastY;
    //private Bitmap mBufferBitmap;
    //private Canvas mBufferCanvas;
    private Bitmap[] mBitmaps = new Bitmap[10];
    private Canvas mCanvas;
    private int count = 0;
    private int countNow = 0;

    private static final int MAX_CACHE_STEP = 100;

    private List<DrawingInfo> mDrawingList;
    private List<DrawingInfo> mRemovedList;

    private Xfermode mClearMode;
    private float mDrawSize;
    private float mEraserSize;

    private boolean mCanEraser;

    private Callback mCallback;

    private Pen mPen;
    private PenColor mColor;

    private Bitmap mBitmap; // 当前涂鸦的原图（旋转后）
    private Bitmap mBitmapEraser; // 橡皮擦底图
    private Bitmap mGraffitiBitmap; // 用绘制涂鸦的图片
    private Canvas mBitmapCanvas;

    // 当前选择的文字信息
    private GraffitiSelectableItem mSelectedItem;

    private float mSelectedItemX, mSelectedItemY;
    private boolean mIsRotatingSelectedItem;
    private float mRotateTextDiff; // 开始旋转图片时的差值（当前图片与触摸点的角度）

    private final RectF dirtyRect = new RectF();

    public enum Mode {
        DRAW,
        ERASER
    }

    public enum Pen {
        HAND, // 手绘
        COPY, // 仿制
        ERASER, // 橡皮擦
        TEXT, // 文本
        BITMAP, // 贴图
    }


    private Mode mMode = Mode.DRAW;

    public PaletteView(Context context) {
        this(context,null);
    }

    public PaletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        initBuffer();
        init();
    }

    public interface Callback {
        void onUndoRedoStatusChanged();
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }

    private void init() {
        //this.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setFilterBitmap(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        //mPaint.setColor(Color.WHITE);
        mDrawSize = 5;
        mEraserSize = 80;
        mPaint.setStrokeWidth(mDrawSize);
        mPaint.setColor(0XFFcccccc);
        // 反锯齿
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        mPen = Pen.HAND;
        mColor = new PenColor(Color.RED);

    }

    private void initBuffer(){
        mBitmaps[countNow] = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
        //mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmaps[countNow]);
        //mBufferCanvas = new Canvas(mBufferBitmap);
    }

    private abstract static class DrawingInfo {
        Paint paint;
        abstract void draw(Canvas canvas);
    }

    private static class PathDrawingInfo extends DrawingInfo{

        Path path;

        @Override
        void draw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }
    }

    private int mGraffitiRotateDegree = 0; // 相对于初始图片旋转的角度

    public int getGraffitiRotateDegree() {
        return mGraffitiRotateDegree;
    }

    public Mode getMode() {
        return mMode;
    }

    private void loadBitmap(){
        mCanvas.setBitmap(mBitmaps[countNow]);
    }

    public void crateNewPage(){
        if(count < 9) {
            count++;
            countNow = count;
            mBitmaps[countNow] = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
            //mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            //mBufferCanvas = new Canvas(mBufferBitmap);
            loadBitmap();
            invalidate();
        }
    }

    public void prevPage(){
        if(countNow > 0){
            countNow --;
            loadBitmap();
            invalidate();
        }
    }

    public void nextPage(){
        if(countNow < 9){
            countNow ++;
            loadBitmap();
            invalidate();
        }
    }

    public void setMode(Mode mode) {
        if (mode != mMode) {
            mMode = mode;
            if (mMode == Mode.DRAW) {
                mPaint.setXfermode(null);
                mPaint.setStrokeWidth(mDrawSize);
                //Log.v("size", toString().valueOf(mDrawSize));
            } else {
                mPaint.setXfermode(mClearMode);
                mPaint.setStrokeWidth(mEraserSize);
            }
        }
    }

    public void setEraserSize(float size) {
        mEraserSize = size*3;
        mPaint.setStrokeWidth(size);
    }

    public void setPenRawSize(float size) {
        mDrawSize = size;
        mPaint.setStrokeWidth(size);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    public void setColor(Bitmap bitmap) {
        if (mBitmap == null) {
            return;
        }
        mColor.setColor(bitmap);
        invalidate();
    }

    public PenColor getColor() {
        return mColor;
    }

    public boolean isSelectedItem() {
        return mSelectedItem != null;
    }

    public void setSelectedItemColor(int color) {
        if (mSelectedItem == null) {
            throw new NullPointerException("Selected item is null!");
        }
        mSelectedItem.getColor().setColor(color);
        invalidate();
    }

    public void setSelectedItemColor(Bitmap bitmap) {
        if (mSelectedItem == null) {
            throw new NullPointerException("Selected item is null!");
        }
        if (mBitmap == null) {
            return;
        }
        mSelectedItem.getColor().setColor(bitmap);
        invalidate();
    }

    public void setPenAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    private void reDraw(){
        if (mDrawingList != null) {
            mBitmaps[countNow].eraseColor(Color.TRANSPARENT);
            for (DrawingInfo drawingInfo : mDrawingList) {
                drawingInfo.draw(mCanvas);
            }
            invalidate();
        }
    }

    public boolean canRedo() {
        return mRemovedList != null && mRemovedList.size() > 0;
    }

    public boolean canUndo(){
        return mDrawingList != null && mDrawingList.size() > 0;
    }


    public void redo() {
        int size = mRemovedList == null ? 0 : mRemovedList.size();
        if (size > 0) {
            DrawingInfo info = mRemovedList.remove(size - 1);
            mDrawingList.add(info);
            mCanEraser = true;
            reDraw();
            if (mCallback != null) {
                mCallback.onUndoRedoStatusChanged();
            }
        }
    }

    public void undo() {
        int size = mDrawingList == null ? 0 : mDrawingList.size();
        if (size > 0) {
            DrawingInfo info = mDrawingList.remove(size - 1);
            if (mRemovedList == null) {
                mRemovedList = new ArrayList<>(MAX_CACHE_STEP);
            }
            if (size == 1) {
                mCanEraser = false;
            }
            mRemovedList.add(info);
            reDraw();
            if (mCallback != null) {
                mCallback.onUndoRedoStatusChanged();
            }
        }
    }

    public void clear() {
        if (mBitmaps[countNow] != null) {
            if (mDrawingList != null) {
                mDrawingList.clear();
            }
            if (mRemovedList != null) {
                mRemovedList.clear();
            }
            mCanEraser = false;
            mBitmaps[countNow].eraseColor(Color.TRANSPARENT);
            invalidate();
            if (mCallback != null) {
                mCallback.onUndoRedoStatusChanged();
            }
        }
    }



    public Bitmap buildBitmap() {
        Bitmap bm = getDrawingCache();
        Bitmap result = Bitmap.createBitmap(bm);
        destroyDrawingCache();
        return result;
    }

    private void saveDrawingPath(){
        if (mDrawingList == null) {
            mDrawingList = new ArrayList<>(MAX_CACHE_STEP);
        } else if (mDrawingList.size() == MAX_CACHE_STEP) {
            mDrawingList.remove(0);
        }
        Path cachePath = new Path(mPath);
        Paint cachePaint = new Paint(mPaint);
        PathDrawingInfo info = new PathDrawingInfo();
        info.path = cachePath;
        info.paint = cachePaint;
        mDrawingList.add(info);
        mCanEraser = true;
        if (mCallback != null) {
            mCallback.onUndoRedoStatusChanged();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmaps[countNow] != null) {
            canvas.drawBitmap(mBitmaps[countNow], 0, 0, null);
        }
        //canvas.drawBitmap(mBitmaps[countNow], 0, 0, null);
        canvas.drawPath(mPath,mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final float x = event.getX();
        final float y = event.getY();
        float touchSize;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchSize = event.getSize();
                if(touchSize > 0.005){
                    setMode(Mode.ERASER);
                    setEraserSize(100);
                }else{
                    setMode(Mode.DRAW);
                }
                mLastX = x;
                mLastY = y;
                if (mPath == null) {
                    mPath = new Path();
                }
                mPath.moveTo(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                //这里终点设为两点的中心点的目的在于使绘制的曲线更平滑，如果终点直接设置为x,y，效果和lineto是一样的,实际是折线效果
                if (mMode == Mode.ERASER && !mCanEraser) {
                    break;
                }
                mPath.quadTo(mLastX, mLastY, (x + mLastX)/2, (y + mLastY)/2);
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (mMode == Mode.DRAW || mCanEraser) {
                    saveDrawingPath();
                }
                mCanvas.drawPath(mPath,mPaint);
                mPath.reset();
                break;
        }
        invalidate();
        return true;
    }
}
