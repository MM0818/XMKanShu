package com.xmkanshu.Data;

import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 翻页性能监控工具类
 *
 * 分场景记录：
 * - FLIP_SAME_CHAPTER：同章翻页（contentMap 内存读取 + Canvas 绘制）
 * - FLIP_CROSS_CHAPTER：跨章翻页（缓存获取 + 文本处理 + 分页 + 绘制）
 *
 * 跨章场景分环节记录：缓存获取耗时、文本处理耗时、Canvas 绘制耗时
 * 聚合统计：P50（中位数）、P95（尾部延迟）、最大值、掉帧次数（>16ms）
 */
public class PagePerfTracker {

    private static final String TAG = "PagePerfTracker";
    private static final int BUFFER_SIZE = 100;
    private static final long SAME_CHAPTER_DROP_THRESHOLD_MS = 16;  // 同章翻页：一帧16ms（60fps）
    private static final long CROSS_CHAPTER_DROP_THRESHOLD_MS = 500; // 跨章翻页：含IO+文本处理，500ms为卡顿阈值

    public enum FlipScenario {
        SAME_CHAPTER,
        CROSS_CHAPTER
    }

    // 单例
    private static final PagePerfTracker INSTANCE = new PagePerfTracker();

    public static PagePerfTracker getInstance() {
        return INSTANCE;
    }

    // 环形数组存储最近 N 次翻页记录
    private final long[] sameChapterTimes = new long[BUFFER_SIZE];
    private final long[] crossChapterTimes = new long[BUFFER_SIZE];
    private final long[] cacheTimes = new long[BUFFER_SIZE];
    private final long[] processTimes = new long[BUFFER_SIZE];
    private final long[] drawTimes = new long[BUFFER_SIZE];

    private int sameIndex = 0;
    private int sameCount = 0;
    private int crossIndex = 0;
    private int crossCount = 0;

    // 跨章节翻页的分环节计时（在 startFlip 和 endFlip 之间由外部写入）
    private long pendingCacheTime = 0;
    private long pendingProcessTime = 0;
    private long pendingDrawTime = 0;

    // 本轮10次翻页涉及的书籍URL集合（用于日志定位）
    private final Set<String> batchBookUrls = new HashSet<>();

    private PagePerfTracker() {}

    /**
     * 记录跨章节翻页的分环节耗时（由 ReadViewModel.loadChapterContentInternal 调用）
     */
    public void recordSubStep(long cacheTime, long processTime) {
        this.pendingCacheTime = cacheTime;
        this.pendingProcessTime = processTime;
    }

    /**
     * 记录跨章节翻页的绘制耗时（由 ReadViewModel.changePageContent 调用）
     */
    public void recordDrawTime(long drawTime) {
        this.pendingDrawTime = drawTime;
    }

    /**
     * 记录一次翻页（总耗时由外部计算后传入）
     *
     * @param scenario   翻页场景
     * @param totalMs    总耗时（ms）
     */
    public void recordFlip(FlipScenario scenario, long totalMs) {
        // 记录本轮涉及的书籍URL
        String bookUrl = GlobalConfig.BookUrl;
        if (bookUrl != null && !bookUrl.isEmpty()) {
            batchBookUrls.add(bookUrl);
        }
        if (scenario == FlipScenario.SAME_CHAPTER) {
            sameChapterTimes[sameIndex] = totalMs;
            sameIndex = (sameIndex + 1) % BUFFER_SIZE;
            if (sameCount < BUFFER_SIZE) sameCount++;
        } else {
            crossChapterTimes[crossIndex] = totalMs;
            cacheTimes[crossIndex] = pendingCacheTime;
            processTimes[crossIndex] = pendingProcessTime;
            drawTimes[crossIndex] = pendingDrawTime;
            crossIndex = (crossIndex + 1) % BUFFER_SIZE;
            if (crossCount < BUFFER_SIZE) crossCount++;
            // 重置分环节计时
            pendingCacheTime = 0;
            pendingProcessTime = 0;
            pendingDrawTime = 0;
        }
    }

    /**
     * 获取统计信息
     */
    public PerfStats getStats(FlipScenario scenario) {
        if (scenario == FlipScenario.SAME_CHAPTER) {
            return computeStats(sameChapterTimes, sameCount, SAME_CHAPTER_DROP_THRESHOLD_MS);
        } else {
            return computeStats(crossChapterTimes, crossCount, CROSS_CHAPTER_DROP_THRESHOLD_MS);
        }
    }

    /**
     * 获取跨章翻页的分环节统计
     */
    public SubStepStats getSubStepStats() {
        int count = crossCount;
        if (count == 0) return new SubStepStats(0, 0, 0, 0, 0, 0);
        return new SubStepStats(
                computePercentile(cacheTimes, count, 50),
                computePercentile(cacheTimes, count, 95),
                computePercentile(processTimes, count, 50),
                computePercentile(processTimes, count, 95),
                computePercentile(drawTimes, count, 50),
                computePercentile(drawTimes, count, 95)
        );
    }

    /**
     * 输出统计日志（可在翻页 N 次后调用，或手动调用）
     */
    public void logStats() {
        PerfStats same = getStats(FlipScenario.SAME_CHAPTER);
        PerfStats cross = getStats(FlipScenario.CROSS_CHAPTER);
        SubStepStats sub = getSubStepStats();

        Log.d(TAG, "========== 翻页性能统计 ==========");
        for (String url : batchBookUrls) {
            String name = url.substring(url.lastIndexOf("/") + 1);
            Log.d(TAG, "  书籍: " + name + " | URL: " + url);
        }
        Log.d(TAG, String.format("[同章翻页] 样本=%d, P50=%dms, P95=%dms, Max=%dms, 卡顿(>%dms)=%d次",
                sameCount, same.p50, same.p95, same.max, SAME_CHAPTER_DROP_THRESHOLD_MS, same.dropCount));
        Log.d(TAG, String.format("[跨章翻页] 样本=%d, P50=%dms, P95=%dms, Max=%dms, 卡顿(>%dms)=%d次",
                crossCount, cross.p50, cross.p95, cross.max, CROSS_CHAPTER_DROP_THRESHOLD_MS, cross.dropCount));
        if (crossCount > 0) {
            Log.d(TAG, String.format("  缓存获取: P50=%dms, P95=%dms", sub.cacheP50, sub.cacheP95));
            Log.d(TAG, String.format("  文本处理: P50=%dms, P95=%dms", sub.processP50, sub.processP95));
            Log.d(TAG, String.format("  Canvas绘制: P50=%dms, P95=%dms", sub.drawP50, sub.drawP95));
        }
        Log.d(TAG, "===================================");
        batchBookUrls.clear();
    }

    /**
     * 重置所有统计数据
     */
    public void reset() {
        Arrays.fill(sameChapterTimes, 0);
        Arrays.fill(crossChapterTimes, 0);
        Arrays.fill(cacheTimes, 0);
        Arrays.fill(processTimes, 0);
        Arrays.fill(drawTimes, 0);
        sameIndex = 0;
        sameCount = 0;
        crossIndex = 0;
        crossCount = 0;
        batchBookUrls.clear();
    }

    private PerfStats computeStats(long[] data, int count, long dropThresholdMs) {
        if (count == 0) return new PerfStats(0, 0, 0, 0);
        long[] sorted = new long[count];
        System.arraycopy(data, 0, sorted, 0, count);
        Arrays.sort(sorted);
        long max = sorted[count - 1];
        int dropCount = 0;
        for (int i = 0; i < count; i++) {
            if (sorted[i] > dropThresholdMs) dropCount++;
        }
        return new PerfStats(
                sorted[count * 50 / 100],       // P50
                sorted[Math.min(count * 95 / 100, count - 1)], // P95
                max,
                dropCount
        );
    }

    private long computePercentile(long[] data, int count, int percentile) {
        if (count == 0) return 0;
        long[] sorted = new long[count];
        System.arraycopy(data, 0, sorted, 0, count);
        Arrays.sort(sorted);
        return sorted[Math.min(count * percentile / 100, count - 1)];
    }

    /**
     * 聚合统计数据
     */
    public static class PerfStats {
        public final long p50;
        public final long p95;
        public final long max;
        public final int dropCount;

        PerfStats(long p50, long p95, long max, int dropCount) {
            this.p50 = p50;
            this.p95 = p95;
            this.max = max;
            this.dropCount = dropCount;
        }
    }

    /**
     * 跨章翻页分环节统计数据
     */
    public static class SubStepStats {
        public final long cacheP50;
        public final long cacheP95;
        public final long processP50;
        public final long processP95;
        public final long drawP50;
        public final long drawP95;

        SubStepStats(long cacheP50, long cacheP95,
                     long processP50, long processP95,
                     long drawP50, long drawP95) {
            this.cacheP50 = cacheP50;
            this.cacheP95 = cacheP95;
            this.processP50 = processP50;
            this.processP95 = processP95;
            this.drawP50 = drawP50;
            this.drawP95 = drawP95;
        }
    }
}
