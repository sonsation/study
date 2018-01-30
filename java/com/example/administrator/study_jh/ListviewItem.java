package com.example.administrator.study_jh;

public class ListviewItem {
    private int icon;
    private String name;
    private String flex;

    public int getIcon(){return icon;}
    public String getName(){return name;}
    public String getFlex(){return flex;}

    public ListviewItem(int icon,String name, String flex){
        this.icon=icon;
        this.name=name;
        this.flex=flex;
    }
}