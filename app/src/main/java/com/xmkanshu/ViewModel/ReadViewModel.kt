package com.xmkanshu.ViewModel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xmkanshu.Adapter.ChapterAdapter
import com.xmkanshu.Cache.BookContentCache
import com.xmkanshu.Data.GlobalConfig
import com.xmkanshu.Data.ReadConfig
import com.xmkanshu.Model.Chapter
import com.xmkanshu.R
import com.xmkanshu.Reptile.GetAndRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/**
 * 阅读页面的ViewModel，替代原来的ReadPresenter
 * 使用LiveData暴露状态，使用协程替代AsyncTask
 */
class ReadViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ReadViewModel"
    }

    // 章节加载状态
    private val _chapterLoadState = MutableLiveData<ChapterLoadState>()
    val chapterLoadState: LiveData<ChapterLoadState> = _chapterLoadState

    // 章节列表加载状态
    private val _chapterListState = MutableLiveData<ChapterListState>()
    val chapterListState: LiveData<ChapterListState> = _chapterListState

    // 当前页面Bitmap
    private val _currentPageBitmap = MutableLiveData<Bitmap?>()
    val currentPageBitmap: LiveData<Bitmap?> = _currentPageBitmap

    // 章节标题
    private val _chapterTitle = MutableLiveData<String>()
    val chapterTitle: LiveData<String> = _chapterTitle

    // 日夜模式切换事件（通知Activity更新UI样式）
    private val _styleChangedEvent = MutableLiveData<Boolean>()
    val styleChangedEvent: LiveData<Boolean> = _styleChangedEvent

    // 阅读进度
    private val _readingProgress = MutableLiveData<String>()
    val readingProgress: LiveData<String> = _readingProgress

    // GetAndRead实例
    private val getBook = GetAndRead(application)

    // Paint对象
    private val textPaint = Paint()

    // Canvas对象
    private var canvas: Canvas? = null

    /**
     * 加载章节内容
     * 使用协程在IO线程执行耗时操作，主线程更新UI
     * @param targetPage 加载完成后要显示的页码，默认0（标题页），传-1表示显示最后一页
     */
    fun loadChapterContent(targetPage: Int = 0) {
        Log.d(TAG, "loadChapterContent被调用, chapternow=${GlobalConfig.chapternow}, list.size=${GlobalConfig.list.size}, targetPage=$targetPage")

        viewModelScope.launch {
            _chapterLoadState.value = ChapterLoadState.Loading

            val startTime = System.currentTimeMillis()
            Log.d(TAG, "开始加载章节: ${GlobalConfig.chapternow}")

            try {
                // 在IO线程执行耗时操作
                val result = withContext(Dispatchers.IO) {
                    loadChapterContentInternal()
                }

                Log.d(TAG, "章节内容加载完成, contentMap.size=${GlobalConfig.contentMap.size}, title=${result.title}")

                // 根据targetPage设置最终页码
                GlobalConfig.Page = if (targetPage == -1) {
                    // -1 表示跳转到末尾（上一章最后一页正文）
                    (GlobalConfig.PageTotal - 1).coerceAtLeast(0)
                } else {
                    targetPage.coerceIn(0, (GlobalConfig.PageTotal - 1).coerceAtLeast(0))
                }

                // 更新UI
                _chapterTitle.value = result.title
                _chapterLoadState.value = ChapterLoadState.Success

                val totalTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "章节加载完成: ${GlobalConfig.chapternow}, 总耗时: ${totalTime}ms, 最终页码: ${GlobalConfig.Page}")

            } catch (e: Exception) {
                Log.e(TAG, "加载章节失败", e)
                _chapterLoadState.value = ChapterLoadState.Error(e.message ?: "未知错误")
            }
        }
    }

    /**
     * 调整字体大小后重新加载当前章节
     * 加载完成后自动修正页码并渲染，避免跳回标题页
     */
    fun reloadWithNewFontSize() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    loadChapterContentInternal()
                }

                _chapterTitle.value = result.title

                // 字体变大时页码可能超出新总页数，需要修正
                if (GlobalConfig.Page >= GlobalConfig.PageTotal) {
                    GlobalConfig.Page = GlobalConfig.PageTotal - 1
                }
                if (GlobalConfig.Page < 0) {
                    GlobalConfig.Page = 0
                }

                val bitmap = changePageContent(GlobalConfig.Page)
                _currentPageBitmap.postValue(bitmap)
                // 不触发 _chapterLoadState，避免 observer 跳回第0页

            } catch (e: Exception) {
                Log.e(TAG, "字体调整后加载失败", e)
            }
        }
    }

    /**
     * 加载章节内容的内部实现（在IO线程执行）
     */
    private fun loadChapterContentInternal(): ChapterResult {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "loadChapterContentInternal开始, chapternow=${GlobalConfig.chapternow}, list.size=${GlobalConfig.list.size}")

        // 1. 文本排版计算
        val layoutStartTime = System.currentTimeMillis()
        textPaint.textSize = ReadConfig.FontSize.toFloat()
        val fm = textPaint.fontMetrics
        GlobalConfig.mFontHeight = (Math.ceil((fm.descent - fm.top).toDouble()) + 2).toInt()
        GlobalConfig.mPageLineNum = GlobalConfig.measuredHeigtt / GlobalConfig.mFontHeight
        val layoutEndTime = System.currentTimeMillis()
        Log.d(TAG, "排版计算完成: mFontHeight=${GlobalConfig.mFontHeight}, mPageLineNum=${GlobalConfig.mPageLineNum}")

        // 2. 获取章节内容
        val cacheStartTime = System.currentTimeMillis()
        var content = ""
        GlobalConfig.contentMap.clear()

        try {
            if (GlobalConfig.chapternow >= 0 && GlobalConfig.chapternow < GlobalConfig.list.size) {
                val currentChapter = GlobalConfig.list[GlobalConfig.chapternow]
                Log.d(TAG, "获取章节内容: url=${currentChapter.url}, title=${currentChapter.title}")
                content = BookContentCache.getCache(currentChapter.url)
                Log.d(TAG, "章节内容长度: ${content.length}, 前100字: ${content.take(100)}")
            } else {
                Log.e(TAG, "chapternow out of bounds: ${GlobalConfig.chapternow}, list.size=${GlobalConfig.list.size}")
                content = "章节索引错误"
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, "获取章节内容失败", e)
            content = "获取内容失败"
        }
        val cacheEndTime = System.currentTimeMillis()

        // 3. 文本处理（分段、分行、分页）
        val processStartTime = System.currentTimeMillis()
        Log.d(TAG, "文本处理前content长度: ${content.length}")
        content = getBook.splitContentFirst(content)
        Log.d(TAG, "分段后content长度: ${content.length}")
        content = getBook.splitcontentSecond(content, ReadConfig.FontSize, GlobalConfig.measuredWidth)
        Log.d(TAG, "分行后content长度: ${content.length}")
        getBook.PageSet(content, GlobalConfig.mPageLineNum, GlobalConfig.contentMap)
        Log.d(TAG, "分页完成: contentMap.size=${GlobalConfig.contentMap.size}, PageTotal=${GlobalConfig.PageTotal}")
        val processEndTime = System.currentTimeMillis()

        // 4. 获取章节标题
        var title = "未知章节"
        try {
            if (GlobalConfig.chapternow >= 0 && GlobalConfig.chapternow < GlobalConfig.list.size) {
                title = GlobalConfig.list[GlobalConfig.chapternow].title
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取章节标题失败", e)
        }

        // 5. 输出性能日志
        val totalTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "章节加载详情: " +
                "总耗时=${totalTime}ms, " +
                "排版计算=${layoutEndTime - layoutStartTime}ms, " +
                "缓存获取=${cacheEndTime - cacheStartTime}ms, " +
                "文本处理=${processEndTime - processStartTime}ms")

        if (cacheEndTime - cacheStartTime > 100) {
            Log.d(TAG, "网络加载（缓存未命中）")
        } else {
            Log.d(TAG, "缓存加载（缓存命中）")
        }

        return ChapterResult(title, content)
    }

    /**
     * 绘制页面内容
     * 返回绘制好的Bitmap
     */
    fun changePageContent(page: Int): Bitmap? {
        val startTime = System.currentTimeMillis()

        // 更新进度
        updateReadingProgress()

        // 设置字体颜色
        val fontStartTime = System.currentTimeMillis()
        try {
            val fontColorRes = if (!ReadConfig.isDark) {
                R.color.default_font_color
            } else {
                R.color.dark_font_color
            }
            textPaint.color = getApplication<Application>().resources.getColor(fontColorRes)
        } catch (e: Exception) {
            Log.e(TAG, "字体颜色资源未找到，使用默认黑色", e)
            textPaint.color = 0xFF000000.toInt()
        }
        val fontEndTime = System.currentTimeMillis()

        // 创建或重用Bitmap
        val bitmapStartTime = System.currentTimeMillis()
        if (GlobalConfig.mutableBitmap == null ||
            GlobalConfig.mutableBitmap.width != GlobalConfig.measuredWidth ||
            GlobalConfig.mutableBitmap.height != GlobalConfig.measuredHeigtt) {

            if (GlobalConfig.mutableBitmap != null && !GlobalConfig.mutableBitmap.isRecycled) {
                GlobalConfig.mutableBitmap.recycle()
            }

            GlobalConfig.mutableBitmap = Bitmap.createBitmap(
                GlobalConfig.measuredWidth,
                GlobalConfig.measuredHeigtt,
                Bitmap.Config.RGB_565
            )
        }
        val bitmapEndTime = System.currentTimeMillis()

        // 设置背景色
        val bgStartTime = System.currentTimeMillis()
        try {
            val bgColorRes = if (!ReadConfig.isDark) {
                R.color.default_read_color
            } else {
                R.color.dark_read_color
            }
            GlobalConfig.mutableBitmap.eraseColor(getApplication<Application>().resources.getColor(bgColorRes))
        } catch (e: Exception) {
            Log.e(TAG, "背景颜色资源未找到，使用默认米黄色", e)
            GlobalConfig.mutableBitmap.eraseColor(0xFFE6DBBF.toInt())
        }
        val bgEndTime = System.currentTimeMillis()

        // 创建Canvas并绘制
        val canvasStartTime = System.currentTimeMillis()
        canvas = Canvas(GlobalConfig.mutableBitmap)

        val drawStartTime = System.currentTimeMillis()
        try {
            if (page == 0) {
                // 绘制章节标题（自动换行）
                val titleText = GlobalConfig.contentMap[page]
                if (titleText != null) {
                    try {
                        val titleLines = wrapText(titleText, textPaint, GlobalConfig.measuredWidth - 10f)
                        val totalHeight = titleLines.size * GlobalConfig.mFontHeight
                        val startY = (GlobalConfig.measuredHeigtt - totalHeight) / 2f + ReadConfig.FontSize
                        for (i in titleLines.indices) {
                            canvas?.drawText(titleLines[i], 5f, startY + i * GlobalConfig.mFontHeight, textPaint)
                        }
                    } catch (e: Exception) {
                        // wrapText 失败时回退到单行显示
                        Log.e(TAG, "标题换行失败，回退单行显示", e)
                        canvas?.drawText(titleText, 5f,
                            (GlobalConfig.measuredHeigtt - GlobalConfig.mFontHeight) / 2f + ReadConfig.FontSize, textPaint)
                    }
                }
            } else {
                // 绘制正文内容
                val tmpstring = GlobalConfig.contentMap[page]
                if (tmpstring != null) {
                    val arrtmp = tmpstring.split("\n")
                    for (i in arrtmp.indices) {
                        canvas?.drawText(arrtmp[i], 5f,
                            ReadConfig.FontSize + GlobalConfig.mFontHeight * i.toFloat(), textPaint)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "绘制文本失败", e)
            canvas?.drawText("内容加载失败", 50f, 100f, textPaint)
        }
        val drawEndTime = System.currentTimeMillis()

        // 输出性能日志
        val totalTime = System.currentTimeMillis() - startTime
        Log.d("PerformanceDetail", "一页绘制总耗时: ${totalTime}ms, " +
                "进度=${fontStartTime - startTime}ms, " +
                "字体=${fontEndTime - fontStartTime}ms, " +
                "Bitmap=${bitmapEndTime - bitmapStartTime}ms, " +
                "背景=${bgEndTime - bgStartTime}ms, " +
                "Canvas=${drawStartTime - canvasStartTime}ms, " +
                "绘制=${drawEndTime - drawStartTime}ms")

        return GlobalConfig.mutableBitmap
    }

    /**
     * 更新阅读进度
     */
    private fun updateReadingProgress() {
        if (GlobalConfig.chapternow == 0) {
            _readingProgress.postValue("0%")
        } else {
            try {
                val b1 = BigDecimal(GlobalConfig.chapternow.toString())
                val b2 = BigDecimal(GlobalConfig.list.size.toString())
                val b3 = BigDecimal(100.00)
                val progress = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP).multiply(b3)
                _readingProgress.postValue("$progress%")
            } catch (e: Exception) {
                Log.e(TAG, "计算进度百分比失败", e)
                _readingProgress.postValue("0%")
            }
        }
    }

    /**
     * 加载章节列表（使用协程替代AsyncTask）
     */
    fun loadChapterList() {
        viewModelScope.launch {
            _chapterListState.value = ChapterListState.Loading

            try {
                val adapter = withContext(Dispatchers.IO) {
                    ChapterAdapter(GlobalConfig.list, getApplication())
                }

                _chapterListState.value = ChapterListState.Success(adapter, GlobalConfig.chapternow)
            } catch (e: Exception) {
                Log.e(TAG, "加载章节列表失败", e)
                _chapterListState.value = ChapterListState.Error(e.message ?: "未知错误")
            }
        }
    }

    /**
     * 上一章
     */
    fun previousChapter() {
        if (GlobalConfig.chapternow > 0) {
            GlobalConfig.chapternow -= 1
            loadChapterContent(targetPage = -1)
            // targetPage=-1 表示跳转到上一章末尾，渲染由 observer 在加载完成后触发

            if (!ReadConfig.isDownload) {
                GetAndRead.ReadingBackground(GlobalConfig.chapternow)
            }
        }
    }

    /**
     * 下一章
     */
    fun nextChapter() {
        if (GlobalConfig.chapternow < GlobalConfig.list.size - 1) {
            GlobalConfig.chapternow += 1
            loadChapterContent() // 默认targetPage=0，显示标题页
            // 渲染由 _chapterLoadState observer 在加载完成后触发，避免竞态导致空白页

            if (!ReadConfig.isDownload) {
                GetAndRead.ReadingBackground(GlobalConfig.chapternow)
            }
        }
    }

    /**
     * 跳转到指定章节
     */
    fun seekToChapter(progress: Int) {
        if (GlobalConfig.list.size > 0) {
            GlobalConfig.chapternow = (GlobalConfig.list.size - 1) * progress / 100
            loadChapterContent() // 默认targetPage=0，显示标题页
            // 渲染由 _chapterLoadState observer 在加载完成后触发，避免竞态导致空白页
        }
    }

    /**
     * 切换日夜模式
     */
    fun toggleDayNight(styleCode: Int) {
        when (styleCode) {
            0 -> {
                ReadConfig.isDark = false
                ReadConfig.fontColor = R.color.default_font_color
                ReadConfig.bgColor = R.color.default_read_color
            }
            1 -> {
                ReadConfig.isDark = true
                ReadConfig.fontColor = R.color.dark_font_color
                ReadConfig.bgColor = R.color.dark_read_color
            }
        }

        // 重绘当前页
        val bitmap = changePageContent(GlobalConfig.Page)
        _currentPageBitmap.postValue(bitmap)

        // 通知Activity更新UI样式
        _styleChangedEvent.postValue(ReadConfig.isDark)
    }

    // 状态类
    sealed class ChapterLoadState {
        object Loading : ChapterLoadState()
        object Success : ChapterLoadState()
        data class Error(val message: String) : ChapterLoadState()
    }

    sealed class ChapterListState {
        object Loading : ChapterListState()
        data class Success(val adapter: ChapterAdapter, val selectedPosition: Int) : ChapterListState()
        data class Error(val message: String) : ChapterListState()
    }

    // 章节结果
    private data class ChapterResult(val title: String, val content: String)

    /**
     * 将长文本按指定宽度自动换行
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var start = 0
        for (j in 1..text.length) {
            if (paint.measureText(text, start, j) >= maxWidth) {
                lines.add(text.substring(start, j))
                start = j
            }
        }
        if (start < text.length) {
            lines.add(text.substring(start))
        }
        return lines
    }
}
