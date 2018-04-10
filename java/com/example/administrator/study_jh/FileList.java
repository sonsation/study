package com.example.administrator.study_jh;

import android.app.Activity;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.study_jh.asynchronous.asynctasks.FileCopy;
import com.example.administrator.study_jh.asynchronous.asynctasks.FileRemove;
import com.example.administrator.study_jh.handler.FragmentCallback;
import com.example.administrator.study_jh.listview.ClipboardListViewAdapter;
import com.example.administrator.study_jh.listview.ClipboardListViewItem;
import com.example.administrator.study_jh.listview.ListviewAdapter;
import com.example.administrator.study_jh.listview.ListviewItem;
import com.example.administrator.study_jh.handler.ClipboardHandler;
import com.example.administrator.study_jh.util.FilesUtil;
import com.example.administrator.study_jh.util.ManagementCache;
import com.example.administrator.study_jh.util.UriPathUtil;
import com.example.administrator.study_jh.util.Settings;
import com.example.administrator.study_jh.util.ZipExtractor;
import com.example.administrator.study_jh.asynchronous.asynctasks.ZipUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Administrator on 2018-02-20.
 */

public class FileList extends Fragment implements FragmentCallback {

    private ArrayList<ListviewItem> dirName = new ArrayList<>();
    public ListView fileListView;
    public ListView clipListView;
    public ListviewAdapter adapter;
    public ClipboardListViewAdapter clipAdapter;

    private boolean hideOption = true;
    private boolean isLongClick = false;
    private int checked = 0;
    private int pageStack = 0;

    static final int PICK_REMOVE_REQUEST = 1;
    static final int PICK_COPY_REQUEST = 2;
    static final int PICK_ZIP_REQUEST = 3;

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
    private View view;
    public ImageButton cancle;

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

        fileListView =  (ListView)view.findViewById(R.id.filelistview);
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
        clipAdapter = new ClipboardListViewAdapter(getActivity(), R.layout.file_clipitem, ClipboardHandler.getClip());
        registerForContextMenu(fileListView);

        if(!ClipboardHandler.getClip().isEmpty()) {
            file_clip.setVisibility(view.VISIBLE);
            clipListView.setAdapter(clipAdapter);
        }

        return view;
    }

    @Override
    public void onBack(){
        if(pageStack > 0){
            goBack();
        } else {
            MainActivity activity = (MainActivity)getActivity();
            activity.setBackkeyListner(null);
            activity.onBackPressed();
        }
    }

    public void goBack(){

        if(pageStack > 0 ) {
            pageStack--;
        }

        int lastPostion = currentPath.lastIndexOf("/");
        String prevPath = currentPath.substring(0, lastPostion);

        if (prevPath.length() < rootPath.length()) {
            Toast.makeText(getActivity(), "최상위 폴더 입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPath = prevPath;
        getDir(currentPath);

        displayPath.setText(currentPath);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        ((MainActivity) activity).setBackkeyListner(this);
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

                    if (path.equals("..")) {
                        goBack();

                    } else {

                        MainActivity activity = (MainActivity)getActivity();
                        activity.setBackkeyListner(FileList.this);

                        File isFile = new File(nextPath);

                        if(isFile.isDirectory()) {
                            pageStack++;
                            currentPath = nextPath;
                            getDir(currentPath);

                        }
                        else {

                            if (new FilesUtil().getFileMimeType(isFile).startsWith("image/")) {

                                Uri temp = new UriPathUtil().getMediaUri(getContext(), isFile.toString());
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

                                Uri temp = new UriPathUtil().getMediaUri(getContext(), isFile.toString());
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

                            else if (new FilesUtil().getFileMimeType(isFile).startsWith("video/")) {

                                Uri temp = new UriPathUtil().getMediaUri(getContext(), isFile.toString());
                                Intent intent = new Intent(Intent.ACTION_VIEW, temp);
                                intent.setDataAndType(temp, "video/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                PackageManager packageManager = getActivity().getPackageManager();
                                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                                boolean isIntentSafe = activities.size() > 0;

                                if (isIntentSafe) {
                                    startActivity(intent);
                                }

                            }

                            else if (new FilesUtil().getFileMimeType(isFile).startsWith("text/")) {

                                Uri temp = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName()+ ".fileprovider" , isFile);
                                Intent intent = new Intent(Intent.ACTION_VIEW, temp);
                                intent.setDataAndType(temp, "text/*");
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
                                Uri temp = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName()+ ".fileprovider" , isFile);
                                packageinstaller.setDataAndType(temp
                                        , "application/vnd.android.package-archive");
                                packageinstaller.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                startActivity(packageinstaller);
                            }

                            else if (new FilesUtil().getFileMimeType(isFile).startsWith("application/pdf")) {

                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(
                                        FileProvider.getUriForFile(getActivity(), getContext().getPackageName() + ".fileprovider" , isFile)
                                        , "application/pdf");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(intent, "Open"));
                            }

                            else if (new FilesUtil().getFileMimeType(isFile).startsWith("application/zip")) {
                                Intent intent = new Intent(getActivity(), ZipExtractor.class);
                                intent.putExtra("path", isFile.toString());
                                startActivity(intent);
                            }

                            else {
                                Toast.makeText(getActivity(), "지원하지 않는 파일 형식입니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    displayPath.setText(currentPath);

                } else {
                    //checked = getCheckedItemCount(fileListView);
                    getItemCount.setText(String.valueOf(checked));
                }
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

                for(int i = getSize-1 ; i >= 0 ; i--) {

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

                init();
                Intent intent = new Intent(getActivity(), FileCopy.class);
                intent.putExtra("path", currentPath);
                startActivityForResult(intent,PICK_COPY_REQUEST);
                drawerLayout.closeDrawer(drawerView);
            }
        });

        super.onStart();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) menuInfo;
        int index= info.position;

        menu.setHeaderTitle("Choose '" + dirName.get(index).getName().toString()+"'");
        menu.add(0, 1, 0, "Select");
        menu.add(0, 2, 0, "Copy");
        menu.add(0, 3, 0, "Cut");
        menu.add(0, 4, 0, "Delete");
        menu.add(0, 5, 0, "Rename");
        menu.add(0, 6, 0, "Detail");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int index= info.position;

        switch(item.getItemId()) {
            case 1 : //Select
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
                //fileListView.setSelection(index-1);
                //fileListView.setItemChecked(index-1, true);

                //checked = getCheckedItemCount(fileListView);
                getItemCount.setText(String.valueOf(checked));

                return true;
            case 2 : //Copy
                copyToClipboard("COPY", currentPath + File.separator + dirName.get(index).getName().toString());
                return true;
            case 3 : //Cut
                copyToClipboard("CUT", currentPath + File.separator + dirName.get(index).getName().toString());
                return true;
            case 4 : //Delete
                new AlertDialog.Builder(getActivity())
                        .setMessage("항목 1개를 삭제합니다.")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent intent = new Intent(getActivity(), FileRemove.class);
                                ArrayList<String> temp = new ArrayList<>();
                                temp.add(currentPath + File.separator + dirName.get(index).getName().toString());
                                intent.putExtra("path", temp);
                                startActivityForResult(intent,PICK_REMOVE_REQUEST);
                            }
                        })

                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return true;

            case 5 : //Detail
                renameFile(currentPath + File.separator + dirName.get(index).getName().toString());
                return true;

        }

        return super.onContextItemSelected(item);
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

                /*
                if(getCheckedItemCount(fileListView) == 0){
                    Toast.makeText(getActivity(), "파일이나 폴더를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }


                else {

                    new AlertDialog.Builder(getActivity())
                            .setMessage("항목 " + getCheckedItemCount(fileListView) + "개를 삭제합니다.")
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    /*
                                    Intent intent = new Intent(getActivity(), FileRemove.class);
                                    intent.putExtra("path", getCheckedItem());
                                    init();
                                    startActivityForResult(intent,PICK_REMOVE_REQUEST);

                                }
                            })

                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();

                }
*/
                break;

            case R.id.file_copy:
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                copyToClipboard("COPY");
                break;

            case R.id.file_move:
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                copyToClipboard("CUT");
                break;


            case R.id.file_rename:
                //renameFile();
                break;


            case R.id.file_allselect:

                /*
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
                */
                break;

            case R.id.file_sorting:
                showSelectShow();
                break;

            case R.id.file_zip:
                compressionSetting();

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

        if(isLongClick == false) {
            dirName.clear();
            dirName.add(0, new ListviewItem(null, "..", ""));
            adapter = new ListviewAdapter(getActivity(), R.layout.listview_item, dirName);

        } else {
            adapter = new ListviewAdapter(getActivity(), R.layout.listview_multi_item, dirName);
            dirName.remove(0);
        }


        adapter.notifyDataSetChanged();

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
                                temp = sf.format(tempList.get(i).lastModified()) + " / " + FilesUtil.calFileSize(tempList.get(i).length());
                            } else {
                                temp = sf.format(tempList.get(i).lastModified());
                            }

                            if(cacheHandler.existCache(tempList.get(i).getName())) {
                                dirName.set(i+1, new ListviewItem(cacheHandler.getCacheFile(getActivity(), tempList.get(i).getName()), tempList.get(i).getName(), temp));
                            }
                            else {
                                dirName.set(i+1, new ListviewItem(new FilesUtil().getFileIcon(tempList.get(i), getActivity()), tempList.get(i).getName(), temp));
                            }
                            listviewNotify();
                        }
                    }
                
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

    public void copyToClipboard(String action){
        /*
        ArrayList<String> getCheckedItem = getCheckedItem();


        if(getCheckedItemCount(fileListView) == 0) {
            Toast.makeText(getActivity(), "선택된 폴더나 파일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        */

        if(action.equals("COPY")) {
            for(int i = 0 ; i < ClipboardHandler.getClip().size() ; i++){
                ClipboardHandler.getClip().get(i).getAction().equals("CUT");
                break;
            }
            ClipboardHandler.setclear();

        } else {
            for(int i = 0 ; i < ClipboardHandler.getClip().size() ; i++){
                ClipboardHandler.getClip().get(i).getAction().equals("CUT");
                break;
            }
            ClipboardHandler.setclear();
        }

        /*
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
            ClipboardHandler.setClip(new ClipboardListViewItem(null, action, getCheckedItem.get(i), getCheckedItemName));

        }
        */

        file_clip.setVisibility(View.VISIBLE);
        clipListView.setAdapter(clipAdapter);

        Toast.makeText(getActivity(), "클립보드에 복사가 완료되었습니다.", Toast.LENGTH_SHORT).show();

    }

    public void copyToClipboard(String action, String path){

        if(action.equals("COPY")) {
            for(int i = 0 ; i < ClipboardHandler.getClip().size() ; i++){
                ClipboardHandler.getClip().get(i).getAction().equals("CUT");
                break;
            }
            ClipboardHandler.setclear();
        } else {
            for(int i = 0 ; i < ClipboardHandler.getClip().size() ; i++){
                ClipboardHandler.getClip().get(i).getAction().equals("CUT");
                break;
            }
            ClipboardHandler.setclear();
        }

        int index = path.lastIndexOf("/");
        String getItemName = path.substring(index+1);

        for(int i = 0 ; i < ClipboardHandler.getClip().size() ; i++) {
            if(ClipboardHandler.getClip().get(i).getPath().equals(path)) {
                ClipboardHandler.removeClip(i);
            }
        }

        ClipboardHandler.setClip(new ClipboardListViewItem(null, action, path, getItemName));

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

    public void renameFile(final String filePath){

        Context mContext = getActivity();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.cdialog,(ViewGroup)view.findViewById(R.id.layout_root));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final EditText cName = (EditText)layout.findViewById(R.id.cText);
        final TextView confirm = (TextView)layout.findViewById(R.id.cText_confirm);

        int last_index = filePath.lastIndexOf("/");

        cName.setText(filePath.substring(last_index+1));
        cName.selectAll();

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

                String newFileName = currentPath+ File.separator+cName.getText().toString();
                File file = new File(newFileName);
                File fileName = new File(filePath);

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
                        fileName.renameTo(file);
                        getDir(currentPath);
                        dialog.dismiss();
                    }
                    getDir(currentPath);
                }
            }
        });
    }

    /*
    public int getCheckedItemCount(RecyclerView list) {
        int count = 0;
        //int getItemCount = list.getCount();

        for(int i = 0 ; i < getItemCount ; i ++){
            //if(list.isItemChecked(i)){
                count ++;
            }
        }
        return count;
    }
    */

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

    /*
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
    */

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

    public void showSelectShow(){

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
                        break;
                    case R.id.sortsize:
                        break;
                    case R.id.sortmodified:
                        break;
                }
            }
        });

        selectSystem.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.setasc:
                        break;
                    case R.id.setdsc:
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

                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog=buider.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == PICK_REMOVE_REQUEST) ||
                (requestCode == PICK_COPY_REQUEST) ||
                (requestCode == PICK_ZIP_REQUEST)) {

            if (resultCode == RESULT_OK) {
                 getDir(currentPath);

            } else if (resultCode == RESULT_CANCELED) {
                getDir(currentPath);
                //Toast.makeText(getActivity(), "작업이 취소 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void compressionSetting(){

        LayoutInflater inflater=getLayoutInflater(null);
        final View dialogView= inflater.inflate(R.layout.zipsettings, null);


        final EditText zipName = (EditText) dialogView.findViewById(R.id.zipName);
        Spinner selectLevel = (Spinner)dialogView.findViewById(R.id.compressionLevel);
        //Spinner selectType = (Spinner)dialogView.findViewById(R.id.compressionType);

        /*
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //Object number = parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        */


        android.app.AlertDialog.Builder buider= new android.app.AlertDialog.Builder(getActivity());
        buider.setView(dialogView);

        buider.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(getActivity(), ZipUtil.class);
                //intent.putExtra("zipList", getCheckedItem());
                intent.putExtra("currentPath", currentPath + File.separator + zipName.getText());
                intent.putExtra("compressionLevel", 9);
                init();
                startActivityForResult(intent, PICK_ZIP_REQUEST);

            }
        });

        buider.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog dialog=buider.create();
        dialog.setCanceledOnTouchOutside(false);//없어지지 않도록 설정
        dialog.show();

    }

    public String getCurrentPath(){ return currentPath; }



}
