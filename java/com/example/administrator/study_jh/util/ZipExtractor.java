package com.example.administrator.study_jh.util;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.study_jh.R;
import com.example.administrator.study_jh.listview.ListviewAdapter;
import com.example.administrator.study_jh.listview.ListviewItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Administrator on 2018-03-27.
 */

public class ZipExtractor extends AppCompatActivity {

    private ArrayList<String> dirName = new ArrayList<>();
    private ArrayAdapter adapter;
    private ListView listView;
    private String filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.zipextractor);

        Intent intent = getIntent();
        filePath = intent.getStringExtra("path");

        listView = (ListView) findViewById(R.id.zip_listview);

        extractZipFiles(filePath);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String path = dirName.get(position).toString();
                extractZipFiles(path);

            }
        });

    }

    public void getDir(){

    }

    public  boolean extractZipFiles(String zip_file ) {
        boolean result = false;

        byte[] data = new byte[4096];
        ZipEntry entry = null;
        ZipInputStream zipstream = null;
        FileOutputStream out = null;

        try {
            zipstream = new ZipInputStream(new FileInputStream(zip_file));

            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dirName);

            while ((entry = zipstream.getNextEntry()) != null) {

                //File entryFile;

                dirName.add(entry.getName());

                if(entry.isDirectory()){
                    Log.e("test", entry + "디렉터리");
                } else {
                    Log.e("test", entry + "파일");
                }

                int read = 0;

                /*

                if (entry.isDirectory()) {
                    File folder = new File(directory + entry.getName());
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    continue;
                } else {
                    entryFile = new File(directory + entry.getName());
                }

                if (!entryFile.exists()) {
                    boolean isFileMake = entryFile.createNewFile();
                }

                out = new FileOutputStream(entryFile);
                while ((read = zipstream.read(data, 0, 2048)) != -1)
                    out.write(data, 0, read);
                */
                //zipstream.closeEntry();
            }


            listView.setAdapter(adapter);

            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = false;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (zipstream != null) {
                try {
                    zipstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
