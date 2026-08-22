package com.xmkanshu.Data;

import android.util.Log;

import com.xmkanshu.Cache.BookContentCache;

/**
 * 预加载章节数动态计算器
 *
 * 综合三个因素计算预加载章节数：
 * 1. 设备可用内存（Runtime.getRuntime().freeMemory()）
 * 2. 单章平均大小（基于已加载章节估算）
 * 3. 用户翻页速度（基于 PagePerfTracker 的翻页频率统计）
 *
 * 参考值：
 * - freeMemory < 20MB → 预加载 1 章（低内存保底）
 * - freeMemory < 50MB → 预加载 3 章（正常）
 * - freeMemory >= 50MB → 预加载 5 章（充足）
 *
 * 响应 onTrimMemory 动态缩减
 */
public class PreloadConfig {

    private static final String TAG = "PreloadConfig";

    // 内存阈值（MB）
    private static final long LOW_MEMORY_THRESHOLD_MB = 20;
    private static final long NORMAL_MEMORY_THRESHOLD_MB = 50;

    // 预加载数量边界
    private static final int MIN_PRELOAD = 1;
    private static final int MAX_PRELOAD = 5;

    // 单章内存占用估算上限（字节），超过此值认为章节偏大，减少预加载
    private static final long LARGE_CHAPTER_THRESHOLD_BYTES = 512 * 1024; // 500KB

    // 翻页速度阈值（ms/页），低于此值认为用户翻页快，增加预加载
    private static final long FAST_FLIP_THRESHOLD_MS = 3000; // 3秒/页

    // onTrimMemory 设置的强制缩减等级，0 表示无缩减
    private static volatile int trimLevel = 0;

    /**
     * 计算当前应该预加载的章节数
     *
     * @return 预加载章节数（1~5）
     */
    public static int calculatePreloadCount() {
        // 1. 基于内存计算基础值
        int baseCount = calculateByMemory();

        // 2. 基于翻页速度调整
        int adjustedCount = adjustByFlipSpeed(baseCount);

        // 3. 基于单章大小调整（避免加载过多大章节导致OOM）
        adjustedCount = adjustByChapterSize(adjustedCount);

        // 4. 应用 onTrimMemory 缩减
        if (trimLevel > 0) {
            adjustedCount = applyTrimLevel(adjustedCount, trimLevel);
        }

        // 5. 夹紧到合法范围
        adjustedCount = Math.max(MIN_PRELOAD, Math.min(MAX_PRELOAD, adjustedCount));

        // 详细日志：每一步的计算过程
        long freeMB = getFreeMemoryMB();
        long maxMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long totalMB = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long avgFlip = PagePerfTracker.getInstance().getAverageFlipInterval();
        long avgChapterBytes = BookContentCache.getAverageContentSize();

        Log.d(TAG, "========== 预加载计算 ==========");
        Log.d(TAG, "内存: 可用=" + freeMB + "MB, 已分配=" + totalMB + "MB, 最大=" + maxMB + "MB");
        Log.d(TAG, "基础值(内存决定): " + baseCount + "章 (可用内存<20MB→1, <50MB→3, >=50MB→5)");
        Log.d(TAG, "翻页速度: 平均间隔=" + avgFlip + "ms, " +
                (avgFlip > 0 ? (avgFlip < FAST_FLIP_THRESHOLD_MS ? "翻页快(+1)" : "正常(+0)") : "无数据(+0)"));
        Log.d(TAG, "章节大小: 平均=" + (avgChapterBytes / 1024) + "KB, " +
                (avgChapterBytes > LARGE_CHAPTER_THRESHOLD_BYTES ? "偏大(-1)" : "正常(+0)"));
        if (trimLevel > 0) {
            Log.d(TAG, "内存压力: onTrimMemory level=" + trimLevel + ", 缩减至=" + applyTrimLevel(adjustedCount, trimLevel));
        }
        Log.d(TAG, "最终预加载: " + adjustedCount + "章");
        Log.d(TAG, "================================");

        return adjustedCount;
    }

    /**
     * 基于设备可用内存计算基础预加载数
     */
    private static int calculateByMemory() {
        long freeMemoryMB = getFreeMemoryMB();

        if (freeMemoryMB < LOW_MEMORY_THRESHOLD_MB) {
            return 1;  // 低内存保底
        } else if (freeMemoryMB < NORMAL_MEMORY_THRESHOLD_MB) {
            return 3;  // 正常
        } else {
            return 5;  // 充足
        }
    }

    /**
     * 基于用户翻页速度调整预加载数
     * 翻页快 → 多预加载（用户读得快，需要提前准备更多章节）
     * 翻页慢 → 少预加载（用户读得慢，不需要提前加载太多）
     */
    private static int adjustByFlipSpeed(int baseCount) {
        long avgFlipInterval = PagePerfTracker.getInstance().getAverageFlipInterval();

        if (avgFlipInterval <= 0) {
            // 没有翻页数据，保持基础值
            return baseCount;
        }

        if (avgFlipInterval < FAST_FLIP_THRESHOLD_MS) {
            // 翻页快，增加 1 章
            return baseCount + 1;
        }
        // 翻页慢不减少（保持基础值即可，减少会导致体验变差）
        return baseCount;
    }

    /**
     * 基于单章平均大小调整预加载数
     * 如果已加载的章节平均占用较大，减少预加载数以控制总内存
     */
    private static int adjustByChapterSize(int baseCount) {
        long avgChapterBytes = BookContentCache.getAverageContentSize();

        if (avgChapterBytes <= 0) {
            // 没有缓存数据，保持基础值
            return baseCount;
        }

        if (avgChapterBytes > LARGE_CHAPTER_THRESHOLD_BYTES) {
            // 大章节，减少 1 章
            return baseCount - 1;
        }
        return baseCount;
    }

    /**
     * 应用 onTrimMemory 缩减
     *
     * level 含义（数值越大越严重）：
     *  5 = TRIM_MEMORY_RUNNING_MODERATE  — 应用在前台运行，系统内存中等紧张
     * 10 = TRIM_MEMORY_RUNNING_LOW       — 应用在前台运行，系统内存低
     * 15 = TRIM_MEMORY_RUNNING_CRITICAL  — 应用在前台运行，系统内存极低
     * 20 = TRIM_MEMORY_UI_HIDDEN         — Activity UI 不可见（弹 Dialog/切后台），不一定是内存紧张
     * 40 = TRIM_MEMORY_BACKGROUND        — 应用在后台，系统内存中等紧张
     * 60 = TRIM_MEMORY_MODERATE          — 应用在后台，系统内存低
     * 80 = TRIM_MEMORY_COMPLETE          — 应用在后台，系统内存极低，即将被杀
     *
     * @param count    当前计算值
     * @param level    onTrimMemory 的级别
     * @return 缩减后的值
     */
    private static int applyTrimLevel(int count, int level) {
        switch (level) {
            case 5:  // TRIM_MEMORY_RUNNING_MODERATE
                return Math.min(count, 4);
            case 10: // TRIM_MEMORY_RUNNING_LOW
                return Math.min(count, 3);
            case 15: // TRIM_MEMORY_RUNNING_CRITICAL
                return Math.min(count, 2);
            case 20: // TRIM_MEMORY_UI_HIDDEN — UI 不可见，温和缩减
                return Math.min(count, 3);
            case 40: // TRIM_MEMORY_BACKGROUND
                return Math.min(count, 2);
            case 60: // TRIM_MEMORY_MODERATE
            case 80: // TRIM_MEMORY_COMPLETE
                return MIN_PRELOAD;
            default:
                return count;
        }
    }

    /**
     * 设置 onTrimMemory 缩减等级（由 MyApplication 调用）
     */
    public static void setTrimLevel(int level) {
        trimLevel = level;
        Log.d(TAG, "onTrimMemory level=" + level + ", 预加载数调整为=" + calculatePreloadCount());
    }

    /**
     * 获取当前缩减等级
     */
    public static int getTrimLevel() {
        return trimLevel;
    }

    /**
     * 获取设备可用内存（MB）
     */
    private static long getFreeMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory());
        return freeMemory / (1024 * 1024);
    }
}
