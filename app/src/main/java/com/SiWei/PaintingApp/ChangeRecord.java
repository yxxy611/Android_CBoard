package com.SiWei.PaintingApp;

import java.util.ArrayList;

/**
 * Created by xiaod on 17/12/8/0008.
 */

public class ChangeRecord {

    ArrayList<Integer> mIndexList;
    boolean isMove = false;
    int psIndex;
    float dX,dY;
    float mScale;
    float mRotation;

    public ChangeRecord(boolean ismove,int index){
        this.isMove = ismove;
        this.psIndex = index;
    }

    public ChangeRecord(ArrayList<Integer> indexlist,boolean ismove,float dx,float dy){
        this.mIndexList = indexlist;
        this.isMove = ismove;
        this.dX = dx;
        this.dY = dy;
    }

}
