package com.example.administrator.study_jh.handler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationHandler {

    public static final int COPY_ID = 0;
    public static final int MMOVE_ID = 1;
    public static final int REMOVE_ID = 2;
    public static final int ZIP_ID = 3;

    public static final String CHANNEL_ID = "channel";

    public static void setMetadata(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotiChannel(Context context){

        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel channelMessage = new NotificationChannel(CHANNEL_ID, "channel_name", android.app.NotificationManager.IMPORTANCE_MIN);
        channelMessage.setDescription("channel description");
        channelMessage.enableLights(true);
        channelMessage.setLightColor(Color.GREEN);
        channelMessage.enableVibration(true);
        channelMessage.setVibrationPattern(new long[]{100, 200, 100, 200});
        channelMessage.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channelMessage);
    }
}
