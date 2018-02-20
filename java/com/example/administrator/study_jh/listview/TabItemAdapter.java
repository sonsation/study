package com.example.administrator.study_jh.listview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.administrator.study_jh.R;

import java.util.ArrayList;

public class TabItemAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<TabItem> data;
    private int layout;

    public TabItemAdapter(Context context, int layout, ArrayList<TabItem> data){
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
    public View getView(final int position, View convertView, ViewGroup parent){

        if(convertView==null){
            convertView=inflater.inflate(layout,parent,false);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        final TabItem listviewitem=data.get(position);

        /*
        ImageButton button1 = (ImageButton) convertView.findViewById(R.id.cancle);
        button1.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                data.remove(data.get(position));
                notifyDataSetChanged();
            }
        });
        */

        TextView name=(TextView)convertView.findViewById(R.id.textView1);
        name.setText(listviewitem.getName());

        return convertView;
    }
}