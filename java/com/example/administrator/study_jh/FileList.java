package com.example.administrator.study_jh;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.study_jh.listview.ClipboardListViewAdapter;
import com.example.administrator.study_jh.listview.ClipboardListViewItem;
import com.example.administrator.study_jh.listview.ListviewAdapter;
import com.example.administrator.study_jh.listview.ListviewItem;
import com.example.administrator.study_jh.util.ClipboardHandler;
import com.example.administrator.study_jh.util.FileCopy;
import com.example.administrator.study_jh.util.FilesUtil;
import com.example.administrator.study_jh.util.ManagementCache;
import com.example.administrator.study_jh.util.Settings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
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

    public int kinds = 0;
    public int system = 0;

    public FileList(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(savedInstanceState != null){
            currentPath = savedInstanceState.getString("path");
        } else {
            currentPath = rootPath;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_filelist, container, false);
        mCustomView = LayoutInflater.from(getActivity()).inflate(R.layout.clip_toolbar, null);

        ab = ((MainActivity)getActivity()).getSupportActionBar();
        ab.setDisplayShowCustomEnabled(false);
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle("FILE MANAGER");

        fileListView = (ListView)view.findViewById(R.id.filelistview);
        clipListView = (ListView)view.findViewById(R.id.file_clipboard);
        getItemCount = (TextView)mCustomView.findViewById(R.id.getitemcount);
        displayPath = (TextView)view.findViewById(R.id.displaypath);
        drawerLayout = (DrawerLayout)view.findViewById(R.id.drawer_layout);
        drawerView = view.findViewById(R.id.drawer);
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

                                Uri temp = FileProvider.getUriForFile(getActivity(), getContext().getPackageName()+ ".fileprovider" , isFile);
                                Intent intent = new Intent(Intent.ACTION_VIEW, temp);
                                intent.setDataAndType(temp , "image/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                PackageManager packageManager = getActivity().getPackageManager();
                                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                                boolean isIntentSafe = activities.size() > 0;

                                if (isIntentSafe) {
                                    startActivity(intent);
                                }

                            }
                            else if (new FilesUtil().getFileMimeType(isFile).startsWith("audio/")) {

                                Uri temp = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName()+ ".fileprovider" , isFile);
                                Intent intent = new Intent(Intent.ACTION_VIEW, temp);
                                intent.setDataAndType(temp, "audio/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                PackageManager packageManager = getActivity().getPackageManager();
                                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                                boolean isIntentSafe = activities.size() > 0;

                                if (isIntentSafe) {
                                    startActivity(intent);
                                }

                            }
                            else if (new FilesUtil().getFileMimeType(isFile).startsWith("application/vnd.android.package-archive")) {

                                Intent packageinstaller = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                packageinstaller.setDataAndType(
                                        FileProvider.getUriForFile(getActivity(), getContext().getPackageName() + ".fileprovider" , isFile)
                                        , "application/vnd.android.package-archive");
                                packageinstaller.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(packageinstaller, "Open"));

                            }

                            else if (new FilesUtil().getFileMimeType(isFile).startsWith("application/pdf")) {

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(
                                        FileProvider.getUriForFile(getActivity(), getContext().getPackageName() + ".fileprovider" , isFile)
                                        , "application/pdf");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(intent, "Open"));
                            }
                            else {
                                Toast.makeText(getActivity(), "지원하지 않는 파일 형식입니다.", Toast.LENGTH_SHORT).show();
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

                int getSize = ClipboardHandler.getClip().size();

                for(int i = 0 ; i < getSize ; i++) {
                    File temp = new File(ClipboardHandler.getClip().get(i).getPath());
                    if(!temp.exists()){
                        ClipboardHandler.removeClip(i);
                    }
                }

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
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                drawerLayout.closeDrawer(drawerView);
            }
        });


        file_paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), FileCopy.class);
                intent.putExtra("path", currentPath);
                startActivity(intent);
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
                Intent intent = new Intent(getActivity(), Settings.class);
                startActivity(intent);
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

                                                            init();
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
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                copyToClipboard(1);
                break;

            case R.id.file_move:
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                copyToClipboard(2);
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

            case R.id.file_sorting:
                showSelectShow();

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

    public void getDir(String dirPath) {

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
            adapter = new ListviewAdapter(getActivity(), R.layout.listview_multi_item, dirName);
            dirName.remove(0);
        }

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
                    dirName.add(new ListviewItem(new FilesUtil().getFileIcon(tempList.get(i), getActivity()), tempList.get(i).getName(), ""));
                }
                else {

                    dirName.add(new ListviewItem(null,tempList.get(i).getName(),""));
                }
            }

            adapter.notifyDataSetChanged();

            thread.start();
        }

        fileListView.setAdapter(adapter);
    }

    class getIconThread implements Runnable {

        SimpleDateFormat sf = new SimpleDateFormat("MM월 dd일 HH:mm");

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

                    ManagementCache cacheHandler = new ManagementCache();

                    if(isLongClick == false) {
                        for (int i = 0; i < tempList.size(); i++) {

                            String temp = null;

                            if(tempList.get(i).isFile()) {
                                if (tempList.get(i).length() / 1024 / 1024 > 1024) {
                                    temp = sf.format(tempList.get(i).lastModified()) + " / " + tempList.get(i).length() / 1024 + "GB";
                                } else if ((tempList.get(i).length() / 1024 / 1024 <= 1024) && (tempList.get(i).length() / 1024 / 1024 >= 1)) {
                                    temp = sf.format(tempList.get(i).lastModified()) + " / " + tempList.get(i).length() / 1024 / 1024 + "MB";
                                } else if (tempList.get(i).length() / 1024 / 1024 < 1) {
                                    double temp1 = (double)tempList.get(i).length() / (double)1024;
                                    String size = String.format("%.2f", temp1);
                                    temp = sf.format(tempList.get(i).lastModified()) + " / " + size + "KB";
                                }
                            } else {
                                temp = sf.format(tempList.get(i).lastModified());
                            }

                            if(cacheHandler.existCache(tempList.get(i).getName())) {
                                dirName.set(i + 1, new ListviewItem(cacheHandler.getCacheFile(getActivity(), tempList.get(i).getName()), tempList.get(i).getName(), temp));
                            }
                            else {
                                dirName.set(i + 1, new ListviewItem(new FilesUtil().getFileIcon(tempList.get(i), getActivity()), tempList.get(i).getName(), temp));
                            }
                            listviewNotify();
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                if(Thread.currentThread().isAlive())
                    Thread.currentThread().interrupt();

            } catch (Exception e) {
                e.printStackTrace();
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
            ArrayList<String> getCheckedItem = getCheckedItem();

            try {

                if(!Thread.currentThread().isInterrupted()) {

                    mProgressDialog.setMax(getCheckedItemCount(fileListView));

                    for (int i = 0; i < getCheckedItem.size() ; i++) {
                            remove(getCheckedItem.get(i));
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
        }
    }

    private void copyToClipboard(int action){

        ArrayList<String> getCheckedItem = getCheckedItem();
        clipAdapter = new ClipboardListViewAdapter(getActivity(), R.layout.file_clipitem, ClipboardHandler.getClip());

        if(getCheckedItemCount(fileListView) == 0) {
            Toast.makeText(getActivity(), "선택된 폴더나 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        for(int i = 0 ; i < getCheckedItem.size() ; i++) {
            for(int j = 0 ; j < ClipboardHandler.getClip().size() ; j++){
                if(getCheckedItem.get(i).equals(ClipboardHandler.getClip().get(j).getPath())){
                    ClipboardHandler.removeClip(j);
                }
                else if(getCheckedItem.get(i).startsWith(ClipboardHandler.getClip().get(j).getPath())){
                    Toast.makeText(getActivity(), "해당 폴더의 상위 폴더가 클립보드에 이미 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        for(int i = 0 ; i < getCheckedItem.size() ; i++) {

            int index = getCheckedItem.get(i).toString().lastIndexOf("/");
            String getCheckedItemName = getCheckedItem.get(i).toString().substring(index+1);
            ClipboardHandler.setClip(action, new ClipboardListViewItem(getCheckedItem.get(i), getCheckedItemName));

        }

        file_clip.setVisibility(View.VISIBLE);
        clipListView.setAdapter(clipAdapter);

        Toast.makeText(getActivity(), "클립보드에 복사가 완료되었습니다.", Toast.LENGTH_SHORT).show();

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

    public ArrayList<String> getCheckedItem() {

        int count = fileListView.getCount();
        ArrayList<String> checkedItemPath = new ArrayList<>();

        for(int i = 0 ; i < count ; i++) {
            if(fileListView.isItemChecked(i)){
                String path = currentPath + File.separator + dirName.get(i).getName().toString();
                checkedItemPath.add(path);
            }
        }

        return checkedItemPath;
    }

    Comparator<ListviewItem> cmpAsc = new Comparator<ListviewItem>() {
        @Override
        public int compare(ListviewItem o1, ListviewItem o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    } ;

    Comparator<ListviewItem> cmpDsc = new Comparator<ListviewItem>() {
        @Override
        public int compare(ListviewItem o1, ListviewItem o2) {
            return o2.getName().compareToIgnoreCase(o1.getName());
        }
    } ;

    void showSelectShow(){

        LayoutInflater inflater=getLayoutInflater(null);
        final View dialogView= inflater.inflate(R.layout.selectsort, null);

        RadioGroup selectKinds = (RadioGroup) dialogView.findViewById(R.id.radioGroup1);
        RadioGroup selectSystem = (RadioGroup) dialogView.findViewById(R.id.radioGroup2);

        android.app.AlertDialog.Builder buider= new android.app.AlertDialog.Builder(getActivity());
        buider.setView(dialogView);

        selectKinds.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.sortname:
                        kinds = 1;
                        break;
                    case R.id.sortsize:
                        kinds = 2;
                        break;
                    case R.id.sortmodified:
                        kinds = 3;
                        break;
                }
            }
        });

        selectSystem.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.setasc:
                        system = 1;
                        break;
                    case R.id.setdsc:
                        system = 2;
                        break;
                }
            }
        });

        buider.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                kinds = 0;
                system = 0;
                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog=buider.create();
        dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정
        dialog.show();
    }

}
