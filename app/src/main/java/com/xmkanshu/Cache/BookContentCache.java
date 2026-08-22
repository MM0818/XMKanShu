package com.xmkanshu.Cache;

import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.Manager.LocalBookParser;
import com.xmkanshu.Reptile.GetAndRead;

import java.util.concurrent.ConcurrentHashMap;


public class BookContentCache {
    //一、ConcurrentHashMap 线程安全缓存=================================================
    //1.使用ConcurrentHashMap实现线程安全=================================================
    /*
        - 线程安全 ：ConcurrentHashMap 是线程安全的，支持多线程并发读写
        - Key-Value 存储 ：Key 是章节 URL，Value 是章节内容
        - 静态变量 ：使用 static 确保全局唯一，整个应用共享同一个缓存。生命周期=应用的生命周期。
                   也就是书籍的整章的章节内容缓存是app启动的时候一直存在的，只要你阅读过那本书，再加上有进度记录，那么第二次打开后加载每章的每页内容就更快了
                   需要注意的是每章的分页内容是全局变量里的，每次打开一本书就会清空初始化一次（这是代码手动清空的具体在ReadingActivity的initContent()），和这个整章章节内容不同
        - 无锁读取 ：读操作不需要加锁，性能优于 Hashtable
    */ 
    private static ConcurrentHashMap<String, String> cacheMap = new ConcurrentHashMap<>();

    /**
     * 获取缓存的对象
     *
     * @param url
     * @return
     */
    //二、缓存策略实现=========================================================
    //1.缓存获取逻辑=============================================================
    public static String getCache(String url) {

//        url = getCacheKey(url);
        // 如果缓冲中有该链接，则返回value
        if (cacheMap.containsKey(url)) {
            return cacheMap.get(url);
        }
        // 如果缓存中没有该链接，把该帐号对象缓存到concurrentHashMap中
        initCache(url);  //从网络获取内容并缓存
        return cacheMap.get(url);
    }

    /**
     * 初始化缓存
     *
     * @param url
     */
    //二、2.缓存初始化=============================================================
    private static void initCache(String url) {
        // 从网络获取章节内容，原来是在这里调用GetBookContent方法啊，缓存没命中的时候。
        // 什么时候需要缓存呢，也就是进行预加载操作的时候，缓存没有命中，就调用这个爬虫方法了。
        //那么线程问题也来了，可以看到，GetAndRead的预加载缓存方法ReadingBackground，就是在主线程外的其他线程处理的，即当前线程为异步线程，网络获取当然也是异步处理的了
        // 而且因为是多线程并发调用，所以还需要考虑线程安全问题。
        // 解决方法：使用ConcurrentHashMap，它是线程安全的，支持多线程并发读写。

        String content;
        // 判断是否是本地书籍章节
        if (url != null && url.startsWith("local_chapter_")) {
            // 本地书籍：从本地文件读取章节内容
            content = getLocalChapterContent(url);
        } else {
            // 网络书籍：从网络获取
            content = GetAndRead.GetBookContent(url);
        }

        if(!content.isEmpty())
        {
            cacheMap.put(url, content);
        }
    }

    /**
     * 获取本地章节内容
     * @param chapterId 章节ID，格式为 "local_chapter_索引"
     * @return 章节内容
     */
    private static String getLocalChapterContent(String chapterId) {
        try {
            // 从章节ID中提取索引
            String indexStr = chapterId.replace("local_chapter_", "");
            int chapterIndex = Integer.parseInt(indexStr);

            // 获取本地文件路径
            String filePath = GlobalConfig.BookUrl;
            if (filePath == null || filePath.isEmpty()) {
                return "";
            }

            // 读取章节内容
            return LocalBookParser.getChapterContent(filePath, chapterIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 拼接一个缓存key
     *
     * @param url
     * @return
     */
//    private static String getCacheKey(String url) {
//        return Thread.currentThread().getId() + "-" + url;
//    }

    /**
     * 移除缓存信息
     *
     * @param url
     */
    public static void removeCache(String url) {
//        cacheMap.remove(getCacheKey(url));
        cacheMap.remove(url);
    }

    /**
     * 清除所有缓存
     *
     */

    public static void removeAll()
    {
        cacheMap.clear();
    }

    /**
     * 获取已缓存章节的平均内容大小（字节），供 PreloadConfig 估算单章内存占用
     *
     * @return 平均字节数，无缓存时返回 0
     */
    public static long getAverageContentSize() {
        if (cacheMap.isEmpty()) return 0;
        long totalBytes = 0;
        for (String content : cacheMap.values()) {
            if (content != null) {
                totalBytes += content.length() * 2; // Java char ≈ 2 bytes
            }
        }
        return totalBytes / cacheMap.size();
    }
}
