package com.SiWei.PaintingApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.CopyOnWriteArrayList;

public class PaletteView extends View {

    //private Bitmap mBufferBitmap;
    //private Canvas mBufferCanvas;
    private Bitmap[] mBitmaps = new Bitmap[10];//位图数组
    //private Canvas mCanvas;
    private int count = 0;
    private int countNow = 0;

    private static final int MAX_CACHE_STEP = 100;

    private Callback mCallback;

    private final RectF dirtyRect = new RectF();

    public static final int ERROR_INIT = -1;
    public static final int ERROR_SAVE = -2;

    private static final float VALUE = 1f;
    private final int TIME_SPAN = 80;

    private GraffitiListener mGraffitiListener;

    private Bitmap mBitmap; // 原图
    private Bitmap mBitmapEraser; // 橡皮擦底图
    //private Bitmap mGraffitiBitmap; // 用绘制涂鸦的图片
    private Canvas mBitmapCanvas;

    private float mPrivateScale; // 图片适应屏幕（mScale=1）时的缩放倍数
    private int mPrivateHeight, mPrivateWidth;// 图片在缩放mPrivateScale倍数的情况下，适应屏幕（mScale=1）时的大小（肉眼看到的在屏幕上的大小）
    private float mCentreTranX, mCentreTranY;// 图片在缩放mPrivateScale倍数的情况下，居中（mScale=1）时的偏移（肉眼看到的在屏幕上的偏移）

    private BitmapShader mBitmapShader; // 用于涂鸦的图片上!
    private BitmapShader mBitmapShaderEraser; // 橡皮擦底图
    private Path mCurrPath; // 当前手写的路径
    private Path mTempPath;

    private Paint mPaint;
    private int mTouchMode; // 触摸模式，用于判断单点或多点触摸
    private int mTouchModeTemp = 1;
    private float mPaintSize;
    private PenColor mColor; // 画笔底色
    private float mScale; // 图片在相对于居中时的缩放倍数 （ 图片真实的缩放倍数为 mPrivateScale*mScale ）

    private float mTransX = 0, mTransY = 0; // 图片在相对于居中时且在缩放mScale倍数的情况下的偏移量 （ 图片真实偏移量为　(mCentreTranX + mTransX)/mPrivateScale*mScale ）


    private boolean mIsPainting = false; // 是否正在绘制
    private boolean isJustDrawOriginal; // 是否只绘制原图

    private boolean mIsDrawableOutside = false; // 触摸时，图片区域外是否绘制涂鸦轨迹
    private boolean mEraserImageIsResizeable;
    private boolean mReady = false;

    private boolean mIsEraserSelected = false;


    // 保存涂鸦操作，便于撤销
    //private CopyOnWriteArrayList<GraffitiPath> mPathStack = new CopyOnWriteArrayList<GraffitiPath>();
//    private CopyOnWriteArrayList<GraffitiPath> mPathStackBackup = new CopyOnWriteArrayList<GraffitiPath>();
    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<Undoable> mUndoStack = new CopyOnWriteArrayList<Undoable>();
    private CopyOnWriteArrayList<GraffitiPath> mPathStack = new CopyOnWriteArrayList<GraffitiPath>();
    private CopyOnWriteArrayList<GraffitiSelectableItem> mSelectableStack = new CopyOnWriteArrayList<>();


    private Pen mPen;
    private Shape mShape;

    private float mTouchDownX, mTouchDownY, mLastTouchX, mLastTouchY, mTouchX, mTouchY;
    private Matrix mShaderMatrix, mMatrixTemp;

    private int mOriginalWidth, mOriginalHeight; // 初始图片的尺寸
    private float mOriginalPivotX, mOriginalPivotY; // 图片中心

    private float tempSize;
    private int tempColor;

    public enum Pen {
        HAND, // 手绘
        COPY, // 仿制
        ERASER, // 橡皮擦
        TEXT, // 文本
        BITMAP, // 贴图
    }

    /**
     * 图形
     */
    public enum Shape {
        HAND_WRITE, //
        ARROW, // 箭头
        LINE, // 直线
        FILL_CIRCLE, // 实心圆
        HOLLOW_CIRCLE, // 空心圆
        FILL_RECT, // 实心矩形
        HOLLOW_RECT, // 空心矩形
    }



    public PaletteView(Context context) {
        this(context,null);
    }

    public PaletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        //initBuffer();
        mBitmap = BitmapFactory.decodeResource(this.getContext().getResources(),R.drawable.bg_gray);
        /*mBitmapEraser = ImageUtils.createBitmapFromPath(eraser, getContext());
        mEraserImageIsResizeable = eraserImageIsResizeable;*/
        mOriginalWidth = mBitmap.getWidth();
        mOriginalHeight = mBitmap.getHeight();
        mOriginalPivotX = mOriginalWidth / 2f;
        mOriginalPivotY = mOriginalHeight / 2f;
        init();
    }

    public interface Callback {
        void onUndoRedoStatusChanged();
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }

    private void init() {
        /*//this.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
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

        mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);*/

        mScale = 1f;
        mPaintSize = 10;
        tempSize = mPaintSize;
        mColor = new PenColor(Color.GRAY);
        mPaint = new Paint();
        mPaint.setStrokeWidth(mPaintSize);
        mPaint.setColor(mColor.getColor());
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 圆滑

        mPen = Pen.HAND;
        mShape = Shape.HAND_WRITE;

        this.mBitmapShader = new BitmapShader(this.mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        if (mBitmapEraser != null) {
            this.mBitmapShaderEraser = new BitmapShader(this.mBitmapEraser, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        } else {
            this.mBitmapShaderEraser = mBitmapShader;
        }

        mShaderMatrix = new Matrix();
        mMatrixTemp = new Matrix();
        mTempPath = new Path();
        /*mCopyLocation = new CopyLocation(150, 150);

        mAmplifierPaint = new Paint();
        mAmplifierPaint.setColor(0xaaffffff);
        mAmplifierPaint.setStyle(Paint.Style.STROKE);
        mAmplifierPaint.setAntiAlias(true);
        mAmplifierPaint.setStrokeJoin(Paint.Join.ROUND);
        mAmplifierPaint.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        mAmplifierPaint.setStrokeWidth(Util.dp2px(getContext(), 10));*/

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setBG();
    }

    /*private void initBuffer(){
        mBitmap =
        //mBitmap = Bitmap.createBitmap(1920  , 1080, Bitmap.Config.ARGB_8888);
        //mBitmaps[countNow] = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
        //mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        //mCanvas = new Canvas(mBitmaps[countNow]);
        //mBufferCanvas = new Canvas(mBufferBitmap);
    }*/

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


    private void loadBitmap(){
        mBitmapCanvas.setBitmap(mBitmaps[countNow]);
    }

    public void crateNewPage(){
        if(count < 9) {
            count++;
            countNow = count;
            mBitmaps[countNow] = mBitmap.copy(Bitmap.Config.RGB_565, true);
            //mBitmaps[countNow] = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
            //mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            //mBufferCanvas = new Canvas(mBufferBitmap);
            loadBitmap();
            invalidate();
        }
    }

    public void prevPage(){
        if(countNow > 0){
            countNow --;
            if(mBitmaps[countNow] != null){
                loadBitmap();
                invalidate();
            }
        }
    }

    public void nextPage(){
        if(countNow < 9){
            countNow ++;
            if(mBitmaps[countNow] != null){
                loadBitmap();
                invalidate();
            }else{
                countNow --;
            }
        }
    }

    public PenColor getColor() {
        return mColor;
    }


    public void setPenAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    /*private void reDraw(){
        if (mDrawingList != null) {
            mBitmaps[countNow].eraseColor(Color.TRANSPARENT);
            for (DrawingInfo drawingInfo : mDrawingList) {
                drawingInfo.draw(mCanvas);
            }
            invalidate();
        }
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
    }*/

    public Bitmap buildBitmap() {
        Bitmap bm = getDrawingCache();
        Bitmap result = Bitmap.createBitmap(bm);
        destroyDrawingCache();
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap.isRecycled() || mBitmaps[countNow].isRecycled()) {
            return;
        }
        canvas.save();
        doDraw(canvas);
        canvas.restore();
    }

    private void doDraw(Canvas canvas) {
        float left = (mCentreTranX + mTransX) / (mPrivateScale * mScale);
        float top = (mCentreTranY + mTransY) / (mPrivateScale * mScale);
        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
        canvas.scale(mPrivateScale * mScale, mPrivateScale * mScale); // 缩放画布
        canvas.translate(left, top); // 偏移画布

        if (!mIsDrawableOutside) { // 裁剪绘制区域为图片区域
            canvas.clipRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        }

        if (isJustDrawOriginal) { // 只绘制原图
            canvas.drawBitmap(mBitmap, 0, 0, null);
            return;
        }

        // 绘制涂鸦
        canvas.drawBitmap(mBitmaps[countNow], 0, 0, null);

        if (mIsPainting) {  //画在view的画布上
            Path path;
            float span = 0;
            // 为了仅点击时也能出现绘图，必须移动path
            if (mTouchDownX == mTouchX && mTouchDownY == mTouchY && mTouchDownX == mLastTouchX && mTouchDownY == mLastTouchY) {
                mTempPath.reset();
                mTempPath.addPath(mCurrPath);
                mTempPath.quadTo(
                        toX(mLastTouchX),
                        toY(mLastTouchY),
                        toX((mTouchX + mLastTouchX + VALUE) / 2),
                        toY((mTouchY + mLastTouchY + VALUE) / 2));
                path = mTempPath;
                span = VALUE;
            } else {
                path = mCurrPath;
                span = 0;
            }
            // 画触摸的路径
            mPaint.setStrokeWidth(mPaintSize);
            if (mShape == Shape.HAND_WRITE) { // 手写
                draw(canvas, mPen, mPaint, path, mShaderMatrix, mColor);
            } else {  // 画图形
                draw(canvas, mPen, mShape, mPaint,
                        toX(mTouchDownX), toY(mTouchDownY), toX(mTouchX + span), toY(mTouchY + span), mShaderMatrix, mColor);
            }
        }
    }

    private void draw(Canvas canvas, Pen pen, Paint paint, Path path, Matrix matrix, PenColor color) {
        resetPaint(pen, paint, matrix, color);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

    }

    private void draw(Canvas canvas, Pen pen, Shape shape, Paint paint, float sx, float sy, float dx, float dy, Matrix matrix, PenColor color) {
        resetPaint(pen, paint, matrix, color);

        paint.setStyle(Paint.Style.STROKE);

        switch (shape) { // 绘制图形
            case ARROW:
                paint.setStyle(Paint.Style.FILL);
                DrawUtil.drawArrow(canvas, sx, sy, dx, dy, paint);
                break;
            case LINE:
                DrawUtil.drawLine(canvas, sx, sy, dx, dy, paint);
                break;
            case FILL_CIRCLE:
                paint.setStyle(Paint.Style.FILL);
            case HOLLOW_CIRCLE:
                DrawUtil.drawCircle(canvas, sx, sy,
                        (float) Math.sqrt((sx - dx) * (sx - dx) + (sy - dy) * (sy - dy)), paint);
                break;
            case FILL_RECT:
                paint.setStyle(Paint.Style.FILL);
            case HOLLOW_RECT:
                DrawUtil.drawRect(canvas, sx, sy, dx, dy, paint);
                break;
            default:
                throw new RuntimeException("unknown shape:" + shape);
        }
    }


    private void draw(Canvas canvas, CopyOnWriteArrayList<GraffitiPath> pathStack) {
        // 还原堆栈中的记录的操作
        for (GraffitiPath path : pathStack) {
            draw(canvas, path);
        }
    }

    private void draw(Canvas canvas, GraffitiPath path) {
        mPaint.setStrokeWidth(path.mStrokeWidth);
        if (path.mShape == Shape.HAND_WRITE) { // 手写
            draw(canvas, path.mPen, mPaint, path.mPath, path.mMatrix, path.mColor);
        } else { // 画图形
            draw(canvas, path.mPen, path.mShape, mPaint,
                    path.mSx, path.mSy, path.mDx, path.mDy, path.mMatrix, path.mColor);
        }
    }

    private void resetPaint(Pen pen, Paint paint, Matrix matrix, PenColor color) {
        switch (pen) { // 设置画笔
            case HAND:
                paint.setShader(null);

                color.initColor(paint, null);
                break;
            case ERASER:
                if (mBitmapShader == mBitmapShaderEraser) { // 图片的矩阵不需要任何偏移
                    mBitmapShaderEraser.setLocalMatrix(null);
                }
                paint.setShader(this.mBitmapShaderEraser);
                break;
        }
    }

    private void resetMatrix() {
        this.mShaderMatrix.set(null);
        this.mBitmapShader.setLocalMatrix(this.mShaderMatrix);

        // 如果使用了自定义的橡皮擦底图，则需要调整矩阵
        if (mPen == Pen.ERASER && mBitmapShader != mBitmapShaderEraser) {
            mMatrixTemp.reset();
            mBitmapShaderEraser.getLocalMatrix(mMatrixTemp);
            mBitmapShader.getLocalMatrix(mMatrixTemp);
            // 缩放橡皮擦底图，使之与涂鸦图片大小一样
            if (mEraserImageIsResizeable) {
                mMatrixTemp.preScale(mBitmap.getWidth() * 1f / mBitmapEraser.getWidth(), mBitmap.getHeight() * 1f / mBitmapEraser.getHeight());
            }
            mBitmapShaderEraser.setLocalMatrix(mMatrixTemp);
        }
    }

    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    public final float toX(float touchX) {
        return (touchX - mCentreTranX - mTransX) / (mPrivateScale * mScale);
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    public final float toY(float touchY) {
        return (touchY - mCentreTranY - mTransY) / (mPrivateScale * mScale);
    }

    /**
     * 坐标换算
     * （公式由toX()中的公式推算出）
     *
     * @param touchX    触摸坐标
     * @param graffitiX 在涂鸦图片中的坐标
     * @return 偏移量
     */
    public final float toTransX(float touchX, float graffitiX) {
        return -graffitiX * (mPrivateScale * mScale) + touchX - mCentreTranX;
    }

    public final float toTransY(float touchY, float graffitiY) {
        return -graffitiY * (mPrivateScale * mScale) + touchY - mCentreTranY;
    }

    /**
     * 调整图片位置
     *
     * 明白下面一点很重要：
     * 假设不考虑任何缩放，图片就是肉眼看到的那么大，此时图片的大小width =  mPrivateWidth * mScale ,
     * 偏移量x = mCentreTranX + mTransX，而view的大小为width = getWidth()。height和偏移量y以此类推。
     */
    private void judgePosition() {
        boolean changed = false;
        if (mPrivateWidth * mScale < getWidth()) { // 限制在view范围内
            if (mTransX + mCentreTranX < 0) {
                mTransX = -mCentreTranX;
                changed = true;
            } else if (mTransX + mCentreTranX + mPrivateWidth * mScale > getWidth()) {
                mTransX = getWidth() - mCentreTranX - mPrivateWidth * mScale;
                changed = true;
            }
        } else { // 限制在view范围外
            if (mTransX + mCentreTranX > 0) {
                mTransX = -mCentreTranX;
                changed = true;
            } else if (mTransX + mCentreTranX + mPrivateWidth * mScale < getWidth()) {
                mTransX = getWidth() - mCentreTranX - mPrivateWidth * mScale;
                changed = true;
            }
        }
        if (mPrivateHeight * mScale < getHeight()) { // 限制在view范围内
            if (mTransY + mCentreTranY < 0) {
                mTransY = -mCentreTranY;
                changed = true;
            } else if (mTransY + mCentreTranY + mPrivateHeight * mScale > getHeight()) {
                mTransY = getHeight() - mCentreTranY - mPrivateHeight * mScale;
                changed = true;
            }
        } else { // 限制在view范围外
            if (mTransY + mCentreTranY > 0) {
                mTransY = -mCentreTranY;
                changed = true;
            } else if (mTransY + mCentreTranY + mPrivateHeight * mScale < getHeight()) {
                mTransY = getHeight() - mCentreTranY - mPrivateHeight * mScale;
                changed = true;
            }
        }
        if (changed) {
            resetMatrix();
        }
    }

    public void changeBG(Bitmap bitmap){
        if (mBitmap == null) {
            return;
        }else{
            mBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
            setBG();
            initCanvas();
            this.mBitmapShader = new BitmapShader(this.mBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

            if (mBitmapEraser != null) {
                this.mBitmapShaderEraser = new BitmapShader(this.mBitmapEraser, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            } else {
                this.mBitmapShaderEraser = mBitmapShader;
            }
            draw(mBitmapCanvas,mPathStack);
            resetMatrix();
            invalidate();
        }
    }

    public void insertImage(Bitmap bitmap){
        mBitmapCanvas.drawBitmap(bitmap,576,324,null);
        invalidate();
    }

    private void initCanvas() {
        if (mBitmaps[countNow] != null) {
            mBitmaps[countNow].recycle();
        }
        mBitmaps[countNow] = mBitmap.copy(Bitmap.Config.RGB_565, true);
        mBitmapCanvas = new Canvas(mBitmaps[countNow]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final float x = event.getX();
        final float y = event.getY();*/
        float touchSize;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //Log.e("lilith", "mTouchModeTemp="+mTouchModeTemp );
                touchSize = event.getSize();
                //电视触摸面积：0.003，手机触摸面积：0.02
                if(mTouchModeTemp == 1){
                    if(touchSize > 0.02){
                        tempSize = mPaintSize;
                        tempColor = mColor.getColor();
                        setPaintSize(80);
                        //mColor.setColor(0xFFFFFFFF);
                        mPaint.setColor(0xFFFFFFFF);
                        setPen(Pen.ERASER);
                    }else{
                        Log.e("lilith", "paintSize=" + tempSize );
                        tempSize = mPaintSize;
                        tempColor = mColor.getColor();
                        //mColor.setColor(0x00FFFFFF);
//                        setPaintSize(tempSize);
//                        mPaint.setColor(tempColor);
                        Log.e("deng", "touchSize="+touchSize );
                        setPen(Pen.HAND);
                    }
                }
                mTouchMode = mTouchModeTemp = 1;
                mTouchDownX = mTouchX = mLastTouchX = event.getX();
                mTouchDownY = mTouchY = mLastTouchY = event.getY();
                mCurrPath = new Path();
                mCurrPath.moveTo(toX(mTouchDownX), toY(mTouchDownY));
                mIsPainting = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode < 2) { // 单点滑动
                    mLastTouchX = mTouchX;
                    mLastTouchY = mTouchY;
                    mTouchX = event.getX();
                    mTouchY = event.getY();
                    if (mShape == Shape.HAND_WRITE) { // 手写
                        mCurrPath.quadTo(
                                toX(mLastTouchX),
                                toY(mLastTouchY),
                                toX((mTouchX + mLastTouchX) / 2),
                                toY((mTouchY + mLastTouchY) / 2));
                    }
                } else { // 多点

                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTouchMode = 0;
                mLastTouchX = mTouchX;
                mLastTouchY = mTouchY;
                mTouchX = event.getX();
                mTouchY = event.getY();

                // 为了仅点击时也能出现绘图，必须移动path
                if (mTouchDownX == mTouchX && mTouchDownY == mTouchY & mTouchDownX == mLastTouchX && mTouchDownY == mLastTouchY) {
                    mTouchX += VALUE;
                    mTouchY += VALUE;
                }
                if(mTouchModeTemp > 3){
                    mIsPainting = false;
                }
                if (mIsPainting) {
                    GraffitiPath path = null;
                    // 把操作记录到加入的堆栈中
                    if (mShape == Shape.HAND_WRITE) { // 手写
                        mCurrPath.quadTo(
                                toX(mLastTouchX),
                                toY(mLastTouchY),
                                toX((mTouchX + mLastTouchX) / 2),
                                toY((mTouchY + mLastTouchY) / 2));
                        path = GraffitiPath.toPath(mPen, mShape, mPaintSize, mColor.copy(), mCurrPath, mPen == Pen.COPY ? new Matrix(mShaderMatrix) : null);
                    }
                    //addPath(path);
                    mPathStack.add(path);
                    draw(mBitmapCanvas, path); // 保存到图片中
                    mIsPainting = false;
                    mTouchModeTemp = 1;

                }
                setPaintSize(tempSize);
                mColor.setColor(tempColor);

                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                mTouchMode -= 1;

                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                mTouchMode += 1;
                mTouchModeTemp = mTouchMode;

                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    /*public final void addPath(GraffitiPath path) {
        mPathStack.add(path);
        mUndoStack.add(path);
        draw(mBitmapCanvas, path); // 保存到图片中
    }*/

    private void setBG() {// 不用resize preview
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        if (nw > nh) {
            mPrivateScale = 1 / nw;
            mPrivateWidth = getWidth();
            mPrivateHeight = (int) (h * mPrivateScale);
        } else {
            mPrivateScale = 1 / nh;
            mPrivateWidth = (int) (w * mPrivateScale);
            mPrivateHeight = getHeight();
        }
        // 使图片居中
        mCentreTranX = (getWidth() - mPrivateWidth) / 2f;
        mCentreTranY = (getHeight() - mPrivateHeight) / 2f;

        initCanvas();
        resetMatrix();
        invalidate();
    }

    public float getmTouchDownX(){
        return mTouchDownX;
    }

    public float getmTouchDownY(){
        return mTouchDownY;
    }

    public Bitmap getmGraffitiBitmap(){
        return mBitmaps[countNow];
    }

    public Bitmap getmBitmapEraser(){
        return mBitmapEraser;
    }

    public CopyOnWriteArrayList<GraffitiPath> getPathStack() {
        return mPathStack;
    }

    public CopyOnWriteArrayList<GraffitiSelectableItem> getSelectedItemStack() {
        return mSelectableStack;
    }

    public final void addPath(GraffitiPath path) {
        mPathStack.add(path);
        mUndoStack.add(path);
        draw(mBitmapCanvas, path); // 保存到图片中
    }

    public final void removePath(GraffitiPath path) {
        mPathStack.remove(path);
        mUndoStack.remove(path);
        initCanvas();
        draw(mBitmapCanvas, mPathStack);
        invalidate();
    }

    public final void addSelectableItem(GraffitiSelectableItem item) {
        mSelectableStack.add(item);
        //mUndoStack.add((Undoable) item);
    }

    public final void removeSelectableItem(GraffitiSelectableItem item) {
        mSelectableStack.remove(item);
        mUndoStack.remove(item);
    }

    public float getOriginalPivotX() {
        return mOriginalPivotX;
    }

    public float getOriginalPivotY() {
        return mOriginalPivotY;
    }


    // ===================== api ==============

    /**
     * 保存
     */
    public void save() {
//            initCanvas();
//            draw(mBitmapCanvas, mPathStackBackup, false);
//            draw(mBitmapCanvas, mPathStack, false);
        mGraffitiListener.onSaved(mBitmaps[countNow], mBitmapEraser);
    }



    /**
     * 清屏
     */
    public void clear() {
        mPathStack.clear();
//        mPathStackBackup.clear();
        initCanvas();
        invalidate();
    }

    /**
     * 撤销
     */
    public void undo() {
        if (mPathStack.size() > 0) {
            mPathStack.remove(mPathStack.size() - 1);
            initCanvas();
            draw(mBitmapCanvas, mPathStack);
            invalidate();
        }
    }

    /**
     * 是否有修改
     */
    public boolean isModified() {
        return mPathStack.size() != 0;
    }

    /**
     * 居中图片
     */
    public void centrePic() {
        mScale = 1;
        // 居中图片
        mTransX = 0;
        mTransY = 0;
        judgePosition();
        invalidate();
    }

    /**
     * 只绘制原图
     *
     * @param justDrawOriginal
     */
    public void setJustDrawOriginal(boolean justDrawOriginal) {
        isJustDrawOriginal = justDrawOriginal;
        invalidate();
    }

    public boolean isJustDrawOriginal() {
        return isJustDrawOriginal;
    }

    /**
     * 设置画笔底色
     *
     * @param color
     */
    public void setColor(int color,int alpha) {
        mColor.setColor(color-0xff000000+(alpha<<24));
        invalidate();
    }
    public void setTmpColor(int color,int alpha) {
        tempColor=color-0xff000000+(alpha<<24);
        invalidate();
    }

    public void setColor(Bitmap bitmap) {
        if (mBitmap == null) {
            return;
        }
        mColor.setColor(bitmap);
        invalidate();
    }

    public void setColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
        if (mBitmap == null) {
            return;
        }
        mColor.setColor(bitmap, tileX, tileY);
        invalidate();
    }

    public PenColor getGraffitiColor() {
        return mColor;
    }

    /**
     * 缩放倍数，图片真实的缩放倍数为 mPrivateScale*mScale
     *
     * @param scale
     */
    public void setScale(float scale) {
        this.mScale = scale;
        judgePosition();
        resetMatrix();
        invalidate();
    }

    public float getScale() {
        return mScale;
    }

    /**
     * 设置画笔
     *
     * @param pen
     */
    public void setPen(Pen pen) {
        if (pen == null) {
            throw new RuntimeException("Pen can't be null");
        }
        mPen = pen;
        resetMatrix();
        invalidate();
    }

    public Pen getPen() {
        return mPen;
    }

    /**
     * 设置画笔形状
     *
     * @param shape
     */
    public void setShape(Shape shape) {
        if (shape == null) {
            throw new RuntimeException("Shape can't be null");
        }
        mShape = shape;
        invalidate();
    }

    public Shape getShape() {
        return mShape;
    }

    public void setTrans(float transX, float transY) {
        mTransX = transX;
        mTransY = transY;
        judgePosition();
        resetMatrix();
        invalidate();
    }

    /**
     * 设置图片偏移
     *
     * @param transX
     */
    public void setTransX(float transX) {
        this.mTransX = transX;
        judgePosition();
        invalidate();
    }

    public float getTransX() {
        return mTransX;
    }

    public void setTransY(float transY) {
        this.mTransY = transY;
        judgePosition();
        invalidate();
    }

    public float getTransY() {
        return mTransY;
    }


    public void setPaintSize(float paintSize) {
        mPaintSize = paintSize;
        invalidate();
    }
    public void setTmpPaintSize(float paintSize) {
        tempSize = paintSize;
        invalidate();
    }


    public float getPaintSize() {
        return mPaintSize;
    }

    /**
     * 触摸时，图片区域外是否绘制涂鸦轨迹
     *
     * @param isDrawableOutside
     */
    public void setIsDrawableOutside(boolean isDrawableOutside) {
        mIsDrawableOutside = isDrawableOutside;
    }

    /**
     * 触摸时，图片区域外是否绘制涂鸦轨迹
     */
    public boolean getIsDrawableOutside() {
        return mIsDrawableOutside;
    }

    public interface GraffitiListener {

        /**
         * 保存图片
         *
         * @param bitmap       涂鸦后的图片
         * @param bitmapEraser 橡皮擦底图
         */
        void onSaved(Bitmap bitmap, Bitmap bitmapEraser);

        /**
         * 出错
         *
         * @param i
         * @param msg
         */
        void onError(int i, String msg);

        /**
         * 准备工作已经完成
         */
        void onReady();
    }

    private static class GraffitiPath implements Undoable {
        Pen mPen; // 画笔类型
        Shape mShape; // 画笔形状
        float mStrokeWidth; // 大小
        PenColor mColor; // 颜色
        Path mPath; // 画笔的路径
        float mSx, mSy; // 映射后的起始坐标，（手指点击）
        float mDx, mDy; // 映射后的终止坐标，（手指抬起）
        Matrix mMatrix; //　仿制图片的偏移矩阵

        static GraffitiPath toShape(Pen pen, Shape shape, float width, PenColor color,
                                    float sx, float sy, float dx, float dy, Matrix matrix) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mShape = shape;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mSx = sx;
            path.mSy = sy;
            path.mDx = dx;
            path.mDy = dy;
            path.mMatrix = matrix;
            return path;
        }

        static GraffitiPath toPath(Pen pen, Shape shape, float width, PenColor color, Path p, Matrix matrix) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mShape = shape;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mPath = p;
            path.mMatrix = matrix;
            return path;
        }
    }
}
