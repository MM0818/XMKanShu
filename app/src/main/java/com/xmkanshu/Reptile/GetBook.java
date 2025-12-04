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
    /*public static ArrayList fengtui()
    {
        ArrayList<HashMap<String,String>>list;
        Document alldoc;
        String name;//书名
        String author;//作者
        String info;//简介
        String link;//书链接
        String piclink;//封面链接
        String picname;
        list=new ArrayList<HashMap<String, String>>();
//        BookInfoCache cache=new BookInfoCache();
        try{
//            alldoc = Jsoup.connect("http://www.biquge.com").get();
            alldoc= Jsoup.connect("http://www.biquge.com/").data("query", "Java").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36").get();
            Elements listClass = alldoc.getElementsByAttributeValue("class", "item");
            for(Element listitem:listClass)
            {
                HashMap<String,String> book = new HashMap<String,String>();
                try {
                    link = listitem.getElementsByTag("a").attr("href");//获取书籍link
                } catch (Exception e) {
                    link = "";
                }
                try{
                    piclink=listitem.getElementsByTag("img").attr("src");//获取书籍封面link
                }catch (Exception e)
                {
                    piclink="";
                }
                try{
                    name=listitem.getElementsByTag("img").attr("alt");//获取书籍名称
                }catch (Exception e)
                {
                    name="";
                }
                try{
                    author=listitem.getElementsByTag("span").text().trim();//获取书籍作者
                }catch (Exception e)
                {
                    author="";
                }
                try{
                    info=listitem.getElementsByTag("dd").text().trim();//获取书籍简介
                }catch (Exception e)
                {
                    info="";
                }

                *//*
                *
                * 子线程爬取封面
                *
                 *//*
                picname=link.replace("/","");
//                String[] arry=picname.split("_");
//                final String finalPicName=arry[1];
                final String finalPicName=picname;
//                final String finalPiclink = piclink;
                BookInfoCache.loadImage(finalPicName,piclink);//采用Haspmap缓存图片至内存

                //边爬书籍边下载图片并保存本地

                *//*class MyPicThread implements Runnable
                {

                    @Override
                    public void run() {
                        Bitmap bitmap = null;
                        File f=new File("/data/data/com.tdkankan/temp/images/"+finalPicName+".jpg");
                        if(f.exists())
                        {
                            Log.d("pic","图片已存在");
                        }else
                        {
                            try {
                                //通过传入的图片地址，获取图片
                                HttpURLConnection connection = (HttpURLConnection) (new URL("http:"+ finalPiclink).openConnection());
                                InputStream is = connection.getInputStream();
                                bitmap = BitmapFactory.decodeStream(is);
                                if(bitmap.getByteCount()!=0)
                                {
                                    Log.d("pic","图片获取成功");
                                    BitmapUtils.writeBitmapToFile("/data/data/com.tdkankan/temp/images/","/data/data/com.tdkankan/temp/images/"+finalPicName+".jpg",bitmap,50);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }*//*

                *//*
                *
                * 书籍信息存入HashMap
                *
                 *//*

                //new MyPicThread().run();

                book.put("name",name);
                book.put("link",link);
                book.put("author",author);
                book.put("info",info);
                book.put("finalPicName",finalPicName);
                book.put("piclink",piclink);

                Log.d("name",name);
                Log.d("link",link);
                Log.d("author",author);
                Log.d("info",info);
                Log.d("finalPicName",finalPicName);
                Log.d("piclink",piclink);

                list.add(book);

//                try{
//
//                }catch (Exception e)
//                {
//
//                }

            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        return list;
    }*/


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


    public static BookInfo GetBookInfo(String url)
    {
        Log.d("GetBookInfo", "开始获取书籍信息，URL参数: " + url);

        String name="";    //书名
        String author="";  //作者
        String picname=""; //封面名字
        String piclink=""; //封面链接
        String info="";    //简介
        String lasttime="";    //最后更新时间
        String newchapter="";  //最新章节
        String newchapterlink="";  //最新章节链接
        int chapternum=0; //总章节

        try{
            // 处理不同格式的URL
            String fullUrl;
            if (url.startsWith("http")) {
                // 已经是完整URL
                fullUrl = url;
            } else if (url.contains("_")) {
                // 格式如 "137_137159"
                fullUrl = "https://www.uuubqg.cc/" + url + "/";
            } else if (url.matches("\\d+") && url.length() > 5) {
                // 纯数字格式，尝试转换为带下划线的格式
                // 尝试自动推断格式
                String formattedUrl;
                if (url.length() == 8) { // 可能是3+5格式
                    formattedUrl = url.substring(0, 3) + "_" + url.substring(3);
                } else if (url.length() == 9) { // 可能是4+5格式
                    formattedUrl = url.substring(0, 4) + "_" + url.substring(4);
                } else if (url.length() == 7) { // 可能是2+5格式
                    formattedUrl = url.substring(0, 2) + "_" + url.substring(2);
                } else {
                    // 无法推断，直接使用
                    formattedUrl = url;
                }
                fullUrl = "https://www.uuubqg.cc/" + formattedUrl + "/";
                Log.d("GetBookInfo", "纯数字转换为: " + formattedUrl);
            } else {
                // 其他情况，直接拼接
                fullUrl = "https://www.uuubqg.cc/" + url + "/";
            }

            Log.d("GetBookInfo", "完整URL: " + fullUrl);

            Document alldoc = Jsoup.connect(fullUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 打印页面信息用于调试
            Log.d("GetBookInfo", "页面标题: " + alldoc.title());

            // 1. 获取书名
            Element nameElement = alldoc.selectFirst("#info h1");
            if (nameElement != null) {
                name = nameElement.text().trim();
                Log.d("GetBookInfo", "获取书名成功: " + name);
            } else {
                Log.d("GetBookInfo", "书名元素未找到");
            }

            // 2. 获取作者 - 使用更灵活的选择器
            // 尝试多种选择器
            // 2. 获取作者 - 针对你的实际HTML结构
            // 最简单直接的方法：直接解析第一个p标签
            Elements infoParagraphs = alldoc.select("#info p");
            if (infoParagraphs != null && !infoParagraphs.isEmpty()) {
                // 第一个p标签就是作者信息
                String authorText = infoParagraphs.get(0).text().trim();
                Log.d("GetBookInfo", "作者标签文本: " + authorText);

                // 方法A：直接分割
                if (authorText.contains("：")) {
                    String[] parts = authorText.split("：");
                    if (parts.length > 1) {
                        author = parts[1].trim();
                        Log.d("GetBookInfo", "方法A提取作者: " + author);
                    }
                }

                // 方法B：使用substring
                if (author.isEmpty() && authorText.contains("者：")) {
                    int index = authorText.indexOf("者：");
                    if (index != -1) {
                        author = authorText.substring(index + 2).trim();
                        Log.d("GetBookInfo", "方法B提取作者: " + author);
                    }
                }

                // 方法C：处理HTML实体后提取
                if (author.isEmpty()) {
                    // 获取原始HTML
                    String authorHtml = infoParagraphs.get(0).html();
                    Log.d("GetBookInfo", "作者HTML: " + authorHtml);

                    // 查找"者："在HTML中的位置
                    if (authorHtml.contains("者：")) {
                        int start = authorHtml.indexOf("者：") + 2;
                        int end = authorHtml.indexOf("<", start);
                        if (end == -1) end = authorHtml.length();
                        author = authorHtml.substring(start, end).trim();
                        Log.d("GetBookInfo", "方法C提取作者: " + author);
                    }
                }
            }

            // 3. 获取最后更新时间
            Element lastTimeElement = alldoc.selectFirst("#info p:contains(最后更新)");
            if (lastTimeElement != null) {
                String lastTimeText = lastTimeElement.text().trim();
                Log.d("GetBookInfo", "最后更新原始文本: " + lastTimeText);

                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("最后更新[：:]\\s*(.+)");
                java.util.regex.Matcher matcher = pattern.matcher(lastTimeText);
                if (matcher.find()) {
                    lasttime = matcher.group(1).trim();
                    Log.d("GetBookInfo", "正则提取最后更新成功: " + lasttime);
                }
            }

            // 4. 获取最新章节
            Element newChapterElement = alldoc.selectFirst("#info p:contains(最新章节)");
            if (newChapterElement != null) {
                String newChapterText = newChapterElement.text().trim();
                Log.d("GetBookInfo", "最新章节原始文本: " + newChapterText);

                // 获取链接
                Element chapterLinkElement = newChapterElement.selectFirst("a");
                if (chapterLinkElement != null) {
                    newchapter = chapterLinkElement.text().trim();
                    newchapterlink = chapterLinkElement.attr("href");
                    Log.d("GetBookInfo", "从链接提取最新章节: " + newchapter);
                } else {
                    // 如果没有链接，从文本提取
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("最新章节[：:]\\s*(.+)");
                    java.util.regex.Matcher matcher = pattern.matcher(newChapterText);
                    if (matcher.find()) {
                        newchapter = matcher.group(1).trim();
                        Log.d("GetBookInfo", "正则提取最新章节: " + newchapter);
                    }
                }
            }

            // 5. 获取简介
            Element introElement = alldoc.selectFirst("#intro");
            if (introElement != null) {
                info = introElement.text().trim();
                // 清理简介文本
                info = info.replace("各位书友要是觉得", "")
                        .replace("还不错的话请不要忘记向您QQ群和微博里的朋友推荐哦！", "")
                        .trim();
                Log.d("GetBookInfo", "获取简介成功，长度: " + info.length());
            }

            // 6. 获取封面图片
            Element imgElement = alldoc.selectFirst("#sidebar img, #fmimg img");
            if (imgElement != null) {
                piclink = imgElement.attr("src");
                // 提取picname
                if (url.contains("_")) {
                    picname = url.replace("_", "");
                } else {
                    picname = url;
                }

                // 确保链接完整
                if (piclink.startsWith("//")) {
                    piclink = "https:" + piclink;
                } else if (piclink.startsWith("/")) {
                    piclink = "https://www.uuubqg.cc" + piclink;
                }

                Log.d("GetBookInfo", "获取封面成功: " + piclink);
            }

            // 7. 计算章节数
            Elements chapterElements = alldoc.select("#list dl dd a");
            if (!chapterElements.isEmpty()) {
                chapternum = chapterElements.size();
                Log.d("GetBookInfo", "总章节数: " + chapternum);
            }

            Log.d("GetBookInfo", "=== 书籍信息汇总 ===");
            Log.d("GetBookInfo", "书名: " + name);
            Log.d("GetBookInfo", "作者: " + author);
            Log.d("GetBookInfo", "最后更新: " + lasttime);
            Log.d("GetBookInfo", "最新章节: " + newchapter);
            Log.d("GetBookInfo", "章节数: " + chapternum);
            Log.d("GetBookInfo", "==================");

        } catch (Exception e) {
            Log.e("GetBookInfo", "获取书籍信息失败: " + e.getMessage());
            e.printStackTrace();
        }

        BookInfo book = new BookInfo(name, author, url, picname, piclink, info, lasttime, newchapter, newchapterlink, chapternum);
        return book;
    }

}
