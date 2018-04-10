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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.administrator.study_jh.asynchronous.services.RemoveService;
import com.example.administrator.study_jh.handler.ProgressHandler;
import com.example.administrator.study_jh.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-03-26.
 */

public class FileRemove extends Activity {

    private ArrayList<String> fileList = new ArrayList<>();

    ProgressBar progress;
    TextView percent;
    TextView nameProcess;
    Button btnStop;
    Button toBack;

    Intent service;

    Messenger mService = null;
    boolean mIsBound;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RemoveService.MSG_SET_PROGRESS:

                    progress.setMax(ProgressHandler.getTotalFileCount());
                    nameProcess.setText(ProgressHandler.getFileName());
                    progress.setProgress(ProgressHandler.getProgressCount());
                    percent.setText(String.format("%d", (int)((double)ProgressHandler.getProgressCount() / (double)ProgressHandler.getTotalFileCount() * 100)) + "%");

                    break;

                case RemoveService.MSG_UNREGISTER_CLIENT:
                    stopService(service);
                    finish();
                default:
                    super.handleMessage(msg);
            }
        }
    }

    void doBindService() {
        bindService(new Intent(this, RemoveService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {

        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, RemoveService.MSG_UNREGISTER_CLIENT);
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
                Message msg = Message.obtain(null, RemoveService.MSG_REGISTER_CLIENT);
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
        setContentView(R.layout.file_remove);

        Intent intent = getIntent();
        fileList = (ArrayList<String>) intent.getSerializableExtra("path");

        progress = findViewById(R.id.progress);
        percent = findViewById(R.id.percent);
        nameProcess = findViewById(R.id.nameProcess);
        btnStop = findViewById(R.id.mExit);
        toBack = findViewById(R.id.toBackground);

        progress.setProgress(0);
        nameProcess.setSelected(true);

        service = new Intent(FileRemove.this, RemoveService.class);
        service.putExtra("fileList", fileList);

        if(startService(service) != null){
            bindService(service, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlHandler(RemoveService.MSG_FORCESTOP_CLIENT);
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
                controlHandler(RemoveService.MSG_TO_BACKGROUND);
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
        controlHandler(RemoveService.MSG_FORCESTOP_CLIENT);
        finish();
    }
}
