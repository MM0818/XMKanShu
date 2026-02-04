package com.xmkanshu.Presente;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;

import androidx.core.view.GravityCompat;

import com.hb.dialog.dialog.LoadingDialog;
import com.xmkanshu.Adapter.ChapterAdapter;
import com.xmkanshu.Cache.BookContentCache;
import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.Data.ReadConfig;
import com.xmkanshu.Model.Chapter;
import com.xmkanshu.R;
import com.xmkanshu.Reptile.GetAndRead;
import com.xmkanshu.UI.ReadingActivity;

import java.math.BigDecimal;


public class ReadPresenter implements BasePresente {
    private Dialog mSettingDialog;//设置视图
    private Dialog mSettingDetailDialog;//详细设置视图
    private ReadingActivity readingActivity;
    private ListView listView;
    private Canvas canvas;
    Paint textPaint = new Paint();
    GetAndRead getBook;
    ChapterAdapter chapterAdapter;
    LoadingDialog loadingDialog;

    public ReadPresenter(ReadingActivity readingActivity) {
        this.readingActivity = readingActivity;
        this.getBook = new GetAndRead(readingActivity); // 修复：在构造函数中初始化
    }

    @Override
    public void showSettingView() {
        if (mSettingDialog != null) {
            mSettingDialog.cancel();
            mSettingDialog = null;
        }

        mSettingDialog = DialogCreater.createReadSetting(readingActivity, this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //详细设置
                showSettingDetailView();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //目录
                readingActivity.drawerLayout.openDrawer(GravityCompat.START);
                if (mSettingDialog != null) {
                    mSettingDialog.dismiss();
                }
                listView = readingActivity.findViewById(R.id.listview_chapter_list);
                new loadTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //上一章
                if (GlobalConfig.chapternow > 0) {
                    GlobalConfig.chapternow -= 1;
                    GlobalConfig.Page = 0;
                    LoadChapterContent();
                    GlobalConfig.SaveReadSetting(readingActivity);//保存阅读进度
                    if (readingActivity != null && readingActivity.tv_read != null) {
                        readingActivity.tv_read.setImageBitmap(changePageContent(GlobalConfig.Page));
                    }
                    if(!ReadConfig.isDownload && getBook != null)
                    {
                        getBook.ReadingBackground(GlobalConfig.chapternow);
                    }
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //下一章
                if (GlobalConfig.chapternow < GlobalConfig.list.size() - 1) {
                    GlobalConfig.chapternow += 1;
                    GlobalConfig.Page = 0;
                    LoadChapterContent();
                    GlobalConfig.SaveReadSetting(readingActivity);//保存阅读进度
                    if (readingActivity != null && readingActivity.tv_read != null) {
                        readingActivity.tv_read.setImageBitmap(changePageContent(GlobalConfig.Page));
                    }
                    if(!ReadConfig.isDownload && getBook != null)
                    {
                        getBook.ReadingBackground(GlobalConfig.chapternow);
                    }

                }
            }
        }, new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //章节跳转
                if (GlobalConfig.list.size() > 0) {
                    GlobalConfig.chapternow = (GlobalConfig.list.size() - 1) * progress / 100;
                    GlobalConfig.Page = 0;
                    LoadChapterContent();
                    GlobalConfig.SaveReadSetting(readingActivity);//保存阅读进度
                    if (readingActivity != null && readingActivity.tv_read != null) {
                        readingActivity.tv_read.setImageBitmap(changePageContent(GlobalConfig.Page));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 不需要实现
            }
        });
    }

    @Override
    public void showSettingDetailView() {
        //显示详细设置
        if (mSettingDialog != null) {
            mSettingDialog.dismiss();
        }
        if (mSettingDetailDialog != null) {
            mSettingDetailDialog.show();
        } else {
            mSettingDetailDialog = DialogCreater.createReadDetailSetting(readingActivity,this);
        }
    }

    @Override
    public void DayAndNightChange(int styleCode) {
        switch (styleCode){
            case 0:
                ReadConfig.isDark = false; // ⭐新增（很重要）
                ReadConfig.fontColor = R.color.default_font_color;
                ReadConfig.bgColor = R.color.default_read_color;
                break;
            case 1:
                ReadConfig.isDark = true;  // ⭐新增（很重要）
                ReadConfig.fontColor = R.color.dark_font_color;
                ReadConfig.bgColor = R.color.dark_read_color;
                break;
        }

        if (readingActivity != null && readingActivity.tv_read != null) {
            readingActivity.intChapterStyle(ReadConfig.bgColor, ReadConfig.fontColor);
            readingActivity.intStyle();

            // ⭐新增：立刻重绘当前页 Bitmap
            readingActivity.tv_read.setImageBitmap(
                    changePageContent(GlobalConfig.Page)
            );
        }
    }


    private class loadTask extends AsyncTask<Void,Integer,Boolean>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new LoadingDialog(readingActivity);
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            chapterAdapter = new ChapterAdapter(GlobalConfig.list, readingActivity);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            if (listView != null && chapterAdapter != null) {
                listView.setAdapter(chapterAdapter);
                if (GlobalConfig.chapternow >= 5) {
                    listView.setSelection(GlobalConfig.chapternow - 5);
                } else {
                    listView.setSelection(0);
                }
            }
        }
    }

    public void LoadChapterContent()
    {
        if (readingActivity == null) {
            Log.e("ReadPresenter", "readingActivity is null!");
            return;
        }

        // 这部分计算可以在后台线程
        textPaint.setTextSize(ReadConfig.FontSize);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        GlobalConfig.mFontHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);
        GlobalConfig.mPageLineNum = (int) (GlobalConfig.measuredHeigtt / GlobalConfig.mFontHeight);

        String content = "";
        GlobalConfig.contentMap.clear();
        try {
            if (GlobalConfig.chapternow >= 0 && GlobalConfig.chapternow < GlobalConfig.list.size()) {
                Chapter currentChapter = GlobalConfig.list.get(GlobalConfig.chapternow);
                content = BookContentCache.getCache(currentChapter.getUrl());
            } else {
                Log.e("ReadPresenter", "chapternow out of bounds: " + GlobalConfig.chapternow);
                content = "章节索引错误";
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e("ReadPresenter", "获取章节内容失败", e);
            content = "获取内容失败";
        }

        content = getBook.splitContentFirst(content);//分段
        content = getBook.splitcontentSecond(content, ReadConfig.FontSize, GlobalConfig.measuredWidth);//段落分行
        getBook.PageSet(content, GlobalConfig.mPageLineNum, GlobalConfig.contentMap);//章节分页并存入hashmap

        // ========== 关键修改：UI更新移到主线程 ==========
        // 修复：在声明时给默认值
        String chapterTitle = "未知章节"; // 默认值

        try {
            if (GlobalConfig.chapternow >= 0 && GlobalConfig.chapternow < GlobalConfig.list.size()) {
                Chapter currentChapter = GlobalConfig.list.get(GlobalConfig.chapternow);
                chapterTitle = currentChapter.getTitle();
            }
            // 如果上面的if条件不满足，chapterTitle保持默认值"未知章节"
        } catch (Exception e) {
            Log.e("ReadPresenter", "获取章节标题失败", e);
            // chapterTitle已经是"未知章节"，不需要再赋值
        }

        // 保存最终值到final变量供Runnable使用
        final String finalChapterTitle = chapterTitle;

        // 检查当前线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 已经在主线程，直接更新
            if (readingActivity.tv_title != null) {
                readingActivity.tv_title.setText(finalChapterTitle);
            }
        } else {
            // 在后台线程，需要切换到主线程
            readingActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (readingActivity.tv_title != null) {
                        readingActivity.tv_title.setText(finalChapterTitle);
                    }
                }
            });
        }
    }

    public Bitmap changePageContent(int page) {
        if (readingActivity == null) {
            Log.e("ReadPresenter", "changePageContent: readingActivity is null!");
            return null;
        }

        // 设置阅读进度百分比
        if (GlobalConfig.chapternow == 0) {
            readingActivity.tv_foot.setText("0%");
        } else {
            try {
                BigDecimal b1 = new BigDecimal(Double.toString(GlobalConfig.chapternow));
                BigDecimal b2 = new BigDecimal(Double.toString(GlobalConfig.list.size()));
                BigDecimal b3 = new BigDecimal(100.00);
                BigDecimal progress = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
                progress = progress.multiply(b3);
                readingActivity.tv_foot.setText(progress + "%");
            } catch (Exception e) {
                Log.e("ReadPresenter", "计算进度百分比失败", e);
                readingActivity.tv_foot.setText("0%");
            }
        }

        // 切换页面 - 设置字体颜色
        try {
            int fontColorRes;
            if (!ReadConfig.isDark) {
                fontColorRes = R.color.default_font_color; // #696969
            } else {
                fontColorRes = R.color.dark_font_color;    // #808080
            }
            textPaint.setColor(readingActivity.getResources().getColor(fontColorRes));
        } catch (Resources.NotFoundException e) {
            // 如果资源找不到，使用硬编码颜色
            Log.e("ReadPresenter", "字体颜色资源未找到，使用默认黑色", e);
            textPaint.setColor(0xFF000000); // 黑色
        }

        // 创建或重用Bitmap
        if (GlobalConfig.mutableBitmap == null ||
                GlobalConfig.mutableBitmap.getWidth() != GlobalConfig.measuredWidth ||
                GlobalConfig.mutableBitmap.getHeight() != GlobalConfig.measuredHeigtt) {

            if (GlobalConfig.mutableBitmap != null && !GlobalConfig.mutableBitmap.isRecycled()) {
                GlobalConfig.mutableBitmap.recycle();
            }

            GlobalConfig.mutableBitmap = Bitmap.createBitmap(
                    GlobalConfig.measuredWidth,
                    GlobalConfig.measuredHeigtt,
                    Bitmap.Config.RGB_565
            );
        }

        // 设置背景色
        try {
            int bgColorRes;
            if (!ReadConfig.isDark) {
                bgColorRes = R.color.default_read_color; // #e6dbbf
            } else {
                bgColorRes = R.color.dark_read_color;    // #141820
            }
            GlobalConfig.mutableBitmap.eraseColor(readingActivity.getResources().getColor(bgColorRes));
        } catch (Resources.NotFoundException e) {
            // 如果资源找不到，使用硬编码颜色
            Log.e("ReadPresenter", "背景颜色资源未找到，使用默认米黄色", e);
            GlobalConfig.mutableBitmap.eraseColor(0xFFE6DBBF); // 米黄色
        }

        canvas = new Canvas(GlobalConfig.mutableBitmap);

        try {
            if (page == 0) {
                // 新章节起始，为标题
                String titleText = GlobalConfig.contentMap.get(page);
                if (titleText != null) {
                    canvas.drawText(titleText, 5,
                            (GlobalConfig.measuredHeigtt - GlobalConfig.mFontHeight) / 2, textPaint);
                }
            } else {
                String tmpstring = GlobalConfig.contentMap.get(page);
                if (tmpstring != null) {
                    String[] arrtmp = tmpstring.split("\n");
                    for (int i = 0; i < arrtmp.length; i++) {
                        canvas.drawText(arrtmp[i], 5,
                                ReadConfig.FontSize + GlobalConfig.mFontHeight * i, textPaint);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ReadPresenter", "绘制文本失败", e);
            canvas.drawText("内容加载失败", 50, 100, textPaint);
        }

        return GlobalConfig.mutableBitmap;
    }
}