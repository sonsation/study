package com.example.administrator.study_jh.asynchronous.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.study_jh.FileList;
import com.example.administrator.study_jh.R;
import com.example.administrator.study_jh.asynchronous.asynctasks.FileCopy;
import com.example.administrator.study_jh.handler.ClipboardHandler;
import com.example.administrator.study_jh.handler.ProgressHandler;
import com.example.administrator.study_jh.listview.ClipboardListViewItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static com.example.administrator.study_jh.util.FilesUtil.countFilesIn;
import static com.example.administrator.study_jh.util.FilesUtil.getValidateFileName;

public class CopyService extends Service{

    String currentPath;
    ArrayList<ClipboardListViewItem> clipList = new ArrayList<>();
    long fileSize = 0;
    int success = 0;
    File toFile;
    File file;
    boolean flag = false;

    private static boolean isRunning = false;
    NotificationManager mNotificationManager;

    Messenger mClients = null;
    final CopyService.copyAsync copyThread = new CopyService.copyAsync();

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_FORCESTOP_CLIENT = 3;
    public static final int MSG_SET_INT_VALUE = 4;
    public static final int MSG_TO_BACKGROUND = 5;

    final Messenger mMessenger = new Messenger(new CopyService.IncomingHandler());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public IBinder onBind(Intent intent) {

        currentPath = intent.getStringExtra("currentPath");
        clipList = new ClipboardHandler().getClip();
        isRunning = true;

        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (copyThread.getStatus() == AsyncTask.Status.RUNNING)
        {
            copyThread.cancel(true);
        }

        isRunning = false;

    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients = msg.replyTo;

                    if(flag){
                        Toast.makeText(CopyService.this,"실행중입니다",Toast.LENGTH_SHORT).show();
                    } else {
                        copyThread.execute();
                    }

                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients = null;
                    break;
                case MSG_FORCESTOP_CLIENT:

                    if(toFile.exists()) {
                        toFile.delete();
                    }
                    if (copyThread.getStatus() == AsyncTask.Status.RUNNING) {
                        copyThread.cancel(true);
                    }

                case MSG_TO_BACKGROUND:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        NotificationChannel channelMessage = new NotificationChannel("channel_id", "channel_name", android.app.NotificationManager.IMPORTANCE_DEFAULT);
                        channelMessage.setDescription("channel description");
                        channelMessage.enableLights(true);
                        channelMessage.setLightColor(Color.GREEN);
                        channelMessage.enableVibration(true);
                        channelMessage.setVibrationPattern(new long[]{100, 200, 100, 200});
                        channelMessage.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                        notificationManager.createNotificationChannel(channelMessage);

                        Notification.Builder mBuilder =
                                new Notification.Builder(CopyService.this)
                                        .setSmallIcon(R.drawable.action_allselect)
                                        .setContentTitle("Copying...")
                                        .setContentText("백그라운드 작업 진행중")
                                        .setChannelId("channel_id")
                                        //         . addAction(null, "취소", mCancelPendingIntent)
                                        .setAutoCancel(true);

                        mNotificationManager =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotificationManager.notify(1, mBuilder.build());
                    }

                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToUI(long size) {

            try {
                    Message msg = Message.obtain(null, MSG_SET_INT_VALUE, (int) size);
                    mClients.send(msg);
            } catch (RemoteException e) {
                mClients = null;
        }
    }

    private void controlHandler(int controlValue) {

            try {
                    Message msg = Message.obtain(null, controlValue);
                    mClients.send(msg);
            }
            catch (RemoteException e) {
                mClients = null;
            }
    }



    public class copyAsync extends AsyncTask<String, Integer, Boolean> {

        boolean running;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            flag = true;
            running = true;
        }

        @Override
        protected Boolean doInBackground(String... params) {

            for(int i = 0 ; i < clipList.size(); i++) {

                String targetedDir = clipList.get(i).getPath().toString(); // Path
                String targetName = clipList.get(i).getName().toString();
                file = new File(targetedDir);
                targetName = getValidateFileName(file, targetName, targetedDir, currentPath);
                toFile = new File(currentPath + File.separator + targetName);

                if (file.isFile()) {
                    copyFile(file, toFile);
                }

                else if (file.isDirectory()) {
                    copyDir(file, toFile);
                }

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {
                flag = false;
                running = false;

                if(mNotificationManager != null) {
                    mNotificationManager.cancel(1);
                }

                controlHandler(MSG_UNREGISTER_CLIENT);
                Toast.makeText(getApplication(), "작업이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {

            if(toFile.exists()) {
                toFile.delete();
            }

            flag = false;
            running = false;
            super.onCancelled();
        }

        long totalSize = 0;

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int size = values[0];

            totalSize = totalSize + size;
            sendMessageToUI(totalSize);
        }

        public void copyFile(File file, File toDir) {

            running = true;

            if (file.exists()) {

                fileSize = file.length();
                int index = file.toString().lastIndexOf("/");
                String fileName = file.toString().substring(index+1);

                new ProgressHandler(fileName, fileSize, success, fileCountClip());

                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;
                FileChannel fcin = null;
                FileChannel fcout = null;

                try {

                    inputStream = new FileInputStream(file);
                    outputStream = new FileOutputStream(toDir);
                    fcin = inputStream.getChannel();
                    fcout = outputStream.getChannel();

                    ByteBuffer buf = ByteBuffer.allocateDirect(64 * 1024);

                    int data = -1;

                    while(running && ((data = fcin.read(buf)) != -1) ){
                        buf.flip();
                        fcout.write(buf);
                        buf.clear();
                        publishProgress(data);
                    }

                    success++;

                    fcout.close();
                    fcin.close();
                    outputStream.close();
                    inputStream.close();
                    totalSize = 0;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void copyDir(File dir, File toDir) {

            if (!toDir.exists()) {
                toDir.mkdirs();
            }

            File[] fList = dir.listFiles();

            if (fList.length > 0) {

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
    }

    int fileCount = 0;

    public int fileCountClip(){

        for(int i  = 0 ; i < clipList.size() ; i++){
            fileCount = fileCount + countFilesIn(new File(clipList.get(i).getPath()));
        }

        return fileCount;
    }


}
