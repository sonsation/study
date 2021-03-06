package com.example.administrator.study_jh.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.administrator.study_jh.R;

import static com.example.administrator.study_jh.util.UriPathUtil.getRealPath;

/**
 * Created by Administrator on 2018-02-14.
 */

public class ImgView extends Activity{

    String path = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_view);

        Intent intent = getIntent();
        Uri data = intent.getData();
        path = getRealPath(getApplicationContext(), data);
        Log.e("test", path);

        ImageView image = findViewById(R.id.img2);
        Bitmap bm = BitmapFactory.decodeFile(path);
        image.setImageBitmap(bm);
    }
}
