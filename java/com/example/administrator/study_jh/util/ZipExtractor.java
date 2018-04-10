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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by Administrator on 2018-03-27.
 */

public class ZipExtractor extends AppCompatActivity {

    private ArrayList<String> dirName = new ArrayList<>();
    HashMap<String, ArrayList<String>> headerDetails = new HashMap<>() ;
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

        //getZipFiles(filePath);
        printFileList(filePath);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String path = dirName.get(position).toString();
                getZipFiles(path);

            }
        });

    }

    public void printFileList(String filePath){

        FileInputStream fis = null;
        ZipInputStream zipIs = null;
        ZipEntry zEntry = null;

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dirName);

        try {
            fis = new FileInputStream(filePath);
            zipIs = new ZipInputStream(new BufferedInputStream(fis));
            while((zEntry = zipIs.getNextEntry()) != null){

                if(zEntry.isDirectory()) {

                    Log.e("test", zEntry.getName() + "is Dir");

                    if(zEntry.getName().contains("/")) {

                        String temp = zEntry.getName();

                        while(temp.contains("/")) {
                            temp = new File(temp).getParent();
                            Log.e("test", temp);
                        }
                    }

                }

                dirName.add(zEntry.getName());
            }
            zipIs.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        listView.setAdapter(adapter);
    }

    public  void getZipFiles(String zip_file) {

        try {

            ZipFile zipFile = new ZipFile(zip_file);
            Enumeration zipEntries = zipFile.entries();

            String fName;

            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dirName);


            while (zipEntries.hasMoreElements()) {

                fName = ((ZipEntry)zipEntries.nextElement()).getName();
                ZipEntry zipentry = ((ZipEntry)zipEntries.nextElement());

                if(zipentry.isDirectory()) {
                    Log.e("test", fName.toString() + "is Dir");
                } else {
                    Log.e("test", fName.toString() + "is File");
                }


                if(!fName.contains("/")) {
                    dirName.add(fName);
                } else {
                    int index = fName.indexOf("/");
                    dirName.add(fName.substring(0, index));
                }

            }




            listView.setAdapter(adapter);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
