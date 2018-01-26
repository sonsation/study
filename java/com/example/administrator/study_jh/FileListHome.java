package com.example.administrator.study_jh;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Administrator on 2018-01-26.
 */

public class FileListHome extends AppCompatActivity {

    private String rootPath = "/sdcard/";
    private String status = Environment.getExternalStorageState();
    //private String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filelisthome);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_name);
        getSupportActionBar().setTitle("File Manager");

        File file = new File(rootPath);

        final TextView space_confirm = (TextView)findViewById(R.id.space_confirm);
        final ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
        //final TextView space_confirm2 = (TextView)findViewById(R.id.space_confirm2);
        final ProgressBar progress2 = (ProgressBar)findViewById(R.id.progressBar2);
        final GridLayout layout = (GridLayout)findViewById(R.id.internal);

        if(status.equalsIgnoreCase(Environment.MEDIA_MOUNTED)){

            layout.setVisibility(View.VISIBLE);

            double internal_total = file.getTotalSpace()/1024/1024/1024;
            double internal_used = internal_total - file.getFreeSpace()/1024/1024/1024;

            space_confirm.setText(internal_used+"GB/"+internal_total+"GB 사용됨");
            progress.setProgress((int)internal_used);
            progress.setMax((int)internal_total);

        }


        //ViewGroup layout = (ViewGroup) findViewById(R.id.internal);
        layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getApplicationContext(), FileListFragment.class);
                startActivity(intent);
            }
        });



        //Toast.makeText(getApplicationContext(), "FreeSpace/TotalSpace =" + i +"/"+ j  , Toast.LENGTH_LONG).show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.filelist_menu_header, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.layout__rightin, R.anim.layout__rightout);
                return true;

            default:

        }
        return super.onOptionsItemSelected(item);
    }
}
