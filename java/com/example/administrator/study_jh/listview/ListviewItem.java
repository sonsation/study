package com.example.administrator.study_jh.listview;

import android.graphics.drawable.Drawable;
import android.widget.CheckBox;

public class ListviewItem {
    private Drawable icon;
    private String name;
    private String flex;

    public Drawable getIcon(){return icon;}
    public String getName(){return name;}
    public String getFlex(){return flex;}

    public ListviewItem(Drawable icon, String name, String flex){
        this.icon=icon;
        this.name=name;
        this.flex=flex;
    }
}