package com.SiWei.PaintingApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.forward.androids.utils.ImageUtils;

public class PaletteView extends View {

    //private Bitmap mBufferBitmap;
    //private Canvas mBufferCanvas;
    //private ArrayList<Bitmap> cacheBitmapsList = new ArrayList<>();
    //private int cacheIndex = 0;
    //private static final int MAX_CACHE_STEP = 100;
    //private Canvas mCanvas;

    //页面相关
    private Bitmap mBitmap;
    private final int mMaxPage = 5;
    private int mPageRealCount = 1;
    private int mPageRealIndex = 1;
    private Bitmap[] mBitmaps = new Bitmap[mMaxPage];
    private Bitmap[] mBitmapsPreview;
    private int mPageIndexNow = 0;
    //private ArrayList<Bitmap> mPageBitmaps = new ArrayList();
    //private int count = 0;

    private Callback mCallback;
    private final RectF dirtyRect = new RectF();
    public static final int ERROR_INIT = -1;
    public static final int ERROR_SAVE = -2;
    private static final float VALUE = 1f;
    private final int TIME_SPAN = 80;
    private GraffitiListener mGraffitiListener;

    //private Bitmap mBitmap; // 原图
    //private Bitmap mBitmapEraser; // 橡皮擦底图
    //private Bitmap mGraffitiBitmap; // 用绘制涂鸦的图片
    private Canvas mBitmapCanvas;
    private int mWidth = 1920;
    private int mHeight = 1080;
    //移动缩放相关
    private float mPrivateScale; // 图片适应屏幕（mScale=1）时的缩放倍数
    private int mPrivateHeight, mPrivateWidth;// 图片在缩放mPrivateScale倍数的情况下，适应屏幕（mScale=1）时的大小（肉眼看到的在屏幕上的大小）
    private float mCentreTranX, mCentreTranY;// 图片在缩放mPrivateScale倍数的情况下，居中（mScale=1）时的偏移（肉眼看到的在屏幕上的偏移）
    private float mScale; // 图片在相对于居中时的缩放倍数 （ 图片真实的缩放倍数为 mPrivateScale*mScale ）
    private float mTransX = 0, mTransY = 0; // 图片在相对于居中时且在缩放mScale倍数的情况下的偏移量 （ 图片真实偏移量为　(mCentreTranX + mTransX)/mPrivateScale*mScale ）
    private int mOriginalWidth, mOriginalHeight; // 初始图片的尺寸
    private float mOriginalPivotX, mOriginalPivotY; // 图片中心
    private boolean mIsDrawableOutside = false; // 触摸时，图片区域外是否绘制涂鸦轨迹

    //private BitmapShader mBitmapShader; // 用于涂鸦的图片上!
    //private BitmapShader mBitmapShaderEraser; // 橡皮擦底图
    //private Path mCurrPath; // 当前手写的路径
    //private boolean isPathOccupied;
    //private Path mCurrPath1;
    //private boolean isPathOccupied1;
    //private Path mTempPath;

    private Paint mPaint;
    private Paint mPaintDraw;
    private Paint mPaintDrawTemp;
    private Paint mPaintEraser;
    private Paint mPaintEraserTemp;
    private float mPaintDrawSize;
    private float mPaintEraserSize;
    private Xfermode mClearMode;
    private int mTouchMode; // 触摸模式，用于判断单点或多点触摸
    //private PenColor mColor; // 画笔底色
    private int mPaintColor;
    private Bitmap mEraserBitmap;

    //private boolean mIsPainting = false; // 是否正在绘制
    private boolean isJustDrawOriginal; // 是否只绘制原图
    private boolean mEraserImageIsResizeable;
    private boolean mReady = false;
    private boolean mIsEraserSelected = false;
    // view 的宽高
    private int mTotalWidth, mTotalHeight;
    // 保存涂鸦操作，便于撤销
    //private CopyOnWriteArrayList<GraffitiPath> mPathStack = new CopyOnWriteArrayList<GraffitiPath>();
    //    private CopyOnWriteArrayList<GraffitiPath> mPathStackBackup = new CopyOnWriteArrayList<GraffitiPath>();
    // 保存涂鸦操作，便于撤销
    private CopyOnWriteArrayList<Undoable> mUndoStack = new CopyOnWriteArrayList<Undoable>();
    private CopyOnWriteArrayList<GraffitiPath> mPathStack = new CopyOnWriteArrayList<GraffitiPath>();
    private CopyOnWriteArrayList<GraffitiSelectableItem> mSelectableStack = new CopyOnWriteArrayList<>();
    //private CopyOnWriteArrayList<Path> mAreaPaths = new CopyOnWriteArrayList<>();
    private Pen mPen;
    //private float mTouchDownX, mTouchDownY, mLastTouchX, mLastTouchY, mTouchX, mTouchY;
    //private float mTouchDownX1, mTouchDownY1, mLastTouchX1, mLastTouchY1, mTouchX1, mTouchY1;

    //双指书写相关
    Path[] mCurrPaths = new Path[2];
    //Path[] mTempPaths = new Path[2];
    Path[] mAreaPaths = new Path[2];
    int[] mPointerIDs = {-1, -1};
    float mTouchSize;
    int downID, moveID, upID;
    float[] mTouchDownXs = {0, 0};
    float[] mTouchDownYs = {0, 0};
    float[] mTouchXs = {0, 0};
    float[] mTouchYs = {0, 0};
    float[] mLastTouchXs = {0, 0};
    float[] mLastTouchYs = {0, 0};

    //套索相关
    private Paint mPaintLasso;
    // private Paint mPaintLassoEraser;
    //private Path mAreaPath;
    //private Path mAreaPath1;
    private ArrayList<Path> mAreaPathsList = new ArrayList<>();
    private float maxX, maxY, minX, minY;
    private float mDX, mDY;
    private boolean mIsLassoMove = false;
    private RectF mLassoArea = new RectF();
    private Path mLassoAreaPath = new Path();
    private ArrayList<Integer> mSelectedPathsIndex = new ArrayList<>();
    private ArrayList<Path> mSelectedPaths = new ArrayList<>();
    private ArrayList<Path> mSelectedAreaPaths = new ArrayList<>();
    private ArrayList<Bitmap> mInsertBitmaps = new ArrayList<>();
    private int mPhase = 0;
    private DashPathEffect mEffect = new DashPathEffect(new float[] {10,10}, mPhase);
    private boolean mIsLassoBitmap = false;
    //private Bitmap mLassoBitmap;
    //private int mLassoBitmapIndex = -1;
    private float mLassoBitmapX,mLassoBitmapY;
    private Matrix tempMatrix = new Matrix();
    float mPathRotation;
    private float mPathScale;
    private float mOldScale = 1f;
    //private float mLassoBitmapMidX,mLassoBitmapMidY;
    private final float mMaxScale = 2f;
    private final float mMinScale = 0.3f;
    private float pathDist = 0;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    //float oldRotation = 0;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    //撤销，重做相关
    private float mDXS, mDYS;
    private ArrayList<ChangeRecord> mChangeRecordList = new ArrayList<>();
    private int mCRIndex, mPSIndex;
    private final int mMaxStep = 26;
    //lasso 按钮
    private View lassoBtn;

    public enum Pen {
        HAND, // 手绘
        ERASER, // 橡皮擦
        LASSO, //套索
        BITMAP, // 贴图
    }

    public PaletteView(Context context) {
        this(context, null);
    }

    public PaletteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
        //initBuffer();
        //mBitmap = BitmapFactory.decodeResource(this.getContext().getResources(),R.drawable.bg_gray);
        /*mBitmapEraser = ImageUtils.createBitmapFromPath(eraser, getContext());
        mEraserImageIsResizeable = eraserImageIsResizeable;*/
        //mOriginalWidth = mBitmap.getWidth();
        //mOriginalHeight = mBitmap.getHeight();
        mOriginalPivotX = mOriginalWidth / 2f;
        mOriginalPivotY = mOriginalHeight / 2f;
        init();
    }

    public interface Callback {
        void onUndoRedoStatusChanged();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void init() {
        mScale = 1f;
        mPaintDrawSize = 4.5f;
        mPaintEraserSize = 100;
        mPaintColor = 0xffbbbbbb;
        mPaint = new Paint();
        //初始化画笔
        mPaintDraw = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaintDraw.setStyle(Paint.Style.STROKE);
        mPaintDraw.setFilterBitmap(true);
        mPaintDraw.setStrokeWidth(mPaintDrawSize);
        mPaintDraw.setColor(mPaintColor);
        mPaintDraw.setAntiAlias(true);
        mPaintDraw.setDither(true);
        mPaintDraw.setStrokeJoin(Paint.Join.ROUND);
        mPaintDraw.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        mPaintDraw.setPathEffect(new CornerPathEffect(30));
        //初始化临时画笔
        mPaintDrawTemp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaintDrawTemp.setStyle(Paint.Style.STROKE);
        mPaintDrawTemp.setFilterBitmap(true);
        mPaintDrawTemp.setStrokeWidth(mPaintDrawSize);
        mPaintDrawTemp.setColor(mPaintColor);
        mPaintDrawTemp.setAntiAlias(true);
        mPaintDrawTemp.setDither(true);
        mPaintDrawTemp.setStrokeJoin(Paint.Join.ROUND);
        mPaintDrawTemp.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        //初始化橡皮擦
        mPaintEraser = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaintEraser.setStyle(Paint.Style.STROKE);
        mPaintEraser.setFilterBitmap(true);
        mPaintEraser.setStrokeWidth(mPaintEraserSize);
        mPaintEraser.setColor(0XFFcccccc);
        mPaintEraser.setAlpha(0);
        mPaintEraser.setAntiAlias(true);
        mPaintEraser.setDither(true);
        mPaintEraser.setStrokeJoin(Paint.Join.ROUND);
        mPaintEraser.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        //mPaintEraser.setStrokeCap(Paint.Cap.SQUARE);
        mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        //mClearMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        //mClearMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        mPaintEraser.setXfermode(mClearMode);
        //初始化临时橡皮擦
        mPaintEraserTemp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaintEraserTemp.setStyle(Paint.Style.STROKE);
        mPaintEraserTemp.setFilterBitmap(true);
        mPaintEraserTemp.setStrokeWidth(mPaintEraserSize);
        mPaintEraserTemp.setColor(0XFFcccccc);
        mPaintEraserTemp.setAlpha(0);
        mPaintEraserTemp.setAntiAlias(true);
        mPaintEraserTemp.setDither(true);
        mPaintEraserTemp.setStrokeJoin(Paint.Join.ROUND);
        mPaintEraserTemp.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        //mPaintEraserTemp.setStrokeCap(Paint.Cap.SQUARE);
        mPaintEraserTemp.setXfermode(mClearMode);
        //初始化套索
        mPaintLasso = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaintLasso.setStyle(Paint.Style.STROKE);
        mPaintLasso.setFilterBitmap(true);
        mPaintLasso.setStrokeWidth(5);
        mPaintLasso.setColor(Color.YELLOW);
        mPaintLasso.setAlpha(90);
        //mPaintLasso.setAntiAlias(true);
        //mPaintLasso.setDither(true);
        mPaintLasso.setStrokeJoin(Paint.Join.ROUND);
        mPaintLasso.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        //mPaintLasso.setPathEffect(new DashPathEffect(new float[]{20, 10}, 10));

        mPen = Pen.HAND;
        mPaint = mPaintDraw;

        lassoBtn = null;
        //mTempPath = new Path();
        //mAreaPath = new Path();
        //初始化画布Bitmap
        mBitmaps[mPageIndexNow] = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmaps[mPageIndexNow]);
        //Bitmap tempBitmap = Bitmap.createBitmap(mBitmaps[mPageIndexNow]);
        //mLassoBitmapMidX = mLassoBitmapMidY = 0;
        initCache();
        //初始化橡皮擦图片
        //mEraserBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.eraserimg100);
        //mEraserBitmap.recycle();
        //cacheBitmapsList.add(tempBitmap);
        setBG();
    }

    private void initCache() {
        mCRIndex = mPSIndex = 0;
        Path path = new Path();
        GraffitiPath graffitiPath = GraffitiPath.toPath(Pen.HAND, 10, Color.GRAY, path, 0,false);
        mPathStack.add(graffitiPath);
        mAreaPathsList.add(path);
        ChangeRecord cr = new ChangeRecord(false, 0);
        mChangeRecordList.add(cr);
        //Log.e("Lilith", "mCRIndex=" + mCRIndex);
        //Log.e("Lilith", "mPSIndex=" + mPSIndex);
    }

    private void setEraserBitmap(float num){
        float scale;
        mEraserBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.eraserimg100);
        scale = mTouchSize * num * 7;
        //Log.e("Lilith", "scale="+scale);
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale);
        //Bitmap.createBitmap(mEraserBitmap,0,0,1,1,matrix,true);
        mEraserBitmap = Bitmap.createBitmap(mEraserBitmap,0,0,mEraserBitmap.getWidth(),mEraserBitmap.getHeight(),matrix,true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;
        //Log.e("lilith", "View 宽度：" + mTotalWidth );
        //Log.e("lilith", "View 高度：" + mTotalHeight );
        setBG();
    }

    private void saveMoveChanged() {
        mCRIndex++;
        ArrayList<Integer> indexList = (ArrayList) mSelectedPathsIndex.clone();
        ChangeRecord cr = new ChangeRecord(indexList, true, mDXS, mDYS);
        if (mCRIndex > mMaxStep - 1) {
            mChangeRecordList.remove(0);
            mCRIndex--;
        } else if (mCRIndex < mChangeRecordList.size()) {
            for (int i = mChangeRecordList.size() - 1; i >= mCRIndex; i--) {
                mChangeRecordList.remove(i);
            }
        }
        mChangeRecordList.add(cr);
        //Log.e("Lilith", "mCRIndex=" + mCRIndex);
        //Log.e("Lilith", "mPSIndex=" + mPSIndex);
    }

    private void saveAddChanged() {
        mCRIndex++;
        ChangeRecord cr = new ChangeRecord(false, mPathStack.size() - 1);
        if (mCRIndex > mMaxStep - 1) {
            mChangeRecordList.remove(0);
            mCRIndex--;
        } else if (mCRIndex < mChangeRecordList.size()) {
            for (int i = mChangeRecordList.size() - 1; i >= mCRIndex; i--) {
                mChangeRecordList.remove(i);
            }
        }
        mChangeRecordList.add(cr);
        //Log.e("Lilith", "mCRIndex=" + mCRIndex);
        //Log.e("Lilith", "mPSIndex=" + mPSIndex);
        //Log.e("Lilith", "index=" + mChangeRecordList.get(mCRIndex).psIndex);
    }

    public void unDo() {
        //int index,dx,dy;
        if (mCRIndex >= 1 && mChangeRecordList.get(mCRIndex) != null) {
            initCanvas();
            if (mChangeRecordList.get(mCRIndex).isMove) {
                for (int i = 0; i < mChangeRecordList.get(mCRIndex).mIndexList.size(); i++) {
                    mPathStack.get(mChangeRecordList.get(mCRIndex).mIndexList.get(i)).mPath.offset(mChangeRecordList.get(mCRIndex).dX, mChangeRecordList.get(mCRIndex).dY);
                }
                draw(mBitmapCanvas, mPathStack, mPSIndex);
            } else {
                mPSIndex--;
                //Log.e("Lilith", "index=" + mChangeRecordList.get(mCRIndex).psIndex);
                draw(mBitmapCanvas, mPathStack, mPSIndex);
            }
            invalidate();
            mCRIndex--;
        } else {
            //Log.e("Lilith", "后已经到头了");
        }
        //Log.e("Lilith", "mCRIndex=" + mCRIndex);
        //Log.e("Lilith", "mPSIndex=" + mPSIndex);
    }

    public void reDo() {
        mCRIndex++;
        if (mCRIndex < mChangeRecordList.size() && mChangeRecordList.get(mCRIndex) != null) {
            initCanvas();
            if (mChangeRecordList.get(mCRIndex).isMove) {
                for (int i = 0; i < mChangeRecordList.get(mCRIndex).mIndexList.size(); i++) {
                    mPathStack.get(mChangeRecordList.get(mCRIndex).mIndexList.get(i)).mPath.offset(-mChangeRecordList.get(mCRIndex).dX, -mChangeRecordList.get(mCRIndex).dY);
                }
                draw(mBitmapCanvas, mPathStack, mPSIndex);
            } else {
                mPSIndex++;
                draw(mBitmapCanvas, mPathStack, mPSIndex);
            }
            invalidate();
        } else {
            mCRIndex--;
            //Log.e("Lilith", "后已经到头了");
        }
        //Log.e("Lilith", "mCRIndex=" + mCRIndex);
        //Log.e("Lilith", "mPSIndex=" + mPSIndex);
    }

    private void clearCacheByIndex(int index){
        //mChangeRecordList = new ArrayList<>();
        for(int i=mPathStack.size()-1;i>=0;i--){
            if(mPathStack.get(i).mPageIndex == index){
                mPathStack.remove(i);
            }
        }
        //mCRIndex = 0;
        mPSIndex = mPathStack.size() - 1;
    }

    private abstract static class DrawingInfo {
        Paint paint;

        abstract void draw(Canvas canvas);
    }

    private static class PathDrawingInfo extends DrawingInfo {

        Path path;

        @Override
        void draw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }
    }

    private void initCanvas() {
        if (mBitmaps[mPageIndexNow] != null) {
            mBitmaps[mPageIndexNow].recycle();
        }
        mBitmaps[mPageIndexNow] = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        //mBitmaps[mPageIndexNow] = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmaps[mPageIndexNow]);
    }

    public void crateNewPage() {
        boolean changed = false;
        for(int i=0;i<mMaxPage;i++){
            if(i != mPageIndexNow){
                if(mBitmaps[i] == null || mBitmaps[i].isRecycled()){
                    mPageIndexNow = i;
                    Log.e("Lilith", "crateNewPage: index" + mPageIndexNow );
                    changed = true;
                    break;
                }
            }
        }
        if(changed){
            mBitmaps[mPageIndexNow] = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mBitmapCanvas.setBitmap(mBitmaps[mPageIndexNow]);
            mPageRealCount ++;
            calculatePageNumber();
            invalidate();
        }
    }

    public void removePage() {
        boolean changed = false;
        int tempIndex = mPageIndexNow;
        for(int i=0;i<mMaxPage;i++){
            if(i != mPageIndexNow && mBitmaps[i] != null && !mBitmaps[i].isRecycled()){
                mPageIndexNow = i;
                changed = true;
                break;
            }
        }
        if(changed){
            mBitmapCanvas.setBitmap(mBitmaps[mPageIndexNow]);
            mBitmaps[tempIndex].recycle();
            clearCacheByIndex(tempIndex);
            mPageRealCount --;
            calculatePageNumber();
            invalidate();
        }
    }

    public void prevPage() {
        //int tempCount = mPageIndexNow;
        boolean changed = false;
        for(int i=mPageIndexNow-1;i>=0;i--){
            if(mBitmaps[i] != null && !mBitmaps[i].isRecycled()){
                mPageIndexNow = i;
                changed = true;
                break;
            }
        }
        if(changed){
            Log.e("Lilith", "mPageIndexNow=" + mPageIndexNow);
            mBitmapCanvas.setBitmap(mBitmaps[mPageIndexNow]);
            calculatePageNumber();
            invalidate();
        }
    }

    public void nextPage() {
        //int tempCount = mPageIndexNow;
        boolean changed = false;
        for(int i=mPageIndexNow + 1;i<mMaxPage;i++){
            if(mBitmaps[i] != null && !mBitmaps[i].isRecycled()){
                mPageIndexNow = i;
                changed = true;
                break;
            }
        }
        if(changed){
            mBitmapCanvas.setBitmap(mBitmaps[mPageIndexNow]);
            calculatePageNumber();
            invalidate();
        }
    }

    private void calculatePageNumber(){
        int pageIndex = 1;
        for(int i=0;i<mPageIndexNow;i++){
            if(mBitmaps[i] != null && !mBitmaps[i].isRecycled()){
                pageIndex++;
            }
        }
        mPageRealIndex = pageIndex;
    }

    public int getPageIndex(){
        return mPageRealIndex;
    }

    public int getPageCount(){
        return mPageRealCount;
    }

    public void setPageIndexNow(int pageIndex){
        mPageIndexNow = pageIndex;
        mBitmapCanvas.setBitmap(mBitmaps[mPageIndexNow]);
        calculatePageNumber();
        invalidate();
    }

    public Bitmap[] getPagePreview(){
        Bitmap[] pagePreview = new Bitmap[mMaxPage];
        for(int i=0;i<mMaxPage;i++){
            if(mBitmaps[i] != null && !mBitmaps[i].isRecycled()){
                pagePreview[i] = mBitmaps[i].copy(Bitmap.Config.ARGB_8888,true);
            }
        }
        return pagePreview;
    }

    public Bitmap buildBitmap() {
        Bitmap bm = getDrawingCache();
        Bitmap result = Bitmap.createBitmap(bm);
        destroyDrawingCache();
        return result;
    }

    public void setPaint(Pen pen) {
        switch (pen) { // 设置画笔
            case HAND:
                mPaint = mPaintDraw;
                break;
            case ERASER:
                mPaint = mPaintEraser;
                break;
            case LASSO:
                mPaint = mPaintLasso;
        }
    }

    public void setPaintColor(int color) {
        //mPaintColor = color-0xff000000+(alpha<<24);
        //Log.i("deng", "setPaintColor_Opa: " + Integer.toBinaryString(mPaintColor & 0xff000000));
        //Log.i("deng", "setPaintColor_Color: " + Integer.toBinaryString(color & 0x00ffffff));
        mPaintColor = mPaintColor & 0xff000000 | color & 0x00ffffff;
        mPaintDraw.setColor(mPaintColor);
        //Log.i("deng", "setPaintColor: " + Integer.toBinaryString(mPaintColor));
    }

    public void setPaintColor(int color,boolean isIncludeAlpha){
        mPaintDraw.setColor(color);
    }

    public void setPaintOpacity(int alpha) {
        mPaintColor = mPaintColor & 0x00ffffff | (alpha << 24);
        mPaintDraw.setColor(mPaintColor);

    }

    //    public void setPaintColor(int color){
    //        mPaintColor = color;
    //        mPaintDraw.setColor(color);
    //    }
    public void setPaintSize(float size) {
        mPaintDraw.setStrokeWidth(size);
    }

    private void resetMatrix() {
  /*      this.mShaderMatrix.set(null);
        this.mBitmapShader.setLocalMatrix(this.mShaderMatrix);

        // 如果使用了自定义的橡皮擦底图，则需要调整矩阵
        if (mPen == Pen.ERASER && mBitmapShader != mBitmapShaderEraser) {
            mMatrixTemp.reset();
            mBitmapShaderEraser.getLocalMatrix(mMatrixTemp);
            mBitmapShader.getLocalMatrix(mMatrixTemp);
            // 缩放橡皮擦底图，使之与涂鸦图片大小一样
            if (mEraserImageIsResizeable) {
                //mMatrixTemp.preScale(mBitmap.getWidth() * 1f / mBitmapEraser.getWidth(), mBitmap.getHeight() * 1f / mBitmapEraser.getHeight());
            }
            mBitmapShaderEraser.setLocalMatrix(mMatrixTemp);
        }*/
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
     * <p>
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

    public void insertImage(Bitmap bitmap,String imgpath) {
        GraffitiPath graffitiPath = null;
        graffitiPath = GraffitiPath.toBitmap(true,imgpath, bitmap.getWidth(),bitmap.getHeight(), 576,324,1f,0f,mPageIndexNow);
        Path areaPath = new Path();
        //mInsertBitmaps.add(bitmap);
        RectF areaRectF = new RectF(576, 324, 576 + bitmap.getWidth(), 324 + bitmap.getHeight());
        //Log.e("Lilith", "insertImage: " + bitmap.getWidth() + " x " + bitmap.getHeight());
        areaPath.addRect(areaRectF, Path.Direction.CCW);
        //areaPath.setFillType(Path.FillType.WINDING);
        mAreaPathsList.add(areaPath);
        mBitmapCanvas.drawBitmap(bitmap, 576, 324, null);
        //mBitmapCanvas.drawPath(areaPath,mPaintLasso);
        mPSIndex ++;
        if (mPSIndex < mPathStack.size()) {
            for (int i = mPathStack.size() - 1; i >= mPSIndex; i--) {
                mPathStack.remove(i);
                mAreaPathsList.remove(i);
            }
        }
        mPathStack.add(graffitiPath);
        saveAddChanged();
        invalidate();
    }

    private boolean isCircleOpen = false;

    public void setCircleState(boolean state) {
        isCircleOpen = state;
    }

    // 触碰两点间距离
    private float spacing() {
        float x = mTouchXs[0] - mTouchXs[1];
        float y = mTouchYs[0] - mTouchYs[1];
        return (float) Math.sqrt(x * x + y * y);
    }
    private float spacingPath() {
        float x = mTouchXs[0] - mTouchXs[1];
        float y = mTouchYs[0] - mTouchYs[1];
        return (float) Math.sqrt(x * x + y * y);
    }

    // 取手势中心点
    private void midPoint() {
        float x = mTouchXs[0] + mTouchXs[1];
        float y = mTouchYs[0] + mTouchYs[1];
        mid.set(x / 2, y / 2);
    }

    // 取旋转角度
    private float rotation() {
        double delta_x = (mTouchXs[0] - mTouchXs[1]);
        double delta_y = (mTouchYs[0] - mTouchYs[1]);
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private void BuildLassoArea() {
        if (mAreaPathsList.size() == 0) {
            return;
        }
        Path lassoPath = new Path();
        Path lassoAreaPath = new Path();
        Path tempPath = new Path();
        ArrayList<Integer> selectedPathIndex = new ArrayList<>();
        RectF lassoArea = new RectF(toX(minX), toY(minY), toX(maxX), toY(maxY));
        mSelectedPaths = new ArrayList<>();

        lassoPath.addRect(lassoArea, Path.Direction.CCW);
        lassoAreaPath.addRect(lassoArea, Path.Direction.CCW);

        for (int i = mAreaPathsList.size() - 1; i >= 0; i--) {
            if (mPathStack.get(i).mPageIndex == mPageIndexNow) {
                tempPath.op(lassoAreaPath, mAreaPathsList.get(i), Path.Op.INTERSECT);
            }
            if (!tempPath.isEmpty()) {
                if(mPathStack.get(i).mIsBitmap){ //选中为图片
                    //根据缩放旋转修改选中图片
                    mIsLassoBitmap = true;
                    mSelectedPaths.clear();
                    selectedPathIndex.clear();
                    selectedPathIndex.add(i);
                    mSelectedAreaPaths.clear();
                    mSelectedAreaPaths.add(mAreaPathsList.get(i));
                    mOldScale = mPathStack.get(i).mScale;
                    //mLassoBitmapIndex = i;
                    break;
                }else{ //选中为笔画
                    if(mPathStack.get(i).mPen == Pen.HAND){
                        mOldScale = 1;
                        selectedPathIndex.add(i);
                        if (i < mPathStack.size()) {
                            mSelectedPaths.add(mPathStack.get(i).mPath);
                            mSelectedAreaPaths.add(mAreaPathsList.get(i));
                            tempPath = new Path();
                        }
                    }
                }
            }
        }
        //Log.e("lilith", "选中的Path总数" + selectedPathIndex.size());
        if (selectedPathIndex.size() == 0) {
            if (lassoBtn != null) {
                lassoBtn.setSelected(false);
            }
            setPen(Pen.HAND);
            return;
        }
        mPaintLasso.setPathEffect(mEffect);
        if(mIsLassoBitmap){
            lassoAreaPath.set(mAreaPathsList.get(selectedPathIndex.get(0)));
        }else{
            for (int i = 0; i < mSelectedPaths.size(); i++) {
                mBitmapCanvas.drawPath(mSelectedPaths.get(i), mPaintLasso);
            }
        }
        mBitmapCanvas.drawPath(lassoAreaPath, mPaintLasso);
        mLassoAreaPath = lassoAreaPath;
        mSelectedPathsIndex = selectedPathIndex;
        mLassoArea = lassoArea;
        maxX = maxY = minX = minY = 0;
        invalidate();
        mIsLassoMove = true;
    }

    //移动套索和被选中的内容
    private void MoveLasso() {
        //Log.e("Lasso", "mTouchMode= " + mTouchMode);
        if(mTouchMode < 2){
            //mLassoAreaPath.addRect(mLassoArea, Path.Direction.CCW);
            //canvas.drawPath(mLassoAreaPath, mPaintLasso);
            mDX = mTouchXs[0] - mLastTouchXs[0];
            mDY = mTouchYs[0] - mLastTouchYs[0];
            //Log.e("Lasso", "mIsLassoBitmap= " + mIsLassoBitmap );
            if(mIsLassoBitmap){
                mPathStack.get(mSelectedPathsIndex.get(0)).imgX += mDX / mScale;
                mPathStack.get(mSelectedPathsIndex.get(0)).imgY += mDY / mScale;
                mSelectedAreaPaths.get(0).offset(mDX / mScale , mDY / mScale);
                //mBitmapCanvas.drawPath(mSelectedAreaPaths.get(0),mPaintLasso);
                //canvas.drawBitmap(mLassoBitmap, mPathStack.get(mSelectedPathsIndex.get(0)).imgX + mDX,mPathStack.get(mSelectedPathsIndex.get(0)).imgY + mDY,null);
            }else{
               // Log.e("Lasso", "mSelectedPaths.size()= " + mSelectedPaths.size() );
                for (int i = 0; i < mSelectedPaths.size(); i++) {
                    mSelectedPaths.get(i).offset(mDX / mScale, mDY / mScale);
                    mSelectedAreaPaths.get(i).offset(mDX / mScale, mDY / mScale);
                    //canvas.drawPath(mSelectedPaths.get(i), mPaintLasso);
                }
            }
            mLassoAreaPath.offset(mDX / mScale, mDY / mScale);
            mDXS -= mDX;
            mDYS -= mDY;
        }else{
            if(mIsLassoBitmap){
                float newDist = spacing();
               // mPathRotation = rotation();
                if(mPathScale > mMaxScale){
                    mPathScale = mMaxScale;
                }else{
                    mPathScale = mOldScale * (newDist / oldDist);
                }
                //Log.e("Lasso", "mPathRotation= " + mPathRotation);
                //Log.e("Lasso", "mPathScale= " + mPathScale);
                //mPathStack.get(mSelectedPathsIndex.get(0)).mRotation = mPathRotation - oldRotation;
                mPathStack.get(mSelectedPathsIndex.get(0)).mScale = mPathScale;
                //套索区域同比缩放
                Matrix matrix = new Matrix();
                matrix.postScale(mPathScale,mPathScale,mPathStack.get(mSelectedPathsIndex.get(0)).imgX,mPathStack.get(mSelectedPathsIndex.get(0)).imgY);
                mLassoAreaPath.set(mAreaPathsList.get(mSelectedPathsIndex.get(0)));
                mLassoAreaPath.transform(matrix);
            }else{
                Matrix matrix = new Matrix();
                float pathOldDist = pathDist;
                pathDist = spacing();
                mPathScale =  1;
                if(pathDist > pathOldDist){
                    mPathScale = 1.05f;
                }
                if(pathDist < pathOldDist){
                    mPathScale = 0.95f;
                }
                matrix.setScale(mPathScale ,mPathScale ,mid.x,mid.y);
                //套索区域同比缩放
                mLassoAreaPath.transform(matrix);
                //Log.e("Lasso", "mPathScale= " + mPathScale);
                //Log.e("Lasso", "midx= " + mid.x);
                //Log.e("Lasso", "midy= " + mid.y);
                for (int i = 0; i < mSelectedPaths.size(); i++) {
                    mSelectedPaths.get(i).transform(matrix);
                    mSelectedAreaPaths.get(i).transform(matrix);
                    //canvas.drawPath(mSelectedPaths.get(i), mPaintLasso);
                }
            }
        }
        initCanvas();
        //draw(canvas,mPathStack,mPSIndex);
        //invalidate();
    }

    /*private void draw(Canvas canvas, Pen pen, Paint paint, Path path) {
        //paint.setStrokeWidth();
        canvas.drawPath(path, paint);
    }*/

    private void draw(Canvas canvas, CopyOnWriteArrayList<GraffitiPath> pathStack, int index) {
        // 还原堆栈中的记录的操作
        /*for (GraffitiPath path : pathStack) {
            draw(canvas, path);
        }*/
        for (int i = 0; i <= index; i++) {
            if (pathStack.get(i).mPageIndex == mPageIndexNow) {
                draw(canvas, pathStack.get(i));
            }
        }
    }

    private void draw(Canvas canvas, GraffitiPath path) {
        tempMatrix = null;
        if(path.mIsBitmap){
            tempMatrix = new Matrix();
            Bitmap bitmap = ImageUtils.createBitmapFromPath(path.mImgPath, 1920, 1080);
            //Log.e("Lasso", "path.mRotation= " + path.mRotation);
            //Log.e("Lasso", "path.mScale= " + path.mScale);
            //Log.e("Lasso", "draw-path.imgX= " + path.imgX);
            //Log.e("Lasso", "draw-path.imgY= " + path.imgY);
            tempMatrix.setTranslate(path.imgX,path.imgY);
            //tempMatrix.postRotate(path.mRotation,path.imgX + bitmap.getWidth() / 2,path.imgY + bitmap.getHeight() / 2);
            tempMatrix.postScale(path.mScale,path.mScale,path.imgX + bitmap.getWidth() / 2,path.imgY + bitmap.getHeight() / 2);
            bitmap = Bitmap.createScaledBitmap(bitmap,path.imgWidth,path.imgHeight,true);
            //canvas.drawBitmap(bitmap,path.imgX,path.imgY,null);
            bitmap = Bitmap.createBitmap(bitmap,0,0,path.imgWidth,path.imgHeight,tempMatrix,true);
            //canvas.drawBitmap(bitmap,tempMatrix,null);
            canvas.drawBitmap(bitmap,path.imgX,path.imgY,null);
        }else{
            if (path.mPen == Pen.HAND) {
                mPaintDrawTemp.setColor(path.mColor);
                mPaintDrawTemp.setStrokeWidth(path.mStrokeWidth);
                canvas.drawPath(path.mPath, mPaintDrawTemp);
            } else {
                mPaintEraserTemp.setColor(path.mColor);
                mPaintEraserTemp.setStrokeWidth(path.mStrokeWidth);
                canvas.drawPath(path.mPath, mPaintEraserTemp);
            }
        }
        //draw(canvas, path.mPen, mPaintTemp , path.mPath,path.mColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.e("Lilith", "onDraw: 出发啦！！！！！！！");
        //Log.e("Lilith", "mPageIndexNow=" + mPageIndexNow);
        float left = (mCentreTranX + mTransX) / (mPrivateScale * mScale);
        float top = (mCentreTranY + mTransY) / (mPrivateScale * mScale);
        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
        canvas.scale(mPrivateScale * mScale, mPrivateScale * mScale); // 缩放画布
            /*Log.e("Lilith", "mCenterTanX=" + mCentreTranX);
            Log.e("Lilith", "mCentreTranY=" + mCentreTranY);
            Log.e("Lilith", "mTransX=" + mTransX);
            Log.e("Lilith", "mTransY=" + mTransY);*/
        canvas.translate(left, top); // 偏移画布
        canvas.drawBitmap(mBitmaps[mPageIndexNow], 0, 0, null);
        /*if (mIsLassoMove) {
            MoveLasso(canvas);
        }*/
        /*
        if(mIsLassoMove){
            draw(canvas,mPathStack,mPSIndex);
            for (int i = 0; i < mSelectedPaths.size(); i++) {
                canvas.drawPath(mSelectedPaths.get(i), mPaintLasso);
            }
            //canvas.drawPath(mLassoAreaPath,mPaintLasso);
        }*/
        if (mPen == Pen.HAND) {
            if (mPointerIDs[0] != -1) {
                canvas.drawPath(mCurrPaths[0], mPaint);
            }
            if (mPointerIDs[1] != -1) {
                canvas.drawPath(mCurrPaths[1], mPaint);
            }
        }
        if(!isCircleOpen && mPen == Pen.ERASER && !mEraserBitmap.isRecycled()){
            canvas.drawBitmap(mEraserBitmap,mTouchXs[0] - mEraserBitmap.getWidth() / 2,mTouchYs[0] - mEraserBitmap.getHeight() / 2,null);
        }
        //canvas.drawBitmap(mBitmaps[mPageIndexNow], 0, 0, null);
/*        if (mBitmap.isRecycled() || mBitmaps[mPageIndexNow].isRecycled()) {
            return;
        }*/
        /*canvas.save();
        if(mIsLassoMove){
            MoveLasso(canvas);
            doDraw(canvas,0);
        }else{
            if(mTouchMode < 2){
                for(int i = 0;i<2;i++){
                    if(mPointerIDs[i] == moveID){
                        doDraw(canvas,mPointerIDs[i]);
                    }
                }
            }else{
                for(int i = 0;i<2;i++){
                    doDraw(canvas,mPointerIDs[i]);
                }
            }
        }
        canvas.restore();
        invalidate();*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mPen == Pen.LASSO) {
            //套索工具
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mTouchMode = 1;
                    mTouchDownXs[0] = mTouchXs[0] = mLastTouchXs[0] = maxX = minX = event.getX();
                    mTouchDownYs[0] = mTouchYs[0] = mLastTouchYs[0] = maxY = minY = event.getY();
                    if (mIsLassoMove) {
                        //draw();
                        initCanvas();
                        draw(mBitmapCanvas,mPathStack,mPSIndex);
                    } else {
                        mCurrPaths[0] = new Path();
                        mAreaPaths[0] = new Path();
                        mCurrPaths[0].moveTo(toX(mTouchDownXs[0]), toY(mTouchDownYs[0]));
                    }
                    //x_down = event.getX();
                    //y_down = event.getY();
                    //savedMatrix.set(matrix);
                    //invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (mIsLassoMove) {
                        //if (mTouchMode < 2) { // 单点滑动
                        //}else{
                        //}
                        mLastTouchXs[0] = mTouchXs[0];
                        mLastTouchYs[0] = mTouchYs[0];
                        mTouchXs[0] = event.getX();
                        mTouchYs[0] = event.getY();
                        MoveLasso();
                        draw(mBitmapCanvas,mPathStack,mPSIndex);
                        for (int i = 0; i < mSelectedPaths.size(); i++) {
                            mBitmapCanvas.drawPath(mSelectedPaths.get(i), mPaintLasso);
                        }
                        mBitmapCanvas.drawPath(mLassoAreaPath, mPaintLasso);
                    } else {
                        if (mTouchMode < 2) { // 单点滑动
                            mLastTouchXs[0] = mTouchXs[0];
                            mLastTouchYs[0] = mTouchYs[0];
                            mTouchXs[0] = event.getX();
                            mTouchYs[0] = event.getY();
                            mCurrPaths[0].lineTo(toX(mLastTouchXs[0]), toY(mLastTouchYs[0]));
                            mAreaPaths[0].addRect(toX(mLastTouchXs[0]), toY(mLastTouchYs[0]), toX(mTouchXs[0]), toY(mTouchYs[0]), Path.Direction.CCW);
                            if (mLastTouchXs[0] > maxX) {
                                maxX = mLastTouchXs[0];
                            }
                            if (mLastTouchYs[0] > maxY) {
                                maxY = mLastTouchYs[0];
                            }
                            if (mLastTouchXs[0] < minX) {
                                minX = mLastTouchXs[0];
                            }
                            if (mLastTouchYs[0] < minY) {
                                minY = mLastTouchYs[0];
                            }
                            mBitmapCanvas.drawPath(mCurrPaths[0], mPaint);
                            //Log.e("lilith","x=" + mTouchX);
                            //Log.e("lilith","y=" + mTouchY);
                        }
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mTouchMode = 0;
                    if(!isCircleOpen){
                        mLastTouchXs[0] = mTouchXs[0];
                        mLastTouchYs[0] = mTouchYs[0];
                        mTouchXs[0] = event.getX();
                        mTouchYs[0] = event.getY();
                        if (mIsLassoMove) {
                            initCanvas();
                            draw(mBitmapCanvas,mPathStack,mPSIndex);
                            if(mIsLassoBitmap){
                                float scale = mPathStack.get(mSelectedPathsIndex.get(0)).mScale;
                                Path path = new Path();
                                float left, top, right, bottom;
                                left = mPathStack.get(mSelectedPathsIndex.get(0)).imgX;
                                top = mPathStack.get(mSelectedPathsIndex.get(0)).imgY;
                                right = mPathStack.get(mSelectedPathsIndex.get(0)).imgX + mPathStack.get(mSelectedPathsIndex.get(0)).imgWidth * scale;
                                bottom = mPathStack.get(mSelectedPathsIndex.get(0)).imgY + mPathStack.get(mSelectedPathsIndex.get(0)).imgHeight * scale;
                                mLassoArea.set(left, top, right, bottom);
                                path.addRect(mLassoArea, Path.Direction.CCW);
                                //path.setFillType(Path.FillType.WINDING);
                                //Log.e("Lasso", "mPathScale=  " + mPathScale);
                                //Log.e("Lasso", "scale=  " + scale);
                                //Log.e("Lasso", "left=  " + left);
                                //Log.e("Lasso", "top=  " + top);
                                //Log.e("Lasso", "right=  " + right);
                                //Log.e("Lasso", "bottom=  " + bottom);
                                mAreaPathsList.get(mSelectedPathsIndex.get(0)).reset();
                                mAreaPathsList.get(mSelectedPathsIndex.get(0)).set(path);
                                //mSelectedAreaPaths.get(0).set(path);
                                //mBitmapCanvas.drawPath(path,mPaintLasso);
                            }else{
                                saveMoveChanged();
                            }
                            invalidate();
                            //初始化套索参数
                            mPaintLasso.setPathEffect(null);
                            mIsLassoMove = false;
                            mSelectedPaths.clear();
                            mSelectedAreaPaths.clear();
                            //mSelectedPathsIndex.clear();
                            mSelectedPathsIndex = new ArrayList<>();
                            mDXS = mDYS = 0;
                            mIsLassoBitmap = false;
                            if (lassoBtn != null) {
                                lassoBtn.setSelected(false);
                            }
                            setPen(Pen.HAND);
                        } else {
                            mCurrPaths[0].lineTo(mLastTouchXs[0], mLastTouchYs[0]);
                            //Log.e("lilith","lalalalalala" + getPen());
                            initCanvas();
                            draw(mBitmapCanvas, mPathStack, mPSIndex);
                            BuildLassoArea();
                            mLassoArea = new RectF();
                        }
                    }
                    isCircleOpen = false;
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    mTouchMode -= 1;
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mTouchMode += 1;
                    mTouchDownXs[1] = mTouchXs[1] = mLastTouchXs[1] = maxX = minX = event.getX(1);
                    mTouchDownYs[1] = mTouchYs[1] = mLastTouchYs[1] = maxY = minY = event.getY(1);
                    oldDist = spacing();
                    //oldRotation = rotation();
                    savedMatrix.set(matrix);
                    midPoint();
                    return true;
            }
        } else {
            //单指，双指书写，擦除
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if(!isCircleOpen){
                        mTouchMode = 1;
                        //电视触摸面积：0.005，手机触摸面积：0.02
                        mTouchSize = event.getSize(0);
                        //Log.e("Lilith", "TouchSize=" + mTouchSize);
                        if (mTouchSize > 0.005f) {
                        //if (mTouchSize > 0.000f) {
                            setPen(Pen.ERASER);
                            //mTouchSize = 0.02f;
                            if(mTouchSize > 0.02) {
                                mTouchSize = 0.02f;
                                mPaintEraserSize = mTouchSize * 1000 * 6;
                                setEraserBitmap(6f);
                            }else if(mTouchSize >= 0.01){
                                mPaintEraserSize = mTouchSize * 1000 * 6;
                                setEraserBitmap(6f);
                            }else{
                                mPaintEraserSize = mTouchSize * 1000 * 13;
                                setEraserBitmap(13f);
                            }
                            mPaint.setStrokeWidth(mPaintEraserSize);
                        } else {
                            setPen(Pen.HAND);
                        }
                        mPointerIDs[0] = event.getPointerId(0);
                        //Log.e("Lilith", "Action_DownID" + mPointerIDs[0]);
                        //Log.e("Lilith", "pointerID" + mPointerIDs[0]);
                        doTouchDown(event, mPointerIDs[0]);
                        //invalidate();
                    }
                    break;
                //return true;
                case MotionEvent.ACTION_MOVE:
                    if(!isCircleOpen){
                        moveID = event.getPointerId(event.getActionIndex());
                        if(mPen == Pen.ERASER){
                            doTouchMove(event, mPointerIDs[0]);
                        }else{
                            if (mTouchMode < 2) {
                                for (int i = 0; i < 2; i++) {
                                    if (mPointerIDs[i] == moveID) {
                                        doTouchMove(event, mPointerIDs[i]);
                                    }
                                }
                            } else {
                                for (int i = 0; i < 2; i++) {
                                    doTouchMove(event, mPointerIDs[i]);
                                }
                            }
                        }
                        invalidate();
                    }
                    break;
                //return true;
                case MotionEvent.ACTION_UP:
                    if(!isCircleOpen){
                        mTouchMode = 0;
                        upID = event.getPointerId(event.getActionIndex());
                        if(mPen == Pen.ERASER){
                            mEraserBitmap.recycle();
                            if (mPointerIDs[0] != -1) {
                                doTouchUp(event, mPointerIDs[0]);
                            }
                        }else{
                            if (mPointerIDs[0] != -1) {
                                doTouchUp(event, mPointerIDs[0]);
                            }
                            if (mPointerIDs[1] != -1) {
                                doTouchUp(event, mPointerIDs[1]);
                            }
                        }
                    }
                    invalidate();
                    mPointerIDs[0] = mPointerIDs[1] = downID = moveID = upID = -1;
                    isCircleOpen = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
                //return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //解决橡皮擦跳动的问题
                    if(!isCircleOpen && mPen != Pen.ERASER){
                        mTouchMode += 1;
                        downID = event.getPointerId(event.getActionIndex());
                        for (int i = 0; i < 2; i++) {
                            if (mPointerIDs[i] == -1) {
                                mPointerIDs[i] = downID;
                                doTouchDown(event, mPointerIDs[i]);
                            }
                        }
                        //Log.e("Lilith", "现在有" + mTouchMode + "个手指按下" );
                        invalidate();
                    }
                    break;
                //return true;
                case MotionEvent.ACTION_POINTER_UP:
                    if(!isCircleOpen && mPen != Pen.ERASER){
                        mTouchMode -= 1;
                        upID = event.getPointerId(event.getActionIndex());
                        for (int i = 0; i < 2; i++) {
                            if (mPointerIDs[i] != -1 && mPointerIDs[i] == upID) {
                                doTouchUp(event, mPointerIDs[i]);
                                mPointerIDs[i] = -1;
                            }
                        }
                        invalidate();
                    }
                    break;
                //return true;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void doTouchDown(MotionEvent event, int id) {
        try {
            mTouchDownXs[id] = mTouchXs[id] = mLastTouchXs[id] = event.getX(event.findPointerIndex(mPointerIDs[id]));
            mTouchDownYs[id] = mTouchYs[id] = mLastTouchYs[id] = event.getY(event.findPointerIndex(mPointerIDs[id]));
            mCurrPaths[id] = new Path();
            mAreaPaths[id] = new Path();
            mCurrPaths[id].moveTo(toX(mTouchDownXs[id]), toY(mTouchDownYs[id]));
            mAreaPaths[id].moveTo(toX(mTouchDownXs[id]), toY(mTouchDownYs[id]));
        } catch (Exception e) {
            //Log.e("error", "down下标越界！！！！！");
        }
    }


    private void doTouchMove(MotionEvent event, int id) {
        try {
            mLastTouchXs[id] = mTouchXs[id];
            mLastTouchYs[id] = mTouchYs[id];
            mTouchXs[id] = event.getX(event.findPointerIndex(id));
            mTouchYs[id] = event.getY(event.findPointerIndex(id));
            //解决慢速划线有延迟问题
            if (Math.abs(mLastTouchXs[id] - mTouchXs[id]) > 0.3f || Math.abs(mLastTouchYs[id] - mTouchYs[id]) > 0.3f) {
                mCurrPaths[id].quadTo(
                        toX(mLastTouchXs[id]),
                        toY(mLastTouchYs[id]),
                        toX((mTouchXs[id] + mLastTouchXs[id]) / 2),
                        toY((mTouchYs[id] + mLastTouchYs[id]) / 2));
                //mAreaPaths[id].addRect(new RectF(toX(mLastTouchXs[id]), toY(mLastTouchYs[id]), toX(mTouchXs[id]), toY(mTouchYs[id])), Path.Direction.CCW);
                //创造线条区域
                mAreaPaths[id].lineTo(toX(mTouchXs[id]),toY(mTouchYs[id]));
                mAreaPaths[id].lineTo(toX(mTouchXs[id] + 0.01f),toY(mTouchYs[id] - 0.01f));
                mAreaPaths[id].lineTo(toX(mLastTouchXs[id] + 0.01f),toY(mLastTouchYs[id] - 0.01f));
                mAreaPaths[id].lineTo(toX(mLastTouchXs[id]),toY(mLastTouchYs[id]));
                //mAreaPaths[id].lineTo(mTouchXs[id],mTouchYs[id]);
                mAreaPaths[id].moveTo(toX(mTouchXs[id]),toY(mTouchYs[id]));
                //mAreaPaths[id].close();

                if (mPen == Pen.ERASER && !isCircleOpen) {
                    mBitmapCanvas.drawPath(mCurrPaths[id], mPaint);
                }
            }
        } catch (Exception e) {
            //Log.e("error", "move下标越界！！！！！");
        }

        //Log.e("Lilith", "id = " + id);

        //Log.e("lilith","x=" + mTouchX);
        //Log.e("lilith","y=" + mTouchY);
    }

    private void doTouchUp(MotionEvent event, int id) {
        try {
            mLastTouchXs[id] = mTouchXs[id];
            mLastTouchYs[id] = mTouchYs[id];
            mTouchXs[id] = event.getX(event.findPointerIndex(id));
            mTouchYs[id] = event.getY(event.findPointerIndex(id));
            // 为了仅点击时也能出现绘图，必须移动path
            if (mTouchDownXs[id] == mTouchXs[id] && mTouchDownYs[id] == mTouchYs[id] & mTouchDownXs[id] == mLastTouchXs[id] && mTouchDownYs[id] == mLastTouchYs[id]) {
                mTouchXs[id] += VALUE;
                mTouchYs[id] += VALUE;
            }
            // 把操作记录到加入的堆栈中
            GraffitiPath path = null;
            //if(mPen == Pen.HAND){
            //}
            mCurrPaths[id].quadTo(
                    toX(mLastTouchXs[id]),
                    toY(mLastTouchYs[id]),
                    toX((mTouchXs[id] + mLastTouchXs[id]) / 2),
                    toY((mTouchYs[id] + mLastTouchYs[id]) / 2));
            //解决快速双指划线有时会连线的问题
            mTouchDownXs[id] = mTouchXs[id] = mLastTouchXs[id] = event.getX(event.findPointerIndex(mPointerIDs[id]));
            mTouchDownYs[id] = mTouchYs[id] = mLastTouchYs[id] = event.getY(event.findPointerIndex(mPointerIDs[id]));
            mPSIndex++;
            if (mPSIndex < mPathStack.size()) {
                for (int i = mPathStack.size() - 1; i >= mPSIndex; i--) {
                    mPathStack.remove(i);
                    mAreaPathsList.remove(i);
                }
            }
            path = GraffitiPath.toPath(mPen, mPaint.getStrokeWidth(), mPaint.getColor(), mCurrPaths[id], mPageIndexNow,false);
            //addPath(path);
            //mPathStack.add(path);
            mPathStack.add(path);
            if(path.mPen == Pen.ERASER){
                mAreaPaths[id].reset();
            }
            mAreaPathsList.add(mAreaPaths[id]);
            saveAddChanged();
            //Log.e("Lilith", "当前画笔是:" + getPen());
            draw(mBitmapCanvas, path); // 保存到图片中
            //mBitmapCanvas.drawPath(mAreaPaths[id],mPaintLasso);
        } catch (Exception e) {
            //Log.e("error", "up下标越界！！！！！");
        }
    }

    private void setBG() {// 不用resize preview
        int w = mBitmaps[mPageIndexNow].getWidth();
        int h = mBitmaps[mPageIndexNow].getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        //Log.e("Lilith", "bitmap width=" + w);
        //Log.e("Lilith", "bitmap height=" + h);
        //Log.e("Lilith", "width=" + getWidth());
        //Log.e("Lilith", "height=" + getHeight());
        if (nw > nh) {
            mPrivateScale = 1 / nw;
            mPrivateWidth = getWidth();
            mPrivateHeight = (int) (h * mPrivateScale);
        } else {
            mPrivateScale = 1 / nh;
            mPrivateWidth = (int) (w * mPrivateScale);
            mPrivateHeight = getHeight();
        }
        //Log.e("Lilith", "mPrivateScale=" + mPrivateScale);
        //Log.e("Lilith", "mPrivateWidth=" + mPrivateWidth);
        //Log.e("Lilith", "mPrivateHeight=" + mPrivateHeight);
        // 使图片居中
        mCentreTranX = (getWidth() - mPrivateWidth) / 2f;
        mCentreTranY = (getHeight() - mPrivateHeight) / 2f;
        //Log.e("Lilith", "mCenterTanX=" + mCentreTranX);
        //Log.e("Lilith", "mCentreTranY=" + mCentreTranY);
        //Log.e("Lilith", "mTransX=" + mTransX);
        //Log.e("Lilith", "mTransY=" + mTransY);

        initCanvas();
        invalidate();
    }

    public float getmTouchDownX() {
        return mTouchDownXs[0];
    }

    public float getmTouchDownY() {
        return mTouchDownYs[0];
    }

    public Bitmap getmGraffitiBitmap() {
        return mBitmaps[mPageIndexNow];
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
        //draw(mBitmapCanvas, path); // 保存到图片中
    }

    public final void removePath(GraffitiPath path) {
        mPathStack.remove(path);
        mUndoStack.remove(path);
        initCanvas();
        //draw(mBitmapCanvas, mPathStack);
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
        //mGraffitiListener.onSaved(mBitmaps[mPageIndexNow], mBitmapEraser);
    }

    /**
     * 清屏
     */
    public void clear() {
        mPathStack.clear();
        mAreaPathsList.clear();
        mSelectedPathsIndex.clear();
        mSelectedPaths.clear();
        mIsLassoMove = false;
        //        mPathStackBackup.clear();
        initCanvas();
        initCache();
        invalidate();
    }

    /**
     * 撤销
     */
    public void undo() {
        if (mPathStack.size() > 0) {
            mPathStack.remove(mPathStack.size() - 1);
            initCanvas();
            //draw(mBitmapCanvas, mPathStack);
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
    /*public void setColor(int color,int alpha) {
        mColor.setColor(color-0xff000000+(alpha<<24));
        invalidate();
    }
    public void setTmpColor(int color,int alpha) {
        tempColor=color-0xff000000+(alpha<<24);
        invalidate();
    }

    public void setColor(Bitmap bitmap) {
        mColor.setColor(bitmap);
        invalidate();
    }

    public void setColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
        mColor.setColor(bitmap, tileX, tileY);
        invalidate();
    }

    public PenColor getGraffitiColor() {
        return mColor;
    }*/

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
        setPaint(mPen);
        invalidate();
    }

    public Pen getPen() {
        return mPen;
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


    public void setPaintDrawSize(float paintSize) {
        mPaintDrawSize = paintSize;
        invalidate();
    }

    public void setPaintEraserSize(float paintSize) {
        mPaintEraser.setStrokeWidth(paintSize);
    }

    public void setTmpPaintSize(float paintSize) {
        //tempSize = paintSize;
        invalidate();
    }


    public float getPaintSize() {
        return mPaint.getStrokeWidth();
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

    public static class GraffitiPath implements Undoable {
        Pen mPen; // 画笔类型
        float mStrokeWidth; // 大小
        int mColor; // 颜色
        Path mPath; // 画笔的路径
        int mPageIndex; //画在哪一页
        boolean mIsBitmap; //是否是图片
        String mImgPath;
        float imgX,imgY;
        int imgWidth,imgHeight;
        float mScale;
        float mRotation;
        //int index; //序号
        //float maxX, maxY, minX, minY;
        //Bitmap bm;

        static GraffitiPath toPath(Pen pen, float width, int color, Path p, int pageindex,boolean isbitmap) {
            GraffitiPath path = new GraffitiPath();
            path.mPen = pen;
            path.mStrokeWidth = width;
            path.mColor = color;
            path.mPath = p;
            path.mPageIndex = pageindex;
            path.mIsBitmap = isbitmap;
            return path;
        }

        static GraffitiPath toBitmap(boolean isbitmap,String imgPath,int imgwidth,int imgheight,float imgx,float imgy,float scale,float rotation,int pageindex) {
            GraffitiPath path = new GraffitiPath();
            path.mIsBitmap = isbitmap;
            path.mImgPath = imgPath;
            path.imgWidth = imgwidth;
            path.imgHeight = imgheight;
            path.imgX = imgx;
            path.imgY = imgy;
            path.mScale = scale;
            path.mRotation = rotation;
            path.mPageIndex = pageindex;
            return path;
        }

    }

    public int getPaintColor() {
        return mPaintColor;
    }

    public void setLassoBtn(View v) {
        lassoBtn = v;
    }
}
