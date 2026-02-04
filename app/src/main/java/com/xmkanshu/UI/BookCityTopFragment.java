package com.xmkanshu.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.hb.dialog.dialog.LoadingDialog;
import com.xmkanshu.Adapter.BookGridRecyclerAdapter;
import com.xmkanshu.Adapter.CustomGridLayoutManager;
import com.xmkanshu.Data.BookInfo;
import com.xmkanshu.Data.BookStoreData;
import com.xmkanshu.Manager.BookDataManager;
import com.xmkanshu.R;
import com.xmkanshu.Reptile.GetAndRead;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BookCityTopFragment extends Fragment {
    private RecyclerView recyclerViewFengTui;
    private RecyclerView recyclerViewQiangTui;
    private RecyclerView recyclerViewNewBooks;
    private RecyclerView recyclerViewRecentUpdate;

    private ArrayList<HashMap<String, String>> list;
    private ArrayList<HashMap<String, String>> list2;
    private ArrayList<HashMap<String, String>> newBooksList;
    private ArrayList<HashMap<String, String>> recentUpdateList;

    private LoadingDialog loadingDialog;

    private BookGridRecyclerAdapter fengTuiAdapter;
    private BookGridRecyclerAdapter qiangTuiAdapter;
    private BookGridRecyclerAdapter newBooksAdapter;
    private BookGridRecyclerAdapter recentUpdateAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bookcity_topfragment, container, false);

        recyclerViewFengTui = view.findViewById(R.id.recyclerview_fengtui);
        recyclerViewQiangTui = view.findViewById(R.id.recyclerview_qiangtui);
        recyclerViewNewBooks = view.findViewById(R.id.recyclerview_newbooks);
        recyclerViewRecentUpdate = view.findViewById(R.id.recyclerview_recentupdate);

        loadingDialog = new LoadingDialog(requireActivity());

        initDataSources();
        setupRecyclerViewLayoutManagers();
        setupScrollConflictResolution();

        //否掉手动
        //new BookStoreDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // 核心修改：从BookDataManager获取预加载数据
        loadBookStoreData();

        return view;
    }

    //后台抓取相关方法
    private void loadBookStoreData() {
        // 显示加载对话框
        loadingDialog.show();

        // 尝试获取缓存数据
        BookStoreData bookStoreData = BookDataManager.getInstance().getBookCityCache();
        if (bookStoreData != null) {
            // 数据已预加载完成，直接更新UI
            updateBookStoreUI(bookStoreData);
            loadingDialog.dismiss();
        } else {
            // 数据未加载完成，注册监听
            BookDataManager.getInstance().setOnBookDataLoadListener(new BookDataManager.OnBookDataLoadListener() {
                @Override
                public void onLoadCompleted(BookStoreData data) {
                    // 主线程更新UI
                    updateBookStoreUI(data);
                    loadingDialog.dismiss();
                }

                @Override
                public void onLoadFailed(String error) {
                    Log.e("BookCityTop", "书城数据加载失败: " + error);
                    loadingDialog.dismiss();
                }
            });
        }
    }
    //后台抓取相关方法
    private void updateBookStoreUI(BookStoreData data) {
        if (data == null) return;

        updateCoverRecommendations(data.getCoverRecommendations());
        updateStrongRecommendations(data.getStrongRecommendations());
        updateRecentUpdates(data.getRecentUpdates());
        updateNewBooks(data.getNewBooks());

        Log.d("BookCityTop", "书城数据加载完成（预加载）");
    }

    private void initDataSources() {
        list = new ArrayList<>();
        list2 = new ArrayList<>();
        newBooksList = new ArrayList<>();
        recentUpdateList = new ArrayList<>();

        fengTuiAdapter = new BookGridRecyclerAdapter(list, requireActivity());
        qiangTuiAdapter = new BookGridRecyclerAdapter(list2, requireActivity());
        newBooksAdapter = new BookGridRecyclerAdapter(newBooksList, requireActivity());
        recentUpdateAdapter = new BookGridRecyclerAdapter(recentUpdateList, requireActivity());

        setupAdaptersClickListeners();

        recyclerViewFengTui.setAdapter(fengTuiAdapter);
        recyclerViewQiangTui.setAdapter(qiangTuiAdapter);
        recyclerViewNewBooks.setAdapter(newBooksAdapter);
        recyclerViewRecentUpdate.setAdapter(recentUpdateAdapter);
    }

    private void setupRecyclerViewLayoutManagers() {
        // 使用自定义的GridLayoutManager，双列布局
        CustomGridLayoutManager layoutManager1 = new CustomGridLayoutManager(requireActivity(), 2);
        CustomGridLayoutManager layoutManager2 = new CustomGridLayoutManager(requireActivity(), 2);
        CustomGridLayoutManager layoutManager3 = new CustomGridLayoutManager(requireActivity(), 2);
        CustomGridLayoutManager layoutManager4 = new CustomGridLayoutManager(requireActivity(), 2);

        // 设置LayoutManager
        recyclerViewFengTui.setLayoutManager(layoutManager1);
        recyclerViewQiangTui.setLayoutManager(layoutManager2);
        recyclerViewNewBooks.setLayoutManager(layoutManager3);
        recyclerViewRecentUpdate.setLayoutManager(layoutManager4);
    }

    /**
     * 设置滚动冲突解决方案
     * 关键：当RecyclerView可以滚动时，让它自己处理；不能滚动时，交给父ScrollView
     */
    private void setupScrollConflictResolution() {
        setupRecyclerViewTouchListener(recyclerViewFengTui);
        setupRecyclerViewTouchListener(recyclerViewQiangTui);
        setupRecyclerViewTouchListener(recyclerViewNewBooks);
        setupRecyclerViewTouchListener(recyclerViewRecentUpdate);
    }

    private void setupRecyclerViewTouchListener(RecyclerView recyclerView) {
        if (recyclerView == null) return;

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private float startY;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = e.getY();
                        // 开始触摸时，告诉父View不要拦截
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float currentY = e.getY();
                        float deltaY = currentY - startY;

                        // 检查RecyclerView是否可以滚动
                        boolean canScrollUp = rv.canScrollVertically(-1);
                        boolean canScrollDown = rv.canScrollVertically(1);

                        // 如果是垂直滑动
                        if (Math.abs(deltaY) > 10) {
                            // 滑动到顶部并且向下滑动
                            if (!canScrollUp && deltaY > 0) {
                                // 允许父View拦截，让NestedScrollView处理
                                rv.getParent().requestDisallowInterceptTouchEvent(false);
                            }
                            // 滑动到底部并且向上滑动
                            else if (!canScrollDown && deltaY < 0) {
                                // 允许父View拦截，让NestedScrollView处理
                                rv.getParent().requestDisallowInterceptTouchEvent(false);
                            }
                            // 在中间区域滑动
                            else {
                                // RecyclerView自己处理
                                rv.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }

    private void setupAdaptersClickListeners() {
        fengTuiAdapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < list.size()) {
                openBookReading(list.get(position));
            }
        });

        qiangTuiAdapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < list2.size()) {
                openBookReading(list2.get(position));
            }
        });

        newBooksAdapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < newBooksList.size()) {
                openBookReading(newBooksList.get(position));
            }
        });

        recentUpdateAdapter.setOnItemClickListener(position -> {
            if (position >= 0 && position < recentUpdateList.size()) {
                openBookReading(recentUpdateList.get(position));
            }
        });
    }

    private void openBookReading(HashMap<String, String> book) {
        if (book != null && book.get("link") != null) {
            // 打印传递的数据用于调试
            Log.d("BookCity", "传递到详情页的数据 - 最新章节: " + book.get("newchapter"));
            Log.d("BookCity", "传递到详情页的数据 - 最后更新: " + book.get("lasttime"));

            String bookLink = book.get("link");
            String bookName = book.get("name");
            String author = book.get("author");
            String info = book.get("info");
            String picname = book.get("picname");
            String piclink = book.get("piclink");
            String lasttime = book.get("lasttime");
            String newchapter = book.get("newchapter");
            String chapternum = book.get("chapternum");

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
        }
    }

    private HashMap<String, String> convertBookInfoToMap(BookInfo book) {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", book.getName() != null ? book.getName() : "");
        map.put("author", book.getAuthor() != null ? book.getAuthor() : "");
        map.put("link", book.getLink() != null ? book.getLink() : "");

        String link = book.getLink() != null ? book.getLink() : "";
        String picname = "";
        if (!link.isEmpty()) {
            String[] parts = link.split("/");
            for (String part : parts) {
                if (part.contains("_")) {
                    picname = part;
                    break;
                }
            }
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

        return map;
    }

    public class BookStoreDataTask extends AsyncTask<Void, Void, BookStoreData> {
        @Override
        protected BookStoreData doInBackground(Void... voids) {
            try {
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
                updateCoverRecommendations(bookStoreData.getCoverRecommendations());
                updateStrongRecommendations(bookStoreData.getStrongRecommendations());
                updateRecentUpdates(bookStoreData.getRecentUpdates());
                updateNewBooks(bookStoreData.getNewBooks());

                Log.d("BookCityTop", "书城数据加载完成");
            } else {
                Log.e("BookCityTop", "书城数据加载失败");
            }

            loadingDialog.dismiss();
        }
    }

    private void updateCoverRecommendations(List<BookInfo> books) {
        if (books == null || books.isEmpty()) {
            return;
        }

        list.clear();
        for (BookInfo book : books) {
            HashMap<String, String> map = convertBookInfoToMap(book);
            list.add(map);
        }

        fengTuiAdapter.notifyDataSetChanged();
    }

    private void updateStrongRecommendations(List<BookInfo> books) {
        if (books == null || books.isEmpty()) {
            return;
        }

        list2.clear();
        for (BookInfo book : books) {
            HashMap<String, String> map = convertBookInfoToMap(book);
            list2.add(map);
        }

        qiangTuiAdapter.notifyDataSetChanged();
    }

    private void updateNewBooks(List<BookInfo> books) {
        if (books == null || books.isEmpty()) {
            return;
        }

        newBooksList.clear();
        for (BookInfo book : books) {
            HashMap<String, String> map = convertBookInfoToMap(book);
            newBooksList.add(map);
        }

        newBooksAdapter.notifyDataSetChanged();
    }

    private void updateRecentUpdates(List<BookInfo> books) {
        if (books == null || books.isEmpty()) {
            return;
        }

        recentUpdateList.clear();
        for (BookInfo book : books) {
            HashMap<String, String> map = convertBookInfoToMap(book);
            recentUpdateList.add(map);
        }

        recentUpdateAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //移除BookDataManager的监听
//        if (fengTuiAdapter != null) fengTuiAdapter.clear();
//        if (qiangTuiAdapter != null) qiangTuiAdapter.clear();
//        if (newBooksAdapter != null) newBooksAdapter.clear();
//        if (recentUpdateAdapter != null) recentUpdateAdapter.clear();

        BookDataManager.getInstance().removeOnBookDataLoadListener();

        if (fengTuiAdapter != null) fengTuiAdapter.clear();
        if (qiangTuiAdapter != null) qiangTuiAdapter.clear();
        if (newBooksAdapter != null) newBooksAdapter.clear();
        if (recentUpdateAdapter != null) recentUpdateAdapter.clear();

        // 关闭加载对话框（防止内存泄漏）
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}