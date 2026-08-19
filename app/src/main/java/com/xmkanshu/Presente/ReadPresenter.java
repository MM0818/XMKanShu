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

//一、MVP架构实现=======================================================================================================
//1.架构设计===========================================================================================================
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

    /*
        - MVP 模式 ：Presenter 作为中介，连接 View（ReadingActivity）和 Model（GetAndRead、BookContentCache）
        - 职责分离 ：View 只负责 UI 显示，Presenter 处理业务逻辑，Model 负责数据获取和存储
        - 接口约束 ：实现 BasePresente 接口，定义统一的方法规范
    */
    public ReadPresenter(ReadingActivity readingActivity) {
        this.readingActivity = readingActivity;
        this.getBook = new GetAndRead(readingActivity); // 修复：在构造函数中初始化
    }

    //一、2.与View的交互===================================================================================================
    /*
        - 回调机制 ：通过匿名内部类实现点击事件回调
        - UI 操作委托 ：将 UI 相关操作委托给 View（ReadingActivity）处理
        - 参数传递 ：向 View 传递必要的数据和状态
    */
   /*
        5 个 new View.OnClickListener()，是因为 Java 没有"直接把函数当参数传" 的语法（Kotlin 可以），只能传一个实现了接口的对象。
        View.OnClickListener 是 Android 自带的单方法接口：new View.OnClickListener() { @Override public void onClick(View v) { ... } }
        = "当场造一个匿名对象，专门负责点按钮后的逻辑"。然后View那边调用这个方法传的组件对象参数按这个方法写的逻辑顺序去传就好了。比如
        第一个参数应该传“详细设置”的组件对象。
        架构方面：逻辑是 View 触发 → Presenter 处理 → View 更新。
   */
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
                 //执行器指定 ：使用 AsyncTask.SERIAL_EXECUTOR 确保任务串行执行。
                 // 用串行执行器不用并行的原因：章节列表需要按顺序显示，串行执行可以确保章节顺序正确，避免并发加载导致的章节列表混乱
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
                    //四、缓存机制=================================================================================
                    //2.预加载机制=================================================================================
                    if(!ReadConfig.isDownload && getBook != null)
                    {
                        /*
                            - 后台预加载 ：在阅读当前章节时，后台预加载下几章内容
                            - 减少等待 ：用户翻到下一章时，内容已加载完成，无需等待
                            - 网络优化 ：只在非下载模式下预加载，避免重复下载
                        */
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

    //二、异步任务处理===================================================================================================
    //1.AsyncTask【/ˈeɪˌsɪŋk/】的使用，章节列表加载==============================================================================================
    /**
     * 异步任务使用的三种类型（三个参数）如下：
        Params，即执行任务时发送给任务的参数类型。
        Progress后台计算期间发布的进度单元的类型。
        Result后台计算结果的类型。
    如果某种类型标记为未使用，声明为Void就行。
     */
    private class loadTask extends AsyncTask<Void,Integer,Boolean>  //异步任务类，用于在后台线程执行耗时操作，没指定执行器的话是默认并行线程池
    {
        //UI线程回调：在主线程执行，在UI线程执行前调用，用于初始化对话框等操作
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new LoadingDialog(readingActivity);   //一个”转圈等待“弹窗类
            loadingDialog.show();
        }

        //后台线程执行耗时操作，如加载章节列表、创建适配器
        @Override
        protected Boolean doInBackground(Void... voids) {
            //自动获取章节列表：章节列表数据已经存储在 GlobalConfig.list 中，这是一个全局变量，在书籍打开时已经加载完成。
            chapterAdapter = new ChapterAdapter(GlobalConfig.list, readingActivity);
            return null;
        }

        /*
            AsyncTask 自动通知主线程更新UI ：AsyncTask 内部封装了 Handler 机制，当 doInBackground() 执行完成后，
            会自动调用 onPostExecute() 方法，该方法在主线程执行。
         */

        //UI线程回调：在主线程执行，在doInBackground方法执行完成后调用，用于更新UI
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            if (listView != null && chapterAdapter != null) {
                //设置适配器
                listView.setAdapter(chapterAdapter);
                //设置默认选中项
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
        long loadStartTime = System.currentTimeMillis();  // 1. 开始总计时
        Log.d("LoadChapter", "开始加载章节: " + GlobalConfig.chapternow);

        if (readingActivity == null) {
            Log.e("ReadPresenter", "readingActivity is null!");
            return;
        }

        // 1. 文本排版计算耗时
        long layoutStartTime = System.currentTimeMillis();
        // 这部分计算可以在后台线程
        //三、2.文本排版计算============================================================================================
        //根据屏幕高度和字体高度计算每页显示的行数，确保在不同密度的屏幕上字体大小一致
        textPaint.setTextSize(ReadConfig.FontSize);
        Paint.FontMetrics fm = textPaint.getFontMetrics();   //字体测量 ：使用 Paint.FontMetrics 获取字体的 ascent、descent 等参数
        GlobalConfig.mFontHeight = (int) (Math.ceil(fm.descent - fm.top) + 2);  //字体高度 ：计算字体的高度，包括上坡度（ascent）、下坡度（descent）和额外的间距（2），**自适应布局 ：根据屏幕尺寸和字体大小动态调整排版
        GlobalConfig.mPageLineNum = (int) (GlobalConfig.measuredHeigtt / GlobalConfig.mFontHeight);  //排版计算 ：根据字体高度和屏幕高度计算每页显示的行数

        long layoutEndTime = System.currentTimeMillis();
        String content = "";
        GlobalConfig.contentMap.clear();
        
        // 2. 缓存获取耗时（关键）
        long cacheStartTime = System.currentTimeMillis();  // 2. 缓存获取开始
        //四、缓存机制===============================================================================================
        //1.章节内容缓存===========================================================================================
        try {
            if (GlobalConfig.chapternow >= 0 && GlobalConfig.chapternow < GlobalConfig.list.size()) {
                Chapter currentChapter = GlobalConfig.list.get(GlobalConfig.chapternow);  //拿到章节对象：通过当前章节数的索引取到arrayList
                //===四、缓存机制=================================================================================
                //本地缓存 ：使用 BookContentCache 缓存章节内容，减少网络请求
                //缓存键 ：使用章节 URL 作为缓存键，确保唯一性
                //离线阅读 ：缓存后无网络也能阅读已缓存章节
                content = BookContentCache.getCache(currentChapter.getUrl());   //再通过url拿到缓存里的章节内容
                //这样看来：ArrayList：通过当前章节数（作索引）拿到url；ConCurrentHashMap：通过url拿到章节的内容
            } else {
                Log.e("ReadPresenter", "chapternow out of bounds: " + GlobalConfig.chapternow);
                //容错处理 ：捕获异常，避免因缓存获取失败导致崩溃
                content = "章节索引错误";
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e("ReadPresenter", "获取章节内容失败", e);
            content = "获取内容失败";
        }
        long cacheEndTime = System.currentTimeMillis();

        // 3. 文本处理耗时（分段、分行、分页）
        long processStartTime = System.currentTimeMillis();

        content = getBook.splitContentFirst(content);//分段
        content = getBook.splitcontentSecond(content, ReadConfig.FontSize, GlobalConfig.measuredWidth);//段落分行
        
        getBook.PageSet(content, GlobalConfig.mPageLineNum, GlobalConfig.contentMap);//章节分页并把页数和该页的内容存入hashmap

        long processEndTime = System.currentTimeMillis();

        // 4. UI更新耗时
        long uiStartTime = System.currentTimeMillis();
        // UI更新移到主线程
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

        //二、2.主线程与后台线程切换=====================================================================================
        // 检查当前线程
        if (Looper.myLooper() == Looper.getMainLooper()) {  //使用 Looper.myLooper() 检查当前是否为主线程
            // 已经在主线程，直接更新
            if (readingActivity.tv_title != null) {  //检查 View 是否为 null，避免崩溃
                readingActivity.tv_title.setText(finalChapterTitle);
            }
        } else {  //UI 操作必须在主线程执行，否则会抛出异常
            // 在后台线程，需要切换到主线程
            readingActivity.runOnUiThread(new Runnable() {  //通过 runOnUiThread 切换到主线程执行 UI 更新
                @Override
                public void run() {
                    if (readingActivity.tv_title != null) {
                        readingActivity.tv_title.setText(finalChapterTitle);
                    }
                }
            });
        }

        long uiEndTime = System.currentTimeMillis();

        // 5. 输出章节加载性能日志
        long loadEndTime = System.currentTimeMillis();
        long totalDuration = loadEndTime - loadStartTime;
        
        Log.d("LoadChapter", 
            "章节加载完成: " + GlobalConfig.chapternow + 
            ", 总耗时: " + totalDuration + "ms, " +
            "排版计算: " + (layoutEndTime - layoutStartTime) + "ms, " +
            "缓存获取: " + (cacheEndTime - cacheStartTime) + "ms, " +
            "文本处理: " + (processEndTime - processStartTime) + "ms, " +
            "UI更新: " + (uiEndTime - uiStartTime) + "ms");
        
        // 判断加载类型
        if (cacheEndTime - cacheStartTime > 100) {
            Log.d("LoadChapter", "⚠️ 网络加载（缓存未命中）");
        } else {
            Log.d("LoadChapter", "✅ 缓存加载（缓存命中）");
        }
    }

    //三、自定义视图绘制=====================================================================================
    //1.Canvas和Bitmap绘制=====================================================================================
    /*
        - Bitmap 管理 ：创建、重用、回收 Bitmap，避免内存泄漏
        - Canvas 绘制 ：使用 Canvas 在 Bitmap 上绘制文本内容
        - 文本测量 ：通过 Paint.FontMetrics 计算字体高度和每页行数
        - 页面布局 ：手动计算文本位置，实现自定义排版
    */
    public Bitmap changePageContent(int page) {
        // 1. 开始总计时
        long totalStartTime = System.currentTimeMillis(); 

        if (readingActivity == null) {
            Log.e("ReadPresenter", "changePageContent: readingActivity is null!");
            return null;
        }

        // 设置阅读进度百分比
        long progressStartTime = System.currentTimeMillis();  // 2. 进度计算开始

        // 设置阅读进度百分比
        if (GlobalConfig.chapternow == 0) {
            readingActivity.tv_foot.setText("0%");
        } else {
            try {
                //  把“当前章节号”和“总章节数”变成 BigDecimal，防止普通 double 除法出现 33.333334 这种丑小数  
                BigDecimal b1 = new BigDecimal(Double.toString(GlobalConfig.chapternow));
                BigDecimal b2 = new BigDecimal(Double.toString(GlobalConfig.list.size()));
                //固定100，待会乘它变成百分数
                BigDecimal b3 = new BigDecimal(100.00);

                // 1. 先算小数：当前 / 总，保留 2 位小数，四舍五入
                // 例：30 / 100 → 0.30
                BigDecimal progress = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
                // 2. 再乘 100 变成百分数：0.30 × 100 → 30.00
                progress = progress.multiply(b3);
                // 3. 把 30.00 显示到底部 TextView
                readingActivity.tv_foot.setText(progress + "%");
            } catch (Exception e) {
                // 万一除零或空指针，打日志并保底显示 0%
                Log.e("ReadPresenter", "计算进度百分比失败", e);
                readingActivity.tv_foot.setText("0%");
            }
        }

        long progressEndTime = System.currentTimeMillis();  // 3. 进度计算结束

        // 切换页面 - 设置字体颜色
        long fontStartTime = System.currentTimeMillis();  // 4. 字体设置开始

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

        long fontEndTime = System.currentTimeMillis();  // 5. 字体设置结束

        // 创建或重用Bitmap
        long bitmapStartTime = System.currentTimeMillis();  // 6. Bitmap处理开始

        // 创建或重用Bitmap： 尺寸变了（屏幕旋转/字号大幅度变化） → 需要重新创建
        if (GlobalConfig.mutableBitmap == null ||
                GlobalConfig.mutableBitmap.getWidth() != GlobalConfig.measuredWidth ||
                GlobalConfig.mutableBitmap.getHeight() != GlobalConfig.measuredHeigtt) {

            // 回收旧的 Bitmap，避免内存泄漏（避免旧和新重叠）
            if (GlobalConfig.mutableBitmap != null && !GlobalConfig.mutableBitmap.isRecycled()) {
                GlobalConfig.mutableBitmap.recycle();  //回收
            }

            // 创建新的 Bitmap，尺寸与屏幕一致（是一个”可变画纸“，根据屏幕大小不同而变化的画纸）==================================
            GlobalConfig.mutableBitmap = Bitmap.createBitmap(
                    GlobalConfig.measuredWidth,
                    GlobalConfig.measuredHeigtt,
                    Bitmap.Config.RGB_565  //阅读不用透明，选 RGB_565 直接省一半内存
            );
        }

        long bitmapEndTime = System.currentTimeMillis();  // 7. Bitmap处理结束

        // 设置背景色
        long bgStartTime = System.currentTimeMillis();  // 8. 背景设置开始

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

        long bgEndTime = System.currentTimeMillis();  // 9. 背景设置结束

        // 创建 Canvas
        long canvasStartTime = System.currentTimeMillis();  // 10. Canvas创建开始

        // 创建 Canvas，将 Bitmap 作为绘制目标（将可变画纸“贴”到 画板Canvas 上）
        canvas = new Canvas(GlobalConfig.mutableBitmap);

        long canvasEndTime = System.currentTimeMillis();  // 11. Canvas创建结束

        // 绘制文本
        long drawStartTime = System.currentTimeMillis();  // 12. 绘制开始

        try {
            //绘制文本
            if (page == 0) {
                // 绘制章节标题：新章节起始，为标题
                String titleText = GlobalConfig.contentMap.get(page);
                if (titleText != null) {
                    canvas.drawText(titleText, 5,
                            (GlobalConfig.measuredHeigtt - GlobalConfig.mFontHeight) / 2, textPaint);
                }
            } else {
                //绘制正文内容
                String tmpstring = GlobalConfig.contentMap.get(page);
                //逐行写内容，x 固定 5 像素（左留白），y 是 基线 = 字号 + 行高×行号，写完一页把 Bitmap 直接给 ImageView 显示，无布局层级，翻页 60 fps
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

        long drawEndTime = System.currentTimeMillis();  // 13. 绘制结束

        long totalEndTime = System.currentTimeMillis();  // 14. 总计时结束
        long totalDuration = totalEndTime - totalStartTime;

        // 输出性能日志
        Log.d("PerformanceDetail", 
            "一页绘制总耗时（）changePageContent: " + totalDuration + "ms, " +
            "进度: " + (progressEndTime - progressStartTime) + "ms, " +
            "字体: " + (fontEndTime - fontStartTime) + "ms, " +
            "Bitmap: " + (bitmapEndTime - bitmapStartTime) + "ms, " +
            "背景: " + (bgEndTime - bgStartTime) + "ms, " +
            "Canvas: " + (canvasEndTime - canvasStartTime) + "ms, " +
            "绘制: " + (drawEndTime - drawStartTime) + "ms");

        return GlobalConfig.mutableBitmap;
    }
}