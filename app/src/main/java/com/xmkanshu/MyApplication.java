package com.xmkanshu;

import android.app.Application;
import android.util.Log;
import com.xmkanshu.Data.PreloadConfig;
import com.xmkanshu.Manager.BookDataManager;


public class MyApplication extends Application {
    //Application类，提供全局上下文对象
    public static String TAG;
    public static MyApplication myApplication;

    public static MyApplication newInstance() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TAG = this.getClass().getSimpleName();
        myApplication = this;

        // 新增：应用启动时后台预加载书城数据
        Log.d(TAG, "应用启动，开始后台预加载书城数据");
        BookDataManager.getInstance().preloadBookCityData(this); // 传入Application上下文，避免内存泄漏

    }

    /**
     * 响应系统内存压力，动态缩减预加载策略
     * 由系统在内存紧张时回调，通知 PreloadConfig 降低预加载章节数
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory called, level=" + level);
        PreloadConfig.setTrimLevel(level);
    }
}
