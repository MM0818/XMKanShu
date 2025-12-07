package com.xmkanshu.Reptile;

import android.content.Context;
import android.text.TextPaint;
import android.util.Log;

import com.xmkanshu.Cache.BookContentCache;
import com.xmkanshu.Data.BookInfo;
import com.xmkanshu.Data.BookStoreData;
import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.Model.Chapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZQZESS
 * @date 1/6/2021.
 * @file GetAndRead
 * GitHub：https://github.com/zqzess
 * 不会停止运行的app不是好app w(ﾟДﾟ)w
 */
public class GetAndRead {
    Context mContext;

    public GetAndRead(Context mContext) {
        this.mContext = mContext;
    }

    // 注意：返回值必须是 ArrayList<Chapter>（和 GlobalConfig.list 类型一致）
//    public static ArrayList<Chapter> getChapter(String url, int chapternum) {
//        Document alldoc;
//        Log.d("调试_章节列表", "开始爬取章节，书籍详情页链接：" + url);
//
//        try {
//            Log.d("GetChapter", "请求正确URL: " + url);
//            alldoc = Jsoup.connect(url)
//                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
//                    .timeout(10000)
//                    .get();
//            Log.d("GetChapter", "网页内容长度: " + alldoc.html().length());
//
//            // 打印body前5000字符，方便查看页面结构
//            String bodyHtml = alldoc.body().html();
//            Log.d("GetChapter", "完整body HTML（前5000字符）: \n" + (bodyHtml.length() > 5000 ? bodyHtml.substring(0, 5000) : bodyHtml));
//
//            // 多选择器适配新域名（重点！覆盖常见章节结构）
//            Elements chapters = alldoc.select("#list dl dd a"); // 旧域名原选择器
//            Log.d("GetChapter", "选择器 #list dl dd a 匹配数: " + chapters.size());
//
//            if (chapters.isEmpty()) {
//                chapters = alldoc.select(".chapter-list ul li a"); // 备选1
//                Log.d("GetChapter", "备选选择器 .chapter-list ul li a 匹配数: " + chapters.size());
//            }
//            if (chapters.isEmpty()) {
//                chapters = alldoc.select("div.chapter-list a"); // 备选2
//                Log.d("GetChapter", "备选选择器 div.chapter-list a 匹配数: " + chapters.size());
//            }
//            if (chapters.isEmpty()) {
//                chapters = alldoc.select("ul.chapter a"); // 备选3
//                Log.d("GetChapter", "备选选择器 ul.chapter a 匹配数: " + chapters.size());
//            }
//            // 终极备选：匹配包含当前书籍路径的a标签（比如 /11_11686/）
//            if (chapters.isEmpty()) {
//                String bookPath = url.substring(url.lastIndexOf("/", url.length()-2) + 1);
//                chapters = alldoc.select("a[href*=" + bookPath + "]");
//                Log.d("GetChapter", "终极备选选择器（包含路径" + bookPath + "）匹配数: " + chapters.size());
//            }
//
//            // 解析章节：直接用你的 Chapter 类（无任何类型冲突！）
//            if (!chapters.isEmpty()) {
//                GlobalConfig.list.clear(); // 清空旧数据
//                int i = 0;
//                for (Element e : chapters) {
//                    i++;
//                    // 保留旧逻辑的过滤规则（按 chapternum 跳过前面的章节）
//                    if (chapternum > 12 && i < 13) continue;
//                    if (chapternum <= 12 && i < chapternum) continue;
//
//                    String chapterTitle = e.text().trim(); // 章节标题
//                    String chapterHref = e.attr("href").trim(); // 相对路径
//                    // 拼接新域名完整URL（你的 Chapter 类里字段叫 url，这里对应上！）
//                    String chapterUrl = "https://www.uuubqg.cc" + chapterHref;
//
//                    // 直接创建你定义的 Chapter 对象（构造方法参数：title + url，和你 Chapter 类完全匹配！）
//                    Chapter chapter = new Chapter(chapterTitle, chapterUrl);
//                    GlobalConfig.list.add(chapter); // 类型完全匹配，不会报错！
//                }
//                Log.d("GetChapter", "成功获取 " + GlobalConfig.list.size() + " 个章节（已过滤）");
//            } else {
//                Log.e("GetChapter", "所有选择器均未匹配到章节！");
//                // 打印前20个a标签，帮助分析结构
//                Elements allLinks = alldoc.select("a");
//                for (int i = 0; i < Math.min(20, allLinks.size()); i++) {
//                    Element link = allLinks.get(i);
//                    Log.d("GetChapter", "a标签[" + i + "]: href=" + link.attr("href") + " | 文本=" + link.text());
//                }
//            }
//
//        } catch (Exception e) {
//            Log.e("GetChapter", "爬取失败: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return GlobalConfig.list;
//    }

    public static ArrayList<Chapter> getChapter(String url, int chapternum) {
        Document alldoc;
        ArrayList<Chapter> chapterList = new ArrayList<>();
        try {
            Log.d("GetChapter", "原始URL: " + url);
            // 清洗URL：去掉重复的域名、多余的/等
            String cleanUrl;
            if (url.startsWith("http")) {
                cleanUrl = url;
            } else if (url.startsWith("/")) {
                cleanUrl = "https://www.uuubqg.cc" + url;
            } else {
                cleanUrl = "https://www.uuubqg.cc/" + url;
            }
            // 处理重复域名的情况（关键修复）
            cleanUrl = cleanUrl.replaceAll("https?://www.uuubqg.cchttps?://", "https://");
            cleanUrl = cleanUrl.replaceAll("https?://www.uuubqg.cc/https?://", "https://");
            Log.d("GetChapter", "清洗后URL: " + cleanUrl);

            alldoc = Jsoup.connect(cleanUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 查找正文卷标签（严格按要求）
            Element mainContentDt = alldoc.selectFirst("div.listmain dl dt:contains(正文卷)");
            if (mainContentDt != null) {
                Log.d("GetChapter", "找到正文卷标签，开始解析章节");
                Elements siblings = mainContentDt.nextElementSiblings();
                for (Element sibling : siblings) {
                    if (sibling.tagName().equals("dt")) {
                        Log.d("GetChapter", "遇到其他DT标签，停止解析正文卷: " + sibling.text());
                        break;
                    }
                    if (sibling.tagName().equals("dd")) {
                        Element aTag = sibling.selectFirst("a");
                        if (aTag != null) {
                            String title = aTag.text().trim();
                            String chapterUrl = aTag.attr("href").trim();
                            if (chapterUrl.startsWith("/")) {
                                chapterUrl = "https://www.uuubqg.cc" + chapterUrl;
                            }
                            chapterList.add(new Chapter(title, chapterUrl));
                            Log.d("GetChapter", "添加正文卷章节: " + title);
                        } else {
                            Log.w("GetChapter", "DD标签中无A标签: " + sibling.html());
                        }
                    }
                }
            } else {
                Log.w("GetChapter", "未找到正文卷标签，解析失败");
            }

            Log.d("GetChapter", "章节解析完成，共获取: " + chapterList.size() + " 个章节");

        } catch (Exception e) {
            Log.e("GetChapter", "章节解析失败: " + e.getMessage(), e); // 打印完整堆栈
        }
        GlobalConfig.list = chapterList;
        return chapterList;
    }

    public static void ReadingBackground(final int chapternow)
    {
//        final ArrayList<HashMap<String, String>> list ;
//        list=new ArrayList<HashMap<String, String>>();
        Thread thread1=new Thread(new Runnable() {
            @Override
            public void run() {
                int tmpcount=chapternow-2;
                for(int i=0;i<5;i++)
                {
                    tmpcount+=1;
                    if(tmpcount<=GlobalConfig.list.size()-1||tmpcount>=0)
                    {
                        //                    String tmpstring=GetBookContent(GlobalConfig.list.get(i).get("link"));//爬取章节内容
//                    tmpstring=splitContentFirst(tmpstring);//分段
                        try{
                            //BookContentCache.getCache(GlobalConfig.list.get(tmpcount).get("link"));//检查是否存在缓存

                            // 修正后：先获取 Chapter 对象，再调用 getUrl()
                            Chapter chapter = GlobalConfig.list.get(tmpcount);
                            BookContentCache.getCache(chapter.getUrl()); // 用 getUrl() 获取章节链接
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread1.start();
//        try {
//            thread1.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

//    //章节内容爬取
//    public static String GetBookContent(String url)
//    {
//        Document alldoc;
//        String content="";
//        try
//        {
//            alldoc = Jsoup.connect(url).data("query", "Java").
//                    userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
//                    .timeout(5000).get();
//            content = alldoc.select("#content").text();//trim() 删除字符串首尾空白字符
////            String title = alldoc.select("#wrapper > div.content_read > div.box_con > div.bookname > h1").text().trim();
//        }catch(Exception e)
//        {
//            e.printStackTrace();
    ////            Toast.makeText(mContext, "章节链接不完整或错误", Toast.LENGTH_SHORT).show();
//        }
//        return content;
//    }

    //章节内容爬取
    public static String GetBookContent(String url) {
        Document alldoc;
        String content = "";
        try {
            alldoc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Mobile Safari/537.36") // 移动端UA
                    .timeout(10000)
                    .get();

            // 新域名章节内容的选择器（根据常见移动端结构调整，后续根据日志优化）
            Element contentElement = alldoc.selectFirst("div#content, div.showtxt, div.chapter-content");
            if (contentElement != null) {
                contentElement.select("script, style, div.ad, span.ad").remove(); // 移除广告和脚本
                content = contentElement.text()
                        .replace("　　", "\n  ")
                        .replace("\n\n", "\n")
                        .trim();
            } else {
                // 打印内容区域附近的HTML，方便后续调整选择器
                Log.e("GetContent", "未找到内容容器，打印页面关键部分：");
                Log.d("GetContent", alldoc.select("div.book-content, div.read-content").html());
                content = "未获取到章节内容，请检查链接或选择器";
            }

        } catch (Exception e) {
            Log.e("GetContent", "获取内容失败: " + e.getMessage());
            content = "获取内容失败：" + e.getMessage();
        }
        return content;
    }

    public String splitContentFirst(String stringcontent)
    {
        /*
         *处理文章第一步，分段落
         */
        if(stringcontent==""||stringcontent==null)
        {
            stringcontent="错误！可能原因：\n1.网络错误，笔趣阁网络连接超时或您的网络错误，请刷新重试\n2.内容错误，该书不存在内容，笔趣阁网站部分书籍没有内容，请等待新书源\n3.链接错误，笔趣阁已更换该书籍内容链接，请提交反馈";
        }
        int count = stringcontent.length();
        int istart = 0;
        String tmp2=stringcontent.substring(1,2);
        String tmp=" ";
        String contentstring="";
        for(int i=1;i<=count;i++)
        {
            if(i!=count)
            {
                String nowWord=stringcontent.substring(istart,i);
                String nextWord=stringcontent.substring(i,i+1);
                if(nowWord.equals("　")&&nextWord.equals("　"))
                {
                    contentstring+="    "+"  ";
                    istart=i+1;
                }
                else if(nowWord.equals(tmp)&&nextWord.equals(tmp2))
                {
                    contentstring+="\n          \n"+" "+" ";
                    istart=i+1;
//                        mRealLine++;
                }
                else
                {
                    contentstring+=(stringcontent.substring(istart,i));
                    istart=i;
                }
            }else
            {
                contentstring+=stringcontent.substring(i-1,i);
            }
        }
        return contentstring;
    }

    public void PageSet(String content, int mPageLineNum, ConcurrentHashMap contentMap)
    {
        /*
         *单章节分页,并将内容存入Hashmap
         */
        String[] arrtmp=content.split("\n");
        String contenttmp="";
        int tmpcount=0;//单页行数
        int Pagecount=1;//单章页数
        try{
            //contentMap.put(0,GlobalConfig.list.get(GlobalConfig.chapternow).get("title"));

            Chapter currentChapter = GlobalConfig.list.get(GlobalConfig.chapternow);
            contentMap.put(0, currentChapter.getTitle()); // 用 getTitle() 获取章节标题
        }catch (IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
        for(int i=0;i<arrtmp.length;i++)
        {
            contenttmp+=arrtmp[i]+"\n";
            tmpcount++;
            if(tmpcount==mPageLineNum)
            {
                contentMap.put(Pagecount,contenttmp);
                Pagecount++;
                tmpcount=0;
                contenttmp="";
            }
        }
        if(!contenttmp.isEmpty())
        {
            contentMap.put(Pagecount,contenttmp);
            Pagecount++;
        }
        /*
         *如果章节过短
         */
        if(Pagecount==0)
        {
            contentMap.put(1,contenttmp);
            GlobalConfig.PageTotal=1;
        }else if(Pagecount==1)
        {
            contentMap.put(1,contenttmp);
            GlobalConfig.PageTotal=2;
            Pagecount=2;
        }else
        {
            GlobalConfig.PageTotal=Pagecount;
        }
//        /*
//        *清除上一章节残留内容影响
//         */
//        if(contentMap.size()>Pagecount)
//        {
//            for(int i=Pagecount;i<=contentMap.size()-Pagecount;i++)
//            {
//                contentMap.remove(i);
//            }
//        }
    }

    public String splitcontentSecond(String content,int FontSize,int measuredWidth)
    {
        /*
         *段落添加分隔符,自动换行
         */
        String[] arrtmp=content.split("\n");
        TextPaint textPaint2 = new TextPaint ( );
        String returntmp="";
        for(int i=0;i<arrtmp.length;i++)
        {
            if(!arrtmp[i].isEmpty())
            {
                int start=0;
                for(int j=0;j<arrtmp[i].length();j++)
                {
                    textPaint2.setTextSize(FontSize);
                    float textwidth=textPaint2.measureText(arrtmp[i].substring(start,j));
                    if(textwidth>=measuredWidth-FontSize)
                    {
                        arrtmp[i]=arrtmp[i].substring(0,j)+"\n"+arrtmp[i].substring(j);
                        start=j;
                    }
                    textPaint2.reset();
                }
            }
            if(returntmp.isEmpty())
            {
                returntmp=returntmp+arrtmp[i];
            }else
            {
                returntmp=returntmp+"\n"+arrtmp[i];
            }
        }
        return returntmp;
    }

    // 新增方法：获取书城首页数据 - 适配现有BookInfo模型
    // 在 GetAndRead 类中修改 getBookStoreData 方法
    public static BookStoreData getBookStoreData(String baseUrl) {
        BookStoreData bookStoreData = new BookStoreData();

        try {
            Document doc = Jsoup.connect(baseUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 1. 获取封面推荐（只有这4本书有封面）
            Elements hotItems = doc.select("div.hot div.l div.item");
            ArrayList<BookInfo> coverRecommendations = new ArrayList<>();
            // 直接使用方法参数的baseUrl，删除重复定义的String baseUrl = "https://www.uuubqg.cc";
            for (Element item : hotItems) {
                // ========== 关键修改1：精确定位图片元素 ==========
                Element img = item.selectFirst("div.image a img"); // 按层级找img，更精准
                String coverUrl = "";
                if (img != null) {
                    String relativeUrl = img.attr("src").trim();
                    // ========== 关键修改2：处理相对路径为绝对路径（用方法参数的baseUrl） ==========
                    if (!relativeUrl.isEmpty()) {
                        if (relativeUrl.startsWith("/")) {
                            coverUrl = baseUrl + relativeUrl; // 拼接方法参数的域名
                        } else if (relativeUrl.startsWith("http")) {
                            coverUrl = relativeUrl; // 已经是绝对路径，直接用
                        } else {
                            coverUrl = baseUrl + "/" + relativeUrl; // 其他情况（极少）
                        }
                    }
                }

                // 获取书籍链接和标题（这部分可以保留，也可以按层级优化）
                Element link = item.selectFirst("dl dt a");
                String title = link != null ? link.text().trim() : "";
                String bookUrl = "";
                if (link != null) {
                    String relativeBookUrl = link.attr("href").trim();
                    // 书籍链接也处理为绝对路径（可选，但建议统一）
                    if (!relativeBookUrl.isEmpty()) {
                        if (relativeBookUrl.startsWith("/")) {
                            bookUrl = baseUrl + relativeBookUrl;
                        } else {
                            bookUrl = relativeBookUrl;
                        }
                    }
                }

                // 获取作者（这部分保留，注意trim()去空格）
                Element authorSpan = item.selectFirst("dl dt span");
                String author = authorSpan != null ? authorSpan.text().trim() : "";

                // 获取简介（trim()去空格，避免多余换行/空格）
                Element desc = item.selectFirst("dl dd");
                String description = desc != null ? desc.text().trim() : "";

                // 构造BookInfo（保留）
                BookInfo book = new BookInfo(
                        title,          // name
                        author,         // author
                        bookUrl,        // link
                        "",             // picname (可以为空)
                        coverUrl,       // piclink (现在是绝对路径)
                        description,    // info
                        "",             // lasttime
                        "",             // newchapter
                        "",             // newchapterlink
                        0               // chapternum
                );
                coverRecommendations.add(book);
                // 新增日志：打印封面URL，方便调试
                Log.d("CoverRecommend", "书名：" + title + "，封面URL：" + coverUrl);
            }
            bookStoreData.setCoverRecommendations(coverRecommendations);

            // 2. 获取强力推荐（现在会获取封面和简介）
            Elements strongRecommendations = doc.select("div.hot div.r.bd ul.lis li, div.hot div.r ul.lis li");
            ArrayList<BookInfo> strongRecs = new ArrayList<>();
            for (Element item : strongRecommendations) {
                // 初始化所有变量，避免空指针
                String title = "";
                String bookUrl = "";
                String categoryText = "";
                String coverUrl = "";
                String description = "";
                String author = ""; // 作者变量提前初始化

                // 获取标题和链接
                Element link = item.selectFirst("a");
                if (link != null) {
                    title = link.text();
                    bookUrl = link.attr("href");
                }

                // 获取分类
                Element category = item.selectFirst("span.s1");
                if (category != null) {
                    categoryText = category.text();
                }

                Log.d("GetBookStore", "强力推荐书籍: " + title + ", 链接: " + bookUrl + ", 分类: " + categoryText);

                // 为强力推荐的书籍获取封面、简介和作者（关键修改）
                if (bookUrl != null && !bookUrl.isEmpty()) {
                    HashMap<String, String> bookInfo = getBookInfoFromDetailPage(bookUrl);
                    // 获取封面
                    if (bookInfo.containsKey("cover")) {
                        coverUrl = bookInfo.get("cover");
                    }
                    // 获取简介
                    if (bookInfo.containsKey("description")) {
                        String detailedDescription = bookInfo.get("description");
                        if (detailedDescription != null && !detailedDescription.isEmpty()) {
                            description = detailedDescription;
                            // 限制简介长度（可选）
                            if (description.length() > 100) {
                                description = description.substring(0, 100) + "...";
                            }
                        }
                    }
                    // 获取作者（关键：这里正确拿到作者并赋值给author变量）
                    if (bookInfo.containsKey("author")) {
                        String detailAuthor = bookInfo.get("author");
                        if (detailAuthor != null && !detailAuthor.isEmpty()) {
                            author = detailAuthor; // 把作者存到author变量里，不是categoryText！
                        }
                    }
                }

                // 如果简介还是空的，用分类填充（可选，看你需求）
                if (description.isEmpty()) {
                    description = categoryText;
                }

                // 构造BookInfo对象（关键：把author变量传进去）
                BookInfo book = new BookInfo(
                        title,
                        author, // 这里直接传author变量，有值就是作者，没值就是空字符串（初始化过的）
                        bookUrl,
                        "",
                        coverUrl,
                        description,
                        "",
                        "",
                        "",
                        0
                );

                strongRecs.add(book);
            }
            bookStoreData.setStrongRecommendations(strongRecs);
            Log.d("GetBookStore", "最终强力推荐书籍数量: " + strongRecs.size());

            // 3. 获取最新入库（现在会获取封面和简介）
            // 3. 获取最新入库（现在会获取封面和简介、作者）
            Elements newBooks = doc.select("div.up div.r ul li");
            ArrayList<BookInfo> newBookList = new ArrayList<>();
            for (Element item : newBooks) {
                // 书名和链接
                Element titleLink = item.selectFirst("span.s2 a");
                String title = titleLink != null ? titleLink.text() : "";
                String bookUrl = titleLink != null ? titleLink.attr("href") : "";

                // 入库时间
                Element addTime = item.selectFirst("span.s5");
                String addTimeStr = addTime != null ? addTime.text() : "";

                // 分类
                Element category = item.selectFirst("span.s1");
                String categoryText = category != null ? category.text() : "";

                // 初始化作者变量（关键：新增）
                String author = "";

                // 为最新入库的书籍获取封面、简介和作者（关键：补充作者逻辑）
                String coverUrl = "";
                String description = categoryText; // 先用分类作为默认简介

                if (bookUrl != null && !bookUrl.isEmpty()) {
                    HashMap<String, String> bookInfo = getBookInfoFromDetailPage(bookUrl);
                    coverUrl = bookInfo.get("cover");
                    String detailedDescription = bookInfo.get("description");

                    // 从详情页获取作者（关键：新增）
                    String detailAuthor = bookInfo.get("author");
                    if (detailAuthor != null && !detailAuthor.isEmpty()) {
                        author = detailAuthor; // 把详情页的作者赋值给变量
                    }

                    // 先替换简介，再限制长度（和最近更新保持一致的逻辑，修复小bug）
                    if (detailedDescription != null && !detailedDescription.isEmpty()) {
                        description = detailedDescription;
                    }
                    // 限制简介长度（移到替换简介之后）
                    if (description.length() > 100) {
                        description = description.substring(0, 100) + "...";
                    }
                }

                BookInfo book = new BookInfo(
                        title,              // name
                        author,             // author（关键：传作者变量，不再是空字符串）
                        bookUrl,            // link
                        "",                 // picname
                        coverUrl,           // piclink
                        description,        // info
                        addTimeStr,         // lasttime
                        "",                 // newchapter
                        "",                 // newchapterlink
                        0                   // chapternum
                );

                newBookList.add(book);
            }
            bookStoreData.setNewBooks(newBookList);

            // 修改最近更新的部分
            // 4. 获取最近更新（现在会获取封面和简介）
            // 4. 获取最近更新（现在会获取封面和简介）
            Elements recentUpdates = doc.select("div.up div.l ul li");
            ArrayList<BookInfo> recentUpdateList = new ArrayList<>();
            for (Element item : recentUpdates) {
                // 书名和链接
                Element titleLink = item.selectFirst("span.s2 a");
                String title = titleLink != null ? titleLink.text() : "";
                String bookUrl = titleLink != null ? titleLink.attr("href") : "";

                // 最新章节
                Element chapterLink = item.selectFirst("span.s3 a");
                String latestChapter = chapterLink != null ? chapterLink.text() : "";
                String latestChapterUrl = chapterLink != null ? chapterLink.attr("href") : "";

                // 作者（首页span.s4）
                Element authorElement = item.selectFirst("span.s4");
                String authorName = authorElement != null ? authorElement.text() : "";
                String author = authorName; // 先初始化作者为首页获取的名字（关键修改1）

                // 更新时间
                Element updateTime = item.selectFirst("span.s5");
                String updateTimeStr = updateTime != null ? updateTime.text() : "";

                // 分类
                Element category = item.selectFirst("span.s1");
                String categoryText = category != null ? category.text() : "";

                // 为最近更新的书籍获取封面和简介
                String coverUrl = "";
                String description = categoryText; // 先用分类作为默认简介

                if (bookUrl != null && !bookUrl.isEmpty()) {
                    HashMap<String, String> bookInfo = getBookInfoFromDetailPage(bookUrl);
                    coverUrl = bookInfo.get("cover");
                    String detailedDescription = bookInfo.get("description");

                    // 如果有获取到详情页简介，替换默认简介（关键修改2：先替换，再截断）
                    if (detailedDescription != null && !detailedDescription.isEmpty()) {
                        description = detailedDescription;
                    }

                    // 限制简介长度（移到替换简介之后，关键修改2）
                    if (description.length() > 100) {
                        description = description.substring(0, 100) + "...";
                    }

                    // 如果详情页有作者信息，覆盖首页的作者名（优先详情页，关键修改3）
                    String detailAuthor = bookInfo.get("author");
                    if (detailAuthor != null && !detailAuthor.isEmpty()) {
                        author = detailAuthor;
                    }
                } else {
                    // 如果没有详情页链接，也检查默认简介长度（可选，看需求）
                    if (description.length() > 100) {
                        description = description.substring(0, 100) + "...";
                    }
                }

                BookInfo book = new BookInfo(
                        title,              // name
                        author,             // author（现在是合并后的值，关键修改4）
                        bookUrl,            // link
                        "",                 // picname
                        coverUrl,           // piclink
                        description,        // info
                        updateTimeStr,      // lasttime
                        latestChapter,      // newchapter
                        latestChapterUrl,   // newchapterlink
                        0                   // chapternum
                );

                recentUpdateList.add(book);
            }
            bookStoreData.setRecentUpdates(recentUpdateList);

            Log.d("GetBookStore", "成功获取书城数据: " +
                    "封面推荐=" + coverRecommendations.size() + ", " +
                    "强力推荐=" + strongRecs.size() + ", " +
                    "最近更新=" + recentUpdateList.size() + ", " +
                    "最新入库=" + newBookList.size());

        } catch (Exception e) {
            Log.e("GetBookStore", "获取书城数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return bookStoreData;
    }

    // 修改 GetAndRead 类中的 getBookCoverFromDetailPage 方法
    public static HashMap<String, String> getBookInfoFromDetailPage(String bookUrl) {
        HashMap<String, String> bookInfo = new HashMap<>();
        // 初始化所有字段
        bookInfo.put("cover", "");
        bookInfo.put("description", "");
        bookInfo.put("author", "");
        bookInfo.put("lasttime", "");       // 新增：最后更新时间
        bookInfo.put("newchapter", "");     // 新增：最新章节
        bookInfo.put("chapternum", "0");    // 新增：章节数
        bookInfo.put("title", "");          // 新增：书名（用于验证）

        if (bookUrl == null || bookUrl.isEmpty()) {
            return bookInfo;
        }

        try {
            // 确保URL是完整的
            String fullUrl;
            if (bookUrl.startsWith("http")) {
                fullUrl = bookUrl;
            } else {
                // 重要：使用正确的域名
                fullUrl = "https://www.uuubqg.cc" + bookUrl;  // 使用uuubqg.cc
            }

            Log.d("GetBookInfoDetail", "开始爬取书籍详情页: " + fullUrl);

            Document doc = Jsoup.connect(fullUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 打印页面标题，确认是否正确访问
            Log.d("GetBookInfoDetail", "页面标题: " + doc.title());

            // 打印 #info 的HTML，方便调试
            Element infoDiv = doc.selectFirst("#info");
            if (infoDiv != null) {
                Log.d("GetBookInfoDetail", "#info HTML: " + infoDiv.html());
            }

            // 1. 获取书名
            Element titleElement = doc.selectFirst("#info h1");
            if (titleElement != null) {
                String title = titleElement.text().trim();
                bookInfo.put("title", title);
                Log.d("GetBookInfoDetail", "获取书名: " + title);
            }

            // 2. 获取作者信息 - 使用灵活的选择器
            Element authorElement = doc.selectFirst("#info p:contains(作者), #info p:contains(作 者)");
            if (authorElement != null) {
                String authorText = authorElement.text().trim();
                Log.d("GetBookInfoDetail", "作者原始文本: " + authorText);

                // 手动处理作者文本，去掉前缀（处理全角空格、半角空格、冒号的情况）
                String author = "";
                // 替换所有全角空格和半角空格，方便统一处理
                String cleanText = authorText.replaceAll("[\\s|&nbsp;|&ensp;|&emsp;]", "").replaceAll("　", "");
                // 找到“作者：”的位置（处理“作者：”或“作作者：”等错误，实际是“作者：”）
                int colonIndex = cleanText.indexOf("作者：");
                if (colonIndex != -1) {
                    author = cleanText.substring(colonIndex + 3).trim(); // “作者：”是3个字符（作+者+：）
                } else {
                    // 兼容其他格式，比如“作 者：”（带空格），去掉所有非文字部分，取后面的内容
                    author = authorText.replaceAll("^.*[：:]", "").trim(); // 匹配任意字符直到最后一个冒号（全角/半角），取后面的内容
                }

                if (!author.isEmpty()) {
                    bookInfo.put("author", author);
                    Log.d("GetBookInfoDetail", "提取到作者: " + author);
                }
            }

            // 3.爬取最后更新时间
            Element lastTimeElement = doc.selectFirst("#info p:contains(最后更新)");
            if (lastTimeElement != null) {
                String lastTimeText = lastTimeElement.text().trim();
                // 提取时间（如“最后更新：2016-06-06 05:28:12”→“2016-06-06 05:28:12”）
                String lastTime = lastTimeText.replaceAll("最后更新：", "").trim();
                bookInfo.put("lasttime", lastTime);
                Log.d("GetBookInfoDetail", "提取到最后更新时间: " + lastTime);
            }

            // 4.爬取最新章节
            Element newChapterElement = doc.selectFirst("#info p:contains(最新章节) a");
            if (newChapterElement != null) {
                String newChapter = newChapterElement.text().trim();
                bookInfo.put("newchapter", newChapter);
                Log.d("GetBookInfoDetail", "提取到最新章节: " + newChapter);
            } else {
                // 若a标签不存在，直接从p标签提取
                Element newChapterP = doc.selectFirst("#info p:contains(最新章节)");
                if (newChapterP != null) {
                    String newChapterText = newChapterP.text().trim().replaceAll("最新章节：", "").trim();
                    bookInfo.put("newchapter", newChapterText);
                }
            }

            // 5. 获取章节数
            try {
                Elements chapters = doc.select("#list dl dd a");
                if (!chapters.isEmpty()) {
                    bookInfo.put("chapternum", String.valueOf(chapters.size()));
                    Log.d("GetBookInfoDetail", "章节数: " + chapters.size());
                }
            } catch (Exception e) {
                Log.e("GetBookInfoDetail", "计算章节数失败: " + e.getMessage());
            }

            // 6. 从详情页获取封面图片
            Element coverImg = doc.selectFirst("#sidebar img");
            if (coverImg != null) {
                String coverUrl = coverImg.attr("src");
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    // 处理相对路径
                    if (coverUrl.startsWith("//")) {
                        coverUrl = "https:" + coverUrl;
                    } else if (coverUrl.startsWith("/")) {
                        coverUrl = "https://www.uuubqg.cc" + coverUrl;  // 使用uuubqg.cc
                    }
                    bookInfo.put("cover", coverUrl);
                    Log.d("GetBookInfoDetail", "封面链接: " + coverUrl);
                }
            }

            // 7. 获取书籍简介
            Element introElement = doc.selectFirst("#intro");
            if (introElement != null) {
                String description = introElement.text();
                if (description != null && !description.isEmpty()) {
                    // 清理简介文本
                    description = description.trim()
                            .replaceAll("\\s+", " ")
                            .replace("　　", " ")
                            .replace("各位书友要是觉得", "")
                            .replace("还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！", "")
                            .trim();
                    bookInfo.put("description", description);
                    Log.d("GetBookInfoDetail", "简介长度: " + description.length());
                }
            }

            Log.d("GetBookInfoDetail", "=== 最终获取结果 ===");
            Log.d("GetBookInfoDetail", "书名: " + bookInfo.get("title"));
            Log.d("GetBookInfoDetail", "作者: " + bookInfo.get("author"));
            Log.d("GetBookInfoDetail", "最后更新: " + bookInfo.get("lasttime"));
            Log.d("GetBookInfoDetail", "最新章节: " + bookInfo.get("newchapter"));
            Log.d("GetBookInfoDetail", "章节数: " + bookInfo.get("chapternum"));
            Log.d("GetBookInfoDetail", "==========================");

        } catch (Exception e) {
            Log.e("GetBookInfoDetail", "获取书籍信息失败: " + e.getMessage());
            e.printStackTrace();
        }

        return bookInfo;
    }

    // 辅助方法：从文本中提取信息
    private static String extractInfo(String text, String key) {
        if (text == null || text.isEmpty() || key == null || key.isEmpty()) {
            return "";
        }
        // 处理带空格的情况，比如“作 者”“作   者”“作者”等，先把key变成模糊匹配（比如key是“作者”，匹配“作.*者”）
        String regex = key.replace("", ".*"); // 变成“作.*者”，匹配中间任意字符（空格、全角空格等）
        // 匹配“作*者：”（*表示任意字符），全角冒号和半角冒号都处理
        Pattern pattern = Pattern.compile(regex + "[：:]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int endIndex = matcher.end();
            return text.substring(endIndex).trim();
        }
        // 兼容直接按冒号分割的情况
        String[] parts = text.split("[：:]");
        if (parts.length >= 2) {
            return parts[1].trim();
        }
        return "";
    }
}