package com.SiWei.PaintingApp;

import android.content.Context;
import android.graphics.Bitmap;
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
}
