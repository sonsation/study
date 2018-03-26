package com.example.administrator.study_jh.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.administrator.study_jh.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2018-03-21.
 */

public class TextEditor extends Activity {
    int id[] = new int[2];
    int x[] = new int[2];
    int y[] = new int[2];
    double distance = 0;
    String result;
    String path = null;
    EditText textViewer;
    ScrollView scroll;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textedit);

        scroll = (ScrollView) findViewById(R.id.scroll_edit);
        textViewer = (EditText) findViewById(R.id.textedit);
        textViewer.setMovementMethod(new ScrollingMovementMethod());
        textViewer.setTextIsSelectable(true);
        scroll.setVerticalScrollBarEnabled(true);


        Intent intent = getIntent();
        Uri data = intent.getData();
        path = data.getPath();
        textViewer.setText(readTxt());

        scroll.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pointer_count = event.getPointerCount(); //현재 터치 발생한 포인트 수를 얻는다.
                if(pointer_count > 2) pointer_count = 2; //4개 이상의 포인트를 터치했더라도 3개까지만 처리를 한다.

                switch(event.getAction() & MotionEvent.ACTION_MASK) {


                    case MotionEvent.ACTION_POINTER_DOWN: //두 개 이상의 포인트에 대한 DOWN을 얻을 때.
                        result = "detect multi touch\n";
                        textViewer.setTextIsSelectable(false);
                        for(int i = 0; i < pointer_count; i++) {
                            id[i] = event.getPointerId(i); //터치한 순간부터 부여되는 포인트 고유번호.
                            x[i] = (int) (event.getX(i));
                            y[i] = (int) (event.getY(i));
                            result += "id[" + id[i] + "] ("+x[i]+","+y[i]+")\n";
                            distance = getDistance (x[0], y[0], x[1], y[1]);
                        }

                        Log.e("0", String.valueOf(distance));
                        break;


                    case MotionEvent.ACTION_MOVE:
                        textViewer.setTextIsSelectable(false);
                        result = "멀티터치 MOVE:\n";
                        for(int i = 0; i < pointer_count; i++) {
                            id[i] = event.getPointerId(i);
                            x[i] = (int) (event.getX(i));
                            y[i] = (int) (event.getY(i));
                            result += "id[" + id[i] + "] ("+x[i]+","+y[i]+")\n";

                        }

                        if(distance > getDistance (x[0], y[0], x[1], y[1])){
                            textViewer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 5);
                        } else {
                            textViewer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        distance = 0;
                        textViewer.setTextIsSelectable(true);
                        result = "";
                        break;

                }

                //textViewer.setText(result);

                return false;
            }
        });

    }

    static Double getDistance(int x, int y, int x1, int y1){
        double getResult = Math.sqrt((x-x1)*(x-x1) + (y-y1) * (y-y1));
        String convertForm = String.format("%.2f", getResult);

        return Double.parseDouble(convertForm);
    }

    private String readTxt() {

        File file = new File(path);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return text.toString();

    }




}
