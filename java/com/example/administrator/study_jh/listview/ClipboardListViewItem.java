package com.example.administrator.study_jh.listview;

/**
 * Created by Administrator on 2018-01-30.
 */

public class ClipboardListViewItem {
    private String path;
    private String name;

    public String getPath(){return path;}
    public String getName(){return name;}

    public ClipboardListViewItem(String path,String name){
        this.path=path;
        this.name=name;
    }
}
