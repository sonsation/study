package com.example.administrator.study_jh.asynchronous.asynctasks;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.administrator.study_jh.asynchronous.services.ZipService;
import com.example.administrator.study_jh.handler.ProgressHandler;
import com.example.administrator.study_jh.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-03-26.
 */

public class ZipUtil extends Activity {

    ArrayList <String> checkedItem = new ArrayList<>();
    private String currentPath;
    private int compressionLevel;
    Intent service;

    ProgressBar total_progress;
    ProgressBar partial_progress;
    TextView total_percent;
    TextView partial_percent;
    TextView nameProcess;
    Button btnStop;
    Button toBack;

    Messenger mService = null;
    boolean mIsBound;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    NotificationManager mNotificationManager;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ZipService.MSG_SET_VALUE:

                    total_progress.setMax((int)ProgressHandler.getTotalFileSize());
                    partial_progress.setMax((int)ProgressHandler.getPartialFileSize());
                    nameProcess.setText(ProgressHandler.getFileName());
                    partial_progress.setProgress(msg.arg1);
                    total_progress.setProgress(msg.arg2);
                    total_percent.setText(String.format("%d", (int)((double)msg.arg2 / (double)ProgressHandler.getTotalFileSize() * 100)) + "%");
                    partial_percent.setText(String.format("%d", (int)((double)msg.arg1/(double)ProgressHandler.getPartialFileSize() * 100)) + "%");

                    break;

                case ZipService.MSG_UNREGISTER_CLIENT:

                    stopService(service);
                    finish();
                default:
                    super.handleMessage(msg);
            }
        }
    }

    void doBindService() {
        bindService(new Intent(this, ZipService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {

        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, ZipService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {

                }
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    void controlHandler(int controlValue) {

        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, controlValue);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {

                }
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mConnection);
        } catch (Throwable t) {
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, ZipService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filecopy_popup);

        Intent intent = getIntent();

        checkedItem = (ArrayList<String>) intent.getSerializableExtra("zipList");
        currentPath = intent.getStringExtra("currentPath");
        compressionLevel = intent.getIntExtra("compressionLevel", 0);

        total_progress = findViewById(R.id.total_progress);
        partial_progress = findViewById(R.id.partial_progress);
        total_percent = findViewById(R.id.total_percent);
        partial_percent = findViewById(R.id.partial_percent);
        nameProcess = findViewById(R.id.nameProcess);
        btnStop = findViewById(R.id.mExit);
        toBack = findViewById(R.id.toBackground);

        total_progress.setProgress(0);
        partial_progress.setProgress(0);
        nameProcess.setSelected(true);

        service = new Intent(ZipUtil.this, ZipService.class);
        service.putExtra("zipList", checkedItem);
        service.putExtra("currentPath", currentPath);
        service.putExtra("compressionLevel", 9);

        if(startService(service) != null){
            bindService(service, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlHandler(ZipService.MSG_FORCESTOP_CLIENT);
                if(stopService(service)){
                    unbindService(mConnection);
                    mIsBound = false;
                }
                finish();
            }
        });

        toBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlHandler(ZipService.MSG_TO_BACKGROUND);
                finish();
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        controlHandler(ZipService.MSG_FORCESTOP_CLIENT);

        if(mNotificationManager != null) {
            mNotificationManager.cancel(1);
        }

        finish();
    }
}
