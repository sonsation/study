package com.example.administrator.study_jh.listview;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.administrator.study_jh.R;

import java.util.ArrayList;
import java.util.List;

public class ListviewAdapter extends RecyclerView.Adapter<ListviewAdapter.ViewHolder> {

    private List<ListviewItem> fileList;
    private LayoutInflater inflater;
    private int layout;

    public ListviewAdapter(Context context, int layout, ArrayList<ListviewItem> fileList) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.fileList = fileList;
        this.layout = layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(
                parent.getContext()).inflate(R.layout.listview_item, parent, false
        );

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,  int position) {

        final ListviewItem item = fileList.get(position);
        holder.icon.setImageDrawable(fileList.get(position).getIcon());
        holder.name.setText(fileList.get(position).getName());
        holder.flex.setText(fileList.get(position).getFlex());

        holder.itemView.setBackgroundColor(item.isBoolean() ? Color.GRAY : Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public int getCount(){return fileList.size();}

    public int getCheckedItemCount(){
        int count = 0;

        for(int i = 0 ; i < fileList.size() ; i++){
            if(fileList.get(i).isBoolean()){
                count++;
            }
        }
        return count;
    }

    public ArrayList<String> getCheckedItem(){

        ArrayList<String> list = new ArrayList<>();

        for(int i = 0 ; i < fileList.size() ; i++){
            if(fileList.get(i).isBoolean()){
                list.add(fileList.get(i).getPath());
            }
        }

        return list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public ImageView icon;
        public TextView name;
        public TextView flex;
        public RadioButton radio;

        ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.imageView1);
            name = itemView.findViewById(R.id.textView1);
            flex = itemView.findViewById(R.id.textView_flex);
            itemView.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            menu.setHeaderTitle("Select The Action");
            menu.add(0, 1, 0, "Select");
            menu.add(0, 2, 0, "Copy");
            menu.add(0, 3, 0, "Cut");
            menu.add(0, 4, 0, "Delete");
            menu.add(0, 5, 0, "Rename");
            menu.add(0, 6, 0, "Detail");
        }
    }

}