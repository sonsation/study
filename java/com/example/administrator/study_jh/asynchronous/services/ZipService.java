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
import android.widget.Toast;

import com.example.administrator.study_jh.R;
import com.example.administrator.study_jh.asynchronous.asynctasks.ZipUtil;
import com.example.administrator.study_jh.handler.ProgressHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipService extends Service {

    private String currentPath;
    private int compressionLevel;
    long partialWrittenSize = 0;
    long TotalFileSize = 0;
    long totalWrittenSize = 0;
    long partialFileSize = 0;
    boolean flag = false;
    boolean running = true;
    private static boolean isRunning = false;

    NotificationManager mNotificationManager;

    ArrayList <String> checkedItem = new ArrayList<>();
    ArrayList<String> mTempPath = new ArrayList<>();
    ArrayList<String> zipList = new ArrayList<>();
    Messenger mClients = null;
    final zipAsync zipThread = new zipAsync();

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_FORCESTOP_CLIENT = 3;
    public static final int MSG_TO_BACKGROUND = 4;
    public static final int MSG_SET_VALUE = 5;

    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    @Override
    public IBinder onBind(Intent intent) {

        checkedItem = (ArrayList<String>) intent.getSerializableExtra("zipList");
        currentPath = intent.getStringExtra("currentPath");
        compressionLevel = intent.getIntExtra("compressionLevel", 0);

        return mMessenger.getBinder();

    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients = msg.replyTo;

                    if(flag){
                        Toast.makeText(ZipService.this,"실행중입니다",Toast.LENGTH_SHORT).show();
                    }else {
                        zipThread.execute();
                    }

                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients = null;
                    break;
                case MSG_FORCESTOP_CLIENT:

                    if (zipThread.getStatus() == AsyncTask.Status.RUNNING) {
                        zipThread.cancel(true);
                    }

                    if(new File(currentPath).exists()) {
                        new File(currentPath).delete();
                    }

                case MSG_TO_BACKGROUND :

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
                                new Notification.Builder(ZipService.this)
                                        .setSmallIcon(R.drawable.action_allselect)
                                        .setContentTitle("Zipping...")
                                        .setContentText("백그라운드 작업 진행중")
                                        .setChannelId("channel_id")
                                        //         . addAction(null, "취소", mCancelPendingIntent)
                                        .setAutoCancel(true);

                        mNotificationManager =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotificationManager.notify(1, mBuilder.build());
                    }

                default:
                    super.handleMessage(msg);
                }
            }
    }

    private void sendMessageToUI(long intvaluetosend, long size) {
            try {
                Message msg = Message.obtain(null, MSG_SET_VALUE, (int)intvaluetosend, (int)size);
                mClients.send(msg);
            }
            catch (RemoteException e) {
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static boolean isRunning()
        {
            return isRunning;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            if (zipThread.getStatus() == AsyncTask.Status.RUNNING)
            {
                zipThread.cancel(true);
            }

            isRunning = false;
    }

    public class zipAsync extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            flag = true;
            running = true;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            zipCompress();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) {

                flag = false;

                if(mNotificationManager != null) {
                    mNotificationManager.cancel(1);
                }

                controlHandler(MSG_UNREGISTER_CLIENT);
                Toast.makeText(getApplication(), "작업이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {

            if(new File(currentPath).exists()) {
                new File(currentPath).delete();
            }
            running = false;

            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int size = values[0];

            partialWrittenSize = partialWrittenSize + size;
            totalWrittenSize = totalWrittenSize + size;
            sendMessageToUI(partialWrittenSize, totalWrittenSize);

        }

        public void zipCompress() {

            for(int i = 0 ; i < checkedItem.size() ; i++) {
                zipList = getFileList(checkedItem.get(i));
            }

            for(int i = 0 ; i < zipList.size() ; i++) {
                TotalFileSize = TotalFileSize + new File(zipList.get(i)).length();
            }

            try {

                byte[] buffer = new byte[4096];

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(currentPath));
                ZipOutputStream zipOs = new ZipOutputStream(bos);
                FileInputStream in = null;
                BufferedInputStream bis = null;
                zipOs.setLevel(9);

                for(int i = 0 ; i < zipList.size() ; i++) {

                    partialFileSize = new File(zipList.get(i)).length();
                    final int index = zipList.get(i).lastIndexOf("/");

                    String processingName = zipList.get(i).toString().substring(index+1);

                    new ProgressHandler(processingName ,partialFileSize , TotalFileSize);

                    int len = 0;

                    if (new File(zipList.get(i)).isFile()) {
                        in = new FileInputStream(zipList.get(i));
                    }

                    bis = new BufferedInputStream(in);

                    String rootPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
                    String name = zipList.get(i).substring(rootPath.length() + 1);
                    ZipEntry ze = new ZipEntry(name);
                    zipOs.putNextEntry(ze);

                    if (new File(zipList.get(i)).isFile()) {

                        while (((len = bis.read(buffer)) > 0) && (running == true)) {
                            zipOs.write(buffer, 0, len);
                            publishProgress(len);
                        }
                    }

                    partialWrittenSize = 0;

                    zipOs.closeEntry();
                    bis.close();
                }

                zipOs.close();
                bos.close();

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                mTempPath.clear();
                zipList.clear();
            }
        }
    }

    public ArrayList<String> getFileList(String FilePath) {

        File temp = new File(FilePath);

        if(temp.isFile()){
            mTempPath.add(temp.getPath());
        } else {

            File[] fileList = temp.listFiles();

            if(fileList.length == 0) {
                mTempPath.add(temp.getPath()+"/");
            }

            for(int i = 0 ; i < fileList.length ; i++) {

                if(fileList[i].isFile()) {
                    mTempPath.add(fileList[i].getPath());
                } else {
                    getFileList(fileList[i].getPath());
                }
            }

        }
        return mTempPath;
    }
}