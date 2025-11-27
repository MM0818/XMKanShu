package com.xmkanshu.Adapter;

import android.app.Activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.ViewUitl.BookItem;
import com.xmkanshu.R;
import com.xmkanshu.UI.BookInfoDetailActivity;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author ZQZESS
 * @date 12/9/2020.
 * @file BookListAdapter2
 * GitHub：https://github.com/zqzess
 * 不会停止运行的app不是好app w(ﾟДﾟ)w
 */
public class BookListAdapter2 extends BaseAdapter {
    private ArrayList<HashMap<String, String>> list;
    private int sumCount;
    Activity activity;
    Bitmap bitmap;
    Bitmap bitmap2;
//    BookInfoCache cache=new BookInfoCache();

    public BookListAdapter2(ArrayList<HashMap<String, String>> list, Activity activity) {
        super();
        this.list = list;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        int count = list.size();
        if (count % 2 == 0) {
            sumCount = count / 2; // 如果是双数直接减半
        } else {
            sumCount = (int) Math.floor((double) count / 2) + 1;
        }

        return sumCount;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // 在 BookListAdapter2 的 getView 方法中修复索引计算
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final BookListAdapter2.ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_bookitem, null);
            holder = new BookListAdapter2.ViewHolder();
            holder.bookItem1 = (BookItem) convertView.findViewById(R.id.bookItem1);
            holder.bookItem2 = (BookItem) convertView.findViewById(R.id.bookItem2);
            convertView.setTag(holder);
        } else {
            holder = (BookListAdapter2.ViewHolder) convertView.getTag();
        }

        // 修复索引计算 - 添加边界检查
        int firstIndex = position * 2;
        int secondIndex = position * 2 + 1;

        // 设置第一个书籍项
        if (firstIndex < list.size()) {
            HashMap<String, String> firstBook = list.get(firstIndex);
            holder.bookItem1.setName(firstBook.get("name"));
            holder.bookItem1.setAuthor(firstBook.get("author"));
            holder.bookItem1.setInfo(firstBook.get("info"));

            String piclink = list.get(position * 2).get("piclink");
            if (piclink != null && !piclink.isEmpty()) {
                piclink = GlobalConfig.PicLinkCheck(piclink);
                // 只有URL不为空时才加载图片
                holder.bookItem1.setPic(piclink, GlobalConfig.bitmapnull);
            } else {
                // 使用默认图片
                holder.bookItem1.setPic("", getBitmapFromRes(activity, R.drawable.nonepic));
            }

            holder.bookItem1.setVisibility(View.VISIBLE);
            holder.bookItem1.setMyItemClickedListener(new MyOnEvenClick(firstIndex));
        } else {
            holder.bookItem1.setVisibility(View.INVISIBLE);
        }

        // 设置第二个书籍项
        if (secondIndex < list.size()) {
            HashMap<String, String> secondBook = list.get(secondIndex);
            holder.bookItem2.setName(secondBook.get("name"));
            holder.bookItem2.setAuthor(secondBook.get("author"));
            holder.bookItem2.setInfo(secondBook.get("info"));

            String piclink2 = secondBook.get("piclink");
            if (piclink2 != null && !piclink2.isEmpty()) {
                piclink2 = GlobalConfig.PicLinkCheck(piclink2);
                holder.bookItem2.setPic(piclink2, getBitmapFromRes(activity, R.drawable.nonepic));
            } else {
                // 如果没有封面，设置默认图片
                holder.bookItem2.setPic("", getBitmapFromRes(activity, R.drawable.nonepic));
            }

            holder.bookItem2.setVisibility(View.VISIBLE);
            holder.bookItem2.setMyItemClickedListener(new MyOnEvenClick(secondIndex));
        } else {
            holder.bookItem2.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    private Bitmap getBitmapFromRes(Activity activity,int resId) {
        Resources res = activity.getResources();
        return BitmapFactory.decodeResource(res, resId);
    }
    private class ViewHolder {
        BookItem bookItem1;
        BookItem bookItem2;
    }

    private class MyOnEvenClick implements BookItem.MyItemClicked {
        int pos = 0;

        public MyOnEvenClick(int position) {
            this.pos = position;
        }

        @Override
        public void myItemClicked() {
            Log.d("clickposition",pos+"");
            Intent intent=new Intent(activity, BookInfoDetailActivity.class);
            intent.putExtra("name",list.get(pos).get("name"));
            intent.putExtra("author",list.get(pos).get("author"));
            intent.putExtra("info",list.get(pos).get("info"));
            intent.putExtra("picname",list.get(pos).get("picname"));
            intent.putExtra("link",list.get(pos).get("link"));
            intent.putExtra("piclink",list.get(pos).get("piclink"));
            activity.startActivity(intent);

        }
    }


}
