package com.example.administrator.study_jh.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.study_jh.R;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.example.administrator.study_jh.util.FilesUtil.getValidateFileName;


/**
 * Created by Administrator on 2018-03-26.
 */

public class ZipUtil extends Activity {

    ArrayList <String> checkedItem = new ArrayList<>();
    private String currentPath;
    private int compressionLevel;
    public ArrayList<String> mTempPath = new ArrayList<>();

    boolean flag = false;
    boolean running = true;

    ProgressBar total_progress;
    ProgressBar partial_progress;
    TextView total_percent;
    TextView partial_percent;
    TextView nameProcess;
    Button btnStop;

    zipAsync zipThread = new zipAsync();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filecopy_popup);

        Intent intent = getIntent();

        checkedItem = (ArrayList<String>) intent.getSerializableExtra("zipList");
        currentPath = intent.getStringExtra("currentPath");
        compressionLevel = intent.getIntExtra("compressionLevel", 0);

        total_progress = (ProgressBar) findViewById(R.id.total_progress);
        partial_progress = (ProgressBar) findViewById(R.id.partial_progress);
        total_percent = (TextView) findViewById(R.id.total_percent);
        partial_percent = (TextView) findViewById(R.id.partial_percent);
        nameProcess = (TextView) findViewById(R.id.nameProcess);
        btnStop = (Button) findViewById(R.id.mExit);


        if(flag){
            Toast.makeText(this,"실행중입니다",Toast.LENGTH_SHORT).show();
        }else {
            zipThread.execute();
        }

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (zipThread.getStatus() == AsyncTask.Status.RUNNING)
                {
                    zipThread.cancel(true);
                }

                setResult(RESULT_CANCELED);

                finish();
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

        if (zipThread.getStatus() == AsyncTask.Status.RUNNING)
        {
            zipThread.cancel(true);
        }

        if(new File(currentPath).exists()) {
            new File(currentPath).delete();
        }

        setResult(RESULT_CANCELED);
        finish();
    }

    public class zipAsync extends AsyncTask<String, Integer, Boolean> {

        long fileSize = 0;
        long totalSize = 0;
        long totalFileSize = 0;
        long singleSize = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            flag = true;
            total_progress.setProgress(0);
            partial_progress.setProgress(0);

        }

        @Override
        protected Boolean doInBackground(String... params) {

            zipCompress();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                    Toast.makeText(getApplication(), "작업이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
            }
        }

        @Override
        protected void onCancelled() {

            if(new File(currentPath).exists()) {
                new File(currentPath).delete();
            }

            setResult(RESULT_CANCELED);
            running = false;
            finish();
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int size = values[0];

            fileSize = fileSize + size;
            totalFileSize = totalFileSize + size;
            partial_progress.setProgress((int)fileSize);
            total_progress.setProgress((int)totalFileSize);

            total_percent.setText(String.format("%d", (int)((double)totalFileSize/(double)totalSize * 100)) + "%");
            partial_percent.setText(String.format("%d", (int)((double)fileSize/(double)singleSize * 100)) + "%");
        }

        public void zipCompress() {

            ArrayList<String> zipList = new ArrayList<>();

            for(int i = 0 ; i < checkedItem.size() ; i++) {
                zipList = getFileList(checkedItem.get(i));
            }

            for(int i = 0 ; i < zipList.size() ; i++) {
                totalSize = totalSize + new File(zipList.get(i)).length();
            }

            for(int i = 0 ; i < zipList.size() ; i++) {
                Log.e("test" , zipList.get(i));
            }

            total_progress.setMax((int) totalSize);

            try {

                byte[] buffer = new byte[4096];

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(currentPath));
                ZipOutputStream zipOs = new ZipOutputStream(bos);
                FileInputStream in = null;
                BufferedInputStream bis = null;
                zipOs.setLevel(9);

                for(int i = 0 ; i < zipList.size() ; i++) {

                    singleSize = new File(zipList.get(i)).length();
                    partial_progress.setMax((int)singleSize);
                    int index = zipList.get(i).lastIndexOf("/");
                    nameProcess.setText(zipList.get(i).toString().substring(index+1));

                    int len = 0;

                    if (new File(zipList.get(i)).isFile()) {
                        in = new FileInputStream(zipList.get(i));
                    }

                    bis = new BufferedInputStream(in);

                    String rootPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
                    String name = zipList.get(i).substring(rootPath.length() + 1);
                    ZipEntry ze = new ZipEntry(name);
                    zipOs.putNextEntry(ze);

                    if (new File(zipList.get(i)).isFile()) {

                        while (((len = bis.read(buffer)) > 0) && (running == true)) {
                            zipOs.write(buffer, 0, len);
                            publishProgress(len);
                        }
                    }

                    fileSize = 0;

                    zipOs.closeEntry();
                    bis.close();
                }

                zipOs.close();
                bos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getFileList(String FilePath) {

        File temp = new File(FilePath);

        if(temp.isFile()){
            mTempPath.add(temp.getPath());
        } else {

            File[] fileList = temp.listFiles();

            if(fileList.length == 0) {
                mTempPath.add(temp.getPath()+"/");
            }

            for(int i = 0 ; i < fileList.length ; i++) {

                if(fileList[i].isFile()) {
                    mTempPath.add(fileList[i].getPath());
                } else {
                    getFileList(fileList[i].getPath());
                }
            }

        }
        return mTempPath;
    }

}
