package com.xmkanshu.Data;

import android.graphics.Bitmap;


public class Picture {
    String name;
    Bitmap bitmap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Picture(String name, Bitmap bitmap) {
        this.name = name;
        this.bitmap = bitmap;
    }
}
