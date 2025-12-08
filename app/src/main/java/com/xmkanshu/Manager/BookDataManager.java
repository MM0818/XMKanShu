package com.xmkanshu.Manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.xmkanshu.Data.BookInfo;
import com.xmkanshu.Data.BookStoreData;
import com.xmkanshu.Reptile.GetAndRead;
import com.xmkanshu.Reptile.GetBook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookDataManager {
    private static final String TAG = "BookDataManager";
    private static BookDataManager sInstance;
    private BookStoreData mBookCityCache;
    private ExecutorService mExecutorService;
    private Handler mMainHandler;
    private boolean isPreloadCompleted = false;
    public interface OnBookDataLoadListener {
        void onLoadCompleted(BookStoreData data);
        void onLoadFailed(String error);
    }
    private OnBookDataLoadListener mLoadListener; // 正确定义成员变量

    private BookDataManager() {
        mExecutorService = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
        mBookCityCache = new BookStoreData();
    }

    public static BookDataManager getInstance() {
        if (sInstance == null) {
            synchronized (BookDataManager.class) {
                if (sInstance == null) {
                    sInstance = new BookDataManager();
                }
            }
        }
        return sInstance;
    }

    public void preloadBookCityData(Context context) {
        if (isPreloadCompleted) {
            Log.d(TAG, "书城数据已预加载完成，无需重复加载");
            return;
        }
        mExecutorService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "开始后台爬取书城数据（GetAndRead）");
                    // 调用原有数据获取方法
                    BookStoreData bookStoreData = GetAndRead.getBookStoreData("https://www.uuubqg.cc/");
                    if (bookStoreData != null) {
                        mBookCityCache = bookStoreData; // 直接缓存整个BookStoreData对象
                        isPreloadCompleted = true;
                        Log.d(TAG, "书城数据预加载完成");
                        notifyLoadCompleted();
                    } else {
                        throw new Exception("GetAndRead返回null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "书城数据预加载失败: " + e.getMessage(), e);
                    notifyLoadFailed(e.getMessage());
                }
            }
        }); // 修复：闭合submit方法的参数
    } // 修复：闭合preloadBookCityData方法

    // 新增：转换HashMap列表到BookInfo列表（解决API 21兼容问题）
    private ArrayList<BookInfo> convertToBookInfoList(ArrayList<HashMap<String, String>> mapList) {
        ArrayList<BookInfo> bookInfoList = new ArrayList<>();
        if (mapList == null || mapList.isEmpty()) {
            return bookInfoList;
        }
        for (HashMap<String, String> map : mapList) {
            try {
                // 替换getOrDefault为兼容写法（API 21+）
                String name = map.get("name") == null ? "" : map.get("name");
                String author = map.get("author") == null ? "" : map.get("author");
                String link = map.get("link") == null ? "" : map.get("link");
                String picname = map.get("picname") == null ? "" : map.get("picname");
                String piclink = map.get("piclink") == null ? "" : map.get("piclink");
                String info = map.get("info") == null ? "" : map.get("info");
                String lasttime = map.get("lasttime") == null ? "" : map.get("lasttime");
                String newchapter = map.get("newchapter") == null ? "" : map.get("newchapter");
                String newchapterlink = map.get("newchapterlink") == null ? "" : map.get("newchapterlink");
                int chapternum = 0;
                try {
                    String chapternumStr = map.get("chapternum") == null ? "0" : map.get("chapternum");
                    chapternum = Integer.parseInt(chapternumStr);
                } catch (NumberFormatException e) {
                    chapternum = 0;
                }
                // 创建BookInfo对象
                BookInfo bookInfo = new BookInfo(
                        name, author, link, picname, piclink,
                        info, lasttime, newchapter, newchapterlink, chapternum
                );
                bookInfoList.add(bookInfo);
            } catch (Exception e) {
                Log.w(TAG, "转换HashMap到BookInfo失败: " + e.getMessage());
                continue;
            }
        }
        return bookInfoList;
    }

    public BookStoreData getBookCityCache() {
        return isPreloadCompleted ? mBookCityCache : null;
    }

    public void setOnBookDataLoadListener(OnBookDataLoadListener listener) {
        this.mLoadListener = listener;
        if (isPreloadCompleted) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mLoadListener != null) {
                        mLoadListener.onLoadCompleted(mBookCityCache);
                    }
                }
            });
        }
    }

    public void removeOnBookDataLoadListener() {
        this.mLoadListener = null;
    }

    private void notifyLoadCompleted() {
        if (mLoadListener != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLoadListener.onLoadCompleted(mBookCityCache);
                }
            });
        }
    }

    private void notifyLoadFailed(String error) {
        if (mLoadListener != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLoadListener.onLoadFailed(error);
                }
            });
        }
    }

    public void release() {
        if (mExecutorService != null && !mExecutorService.isShutdown()) {
            mExecutorService.shutdown();
        }
        mLoadListener = null;
        mBookCityCache = null;
        isPreloadCompleted = false;
    }
}