package com.example.administrator.study_jh;

public class ListviewItem {
    private int icon;
    private String name;

    public int getIcon(){return icon;}
    public String getName(){return name;}

    public ListviewItem(int icon,String name){
        this.icon=icon;
        this.name=name;
    }
}