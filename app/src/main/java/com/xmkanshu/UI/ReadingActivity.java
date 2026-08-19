package com.xmkanshu.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
import android.widget.TextView;

import com.hb.dialog.dialog.LoadingDialog;
import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.Data.ReadConfig;
import com.xmkanshu.Manager.LocalBookParser;
import com.xmkanshu.Model.Chapter;
import com.xmkanshu.Presente.ReadPresenter;
import com.xmkanshu.R;
import com.xmkanshu.Reptile.GetAndRead;
import com.xmkanshu.ViewUitl.BatteryView;

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
    public ReadPresenter mReadPresenter;
    LoadingDialog loadingDialog;
    private long openStartTime;  // 添加成员变量记录开始时间指标

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

        new initReadTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

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
                    if (GlobalConfig.chapternow == GlobalConfig.list.size() - 1 && GlobalConfig.Page == GlobalConfig.PageTotal) { //本书最后一章最后一页
                        GlobalConfig.chapternow = GlobalConfig.list.size() - 1;
                        GlobalConfig.Page = GlobalConfig.Page - 1;
                    } else {
                        if (GlobalConfig.Page == GlobalConfig.PageTotal) {
                            /*
                             *本章末尾，切换章节标签，跳转下一章节
                             */
                            GlobalConfig.chapternow += 1;
                            mReadPresenter.LoadChapterContent();
                            GlobalConfig.Page = 0;
                            if (!ReadConfig.isDownload) {
                                GetAndRead.ReadingBackground(GlobalConfig.chapternow);
//                                Log.d("isdownload","2");
                            }
                        }
                    }

                    Log.d("PageSet", "Page=" + GlobalConfig.Page + "Cahapter:" + GlobalConfig.chapternow);
                    bitmap2 = mReadPresenter.changePageContent(GlobalConfig.Page);
                    GlobalConfig.SaveReadSetting(getApplicationContext());//保存阅读进度
                    tv_read.setImageBitmap(bitmap2);

                    //探索上面的翻页流畅度性能！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                    long duration = System.currentTimeMillis() - startTime;  // 计算耗时
                    Log.d("PagePerformance", "右翻页总耗时: " + duration + "ms, " + "章节: " + GlobalConfig.chapternow + ", 页码: " + GlobalConfig.Page);

                    try {
                        //bitmap.recycle();  //老版本这样写会卡
                        bitmap = null;
                        //System.gc();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    Toast.makeText(getApplicationContext(), "右：X="+event.getX()+"Y="+event.getY(), Toast.LENGTH_SHORT).show();
                }
                /*
                 *左侧翻页
                 */
                if (isLeft) {
                    long startTime = System.currentTimeMillis();  // 记录开始时间
                    GlobalConfig.Page = GlobalConfig.Page - 1;
                    if (GlobalConfig.Page < 0 && GlobalConfig.chapternow != 0) {
                        /*
                         *本章起始，切换章节标签，跳转上一章节
                         */
                        GlobalConfig.chapternow = GlobalConfig.chapternow - 1;
                        mReadPresenter.LoadChapterContent();
                        GlobalConfig.Page = GlobalConfig.PageTotal - 1;
                    } else if (GlobalConfig.Page <= 0 && GlobalConfig.chapternow == 0) {
                        GlobalConfig.chapternow = 0;
                        mReadPresenter.LoadChapterContent();
                        GlobalConfig.Page = 0;
                    }
                    Log.d("PageSet", "Page=" + GlobalConfig.Page + "Cahapter:" + GlobalConfig.chapternow);
                    bitmap = mReadPresenter.changePageContent(GlobalConfig.Page);
                    GlobalConfig.SaveReadSetting(getApplicationContext());//保存阅读进度
                    tv_read.setImageBitmap(bitmap);

                    //探索上面的翻页流畅度性能！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                    long duration = System.currentTimeMillis() - startTime;  // 计算耗时
                    Log.d("PagePerformance", "左翻页总耗时: " + duration + "ms, " + "章节: " + GlobalConfig.chapternow + ", 页码: " + GlobalConfig.Page);

                    try {
                        //bitmap2.recycle();  //老
                        bitmap2 = null;
                        //System.gc();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                /*
                 *中央
                 */
                if (isCenter) {
                    mReadPresenter.showSettingView();
//                    Toast.makeText(getApplicationContext(), "中：X="+event.getX()+"Y="+event.getY(), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


    }

    public void intStyle() {
        int bgColor = resolveColor(ReadConfig.bgColor);
        int fontColor = resolveColor(ReadConfig.fontColor);

        layout_title.setBackgroundColor(bgColor);

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
        //初始化数据
        //根据屏幕尺寸调整布局参数：确保在不同尺寸的屏幕上都能正常显示
        GlobalConfig.measuredWidth = GlobalConfig.screenWidth;//控件列宽度
        GlobalConfig.measuredHeigtt = GlobalConfig.screenHeight;//控件高度

        //原来每本书的某章的分页内容全局变量都是点开这本书之后一次性分好页并存的，再点下一本的时候会被清理掉，因为手写了清理的代码
        // 除了某章的全部内容会生命周期和app的一样，因为有单独实现一个BookContentCache类，hashmap是static类型的，也就是全局唯一，整个应用共享一个缓存
        if (GlobalConfig.list.size() > 0) {
            GlobalConfig.list.clear();
        }

        if (GlobalConfig.contentMap.size() > 0) {
            GlobalConfig.contentMap.clear();
        }

        GlobalConfig.GetReadSetting(getApplicationContext());//读取阅读进度

        // 判断是否是本地书籍
        if (isLocalBook(url)) {
            // 本地书籍：从本地文件解析章节
            List<Chapter> chapters = LocalBookParser.parseChapters(url);
            GlobalConfig.list.addAll(chapters);
        } else {
            // 网络书籍：从网络爬取
            if (!ReadConfig.isDownload) {//isDownload默认未下载，isDownload==true返回false
                GetAndRead.getChapter(url, chapternum);
                GetAndRead.ReadingBackground(GlobalConfig.chapternow);
            }
        }
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
    
    private class initReadTask extends AsyncTask<Void,Integer,Boolean>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            openStartTime = System.currentTimeMillis();  // 记录开始时间
            Log.d("BookOpen", "onPreExecute：开始加载书籍");

            loadingDialog.setMessage("加载中...");
            loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
            loadingDialog.setCanceledOnTouchOutside(false); // 点击加载框以外的区域
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            long taskStartTime = System.currentTimeMillis();  // 1. 任务开始计时
            Log.d("doInBackground", "开始异步初始化");

            if (!GlobalConfig.BookUrl.isEmpty()) {
                GlobalConfig.BookUrl = "";
            }
            Intent intent = getIntent();
            String bookUrl = intent.getStringExtra("link");
            GlobalConfig.chapternum=intent.getIntExtra("chapternum",0);

            // 新增日志：打印BookUrl的真实值（带引号，方便看是否有多余斜杠或空格）
            Log.d("URL_DEBUG", "BookUrl的值：\"" + GlobalConfig.BookUrl + "\"");

            String targetUrl;
            // 判断是否是本地书籍
            if (bookUrl != null && (bookUrl.startsWith("/") || bookUrl.contains("com.xmkanshu"))) {
                // 本地书籍：直接使用本地路径
                targetUrl = bookUrl;
                GlobalConfig.BookUrl = bookUrl;
                Log.d("URL_DEBUG", "本地书籍路径：\"" + targetUrl + "\"");
            } else {
                // 网络书籍：拼接域名
                if (bookUrl != null) {
                    bookUrl = bookUrl.replaceAll("^/+", ""); // 替换开头1个或多个/为空
                } else {
                    bookUrl = "";
                }
                GlobalConfig.BookUrl = bookUrl; // 更新全局变量
                targetUrl = "https://www.uuubqg.cc/" + bookUrl;
                Log.d("URL_DEBUG", "修复后最终URL：\"" + targetUrl + "\"");
            }

            ReadConfig.ReadSetting(ReadingActivity.this);

            // 2. 初始化 Presenter 耗时
            long presenterStartTime = System.currentTimeMillis();
            // 关键修正：传入新域名 targetUrl，不再拼接旧域名！
            initContent(targetUrl, GlobalConfig.chapternum);

            mReadPresenter = new ReadPresenter(ReadingActivity.this);
            long presenterEndTime = System.currentTimeMillis();

             // 3. 加载章节内容耗时
            long chapterStartTime = System.currentTimeMillis();
            mReadPresenter.LoadChapterContent();

            long chapterEndTime = System.currentTimeMillis();

            // 4. 首次渲染耗时
            long renderStartTime = System.currentTimeMillis();
            Bitmap firstPage = mReadPresenter.changePageContent(0);
            long renderEndTime = System.currentTimeMillis();

            long taskEndTime = System.currentTimeMillis();
            long totalDuration = taskEndTime - taskStartTime;

            Log.d("doInBackground",
                "异步初始化完成, （后台）总耗时: " + totalDuration + "ms, " +
                "Presenter初始化: " + (presenterEndTime - presenterStartTime) + "ms, " +
                "章节加载: " + (chapterEndTime - chapterStartTime) + "ms, " +
                "首次渲染: " + (renderEndTime - renderStartTime) + "ms");

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
//            mReadPresenter = new ReadPresenter(ReadingActivity.this);
            if (!ReadConfig.isDark) {
                intChapterStyle(R.color.default_read_color, R.color.default_font_color);
            } else {
                intChapterStyle(R.color.default_read_color, R.color.dark_font_color);
            }
//            mReadPresenter.LoadChapterContent();
            long totalDuration = System.currentTimeMillis() - openStartTime;  // 计算总耗时
            Log.d("BookOpen", "AsyncTask的从doInBackgrond到onPostExecute：书籍加载完成，用户感知总耗时: " + totalDuration + "ms");

            loadingDialog.dismiss();
            intStyle();
        }
    }
}