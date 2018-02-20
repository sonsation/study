package com.example.administrator.study_jh.listview;

import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2018-01-30.
 */

public class TabItem {
    private Fragment fragment;
    private String name;

    public Fragment getFragment(){return fragment;}
    public String getName(){return name;}

    public TabItem(Fragment fName, String name){
        this.fragment=fName;
        this.name=name;
    }
}
