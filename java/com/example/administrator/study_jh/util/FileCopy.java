package com.example.administrator.study_jh.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.study_jh.FileList;
import com.example.administrator.study_jh.R;
import com.example.administrator.study_jh.listview.ClipboardListViewItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static com.example.administrator.study_jh.util.FilesUtil.getValidateFileName;

/**
 * Created by Administrator on 2018-02-22.
 */

public class FileCopy extends Activity {

    boolean flag = false;
    boolean running = true;
    copyAsync copyThread = new copyAsync();

    ProgressBar progressBar;
    Button btnStart;
    TextView result;
    TextView processing;
    TextView fileName;
    String currentPath = null;

    ArrayList<ClipboardListViewItem> clipList = new ArrayList<>();
    int clipSize = 0;
    int clipIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filecopy_popup);

        Intent intent = getIntent();
        currentPath = intent.getStringExtra("path");

        clipList = new ClipboardHandler().getClip();
        clipSize = clipList.size();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        fileName = (TextView) findViewById(R.id.fileName);
        processing = (TextView) findViewById(R.id.processing);
        btnStart = (Button) findViewById(R.id.mExit);

        if(flag){
            Toast.makeText(this,"실행중입니다",Toast.LENGTH_SHORT).show();
        }else {
            copyThread.execute();
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (copyThread.getStatus() == AsyncTask.Status.RUNNING)
                {
                    copyThread.cancel(true);
                }
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

    }

    public class copyAsync extends AsyncTask<String, Integer, Boolean> {

        long fileSize = 0;
        File toFile;
        File file;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            flag = true;
            progressBar.setProgress(0);

            String targetedDir = clipList.get(clipIndex).getPath().toString(); // Path
            String targetName = clipList.get(clipIndex).getName().toString();
            file = new File(targetedDir);
            targetName = getValidateFileName(file, targetName, targetedDir, currentPath);
            toFile = new File(currentPath + File.separator + targetName);

        }

        @Override
        protected Boolean doInBackground(String... params) {


            if (file.isFile()) {
                copyFile(file, toFile);
            }

            if (file.isDirectory()) {
                copyDir(file, toFile);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {

                if (clipIndex < clipSize-1) {
                    clipIndex++;
                    new copyAsync().execute();
                } else {
                    flag = false;
                    Toast.makeText(getApplication(), "완료.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

        @Override
        protected void onCancelled() {

            runOnUiThread(new Runnable() {
                public void run() {
                    if(toFile.exists()) {
                        toFile.delete();
                    }
                }
            });

            running = false;
            finish();
            super.onCancelled();
        }

        long totalSize = 0;

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int size = values[0];

            totalSize = totalSize + size;
            long percentage = totalSize / fileSize * 100;
            processing.setText(totalSize/1024/1024 + " / " + fileSize/1024/1024 + "MB" +" (" + percentage + "%)");
            progressBar.setProgress((int)totalSize);
        }

        public void copyFile(File file, File toDir) {

            running = true;
            progressBar.setProgress(0);
            int index = file.toString().lastIndexOf("/");
            String sourceName = file.toString().substring(index+1);

            if (file.exists()) {

                fileName.setText(sourceName);
                fileSize = file.length();
                progressBar.setMax((int)fileSize);

                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;
                FileChannel fcin = null;
                FileChannel fcout = null;

                try {

                    inputStream = new FileInputStream(file);
                    outputStream = new FileOutputStream(toDir);
                    fcin = inputStream.getChannel();
                    fcout = outputStream.getChannel();

                    ByteBuffer buf = ByteBuffer.allocateDirect(64 * 1024);

                    int data = -1;

                    while(running && ((data = fcin.read(buf)) != -1) ){
                        buf.flip();
                        fcout.write(buf);
                        buf.clear();
                        publishProgress(data);
                    }

                    fcout.close();
                    fcin.close();
                    outputStream.close();
                    inputStream.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void copyDir(File dir, File toDir) {

            if (!toDir.exists()) {
                toDir.mkdirs();
            }

            File[] fList = dir.listFiles();

            if (fList.length > 0) {

                for (int i = 0; i < fList.length; i++) {

                    File oldPath = new File(fList[i].getPath());
                    File newPath = new File(toDir.getPath() + File.separator + fList[i].getName());

                    if (fList[i].isDirectory()) {
                        copyDir(oldPath, newPath);
                    } else if (fList[i].isFile()) {
                        copyFile(fList[i], newPath);
                    }

                }
            }
        }
    }
}
