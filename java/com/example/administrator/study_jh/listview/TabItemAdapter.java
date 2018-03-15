package com.example.administrator.study_jh.listview;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.study_jh.FileList;
import com.example.administrator.study_jh.R;
import com.example.administrator.study_jh.util.ImgView;

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
        }

        final TabItem listviewitem=data.get(position);

        ImageButton fragment_cancle = (ImageButton) convertView.findViewById(R.id.fragment_cancle);
        fragment_cancle.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                if(position == 0){
                    return ;
                } else {
                    data.remove(data.get(position));
                    notifyDataSetChanged();
                }
            }
        });

        ImageView fragment_icon= (ImageView)convertView.findViewById(R.id.fragment_icon);
        fragment_icon.setImageDrawable(listviewitem.getIcon());

        TextView fragment_name=(TextView)convertView.findViewById(R.id.fragment_textview);
       fragment_name.setText(listviewitem.getName());

        return convertView;
    }
}