package com.example.administrator.study_jh.listview;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.CheckBox;

public class ListviewItem {
    private Drawable icon;
    private String name;
    private String path;
    private String flex;
    private boolean mBoolean;

    public Drawable getIcon(){return icon;}
    public String getName(){return name;}
    public String getFlex(){return flex;}
    public String getPath(){return path;}

    public ListviewItem(Drawable icon, String name, String path, String flex, boolean mBoolean){
        this.icon=icon;
        this.name=name;
        this.path=path;
        this.flex=flex;
        this.mBoolean = mBoolean;
    }

    public boolean isBoolean() {
        return mBoolean;
    }
    public void setBoolean(boolean mBoolean) {
        this.mBoolean = mBoolean;
    }
    public void setImage(Drawable icon) { this.icon = icon; }
}