package com.example.administrator.study_jh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class FileListFragment extends AppCompatActivity {

    Parcelable state;

    private boolean hideOption = true;
    private boolean isLongClick = false;
    private ArrayList<String> dirName;
    private ArrayAdapter<String> listAdapter;
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String nextPath = "";
    private String prevPath = "";
    private String currentPath = "";
    private ListView ListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.fragment_filelist);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_name);
        getSupportActionBar().setTitle("File Manager");

        ListView = (ListView)(findViewById(R.id.filelistview));

        if(savedInstanceState != null){
            currentPath = savedInstanceState.getString("path");
            ListView.onRestoreInstanceState(state);
            Toast.makeText(getApplicationContext(), "welcome back", Toast.LENGTH_SHORT).show();
        } else {
            currentPath = rootPath;
            Toast.makeText(getApplicationContext(), "welcome", Toast.LENGTH_SHORT).show();
        }

        getDir(currentPath);

            ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (isLongClick == false) {

                        String path = dirName.get(position).toString();
                        nextPath = currentPath + "/" + path;

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

                isLongClick = true;
                ListView.requestFocusFromTouch();
                getDir(currentPath);
                listAdapter.notifyDataSetChanged();
                ListView.setSelection(position);
                ListView.setItemChecked(position, true);


                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.filelist_menu_header, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        CheckBox cb = (CheckBox)findViewById(R.id.hide_option);

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.layout__rightin, R.anim.layout__rightout);
                return true;
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(getApplicationContext(), "환경설정 버튼 클릭됨", Toast.LENGTH_LONG).show();
                return true;

            case R.id.hide_option:

                if((cb.isChecked())){
                    cb.setChecked(false);
                    //hideOption = false;
                }
                else {
                    cb.setChecked(true);
                    //hideOption = true;
                }
                //getDir(currentPath);

                return false;
            default:

        }
        return super.onOptionsItemSelected(item);
    }


    private void getDir(String dirPath) {

        dirName = new ArrayList<>();

        if(isLongClick == false) {
            listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dirName);
        }
        else {
            listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, dirName);
        }

        File files = new File(dirPath);
        File[] fileList = files.listFiles();

        dirName.clear();
        dirName.add("..");

        for(int i=0 ; i < fileList.length ; i++) {

            if(hideOption == true) {
                if (!fileList[i].getName().startsWith(".")) {
                    dirName.add(fileList[i].getName());
                }
            }
            else {
                dirName.add(fileList[i].getName());
            }
        }

        if(isLongClick == true) {
            dirName.remove(0);
        }

        ListView.setAdapter(listAdapter);
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

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        outState.putString("path", currentPath);
        state = ListView.onSaveInstanceState();

        // etc.

        super.onSaveInstanceState(outState);
    }
}