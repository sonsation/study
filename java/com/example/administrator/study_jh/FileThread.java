package com.example.administrator.study_jh;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2018-01-31.
 */

public class FileThread implements Runnable {

    FileListFragment main;
    int count = main.clipListView.getCount();

    public void run(){

        try {

            for (int i = 0; i < count; i++) {
                String targetedDir = main.clipListitem.get(i).getPath().toString();
                String targetFile =  main.clipListitem.get(i).getName().toString();
                File file = new File(targetedDir);
                //File target = new File(path.currentPath)
                main.copyFile(file, main.currentPath + File.separator + targetFile);
            }

            Thread.sleep(1000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
