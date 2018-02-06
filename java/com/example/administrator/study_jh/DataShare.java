package com.example.administrator.study_jh;

import java.io.Serializable;

/**
 * Created by Administrator on 2018-02-06.
 */

public class DataShare implements Serializable {

    public String name;
    public int totalitemcount;

    public DataShare() {}

    public DataShare(String name, int totalitemcount) {
        this.name = name;
        this.totalitemcount = totalitemcount;
    }
}
