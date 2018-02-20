package com.example.administrator.study_jh;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.study_jh.listview.ClipboardListViewAdapter;
import com.example.administrator.study_jh.listview.ClipboardListViewItem;
import com.example.administrator.study_jh.listview.ListviewAdapter;
import com.example.administrator.study_jh.listview.ListviewItem;
import com.example.administrator.study_jh.util.FilesUtil;
import com.example.administrator.study_jh.util.ImgView;
import com.example.administrator.study_jh.util.ManagementCache;

import static com.example.administrator.study_jh.util.FilesUtil.copyDir;
import static com.example.administrator.study_jh.util.FilesUtil.copyFile;
import static com.example.administrator.study_jh.util.FilesUtil.getValidateFileName;
import static com.example.administrator.study_jh.util.FilesUtil.remove;

public class FileListFragment extends AppCompatActivity {

    private boolean hideOption = true;
    private boolean isLongClick = false;
    private String rootPath = "";
    public String currentPath = "";
    public int checked = 0;
    public int pageStack = 0;
    public long pressedTime = 0;
    private ArrayList<ListviewItem> dirName = new ArrayList<>();
    public ArrayList<ClipboardListViewItem> clipListitem = new ArrayList<>();
    private ListView fileListView;
    public ListView clipListView;
    private ListviewAdapter adapter;
    public ClipboardListViewAdapter clipAdapter;
    public Menu mMenu;
    private DrawerLayout drawerLayout;
    private View drawerView;
    public ProgressDialog mProgressDialog;
    public ActionBar ab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.fragment_filelist);
        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowCustomEnabled(false);
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle("FILE MANAGER");

        clipListView = (ListView)findViewById(R.id.file_clipboard);
        fileListView = (ListView)findViewById(R.id.filelistview);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = (View)findViewById(R.id.drawer);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        final TextView displayPath = (TextView)findViewById(R.id.displaypath);

        //FloatingActionButton mFloatingButton = (FloatingActionButton) findViewById(R.id.fab);
        //mFloatingButton.setAlw(fileListView);


        rootPath = getIntent().getStringExtra("path");

        if(savedInstanceState != null){
            currentPath = savedInstanceState.getString("path");
        } else {
            currentPath = rootPath;
        }

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
                                Toast.makeText(getApplicationContext(), "최상위 폴더 입니다.", Toast.LENGTH_SHORT).show();
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
                                    Intent intent = new Intent(getApplication(), ImgView.class);
                                    intent.putExtra("imgPath", isFile.getPath());
                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Not yet", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        displayPath.setText(currentPath);

                    } else {
                        TextView getitemcount = (TextView)findViewById(R.id.getitemcount);
                        checked = getCheckedItemCount(fileListView);
                        getitemcount.setText(String.valueOf(checked));
                    }
                }
            });

        fileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                ab.setDisplayShowTitleEnabled(false);
                ab.setDisplayShowCustomEnabled(true);
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setHomeAsUpIndicator(R.drawable.cancle);

                View mCustomView = LayoutInflater.from(getApplication()).inflate(R.layout.clip_toolbar, null);
                ab.setCustomView(mCustomView);
                mMenu.setGroupVisible(R.id.longfalse, false);
                mMenu.setGroupVisible(R.id.longtrue, true);
                mMenu.setGroupVisible(R.id.under_two, true);

                isLongClick = true;
                fileListView.requestFocusFromTouch();
                getDir(currentPath);
                fileListView.setSelection(position-1);
                fileListView.setItemChecked(position-1, true);

                TextView getitemcount = (TextView)findViewById(R.id.getitemcount);
                checked = getCheckedItemCount(fileListView);
                getitemcount.setText(String.valueOf(checked));

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
                Button visible = (Button)findViewById(R.id.file_clip);
                visible.setVisibility(View.INVISIBLE);
                clipListitem.clear();
                drawerLayout.closeDrawer(drawerView);
            }
        });

        Button file_paste = (Button)findViewById(R.id.file_paste);

        file_paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final copyThread runnable = new copyThread();
                final Thread copyThread = new Thread(runnable);

                mProgressDialog = new ProgressDialog(
                        FileListFragment.this);

                runOnUiThread(new Runnable() {
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

                                        Toast.makeText(getBaseContext(),
                                                "복사가 취소 되었습니다.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                        mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "숨기기",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                        Toast.makeText(getBaseContext(),
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        mMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.filelist_menu_header, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if((checked > 1) && (isLongClick == true)) {
            mMenu.setGroupVisible(R.id.over_two, true);
            mMenu.setGroupVisible(R.id.under_two, false);

        }

        if((checked <= 1) && (isLongClick == true)) {
            mMenu.setGroupVisible(R.id.over_two, false);
            mMenu.setGroupVisible(R.id.under_two, true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:

                if(isLongClick == true) {
                    init();
                }
                else {
                    this.finish();
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
                    Toast.makeText(getApplicationContext(), "파일이나 폴더를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }

                else {


                    new AlertDialog.Builder(this)
                            .setMessage("항목 " + getCheckedItemCount(fileListView) + "개를 삭제합니다.")
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    final removeThread runnable = new removeThread();
                                    final Thread removeThread = new Thread(runnable);

                                    mProgressDialog = new ProgressDialog(
                                            FileListFragment.this);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if(removeThread.isAlive()==false){
                                                removeThread.start();
                                            }

                                            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                            mProgressDialog.setMessage("삭제중..");
                                            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "취소",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog,
                                                                            int which) {

                                                            removeThread.interrupt();

                                                            Toast.makeText(getBaseContext(),
                                                                    "삭제가 취소 되었습니다.",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "숨기기",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog,
                                                                            int which) {

                                                            Toast.makeText(getBaseContext(),
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
                Button visible = (Button)findViewById(R.id.file_clip);
                visible.setVisibility(View.VISIBLE);
                copyToClipboard();
                init();
                break;

            case R.id.file_rename:

                for (int i = 0; i < fileListView.getCount(); i++) {
                    if (fileListView.isItemChecked(i)) {
                        renameDir(dirName.get(i).getName().toString());
                    }
                }
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

                TextView getitemcount = (TextView)findViewById(R.id.getitemcount);
                checked = getCheckedItemCount(fileListView);
                getitemcount.setText(String.valueOf(checked));

                break;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){

        if(isLongClick == true) {
            init();
        }
        else if((pageStack > 0) && (isLongClick == false)){

            pageStack--;

            int lastPostion = currentPath.lastIndexOf("/");
            String prevPath = currentPath.substring(0, lastPostion);
            currentPath = prevPath;

            getDir(currentPath);
        }
        else {
            if ( pressedTime == 0 ) {
                Toast.makeText(getApplication(), " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
                pressedTime = System.currentTimeMillis();
            }
            else {
                int seconds = (int) (System.currentTimeMillis() - pressedTime);

                if ( seconds > 2000 ) {
                    Toast.makeText(getApplication(), " 한 번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
                    pressedTime = 0 ;
                }
                else {
                    super.onBackPressed();
                }
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("path", currentPath);

        super.onSaveInstanceState(outState);
    }

    private void getDir(String dirPath) {

        getIconThread Runnable = new getIconThread();
        Thread thread = new Thread((Runnable));

        if(thread.isAlive()==true){
            listviewNotify();
            thread.interrupt();
        }

        if(isLongClick == false) {
            dirName.clear();
            dirName.add(0, new ListviewItem(null, "..", ""));
            adapter = new ListviewAdapter(this, R.layout.listview_item, dirName);

        } else {
            dirName.remove(0);
            adapter = new ListviewAdapter(this, R.layout.listview_multi_item, dirName);
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
                dirName.add(new ListviewItem(new FilesUtil().getFileIcon(tempList.get(i), getApplication()), tempList.get(i).getName(), sf.format(tempList.get(i).lastModified())));
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
                                    dirName.set(i + 1, new ListviewItem(new ManagementCache().getCacheFile(getApplication(), tempList.get(i).getName()), tempList.get(i).getName(), sf.format(tempList.get(i).lastModified())));
                                }
                                else {
                                    dirName.set(i + 1, new ListviewItem(new FilesUtil().getFileIcon(tempList.get(i), getApplication()), tempList.get(i).getName(), sf.format(tempList.get(i).lastModified())));
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

    public void renameDir(String dir){

        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.cdialog,(ViewGroup) findViewById(R.id.layout_root));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText cName = (EditText)layout.findViewById(R.id.cText);
        final TextView confirm = (TextView)layout.findViewById(R.id.cText_confirm);
        cName.setText(dir.toString());

        builder.setView(layout);
        builder.setMessage("이름 수정");
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
                File file = new File(currentPath + File.separator + cName.getText().toString());

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
                        if(file.renameTo(new File(currentPath+ File.separator + cName.getText().toString()))) {
                            Toast.makeText(getApplicationContext(), "변경 성공", Toast.LENGTH_SHORT).show();
                            Log.i("0", currentPath+ File.separator + cName.getText().toString() );
                        }
                                else {
                            Toast.makeText(getApplicationContext(), "변경 실패", Toast.LENGTH_SHORT).show();
                            Log.i("0", currentPath+ File.separator + cName.getText().toString() );
                        }

                        dialog.dismiss();
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void copyToClipboard(){

        int count = fileListView.getCount();
        int index = clipListView.getCount();
        boolean result = true;
        clipAdapter = new ClipboardListViewAdapter(this, R.layout.file_clipitem, clipListitem);

        for(int i = 0 ; i < count ; i++) {
            if(fileListView.isItemChecked(i)) {
                String filePath = currentPath + File.separator + dirName.get(i).getName().toString();

                for(int j = 0 ; j < index ; j++) {
                    if(clipListitem.get(j).getPath().toString().equals(filePath)) {
                        Toast.makeText(getApplicationContext(), "해당 파일이나 폴더가 클립보드에 이미 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                        result = false;
                        break;
                    }
                    else if((new File(filePath).isDirectory()) && (clipListitem.get(j).getPath().toString().startsWith(filePath))) {
                        Toast.makeText(getApplicationContext(), "해당 폴더의 하위 폴더가 클립보드에 이미 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                        result = false;
                        break;
                    }
                }

                if(result == true) {
                    clipListitem.add(new ClipboardListViewItem(filePath, dirName.get(i).getName().toString()));
                }
            }
        }
        clipListView.setAdapter(clipAdapter);
    }

    class removeThread implements Runnable {

            public void run() {
                int count = fileListView.getCount();
                boolean shutdown = false;

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

                        runOnUiThread(new Runnable() {
                            public void run() {
                                mProgressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "작업이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
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
                            count = count + new FilesUtil().countFilesIn(new File(clipListitem.get(i).getPath().toString()));
                        }

                         mProgressDialog.setMax(count);

                        for (int i = 0; i < clipListView.getCount(); i++) {

                            String targetedDir = clipListitem.get(i).getPath().toString(); // Path
                            String targetName = clipListitem.get(i).getName().toString();

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

                    runOnUiThread(new Runnable() {
                        public void run() {
                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "작업이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
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
        ab.setDisplayShowTitleEnabled(true);
        ab.setDisplayShowCustomEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);
        mMenu.setGroupVisible(R.id.longfalse, true);
        mMenu.setGroupVisible(R.id.longtrue, false);
        mMenu.setGroupVisible(R.id.under_two, false);
        mMenu.setGroupVisible(R.id.over_two, false);
        getDir(currentPath);
    }

    public void listviewNotify(){
        runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}