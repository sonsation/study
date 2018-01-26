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

public class ListviewAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<ListviewItem> data;
    private int layout;

    public ListviewAdapter(Context context, int layout, ArrayList<ListviewItem> data){
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

        ListviewItem listviewitem=data.get(position);

        ImageView icon=(ImageView)convertView.findViewById(R.id.imageView1);
        icon.setImageResource(listviewitem.getIcon());

        TextView name=(TextView)convertView.findViewById(R.id.textView1);
        name.setText(listviewitem.getName());

        return convertView;
    }
}