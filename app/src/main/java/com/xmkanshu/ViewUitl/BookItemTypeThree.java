package com.xmkanshu.ViewUitl;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xmkanshu.R;


public class BookItemTypeThree extends RelativeLayout {
    private BookItemTypeThree.MyItemClicked myItemClicked;
    private BookItemTypeThree.MyItemLongClicked myItemLongClicked;
    private RelativeLayout relativeLayout1;// 容器包含项目
    private TextView tv_name;// 书名
    public ImageView img_pic;//封面

    private String name;
//    private Bitmap pic;
    private Bitmap pic;
    //    private Boolean isSelected = false;// 是否被选中
    public BookItemTypeThree(Context context) {
        super(context);
    }

    public BookItemTypeThree(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.bookitem3,this);
        relativeLayout1 = (RelativeLayout) findViewById(R.id.relativeLayout_bookshelf);
        tv_name=(TextView)findViewById(R.id.tv_bookshelf_bookname);
        img_pic=(ImageView)findViewById(R.id.img_bookshelf_book);
        relativeLayout1.setOnClickListener(new BookItemTypeThree.MyOnClick());
        relativeLayout1.setOnLongClickListener(new BookItemTypeThree.MyOnLongClick());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        tv_name.setText(name);
    }


//    public Bitmap getPic() {
//        return pic;
//    }
//
//    public void setPic(Bitmap pic) {
//        this.pic = pic;
//        img_pic.setImageBitmap(pic);
//    }


    public Bitmap getPic() {
        return pic;
    }

    public void setPic(Bitmap pic) {
        this.pic = pic;
        img_pic.setImageBitmap(pic);
    }

    private class MyOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
		/*	if (!isSelected) {
				relativeLayout1.setBackgroundResource(R.drawable.selectedborder);
				myItemClicked.myItemClicked();
			} else {
				relativeLayout1.setBackgroundResource(R.drawable.border);
				myItemClicked.myItemClicked();
			}*/
            myItemClicked.myItemClicked();
        }
    }
    public interface MyItemClicked {
        public void myItemClicked();
    }

    public void setMyItemClickedListener(BookItemTypeThree.MyItemClicked myItemClicked) {
        this.myItemClicked = myItemClicked;
    }

    private class MyOnLongClick implements OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            if (myItemLongClicked != null) {
                myItemLongClicked.myItemLongClicked();
            }
            return true;
        }
    }

    public interface MyItemLongClicked {
        public void myItemLongClicked();
    }

    public void setMyItemLongClickedListener(BookItemTypeThree.MyItemLongClicked listener) {
        this.myItemLongClicked = listener;
    }
//    private Bitmap getBitmapFromRes(int resId) {
//        Resources res = MyApplication.myApplication.getResources();
//        return BitmapFactory.decodeResource(res, resId);
//    }
}
