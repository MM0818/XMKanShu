package com.xmkanshu.ViewUitl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.View;

/* 代码思路：
    系统电量变化
        ↓
    发送广播 ACTION_BATTERY_CHANGED
        ↓
    onReceive() 解析 level/scale/status
        ↓
    setPower() 更新 mPower + mIsCharging
        ↓
    invalidate() 请求重绘
        ↓
    下一帧 onDraw() 用新电量画芯
        ↓
    屏幕刷新，电池显示新电量
*/

public class BatteryView extends View {  //自定义View必须继承自View或者其子类
    //电池尺寸参数
    private int mMargin = 5;    //电池内芯与边框的距离
    private int mBoder = 4;     //电池外框的宽带
    private int mWidth = 70;    //总长
    private int mHeight = 40;   //总高
    private int mHeadWidth = 6;
    private int mHeadHeight = 10;
    private RectF mMainRect;
    private RectF mHeadRect;
    private float mRadius = 4f;   //圆角
    
    //电池状态
    private float mPower;  //电量百分比
    private boolean mIsCharging;    //是否在充电

    //构造方法：必须要实现，用于创建View对象，带默认样式的（很少用）
    public BatteryView(Context context, AttributeSet attrs, int defStyleAttr) { //三个参数：上下文、属性集、默认样式属性
        super(context, attrs, defStyleAttr);
        initView();
    }

    //XML布局里用（最常见）
    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    //代码里new BatteryView(this)
    public BatteryView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        //初始化电池头矩形，（边框）
        mHeadRect = new RectF(0, (mHeight - mHeadHeight)/2, mHeadWidth, (mHeight + mHeadHeight)/2);

        //初始化电池芯矩形
        float left = mHeadRect.width();  //从电池头右边开始接电池身，表示电池身左边从哪开始
        float top = mBoder;  //电池身上边从哪开始：留边框宽度
        float right = mWidth-mBoder;  //电池身右边到哪结束
        float bottom = mHeight-mBoder;  //电池身下边到哪结束

        mMainRect = new RectF(left, top, right, bottom);  //一个圆角矩形区域
    }

    /* 对于为什么这个自定义View不用重写onLayout方法
        XML 里的 layout_width/height
            ↓
        父 ViewGroup 的 onMeasure 算子 View 大小
            ↓
        父 ViewGroup 的 onLayout 告诉子 View：你左上角在 (x,y)，宽 w 高 h
            ↓
        子 View（你的 BatteryView）拿到位置，保存到 mLeft/mTop/mRight/mBottom
            ↓
        子 View 的 onDraw 用这些位置画画
    */

    //绘制视图：必须要实现，用于绘制视图的内容（核心！）
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint1 = new Paint();

        //画电池头：先定好画笔属性：样式为实心，颜色为白色
        paint1.setStyle(Paint.Style.FILL);  //实心
        paint1.setColor(Color.WHITE);
        canvas.drawRect(mHeadRect, paint1);  //画板类：（边框+画笔）

        //画电池身
        paint1.setStyle(Paint.Style.STROKE);    //设置空心矩形
        paint1.setStrokeWidth(mBoder);          //设置边框宽度
        paint1.setColor(Color.WHITE);
        canvas.drawRoundRect(mMainRect, mRadius, mRadius, paint1);  //椭圆角，传水平和垂直方向的弯曲程度

        //画电池芯，直接涂色
        Paint paint = new Paint();
        if (mIsCharging) {  //充电，电池芯为绿色
            paint.setColor(Color.GREEN);
        } else {
            if (mPower < 0.1) {  //电量小于10%，电池芯为红色
                paint.setColor(Color.RED);
            } else {  //电量大于等于10%，电池芯为白色
                paint.setColor(Color.WHITE);
            }
        }

        //根据电量计算电池芯宽度：电量百分比 * (电池芯宽度 - 电池芯与边框的距离*2)
        int width   = (int) (mPower * (mMainRect.width() - mMargin*2));

        int left    = (int) (mMainRect.right - mMargin - width);
        int right   = (int) (mMainRect.right - mMargin);
        int top     = (int) (mMainRect.top + mMargin);
        int bottom  = (int) (mMainRect.bottom - mMargin);
        
        Rect rect = new Rect(left,top,right, bottom);

        canvas.drawRect(rect, paint);  //画板类，依旧传个轮廓+画笔
    }

    //测量视图尺寸：必须要实现，用于测量视图的宽度和高度。告诉父View我多大！
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置最终测量结果：70x40像素，固定大小，不随父布局变化
        setMeasuredDimension(mWidth, mHeight);
    }

    //电量更新机制
    private void setPower(float power) {
        mPower = power;  //广播接收器算的数值
        invalidate();  //触发重绘：刷新视图，系统会在下一帧回调onDraw（）
    }

    //匿名内部类：监听系统电量变化广播：注册广播接收器，监听电池电量变化
    //1、新建new一个广播接收器（匿名内部类）
    private BroadcastReceiver mPowerConnectionReceiver = new BroadcastReceiver() {
        //2、收到广播时，系统回调这个方法
        @Override
        public void onReceive(Context context, Intent intent) {
            //解析广播数据
            //1、是否在充电
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);  //第一个参数：电池状态，系统写好的常量，直接用
            //判断：状态是“充电中”或“已充满”->正在充电
            mIsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            //2、当前电量百分比
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);  //当前电量
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);  //最大电量

            setPower(((float) level)/scale);  //将当前电量转换为百分比，赋值给power
        }
    };

    //生命周期管理：当视图被添加到窗口时调用，注册广播接收器
    @Override
    protected void onAttachedToWindow() {
        // View 显示到屏幕，开始监听电量
        getContext().registerReceiver(mPowerConnectionReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));  // 只收电量变化广播
        super.onAttachedToWindow();
    }

    //生命周期管理：当视图从窗口移除时调用，取消注册广播接收器
    @Override
    protected void onDetachedFromWindow() {
        // View 从屏幕消失，取消监听，防止内存泄漏
        getContext().unregisterReceiver(mPowerConnectionReceiver);
        super.onDetachedFromWindow();
    }
}
