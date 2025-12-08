package com.xmkanshu.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xmkanshu.Data.GlobalConfig;
import com.xmkanshu.R;
import com.xmkanshu.UI.ReadingActivity;
import com.xmkanshu.ViewUitl.BookItemTypeThree;
import com.xmkanshu.greendao.model.Bookinfodb;

import java.util.List;

//已经改成了用Glide库显示图片
public class BookShelfAdapter extends BaseAdapter {
    private List<Bookinfodb> list;
    private int sumCount;
    Activity activity;
    // 定义Glide的RequestOptions（复用，优化性能）
    private RequestOptions glideOptions;

    public BookShelfAdapter(List<Bookinfodb> list, Activity activity) {
        super();
        this.list = list;
        this.activity = activity;
        // 初始化Glide的配置（占位图、错误图）
        glideOptions = new RequestOptions()
                .placeholder(R.drawable.nonepic) // 加载中显示的图片
                .error(R.drawable.nonepic);      // 加载失败显示的图片
    }

    @Override
    public int getCount() {
        int count = list.size();
        if (count == 0) return 0;
        if (count % 3 == 0) {
            sumCount = count / 3;
        } else {
            sumCount = count / 3 + 1; // 简化Math.floor的写法（整数除法本身会向下取整）
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_bookshelf, null);
            holder = new ViewHolder();
            holder.bookItem1 = convertView.findViewById(R.id.bookItemTypeThree);
            holder.bookItem2 = convertView.findViewById(R.id.bookItemTypeThree2);
            holder.bookItem3 = convertView.findViewById(R.id.bookItemTypeThree3);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 加载第一个书籍的图片（使用Glide）
        Bookinfodb book1 = list.get(position * 3);
        String piclink1 = GlobalConfig.PicLinkCheck(book1.getPiclink());
        holder.bookItem1.setName(book1.getName());
        Glide.with(activity)
                .load(piclink1)
                .apply(glideOptions)
                .into(holder.bookItem1.img_pic); // 假设BookItemTypeThree的img_pic是ImageView类型

        // 加载第二个书籍的图片（使用Glide）
        if (position * 3 + 1 < list.size()) {
            holder.bookItem2.setVisibility(View.VISIBLE);
            Bookinfodb book2 = list.get(position * 3 + 1);
            String piclink2 = GlobalConfig.PicLinkCheck(book2.getPiclink());
            holder.bookItem2.setName(book2.getName());
            Glide.with(activity)
                    .load(piclink2)
                    .apply(glideOptions)
                    .into(holder.bookItem2.img_pic);
        } else {
            holder.bookItem2.setVisibility(View.INVISIBLE);
        }

        // 加载第三个书籍的图片（使用Glide）
        if (position * 3 + 2 < list.size()) {
            holder.bookItem3.setVisibility(View.VISIBLE);
            Bookinfodb book3 = list.get(position * 3 + 2);
            String piclink3 = GlobalConfig.PicLinkCheck(book3.getPiclink());
            holder.bookItem3.setName(book3.getName());
            Glide.with(activity)
                    .load(piclink3)
                    .apply(glideOptions)
                    .into(holder.bookItem3.img_pic);
        } else {
            holder.bookItem3.setVisibility(View.INVISIBLE);
        }

        // 设置点击事件（注意：第二个和第三个的pos参数之前有错误，这里修正）
        holder.bookItem1.setMyItemClickedListener(new MyOnEvenClick(position * 3));
        holder.bookItem2.setMyItemClickedListener(new MyOnEvenClick(position * 3 + 1));
        holder.bookItem3.setMyItemClickedListener(new MyOnEvenClick(position * 3 + 2)); // 之前是position*3+1，这里修正为+2

        return convertView;
    }

    private class ViewHolder {
        BookItemTypeThree bookItem1;
        BookItemTypeThree bookItem2;
        BookItemTypeThree bookItem3;
    }

    private class MyOnEvenClick implements BookItemTypeThree.MyItemClicked {
        int pos = 0;

        public MyOnEvenClick(int position) {
            this.pos = position;
        }

        @Override
        public void myItemClicked() {
            Log.d("clickposition", pos + "");
            if (pos >= list.size()) return; // 防止数组越界
            Intent intent = new Intent(activity, ReadingActivity.class);
            intent.putExtra("link", list.get(pos).getLink());
            intent.putExtra("chapternum", list.get(pos).getChapternum());
            activity.startActivity(intent);
        }
    }
}