package com.example.administrator.study_jh;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.String.valueOf;

public class FileListFragment extends AppCompatActivity {

    private boolean hideOption = true;
    private boolean isLongClick = false;
    private ArrayList<ListviewItem> dirName;
    private String rootPath = "";
    private String nextPath = "";
    private String prevPath = "";
    public String currentPath = "";
    private ListView fileListView;
    private String fName="";
    private ListviewAdapter adapter;
    public ClipboardListViewAdapter clipAdapter;
    public ArrayList<ClipboardListViewItem> clipListitem = new ArrayList<>();;
    public Menu mMenu;
    private DrawerLayout drawerLayout;
    private View drawerView;
    private TextView currentpath_view;
    public ListView clipListView = null ;
    public static final int SEND_INFORMATION = 0;
    public static final int SEND_STOP = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_filelist);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_name);
        getSupportActionBar().setTitle("FILE MANAGER");

        clipListView = (ListView) findViewById(R.id.file_clipboard);
        fileListView = (ListView)(findViewById(R.id.filelistview));
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        currentpath_view = (TextView) findViewById(R.id.currentpath_view);
        drawerView = (View)findViewById(R.id.drawer);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        currentpath_view.setText(currentPath.toString());

        rootPath = getIntent().getStringExtra("path");

        if(savedInstanceState != null){
            currentPath = savedInstanceState.getString("path");
        } else {
            currentPath = rootPath;
        }

        getDir(currentPath);

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (isLongClick == false) {

                        String path = dirName.get(position).getName().toString();
                        nextPath = currentPath + File.separator + path;

                        int lastPostion = currentPath.lastIndexOf("/");
                        prevPath = currentPath.substring(0, lastPostion);

                        currentpath_view.setText(currentPath.toString());

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

        fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isLongClick = true;
                fileListView.requestFocusFromTouch();
                getDir(currentPath);
                adapter.notifyDataSetChanged();
                fileListView.setSelection(position);
                fileListView.setItemChecked(position, true);

                return true;
            }
        });

        Button file_clip = (Button)findViewById(R.id.file_clip);
        file_clip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drawerLayout.isDrawerOpen(drawerView)) {
                    drawerLayout.closeDrawer(drawerView);
                }
                else {
                    drawerLayout.openDrawer(drawerView);
                }
            }
        });

        Button clip_clear = (Button)findViewById(R.id.clip_clear);
        clip_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clipListitem.clear();
                drawerLayout.closeDrawer(drawerView);
            }
        });

        Button file_paste = (Button)findViewById(R.id.file_paste);

        file_paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startCopyThread();


                //clipListitem.clear();
                drawerLayout.closeDrawer(drawerView);
            }
        });


        clipListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = clipListitem.get(position).getName().toString();
                //int indexOfNmae = path.lastIndexOf(".");
                //String name = path.substring(indexOfNmae+1, path.length());
                //Toast.makeText(getApplicationContext(), "path = "+name , Toast.LENGTH_SHORT).show();

                //if(timer.result == true) {
                 //   Toast.makeText(getApplicationContext(), "경과시간 = "+ (end-start) , Toast.LENGTH_SHORT).show();
               // }



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
            case R.id.file_settings:
                String test = "123";
                //Toast.makeText(getApplicationContext(), "추출 값 = "+ (temp+1) , Toast.LENGTH_SHORT).show();

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
                int count = fileListView.getCount();

                for(int i = 0 ; i < count ; i++) {
                    if (fileListView.isItemChecked(i)){
                        String path = currentPath+File.separator+dirName.get(i).getName().toString();
                        remove(path);
                    }
                }

                return true;

            case R.id.file_copy:
                copyToClipboard();
                return true;

            case R.id.file_rename:
                //renameFile();
                return true;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
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

    private void getDir(String dirPath) {

        dirName = new ArrayList<>();
        SimpleDateFormat sf = new SimpleDateFormat("최종수정 : yyyy-MM-dd / HH:mm:ss");

        if(isLongClick == false) {
            adapter = new ListviewAdapter(this, R.layout.listview_item, dirName);
        } else {
            adapter = new ListviewAdapter(this, R.layout.listview_multi_item, dirName);
        }

        File files = new File(dirPath);
        File[] fileList = files.listFiles();

        dirName.clear();
        dirName.add(new ListviewItem(0 ,"..", "" ));

        if(isLongClick == true) {
            dirName.remove(0);
        }

        for(int i=0 ; i < fileList.length ; i++) {

            if(hideOption == true) {
                if (!fileList[i].getName().startsWith(".")) {
                    dirName.add(new ListviewItem(R.drawable.folder,fileList[i].getName(),sf.format(fileList[i].lastModified())));
                }
            }
            else {
                dirName.add(new ListviewItem(R.drawable.folder,fileList[i].getName(),sf.format(fileList[i].lastModified())));
            }
        }
        fileListView.setAdapter(adapter);
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

    private void copyToClipboard(){

        int count = fileListView.getCount();
        clipAdapter = new ClipboardListViewAdapter(this, R.layout.listview_multi_item, clipListitem);

        for(int i = 0 ; i < count ; i++) {
            if(fileListView.isItemChecked(i)) {
                String test = currentPath + File.separator + dirName.get(i).getName().toString();
                clipListitem.add(new ClipboardListViewItem(test,dirName.get(i).getName().toString(),""));
            }
        }
        clipListView.setAdapter(clipAdapter);
    }



    public void remove(String path) {

        File file = new File(path);
        File[] tempFile = file.listFiles();

        if(tempFile.length >0){
            for (int i = 0; i < tempFile.length; i++)
            {
                if(tempFile[i].isFile()){
                    tempFile[i].delete();
                }
                else{
                    remove(tempFile[i].getPath());
                }
                tempFile[i].delete();
            }
            file.delete();
        }
        else {
            if(file.exists()) {
                file.delete();
            }
        }
    }

    public boolean copyFile(File beforeDir, String afterDir){
        boolean result;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        int data = -1;

        if(beforeDir != null && beforeDir.exists()){

            try {
                fis = new FileInputStream(beforeDir);
                bis = new BufferedInputStream(fis);
                fos = new FileOutputStream(afterDir);
                bos = new BufferedOutputStream(fos);

                while((data = bis.read())!= -1){
                    bos.write(data);
                }
                bos.flush();
                bos.close(); fos.close(); bis.close(); fis.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    class copyThread implements Runnable {

        int count = clipListView.getCount();
        long start, end;
        boolean result;

        public void run(){
            try {

                start = System.currentTimeMillis();

                if(count > 0) {
                    for (int i = 0; i < count; i++) {
                        String targetedDir = clipListitem.get(i).getPath().toString();
                        String targetName = clipListitem.get(i).getName().toString();
                        String tempTarget = null;
                        File file = new File(targetedDir);

                        if (targetedDir.equals(currentPath + File.separator + targetName)) {
                            int dotIndex = targetName.lastIndexOf(".");
                            String tempName = targetName.substring(0, dotIndex);
                            String tempExtension = targetName.substring(dotIndex, targetName.length());
                            tempTarget = tempName + "_copied" + tempExtension;  //original_copied
                        }

                        File tempFIle = new File(currentPath+File.separator+tempTarget); //original_copied
                        File tempPath = new File(currentPath+File.separator);
                        File[] fileList = tempPath.listFiles();

                        while(tempFIle.exists()) {
                            int dotIndex = tempTarget.lastIndexOf(".");
                            String tempName = tempTarget.substring(0, dotIndex);
                            String tempExtension = tempTarget.substring(dotIndex, tempTarget.length());
                            tempTarget = tempName + "(1)" + tempExtension; //original_copied(1)
                        }

                        copyFile(file, currentPath + File.separator + tempTarget);
                        result = false;
                    }
                }

                end = System.currentTimeMillis();

                // 메시지 얻어오기
                Message message = handler.obtainMessage();
                // 메시지 ID 설정
                message.what = SEND_INFORMATION;
                // 메시지 내용 설정(int)
                message.arg1 = (int)(end-start);
                // 메시지 내용 설정 (Object)
                String information = new String("소요시간");
                message.obj = information;

                // 메시지 전송
                handler.sendMessage(message);

                Thread.sleep(1000);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void startCopyThread() {
        copyThread runnable = new copyThread();
        Thread thread = new Thread(runnable);
        thread.start();
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case SEND_INFORMATION:
                    Toast.makeText(getApplicationContext(), Integer.toString(msg.arg1) + msg.obj, Toast.LENGTH_SHORT).show();
                    getDir(currentPath);
                    //textView.setText(Integer.toString(msg.arg1) + msg.obj);
                    break;
               // case SEND_STOP:
                //    //thread.stopThread();
                //    //textView.setText("Thread 가 중지됨.");
                    //break;
                default:
                    break;
            }
        }
    };
}