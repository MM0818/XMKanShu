package com.xmkanshu.Reptile;

import android.util.Log;

import com.xmkanshu.Cache.BookInfoCache;
import com.xmkanshu.Data.BookInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GetBook {
    private static final String TAG = "GetBook"; // 新增TAG常量

    //五、功能模块=====================================================================================================
    //1.封面推荐===================================================================================================
    /*
        定位首页推荐区域的HTML元素
        1.使用Jsoup库连接目标URL（如：http://www.uuubqg.cc/）
        2.通过CSS选择器（如：.item）定位推荐书籍的HTML元素
        3.遍历每个元素，提取书籍名称、作者、封面图片等信息
        4.将提取到的信息存储到HashMap中
        5.将每个HashMap添加到ArrayList中
        6.返回包含所有推荐书籍信息的ArrayList
    */
    public static ArrayList fengtui() {
        ArrayList<HashMap<String, String>> list;
        Document alldoc;
        String link;//书链接
        list = new ArrayList<HashMap<String, String>>();

        try {
            //==一、网络爬虫技术===============================================================================
            //1.Jsoup库的使用
            alldoc = Jsoup.connect("http://www.uuubqg.cc/")
                    .data("query", "Java")  //通过CSS选择器定位HTML元素
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .get();  //支持HTTP GET请求获取页面内容

            //2.HTML元素解析
            Elements listClass = alldoc.getElementsByAttributeValue("class", "item");  //通过class属性值获取元素
            for (Element listitem : listClass) {
                HashMap<String, String> book = new HashMap<String, String>();

                try {
                    //link 就是 <a href="..."> 里的地址，即“书源详情页相对地址”，后面要拿它拼成完整 URL 再去抓章节列表
                    link = listitem.getElementsByTag("a").attr("href");//获取书籍link（href属性值）
                } catch (Exception e) {
                    link = "";
                }

                //==二、缓存机制======================================================================================
                //1.书籍信息缓存=====================================================================================
                String id = link.replace("/", "");
                final BookInfo bookInfo = BookInfoCache.loadBook(id);  //本地缓存书籍信息，减少重复网络请求，提高响应速度，支持离线访问已缓存的书籍信息

                book.put("name", bookInfo.getName());
                book.put("author", bookInfo.getAuthor());
                book.put("link", link);
                book.put("picname", bookInfo.getPicname());
                book.put("piclink", bookInfo.getPiclink());
                book.put("info", bookInfo.getInfo());
                book.put("lasttime", bookInfo.getLasttime());
                book.put("newchapter", bookInfo.getNewchapter());
                book.put("newchapterlink", bookInfo.getNewchapterlink());
                book.put("chapternum", String.valueOf(bookInfo.getChapternum())); // 补充chapternum字段

                Log.d("name", bookInfo.getName());
                Log.d("author", bookInfo.getAuthor());
                Log.d("link", link);
                Log.d("picname", bookInfo.getPicname());
                Log.d("piclink", bookInfo.getPiclink());
                Log.d("info", bookInfo.getInfo());
                Log.d("lasttime", bookInfo.getLasttime());
                Log.d("newchapter", bookInfo.getNewchapter());
                Log.d("newchapterlink", bookInfo.getNewchapterlink());
                Log.d("chapternum", bookInfo.getChapternum() + "");

                list.add(book);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    //五、2.强力推荐==========================================================================================
    /*
        定位首页强力推荐区域的HTML元素（与封面推荐类似的处理逻辑，代码复用）
        1.使用Jsoup库连接目标URL（如：http://www.uuubqg.cc/）
        2.通过CSS选择器（如：#hotcontent > div.r > ul:nth-child(4) > li）定位推荐书籍的HTML元素
        3.遍历每个元素，提取书籍名称、作者、封面图片等信息
        4.将提取到的信息存储到HashMap中
        5.将每个HashMap添加到ArrayList中
        6.返回包含所有强力推荐书籍信息的ArrayList
    */
    public static ArrayList qiangtui() {
        Document alldoc;
        final ArrayList<HashMap<String, String>> list;
        list = new ArrayList<HashMap<String, String>>();
        try {
            alldoc = Jsoup.connect("http://www.uuubqg.cc/")
                    .data("query", "Java")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .get();

            Elements listClass = alldoc.select("#hotcontent > div.r > ul:nth-child(4) > li");
            for (Element listitem : listClass) {
                final String url = listitem.getElementsByTag("a").attr("href");//获取书籍link
                //二、2.缓存键生成================================================================================
                //简单高效的缓存键生成策略，确保缓存键的唯一性，便于后续查找和管理缓存
                final String id = url.replace("/", "");  // 将 URL 转换为缓存键
                final BookInfo bookInfo = BookInfoCache.loadBook(id);

                HashMap<String, String> book = new HashMap<String, String>();
                book.put("name", bookInfo.getName());
                book.put("author", bookInfo.getAuthor());
                book.put("link", url);
                book.put("picname", bookInfo.getPicname());
                book.put("piclink", bookInfo.getPiclink());
                book.put("info", bookInfo.getInfo());
                book.put("lasttime", bookInfo.getLasttime());
                book.put("newchapter", bookInfo.getNewchapter());
                book.put("newchapterlink", bookInfo.getNewchapterlink());
                book.put("chapternum", String.valueOf(bookInfo.getChapternum())); // 补充chapternum字段

                list.add(book);

                Log.d("name", bookInfo.getName());
                Log.d("author", bookInfo.getAuthor());
                Log.d("link", url);
                Log.d("picname", bookInfo.getPicname());
                Log.d("piclink", bookInfo.getPiclink());
                Log.d("info", bookInfo.getInfo());
                Log.d("lasttime", bookInfo.getLasttime());
                Log.d("newchapter", bookInfo.getNewchapter());
                Log.d("newchapterlink", bookInfo.getNewchapterlink());
                Log.d("chapternum", bookInfo.getChapternum() + "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    //五、3.最新入库=================================================================================================
    public static ArrayList ruku() {
        Document alldoc;
        ArrayList<HashMap<String, String>> list;
        list = new ArrayList<HashMap<String, String>>();
        try {
            alldoc = Jsoup.connect("http://www.uuubqg.cc/")
                    .data("query", "Java")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .get();

            Elements listClass = alldoc.select("#newscontent > div.r > ul > li");
            for (Element listitem : listClass) {
                HashMap<String, String> book = new HashMap<String, String>();
                String url = listitem.getElementsByTag("a").attr("href");//获取书籍link
                String id = url.replace("/", "");
                BookInfo bookInfo = BookInfoCache.loadBook(id);

                book.put("name", bookInfo.getName());
                book.put("author", bookInfo.getAuthor());
                book.put("link", url);
                book.put("picname", bookInfo.getPicname());
                book.put("piclink", bookInfo.getPiclink());
                book.put("info", bookInfo.getInfo());
                book.put("lasttime", bookInfo.getLasttime());
                book.put("newchapter", bookInfo.getNewchapter());
                book.put("newchapterlink", bookInfo.getNewchapterlink());
                book.put("chapternum", String.valueOf(bookInfo.getChapternum())); // 补充chapternum字段

                list.add(book);

                Log.d("name", bookInfo.getName());
                Log.d("author", bookInfo.getAuthor());
                Log.d("link", url);
                Log.d("picname", bookInfo.getPicname());
                Log.d("piclink", bookInfo.getPiclink());
                Log.d("info", bookInfo.getInfo());
                Log.d("lasttime", bookInfo.getLasttime());
                Log.d("newchapter", bookInfo.getNewchapter());
                Log.d("newchapterlink", bookInfo.getNewchapterlink());
                Log.d("chapternum", bookInfo.getChapternum() + "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    //五、4.书籍详情获取================================================================================================
    /*
        定位书籍详情区域的HTML元素（与封面推荐类似的处理逻辑，代码复用），主要是使用CSS选择器一个一个的找标签定位元素。分别存对应变量里后，再做参数全传进BookInfo对象里存储。
        1.书名：#info h1
        2.作者：#info p:contains(作者)
        3.封面图片：#fmimg img
        4.书籍介绍：#intro p
        5.最新更新时间：#info p:contains(最新更新时间)
        6.最新章节：#info p:contains(最新章节)
        7.章节数：#info p:contains(章节数)
    */
    public static BookInfo GetBookInfo(String id) {
        Log.d(TAG, "开始获取书籍信息，ID参数: " + id);

        String name = "";
        String author = "";
        String picname = "";
        String piclink = "";
        String info = "";
        String lasttime = "";
        String newchapter = "";
        String newchapterlink = "";
        int chapternum = 0;

        //一、3.URL处理与清洗
        try {
            //正则表达式处理URL格式
            //正则目的：不管 link 是完整 URL 还是相对路径，最后都变成同一套干净格式，方便缓存、拼接、去重。
            //下面的逻辑就是一句话：“脱头去尾，斜杠变下划线”，如https://www.uuubqg.cc//book//12345// -> book_12345
            String cleanId = id.replaceAll("https?://[^/]+", "").replaceAll("/+", "_").replaceAll("^_+", "").replaceAll("_+$", "");
            String fullUrl;

            if (cleanId.contains("_")) {
                fullUrl = "https://www.uuubqg.cc/" + cleanId + "/";
            } else {
                fullUrl = "https://www.uuubqg.cc/" + cleanId + "/";
            }
            Log.d(TAG, "修复后完整URL: " + fullUrl);

            Document alldoc = Jsoup.connect(fullUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();

            Element nameElement = alldoc.selectFirst("#info h1");
            if (nameElement != null) {
                name = nameElement.text().trim();
                Log.d(TAG, "获取书名成功: " + name);
            } else {
                Log.w(TAG, "书名元素未找到");
            }

            //==四、数据处理与转换====================================================================================
            //1.数据提取与清洗=======================================================================================
            Element authorElement = alldoc.selectFirst("#info p:contains(作)");  //CSS选择器精确定位目标元素
            if (authorElement != null) {
                //文本清洗，去除多余的空格和前缀
                String authorText = authorElement.text().trim();
                //第一个参数——正则表达式参数的意思：\\s* 代表 0 个或多个空白（空格、Tab、换行）。[:：] 代表 匹配英文冒号: 或中文全角冒号：（括号里任选一个）
                author = authorText.replaceAll("作\\s*者[:：]\\s*", "").trim();  
                Log.d(TAG, "获取作者成功: " + author);
            } else {
                Log.w(TAG, "作者元素未找到");
            }

            Element lastTimeElement = alldoc.selectFirst("#info p:contains(最后更新)");
            if (lastTimeElement != null) {
                String lastTimeText = lastTimeElement.text().trim();
                lasttime = lastTimeText.replaceAll("最后更新[:：]\\s*", "").trim();
                Log.d(TAG, "获取最后更新时间成功: " + lasttime);
            } else {
                Log.w(TAG, "最后更新时间元素未找到");
            }

            Element newChapterP = alldoc.selectFirst("#info p:contains(最新章节)");
            if (newChapterP != null) {
                Element newChapterA = newChapterP.selectFirst("a");
                if (newChapterA != null) {
                    newchapter = newChapterA.text().trim();
                    newchapterlink = newChapterA.attr("href").trim();
                    if (newchapterlink.startsWith("/")) {
                        newchapterlink = "https://www.uuubqg.cc" + newchapterlink;
                    }
                    Log.d(TAG, "获取最新章节成功（带链接）: " + newchapter);
                } else {
                    String newChapterText = newChapterP.text().trim();
                    newchapter = newChapterText.replaceAll("最新章节[:：]\\s*", "").trim();
                    Log.d(TAG, "获取最新章节成功（无链接）: " + newchapter);
                }
            } else {
                Log.w(TAG, "最新章节元素未找到");
            }

            Element introElement = alldoc.selectFirst("#intro");
            if (introElement != null) {
                info = introElement.text().trim();
                info = info.replaceAll("各位书友要是觉得.*?推荐哦！", "").trim();
                Log.d(TAG, "获取简介成功，长度: " + info.length());
            } else {
                Log.w(TAG, "简介元素未找到");
            }

            Element imgElement = alldoc.selectFirst("#fmimg img, #sidebar img");
            if (imgElement != null) {
                piclink = imgElement.attr("src").trim();
                if (piclink.startsWith("/")) {
                    piclink = "https://www.uuubqg.cc" + piclink;
                }
                picname = cleanId;
                Log.d(TAG, "获取封面成功: " + piclink);
            } else {
                Log.w(TAG, "封面元素未找到");
            }

            Element mainContentDt = alldoc.selectFirst("div.listmain dl dt:contains(正文卷)");
            if (mainContentDt != null) {
                Elements mainChapterDd = mainContentDt.nextElementSiblings().select("dd");
                chapternum = mainChapterDd.size();
                Log.d(TAG, "获取正文卷章节数成功: " + chapternum);
            } else {
                Elements allDd = alldoc.select("div.listmain dl dd");
                chapternum = allDd.size();
                Log.d(TAG, "未找到正文卷，统计所有章节数: " + chapternum);
            }

        } catch (Exception e) {
            Log.e(TAG, "获取书籍信息失败: " + e.getMessage(), e);
        }

        //四、2.数据结构构建========================================================================================
        //获取到的最新入库时间、简历、作者等等这些信息都一起给BookInfo对象存起来了
        BookInfo book = new BookInfo(name, author, id, picname, piclink, info, lasttime, newchapter, newchapterlink, chapternum);
        Log.d(TAG, "书籍信息对象创建完成: " + book.getName());
        return book;
    }

    //五、5.章节列表获取================================================================================================
    /*
        - 定位正文卷区域，过滤其他章节
        - 批量处理章节链接和标题
        - 构建章节列表数据结构
    */
    public static ArrayList<HashMap<String, String>> getChaptersFromMainContent(String bookId) {
        ArrayList<HashMap<String, String>> chapterList = new ArrayList<>();
        try {
            String[] idParts = bookId.split("_");
            String prefix = idParts.length > 0 ? idParts[0] : bookId;
            String fullUrl = "https://www.uuubqg.cc/book/" + prefix + "/" + bookId + "/";

            Document alldoc = Jsoup.connect(fullUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();

            Elements allListElements = alldoc.select("#listmain dl > *");
            boolean isMainContent = false;
            for (Element el : allListElements) {
                if (el.tagName().equals("dt")) { // 定位章节标题
                    if (el.text().contains("正文卷")) {
                        isMainContent = true;
                        continue;
                    } else if (el.text().contains("最新章节")) {
                        isMainContent = false;
                        continue;
                    }
                }

                if (el.tagName().equals("dd") && isMainContent) {  // 定位章节链接
                    Element aTag = el.selectFirst("a");
                    if (aTag != null) {
                        HashMap<String, String> chapter = new HashMap<>();
                        chapter.put("title", aTag.text().trim());
                        String chapterUrl = aTag.attr("href");
                        
                        if (chapterUrl.startsWith("/")) {
                            chapterUrl = "https://www.uuubqg.cc" + chapterUrl;
                        }
                        chapter.put("url", chapterUrl);
                        chapterList.add(chapter);
                    }
                }
            }
            Log.d(TAG, "爬取正文卷章节数: " + chapterList.size());
        } catch (Exception e) {
            Log.e(TAG, "爬取章节失败: " + e.getMessage());
            e.printStackTrace();
        }
        return chapterList;
    }
}