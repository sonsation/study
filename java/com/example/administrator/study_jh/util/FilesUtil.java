package com.example.administrator.study_jh.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.example.administrator.study_jh.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Administrator on 2018-02-13.
 */

public class FilesUtil {

    public static String getFileNmae(String fileName) {
        if(fileName.contains(".")){
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        else {
            return "";
        }
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

    public static String getFileMimeType(File file)
    {
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(file.getName()));
        if (type == null) return "*/*";
        return type;
    }

    @SuppressLint("NewApi")
    public Drawable getFileIcon(File file, Context context){

        if(file.isDirectory()){
            return context.getDrawable(R.drawable.folder);
        }

        else {

            if (getFileExtension(file).equals("apk")) {
                String filePath = file.getPath();
                PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);

                if (packageInfo != null) {
                    ApplicationInfo appinfo = packageInfo.applicationInfo;

                    if (Build.VERSION.SDK_INT >= 8) {
                        appinfo.sourceDir = filePath;
                        appinfo.publicSourceDir = filePath;
                    }

                    Drawable appIcon = appinfo.loadIcon(context.getPackageManager());

                    return appIcon;
                }
            } else if (getFileMimeType(file).startsWith("image/")) {

                // 읽어드릴 파일
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inPreferredConfig = Bitmap.Config.RGB_565;
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getPath(), bounds);


                bounds.inSampleSize = calculateInSampleSize(bounds, 50, 50);
                bounds.inJustDecodeBounds = false;

                Bitmap icon = BitmapFactory.decodeFile(file.getPath(), bounds);

                if(!(new ManagementCache().existCache(file.getName()))) {
                    new ManagementCache().saveBitmapToJpeg(icon, file.getName());
                }

                BitmapDrawable convertDrawble = new BitmapDrawable(context.getResources(), icon);

                return convertDrawble;

            } else if (getFileMimeType(file).startsWith("audio/")) {

                MediaMetadataRetriever metadataRetriever;
                metadataRetriever = new MediaMetadataRetriever();
                metadataRetriever.setDataSource(file.getAbsolutePath());
                byte[] picture = metadataRetriever.getEmbeddedPicture();
                if (picture == null) return null;
                metadataRetriever.release();

                Bitmap icon = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                BitmapDrawable convertDrawble = new BitmapDrawable(context.getResources(), icon);

                return convertDrawble;
            } else if (getFileMimeType(file).startsWith("video/")) {

                Bitmap icon = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
                BitmapDrawable convertDrawble = new BitmapDrawable(context.getResources(), icon);

                return convertDrawble;
            } else {
                return context.getDrawable(R.drawable.file);
            }

        }

        return null;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static String calFileSize(long fileSize){

        String size = null;
        long cal = 1024 * 1024;

        if(fileSize / cal > 1024) {
            size = String.format("%.2f",(double)fileSize / (double)cal / (double) 1024) + "GB";
        } else if ((fileSize / cal <= 1024) && (fileSize / cal) >= 1){
            size = String.format("%.2f",(double)fileSize/(double)cal) + "MB";
        } else if (fileSize / cal  < 1 ){
            size = String.format("%.2f", (double)fileSize / (double)1024) + "KB";
        }
        return size;
    }

}
