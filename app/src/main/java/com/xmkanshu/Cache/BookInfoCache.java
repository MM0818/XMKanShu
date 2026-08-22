package com.xmkanshu.Cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import com.xmkanshu.Data.BookInfo;
import com.xmkanshu.Reptile.GetBook;


public class BookInfoCache {

    private static final String TAG = "BookInfoCache";

    // 书籍信息缓存：key=书籍ID, value=BookInfo
    // 限制50条，每本BookInfo约1-2KB，总占用约100KB
    private static final LruCache<String, BookInfo> bookCache = new LruCache<String, BookInfo>(50) {
        @Override
        protected int sizeOf(String key, BookInfo value) {
            return 1; // 每条计为1，maxSize=50即最多50条
        }
    };

    // 封面图片缓存：key=图片URL, value=Bitmap
    // 限制为最大内存的1/16，封面图经过compressImage(JPEG quality=50)压缩后通常几十KB
    private static final LruCache<String, Bitmap> imageCache;

    static {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024); // KB
        int cacheSize = maxMemory / 16; // 约总内存的1/16
        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024; // 返回KB
            }
        };
        Log.d(TAG, "图片缓存初始化: maxMemory=" + (maxMemory / 1024) + "MB, imageCache=" + (cacheSize / 1024) + "MB");
    }

    /**
     * 加载书籍信息（带缓存）
     * 替代原来的 GlobalConfig.bookmap 无界 ConcurrentHashMap
     */
    public static BookInfo loadBook(String id) {
        Log.d("BookCache", "尝试加载书籍缓存，ID: " + id);

        // 先从 LruCache 查找
        BookInfo cached = bookCache.get(id);
        if (cached != null) {
            Log.d("BookCache", "图书缓存已命中:" + id);
            return cached;
        }

        // 缓存未命中，从网络获取
        Log.d("BookCache", "图书缓存未命中，网络获取:" + id);
        BookInfo book = GetBook.GetBookInfo(id);
        if (book != null) {
            bookCache.put(id, book);
        }
        return book;
    }

    /**
     * 加载封面图片（带缓存）
     * 注意：当前无活跃调用方，保留供后续使用
     */
    public static Bitmap loadImage(final String url, String dlink) {
        // 先从 LruCache 查找
        Bitmap cached = imageCache.get(url);
        if (cached != null) {
            Log.d("picCacheGet", "图片缓存已命中:" + url);
            return cached;
        }

        Log.d("picCacheGet", "图片缓存不存在，网络获取：" + url);
        try {
            java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) (new java.net.URL("http:" + dlink).openConnection());
            java.io.InputStream is = connection.getInputStream();
            Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                bitmap = com.xmkanshu.Data.BitmapUtils.compressImage(bitmap);
                if (bitmap != null) {
                    imageCache.put(url, bitmap);
                }
            }
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "图片加载失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 响应系统内存压力，清理缓存
     * 由 MyApplication.onTrimMemory() 调用
     *
     * @param level onTrimMemory 级别
     */
    public static void trimMemory(int level) {
        if (level >= 20) { // TRIM_MEMORY_UI_HIDDEN 及以上
            // 清空图片缓存（占内存最大）
            int imageEvicted = imageCache.size();
            imageCache.evictAll();
            Log.d(TAG, "trimMemory: 清空图片缓存, 释放约" + imageEvicted + "KB");
        }
        if (level >= 40) { // TRIM_MEMORY_BACKGROUND 及以上
            // 清空书籍信息缓存
            int bookEvicted = bookCache.size();
            bookCache.evictAll();
            Log.d(TAG, "trimMemory: 清空书籍信息缓存, 释放" + bookEvicted + "条");
        }
    }

    /**
     * 获取缓存统计信息（调试用）
     */
    public static String getCacheStats() {
        return "BookInfoCache[book=" + bookCache.size() + "/" + bookCache.maxSize()
                + ", image=" + imageCache.size() + "/" + imageCache.maxSize() + "KB]";
    }
}
