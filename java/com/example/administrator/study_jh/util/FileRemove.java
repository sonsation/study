package com.example.administrator.study_jh.util;

import android.app.Activity;
import android.content.Intent;
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

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018-03-21.
 */

public class FileRemove extends Activity {

    boolean flag = false;
    boolean running = true;
    removeAsync removeThread = new removeAsync();

    private ArrayList<String> fileList = new ArrayList<>();
    private String target = null;
    ProgressBar progressBar;
    Button btnStart;
    TextView result;
    TextView processing;
    TextView fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filecopy_popup);
        Intent intent = getIntent();

        fileList = (ArrayList<String>) intent.getSerializableExtra("path");
        //progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //fileName = (TextView) findViewById(R.id.fileName);
        //processing = (TextView) findViewById(R.id.processing);
        btnStart = (Button) findViewById(R.id.mExit);

        for(int i = 0 ; i< fileList.size(); i++){
            Log.e("e", fileList.get(i));
        }

        if(flag){
            Toast.makeText(this,"실행중입니다",Toast.LENGTH_SHORT).show();
        }else {
            removeThread.execute();
        }

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (removeThread.getStatus() == AsyncTask.Status.RUNNING)
                {
                    removeThread.cancel(true);
                }
            }
        });

    }

    public class removeAsync extends AsyncTask<String, Integer, Boolean> {

        int fileCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            flag = true;
            progressBar.setProgress(0);
            progressBar.setMax(fileList.size());
        }

        @Override
        protected Boolean doInBackground(String... params) {

            for (int i = 0; i < fileList.size(); i++) {
                remove(fileList.get(i));
                fileCount++;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                flag = false;
                Toast.makeText(getApplication(), "작업이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            running = false;
            setResult(RESULT_CANCELED);
            finish();
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(fileCount);
        }

        public void remove(String path) {
            if (running == true) {
                File file = new File(path);
                File[] tempFile = file.listFiles();

                if (file.exists()) {
                    if (file.isDirectory()) {
                        for (int i = 0; i < tempFile.length; i++) {
                            if (tempFile[i].isFile()) {
                                tempFile[i].delete();
                            } else {
                                remove(tempFile[i].getPath());
                            }
                            tempFile[i].delete();
                        }
                        file.delete();
                    } else if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
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
        setResult(RESULT_CANCELED);
        finish();
    }
}
