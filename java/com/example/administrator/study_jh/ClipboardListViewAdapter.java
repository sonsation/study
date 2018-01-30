package com.example.administrator.study_jh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ClipboardListViewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<ClipboardListViewItem> data;
    private int layout;

    public ClipboardListViewAdapter(Context context, int layout, ArrayList<ClipboardListViewItem> data){
        this.inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data=data;
        this.layout=layout;
    }

    @Override
    public int getCount(){return data.size();}

    @Override
    public String getItem(int position){return data.get(position).getName();}

    @Override
    public long getItemId(int position){return position;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        if(convertView==null){
            convertView=inflater.inflate(layout,parent,false);
        }

        ClipboardListViewItem listviewitem=data.get(position);

        TextView name=(TextView)convertView.findViewById(R.id.textView1);
        name.setText(listviewitem.getName());

        TextView flex =(TextView)convertView.findViewById(R.id.textView_flex);
        flex.setText(listviewitem.getFlex());

        return convertView;
    }
}