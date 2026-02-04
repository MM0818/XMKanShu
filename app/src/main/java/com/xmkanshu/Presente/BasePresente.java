package com.xmkanshu.Presente;

import android.graphics.Bitmap;


public interface BasePresente {
    void showSettingView();
    void showSettingDetailView();
    void DayAndNightChange(int styleCode);
    void LoadChapterContent();
    Bitmap changePageContent(int page);
}
