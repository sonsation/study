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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018-01-26.
 */

public class FileListHome extends AppCompatActivity {

    private String internal_path = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String external_path = "";
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

        File file;

        final TextView space_confirm = (TextView)findViewById(R.id.space_confirm);
        final TextView external_space_confirm = (TextView)findViewById(R.id.external_space_confirm);
        final ProgressBar progress = (ProgressBar)findViewById(R.id.progress);
        final ProgressBar external_progress = (ProgressBar)findViewById(R.id.external_progress);
        final GridLayout layout = (GridLayout)findViewById(R.id.internal);
        final GridLayout external_layout = (GridLayout)findViewById(R.id.external);

        if(status.equalsIgnoreCase(Environment.MEDIA_MOUNTED)){

            layout.setVisibility(View.VISIBLE);

            file = new File(internal_path);

            double internal_total = file.getTotalSpace()/1024/1024/1024;
            double internal_used = internal_total - file.getFreeSpace()/1024/1024/1024;

            space_confirm.setText(internal_used+"GB/"+internal_total+"GB 사용됨");
            progress.setProgress((int)internal_used);
            progress.setMax((int)internal_total);

        }

        if(getExternalSdCardPath().length() > 0) {

            external_path = getExternalSdCardPath();
            external_layout.setVisibility(View.VISIBLE);

            file = new File(external_path);

            double external_total = file.getTotalSpace() / 1024 / 1024 / 1024;
            double external_used = external_total - file.getFreeSpace() / 1024 / 1024 / 1024;

            external_space_confirm.setText(external_used + "GB/" + external_total + "GB 사용됨");
            external_progress.setProgress((int) external_used);
            external_progress.setMax((int) external_total);

        }

        layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getApplicationContext(), FileListFragment.class);
                intent.putExtra("path", internal_path);
                startActivity(intent);
            }
        });

        external_layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(getApplicationContext(), FileListFragment.class);
                intent.putExtra("path", external_path);
                startActivity(intent);
            }
        });
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

    public static String getExternalSdCardPath() {
        String path = null;

        File sdCardFile = null;
        List<String> sdCardPossiblePath = Arrays.asList("external_sd", "ext_sd", "external", "extSdCard");

        for (String sdPath : sdCardPossiblePath) {
            File file = new File("/mnt/", sdPath);

            if (file.isDirectory() && file.canWrite()) {
                path = file.getAbsolutePath();

                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                File testWritable = new File(path, "test_" + timeStamp);

                if (testWritable.mkdirs()) {
                    testWritable.delete();
                }
                else {
                    path = null;
                }
            }
        }

        if (path != null) {
            sdCardFile = new File(path);
        }
        else {
            sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        }

        return sdCardFile.getAbsolutePath();
    }


}
