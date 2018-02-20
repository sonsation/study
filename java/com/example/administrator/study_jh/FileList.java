package com.example.administrator.study_jh;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.study_jh.listview.ClipboardListViewAdapter;
import com.example.administrator.study_jh.listview.ClipboardListViewItem;
import com.example.administrator.study_jh.listview.ListviewAdapter;
import com.example.administrator.study_jh.listview.ListviewItem;
import com.example.administrator.study_jh.listview.TabItem;
import com.example.administrator.study_jh.listview.TabItemAdapter;
import com.example.administrator.study_jh.util.ClipboardHandler;
import com.example.administrator.study_jh.util.FilesUtil;
import com.example.administrator.study_jh.util.ImgView;
import com.example.administrator.study_jh.util.ManagementCache;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.example.administrator.study_jh.util.FilesUtil.copyDir;
import static com.example.administrator.study_jh.util.FilesUtil.copyFile;
import static com.example.administrator.study_jh.util.FilesUtil.getValidateFileName;
import static com.example.administrator.study_jh.util.FilesUtil.remove;


/**
 * Created by Administrator on 2018-02-20.
 */

public class FileList extends Fragment {

    private ArrayList<ListviewItem> dirName = new ArrayList<>();
    public ListView fileListView;
    public ListView clipListView;
    public ListviewAdapter adapter;
    public ClipboardListViewAdapter clipAdapter;

    private boolean hideOption = true;
    private boolean isLongClick = false;

    private int checked = 0;
    private int pageStack = 0;

    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String currentPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private Menu mMenu;
    private ActionBar ab;
    private TextView getItemCount;
    private View mCustomView;
    private TextView displayPath;
    private DrawerLayout drawerLayout;
    private View drawerView;
    private Button file_clip;
    private Button clip_clear;
    private Button file_paste;
    private ProgressDialog mProgressDialog;
    private View view;
    public ImageButton cancle;

    public FileList(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        //rootPath = getFragmentManager().getStringExtra("path");

        if(savedInstanceState != null){
            Log.i("0", "번들 있음");
            currentPath = savedInstanceState.getString("path");
        } else {
            Log.i("0", "번들 x");
            currentPath = rootPath;
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_filelist, container, false);
        mCustomView = LayoutInflater.from(getActivity()).inflate(R.layout.clip_toolbar, null);

        View test = LayoutInflater.from(getActivity()).inflate(R.layout.tab_listview, null);

        ab = ((MainActivity)getActivity()).getSupportActionBar();
        ab.setDisplayShowCustomEnabled(false);
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle("FILE MANAGER");

        fileListView = (ListView)view.findViewById(R.id.filelistview);
        clipListView = (ListView)view.findViewById(R.id.file_clipboard);
        getItemCount = (TextView)mCustomView.findViewById(R.id.getitemcount);
        displayPath = (TextView)view.findViewById(R.id.displaypath);
        drawerLayout = (DrawerLayout)view.findViewById(R.id.drawer_layout);
        drawerView = (View)view.findViewById(R.id.drawer);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        file_clip = (Button)view.findViewById(R.id.file_clip);
        clip_clear = (Button)view.findViewById(R.id.clip_clear);
        file_paste = (Button)view.findViewById(R.id.file_paste);
        cancle = (ImageButton)mCustomView.findViewById(R.id.cancle);

        if(!ClipboardHandler.getClip().isEmpty()) {
            file_clip.setVisibility(view.VISIBLE);
            clipAdapter = new ClipboardListViewAdapter(getActivity(), R.layout.file_clipitem, ClipboardHandler.getClip());
            clipListView.setAdapter(clipAdapter);
        }

        ListView fileListView1 = (ListView)test.findViewById(R.id.tab_listview);
        ClipboardListViewAdapter clipAdapter1 = new ClipboardListViewAdapter(getActivity(), R.layout.tab_listview, ClipboardHandler.getClip());
        //tabMenu.add(new TabItem(null,"test"));
        fileListView1.setAdapter(clipAdapter1);

        return view;
    }

    @Override
    public void onStart() {

        displayPath.setText(currentPath);
        getDir(currentPath);

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (isLongClick == false) {

                    String path = dirName.get(position).getName().toString();
                    String nextPath = currentPath + File.separator + path;

                    int lastPostion = currentPath.lastIndexOf("/");
                    String prevPath = currentPath.substring(0, lastPostion);

                    if (path.equals("..")) {

                        if (prevPath.length() < rootPath.length()) {
                            Toast.makeText(getActivity(), "최상위 폴더 입니다.", Toast.LENGTH_SHORT).show();
                        } else {

                            if(pageStack > 0) {
                                pageStack--;
                            }
                            currentPath = prevPath;
                            getDir(currentPath);
                        }
                    } else {
                        File isFile = new File(nextPath);

                        if(isFile.isDirectory()) {
                            pageStack++;
                            currentPath = nextPath;
                            getDir(currentPath);

                        }
                        else {
                            if (new FilesUtil().getFileMimeType(isFile).startsWith("image/")) {
                                Intent intent = new Intent(getActivity(), ImgView.class);
                                intent.putExtra("imgPath", isFile.getPath());
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(getActivity(), "Not yet", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    displayPath.setText(currentPath);

                } else {
                    checked = getCheckedItemCount(fileListView);
                    getItemCount.setText(String.valueOf(checked));
                }
            }
        });

        fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                ab.setDisplayShowTitleEnabled(false);
                ab.setCustomView(mCustomView);
                ab.setDisplayHomeAsUpEnabled(false);
                ab.setDisplayShowCustomEnabled(true);

                mMenu.setGroupVisible(R.id.longfalse, false);
                mMenu.setGroupVisible(R.id.longtrue, true);
                mMenu.setGroupVisible(R.id.under_two, true);

                isLongClick = true;
                fileListView.requestFocusFromTouch();
                getDir(currentPath);
                fileListView.setSelection(position-1);
                fileListView.setItemChecked(position-1, true);

                checked = getCheckedItemCount(fileListView);
                getItemCount.setText(String.valueOf(checked));

                return true;
            }
        });

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });


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


        clip_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                file_clip.setVisibility(View.INVISIBLE);
                ClipboardHandler.setclear();
                drawerLayout.closeDrawer(drawerView);
            }
        });


        file_paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final FileList.copyThread runnable = new FileList.copyThread();
                final Thread copyThread = new Thread(runnable);

                mProgressDialog = new ProgressDialog(getActivity());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(copyThread.isAlive()==false) {
                            copyThread.start();
                        }

                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setMessage("복사중..");
                        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                        copyThread.interrupt();

                                        Toast.makeText(getActivity(),
                                                "복사가 취소 되었습니다.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                        mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "숨기기",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                        Toast.makeText(getActivity(),
                                                "Hide clicked",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                        mProgressDialog.show();// show dialog

                        if(!mProgressDialog.isShowing()){
                            mProgressDialog.show();
                        }
                    }
                });
                drawerLayout.closeDrawer(drawerView);
            }
        });

        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.filelist_menu_header, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if(isLongClick == true) {
                    init();
                }
                break;

            case R.id.file_settings:

                break;

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

                break;

            case R.id.file_newfolder:
                createFolder();
                break;

            case R.id.file_remove:
                if(getCheckedItemCount(fileListView) == 0){
                    Toast.makeText(getActivity(), "파일이나 폴더를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }

                else {


                    new AlertDialog.Builder(getActivity())
                            .setMessage("항목 " + getCheckedItemCount(fileListView) + "개를 삭제합니다.")
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    final FileList.removeThread runnable = new FileList.removeThread();
                                    final Thread removeThread = new Thread(runnable);

                                    mProgressDialog = new ProgressDialog(getActivity());

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (removeThread.isAlive() == false) {
                                                removeThread.start();
                                            }

                                            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                            mProgressDialog.setMessage("삭제중..");
                                            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog,
                                                                            int which) {

                                                            removeThread.interrupt();

                                                            Toast.makeText(getActivity(),
                                                                    "삭제가 취소 되었습니다.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "숨기기",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog,
                                                                            int which) {

                                                            Toast.makeText(getActivity(),
                                                                    "Hide clicked",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                            mProgressDialog.show();// show dialog

                                            if (!mProgressDialog.isShowing()) {
                                                mProgressDialog.show();
                                            }
                                        }
                                    });

                                }
                            })

                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                }

                break;

            case R.id.file_copy:
                file_clip.setVisibility(View.VISIBLE);
                copyToClipboard();
                init();
                break;

            case R.id.file_rename:

                break;


            case R.id.file_allselect:

                int count = fileListView.getCount();

                if(count == getCheckedItemCount(fileListView)){
                    for(int i = 0 ; i < count ; i++){
                        fileListView.setItemChecked(i, false);
                    }
                } else {
                    for(int i = 0 ; i < count ; i++){
                        fileListView.setItemChecked(i, true);
                    }
                }

                checked = getCheckedItemCount(fileListView);
                getItemCount.setText(String.valueOf(checked));


                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if((checked > 1) && (isLongClick == true)) {
            mMenu.setGroupVisible(R.id.over_two, true);
            mMenu.setGroupVisible(R.id.under_two, false);

        }

        if((checked <= 1) && (isLongClick == true)) {
            mMenu.setGroupVisible(R.id.over_two, false);
            mMenu.setGroupVisible(R.id.under_two, true);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("path", currentPath);

        super.onSaveInstanceState(outState);
    }

    private void getDir(String dirPath) {

        FileList.getIconThread Runnable = new FileList.getIconThread();
        Thread thread = new Thread((Runnable));

        if(thread.isAlive()==true){
            listviewNotify();
            thread.interrupt();
        }

        if(isLongClick == false) {
            dirName.clear();
            dirName.add(0, new ListviewItem(null, "..", ""));
            adapter = new ListviewAdapter(getActivity(), R.layout.listview_item, dirName);

        } else {
            dirName.remove(0);
            adapter = new ListviewAdapter(getActivity(), R.layout.listview_multi_item, dirName);
        }

        SimpleDateFormat sf = new SimpleDateFormat("최종수정 : yyyy-MM-dd / HH:mm:ss");

        File files = new File(dirPath);
        File[] fileList = files.listFiles();
        List<File> tempList = new ArrayList<>();

        if(hideOption == true) {
            for(int i=0 ; i < fileList.length ; i++) {
                if (!fileList[i].getName().startsWith(".")) {
                    tempList.add(fileList[i]);
                }
            }
        } else {
            for(int i=0 ; i < fileList.length ; i++) {
                tempList.add(fileList[i]);
            }
        }

        if(isLongClick == false) {

            for(int i=0 ; i < tempList.size() ; i++) {
                if(tempList.get(i).isDirectory()) {
                    dirName.add(new ListviewItem(new FilesUtil().getFileIcon(tempList.get(i), getActivity()), tempList.get(i).getName(), sf.format(tempList.get(i).lastModified())));
                }
                else {
                    dirName.add(new ListviewItem(null,tempList.get(i).getName(),sf.format(tempList.get(i).lastModified())));
                }
            }
            thread.start();
        }

        fileListView.setAdapter(adapter);
    }

    class getIconThread implements Runnable {

        SimpleDateFormat sf = new SimpleDateFormat("최종수정 : yyyy-MM-dd / HH:mm:ss");

        public void run() {
            try {

                if(!Thread.currentThread().isInterrupted()) {

                    File files = new File(currentPath);
                    File[] fileList = files.listFiles();
                    List<File> tempList = new ArrayList<>();

                    if(hideOption == true) {
                        for(int i=0 ; i < fileList.length ; i++) {
                            if (!fileList[i].getName().startsWith(".")) {
                                tempList.add(fileList[i]);
                            }
                        }
                    } else {
                        for(int i=0 ; i < fileList.length ; i++) {
                            tempList.add(fileList[i]);
                        }
                    }

                    String getCacheDir = new ManagementCache().getCacheDIr();


                    if(isLongClick == false) {
                        for (int i = 0; i < tempList.size(); i++) {
                            String getCacheName = getCacheDir +File.separator+".cache_"+tempList.get(i).getName();
                            File temp = new File(getCacheName);

                            if(temp.exists()) {
                                dirName.set(i + 1, new ListviewItem(new ManagementCache().getCacheFile(getActivity(), tempList.get(i).getName()), tempList.get(i).getName(), sf.format(tempList.get(i).lastModified())));
                            }
                            else {
                                dirName.set(i + 1, new ListviewItem(new FilesUtil().getFileIcon(tempList.get(i), getActivity()), tempList.get(i).getName(), sf.format(tempList.get(i).lastModified())));
                            }
                            listviewNotify();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void listviewNotify(){
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    class removeThread implements Runnable {

        public void run() {
            int count = fileListView.getCount();

            try {

                if(!Thread.currentThread().isInterrupted()) {

                    mProgressDialog.setMax(getCheckedItemCount(fileListView));

                    for (int i = 0; i < count; i++) {
                        if (fileListView.isItemChecked(i)) {

                            String path = currentPath + File.separator + dirName.get(i).getName().toString();
                            mProgressDialog.setMessage(path);
                            mProgressDialog.setProgress(i);
                            remove(path);
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            mProgressDialog.dismiss();
                            Toast.makeText(getActivity(), "작업이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                            init();
                        }
                    });
                }

            } catch (Exception  e) {
                e.printStackTrace();
            }
            finally {
                Thread.currentThread().interrupt();
            }
        }
    }


    class copyThread implements Runnable {

        public void run(){

            try {

                int count = 0;

                if (!Thread.currentThread().isInterrupted()) {
                    for (int i = 0; i < clipListView.getCount(); i++) {
                        count = count + new FilesUtil().countFilesIn(new File(ClipboardHandler.getClip().get(i).getPath().toString()));
                    }

                    mProgressDialog.setMax(count);

                    for (int i = 0; i < clipListView.getCount(); i++) {

                        String targetedDir = ClipboardHandler.getClip().get(i).getPath().toString(); // Path
                        String targetName = ClipboardHandler.getClip().get(i).getName().toString();

                        File file = new File(targetedDir);
                        targetName = getValidateFileName(file, targetName, targetedDir, currentPath);

                        File toFile = new File(currentPath + File.separator + targetName);

                        if (file.isFile()) {
                            copyFile(file, toFile);
                        }

                        if (file.isDirectory()) {
                            copyDir(file, toFile);
                        }
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            mProgressDialog.dismiss();
                            Toast.makeText(getActivity(), "작업이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                            init();
                        }
                    });
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                Thread.currentThread().interrupt();
            }
        }
    }


    private void copyToClipboard(){

        int count = fileListView.getCount();
        int index = clipListView.getCount();
        boolean result = true;
        clipAdapter = new ClipboardListViewAdapter(getActivity(), R.layout.file_clipitem, ClipboardHandler.getClip());

        for(int i = 0 ; i < count ; i++) {
            if(fileListView.isItemChecked(i)) {
                String filePath = currentPath + File.separator + dirName.get(i).getName().toString();

                for(int j = 0 ; j < index ; j++) {

                    if(ClipboardHandler.getClip().get(j).getPath().toString().equals(filePath)) {
                        Toast.makeText(getActivity(), "해당 파일이나 폴더가 클립보드에 이미 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                        result = false;
                        break;
                    }
                    else if((new File(filePath).isDirectory()) && (ClipboardHandler.getClip().get(j).getPath().toString().startsWith(filePath))) {
                        Toast.makeText(getActivity(), "해당 폴더의 하위 폴더가 클립보드에 이미 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                        result = false;
                        break;
                    }

                }

                if(result == true) {
                    ClipboardHandler.setClip(new ClipboardListViewItem(filePath, dirName.get(i).getName().toString()));
                }
            }
        }
        clipListView.setAdapter(clipAdapter);
    }


    public void createFolder(){

        Context mContext = getActivity();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.cdialog,(ViewGroup)view.findViewById(R.id.layout_root));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
                File file = new File(currentPath+ File.separator+cName.getText().toString());

                if(cName.getText().toString().isEmpty()){
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

    public int getCheckedItemCount(ListView list) {
        int count = 0;
        int getItemCount = list.getCount();

        for(int i = 0 ; i < getItemCount ; i ++){
            if(list.isItemChecked(i)){
                count ++;
            }
        }

        return count;
    }

    public void init(){
        isLongClick = false;
        ab.setDisplayShowCustomEnabled(false);
        ab.setDisplayShowTitleEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        mMenu.setGroupVisible(R.id.longfalse, true);
        mMenu.setGroupVisible(R.id.longtrue, false);
        mMenu.setGroupVisible(R.id.under_two, false);
        mMenu.setGroupVisible(R.id.over_two, false);
        getDir(currentPath);
    }

}
