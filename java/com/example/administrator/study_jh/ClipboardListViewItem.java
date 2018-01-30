package com.example.administrator.study_jh;

/**
 * Created by Administrator on 2018-01-30.
 */

public class ClipboardListViewItem {
    private String path;
    private String name;
    private String flex;

    public String getPath(){return path;}
    public String getName(){return name;}
    public String getFlex(){return flex;}

    public ClipboardListViewItem(String path,String name, String flex){
        this.path=path;
        this.name=name;
        this.flex=flex;
    }
}
