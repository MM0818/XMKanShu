package com.xmkanshu.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.view.GravityCompat;

import com.hb.dialog.dialog.LoadingDialog;
import com.xmkanshu.Adapter.ChapterAdapter;
import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.Data.PagePerfTracker;
import com.xmkanshu.Data.ReadConfig;
import com.xmkanshu.Manager.LocalBookParser;
import com.xmkanshu.Model.Chapter;
import com.xmkanshu.Presente.DialogCreater;
import com.xmkanshu.R;
import com.xmkanshu.Reptile.GetAndRead;
import com.xmkanshu.ViewUitl.BatteryView;
import com.xmkanshu.ViewModel.ReadViewModel;

import java.util.List;

public class ReadingActivity extends AppCompatActivity {
    public TextView tv_title;
    public TextView tv_foot;
    public TextView tv_battery_valuel;
    public ImageView tv_read;
    public LinearLayout linearLayout;
    public LinearLayout layout_title;
    public LinearLayout layout_foot;
    public DrawerLayout drawerLayout;
    public TextView tv_book_chapter;
    public TextView tv_chapter_sort;
    public LinearLayout layout_read_chapter_list_view;

    Bitmap bitmap;
    Bitmap bitmap2;
    private BatteryView mBatteryView;
    private ReadViewModel readViewModel;
    LoadingDialog loadingDialog;
    private long openStartTime;  // 添加成员变量记录开始时间指标
    private boolean pendingChapterLoad = false; // 跨章节翻页时标记，等待observer渲染
    private long flipStartTime = 0;             // 跨章节翻页起始时间（用于PagePerfTracker）
    private boolean initialLoadDone = false;    // 区分初始加载和后续章节切换
    private int flipCount = 0;                  // 翻页计数，每10次输出统计日志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long openStartTime = System.currentTimeMillis();  // 开始计时
        Log.d("BookOpen", "开始打开书籍");

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_read);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏android系统的状态栏
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

        //动态获取屏幕尺寸
        GlobalConfig.screenWidth = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽
        GlobalConfig.screenHeight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高

        loadingDialog=new LoadingDialog(this);
        findId();

        // 初始化ViewModel
        readViewModel = new ViewModelProvider(this).get(ReadViewModel.class);
        observeViewModel();

        // 使用协程替代AsyncTask
        initBookWithCoroutines();

        //注册电量广播接收器以获取电量信息
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);

        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Boolean isRight = event.getX() > (GlobalConfig.screenWidth / 3 * 2);   //如果用户点击右侧空白区域，即往前翻页
                Boolean isLeft = event.getX() < (GlobalConfig.screenWidth / 3); //左
                Boolean isCenter = event.getX() > (GlobalConfig.screenWidth / 3) && event.getX() < (GlobalConfig.screenWidth / 3 * 2);  //中间
                if (isRight) {
                    long startTime = System.currentTimeMillis();  // 记录开始时间

                    GlobalConfig.Page = GlobalConfig.Page + 1;  //阅读页码数+1
                    pendingChapterLoad = false;
                    if (GlobalConfig.chapternow == GlobalConfig.list.size() - 1 && GlobalConfig.Page == GlobalConfig.PageTotal) { //本书最后一章最后一页
                        GlobalConfig.chapternow = GlobalConfig.list.size() - 1;
                        GlobalConfig.Page = GlobalConfig.Page - 1;
                    } else {
                        if (GlobalConfig.Page == GlobalConfig.PageTotal) {
                            /*
                             *本章末尾，切换章节标签，跳转下一章节
                             */
                            GlobalConfig.chapternow += 1;
                            readViewModel.loadChapterContent(0);
                            pendingChapterLoad = true;
                            if (!ReadConfig.isDownload) {
                                GetAndRead.ReadingBackground(GlobalConfig.chapternow);
                            }
                        }
                    }

                    Log.d("PageSet", "Page=" + GlobalConfig.Page + "Cahapter:" + GlobalConfig.chapternow);
                    if (!pendingChapterLoad) {
                        // 同章内翻页，contentMap已就绪，直接渲染
                        bitmap2 = readViewModel.changePageContent(GlobalConfig.Page);
                        tv_read.setImageBitmap(bitmap2);
                        long duration = System.currentTimeMillis() - startTime;
                        PagePerfTracker.getInstance().recordFlip(PagePerfTracker.FlipScenario.SAME_CHAPTER, duration);
                        logFlipStats();
                    } else {
                        // 跨章节：记录起始时间，observer 完成后统计
                        flipStartTime = startTime;
                    }
                    // 跨章节时由 chapterLoadState observer 在加载完成后渲染，避免空白页
                    GlobalConfig.SaveReadSetting(getApplicationContext());//保存阅读进度

                    try {
                        bitmap = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                /*
                 *左侧翻页
                 */
                if (isLeft) {
                    long startTime = System.currentTimeMillis();  // 记录开始时间
                    GlobalConfig.Page = GlobalConfig.Page - 1;
                    pendingChapterLoad = false;
                    if (GlobalConfig.Page < 0 && GlobalConfig.chapternow != 0) {
                        /*
                         *本章起始，切换章节标签，跳转上一章节末尾
                         */
                        GlobalConfig.chapternow = GlobalConfig.chapternow - 1;
                        readViewModel.loadChapterContent(-1);
                        pendingChapterLoad = true;
                    } else if (GlobalConfig.Page <= 0 && GlobalConfig.chapternow == 0) {
                        GlobalConfig.chapternow = 0;
                        readViewModel.loadChapterContent(0);
                        pendingChapterLoad = true;
                    }
                    Log.d("PageSet", "Page=" + GlobalConfig.Page + "Cahapter:" + GlobalConfig.chapternow);
                    if (!pendingChapterLoad) {
                        // 同章内翻页，contentMap已就绪，直接渲染
                        bitmap = readViewModel.changePageContent(GlobalConfig.Page);
                        tv_read.setImageBitmap(bitmap);
                        long duration = System.currentTimeMillis() - startTime;
                        PagePerfTracker.getInstance().recordFlip(PagePerfTracker.FlipScenario.SAME_CHAPTER, duration);
                        logFlipStats();
                    } else {
                        // 跨章节：记录起始时间，observer 完成后统计
                        flipStartTime = startTime;
                    }
                    // 跨章节时由 chapterLoadState observer 在加载完成后渲染，避免空白页
                    GlobalConfig.SaveReadSetting(getApplicationContext());//保存阅读进度

                    try {
                        bitmap2 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                /*
                 *中央
                 */
                if (isCenter) {
                    showSettingView();
                }
                return false;
            }
        });


    }

    /**
     * 观察ViewModel的状态变化
     */
    private void observeViewModel() {
        // 观察章节标题
        readViewModel.getChapterTitle().observe(this, title -> {
            if (tv_title != null) {
                tv_title.setText(title);
            }
        });

        // 观察阅读进度
        readViewModel.getReadingProgress().observe(this, progress -> {
            if (tv_foot != null) {
                tv_foot.setText(progress);
            }
        });

        // 观察页面Bitmap
        readViewModel.getCurrentPageBitmap().observe(this, bitmap -> {
            if (bitmap != null && tv_read != null) {
                tv_read.setImageBitmap(bitmap);
            }
        });

        // 观察日夜模式切换
        readViewModel.getStyleChangedEvent().observe(this, isDark -> {
            intStyle();
        });
    }

    /**
     * 使用协程初始化书籍（替代AsyncTask）
     */
    private void initBookWithCoroutines() {
        openStartTime = System.currentTimeMillis();
        Log.d("BookOpen", "开始加载书籍");

        loadingDialog.setMessage("加载中...");
        loadingDialog.setCancelable(true);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        // 获取Intent中的书籍信息
        Intent intent = getIntent();
        String bookUrl = intent.getStringExtra("link");
        int chapternum = intent.getIntExtra("chapternum", 0);

        // 处理URL
        String targetUrl;
        if (bookUrl != null && (bookUrl.startsWith("/") || bookUrl.contains("com.xmkanshu"))) {
            // 本地书籍
            targetUrl = bookUrl;
            GlobalConfig.BookUrl = bookUrl;
        } else {
            // 网络书籍
            if (bookUrl != null) {
                bookUrl = bookUrl.replaceAll("^/+", "");
            } else {
                bookUrl = "";
            }
            GlobalConfig.BookUrl = bookUrl;
            targetUrl = "https://www.uuubqg.cc/" + bookUrl;
        }

        // 读取设置
        ReadConfig.ReadSetting(this);

        // 初始化章节列表（关键！）
        Log.d("BookOpen", "initContent前: GlobalConfig.list.size=" + GlobalConfig.list.size());
        initContent(targetUrl, chapternum);
        Log.d("BookOpen", "initContent后: GlobalConfig.list.size=" + GlobalConfig.list.size() +
                ", chapternow=" + GlobalConfig.chapternow +
                ", Page=" + GlobalConfig.Page +
                ", measuredWidth=" + GlobalConfig.measuredWidth +
                ", measuredHeigtt=" + GlobalConfig.measuredHeigtt);

        // 使用ViewModel的协程加载章节内容，传入保存的页码以恢复阅读位置
        int savedPage = GlobalConfig.Page;
        readViewModel.loadChapterContent(savedPage);

        // 观察加载状态
        readViewModel.getChapterLoadState().observe(this, state -> {
            if (state instanceof ReadViewModel.ChapterLoadState.Success) {
                long totalDuration = System.currentTimeMillis() - openStartTime;
                Log.d("BookOpen", "书籍加载完成，用户感知总耗时: " + totalDuration + "ms");

                // 绘制当前页（由loadChapterContent的targetPage决定是第0页还是末尾页）
                Bitmap firstPage = readViewModel.changePageContent(GlobalConfig.Page);
                if (firstPage != null && tv_read != null) {
                    tv_read.setImageBitmap(firstPage);
                }

                if (initialLoadDone && flipStartTime > 0) {
                    // 非初始加载：记录跨章节翻页耗时
                    long crossDuration = System.currentTimeMillis() - flipStartTime;
                    PagePerfTracker.getInstance().recordFlip(PagePerfTracker.FlipScenario.CROSS_CHAPTER, crossDuration);
                    flipStartTime = 0;
                    logFlipStats();
                }
                initialLoadDone = true;

                loadingDialog.dismiss();
                intStyle();
            } else if (state instanceof ReadViewModel.ChapterLoadState.Error) {
                loadingDialog.dismiss();
                Log.e("BookOpen", "加载失败: " + ((ReadViewModel.ChapterLoadState.Error) state).getMessage());
            }
        });
    }

    public void intStyle() {
        int bgColor = resolveColor(ReadConfig.bgColor);
        int fontColor = resolveColor(ReadConfig.fontColor);

        layout_title.setBackgroundColor(bgColor);
        layout_foot.setBackgroundColor(bgColor);
        linearLayout.setBackgroundColor(bgColor);

        tv_title.setTextColor(fontColor);
        tv_foot.setTextColor(fontColor);
        tv_battery_valuel.setTextColor(fontColor);
    }

    private int resolveColor(int resId) {
        try {
            // 如果是 color 资源
            return ContextCompat.getColor(this, resId);
        } catch (Exception e) {
            // 如果是 attr
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(resId, typedValue, true);
            return typedValue.data;
        }
    }



    public void intChapterStyle(int color, int fontColor) {
        layout_read_chapter_list_view.setBackgroundResource(color);
        tv_chapter_sort.setTextColor(getResources().getColor(fontColor));
        tv_book_chapter.setTextColor(getResources().getColor(fontColor));
    }

    private void findId() {
        tv_title = findViewById(R.id.tv_title);
        tv_foot = findViewById(R.id.tv_foot);
        tv_read = findViewById(R.id.tv_read);
        tv_battery_valuel = findViewById(R.id.tv_battery_value);
        linearLayout = findViewById(R.id.layout_read);
        layout_title = findViewById(R.id.layout_read_title);
        layout_foot = findViewById(R.id.layout_read_foot);
        mBatteryView = (BatteryView) findViewById(R.id.tv_battery);
        drawerLayout = findViewById(R.id.dl_read_activity);
        tv_book_chapter = findViewById(R.id.tv_book_chapter);
        tv_chapter_sort = findViewById(R.id.tv_chapter_sort);
        layout_read_chapter_list_view = findViewById(R.id.layout_read_chapter_list_view);
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取当前电量，如未获取具体数值，则默认为0
            int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            //获取最大电量，如未获取到具体数值，则默认为100
            int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            //显示电量
            tv_battery_valuel.setText((batteryLevel * 100 / batteryScale) + " % ");
        }
    };

    public void initContent(String url, int chapternum) {
        Log.d("BookOpen", "initContent开始: url=" + url + ", chapternum=" + chapternum);

        //初始化数据
        //根据屏幕尺寸调整布局参数：确保在不同尺寸的屏幕上都能正常显示
        GlobalConfig.measuredWidth = GlobalConfig.screenWidth;//控件列宽度
        GlobalConfig.measuredHeigtt = GlobalConfig.screenHeight;//控件高度
        Log.d("BookOpen", "屏幕尺寸: screenWidth=" + GlobalConfig.screenWidth + ", screenHeight=" + GlobalConfig.screenHeight);

        //原来每本书的某章的分页内容全局变量都是点开这本书之后一次性分好页并存的，再点下一本的时候会被清理掉，因为手写了清理的代码
        // 除了某章的全部内容会生命周期和app的一样，因为有单独实现一个BookContentCache类，hashmap是static类型的，也就是全局唯一，整个应用共享一个缓存
        if (GlobalConfig.list.size() > 0) {
            GlobalConfig.list.clear();
        }

        if (GlobalConfig.contentMap.size() > 0) {
            GlobalConfig.contentMap.clear();
        }

        GlobalConfig.GetReadSetting(getApplicationContext());//读取阅读进度
        Log.d("BookOpen", "读取阅读进度: chapternow=" + GlobalConfig.chapternow + ", Page=" + GlobalConfig.Page);

        // 清除上一本书的解析缓存
        LocalBookParser.clearCache();

        // 判断是否是本地书籍
        if (isLocalBook(url)) {
            // 本地书籍：从本地文件解析章节
            Log.d("BookOpen", "本地书籍，开始解析章节");
            List<Chapter> chapters = LocalBookParser.parseChapters(url);
            GlobalConfig.list.addAll(chapters);
            Log.d("BookOpen", "本地书籍解析完成: 章节数=" + chapters.size());
            GetAndRead.ReadingBackground(GlobalConfig.chapternow);
        } else {
            // 网络书籍：从网络爬取
            Log.d("BookOpen", "网络书籍，开始爬取章节");
            if (!ReadConfig.isDownload) {//isDownload默认未下载，isDownload==true返回false
                GetAndRead.getChapter(url, chapternum);
                Log.d("BookOpen", "爬取完成: GlobalConfig.list.size=" + GlobalConfig.list.size());
                GetAndRead.ReadingBackground(GlobalConfig.chapternow);
            }
        }
        Log.d("BookOpen", "initContent结束: GlobalConfig.list.size=" + GlobalConfig.list.size());
    }

    /**
     * 判断是否是本地书籍
     * 本地书籍的link是文件路径（以/开头或包含应用包名）
     */
    private boolean isLocalBook(String url) {
        if (url == null) return false;
        // 本地路径特征：以/开头，或者是应用私有目录路径
        return url.startsWith("/") || url.contains("com.xmkanshu");
    }

    /**
     * 显示设置对话框（替代原ReadPresenter.showSettingView）
     */
    public void showSettingView() {
        DialogCreater.createReadSetting(this, readViewModel,
                v -> showSettingDetailView(),
                v -> {
                    // 目录按钮点击：打开抽屉并加载章节列表
                    drawerLayout.openDrawer(GravityCompat.START);
                    loadChapterList();
                },
                v -> readViewModel.previousChapter(),
                v -> readViewModel.nextChapter(),
                new android.widget.SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                        readViewModel.seekToChapter(progress);
                    }
                    @Override
                    public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
                });
    }

    /**
     * 加载章节列表到目录
     */
    private void loadChapterList() {
        Log.d("BookOpen", "loadChapterList: GlobalConfig.list.size=" + GlobalConfig.list.size());
        ListView listView = findViewById(R.id.listview_chapter_list);
        if (listView != null && GlobalConfig.list.size() > 0) {
            ChapterAdapter chapterAdapter = new ChapterAdapter(GlobalConfig.list, this);
            listView.setAdapter(chapterAdapter);
            // 设置默认选中项
            if (GlobalConfig.chapternow >= 5) {
                listView.setSelection(GlobalConfig.chapternow - 5);
            } else {
                listView.setSelection(0);
            }
            Log.d("BookOpen", "章节列表加载完成");
        } else {
            Log.e("BookOpen", "listView为空或章节列表为空");
        }
    }

    /**
     * 每10次翻页输出一次性能统计日志
     */
    private void logFlipStats() {
        flipCount++;
        if (flipCount % 10 == 0) {
            PagePerfTracker.getInstance().logStats();
        }
    }

    /**
     * 显示详细设置对话框
     */
    public void showSettingDetailView() {
        DialogCreater.createReadDetailSetting(this, readViewModel);
    }

    /**
     * 加载章节内容（供ChapterAdapter调用）
     */
    public void loadChapterContent() {
        readViewModel.loadChapterContent(0);
    }

    /**
     * 更新页面显示（供ChapterAdapter调用）
     */
    public void updatePageDisplay() {
        Bitmap bitmap = readViewModel.changePageContent(GlobalConfig.Page);
        if (bitmap != null && tv_read != null) {
            tv_read.setImageBitmap(bitmap);
        }
    }
}