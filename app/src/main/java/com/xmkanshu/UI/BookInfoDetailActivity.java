package com.xmkanshu.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hb.dialog.dialog.LoadingDialog;
import com.xmkanshu.Cache.BookInfoCache;
import com.xmkanshu.Cache.ImageCacheManager;
import com.xmkanshu.Data.BookInfo;
import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.R;
import com.xmkanshu.Reptile.GetBook;
import com.xmkanshu.greendao.DaoHelper;
import com.xmkanshu.greendao.model.Bookinfodb;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

/**
 * @author ZQZESS
 * @date 12/9/2020-9:21 PM
 * @file BookInfoDetailActivity.java
 * GitHub：https://github.com/zqzess
 * 不会停止运行的app不是好app w(ﾟДﾟ)w
 */
public class BookInfoDetailActivity extends AppCompatActivity {
    TextView tv_title;
    TextView tv_name;
    TextView tv_info;
    TextView tv_lasttime;
    TextView tv_newchapter;
    TextView tv_author;
    ImageView img_pic;
    Button btn_add;
    Button btn_read;
    String title;   //书名
    String info;    //简介
    String author;  //作者
    String picname; //封面id或书本id，例如6_506
    String piclink; //封面链接
    String link;    //书籍简链接，例如/6_506/
    String newchapter;  //最新章节名
    String lasttime;    //最后更新时间
    int chapternum; //总章节
    BookInfo bookInfo;
    LoadingDialog loadingDialog;
    DaoHelper mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookinfodetail);
        getSupportActionBar().hide();

        findId();
//        initView();
        loadingDialog=new LoadingDialog(this);
        new GetDataTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        mDb=DaoHelper.getInstance(BookInfoDetailActivity.this);
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookInfoDetailActivity.this, ReadingActivity.class);
                intent.putExtra("link", link);
                intent.putExtra("chapternum",chapternum);
                startActivity(intent);
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Long bookid;
                String name;    //书名
                String author;  //作者
                String link;    //书链接
                String piclink; //封面链接
                String info;    //简介
                String lasttime;    //最后更新时间
                String newchapter;  //最新章节
                String newchapterlink;  //最新章节链接
                int chapternum; //总章节
                String linkfrom;    //书源
                 */
//                String name=BookInfoCache.loadBook(link).getName();
                if(btn_add.getText().toString().equals("加入书架"))
                {//加入书架
                    Bookinfodb book=new Bookinfodb(null,title,author,link,piclink,info,lasttime,newchapter,"",chapternum,"biquge");
                    mDb.insertOrReplace(book);
                    if(mDb.search(link)!=null)
                    {
                        Toast.makeText(BookInfoDetailActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
                        btn_add.setText("移出书架");
                    }
                }else if(btn_add.getText().toString().equals("移出书架"))
                {//移出书架
                    mDb.delete(mDb.search(link).getBookid());
                    if(mDb.search(link)==null)
                    {
                        btn_add.setText("加入书架");
                    }
                }
            }
        });
    }

    // 修改 GetData 方法，优先使用传递的数据
    // 修改 GetData 方法，添加日志
    private void GetData() {
        Intent intent = getIntent();
        Log.d("BookInfoDetail", "=== 从书城页面接收的数据 ===");
        // 逐个打印，避免空指针
        Log.d("BookInfoDetail", "书名: " + safeLog(intent.getStringExtra("name")));
        Log.d("BookInfoDetail", "作者: " + safeLog(intent.getStringExtra("author")));
        Log.d("BookInfoDetail", "最后更新: " + safeLog(intent.getStringExtra("lasttime")));
        Log.d("BookInfoDetail", "最新章节: " + safeLog(intent.getStringExtra("newchapter")));
        Log.d("BookInfoDetail", "==========================");

        // 赋值时用空字符串兜底
        title = safeGet(intent, "name");
        info = safeGet(intent, "info");
        link = safeGet(intent, "link");
        author = safeGet(intent, "author");
        picname = safeGet(intent, "picname");
        piclink = safeGet(intent, "piclink");
        lasttime = safeGet(intent, "lasttime");
        newchapter = safeGet(intent, "newchapter");
        String chapternumStr = safeGet(intent, "chapternum");

        // 处理piclink的补全
        if (!piclink.isEmpty() && piclink.startsWith("/")) {
            piclink = "https://www.uuubqg.cc" + piclink;
        }

        // 处理章节数
        try {
            chapternum = Integer.parseInt(chapternumStr);
        } catch (Exception e) {
            chapternum = 0;
        }

        // 数据补充逻辑：只有当数据为空时才从缓存/网络获取
        boolean needRefresh = lasttime.isEmpty() || newchapter.isEmpty();
        Log.d("BookInfoDetail", "是否需要刷新数据: " + needRefresh);
        if (needRefresh) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 清洗link得到id
                        String cleanId = link.replaceAll("https?://[^/]+", "").replaceAll("/+", "_").replaceAll("^_+", "").replaceAll("_+$", "");
                        Log.d("BookInfoDetail", "清洗后的ID: " + cleanId);
                        if (!cleanId.isEmpty()) {
                            BookInfo freshInfo = GetBook.GetBookInfo(cleanId);
                            if (freshInfo != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 只补充为空的字段
                                        if (lasttime.isEmpty()) lasttime = safeGet(freshInfo.getLasttime());
                                        if (newchapter.isEmpty()) newchapter = safeGet(freshInfo.getNewchapter());
                                        if (author.isEmpty()) author = safeGet(freshInfo.getAuthor());
                                        if (title.isEmpty()) title = safeGet(freshInfo.getName());
                                        Log.d("BookInfoDetail", "从网络补充数据后: ");
                                        Log.d("BookInfoDetail", "最后更新: " + lasttime);
                                        Log.d("BookInfoDetail", "最新章节: " + newchapter);
                                        initView(); // 重新初始化UI
                                    }
                                });
                            } else {
                                Log.w("BookInfoDetail", "网络获取书籍信息返回null");
                            }
                        } else {
                            Log.w("BookInfoDetail", "清洗后的ID为空，无法获取数据");
                        }
                    } catch (Exception e) {
                        Log.e("BookInfoDetail", "补充数据失败: " + e.getMessage(), e);
                    }
                }
            }).start();
        } else {
            Log.d("BookInfoDetail", "数据完整，无需刷新");
        }
    }

    // 辅助方法：安全获取Intent参数（避免空指针）
    private String safeGet(Intent intent, String key) {
        return intent.getStringExtra(key) == null ? "" : intent.getStringExtra(key).trim();
    }

    // 辅助方法：安全获取对象属性（避免空指针）
    private String safeGet(String str) {
        return str == null ? "" : str.trim();
    }

    // 辅助方法：安全打印日志（避免空指针）
    private String safeLog(String str) {
        return str == null ? "null" : str;
    }

    private void initView() {
        Log.d("BookInfoDetail", "=== 初始化UI数据 ===");
        Log.d("BookInfoDetail", "设置标题: " + title);
        Log.d("BookInfoDetail", "设置作者: " + (author != null ? author : "null"));
        Log.d("BookInfoDetail", "设置最后更新: " + (lasttime != null ? lasttime : "null"));
        Log.d("BookInfoDetail", "设置最新章节: " + (newchapter != null ? newchapter : "null"));
        Log.d("BookInfoDetail", "==========================");

        tv_title.setText(title);
        tv_name.setText(title);
        tv_info.setText(info);

        // 检查作者TextView是否存在
        if (tv_author != null) {
            tv_author.setText(author != null ? author : "未知作者");
            Log.d("BookInfoDetail", "作者TextView设置完成");
        } else {
            Log.e("BookInfoDetail", "作者TextView为空！");
        }

        // 检查最后更新TextView是否存在
        if (tv_lasttime != null) {
            tv_lasttime.setText(lasttime != null ? lasttime : "未知");
            Log.d("BookInfoDetail", "最后更新TextView设置完成");
        } else {
            Log.e("BookInfoDetail", "最后更新TextView为空！");
        }

        // 检查最新章节TextView是否存在
        if (tv_newchapter != null) {
            tv_newchapter.setText(newchapter != null ? newchapter : "未知");
            Log.d("BookInfoDetail", "最新章节TextView设置完成");
        } else {
            Log.e("BookInfoDetail", "最新章节TextView为空！");
        }
    }

    private void findId() {
        tv_title = findViewById(R.id.tv_bookinfo_detail_title);
        tv_name = findViewById(R.id.tv_bookinfo_detail_name);
        tv_info = findViewById(R.id.tv_bookinfo_detail_info);
        tv_lasttime = findViewById(R.id.tv_bookinfo_detail_lasttime);
        tv_newchapter = findViewById(R.id.tv_bookinfo_detail_newchapter);
        tv_author = findViewById(R.id.tv_bookinfo_detail_author);
        img_pic = findViewById(R.id.img_bookinfo_detail_1);
        btn_add = findViewById(R.id.btn_bookinfo_detail_add);
        btn_read = findViewById(R.id.btn_bookinfo_detail_read);
    }

    private Bitmap getBitmapFromRes(int resId) {
        Resources res = this.getResources();
        return BitmapFactory.decodeResource(res, resId);
    }

    private void preInitBookInfo(String url) {
        Log.d("BookInfoDetail", "preInitBookInfo - 原始URL: " + url);

        String bookId = url;

        // 情况1：如果url是类似"137137159"的纯数字
        if (url != null && url.matches("\\d+") && url.length() > 5) {
            // 不自动转换，因为这样容易出错
            // 直接使用原始的链接来获取
            bookId = url;
            Log.d("BookInfoDetail", "纯数字ID，不转换: " + bookId);
        }
        // 情况2：如果url是链接的一部分，如"/137_137159/"
        else if (url != null && url.contains("/")) {
            // 从链接中提取ID，如从"/137_137159/"提取"137_137159"
            String[] parts = url.split("/");
            for (String part : parts) {
                if (part.contains("_")) {
                    bookId = part;
                    break;
                }
            }
            Log.d("BookInfoDetail", "从链接提取ID: " + url + " -> " + bookId);
        }

        Log.d("BookInfoDetail", "最终使用的书籍ID: " + bookId);

        bookInfo = BookInfoCache.loadBook(bookId);
        if (bookInfo != null) {
            Log.d("BookInfoDetail", "从缓存获取到书籍信息:");
            Log.d("BookInfoDetail", "  作者: " + (bookInfo.getAuthor() != null ? bookInfo.getAuthor() : "空"));
            Log.d("BookInfoDetail", "  最后更新: " + (bookInfo.getLasttime() != null ? bookInfo.getLasttime() : "空"));
            Log.d("BookInfoDetail", "  最新章节: " + (bookInfo.getNewchapter() != null ? bookInfo.getNewchapter() : "空"));
            Log.d("BookInfoDetail", "  章节数: " + bookInfo.getChapternum());
        } else {
            Log.d("BookInfoDetail", "缓存中没有找到书籍信息");
        }
    }


    private class GetDataTask extends AsyncTask<Void,Integer,Boolean>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.setMessage("加载中...");
            loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
            loadingDialog.setCanceledOnTouchOutside(false); // 点击加载框以外的区域
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            GetData();
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            loadingDialog.dismiss();
            initView();
            // 加载封面图片
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.nonepic)
                    .error(R.drawable.nonepic)
                    .centerCrop();
            Glide.with(BookInfoDetailActivity.this)
                    .load(piclink)
                    .apply(options)
                    .into(img_pic);
        }
    }
}
