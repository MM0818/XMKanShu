package com.xmkanshu.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xmkanshu.Cache.ImageCacheManager;
import com.xmkanshu.R;

import java.util.ArrayList;
import java.util.HashMap;
//封面推荐适配器---------------------------------------------------------------
public class BookGridRecyclerAdapter extends RecyclerView.Adapter<BookGridRecyclerAdapter.ViewHolder> {
    private ArrayList<HashMap<String, String>> list;
    private Context context;
    private OnItemClickListener onItemClickListener;

    // 缓存默认图片的Bitmap
    private Bitmap defaultBitmap = null;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public BookGridRecyclerAdapter(ArrayList<HashMap<String, String>> list, Context context) {
        this.list = list;
        this.context = context;
        loadDefaultBitmap();
    }

    private void loadDefaultBitmap() {
        if (defaultBitmap == null) {
            try {
                defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.nonepic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getDefaultBitmap() {
        if (defaultBitmap == null) {
            loadDefaultBitmap();
        }
        return defaultBitmap;
    }

    //视图创建 ： onCreateViewHolder 创建新的 ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用一行两本书的布局
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_grid, parent, false);
        return new ViewHolder(view);
    }

    //数据绑定 ： onBindViewHolder 将数据绑定到已有 ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> book = list.get(position);

        // 设置书籍信息
        holder.tvTitle.setText(book.get("name"));
        holder.tvAuthor.setText(book.get("author"));

        // 替换原来的ImageCacheManager.loadImage逻辑
        String piclink = book.get("piclink");
        if (piclink != null && !piclink.isEmpty()) {
            Glide.with(context) //创建 Glide 实例，绑定上下文
                    .load(piclink)  //加载网络图片（支持url、资源ID等等）
                    .placeholder(R.drawable.nonepic)  //加载占位图（加载完成前显示）
                    .error(R.drawable.nonepic)  //加载失败图（加载失败显示）
                    .into(holder.ivCover);      //将图片加载在ImageView中    
        } else {
            holder.ivCover.setImageResource(R.drawable.nonepic);
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    //数量计算 ： getItemCount 返回数据集大小
    @Override
    public int getItemCount() {
        return list.size();
    }

    //- 组件绑定 ：通过 findViewById 绑定 UI 组件
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvAuthor;

        ViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.img_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            tvAuthor = itemView.findViewById(R.id.tv_book_author);
        }
    }

    public void clear() {
        if (defaultBitmap != null && !defaultBitmap.isRecycled()) {
            defaultBitmap.recycle();
            defaultBitmap = null;
        }
    }
}