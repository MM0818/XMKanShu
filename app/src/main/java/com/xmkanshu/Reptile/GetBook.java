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

    public static ArrayList fengtui() {
        ArrayList<HashMap<String, String>> list;
        Document alldoc;
        String link;//书链接
        list = new ArrayList<HashMap<String, String>>();
        try {
            alldoc = Jsoup.connect("http://www.uuubqg.cc/")
                    .data("query", "Java")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.108 Safari/537.36")
                    .get();
            Elements listClass = alldoc.getElementsByAttributeValue("class", "item");
            for (Element listitem : listClass) {
                HashMap<String, String> book = new HashMap<String, String>();

                try {
                    link = listitem.getElementsByTag("a").attr("href");//获取书籍link
                } catch (Exception e) {
                    link = "";
                }

                String id = link.replace("/", "");
                final BookInfo bookInfo = BookInfoCache.loadBook(id);

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
                final String id = url.replace("/", "");
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

        try {
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

            Element authorElement = alldoc.selectFirst("#info p:contains(作)");
            if (authorElement != null) {
                String authorText = authorElement.text().trim();
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

        BookInfo book = new BookInfo(name, author, id, picname, piclink, info, lasttime, newchapter, newchapterlink, chapternum);
        Log.d(TAG, "书籍信息对象创建完成: " + book.getName());
        return book;
    }

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
            Log.d(TAG, "爬取正文卷章节数: " + chapterList.size());
        } catch (Exception e) {
            Log.e(TAG, "爬取章节失败: " + e.getMessage());
            e.printStackTrace();
        }
        return chapterList;
    }
}