package com.example.administrator.study_jh.handler;

import com.example.administrator.study_jh.listview.TabItem;

import java.util.ArrayList;

public class TabMenuHandler {

    private static ArrayList<TabItem> listItem = new ArrayList<>();

    public TabMenuHandler(){}

    public static void set(TabItem path){
        listItem.add(path);
    }

    public static void clear(){
        listItem.clear();
    }

    public static ArrayList<TabItem> getTab(){
        return listItem;
    }
}
