package com.SiWei.PaintingApp;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

/**
 * Created by xiaod on 2017/10/19.
 */

public class PenColor {
    public enum Type{
        COLOR,
        BITMAP
    }

    private int mColor;
    private Bitmap mBitmap;
    private Type mType;
    private Shader.TileMode mTileX = Shader.TileMode.MIRROR;
    private Shader.TileMode mTileY = Shader.TileMode.MIRROR;  // 镜像

    public PenColor(int color){
        mType = Type.COLOR;
        mColor = color;
    }

    public PenColor(Bitmap bitmap) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
    }

    public PenColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
        mTileX = tileX;
        mTileY = tileY;
    }

    public void initColor(Paint paint, Matrix matrix) {
        if (mType == Type.COLOR) {
            paint.setColor(mColor);
        } else if (mType == Type.BITMAP) {
            BitmapShader shader = new BitmapShader(mBitmap, mTileX, mTileY);
            shader.setLocalMatrix(matrix);
            paint.setShader(shader);
        }
    }

    public void setColor(int color) {
        mType = Type.COLOR;
        mColor = color;
    }

    public void setColor(Bitmap bitmap) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
    }

    public void setColor(Bitmap bitmap, Shader.TileMode tileX, Shader.TileMode tileY) {
        mType = Type.BITMAP;
        mBitmap = bitmap;
        mTileX = tileX;
        mTileY = tileY;
    }

    public int getColor() {
        return mColor;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Type getType() {
        return mType;
    }

    public PenColor copy() {
        PenColor color = null;
        if (mType == Type.COLOR) {
            color = new PenColor(mColor);
        } else {
            color = new PenColor(mBitmap);
        }
        color.mTileX = mTileX;
        color.mTileY = mTileY;
        return color;
    }


}
