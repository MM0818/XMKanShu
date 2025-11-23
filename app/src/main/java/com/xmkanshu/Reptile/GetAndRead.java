package com.xmkanshu.Reptile;

import android.content.Context;
import android.text.TextPaint;
import android.util.Log;

import com.xmkanshu.Cache.BookContentCache;
import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.Model.Chapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
        Log.d("调试_章节列表", "开始爬取章节，书籍详情页链接：" + url);

        try {
            Log.d("GetChapter", "请求正确URL: " + url);
            alldoc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();
            Log.d("GetChapter", "网页内容长度: " + alldoc.html().length());

            // ======================================
            // 核心修改：只抓取「正文卷」部分的章节
            // ======================================
            Elements chapters = new Elements();

            // 1. 找到包含"正文卷"的<dt>标签
            Element mainContentDt = alldoc.selectFirst("div.listmain dl dt:contains(正文卷)");

            if (mainContentDt != null) {
                Log.d("GetChapter", "找到正文卷标签：" + mainContentDt.text().trim());

                // 2. 获取正文卷<dt>后面的所有兄弟节点
                Elements siblings = mainContentDt.nextElementSiblings();

                // 3. 遍历所有兄弟节点，收集所有<dd>里的<a>标签
                for (Element sibling : siblings) {
                    // 如果遇到下一个<dt>标签（比如"最新章节"或其他卷），就停止收集
                    if (sibling.tagName().equals("dt")) {
                        Log.d("GetChapter", "遇到下一个dt标签，停止收集: " + sibling.text());
                        break;
                    }

                    // 只收集<dd>标签中的<a>标签
                    if (sibling.tagName().equals("dd")) {
                        Element aTag = sibling.selectFirst("a");
                        if (aTag != null) {
                            chapters.add(aTag);
                        }
                    }
                }

                Log.d("GetChapter", "从正文卷部分获取到章节数: " + chapters.size());

            } else {
                // 兼容逻辑：如果没找到"正文卷"标签，使用原来的选择器
                Log.d("GetChapter", "未找到「正文卷」标签，使用备用选择器");
                chapters = alldoc.select("#list dl dd a");
                if (chapters.isEmpty()) {
                    chapters = alldoc.select(".chapter-list ul li a");
                }
                if (chapters.isEmpty()) {
                    chapters = alldoc.select("div.chapter-list a");
                }
                if (chapters.isEmpty()) {
                    chapters = alldoc.select("ul.chapter a");
                }
            }

            // ======================================
            // 解析章节数据（保持不变）
            // ======================================
            if (!chapters.isEmpty()) {
                GlobalConfig.list.clear();

                // 注意：现在不需要跳过前面的章节了，因为我们已经只获取了正文卷部分
                for (Element e : chapters) {
                    String chapterTitle = e.text().trim();
                    String chapterHref = e.attr("href").trim();

                    // 确保URL是完整的（处理相对路径）
                    String chapterUrl;
                    if (chapterHref.startsWith("http")) {
                        chapterUrl = chapterHref;
                    } else {
                        chapterUrl = "https://www.biqugeu.net" + chapterHref;
                    }

                    Chapter chapter = new Chapter(chapterTitle, chapterUrl);
                    GlobalConfig.list.add(chapter);
                }

                Log.d("GetChapter", "成功获取 " + GlobalConfig.list.size() + " 个章节（仅正文卷）");

            } else {
                Log.e("GetChapter", "未匹配到任何章节！");
                // 打印章节容器HTML，方便调试
                Element listMain = alldoc.selectFirst("div.listmain");
                if (listMain != null) {
                    Log.d("GetChapter", "章节容器HTML: " + listMain.html().substring(0, Math.min(2000, listMain.html().length())));
                }
            }

        } catch (Exception e) {
            Log.e("GetChapter", "爬取失败: " + e.getMessage());
            e.printStackTrace();
        }
        return GlobalConfig.list;
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
}