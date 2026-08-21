package com.xmkanshu.UI;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xmkanshu.Adapter.BookShelfAdapter;
import com.xmkanshu.Manager.LocalBookManager;
import com.xmkanshu.Manager.LocalBookParser;
import com.xmkanshu.Model.Chapter;
import com.xmkanshu.R;
import com.xmkanshu.greendao.DaoHelper;
import com.xmkanshu.greendao.model.Bookinfodb;

import java.util.List;


public class BookShelfFragment extends Fragment {
    TextView tv_search;
    TextView tv_import;
    private DaoHelper mDb;
    ListView listView;
    BookShelfAdapter adapter;
    SwipeRefreshLayout refreshP;
    List<Bookinfodb> list;
    Context context;
    private LocalBookManager localBookManager;

    // SAF文件选择器
    private ActivityResultLauncher<Intent> filePickerLauncher;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册文件选择器
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            importLocalBook(uri);
                        }
                    }
                }
        );
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bookshelf, container, false);
        initView(view);
        ClickEvent();
        return view;
    }
    private void initView(View view)
    {
        context=this.getActivity();
        tv_search=(TextView) view.findViewById(R.id.tv_bookshelf_search);
        tv_import=(TextView) view.findViewById(R.id.tv_bookshelf_import);
        listView=(ListView)view.findViewById(R.id.listview_bookshelf);
        refreshP=(SwipeRefreshLayout)view.findViewById(R.id.bookshelf_refresh);
        mDb=DaoHelper.getInstance(context);
        localBookManager = new LocalBookManager(context);
        list=mDb.searchAll();
        adapter=new BookShelfAdapter(list,BookShelfFragment.this.getActivity());
        adapter.setOnBookLongClickListener((position, book) -> showDeleteDialog(book));
        listView.setAdapter(adapter);

    }
    private void ClickEvent()
    {
        tv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(BookShelfFragment.this.getActivity(),SearchActivity.class);
                startActivity(intent);
            }
        });

        // 导入本地书籍按钮
        tv_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        refreshP.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });
    }

    /**
     * 打开文件选择器
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain"); // 只显示txt文件
        filePickerLauncher.launch(intent);
    }

    /**
     * 导入本地书籍
     */
    private void importLocalBook(Uri uri) {
        // 获取文件名
        String fileName = getFileNameFromUri(uri);
        if (fileName == null || !fileName.endsWith(".txt")) {
            Toast.makeText(context, "请选择txt格式的文件", Toast.LENGTH_SHORT).show();
            return;
        }

        // 生成唯一文件名
        String uniqueFileName = localBookManager.generateUniqueFileName(fileName);

        // 复制文件到本地
        String localPath = localBookManager.copyFileToLocal(uri, uniqueFileName);
        if (localPath == null) {
            Toast.makeText(context, "导入失败：无法复制文件", Toast.LENGTH_SHORT).show();
            return;
        }

        // 解析章节
        List<Chapter> chapters = LocalBookParser.parseChapters(localPath);
        if (chapters.isEmpty()) {
            Toast.makeText(context, "导入失败：无法解析章节", Toast.LENGTH_SHORT).show();
            return;
        }

        // 提取书名
        String bookName = LocalBookParser.extractBookName(fileName);

        // 保存到数据库
        Bookinfodb book = new Bookinfodb();
        book.setName(bookName);
        book.setAuthor("未知作者");
        book.setLink(localPath); // 本地路径作为link
        book.setPiclink(""); // 无封面
        book.setInfo("本地导入书籍");
        book.setLasttime("");
        book.setNewchapter(chapters.get(chapters.size() - 1).getTitle());
        book.setNewchapterlink("");
        book.setChapternum(chapters.size());
        book.setLinkfrom("local"); // 标记为本地书籍

        mDb.insert(book);

        // 刷新书架
        Refresh();

        Toast.makeText(context, "导入成功：" + bookName, Toast.LENGTH_SHORT).show();
    }

    /**
     * 从Uri获取文件名
     */
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try {
                android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        fileName = cursor.getString(index);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    void Refresh()
    {
        list.clear();
        list.addAll(mDb.searchAll());
        adapter.notifyDataSetChanged();
        refreshP.setRefreshing(false);
    }

    /**
     * 显示删除确认弹窗
     */
    private void showDeleteDialog(Bookinfodb book) {
        new AlertDialog.Builder(context)
                .setTitle("删除书籍")
                .setMessage("确定要删除《" + book.getName() + "》吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 删除数据库记录
                    mDb.delete(book.getBookid());

                    // 如果是本地书籍，同时删除文件
                    if ("local".equals(book.getLinkfrom())) {
                        String filePath = book.getLink();
                        if (filePath != null) {
                            java.io.File file = new java.io.File(filePath);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }

                    // 刷新书架
                    Refresh();
                    Toast.makeText(context, "已删除《" + book.getName() + "》", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    @Override
    public void onStart() {
        super.onStart();
        refreshP.post(new Runnable() {
            @Override
            public void run() {
                refreshP.setRefreshing(true);
                Refresh();
            }
        });
    }
}
