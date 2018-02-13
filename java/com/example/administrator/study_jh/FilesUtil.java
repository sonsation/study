package com.example.administrator.study_jh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Administrator on 2018-02-13.
 */

public class FilesUtil {

    public static boolean copyFile(File file, File toDir){
        boolean result;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        int data = -1;

        if(file.exists()){

            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                fos = new FileOutputStream(toDir);
                bos = new BufferedOutputStream(fos);

                while((data = bis.read())!= -1){
                    bos.write(data);
                }
                bos.flush();
                bos.close(); fos.close(); bis.close(); fis.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            result = true;
        }
        else {
            result = false;
        }
        return result;
    }

    public static void copyDir(File dir, File toDir){

        if(!toDir.exists()){
            toDir.mkdirs();
        }

        File[] fList = dir.listFiles();

        if(fList.length > 0) {

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

    public static void remove(String path) {

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
            }
            else if (file.isFile()) {
                file.delete();
            }
        }
    }

    public static String getFileNmae(String fileName) {
        if(fileName.contains(".")){
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        else {
            return "";
        }
    }

    public static String getFileExtension(File file)
    {
        return getFileExtension(file.getName());
    }

    public static String getFileExtension(String fileName) {

        if(fileName.contains(".")){
            return fileName.substring(fileName.lastIndexOf(".")+1);
        }
        else {
            return "";
        }
    }

    public static String getValidateFileName(File direction, String fileName, String fileDirection, String path) {

        int index = 0;
        int number = 2;

        if (direction.isFile()) {

            if (fileDirection.equals(path + File.separator + fileName)) {
                fileName = getFileNmae(fileName) + "_copied." + getFileExtension(fileName);
            }

            File temp = new File(path);
            File[] tempList = temp.listFiles();

            for (int j = 0; j < tempList.length; j++) {
                if (tempList[j].getName().equals(fileName)) {
                    fileName = getFileNmae(fileName) + "(2)." + getFileExtension(fileName);
                    break;
                }
            }

            for (int j = 0; j < tempList.length; j++) {
                while (tempList[j].getName().equals(fileName)) {
                    index = fileName.lastIndexOf("(");
                    int dotIndex = fileName.lastIndexOf(".");
                    String beforeDot = fileName.substring(0, index);
                    String afterDot = fileName.substring(dotIndex, fileName.length());
                    fileName = beforeDot + "(" + Integer.valueOf(number) + ")" + afterDot;
                    number++;
                }
            }
        } else if (direction.isDirectory()) {

            if (fileDirection.equals(path + File.separator + fileName)) {
                fileName = fileName + "_copied";
            }

            File temp = new File(path);
            File[] tempList = temp.listFiles();

            for (int j = 0; j < tempList.length; j++) {
                if (tempList[j].getName().equals(fileName)) {
                    fileName = fileName + "(2)";
                    break;
                }
            }

            for (int j = 0; j < tempList.length; j++) {
                while (tempList[j].getName().equals(fileName)) {
                    index = fileName.lastIndexOf("(");
                    String beforeDot = fileName.substring(0, index);
                    fileName = beforeDot + "(" + Integer.valueOf(number) + ")";
                    number++;
                }
            }

        }
        return fileName;
    }

    public static int countFilesIn(File root)
    {
        if (root.isDirectory() == false) return 1;
        File[] files = root.listFiles();
        if (files == null) return 0;

        int n = 0;

        for (File file : files)
        {
            if (file.isDirectory())
                n += countFilesIn(file);
            else
                n ++;
        }
        return n;
    }

    public static String getFileMimeType(File file)
    {
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(file.getName()));
        if (type == null) return "*/*";
        return type;
    }

    @SuppressLint("NewApi")
    public static int getFileIcon(File file, Context context){

        if(file.isDirectory()){
            return R.drawable.folder;
        }
        else if(file.isFile()){
            if(getFileExtension(file).equals("apk")) {
                String filePath = file.getPath();
                PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
                if(packageInfo != null) {
                    ApplicationInfo appinfo = packageInfo.applicationInfo;
                    if(Build.VERSION.SDK_INT >= 8) {
                        appinfo.sourceDir = filePath;
                        appinfo.publicSourceDir = filePath;
                    }
                    Drawable icon1 = appinfo.loadIcon(context.getPackageManager());
                    //return icon1;
                }
            }
            else {
                return R.drawable.file;
            }
        }
        return 0;
    }

}
