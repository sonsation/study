package com.example.administrator.study_jh.listview;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2018-01-30.
 */

public class TabItem {
    private Drawable icon;
    private Fragment fragment;
    private String name;

    public Drawable getIcon(){return icon;}
    public Fragment getFragment(){return fragment;}
    public String getName(){return name;}

    public TabItem(Drawable icon, Fragment fragment, String name){
        this.icon=icon;
        this.fragment=fragment;
        this.name=name;
    }
}
