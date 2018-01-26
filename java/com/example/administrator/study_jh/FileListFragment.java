package com.example.administrator.study_jh;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileListFragment extends AppCompatActivity {

    private boolean hideOption = true;
    private boolean isLongClick = false;
    private ArrayList<ListviewItem> dirName;
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String nextPath = "";
    private String prevPath = "";
    private String currentPath = "";
    private ListView ListView;
    private String fName="";
    private ListviewAdapter adapter;
    Menu mMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_filelist);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_name);
        getSupportActionBar().setTitle("");

        ListView = (ListView)(findViewById(R.id.filelistview));

        if(savedInstanceState != null){
            currentPath = savedInstanceState.getString("path");
        } else {
            currentPath = rootPath;
        }

        getDir(currentPath);

            ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (isLongClick == false) {

                        getSupportActionBar().setTitle(currentPath.toString());

                        String path = dirName.get(position).getName().toString();
                        nextPath = currentPath + File.separator + path;

                        int lastPostion = currentPath.lastIndexOf("/");
                        prevPath = currentPath.substring(0, lastPostion);

                        if (path.equals("..")) {

                            if (prevPath.length() < rootPath.length()) {
                                Toast.makeText(getApplicationContext(), "최상위 폴더 입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                getDir(prevPath);
                                currentPath = prevPath;
                            }
                        } else {
                            getDir(nextPath);
                            currentPath = nextPath;
                        }
                    }
                }
            });

        ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int count;
                count = adapter.getCount();

                for (int i=0; i<count; i++) {
                    ListView.setItemChecked(i, true);
                }

                isLongClick = true;
                ListView.requestFocusFromTouch();
                getDir(currentPath);
                Toast.makeText(getApplicationContext(), "최상위 폴더 입니다" + count, Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                ListView.setSelection(position);
                ListView.setItemChecked(position, true);

                return true;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        mMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.filelist_menu_header, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(isLongClick == false) {

            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setVisible(true);
            menu.getItem(4).setVisible(true);

            for(int i = 5 ; i < 12 ; i++) {
                menu.getItem(i).setVisible(false);
            }

        } else {

            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setVisible(false);
            menu.getItem(4).setVisible(false);

            for(int i = 5 ; i < 12 ; i++) {
                menu.getItem(i).setVisible(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);


        switch (item.getItemId()) {
            case android.R.id.home:
                if(isLongClick == true) {
                    isLongClick = false;
                    getDir(currentPath);
                }
                else {
                    this.finish();
                }
                return true;
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(getApplicationContext(), "환경설정 버튼 클릭됨", Toast.LENGTH_LONG).show();
                return true;

            case R.id.hide_option:

                MenuItem hide_option = mMenu.findItem(R.id.hide_option);

                if(hide_option.isChecked()==false) {
                    hide_option.setChecked(true);
                    hideOption = false;
                }
                else {
                    hide_option.setChecked(false);
                    hideOption = true;
                }

                getDir(currentPath);

                return true;

            case R.id.file_newfolder:
                createFolder();

                return true;

            case R.id.file_remove:
                remove();

                return true;

            default:

        }
        return super.onOptionsItemSelected(item);
    }

    private void getDir(String dirPath) {

        dirName = new ArrayList<>();

        if(isLongClick == false) {
            adapter = new ListviewAdapter(this, R.layout.listview_item, dirName);
        } else {
            adapter = new ListviewAdapter(this, R.layout.listview_multi_item, dirName);
        }

        File files = new File(dirPath);
        File[] fileList = files.listFiles();

        dirName.clear();
        dirName.add(new ListviewItem(0, ".."));

        if(isLongClick == true) {
            dirName.remove(0);
        }

        for(int i=0 ; i < fileList.length ; i++) {

            if(hideOption == true) {
                if (!fileList[i].getName().startsWith(".")) {
                    dirName.add(new ListviewItem(R.drawable.folder,fileList[i].getName()));
                }
            }
            else {
                dirName.add(new ListviewItem(R.drawable.folder,fileList[i].getName()));
            }
        }

        ListView.setAdapter(adapter);


    }

    @Override
    public void onBackPressed(){

        if(isLongClick == true) {
            isLongClick = false;
            getDir(currentPath);
        }
        else {
            super.onBackPressed();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("path", currentPath);

        super.onSaveInstanceState(outState);
    }

    public void createFolder(){

        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.cdialog,(ViewGroup) findViewById(R.id.layout_root));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText cName = (EditText)layout.findViewById(R.id.cText);
        final TextView confirm = (TextView)layout.findViewById(R.id.cText_confirm);

        builder.setView(layout);
        builder.setMessage("새 폴더 생성");
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fName = cName.getText().toString();
                File file = new File(currentPath+ File.separator+fName);

                if(fName.isEmpty()){
                    confirm.setText("이름을 입력해주세요.");
                    confirm.setTextColor(Color.parseColor("#FE0000"));
                }
                else {
                    if(file.exists()){
                        confirm.setText("사용 중인 이름입니다.");
                        confirm.setTextColor(Color.parseColor("#FE0000"));
                    }
                    else {
                        file.mkdirs();
                        dialog.dismiss();
                    }
                    getDir(currentPath);
                }
            }
        });
    }

    public void remove() {


        int count = ListView.getCount();

        for(int i = 0 ; i < count ; i++) {
            if(ListView.isItemChecked(i)){
                String test = dirName.get(i).getName().toString();

                File file = new File(currentPath+File.separator+test);

                if(file.exists()) {
                    file.delete();
                }
            }
            else {
               Toast.makeText(getApplicationContext(), "파일이나 폴더를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
        getDir(currentPath);
    }
}