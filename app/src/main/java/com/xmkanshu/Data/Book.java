package com.xmkanshu.Data;


public class Book {
    String name;
    BookInfo bookInfo;

    public Book(String name, BookInfo bookInfo) {
        this.name = name;
        this.bookInfo = bookInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BookInfo getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(BookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }
}
