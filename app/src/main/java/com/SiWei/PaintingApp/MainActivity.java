package com.SiWei.PaintingApp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.SiWei.PaintingApp.R.id.main_menu;
import static com.SiWei.PaintingApp.R.id.slider;

public class MainActivity extends AppCompatActivity implements PaletteView.Callback,View.OnClickListener, CustomSlideToUnlockView.CallBack,View.OnTouchListener, SeekBar.OnSeekBarChangeListener, Handler.Callback {

//    private View mUndoView;
//    private View mRedoView;
//    private View mPenView;
//    private View mEraserView;
//    private View mClearView;
    private PaletteView mPaletteView;
    private ProgressDialog mSaveProgressDlg;
    private static final int MSG_SAVE_SUCCESS = 1;
    private static final int MSG_SAVE_FAILED = 2;
    private Handler mHandler;

    private SeekBar mSeekBar;
    private GridLayout mMainMenu;
    private CustomSlideToUnlockView mCustomSlideToUnlockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mPaletteView = (PaletteView) findViewById(R.id.palette);
       // mPaletteView.setCallback(this);
        mPaletteView.setOnTouchListener(this);

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

        mSeekBar = (SeekBar) findViewById(R.id.seekBar2);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(10);
        mSeekBar.setOnSeekBarChangeListener(this);

        mMainMenu = (GridLayout) findViewById(main_menu);

        mCustomSlideToUnlockView = (CustomSlideToUnlockView) findViewById(slider);
        mCustomSlideToUnlockView.setmCallBack(this);

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
        mPaletteView.setPenRawSize(progress);
        mPaletteView.setEraserSize(progress);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.undo_button:
                mPaletteView.undo();
                break;
            case R.id.redo_button:
                mPaletteView.redo();
                break;
            case R.id.pen_button:
                v.setSelected(true);
//                mEraserView.setSelected(false);
                mPaletteView.setMode(PaletteView.Mode.DRAW);
                break;
            case R.id.erase_button:
                v.setSelected(true);
//                mPenView.setSelected(false);
                mPaletteView.setMode(PaletteView.Mode.ERASER);
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
        }
    }

    @Override
    public void onSlide(int distance) {

    }

    @Override
    public void onUnlocked() {
        Log.i("slider", "wiped");
        mPaletteView.clear();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i("panel", "down"+String.valueOf(motionEvent.getSize()));
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i("panel", "move"+String.valueOf(motionEvent.getSize()));
        }
        return false;
    }
}
