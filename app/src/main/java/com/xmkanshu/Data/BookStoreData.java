package com.xmkanshu.Data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 书城数据结构
 */
public class BookStoreData {
    private ArrayList<BookInfo> coverRecommendations;    // 封面推荐
    private ArrayList<BookInfo> strongRecommendations;   // 强力推荐
    private HashMap<String, ArrayList<BookInfo>> categoryBooks; // 分类推荐
    private ArrayList<BookInfo> recentUpdates;           // 最近更新
    private ArrayList<BookInfo> newBooks;                // 最新入库

    public BookStoreData() {}

    public ArrayList<BookInfo> getCoverRecommendations() { return coverRecommendations; }
    public void setCoverRecommendations(ArrayList<BookInfo> coverRecommendations) { this.coverRecommendations = coverRecommendations; }

    public ArrayList<BookInfo> getStrongRecommendations() { return strongRecommendations; }
    public void setStrongRecommendations(ArrayList<BookInfo> strongRecommendations) { this.strongRecommendations = strongRecommendations; }

    public HashMap<String, ArrayList<BookInfo>> getCategoryBooks() { return categoryBooks; }
    public void setCategoryBooks(HashMap<String, ArrayList<BookInfo>> categoryBooks) { this.categoryBooks = categoryBooks; }

    public ArrayList<BookInfo> getRecentUpdates() { return recentUpdates; }
    public void setRecentUpdates(ArrayList<BookInfo> recentUpdates) { this.recentUpdates = recentUpdates; }

    public ArrayList<BookInfo> getNewBooks() { return newBooks; }
    public void setNewBooks(ArrayList<BookInfo> newBooks) { this.newBooks = newBooks; }
}