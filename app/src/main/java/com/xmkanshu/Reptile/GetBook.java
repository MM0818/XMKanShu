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

/**
 * @author ZQZESS
 * @date 12/9/2020.
 * @file GetBook
 * GitHub：https://github.com/zqzess
 * 不会停止运行的app不是好app w(ﾟДﾟ)w
 */
public class GetBook {

    public static ArrayList fengtui()
    {
        ArrayList<HashMap<String,String>>list;
        Document alldoc;
        String link;//书链接
        list=new ArrayList<HashMap<String, String>>();
        try{
            alldoc= Jsoup.connect("http://www.uuubqg.cc/").data("query", "Java").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36").get();
            Elements listClass = alldoc.getElementsByAttributeValue("class", "item");
            for(Element listitem:listClass)
            {
                HashMap<String,String> book = new HashMap<String,String>();

                try {
                     link = listitem.getElementsByTag("a").attr("href");//获取书籍link
                } catch (Exception e) {
                    link = "";
                }

                String id=link.replace("/","");
                final BookInfo bookInfo= BookInfoCache.loadBook(id);

                /*
                 *
                 * 子线程爬取封面
                 *
                 */

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        BookInfoCache.loadImage(bookInfo.getPicname(),bookInfo.getPiclink());//采用Haspmap缓存图片至内存
//                    }
//                }).start();
//                BookInfoCache.loadImage(bookInfo.getPicname(),bookInfo.getPiclink());//采用Haspmap缓存图片至内存

                book.put("name",bookInfo.getName());
                book.put("author",bookInfo.getAuthor());
                book.put("link",link);
                book.put("picname",bookInfo.getPicname());
                book.put("piclink",bookInfo.getPiclink());
                book.put("info",bookInfo.getInfo());
                book.put("lasttime",bookInfo.getLasttime());
                book.put("newchapter",bookInfo.getNewchapter());
                book.put("newchapterlink",bookInfo.getNewchapterlink());

                Log.d("name",bookInfo.getName());
                Log.d("author",bookInfo.getAuthor());
                Log.d("link",link);
                Log.d("picname",bookInfo.getPicname());
                Log.d("piclink",bookInfo.getPiclink());
                Log.d("info",bookInfo.getInfo());
                Log.d("lasttime",bookInfo.getLasttime());
                Log.d("newchapter",bookInfo.getNewchapter());
                Log.d("newchapterlink",bookInfo.getNewchapterlink());
                Log.d("chapternum",bookInfo.getChapternum()+"");

                list.add(book);

            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList qiangtui()
    {
        Document alldoc;
        final ArrayList<HashMap<String,String>>list;
        list=new ArrayList<HashMap<String, String>>();
        try{
            alldoc= Jsoup.connect("http://www.uuubqg.cc/").data("query", "Java").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36").get();
//            Elements listClass=alldoc.getElementsByAttributeValue("class","r");
            Elements listClass=alldoc.select("#hotcontent > div.r > ul:nth-child(4) > li");
            for(Element listitem:listClass) {

                final String url = listitem.getElementsByTag("a").attr("href");//获取书籍link
                final String id = url.replace("/", "");

                final BookInfo bookInfo= BookInfoCache.loadBook(id);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        BookInfoCache.loadImage(bookInfo.getPicname(),bookInfo.getPiclink());//采用Haspmap缓存图片至内存
//                    }
//                }).start();
//                BookInfoCache.loadImage(bookInfo.getPicname(),bookInfo.getPiclink());//采用Haspmap缓存图片至内存

                HashMap<String, String> book = new HashMap<String, String>();
                book.put("name",bookInfo.getName());
                book.put("author",bookInfo.getAuthor());
                book.put("link",url);
                book.put("picname",bookInfo.getPicname());
                book.put("piclink",bookInfo.getPiclink());
                book.put("info",bookInfo.getInfo());
                book.put("lasttime",bookInfo.getLasttime());
                book.put("newchapter",bookInfo.getNewchapter());
                book.put("newchapterlink",bookInfo.getNewchapterlink());

                list.add(book);

                Log.d("name",bookInfo.getName());
                Log.d("author",bookInfo.getAuthor());
                Log.d("link",url);
                Log.d("picname",bookInfo.getPicname());
                Log.d("piclink",bookInfo.getPiclink());
                Log.d("info",bookInfo.getInfo());
                Log.d("lasttime",bookInfo.getLasttime());
                Log.d("newchapter",bookInfo.getNewchapter());
                Log.d("newchapterlink",bookInfo.getNewchapterlink());
                Log.d("chapternum",bookInfo.getChapternum()+"");

            }


        }catch (IOException e)
        {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList ruku()
    {
        Document alldoc;
        ArrayList<HashMap<String,String>>list;
        list=new ArrayList<HashMap<String, String>>();
        try{
            alldoc= Jsoup.connect("http://www.uuubqg.cc/").data("query", "Java").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36").get();
//            Elements listClass=alldoc.getElementsByAttributeValue("class","r");
            Elements listClass=alldoc.select("#newscontent > div.r > ul > li");
            for(Element listitem:listClass)
            {
                HashMap<String,String> book = new HashMap<String,String>();
                String url=listitem.getElementsByTag("a").attr("href");//获取书籍link
                String id=url.replace("/","");
                BookInfo bookInfo= BookInfoCache.loadBook(id);

                book.put("name",bookInfo.getName());
                book.put("author",bookInfo.getAuthor());
                book.put("link",url);
                book.put("picname",bookInfo.getPicname());
                book.put("piclink",bookInfo.getPiclink());
                book.put("info",bookInfo.getInfo());
                book.put("lasttime",bookInfo.getLasttime());
                book.put("newchapter",bookInfo.getNewchapter());
                book.put("newchapterlink",bookInfo.getNewchapterlink());

                list.add(book);

                Log.d("name",bookInfo.getName());
                Log.d("author",bookInfo.getAuthor());
                Log.d("link",url);
                Log.d("picname",bookInfo.getPicname());
                Log.d("piclink",bookInfo.getPiclink());
                Log.d("info",bookInfo.getInfo());
                Log.d("lasttime",bookInfo.getLasttime());
                Log.d("newchapter",bookInfo.getNewchapter());
                Log.d("newchapterlink",bookInfo.getNewchapterlink());
                Log.d("chapternum",bookInfo.getChapternum()+"");

            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        return list;
    }


    public static BookInfo GetBookInfo(String id) {
        Log.d("GetBookInfo", "开始获取书籍信息，ID参数: " + id);

        String name="";
        String author="";
        String picname="";
        String piclink="";
        String info="";
        String lasttime="";
        String newchapter="";
        String newchapterlink="";
        int chapternum=0;

        try{
            // 修复URL拼接规则：直接使用id拼接成基础链接
            String fullUrl;
            // 清洗id中的非法字符（去掉域名、多余的/等）
            String cleanId = id.replaceAll("https?://[^/]+", "").replaceAll("/+", "_").replaceAll("^_+", "").replaceAll("_+$", "");
            if (cleanId.contains("_")) {
                fullUrl = "https://www.uuubqg.cc/" + cleanId + "/";
            } else {
                // 若id是纯数字，按原有逻辑尝试拼接（备用）
                fullUrl = "https://www.uuubqg.cc/" + cleanId + "/";
            }
            Log.d("GetBookInfo", "修复后完整URL: " + fullUrl); // 新增日志

            Document alldoc = Jsoup.connect(fullUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 1. 获取书名
            Element nameElement = alldoc.selectFirst("#info h1");
            if (nameElement != null) {
                name = nameElement.text().trim();
                Log.d("GetBookInfo", "获取书名成功: " + name);
            } else {
                Log.w("GetBookInfo", "书名元素未找到"); // 改为警告级别
            }

            // 2. 获取作者（模仿成功的逻辑，精准匹配）
            Element authorElement = alldoc.selectFirst("#info p:contains(作)");
            if (authorElement != null) {
                String authorText = authorElement.text().trim();
                // 处理全角空格、半角空格、冒号的各种组合
                author = authorText.replaceAll("作\\s*者[:：]\\s*", "").trim();
                Log.d("GetBookInfo", "获取作者成功: " + author);
            } else {
                Log.w("GetBookInfo", "作者元素未找到");
            }

            // 3. 获取最后更新时间（完全模仿作者的抓取逻辑）
            Element lastTimeElement = alldoc.selectFirst("#info p:contains(最后更新)");
            if (lastTimeElement != null) {
                String lastTimeText = lastTimeElement.text().trim();
                lasttime = lastTimeText.replaceAll("最后更新[:：]\\s*", "").trim();
                Log.d("GetBookInfo", "获取最后更新时间成功: " + lasttime);
            } else {
                Log.w("GetBookInfo", "最后更新时间元素未找到");
            }

            // 4. 获取最新章节（完全模仿作者的抓取逻辑）
            Element newChapterP = alldoc.selectFirst("#info p:contains(最新章节)");
            if (newChapterP != null) {
                Element newChapterA = newChapterP.selectFirst("a");
                if (newChapterA != null) {
                    newchapter = newChapterA.text().trim();
                    newchapterlink = newChapterA.attr("href").trim();
                    // 补全章节链接
                    if (newchapterlink.startsWith("/")) {
                        newchapterlink = "https://www.uuubqg.cc" + newchapterlink;
                    }
                    Log.d("GetBookInfo", "获取最新章节成功（带链接）: " + newchapter);
                } else {
                    String newChapterText = newChapterP.text().trim();
                    newchapter = newChapterText.replaceAll("最新章节[:：]\\s*", "").trim();
                    Log.d("GetBookInfo", "获取最新章节成功（无链接）: " + newchapter);
                }
            } else {
                Log.w("GetBookInfo", "最新章节元素未找到");
            }

            // 5. 获取简介
            Element introElement = alldoc.selectFirst("#intro");
            if (introElement != null) {
                info = introElement.text().trim();
                info = info.replaceAll("各位书友要是觉得.*?推荐哦！", "").trim();
                Log.d("GetBookInfo", "获取简介成功，长度: " + info.length());
            } else {
                Log.w("GetBookInfo", "简介元素未找到");
            }

            // 6. 获取封面图片
            Element imgElement = alldoc.selectFirst("#fmimg img, #sidebar img");
            if (imgElement != null) {
                piclink = imgElement.attr("src").trim();
                if (piclink.startsWith("/")) {
                    piclink = "https://www.uuubqg.cc" + piclink;
                }
                picname = cleanId; // 用清洗后的id作为picname
                Log.d("GetBookInfo", "获取封面成功: " + piclink);
            } else {
                Log.w("GetBookInfo", "封面元素未找到");
            }

            // 7. 计算章节数（从正文卷开始）
            Element mainContentDt = alldoc.selectFirst("div.listmain dl dt:contains(正文卷)");
            if (mainContentDt != null) {
                Elements mainChapterDd = mainContentDt.nextElementSiblings().select("dd");
                chapternum = mainChapterDd.size();
                Log.d("GetBookInfo", "获取正文卷章节数成功: " + chapternum);
            } else {
                // 备用：统计所有dd
                Elements allDd = alldoc.select("div.listmain dl dd");
                chapternum = allDd.size();
                Log.d("GetBookInfo", "未找到正文卷，统计所有章节数: " + chapternum);
            }

        } catch (Exception e) {
            Log.e("GetBookInfo", "获取书籍信息失败: " + e.getMessage(), e); // 打印完整堆栈
        }

        BookInfo book = new BookInfo(name, author, id, picname, piclink, info, lasttime, newchapter, newchapterlink, chapternum);
        Log.d("GetBookInfo", "书籍信息对象创建完成: " + book.toString()); // 若BookInfo有toString方法，否则打印关键字段
        return book;
    }

    //专门用于爬取正文卷的章节列表
    public static ArrayList<HashMap<String, String>> getChaptersFromMainContent(String bookId) {
        ArrayList<HashMap<String, String>> chapterList = new ArrayList<>();
        try {
            // 拼接章节列表页URL（与详情页URL相同）
            String[] idParts = bookId.split("_");
            String prefix = idParts.length > 0 ? idParts[0] : bookId;
            String fullUrl = "https://www.uuubqg.cc/book/" + prefix + "/" + bookId + "/";

            Document alldoc = Jsoup.connect(fullUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 遍历列表元素，跳过最新章节，从正文卷开始
            Elements allListElements = alldoc.select("#listmain dl > *");
            boolean isMainContent = false;
            for (Element el : allListElements) {
                if (el.tagName().equals("dt")) {
                    if (el.text().contains("正文卷")) {
                        isMainContent = true;
                        continue;
                    } else if (el.text().contains("最新章节")) {
                        isMainContent = false;
                        continue;
                    }
                }
                if (el.tagName().equals("dd") && isMainContent) {
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
            Log.d("GetBook", "爬取正文卷章节数: " + chapterList.size());
        } catch (Exception e) {
            Log.e("GetBook", "爬取章节失败: " + e.getMessage());
            e.printStackTrace();
        }
        return chapterList;
    }

}
