package com.example.administrator.study_jh.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2018-02-17.
 */

public class ManagementCache {

    private final static String cacheDirName = ".cache_fileFactory";
    private final static String cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + cacheDirName;
    private final static String cacheExtenstion = ".cache_";

    public String getCacheDIr(){

        File cache = new File(cacheDir);

        if(!cache.exists()) {
            cache.mkdirs();
        }

        return cacheDir;
    }

    public void saveBitmapToJpeg(Bitmap bitmap, String dir){

        File todir = new File(cacheDir + File.separator + cacheExtenstion + dir);

        try{
            if((!todir.exists()) || (!dir.startsWith(getCacheDIr()))) {

                FileOutputStream out = new FileOutputStream(todir);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);

                out.flush();
                out.close();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean existCache(String cache){

        File fCache = new File(getCacheDIr() + File.separator + cacheExtenstion + cache);

        if(fCache.exists()){
            return true;
        } else {
            return false;
        }

    }

    public Drawable getCacheFile(Context context, String cacheFileName){

        String findCache = cacheDir + File.separator + cacheExtenstion + cacheFileName;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        Bitmap icon = BitmapFactory.decodeFile(findCache, options);
        BitmapDrawable convertDrawble = new BitmapDrawable(context.getResources(), icon);

        return convertDrawble;
    }
}
