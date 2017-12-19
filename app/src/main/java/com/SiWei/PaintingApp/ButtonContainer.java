package com.SiWei.PaintingApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.AppCompatImageButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by yxxy6 on 12/14/2017.
 */

public class ButtonContainer {
    private Context mContext;
    private int maxItem, mButtonIndex;
    private LinearLayout mLayout;
    private ArrayList<ImageButton> mButtons;

    ButtonContainer(Context c) {
        mContext = c;
        maxItem = 5;
        mButtons = new ArrayList<ImageButton>();
        mButtonIndex = 0;
    }

    public ImageButton addView(Bitmap img) {
        ImageButton button = new ImageButton(mContext);
        button.setImageBitmap(img);
        button.setMaxWidth(240);
        button.setMinimumWidth(240);
        button.setMaxHeight(135);
        button.setMinimumHeight(135);
        mButtons.add(button);
        mButtonIndex++;
        mLayout.addView(button);
        return button;
    }

    public void removeView(int index) {
        if (mLayout.getChildCount() > 1) {
            mLayout.removeViewAt(index);
        }
    }

    public void setLayout(LinearLayout l) {
        mLayout = l;
    }

    public ArrayList<ImageIndexedButton> initButtons(Bitmap[] imgs) {
        ArrayList<ImageIndexedButton> imgBtns = new ArrayList<>();
        for (int i = 0; i < imgs.length; i++
                ) {
            if (imgs[i] != null) {
                ImageIndexedButton button = new ImageIndexedButton(mContext);
                button.setImageBitmap(imgs[i]);
                button.setMaxWidth(240);
                button.setMinimumWidth(240);
                button.setMaxHeight(135);
                button.setMinimumHeight(135);
                button.setIndex(i);
                imgBtns.add(button);
                mButtons.add(button);
                mButtonIndex++;
                mLayout.addView(button);
            }
        }
        return imgBtns;
    }
    public void removeButtons(){
        mLayout.removeAllViews();
    }

    /**
     * Created by yxxy6 on 12/19/2017.
     */

    public static class ImageIndexedButton extends AppCompatImageButton {
        private int buttonIndex;
        public ImageIndexedButton(Context context) {
            super(context);
            buttonIndex = -1;
        }
        public void setIndex(int index){
            buttonIndex = index;
        }
        public int getIndex(){
            return buttonIndex;
        }
    }
}
