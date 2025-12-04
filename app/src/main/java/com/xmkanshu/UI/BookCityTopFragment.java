//package com.xmkanshu.UI;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ListView;
//
//import com.hb.dialog.dialog.LoadingDialog;
//import com.xmkanshu.Adapter.BookListAdapter2;
//import com.xmkanshu.R;
//import com.xmkanshu.Reptile.GetBook;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
///**
// * @author ZQZESS
// * @date 12/9/2020-12:56 AM
// * @file BookCityTopFragment.java
// * GitHub：https://github.com/zqzess
// *不会停止运行的app不是好app w(ﾟДﾟ)w
// */
//public class BookCityTopFragment extends Fragment {
//    ListView listView;
//    ListView listView2;
//    private ArrayList<HashMap<String, String>> list;
//    private ArrayList<HashMap<String, String>> list2;
//    private ArrayList<HashMap<String, String>> list3;
//    LoadingDialog loadingDialog;
//    /**
//     * 延迟时间
//     */
//    private static final int DELAY_TIME = 70;
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getContext();
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.activity_bookcity_topfragment, container, false);
//
//        listView = (ListView)view.findViewById(R.id.listview_fengtui);
//        listView2 = (ListView)view.findViewById(R.id.listview_qiangtui);
//        loadingDialog= new LoadingDialog(BookCityTopFragment.this.getActivity());
//        new FengTuiTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        new QiangTuiTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        return view;
//    }
//
//    public class FengTuiTask extends AsyncTask<Void,Integer,Boolean>
//    {
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            try {
//                    list=GetBook.fengtui();
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            return true;
//        }
//
//        //执行前调用
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            loadingDialog.setMessage("loading");
//            loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
//            loadingDialog.setCanceledOnTouchOutside(false); // 点击加载框以外的区域
//            loadingDialog.show();
//        }
//
//        //执行完成后调用
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            super.onPostExecute(aBoolean);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    BookListAdapter2 adapter = new BookListAdapter2(list,BookCityTopFragment.this.getActivity());
//                    listView.setAdapter(adapter);
//                }
//            },DELAY_TIME);
//
//        }
//    }
//
//    public class QiangTuiTask extends AsyncTask<Void,Integer,Boolean>
//    {
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            try {
//                list2=GetBook.qiangtui();
//            }catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            super.onPostExecute(aBoolean);
//            loadingDialog.dismiss();
//            BookListAdapter2 adapter = new BookListAdapter2(list2,BookCityTopFragment.this.getActivity());
//            listView2.setAdapter(adapter);
//        }
//    }
//}




package com.xmkanshu.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hb.dialog.dialog.LoadingDialog;
import com.xmkanshu.Adapter.BookListAdapter2;
import com.xmkanshu.Adapter.FengTuiAdapter;
import com.xmkanshu.Data.BookInfo;
import com.xmkanshu.Data.BookStoreData;
import com.xmkanshu.R;
import com.xmkanshu.Reptile.GetAndRead;
import com.xmkanshu.Reptile.GetBook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author ZQZESS
 * @date 12/9/2020-12:56 AM
 * @file BookCityTopFragment.java
 * GitHub：https://github.com/zqzess
 *不会停止运行的app不是好app w(ﾟДﾟ)w
 */
public class BookCityTopFragment extends Fragment {
    ListView listView;
    ListView listView2;
    ListView listViewNewBooks;   // 最新入库
    ListView listViewRecentUpdate; // 最近更新

    private ArrayList<HashMap<String, String>> list;        // 封面推荐数据
    private ArrayList<HashMap<String, String>> list2;       // 上期强推数据
    private ArrayList<HashMap<String, String>> newBooksList;     // 最新入库数据
    private ArrayList<HashMap<String, String>> recentUpdateList; // 最近更新数据

    LoadingDialog loadingDialog;

    // 适配器
    private FengTuiAdapter fengTuiAdapter;
    private BookListAdapter2 qiangTuiAdapter;
    private BookListAdapter2 newBooksAdapter;
    private BookListAdapter2 recentUpdateAdapter;

    /**
     * 延迟时间
     */
    private static final int DELAY_TIME = 70;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bookcity_topfragment, container, false);

        listView = (ListView)view.findViewById(R.id.listview_fengtui);
        listView2 = (ListView)view.findViewById(R.id.listview_qiangtui);

        // 初始化新的ListView
        listViewNewBooks = (ListView)view.findViewById(R.id.listview_newbooks);
        listViewRecentUpdate = (ListView)view.findViewById(R.id.listview_recentupdate);

        loadingDialog= new LoadingDialog(BookCityTopFragment.this.getActivity());

        // 初始化数据源
        initDataSources();

        // 只使用新的书城数据任务，不再使用原有的 FengTuiTask 和 QiangTuiTask
        new BookStoreDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return view;
    }

    private void initDataSources() {
//        list = new ArrayList<>();
//        list2 = new ArrayList<>();
//        newBooksList = new ArrayList<>();
//        recentUpdateList = new ArrayList<>();
//
//        // 初始化适配器
//        fengTuiAdapter = new FengTuiAdapter(list, getActivity());
//        qiangTuiAdapter = new BookListAdapter2(list2, getActivity());
//        newBooksAdapter = new BookListAdapter2(newBooksList, getActivity());
//        recentUpdateAdapter = new BookListAdapter2(recentUpdateList, getActivity());
//
//        // 设置适配器
//        listView.setAdapter(fengTuiAdapter);
//        listView2.setAdapter(qiangTuiAdapter);
//        if (listViewNewBooks != null) {
//            listViewNewBooks.setAdapter(newBooksAdapter);
//        }
//        if (listViewRecentUpdate != null) {
//            listViewRecentUpdate.setAdapter(recentUpdateAdapter);
//        }

        list = new ArrayList<>();
        list2 = new ArrayList<>();
        newBooksList = new ArrayList<>();
        recentUpdateList = new ArrayList<>();

        // 初始化适配器
        fengTuiAdapter = new FengTuiAdapter(list, getActivity());
        qiangTuiAdapter = new BookListAdapter2(list2, getActivity());
        newBooksAdapter = new BookListAdapter2(newBooksList, getActivity());
        recentUpdateAdapter = new BookListAdapter2(recentUpdateList, getActivity());

        // 设置封面推荐的点击监听 - 添加详细日志
        fengTuiAdapter.setOnBookItemClickListener(new FengTuiAdapter.OnBookItemClickListener() {
            @Override
            public void onBookItemClick(int position) {
                Log.d("BookCityTop", "封面推荐点击事件触发，位置: " + position);
                // 检查位置是否有效
                if (position >= 0 && position < list.size()) {
                    HashMap<String, String> book = list.get(position);
                    Log.d("BookCityTop", "点击的书籍: " + book.get("name") + ", 链接: " + book.get("link"));
                    openBookReading(book);
                } else {
                    Log.e("BookCityTop", "无效的位置: " + position + ", 列表大小: " + list.size());
                }
            }
        });

        // 设置适配器
        listView.setAdapter(fengTuiAdapter);
        listView2.setAdapter(qiangTuiAdapter);
        if (listViewNewBooks != null) {
            listViewNewBooks.setAdapter(newBooksAdapter);
        }
        if (listViewRecentUpdate != null) {
            listViewRecentUpdate.setAdapter(recentUpdateAdapter);
        }

    }

    // 在 BookCityTopFragment 类中添加这个方法
    private void openBookReading(HashMap<String, String> book) {
        if (book != null && book.get("link") != null) {
            String bookLink = book.get("link");
            String bookName = book.get("name");
            String author = book.get("author");
            String info = book.get("info");
            String picname = book.get("picname");
            String piclink = book.get("piclink");
            String lasttime = book.get("lasttime");
            String newchapter = book.get("newchapter");
            String chapternum = book.get("chapternum");

            // 详细日志：检查传递的所有数据
            Log.d("BookCityTop", "=== 书籍详情页跳转数据 ===");
            Log.d("BookCityTop", "书名: " + bookName);
            Log.d("BookCityTop", "作者: " + (author != null ? author : "null"));
            Log.d("BookCityTop", "最后更新: " + (lasttime != null ? lasttime : "null"));
            Log.d("BookCityTop", "最新章节: " + (newchapter != null ? newchapter : "null"));
            Log.d("BookCityTop", "章节数: " + (chapternum != null ? chapternum : "null"));
            Log.d("BookCityTop", "图片链接: " + (piclink != null ? piclink : "null"));
            Log.d("BookCityTop", "简介: " + (info != null ? info.substring(0, Math.min(30, info.length())) + "..." : "null"));
            Log.d("BookCityTop", "链接: " + bookLink);
            Log.d("BookCityTop", "==========================");

            // 跳转到书籍详情页
            Intent intent = new Intent(getActivity(), BookInfoDetailActivity.class);
            intent.putExtra("name", bookName);
            intent.putExtra("author", author != null ? author : "");
            intent.putExtra("info", info != null ? info : "");
            intent.putExtra("picname", picname != null ? picname : "");
            intent.putExtra("link", bookLink);
            intent.putExtra("piclink", piclink != null ? piclink : "");
            intent.putExtra("lasttime", lasttime != null ? lasttime : "");
            intent.putExtra("newchapter", newchapter != null ? newchapter : "");
            intent.putExtra("chapternum", chapternum != null ? chapternum : "0");
            startActivity(intent);
        } else {
            Log.e("BookCityTop", "书籍数据为空或链接无效");
        }
    }

    // 将BookInfo转换为HashMap格式
    // 在 convertBookInfoToMap 方法中生成 picname
    private HashMap<String, String> convertBookInfoToMap(BookInfo book) {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", book.getName() != null ? book.getName() : "");
        map.put("author", book.getAuthor() != null ? book.getAuthor() : "");
        map.put("link", book.getLink() != null ? book.getLink() : "");

        // 关键修复：picname应该保持原始格式，不要去掉下划线！
        String link = book.getLink() != null ? book.getLink() : "";
        String picname = "";
        if (!link.isEmpty()) {
            // 从 "/137_137159/" 中提取 "137_137159"
            // 不要去掉下划线！
            String[] parts = link.split("/");
            for (String part : parts) {
                if (part.contains("_")) {
                    picname = part;  // 保持 "137_137159" 格式
                    break;
                }
            }
            // 如果没找到带下划线的部分，再尝试其他方法
            if (picname.isEmpty()) {
                picname = link.replace("/", "").replace("_", "");
            }
        }
        map.put("picname", picname);

        map.put("piclink", book.getPiclink() != null ? book.getPiclink() : "");
        map.put("info", book.getInfo() != null ? book.getInfo() : "");
        map.put("lasttime", book.getLasttime() != null ? book.getLasttime() : "");
        map.put("newchapter", book.getNewchapter() != null ? book.getNewchapter() : "");
        map.put("newchapterlink", book.getNewchapterlink() != null ? book.getNewchapterlink() : "");
        map.put("chapternum", String.valueOf(book.getChapternum()));

        Log.d("BookCityTop", "convertBookInfoToMap - picname: " + picname + ", link: " + link);

        return map;
    }

    // 新增：书城数据任务
    public class BookStoreDataTask extends AsyncTask<Void, Void, BookStoreData>
    {
        @Override
        protected BookStoreData doInBackground(Void... voids) {
            try {
                // 调用我们新增的方法获取完整的书城数据
                return GetAndRead.getBookStoreData("https://www.biqugeu.net");
            } catch (Exception e) {
                Log.e("BookCityTop", "获取书城数据失败: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(BookStoreData bookStoreData) {
            super.onPostExecute(bookStoreData);

            if (bookStoreData != null) {
                // 更新封面推荐（使用新的数据源）
                updateCoverRecommendations(bookStoreData.getCoverRecommendations());
                // 更新上期强推（使用新的数据源）
                updateStrongRecommendations(bookStoreData.getStrongRecommendations());
                // 更新最近更新（使用新的数据源）
                updateRecentUpdates(bookStoreData.getRecentUpdates());
                // 更新最新入库（使用新的数据源）
                updateNewBooks(bookStoreData.getNewBooks());

                Log.d("BookCityTop", "书城数据加载完成 - 所有栏目都使用新数据源");
            } else {
                Log.e("BookCityTop", "书城数据加载失败");
            }

            // 所有任务完成后关闭加载对话框
            loadingDialog.dismiss();
        }
    }

    // 更新封面推荐
    // 在 updateCoverRecommendations 方法中修改：
    private void updateCoverRecommendations(List<BookInfo> books) {
        if (books == null || books.isEmpty()) {
            Log.d("BookCityTop", "封面推荐数据为空");
            return;
        }

        Log.d("BookCityTop", "封面推荐数量: " + books.size());
        list.clear();

        for (int i = 0; i < books.size(); i++) {
            BookInfo book = books.get(i);

            // 详细检查BookInfo对象的数据
            Log.d("BookCityTop", "封面推荐[" + i + "] - BookInfo对象:");
            Log.d("BookCityTop", "  name: " + book.getName());
            Log.d("BookCityTop", "  author: " + book.getAuthor());
            Log.d("BookCityTop", "  lasttime: " + book.getLasttime());
            Log.d("BookCityTop", "  newchapter: " + book.getNewchapter());
            Log.d("BookCityTop", "  chapternum: " + book.getChapternum());
            Log.d("BookCityTop", "  link: " + book.getLink());

            HashMap<String, String> map = convertBookInfoToMap(book);
            list.add(map);
        }

        fengTuiAdapter.notifyDataSetChanged();
    }

    // 更新上期强推
    private void updateStrongRecommendations(List<BookInfo> books) {
        if (books == null || books.isEmpty()) {
            Log.d("BookCityTop", "强力推荐数据为空");
            return;
        }

        Log.d("BookCityTop", "强力推荐数量: " + books.size());
        list2.clear();  // 使用 list2 而不是 qiangTuiList

        for (BookInfo book : books) {
            HashMap<String, String> map = convertBookInfoToMap(book);
            list2.add(map);  // 使用 list2 而不是 qiangTuiList
        }

        qiangTuiAdapter.notifyDataSetChanged();
    }

    // 更新最新入库
    private void updateNewBooks(List<BookInfo> books) {
        if (books == null || books.isEmpty() || listViewNewBooks == null) {
            Log.d("BookCityTop", "最新入库数据为空或ListView未初始化");
            return;
        }

        newBooksList.clear();
        for (BookInfo book : books) {
            HashMap<String, String> map = convertBookInfoToMap(book);
            newBooksList.add(map);
        }

        newBooksAdapter.notifyDataSetChanged();
        Log.d("BookCityTop", "最新入库更新: " + newBooksList.size() + " 本书");
    }

    // 更新最近更新
    private void updateRecentUpdates(List<BookInfo> books) {
        if (books == null || books.isEmpty() || listViewRecentUpdate == null) {
            Log.d("BookCityTop", "最近更新数据为空或ListView未初始化");
            return;
        }

        recentUpdateList.clear();
        for (BookInfo book : books) {
            HashMap<String, String> map = convertBookInfoToMap(book);
            recentUpdateList.add(map);
        }

        recentUpdateAdapter.notifyDataSetChanged();
        Log.d("BookCityTop", "最近更新更新: " + recentUpdateList.size() + " 本书");
    }
}