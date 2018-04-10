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

import com.example.administrator.study_jh.R;
import com.example.administrator.study_jh.asynchronous.asynctasks.FileRemove;
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

public class RemoveService extends Service{

    boolean flag = false;
    boolean running = true;
    private static boolean isRunning = false;
    private ArrayList<String> fileList = new ArrayList<>();

    Messenger mClients = null;
    final RemoveService.removeAsync removeThread = new RemoveService.removeAsync();

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_FORCESTOP_CLIENT = 3;
    public static final int MSG_TO_BACKGROUND = 4;
    public static final int MSG_SET_PROGRESS = 5;

    ArrayList<ClipboardListViewItem> clipList = new ArrayList<>();
    NotificationManager mNotificationManager;
    final Messenger mMessenger = new Messenger(new RemoveService.IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    @Override
    public IBinder onBind(Intent intent) {

        fileList = (ArrayList<String>) intent.getSerializableExtra("fileList");

        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients = msg.replyTo;

                    if(flag){
                        Toast.makeText(RemoveService.this,"실행중입니다",Toast.LENGTH_SHORT).show();
                    }else {
                        removeThread.execute();
                    }

                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients = null;
                    break;
                case MSG_FORCESTOP_CLIENT:

                    if (removeThread.getStatus() == AsyncTask.Status.RUNNING) {
                        removeThread.cancel(true);
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
                                new Notification.Builder(RemoveService.this)
                                        .setSmallIcon(R.drawable.action_allselect)
                                        .setContentTitle("Removing...")
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

        if (removeThread.getStatus() == AsyncTask.Status.RUNNING)
        {
            removeThread.cancel(true);
        }

        isRunning = false;
    }

    public class removeAsync extends AsyncTask<String, Integer, Boolean> {

        int fileCount=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            flag = true;
        }

        @Override
        protected Boolean doInBackground(String... params) {

            for (int i = 0; i < fileList.size(); i++) {
                remove(fileList.get(i));
            }
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

            if (removeThread.getStatus() == AsyncTask.Status.RUNNING)
            {
                removeThread.cancel(true);
            }

            controlHandler(MSG_UNREGISTER_CLIENT);

            running = false;
            super.onCancelled();
        }

        public void remove(String path) {

            if (running == true) {

                File file = new File(path);
                File[] tempFile = file.listFiles();

                if (file.exists()) {

                    if (file.isDirectory()) {

                        for (int i = 0; i < tempFile.length; i++) {

                            if (tempFile[i].isFile()) {
                                fileCount++;
                                int last_index = tempFile[i].getPath().lastIndexOf("/");
                                String fileNmae = tempFile[i].getPath().substring(last_index+1);
                                new ProgressHandler(fileNmae, fileCount, totalFIleCount());
                                controlHandler(MSG_SET_PROGRESS);

                                tempFile[i].delete();
                            } else {
                                remove(tempFile[i].getPath());
                            }

                            fileCount++;
                            int last_index = tempFile[i].getPath().lastIndexOf("/");
                            String fileNmae = tempFile[i].getPath().substring(last_index+1);
                            new ProgressHandler(fileNmae, fileCount, totalFIleCount());
                            controlHandler(MSG_SET_PROGRESS);
                            tempFile[i].delete();
                        }

                        file.delete();
                    } else if (file.isFile()) {

                        fileCount++;
                        int last_index = file.getPath().lastIndexOf("/");
                        String fileNmae = file.getPath().substring(last_index+1);
                        new ProgressHandler(fileNmae, fileCount, totalFIleCount());
                        controlHandler(MSG_SET_PROGRESS);
                        file.delete();
                    }
                }
            }
        }
    }

    int fileCount = 0;

    public int totalFIleCount(){

        for(int i  = 0 ; i < clipList.size() ; i++){
            fileCount = fileCount + countFilesIn(new File(clipList.get(i).getPath()));
        }

        return fileCount;
    }

}
