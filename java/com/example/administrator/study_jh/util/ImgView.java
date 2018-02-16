package com.example.administrator.study_jh.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.administrator.study_jh.R;

/**
 * Created by Administrator on 2018-02-14.
 */

public class ImgView extends Activity{

    String path = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_view);

        path = getIntent().getStringExtra("imgPath");
        ImageView image = (ImageView)findViewById(R.id.img2);
        Bitmap bm = BitmapFactory.decodeFile(path);
        image.setImageBitmap(bm);
    }
}
