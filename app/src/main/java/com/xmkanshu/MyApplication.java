package com.xmkanshu;

import android.app.Application;
import android.util.Log;
import com.xmkanshu.Manager.BookDataManager;

/**
 * @author ZQZESS
 * @date 1/7/2021.
 * @file MyApplication
 * GitHub：https://github.com/zqzess
 * 不会停止运行的app不是好app w(ﾟДﾟ)w
 */
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
}
