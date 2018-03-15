package com.example.administrator.study_jh.listview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.study_jh.R;

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

        final SonsationViewHolder viewHolder;

        if(convertView==null){
            convertView=inflater.inflate(layout,parent,false);
            viewHolder = new SonsationViewHolder();
            viewHolder.icon=(ImageView)convertView.findViewById(R.id.imageView1);
            viewHolder.name=(TextView)convertView.findViewById(R.id.textView1);
            viewHolder.flex =(TextView)convertView.findViewById(R.id.textView_flex);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (SonsationViewHolder) convertView.getTag();
        }

        viewHolder.icon.setImageDrawable(data.get(position).getIcon());
        viewHolder.name.setText(data.get(position).getName());
        viewHolder.flex.setText(data.get(position).getFlex());

        return convertView;
    }
}