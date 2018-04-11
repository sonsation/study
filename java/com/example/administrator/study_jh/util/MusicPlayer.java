package com.example.administrator.study_jh.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.administrator.study_jh.R;

import java.io.IOException;

import static com.example.administrator.study_jh.util.UriPathUtil.getRealPath;

/**
 * Created by Administrator on 2018-02-22.
 */

public class MusicPlayer extends Activity {

    private String musicPath = null;
    private static MediaPlayer mp;
    private  SeekBar seekBar;
    private ImageButton mStart;
    private ImageView mAlbum;
    private TextView mName;
    private getPosition thread = new getPosition();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.musicplayer);

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        mStart = (ImageButton) findViewById(R.id.music_start);
        mAlbum = (ImageView)findViewById(R.id.music_album);
        mName = (TextView)findViewById(R.id.music_name);

        Intent intent = getIntent();
        Uri data = intent.getData();
        Log.e("test", data.getPath());
        musicPath = getRealPath(getApplicationContext(),data);


        MediaMetadataRetriever metadataRetriever;
        metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(musicPath);
        String albumName = metadataRetriever.extractMetadata(metadataRetriever.METADATA_KEY_TITLE);
        byte[] picture = metadataRetriever.getEmbeddedPicture();
        metadataRetriever.release();

        if(picture != null) {
            Bitmap icon = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            BitmapDrawable convertDrawble = new BitmapDrawable(getResources(), icon);
            mAlbum.setImageDrawable(convertDrawble);
            mName.setText(albumName);
        }

        if(musicPath != null) {

            try {
                String url = musicPath; // your URL here
                mp = new MediaPlayer();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource(url);
                mp.prepare();
                seekBar.setMax(mp.getDuration());
                mp.start();
                thread.start();

            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        mStart.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {

                if(mp.isPlaying()){
                    mp.pause();
                } else {
                    mp.start();
                    thread = new getPosition();
                    thread.start();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if(fromUser) {
                    mp.seekTo(progress);
                }
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
        mp.stop();
        mp.reset();
        mp.release();
        super.onBackPressed();
    }

    public class getPosition extends Thread {

        public void run() {

            try {

                while (mp.isPlaying()) {
                    seekBar.setProgress(mp.getCurrentPosition());
                }

                if (Thread.currentThread().isAlive())
                    Thread.currentThread().interrupt();

            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
