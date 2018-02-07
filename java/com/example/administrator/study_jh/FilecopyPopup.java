package com.example.administrator.study_jh;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2018-01-30.
 */

public class FilecopyPopup extends Dialog {

    public FilecopyPopup(Activity activity) {
        super(activity);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //다이얼 로그 제목을 삭제하자
        setContentView(R.layout.filecopy_popup); // 다이얼로그 보여줄 레이아웃
    }
}
