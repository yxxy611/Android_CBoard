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
        //mPaintDraw.setAntiAlias(true);
        //mPaintDraw.setDither(true);
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
        mPaintEraser.setStrokeCap(Paint.Cap.SQUARE);// 圆滑
        mClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        //mClearMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
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
        mPaintEraserTemp.setStrokeCap(Paint.Cap.SQUARE);// 圆滑
        mPaintEraserTemp.setXfermode(mClearMode);
        //初始化套索
        mPaintLasso = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaintLasso.setStyle(Paint.Style.STROKE);
        mPaintLasso.setFilterBitmap(true);
        mPaintLasso.setStrokeWidth(5);
        mPaintLasso.setColor(Color.YELLOW);
        mPaintLasso.setAlpha(90);
        mPaintLasso.setAntiAlias(true);
        mPaintLasso.setDither(true);
        mPaintLasso.setStrokeJoin(Paint.Join.ROUND);
        mPaintLasso.setStrokeCap(Paint.Cap.ROUND);// 圆滑
        mPaintLasso.setPathEffect(new DashPathEffect(new float[]{30, 30}, 10));

        mPen = Pen.HAND;
        mPaint = mPaintDraw;

        lassoBtn = null;
        //mTempPath = new Path();
        //mAreaPath = new Path();
        //初始化画布Bitmap
        mBitmaps[mPageIndexNow] = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmaps[mPageIndexNow]);
        Bitmap tempBitmap = Bitmap.createBitmap(mBitmaps[mPageIndexNow]);
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
        Log.e("Lilith", "mCRIndex=" + mCRIndex);
        Log.e("Lilith", "mPSIndex=" + mPSIndex);
    }

    private void setEraserBitmap(int num){
        float scale;
        mEraserBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.eraserimg100);
        scale = mTouchSize * num * 10;
        Log.e("Lilith", "scale="+scale);
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

    public void Test1() {
        Path testPath1 = new Path();
        Path testPath2 = new Path();
        testPath1.lineTo(500, 500);
        mBitmapCanvas.drawPath(testPath1, mPaintDraw);
        invalidate();
        RectF testRectF1 = new RectF();
        RectF testRectF2 = new RectF();
        /*RectF bounds1 = new RectF();
        RectF bounds2 = new RectF();
        Region region1 = new Region();
        Region region2 = new Region();
        //testRectF1.set(500,500,1000,600);
        //testPath1.addRect(testRectF1,Path.Direction.CCW);
        //testRectF2.set(500,500,1000,800);
        //testPath2.addRect(testRectF2,Path.Direction.CCW);
       //testPath1.setLastPoint(500,500);
        testPath1.lineTo(500,500);
        testPath1.moveTo(500,500);
        testPath1.lineTo(500,600);
        testPath2.lineTo(400,500);
        testPath2.moveTo(400,500);
        testPath2.lineTo(600,500);
        testPath1.computeBounds(bounds1,true);
        region1.setPath(testPath1,)
        mBitmapCanvas.drawPath(testPath1,mPaint);
        mBitmapCanvas.drawPath(testPath2,mPaint);
        testPath1.op(testPath2, Path.Op.INTERSECT);*/
        //testRectF1.set(500,500,501,800);
        //testPath1.addRect(testRectF1, Path.Direction.CCW);
        //for(int i = 0;i < mAreaPaths.size();i++){
        //testPath2.op(testPath1,mAreaPaths.get(i), Path.Op.INTERSECT);
        //}
        //mBitmapCanvas.drawRect(testRectF1,mPaint);
        //Log.e("lilith", "是否存在交集" + !testPath2.isEmpty());
        //setPen(Pen.LASSO);
        //Log.e("lilith","当前画笔是:" + getPen());
        //invalidate();
        /*testRectF.set(500,500,1000,600);
        mBitmapCanvas.drawRect(testRectF,mPaint);
        invalidate();*/
        //cacheBitmapsList.get(0) = Bitmap.createBitmap(mBitmaps[mPageIndexNow]);
    }

    public void Test2() {
        Bitmap[] bms = getPagePreview();
        for(int i=0;i<mMaxPage;i++){
            if(bms[i] != null){
                mBitmapCanvas.drawBitmap(bms[i],1630,880,null);
            }
        }
        /*Path testPath1 = new Path();
        Path testPath2 = new Path();
        testPath1.moveTo(300, 300);
        testPath1.lineTo(600, 600);
        mBitmapCanvas.drawPath(testPath1, mPaintDraw);
        testPath1.offset(-100, -100);
        mBitmapCanvas.drawPath(testPath1, mPaintDraw);*/
        invalidate();
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
        Log.e("Lilith", "mCRIndex=" + mCRIndex);
        Log.e("Lilith", "mPSIndex=" + mPSIndex);
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
        Log.e("Lilith", "mCRIndex=" + mCRIndex);
        Log.e("Lilith", "mPSIndex=" + mPSIndex);
        Log.e("Lilith", "index=" + mChangeRecordList.get(mCRIndex).psIndex);
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
                Log.e("Lilith", "index=" + mChangeRecordList.get(mCRIndex).psIndex);
                draw(mBitmapCanvas, mPathStack, mPSIndex);
            }
            invalidate();
            mCRIndex--;
        } else {
            Log.e("Lilith", "后已经到头了");
        }
        Log.e("Lilith", "mCRIndex=" + mCRIndex);
        Log.e("Lilith", "mPSIndex=" + mPSIndex);
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
            Log.e("Lilith", "后已经到头了");
        }
        Log.e("Lilith", "mCRIndex=" + mCRIndex);
        Log.e("Lilith", "mPSIndex=" + mPSIndex);
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
        lassoAreaPath.setFillType(Path.FillType.WINDING);

        for (int i = 0; i < mAreaPathsList.size(); i++) {
            if (mPathStack.get(i).mPageIndex == mPageIndexNow) {
                tempPath.op(lassoAreaPath, mAreaPathsList.get(i), Path.Op.INTERSECT);
            }
            if (!tempPath.isEmpty()) {
                selectedPathIndex.add(i);
                tempPath.setFillType(Path.FillType.WINDING);
                if (i < mPathStack.size()) {
                    mSelectedPaths.add(mPathStack.get(i).mPath);
                    mSelectedAreaPaths.add(mAreaPathsList.get(i));
                    tempPath = new Path();
                }
            }
        }
        Log.e("lilith", "选中的Path总数" + selectedPathIndex.size());
        if (selectedPathIndex.size() == 0) {
            return;
        }
        //initCanvas();
        /*for(int i = 0;i < selectedPath.size();i++){
            for(int j =0;j < mPathStack.size();j++){
                if(selectedPath.get(i) == j){
                    draw(mBitmapCanvas,mPathStack.get(j));
                    break;
                }
            }
        }*/
        //mBitmapCanvas.drawColor(Color.WHITE);
        mBitmapCanvas.drawPath(lassoAreaPath, mPaintLasso);
        for (int i = 0; i < mSelectedPaths.size(); i++) {
            mBitmapCanvas.drawPath(mSelectedPaths.get(i), mPaintLasso);
        }
        //path = GraffitiPath.toPath(mPen,mPaintLasso.getStrokeWidth(), Color.YELLOW, lassoPath);
        //mPathStack.add(path);
        mLassoArea = lassoArea;
        mSelectedPathsIndex = selectedPathIndex;
        //mLassoAreaPath = lassoAreaPath;
        maxX = maxY = minX = minY = 0;
        invalidate();
    }

    //移动套索和被选中的内容
    private void MoveLasso(Canvas canvas) {
        mLassoAreaPath = new Path();
        float left, top, right, bottom;
        left = mLassoArea.left + mTouchXs[0] - mLastTouchXs[0];
        top = mLassoArea.top + mTouchYs[0] - mLastTouchYs[0];
        right = mLassoArea.right + mTouchXs[0] - mLastTouchXs[0];
        bottom = mLassoArea.bottom + mTouchYs[0] - mLastTouchYs[0];
        mLassoArea.set(left, top, right, bottom);
        mLassoAreaPath.addRect(mLassoArea, Path.Direction.CCW);
        canvas.drawPath(mLassoAreaPath, mPaintLasso);
        mDX = mTouchXs[0] - mLastTouchXs[0];
        mDY = mTouchYs[0] - mLastTouchYs[0];
        for (int i = 0; i < mSelectedPaths.size(); i++) {
            mSelectedPaths.get(i).offset(mDX / mScale, mDY / mScale);
            mSelectedAreaPaths.get(i).offset(mDX / mScale, mDY / mScale);
            canvas.drawPath(mSelectedPaths.get(i), mPaintLasso);
            //canvas.drawPath(mSelectedAreaPaths.get(i),mPaintLasso);
        }
        mDXS -= mDX;
        mDYS -= mDY;
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

    public void removePage(int index) {
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
            mPageRealCount --;
            calculatePageNumber();
            invalidate();
        }
    }

    private void clearCache(){
        for(int i=0;i<mPathStack.size();i++){

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

    public void setPenAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    /*private void reDraw(){
        if (mDrawingList != null) {
            mBitmaps[mPageIndexNow].eraseColor(Color.TRANSPARENT);
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
        Log.i("deng", "setPaintColor_Opa: " + Integer.toBinaryString(mPaintColor & 0xff000000));
        Log.i("deng", "setPaintColor_Color: " + Integer.toBinaryString(color & 0x00ffffff));
        mPaintColor = mPaintColor & 0xff000000 | color & 0x00ffffff;
        mPaintDraw.setColor(mPaintColor);
        Log.i("deng", "setPaintColor: " + Integer.toBinaryString(mPaintColor));
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

    /*private void resetPaint(Pen pen, Paint paint,PenColor color) {
        switch (pen) { // 设置画笔
            case HAND:
                mPaint = mPaintDraw;
                color.initColor(paint, null);
                break;
            case ERASER:
                mPaint = mPaintEraser;
                break;
        }
    }*/

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
        graffitiPath = GraffitiPath.toBitmap(true,imgpath,576,324);
        mPathStack.add(graffitiPath);
        Path areaPath = new Path();
        mInsertBitmaps.add(bitmap);

        RectF areaRectF = new RectF(576, 324, 576 + bitmap.getWidth(), 324 + bitmap.getHeight());
        areaPath.addRect(areaRectF, Path.Direction.CCW);
        areaPath.setFillType(Path.FillType.WINDING);
        mAreaPathsList.add(areaPath);
        mBitmapCanvas.drawBitmap(bitmap, 576, 324, null);
        invalidate();
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
        if (path.mPen == Pen.HAND) {
            mPaintDrawTemp.setColor(path.mColor);
            mPaintDrawTemp.setStrokeWidth(path.mStrokeWidth);
            canvas.drawPath(path.mPath, mPaintDrawTemp);
        } else {
            mPaintEraserTemp.setColor(path.mColor);
            mPaintEraserTemp.setStrokeWidth(path.mStrokeWidth);
            canvas.drawPath(path.mPath, mPaintEraserTemp);
        }
        //draw(canvas, path.mPen, mPaintTemp , path.mPath,path.mColor);
    }

    //画出所有未被选中的path
    private void draw() {
        initCanvas();
        flag:
        for (int i = 0; i <= mPSIndex; i++) {
            for (int j = 0; j < mSelectedPaths.size(); j++) {
                Log.e("lilith", "真假" + (mSelectedPaths.get(j) == mPathStack.get(i).mPath));
                if (mSelectedPaths.get(j) == mPathStack.get(i).mPath) {
                    continue flag;
                }
            }
            mPaintDrawTemp.setStrokeWidth(mPathStack.get(i).mStrokeWidth);
            mPaintDrawTemp.setColor(mPathStack.get(i).mColor);
            if (mPathStack.get(i).mPageIndex == mPageIndexNow) {
                mBitmapCanvas.drawPath(mPathStack.get(i).mPath, mPaintDrawTemp);
            }
        }
        for (int i = 0; i < mInsertBitmaps.size(); i++) {
            mBitmapCanvas.drawBitmap(mInsertBitmaps.get(i), 576, 324, null);
        }
        invalidate();
    }

    private void doDraw(Canvas canvas, int id) {
        /*if(id == -1){
            return;
        }
        float left = (mCentreTranX + mTransX) / (mPrivateScale * mScale);
        float top = (mCentreTranY + mTransY) / (mPrivateScale * mScale);
        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
        canvas.scale(mPrivateScale * mScale, mPrivateScale * mScale); // 缩放画布
        canvas.translate(left, top); // 偏移画布

        // 绘制涂鸦
        canvas.drawBitmap(mBitmaps[mPageIndexNow], 0, 0, null);

        if (mIsPainting) {  //画在view的画布上
            Path path;
            float span = 0;
            // 为了仅点击时也能出现绘图，必须移动path
            if (mTouchDownXs[id] == mTouchXs[id] && mTouchDownYs[id] == mTouchYs[id] && mTouchDownXs[id] == mLastTouchXs[id] && mTouchDownYs[id] == mLastTouchYs[id]) {
                mTempPaths[id] = new Path();
                //mTempPaths[id].reset();
                mTempPaths[id].addPath(mCurrPaths[id]);
                mTempPaths[id].quadTo(
                        toX(mLastTouchXs[id]),
                        toY(mLastTouchYs[id]),
                        toX((mTouchXs[id] + mLastTouchXs[id] + VALUE) / 2),
                        toY((mTouchYs[id] + mLastTouchYs[id] + VALUE) / 2));
                path = mTempPaths[id];
                span = VALUE;
            } else {
                path = mCurrPaths[id];
                span = 0;
            }
            path = mCurrPaths[id];
            // 画触摸的路径
            //mPaint.setStrokeWidth(mPaintDrawSize);
            if(mPen == Pen.HAND){
                draw(canvas, mPen, mPaint, path, mPaintColor);
            }else if(mPen == Pen.LASSO){
                draw(canvas, mPen, mPaint, path, mPaintColor);
            }else{
                draw(mBitmapCanvas, mPen, mPaint, path, mPaintColor);
            }
        }*/
    }

    public void setBitmapScale(float scale) {
        float left = (mCentreTranX + mTransX) / (mPrivateScale * mScale);
        float top = (mCentreTranY + mTransY) / (mPrivateScale * mScale);
        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系
        mBitmapCanvas.scale(mPrivateScale * mScale, mPrivateScale * mScale); // 缩放画布
        mBitmapCanvas.translate(left, top); // 偏移画布
        invalidate();
        /*Matrix matrix = new Matrix();
        matrix.postScale(scale,scale);
        Bitmap resizeBmp = Bitmap.createBitmap(mBitmaps[mPageIndexNow],0,0,mBitmaps[mPageIndexNow].getWidth(),mBitmaps[mPageIndexNow].getHeight(),matrix,true);
        mBitmapCanvas.setBitmap(resizeBmp);
        invalidate();*/
    }

    private boolean isCircleOpen = false;

    public void setCircleState(boolean state) {
        isCircleOpen = state;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("Lilith", "onDraw: 出发啦！！！！！！！");
        Log.e("Lilith", "mPageIndexNow=" + mPageIndexNow);
        if (!isCircleOpen) {
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
            if (mIsLassoMove) {
                MoveLasso(canvas);
            }
            if (mPen == Pen.HAND) {
                if (mPointerIDs[0] != -1) {
                    canvas.drawPath(mCurrPaths[0], mPaint);
                }
                if (mPointerIDs[1] != -1) {
                    canvas.drawPath(mCurrPaths[1], mPaint);
                }
            }
            if(mPen == Pen.ERASER  && !mEraserBitmap.isRecycled()){
                canvas.drawBitmap(mEraserBitmap,mTouchXs[0] - mEraserBitmap.getWidth() / 2,mTouchYs[0] - mEraserBitmap.getHeight() / 2,null);
            }
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
        if (mPen == Pen.LASSO && !isCircleOpen) {
            //套索工具
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mTouchMode = 1;
                    mTouchDownXs[0] = mTouchXs[0] = mLastTouchXs[0] = maxX = minX = event.getX();
                    mTouchDownYs[0] = mTouchYs[0] = mLastTouchYs[0] = maxY = minY = event.getY();
                    if (mIsLassoMove) {
                        draw();
                    } else {
                        mCurrPaths[0] = new Path();
                        mAreaPaths[0] = new Path();
                        mCurrPaths[0].moveTo(toX(mTouchDownXs[0]), toY(mTouchDownYs[0]));
                    }
                    //invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (mTouchMode < 2) { // 单点滑动
                        mLastTouchXs[0] = mTouchXs[0];
                        mLastTouchYs[0] = mTouchYs[0];
                        mTouchXs[0] = event.getX();
                        mTouchYs[0] = event.getY();
                        if (mIsLassoMove) {
                        } else {
                            /*mCurrPaths[0].quadTo(
                                    toX(mLastTouchXs[0]),
                                    toY(mLastTouchYs[0]),
                                    toX((mTouchXs[0] + mLastTouchXs[0]) / 2),
                                    toY((mTouchYs[0] + mLastTouchYs[0]) / 2));*/
                            //mCurrPaths[0].lineTo(mTouchXs[0],mTouchYs[0]);
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
                        }
                        //Log.e("lilith","x=" + mTouchX);
                        //Log.e("lilith","y=" + mTouchY);
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mTouchMode = 0;
                    mLastTouchXs[0] = mTouchXs[0];
                    mLastTouchYs[0] = mTouchYs[0];
                    mTouchXs[0] = event.getX();
                    mTouchYs[0] = event.getY();
                    if (mIsLassoMove) {
                        initCanvas();
                        //flag:
                        for (int i = 0; i <= mPSIndex; i++) {
                            mPaintDrawTemp.setStrokeWidth(mPathStack.get(i).mStrokeWidth);
                            mPaintDrawTemp.setColor(mPathStack.get(i).mColor);
                            if (mPathStack.get(i).mPageIndex == mPageIndexNow) {
                                mBitmapCanvas.drawPath(mPathStack.get(i).mPath, mPaintDrawTemp);
                            }
                        }
                        invalidate();
                        saveMoveChanged();
                        mIsLassoMove = false;
                        mSelectedPaths.clear();
                        mSelectedAreaPaths.clear();
                        mSelectedPathsIndex.clear();
                        mSelectedPathsIndex = new ArrayList<>();
                        mDXS = mDYS = 0;
                        if (lassoBtn != null) {
                            lassoBtn.setSelected(false);
                        }
                        setPen(Pen.HAND);
                    } else {
                        /*mCurrPaths[0].quadTo(
                                toX(mLastTouchXs[0]),
                                toY(mLastTouchYs[0]),
                                toX((mTouchXs[0] + mLastTouchXs[0]) / 2),
                                toY((mTouchYs[0] + mLastTouchYs[0]) / 2));*/
                        mCurrPaths[0].lineTo(mLastTouchXs[0], mLastTouchYs[0]);
                        //Log.e("lilith","lalalalalala" + getPen());
                        initCanvas();
                        draw(mBitmapCanvas, mPathStack, mPSIndex);
                        BuildLassoArea();
                        mIsLassoMove = true;
                        mLassoArea = new RectF();
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_POINTER_UP:
                    mTouchMode -= 1;
                    invalidate();
                    return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mTouchMode += 1;
                    invalidate();
                    return true;
            }
        } else if (!isCircleOpen) {
            //单指，双指书写
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mTouchMode = 1;
                    //电视触摸面积：0.003，手机触摸面积：0.02
                    mTouchSize = event.getSize(0);
                    Log.e("Lilith", "TouchSize=" + mTouchSize);
                    if (mTouchSize > 0.02) {
                        setPen(Pen.ERASER);
                        if (mTouchSize > 0.01) {
                            mPaintEraserSize = mTouchSize * 1000 * 6;
                            setEraserBitmap(6);
                        } else {
                            mPaintEraserSize = mTouchSize * 1000 * 13;
                            setEraserBitmap(13);
                        }
                        mPaint.setStrokeWidth(mPaintEraserSize);
                    } else {
                        setPen(Pen.HAND);
                    }
                    mPointerIDs[0] = event.getPointerId(0);
                    //Log.e("Lilith", "Action_DownID" + mPointerIDs[0]);
                    //Log.e("Lilith", "pointerID" + mPointerIDs[0]);
                    doTouchDown(event, mPointerIDs[0]);
                    invalidate();
                    break;
                //return true;
                case MotionEvent.ACTION_MOVE:
                    moveID = event.getPointerId(event.getActionIndex());
                    /*if(mPointerIDs[0] == moveID){
                        doTouchMove(event,mPointerIDs[0]);
                    }else{
                        doTouchMove(event,mPointerIDs[1]);
                    }*/
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
                    invalidate();
                    break;
                //return true;
                case MotionEvent.ACTION_UP:
                    mTouchMode = 0;
                    upID = event.getPointerId(event.getActionIndex());
                    //mTouchID1 += 1;
                    //Log.e("Lilith", "action up num" + mTouchID1);
                    /*for(int i = 0;i<2;i++){
                        if(mPointerIDs[i] != -1 && mPointerIDs[i] == upID){
                            doTouchUp(event,mPointerIDs[i]);
                        }
                    }*/
                    if(mPen == Pen.ERASER){
                        mEraserBitmap.recycle();
                    }
                    if (mPointerIDs[0] != -1) {
                        doTouchUp(event, mPointerIDs[0]);
                    }
                    if (mPointerIDs[1] != -1) {
                        doTouchUp(event, mPointerIDs[1]);
                    }
                    invalidate();
                    mPointerIDs[0] = mPointerIDs[1] = downID = moveID = upID = -1;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
                //return true;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mTouchMode += 1;
                    if (mTouchMode > 2) {
                        return false;
                    }
                    downID = event.getPointerId(event.getActionIndex());
                    for (int i = 0; i < 2; i++) {
                        if (mPointerIDs[i] == -1) {
                            mPointerIDs[i] = downID;
                            doTouchDown(event, mPointerIDs[i]);
                        }
                    }
                    //Log.e("Lilith", "现在有" + mTouchMode + "个手指按下" );
                    invalidate();
                    break;
                //return true;
                case MotionEvent.ACTION_POINTER_UP:
                    mTouchMode -= 1;
                    if (mTouchMode > 2) {
                        break;
                    }
                    upID = event.getPointerId(event.getActionIndex());
                    for (int i = 0; i < 2; i++) {
                        if (mPointerIDs[i] != -1 && mPointerIDs[i] == upID) {
                            doTouchUp(event, mPointerIDs[i]);
                            mPointerIDs[i] = -1;
                        }
                    }
                    invalidate();
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
        } catch (Exception e) {
            Log.e("error", "down下标越界！！！！！");
        }
    }


    private void doTouchMove(MotionEvent event, int id) {
        try {
            mLastTouchXs[id] = mTouchXs[id];
            mLastTouchYs[id] = mTouchYs[id];
            mTouchXs[id] = event.getX(event.findPointerIndex(id));
            mTouchYs[id] = event.getY(event.findPointerIndex(id));
            if (Math.abs(mLastTouchXs[id] - mTouchXs[id]) > 1 || Math.abs(mLastTouchYs[id] - mTouchYs[id]) > 1) {
                mCurrPaths[id].quadTo(
                        toX(mLastTouchXs[id]),
                        toY(mLastTouchYs[id]),
                        toX((mTouchXs[id] + mLastTouchXs[id]) / 2),
                        toY((mTouchYs[id] + mLastTouchYs[id]) / 2));
                //mCurrPaths[id].lineTo(mLastTouchXs[id],mLastTouchYs[id]);
                //mCurrPaths[id].lineTo(mTouchXs[id],mTouchYs[id]);
                //mCurrPaths[id].lineTo(mTouchXs[id]+0.1f,mTouchYs[id]+0.1f);
                //mCurrPaths[id].lineTo(mLastTouchXs[id]+0.1f,mLastTouchYs[id]+0.1f);
                //mCurrPaths[id].lineTo(mLastTouchXs[id],mLastTouchYs[id]);
                //mCurrPaths[id].lineTo(mTouchXs[id],mTouchYs[id]);
                mAreaPaths[id].addRect(new RectF(toX(mLastTouchXs[id]), toY(mLastTouchYs[id]), toX(mTouchXs[id]), toY(mTouchYs[id])), Path.Direction.CCW);
                if (mPen == Pen.ERASER) {
                    mBitmapCanvas.drawPath(mCurrPaths[id], mPaint);
                }
                //if(mLastTouchXs[id] != mTouchXs[id] && mLastTouchYs[id] != mTouchYs[id]){
                //}
            }
        } catch (Exception e) {
            Log.e("error", "move下标越界！！！！！");
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
            //mCurrPaths[id].lineTo(mLastTouchXs[id],mLastTouchYs[id]);
            //mCurrPaths[id].lineTo(mLastTouchXs[id],mLastTouchYs[id]);
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
            mAreaPathsList.add(mAreaPaths[id]);
            saveAddChanged();
            Log.e("Lilith", "当前画笔是:" + getPen());
            draw(mBitmapCanvas, path); // 保存到图片中
        } catch (Exception e) {
            Log.e("error", "up下标越界！！！！！");
        }
    }

    /*public final void addPath(GraffitiPath path) {
        mPathStack.add(path);
        mUndoStack.add(path);
        draw(mBitmapCanvas, path); // 保存到图片中
    }*/

    private void setBG() {// 不用resize preview
        int w = mBitmaps[mPageIndexNow].getWidth();
        int h = mBitmaps[mPageIndexNow].getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        Log.e("Lilith", "bitmap width=" + w);
        Log.e("Lilith", "bitmap height=" + h);
        Log.e("Lilith", "width=" + getWidth());
        Log.e("Lilith", "height=" + getHeight());
        if (nw > nh) {
            mPrivateScale = 1 / nw;
            mPrivateWidth = getWidth();
            mPrivateHeight = (int) (h * mPrivateScale);
        } else {
            mPrivateScale = 1 / nh;
            mPrivateWidth = (int) (w * mPrivateScale);
            mPrivateHeight = getHeight();
        }
        Log.e("Lilith", "mPrivateScale=" + mPrivateScale);
        Log.e("Lilith", "mPrivateWidth=" + mPrivateWidth);
        Log.e("Lilith", "mPrivateHeight=" + mPrivateHeight);
        // 使图片居中
        mCentreTranX = (getWidth() - mPrivateWidth) / 2f;
        mCentreTranY = (getHeight() - mPrivateHeight) / 2f;
        Log.e("Lilith", "mCenterTanX=" + mCentreTranX);
        Log.e("Lilith", "mCentreTranY=" + mCentreTranY);
        Log.e("Lilith", "mTransX=" + mTransX);
        Log.e("Lilith", "mTransY=" + mTransY);

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

    /*public Bitmap getmBitmapEraser(){
        //return mBitmapEraser;
    }*/

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
        int index; //序号
        float maxX, maxY, minX, minY;
        Bitmap bm;

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

        static GraffitiPath toBitmap(boolean isbitmap,String imgPath,float imgx,float imgy) {
            GraffitiPath path = new GraffitiPath();
            path.mIsBitmap = isbitmap;
            path.mImgPath = imgPath;
            path.imgX = imgx;
            path.imgY = imgy;
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
