//package com.xmkanshu.Adapter;
//
//import android.graphics.Color;
//import android.os.AsyncTask;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.view.GravityCompat;
//
//import com.hb.dialog.dialog.LoadingDialog;
//import com.xmkanshu.Data.GlobalConfig;
//import com.xmkanshu.Data.ReadConfig;
//import com.xmkanshu.R;
//import com.xmkanshu.Reptile.GetAndRead;
//import com.xmkanshu.UI.ReadingActivity;
//
//import java.util.ArrayList;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @author ZQZESS
// * @date 1/6/2021.
// * @file ChapterAdapter
// * GitHub：https://github.com/zqzess
// * 不会停止运行的app不是好app w(ﾟДﾟ)w
// */
//public class ChapterAdapter extends BaseAdapter {
//    private ArrayList<ConcurrentHashMap<String, String>> list;
//    ReadingActivity activity;
//    LoadingDialog loadingDialog;
//
//    public ChapterAdapter(ArrayList<ConcurrentHashMap<String, String>> list, ReadingActivity activity) {
//        this.list = list;
//        this.activity = activity;
//    }
//
//    @Override
//    public int getCount() {
//        return list.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return list.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    @NonNull
//    @Override
//    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        View v = LayoutInflater.from(activity).inflate(R.layout.listview_chapter_list, null);
//        TextView textView=v.findViewById(R.id.tv_listview_chapter_list_name);
//        if(position== GlobalConfig.chapternow)
//        {
//            textView.setTextColor(Color.RED);
//        }else
//        {
//            textView.setTextColor(activity.getResources().getColor(ReadConfig.fontColor));
//        }
//        textView.setText(list.get(position).get("title"));
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loadingDialog=new LoadingDialog(activity);
//                GlobalConfig.chapternow=position;
//                GlobalConfig.Page=0;
////                activity.mReadPresenter.LoadChapterContent();
////                GlobalConfig.SaveReadSetting(activity);//保存阅读进度
////                activity.tv_read.setImageBitmap(activity.mReadPresenter.changePageContent(GlobalConfig.Page));
////                GetAndRead.ReadingBackground(GlobalConfig.chapternow);
//                new setChapter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//                activity.drawerLayout.closeDrawer(GravityCompat.START);
//            }
//        });
//        return v;
//    }
//    private class setChapter extends AsyncTask<Void,Integer,Boolean>
//    {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            loadingDialog.setMessage("加载中...");
//            loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
//            loadingDialog.setCanceledOnTouchOutside(false); // 点击加载框以外的区域
//            loadingDialog.show();
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            activity.mReadPresenter.LoadChapterContent();
//            GlobalConfig.SaveReadSetting(activity);//保存阅读进度
//            GetAndRead.ReadingBackground(GlobalConfig.chapternow);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            super.onPostExecute(aBoolean);
//            activity.tv_read.setImageBitmap(activity.mReadPresenter.changePageContent(GlobalConfig.Page));
//            loadingDialog.dismiss();
//        }
//    }
//}


package com.xmkanshu.Adapter;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;

import com.hb.dialog.dialog.LoadingDialog;
import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.Data.ReadConfig;
import com.xmkanshu.Model.Chapter; // 1. 导入 Chapter 类
import com.xmkanshu.R;
import com.xmkanshu.Reptile.GetAndRead;
import com.xmkanshu.UI.ReadingActivity;

import java.util.ArrayList;

/**
 * @author ZQZESS
 * @date 1/6/2021.
 * @file ChapterAdapter
 * GitHub：https://github.com/zqzess
 * 不会停止运行的app不是好app w(ﾟДﾟ)w
 */
public class ChapterAdapter extends BaseAdapter {
    // 2. 把泛型从 ConcurrentHashMap 改成 Chapter
    private ArrayList<Chapter> list;
    ReadingActivity activity;
    LoadingDialog loadingDialog;

    // 3. 构造方法参数也改成 ArrayList<Chapter>
    public ChapterAdapter(ArrayList<Chapter> list, ReadingActivity activity) {
        this.list = list;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    // 4. getItem 返回值改为 Chapter（可选，优化类型安全）
    @Override
    public Chapter getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position; // 原返回 0 不好，改成返回 position（唯一标识）
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = LayoutInflater.from(activity).inflate(R.layout.listview_chapter_list, null);
        TextView textView = v.findViewById(R.id.tv_listview_chapter_list_name);

        // 5. 获取当前章节对象（用 Chapter 替代 HashMap）
        Chapter currentChapter = list.get(position);

        // 章节选中状态（红色）逻辑不变
        if (position == GlobalConfig.chapternow) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(activity.getResources().getColor(ReadConfig.fontColor));
        }

        // 6. 用 Chapter 的 getTitle() 方法获取标题（替代 map.get("title")）
        textView.setText(currentChapter.getTitle());

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = new LoadingDialog(activity);
                GlobalConfig.chapternow = position;
                GlobalConfig.Page = 0;
                new setChapter().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                activity.drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        return v;
    }

    private class setChapter extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.setMessage("加载中...");
            loadingDialog.setCancelable(true);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            activity.mReadPresenter.LoadChapterContent();
            GlobalConfig.SaveReadSetting(activity);
            GetAndRead.ReadingBackground(GlobalConfig.chapternow);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            activity.tv_read.setImageBitmap(activity.mReadPresenter.changePageContent(GlobalConfig.Page));
            loadingDialog.dismiss();
        }
    }
}