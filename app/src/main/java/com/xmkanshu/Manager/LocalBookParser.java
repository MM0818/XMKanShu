package com.xmkanshu.Manager;

import com.xmkanshu.Model.Chapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地书籍解析类
 * 负责解析txt文件，自动识别章节
 */
public class LocalBookParser {
    // 常见章节格式的正则表达式
    private static final String[] CHAPTER_PATTERNS = {
            "第[0-9一二三四五六七八九十百千零壹贰叁肆伍陆柒捌玖拾佰仟]+[章节回卷集部篇][\\s\\S]*",
            "Chapter\\s*[0-9]+[\\s\\S]*",
            "CHAPTER\\s*[0-9]+[\\s\\S]*",
            "\\d+[\\.、][\\s\\S]*"
    };

    /**
     * 解析本地txt文件，提取章节列表
     * @param filePath 文件路径
     * @return 章节列表
     */
    public static List<Chapter> parseChapters(String filePath) {
        List<Chapter> chapters = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return chapters;
        }

        try {
            // 尝试不同编码读取文件
            BufferedReader reader = null;
            String encoding = detectEncoding(file);

            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

                List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                // 查找章节标题
                List<Integer> chapterStartLines = new ArrayList<>();
                List<String> chapterTitles = new ArrayList<>();

                for (int i = 0; i < lines.size(); i++) {
                    String trimmedLine = lines.get(i).trim();
                    if (isChapterTitle(trimmedLine)) {
                        chapterStartLines.add(i);
                        chapterTitles.add(trimmedLine);
                    }
                }

                // 如果没有找到章节，把整个文件作为一章
                if (chapterStartLines.isEmpty()) {
                    chapters.add(new Chapter("全文", "local_chapter_0"));
                    return chapters;
                }

                // 创建章节对象
                for (int i = 0; i < chapterStartLines.size(); i++) {
                    String title = chapterTitles.get(i);
                    String chapterId = "local_chapter_" + i;
                    chapters.add(new Chapter(title, chapterId));
                }

            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chapters;
    }

    /**
     * 获取指定章节的内容
     * @param filePath 文件路径
     * @param chapterIndex 章节索引
     * @return 章节内容
     */
    public static String getChapterContent(String filePath, int chapterIndex) {
        File file = new File(filePath);
        if (!file.exists()) {
            return "";
        }

        try {
            String encoding = detectEncoding(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();

            // 查找所有章节起始行
            List<Integer> chapterStartLines = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                String trimmedLine = lines.get(i).trim();
                if (isChapterTitle(trimmedLine)) {
                    chapterStartLines.add(i);
                }
            }

            // 如果没有找到章节，返回全文
            if (chapterStartLines.isEmpty()) {
                StringBuilder content = new StringBuilder();
                for (String l : lines) {
                    content.append(l).append("\n");
                }
                return content.toString();
            }

            // 获取指定章节的内容
            if (chapterIndex < 0 || chapterIndex >= chapterStartLines.size()) {
                return "";
            }

            int startLine = chapterStartLines.get(chapterIndex);
            int endLine = (chapterIndex + 1 < chapterStartLines.size()) ?
                    chapterStartLines.get(chapterIndex + 1) : lines.size();

            StringBuilder content = new StringBuilder();
            for (int i = startLine; i < endLine; i++) {
                content.append(lines.get(i)).append("\n");
            }

            return content.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 判断一行是否是章节标题
     */
    private static boolean isChapterTitle(String line) {
        if (line == null || line.isEmpty() || line.length() > 100) {
            return false;
        }

        for (String pattern : CHAPTER_PATTERNS) {
            if (line.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测文件编码
     */
    private static String detectEncoding(File file) {
        try {
            // 简单的编码检测：先尝试UTF-8，失败则使用GBK
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            char[] buffer = new char[4096];
            int bytesRead = reader.read(buffer);
            reader.close();

            if (bytesRead > 0) {
                // 检查是否有乱码特征
                String sample = new String(buffer, 0, bytesRead);
                if (!sample.contains("�")) {
                    return "UTF-8";
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return "GBK";
    }

    /**
     * 从文件路径提取书名
     * @param filePath 文件路径
     * @return 书名（不含扩展名）
     */
    public static String extractBookName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "未知书籍";
        }

        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
}
