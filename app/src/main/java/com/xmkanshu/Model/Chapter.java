package com.xmkanshu.Model; // 包名要和你的项目结构一致

// 简单的章节数据类（存储标题和链接）
public class Chapter {
    private String title; // 章节标题（比如“第一章 标题”）
    private String url;   // 章节链接（比如“https://www.biqugeu.net/11_11686/123.html”）

    // 构造方法（必须有，用于创建对象）
    public Chapter(String title, String url) {
        this.title = title;
        this.url = url;
    }

    // Getter 方法（用于后续获取章节信息，根据需要添加）
    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}