package com.SiWei.PaintingApp;

import android.util.Log;
import android.widget.Button;

import static android.content.ContentValues.TAG;

/**
 * Created by yxxy6 on 12/21/2017.
 */

public class PageButtonConfig {
    private Button mButton;
    private PaletteView mPaletteView;
    public PageButtonConfig(Button btn,PaletteView pv){
        mButton = btn;
        mPaletteView = pv;
        update();
    }
    public void setPage(int c,int s){
        mButton.setText(String.valueOf(c)+"/"+String.valueOf(s));
    }
    public void update(){
        Log.i(TAG, "currentP: "+mPaletteView.getPageIndex()+"\n"+"totalP: "+mPaletteView.getPageCount());
        setPage(mPaletteView.getPageIndex(),mPaletteView.getPageCount());

    }
}
