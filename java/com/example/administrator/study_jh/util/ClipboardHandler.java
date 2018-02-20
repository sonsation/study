package com.example.administrator.study_jh.util;

import com.example.administrator.study_jh.listview.ClipboardListViewItem;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by Administrator on 2018-02-21.
 */

public class ClipboardHandler {

    public static ArrayList<ClipboardListViewItem> listItem = new ArrayList<>();

    public ClipboardHandler(){}

    public static void setClip(ClipboardListViewItem path){
        listItem.add(path);
    }

    public static void setclear(){
        listItem.clear();
    }

    public static ArrayList<ClipboardListViewItem> getClip(){
        return listItem;
    }

}
