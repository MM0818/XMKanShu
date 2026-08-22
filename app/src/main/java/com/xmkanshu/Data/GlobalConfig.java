package com.xmkanshu.Data;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import com.xmkanshu.Model.Chapter;
import com.xmkanshu.greendao.model.Bookinfodb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;


public class GlobalConfig {
    // bookmap 已迁移至 BookInfoCache 内部的 LruCache，避免无界缓存导致 OOM
    public static ConcurrentHashMap<Integer,String> contentMap=new ConcurrentHashMap();//页对应的内容
    public static int measuredWidth=0;//控件列宽度
    public static int measuredHeigtt=0;//控件高度
    public static int screenWidth=0; // 屏幕宽
    public static int screenHeight=0; // 屏幕高
    public static int mPageLineNum = 0;// 每一页显示的行数
    public static int chapternow=0;//当前章节数
    public static Bitmap mutableBitmap;
    public static int Page=0;//单章当前所在页
    public static int PageTotal=1;//单章总页数
    public static int mFontHeight = 0;// 绘制字体高度
    public static String BookUrl="";//书籍链接
    public static int chapternum=0; //书籍总章节
    public static Bitmap bitmapnull=null;
    //    public static int sysLight=0;//系统亮度
    // 原错误定义（存储 ConcurrentHashMap）：
    // public static ArrayList<ConcurrentHashMap<String, String>> list = new ArrayList<>();

    // 修正后（存储 Chapter 对象）：章节对象，用数组来存，章节数据结构有章节标题以及章节url
    public static ArrayList<Chapter> list = new ArrayList<>(); // 泛型改为 Chapter
//    public static Map<String,BookChapter> bookchapter=new HashMap<String,BookChapter>();
    public static List<Bookinfodb> booklist;

    /**
     * 获取屏幕的亮度
     */
    public static int getScreenBrightness(Context context) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = context.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }
    /**
     * 设置当前Activity显示时的亮度
     * 屏幕亮度最大数值一般为255，各款手机有所不同
     * screenBrightness 的取值范围在[0,1]之间
     */
    public static void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        activity.getWindow().setAttributes(lp);
        ReadConfig.appLight=brightness;
    }
    /**
     * 保存亮度设置状态，退出app也能保持设置状态
     * 修改系统亮度
     */
    public static void saveBrightness(Context context, int brightness) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
    }

    //SharedPreferences持久化存储阅读进度保存
    /*
        - 持久化存储，应用退出后不丢失
        - 每本书独立的 SharedPreferences 文件
        - 下次打开自动恢复阅读进度
        存储结构：
            比如某本书的bookUrl："https://www.uuubqg.cc/book/12345.html"，万相之王的是：https://www.uuubqg.cc/137_137159/
            存储文件名：137_137159（去掉斜杠了）
            <Page, 10>   
            <PageTotal, 10>
            <chapternow, 5>
    */
    public static void SaveReadSetting(Context context)
    {
        // 获取"记事本"，文件名是书的 URL 去掉斜杠（保证每本书一个文件），生命周期：换书就换文件
        // 比如 BookUrl="/book/123/" → 文件名变成 "book123"
        //两个参数，第一个是文件名：每本书不同，第二个是模式：只有本APP能读
        SharedPreferences sp=context.getSharedPreferences(BookUrl.replace("/",""),MODE_PRIVATE);
        
        // 1. Editor 是定义在 SharedPreferences 内部的接口
        //外部类.内部类：职责分离，比如外部类负责随时读，内部类负责批量写
        //打开编辑模式（拿出笔准备写）
        SharedPreferences.Editor edit = sp.edit();

        // 写三行数据（键 → 值）
        edit.putInt("Page",Page);  //当前所在页= 第几页
        edit.putInt("PageTotal",PageTotal);  //当前章节总页数
        edit.putInt("chapternow",chapternow);  //当前章节数
        
        //同步立即写（有返回值 true/false）
        edit.commit();
    }

    //SharedPreferences持久化存储阅读进度恢复
    /*
        - 从每本书的 SharedPreferences 文件中读取保存的阅读进度
        - 如果文件不存在或数据损坏，提供默认值
    */
    public static void GetReadSetting(Context context)
    {
        // 打开同一本"记事本"（文件名必须和保存时一样）
        SharedPreferences sp=context.getSharedPreferences(BookUrl.replace("/",""),MODE_PRIVATE);
        
        // 读数据：getInt("键", 默认值)
        // 如果文件不存在或键找不到，就用后面的默认值
        Page=sp.getInt("Page",0);
        PageTotal=sp.getInt("PageTotal",1);
        chapternow=sp.getInt("chapternow",0);
    }

    // 在 GlobalConfig 类中修改 PicLinkCheck 方法
    public static String PicLinkCheck(String piclink) {
        // 添加空值检查
        if (piclink == null || piclink.isEmpty()) {
            return "";
        }

        // 原有的处理逻辑，但需要添加边界检查
        try {
            if (piclink.startsWith("//")) {
                piclink = "https:" + piclink;
            } else if (piclink.startsWith("/")) {
                piclink = "https://www.biqugeu.net" + piclink;
            }

            // 确保后续操作不会出现空字符串
            if (piclink.isEmpty()) {
                return "";
            }

            // 这里可能是导致错误的地方，检查 substring 操作
            // 如果 piclink 为空或长度不足，就会报错
            // 比如：if (piclink.substring(0, 2).equals("xx"))
            // 需要确保字符串长度足够

            return piclink;
        } catch (Exception e) {
            Log.e("PicLinkCheck", "处理图片链接出错: " + e.getMessage());
            return "";
        }
    }
}
