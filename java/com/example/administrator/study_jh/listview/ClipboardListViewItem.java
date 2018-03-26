package com.example.administrator.study_jh.listview;

import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2018-01-30.
 */

public class ClipboardListViewItem {
    private Drawable icon;
    private String action;
    private String path;
    private String name;

    public Drawable getIcon(){return icon;}
    public String getAction(){return action;}
    public String getPath(){return path;}
    public String getName(){return name;}

    public ClipboardListViewItem(Drawable icon, String action, String path, String name){
        this.icon=icon;
        this.action=action;
        this.path=path;
        this.name=name;
    }
}
