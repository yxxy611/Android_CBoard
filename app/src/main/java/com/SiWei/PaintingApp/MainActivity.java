package com.SiWei.PaintingApp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cn.forward.androids.utils.ImageUtils;
import cn.forward.androids.utils.LogUtil;
import cn.forward.androids.utils.ThreadUtil;
import cn.hzw.imageselector.ImageSelectorActivity;

import static com.SiWei.PaintingApp.R.id.eraser_bar;
import static com.SiWei.PaintingApp.R.id.main_menu;
import static com.SiWei.PaintingApp.R.id.slider;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, PaletteView.Callback, View.OnClickListener, CustomSlideToUnlockView.CallBack, View.OnTouchListener, SeekBar.OnSeekBarChangeListener, Handler.Callback {

    //    private View mUndoView;
    //    private View mRedoView;
    //    private View mPenView;
    //    private View mEraserView;
    //    private View mClearView;

    public static final String KEY_IMAGE_PATH = "key_image_path";
    public static final int RESULT_ERROR = -111; // 出现错误

    private String mImagePath;
    private Bitmap mBitmap;

    public static final int REQ_CODE_SELECT_IMAGE = 100;
    public static final int REQ_CODE_GRAFFITI = 101;
    private String imgPath;
    private TextView mPath;
    private int imgCODE = 0;

    private CirclePanelView mCirclePanelView;
    private int[] paletteReq;//需求颜色色板
    //扫码相关
    private QRShareView mQRShareView;
    private LinearLayout mQRLayout;
    private RelativeLayout mPWDlayout;
    private Switch mPwdSwitch;
    private PasswordInputView edtPwd;
    private NumKeyboardUtil keyboardUtil;

    private PaletteView mPaletteView;
    private ProgressDialog mSaveProgressDlg;
    private static final int MSG_SAVE_SUCCESS = 1;
    private static final int MSG_SAVE_FAILED = 2;
    private Handler mHandler;

    private SeekBar mSeekBar;
    private GridLayout mMainMenu;
    private LinearLayout mEraserBar;
    private CustomSlideToUnlockView mCustomSlideToUnlockView;

    private TextView mPaintSizeView;

    private View mBtnColor;
    private Runnable mUpdateScale;

    private int mTouchMode;
    private boolean mIsMovingPic = false;

    // 手势操作相关
    private float mOldScale, mOldDist, mNewDist, mToucheCentreXOnGraffiti,
            mToucheCentreYOnGraffiti, mTouchCentreX, mTouchCentreY;// 双指距离

    private float mTouchLastX, mTouchLastY;

    private boolean mIsScaling = false;
    private float mScale = 1f;
    private final float mMaxScale = 3f; // 最大缩放倍数
    private final float mMinScale = 1f; // 最小缩放倍数
    private final int TIME_SPAN = 40;
    private View mBtnMovePic, mBtnHidePanel, mSettingsPanel;

    private int mTouchSlop;

    private AlphaAnimation mViewShowAnimation, mViewHideAnimation; // view隐藏和显示时用到的渐变动画

    // 当前屏幕中心点对应在GraffitiView中的点的坐标
    float mCenterXOnGraffiti;
    float mCenterYOnGraffiti;

    private TouchMode touchMode;

    //private View mBtnColor;

    private ImageButton black_btn, red_btn, green_btn, blue_btn, purple_btn;
    private ImageButton[] mColorBtns;

    private ButtonContainer mButtonContainer;
    private LinearLayout mButtonContainerLayout;

    Toast tScale;

    public enum TouchMode {
        Move,
        Board
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        mPaletteView = (PaletteView) findViewById(R.id.palette);
        // mPaletteView.setCallback(this);
        mPaletteView.setOnTouchListener(this);
        mCirclePanelView = (CirclePanelView) findViewById(R.id.circlePanel);
        paletteReq = mCirclePanelView.getPaletteRequired(new int[]{7, 0, 3, 5});
        touchMode = TouchMode.Board;
        //二维码相关
        mQRShareView = (QRShareView) findViewById(R.id.QRView);
        mQRLayout = (LinearLayout) findViewById(R.id.QRLayout);
        mPWDlayout = (RelativeLayout) findViewById(R.id.pwd_layout);
        edtPwd = (PasswordInputView) findViewById(R.id.trader_pwd_set_pwd_edittext);
        edtPwd.setInputType(InputType.TYPE_NULL); // 屏蔽系统软键盘
        // 自定义软键盘
        if (keyboardUtil == null)
            keyboardUtil = new NumKeyboardUtil(this, this, edtPwd);
        edtPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                keyboardUtil.showKeyboard();
                return false;
            }
        });
        mPwdSwitch = (Switch) findViewById(R.id.pwd_switch);
        mPwdSwitch.setOnCheckedChangeListener(this);
        mColorBtns = new ImageButton[]{
                (ImageButton) findViewById(R.id.pencolor_black_button),
                (ImageButton) findViewById(R.id.pencolor_red_button),
                (ImageButton) findViewById(R.id.pencolor_green_button),
                (ImageButton) findViewById(R.id.pencolor_blue_button),
                (ImageButton) findViewById(R.id.pencolor_purple_button)
        };
        black_btn = (ImageButton) findViewById(R.id.pencolor_black_button);
        red_btn = (ImageButton) findViewById(R.id.pencolor_red_button);
        green_btn = (ImageButton) findViewById(R.id.pencolor_green_button);
        blue_btn = (ImageButton) findViewById(R.id.pencolor_blue_button);
        purple_btn = (ImageButton) findViewById(R.id.pencolor_purple_button);


        //        mUndoView = findViewById(R.id.undo);
        //        mRedoView = findViewById(R.id.redo);
        //        mPenView = findViewById(R.id.pen);
        //        mPenView.setSelected(true);
        //        mEraserView = findViewById(R.id.eraser);
        //        mClearView = findViewById(R.id.clear);
        //
        //        mUndoView.setOnClickListener(this);
        //        mRedoView.setOnClickListener(this);
        //        mPenView.setOnClickListener(this);
        //        mEraserView.setOnClickListener(this);
        //        mClearView.setOnClickListener(this);
        //
        //        mUndoView.setEnabled(false);
        //        mRedoView.setEnabled(false);

        mHandler = new Handler(this);
        mMainMenu = (GridLayout) findViewById(main_menu);
        mEraserBar = (LinearLayout) findViewById(eraser_bar);

        mCustomSlideToUnlockView = (CustomSlideToUnlockView) findViewById(slider);
        mCustomSlideToUnlockView.setmCallBack(this);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_gray);
        mBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        //mPaletteView.setColor(mCirclePanelView.getmColorValue(),mCirclePanelView.getmOpacityValue());
        //Log.e("lilith", "paintSize =" + mCirclePanelView.getmBrushValue()*3);
        //mPaletteView.setPaintSize(mCirclePanelView.getmBrushValue()*3);
        mPaletteView.setLassoBtn(findViewById(R.id.lasso_button));
        //mBtnColor = findViewById(R.id.btn_set_color);
        //缩略图相关
        mButtonContainer = new ButtonContainer(this);
        mButtonContainerLayout = (LinearLayout) findViewById(R.id.thumbnail_container);
        mButtonContainerLayout.setVisibility(View.INVISIBLE);
        mButtonContainer.setLayout(mButtonContainerLayout);
        Bitmap bitmap1 = new BitmapFactory().decodeResource(getResources(),R.drawable.ic_add);
        mButtonContainer.addView(bitmap1);

        //tScale = Toast.makeText(getApplicationContext(), "当前缩放比例: " + mScale * 100 + "%", Toast.LENGTH_SHORT);

    }


    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        //mPaletteView.setPenRawSize(progress);
        //mPaletteView.setEraserSize(progress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(MSG_SAVE_FAILED);
        mHandler.removeMessages(MSG_SAVE_SUCCESS);
    }

    private void initSaveProgressDlg() {
        mSaveProgressDlg = new ProgressDialog(this);
        mSaveProgressDlg.setMessage("正在保存,请稍候...");
        mSaveProgressDlg.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SAVE_FAILED:
                mSaveProgressDlg.dismiss();
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                break;
            case MSG_SAVE_SUCCESS:
                mSaveProgressDlg.dismiss();
                Toast.makeText(this, "画板已保存", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private static void scanFile(Context context, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

    private static String saveImage(Bitmap bmp, int quality) {
        if (bmp == null) {
            return null;
        }
        File appDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (appDir == null) {
            return null;
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                if (mSaveProgressDlg == null) {
                    initSaveProgressDlg();
                }
                mSaveProgressDlg.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bm = mPaletteView.buildBitmap();
                        String savedFile = saveImage(bm, 100);
                        if (savedFile != null) {
                            scanFile(MainActivity.this, savedFile);
                            mHandler.obtainMessage(MSG_SAVE_SUCCESS).sendToTarget();
                        } else {
                            mHandler.obtainMessage(MSG_SAVE_FAILED).sendToTarget();
                        }
                    }
                }).start();
                break;
        }
        return true;
    }

    @Override
    public void onUndoRedoStatusChanged() {
        //        mUndoView.setEnabled(mPaletteView.canUndo());
        //        mRedoView.setEnabled(mPaletteView.canRedo());
    }

    private boolean mDone = false;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.pwd_switch:
                if (isChecked) {
                    mPWDlayout.setVisibility(View.VISIBLE);
                    Log.i("deng", "onCheckedChanged: " + isChecked);
                    // TODO: 12/3/2017 准备接收密码
                } else {
                    mPWDlayout.setVisibility(View.GONE);
                    // TODO: 12/3/2017 无需密码
                }
                break;
        }
    }


    @Override
    public void onClick(View v) {
        mDone = false;
        switch (v.getId()) {
            case R.id.pencolor_black_button:
                setColorBtn(0);
                mPaletteView.setPaintColor(0xffbbbbbb);
                break;
            case R.id.pencolor_red_button:
                setColorBtn(1);
                mPaletteView.setPaintColor(paletteReq[0]);
                break;
            case R.id.pencolor_green_button:
                setColorBtn(2);
                mPaletteView.setPaintColor(paletteReq[1]);
                break;
            case R.id.pencolor_blue_button:
                setColorBtn(3);
                mPaletteView.setPaintColor(paletteReq[2]);
                break;
            case R.id.pencolor_purple_button:
                setColorBtn(4);
                mPaletteView.setPaintColor(paletteReq[3]);
                break;
            case R.id.undo_button:
                mPaletteView.unDo();
                //mPaletteView.Test1();
                break;
            case R.id.redo_button:
                mPaletteView.reDo();
                //mPaletteView.Test2();
                //mCirclePanelView.awake();
                //mPaletteView.redo();
                break;
            //            case R.id.lasso_button:
            //                v.setSelected(true);
            //                mPaletteView.setPen(PaletteView.Pen.HAND);
            ////                mEraserView.setSelected(false);
            //                //mPaletteView.setMode(PaletteView.Mode.DRAW);
            //                break;
            case R.id.erase_menu_button:
                Log.i("eraserbar", toString().valueOf(mEraserBar.getVisibility()));
                if (mEraserBar.getVisibility() == View.INVISIBLE) {
                    mEraserBar.setVisibility(View.VISIBLE);
                } else {
                    mEraserBar.setVisibility(View.INVISIBLE);
                }
                v.setSelected(!v.isSelected());
                //                mPenView.setSelected(false);

                mPaletteView.setPen(PaletteView.Pen.ERASER);
                //mPaletteView.setMode(PaletteView.Mode.ERASER);
                break;
            case R.id.clear:
                mPaletteView.clear();
                break;
            case R.id.main_menu_button:
                if (mMainMenu.getVisibility() == View.INVISIBLE) {
                    mMainMenu.setVisibility(View.VISIBLE);
                } else {
                    mMainMenu.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.new_page_button:
                mPaletteView.crateNewPage();
                ImageButton button = mButtonContainer.addView(new BitmapFactory().decodeResource(getResources(),R.drawable.ic_add));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("deng", "onClick: "+12321321);
                    }
                });
                break;
            case R.id.prev_page_button:
                mPaletteView.prevPage();
                break;
            case R.id.next_page_button:
                mPaletteView.nextPage();
                break;
            case R.id.remove_page_button:
                mPaletteView.removePage(0);
                //mButtonContainer.removeView(0);
                //mPaletteView.removePage(0);
                //mCirclePanelView.dismiss();
                break;
            case R.id.page_count_button:
                if (mButtonContainerLayout.getVisibility() == View.INVISIBLE) {
                    mButtonContainerLayout.setVisibility(View.VISIBLE);
                } else {
                    mButtonContainerLayout.setVisibility(View.INVISIBLE);
                }
                v.setSelected(!v.isSelected());
                break;
            case R.id.select_drag_button:
                v.setSelected(!v.isSelected());
                mIsMovingPic = v.isSelected();
                if (mIsMovingPic) {
                    touchMode = TouchMode.Move;
                } else {
                    touchMode = TouchMode.Board;
                }
                if (mIsMovingPic) {
                    Toast.makeText(getApplicationContext(), R.string.graffiti_moving_pic, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "移动、缩放模式结束", Toast.LENGTH_SHORT).show();
                }
                mDone = true;
                break;
            case R.id.lasso_button:
                mPaletteView.setPen(PaletteView.Pen.LASSO);
                v.setSelected(true);
                break;
            case R.id.close_button:
                this.finish();
                break;
            case R.id.save_button:
                //onSaved(mPaletteView.getmGraffitiBitmap());
                onSaved(mergeBitmap(mBitmap, mPaletteView.getmGraffitiBitmap()));
                break;
            case R.id.change_bg_button:
                ImageSelectorActivity.startActivityForResult(REQ_CODE_SELECT_IMAGE, MainActivity.this, null, false);
                imgCODE = 0;
                break;
            case R.id.insert_img_button:
               /* System.out.println(mPaletteView.getOriginalPivotX());
                System.out.println(mPaletteView.getOriginalPivotY());
                createGraffitiBitmap(null, mPaletteView.getOriginalPivotX(),mPaletteView.getOriginalPivotY());*/
                ImageSelectorActivity.startActivityForResult(REQ_CODE_SELECT_IMAGE, MainActivity.this, null, false);
                imgCODE = 1;
                break;
            case R.id.share_button://二维码分享
                if (mQRLayout.getVisibility() == View.GONE) {
                    mQRShareView.setQRContent("http://www.baidu.com");
                    mQRLayout.setVisibility(View.VISIBLE);
                } else {
                    mQRLayout.setVisibility(View.GONE);
                }

                break;
            case R.id.trader_pwd_set_next_button://密码确定
                // TODO: 12/3/2017 记录传送密码
                mPWDlayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (data == null) {
                return;
            }
            final ArrayList<String> list = data.getStringArrayListExtra(ImageSelectorActivity.KEY_PATH_LIST);
            if (list != null && list.size() > 0) {
                LogUtil.d("Graffiti", list.get(0));
                imgPath = list.get(0);
                mBitmap = ImageUtils.createBitmapFromPath(imgPath, 1920, 1080);
                if (mBitmap != null) {
                    switch (imgCODE) {
                        case 0:
                            mBitmap = ImageUtils.createBitmapFromPath(imgPath, 1920, 1080);
                            //Drawable bg =new BitmapDrawable(mBitmap);
                            /*Resources resources = getResources();
                            Drawable bg = resources.getDrawable(R.drawable.bg);*/
                            Drawable bg = new BitmapDrawable(mBitmap);
                            mPaletteView.setBackground(bg);
                            //mPaletteView.changeBG(mBitmap);
                            Toast.makeText(getApplicationContext(), "背景更换成功", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            mBitmap = ImageUtils.createBitmapFromPath(imgPath, 1500, 800);
                            mPaletteView.insertImage(mBitmap,imgPath);
                            Toast.makeText(getApplicationContext(), "图片插入成功", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }
    }

    // 添加贴图
   /* private void createGraffitiBitmap(final GraffitiBitmap graffitiBitmap, final float x, final float y) {
        Activity activity = this;

        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        ViewGroup container = (ViewGroup) View.inflate(getApplicationContext(), R.layout.graffiti_create_bitmap, null);
        final Dialog finalDialog = dialog;
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalDialog.dismiss();
            }
        });
        dialog.setContentView(container);

        ViewGroup selectorContainer = (ViewGroup) finalDialog.findViewById(R.id.graffiti_image_selector_container);
        ImageSelectorView selectorView = new ImageSelectorView(this, false, 1, null, new ImageSelectorView.ImageSelectorListener() {
            @Override
            public void onCancel() {
                finalDialog.dismiss();
            }

            @Override
            public void onEnter(List<String> pathList) {
                finalDialog.dismiss();
                Bitmap bitmap = ImageUtils.createBitmapFromPath(pathList.get(0), mPaletteView.getWidth() / 4, mPaletteView.getHeight() / 4);

                if (graffitiBitmap == null) {
                    mPaletteView.addSelectableItem(new GraffitiBitmap(mPaletteView.getPen(), bitmap, mPaletteView.getPaintSize(), mPaletteView.getColor().copy(),
                            0, mPaletteView.getGraffitiRotateDegree(), x, y, mPaletteView.getOriginalPivotX(), mPaletteView.getOriginalPivotY()));
                } else {
                    graffitiBitmap.setBitmap(bitmap);
                }
                mPaletteView.invalidate();
            }
        });
        selectorContainer.addView(selectorView);
    }*/

    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {
        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            Log.e("Lilith", "backBitmap=" + backBitmap + ";frontBitmap=" + frontBitmap);
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Rect baseRect = new Rect(0, 0, 1920, 1080);
        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null);
        return bitmap;
    }

    public void onSaved(Bitmap bitmap) { // 保存图片
        /*if (bitmapEraser != null) {
            bitmapEraser.recycle(); // 回收图片，不再涂鸦，避免内存溢出
        }*/
        File graffitiFile = null;
        File file = null;
        String savePath = null;
        //String savePath = mPaletteView.mSavePath;
        boolean isDir = true;
        //boolean isDir = mPaletteView.mSavePathIsDir;
        if (TextUtils.isEmpty(savePath)) {
            File dcimFile = new File(Environment.getExternalStorageDirectory(), "DCIM");
            graffitiFile = new File(dcimFile, "CBoard");
            //　保存的路径
            file = new File(graffitiFile, System.currentTimeMillis() + ".jpg");
        } else {
            if (isDir) {
                graffitiFile = new File(savePath);
                //　保存的路径
                file = new File(graffitiFile, System.currentTimeMillis() + ".jpg");
            } else {
                file = new File(savePath);
                graffitiFile = file.getParentFile();
            }
        }
        graffitiFile.mkdirs();

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
            ImageUtils.addImage(getContentResolver(), file.getAbsolutePath());
            Intent intent = new Intent();
            intent.putExtra(KEY_IMAGE_PATH, file.getAbsolutePath());
            setResult(Activity.RESULT_OK, intent);
            Toast.makeText(getApplicationContext(), "保存成功！", Toast.LENGTH_SHORT).show();
            //finish();
        } catch (Exception e) {
            e.printStackTrace();
            onError(PaletteView.ERROR_SAVE, e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void onError(int i, String msg) {
        setResult(RESULT_ERROR);
        finish();
    }

    @Override
    public void onSlide(int distance) {

    }

    @Override
    public void onUnlocked() {
        Log.i("slider", "wiped");
        mPaletteView.clear();
    }

    boolean mIsBusy = false; // 避免双指滑动，手指抬起时处理单指事件。

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (touchMode) {
            case Move:
                if (!mIsMovingPic) {
                    return false;  // 交给下一层的涂鸦处理
                }
                mScale = mPaletteView.getScale();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchMode = 1;
                        mTouchLastX = event.getX();
                        mTouchLastY = event.getY();
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mTouchMode = 0;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (mTouchMode < 2) { // 单点滑动
                            if (mIsBusy) { // 从多点触摸变为单点触摸，忽略该次事件，避免从双指缩放变为单指移动时图片瞬间移动
                                mIsBusy = false;
                                mTouchLastX = event.getX();
                                mTouchLastY = event.getY();
                                return true;
                            }
                            float tranX = event.getX() - mTouchLastX;
                            float tranY = event.getY() - mTouchLastY;
                            mPaletteView.setTrans(mPaletteView.getTransX() + tranX, mPaletteView.getTransY() + tranY);
                            mTouchLastX = event.getX();
                            mTouchLastY = event.getY();
                        } else { // 多点
                            mNewDist = spacing(event);// 两点滑动时的距离
                            if (Math.abs(mNewDist - mOldDist) >= mTouchSlop) {
                                float scale = mNewDist / mOldDist;
                                mScale = mOldScale * scale;

                                if (mScale > mMaxScale) {
                                    mScale = mMaxScale;
                                }
                                if (mScale < mMinScale) { // 最小倍数
                                    mScale = mMinScale;
                                }
                                // 围绕坐标(0,0)缩放图片
                                mPaletteView.setScale(mScale);
                                Log.e("Lilith", "mScale= " + mScale);
                                // 缩放后，偏移图片，以产生围绕某个点缩放的效果
                                float transX = mPaletteView.toTransX(mTouchCentreX, mToucheCentreXOnGraffiti);
                                float transY = mPaletteView.toTransY(mTouchCentreY, mToucheCentreYOnGraffiti);
                                mPaletteView.setTrans(transX, transY);
                                /*tScale = Toast.makeText(getApplicationContext(), "当前缩放比例: " + mScale * 100 + "%", Toast.LENGTH_SHORT);
                                tScale.cancel();
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        tScale.show();   // 会发现延迟之后就显示出来了
                                    }
                                }, 200);*/
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_POINTER_UP:
                        mTouchMode -= 1;
                        return true;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mTouchMode += 1;
                        mOldScale = mPaletteView.getScale();
                        mOldDist = spacing(event);// 两点按下时的距离
                        mTouchCentreX = (event.getX(0) + event.getX(1)) / 2;// 不用减trans
                        mTouchCentreY = (event.getY(0) + event.getY(1)) / 2;
                        mToucheCentreXOnGraffiti = mPaletteView.toX(mTouchCentreX);
                        mToucheCentreYOnGraffiti = mPaletteView.toY(mTouchCentreY);
                        mIsBusy = true; // 标志位多点触摸
                        return true;
                }
                break;
            case Board:
                int index = event.getActionIndex();
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.e("asdf", "第1个手指按下");
                        break;
                    case MotionEvent.ACTION_UP:
                        mCirclePanelView.dismiss();
                        mPaletteView.setCircleState(false);
                        Log.e("asdf", "最后1个手指抬起");
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        if (!mCirclePanelView.isAwake() && index == 4) {
                            if (mCirclePanelView.getGestureCenter() == null) {
                                mCirclePanelView.setGestureCenter((event.getX(0) + event.getX(1) + event.getX(2) + event.getX(3) + event.getX(4)) / 5, (event.getY(0) + event.getY(1) + event.getY(2) + event.getY(3) + event.getY(4)) / 5);
                            }
                            mCirclePanelView.awake();
                            mPaletteView.setCircleState(true);
                        } else if (mCirclePanelView.isAwake()) {
                            mCirclePanelView.confirm();
                            //mPaletteView.setTempColor(mCirclePanelView.getmColorValue(),mCirclePanelView.getmOpacityValue());
                            if (mCirclePanelView.isColorChanged) {
                                mPaletteView.setPaintColor(mCirclePanelView.getmColorValue());
                                mPaletteView.setPaintOpacity(mCirclePanelView.getmOpacityValue());
                                setColorBtn(-1);
                            } else {
                                mPaletteView.setPaintOpacity(mCirclePanelView.getmOpacityValue());
                            }

                            //mPaletteView.setTmpPaintSize(mCirclePanelView.getmBrushValue()*3);
                            mPaletteView.setPaintSize(mCirclePanelView.getmBrushValue() * 3);
                        }
                        Log.e("asdf", "第" + (index + 1) + "个手指按下");
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        Log.e("asdf", "第" + (index + 1) + "个手指抬起");
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (event.getPointerCount() == 5 && mCirclePanelView.isAwake()) {
                            mCirclePanelView.setVectorMoved(event.getX(1), event.getY(1));
                            mCirclePanelView.menuScrollT();
                        }
                        break;
                }
                break;
        }
        return false;
    }

    /**
     * 计算两指间的距离
     *
     * @param event
     * @return
     */

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 放大缩小
     */
    private class ScaleOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    scalePic(v);
                    v.setSelected(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsScaling = false;
                    v.setSelected(false);
                    break;
            }
            return true;
        }
    }

    /**
     * 缩放
     *
     * @param v
     */
    public void scalePic(View v) {
        if (mIsScaling)
            return;
        mIsScaling = true;
        mScale = mPaletteView.getScale();

        // 确定当前屏幕中心点对应在GraffitiView中的点的坐标，之后将围绕这个点缩放
        mCenterXOnGraffiti = mPaletteView.toX(mPaletteView.getWidth() / 2);
        mCenterYOnGraffiti = mPaletteView.toY(mPaletteView.getHeight() / 2);

        /*if (v.getId() == R.id.btn_amplifier) { // 放大
            ThreadUtil.getInstance().runOnAsyncThread(new Runnable() {
                public void run() {
                    do {
                        mScale += 0.05f;
                        if (mScale > mMaxScale) {
                            mScale = mMaxScale;
                            mIsScaling = false;
                        }
                        updateScale();
                        try {
                            Thread.sleep(TIME_SPAN);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (mIsScaling);

                }
            });
        } else if (v.getId() == R.id.btn_reduce) { // 缩小
            ThreadUtil.getInstance().runOnAsyncThread(new Runnable() {
                public void run() {
                    do {
                        mScale -= 0.05f;
                        if (mScale < mMinScale) {
                            mScale = mMinScale;
                            mIsScaling = false;
                        }
                        updateScale();
                        try {
                            Thread.sleep(TIME_SPAN);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (mIsScaling);
                }
            });
        }*/
    }

    private void updateScale() {
        if (mUpdateScale == null) {

            mUpdateScale = new Runnable() {
                public void run() {
                    // 围绕坐标(0,0)缩放图片
                    mPaletteView.setScale(mScale);
                    // 缩放后，偏移图片，以产生围绕某个点缩放的效果
                    float transX = mPaletteView.toTransX(mPaletteView.getWidth() / 2, mCenterXOnGraffiti);
                    float transY = mPaletteView.toTransY(mPaletteView.getHeight() / 2, mCenterYOnGraffiti);
                    mPaletteView.setTrans(transX, transY);
                }
            };
        }
        ThreadUtil.getInstance().runOnMainThread(mUpdateScale);
    }

    private void setColorBtn(int index) {
        if (index == -1) {
            for (int i = 0; i < mColorBtns.length; i++) {
                mColorBtns[i].setSelected(false);
            }
        } else {
            for (int i = 0; i < mColorBtns.length; i++) {
                mColorBtns[i].setSelected(false);
            }
            mColorBtns[index].setSelected(true);
        }
    }

}
