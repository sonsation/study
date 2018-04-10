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

import com.example.administrator.study_jh.asynchronous.services.CopyService;
import com.example.administrator.study_jh.handler.ProgressHandler;
import com.example.administrator.study_jh.R;

/**
 * Created by Administrator on 2018-03-26.
 */

public class FileCopy extends Activity {
        private String currentPath;

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

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CopyService.MSG_SET_INT_VALUE:

                    total_progress.setMax(ProgressHandler.getTotalFileCount());
                    partial_progress.setMax((int)ProgressHandler.getFileSize());
                    nameProcess.setText(ProgressHandler.getFileName());
                    partial_progress.setProgress(msg.arg1);
                    total_progress.setProgress(ProgressHandler.getProgressCount());
                    total_percent.setText(String.format("%d", (int)((double)ProgressHandler.getProgressCount() / (double)ProgressHandler.getTotalFileCount() * 100)) + "%");
                    partial_percent.setText(String.format("%d", (int)((double)msg.arg1/(double)ProgressHandler.getFileSize() * 100)) + "%");
                    break;

                case CopyService.MSG_UNREGISTER_CLIENT:

                    stopService(service);
                    finish();
                    break;

                default:
                    super.handleMessage(msg);
            }
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
                    mService = null;
                }
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, CopyService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
            }
        }

        @Override
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
        currentPath = intent.getStringExtra("path");

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

        service = new Intent(FileCopy.this, CopyService.class);
        service.putExtra("currentPath", currentPath);

        if(startService(service) != null){
            bindService(service, mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlHandler(CopyService.MSG_FORCESTOP_CLIENT);
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
                controlHandler(CopyService.MSG_TO_BACKGROUND);
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
        controlHandler(CopyService.MSG_FORCESTOP_CLIENT);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mConnection);
            mIsBound = false;
        } catch (Throwable t) {
        }
    }
}
