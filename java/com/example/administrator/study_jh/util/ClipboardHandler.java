package com.example.administrator.study_jh.util;

import android.util.Log;

import com.example.administrator.study_jh.listview.ClipboardListViewItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Administrator on 2018-02-21.
 */

public class ClipboardHandler {

    public enum Action {
        Copy,
        Cut,
    }

    public static HashMap<ClipboardListViewItem, ClipboardListViewItem> clipListItem = new HashMap<>();
    public static ArrayList<ClipboardListViewItem> listItem = new ArrayList<>();

    public ClipboardHandler(){}

    public static void setClip(int index, ClipboardListViewItem path){
        listItem.add(path);
    }

    public static void removeClip(int position){ listItem.remove(position); }

    public static void setclear(){
        listItem.clear();
    }

    public static ArrayList<ClipboardListViewItem> getClip(){
        return listItem;
    }

    public static HashMap<Action, ClipboardListViewItem> getHashClip() { return clipListItem; }

}
