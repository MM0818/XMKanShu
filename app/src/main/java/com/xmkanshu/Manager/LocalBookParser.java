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
    // 章节解析结果缓存，避免重复读取整个文件
    private static List<String> cachedLines;
    private static List<Integer> cachedChapterStartLines;
    private static String cachedFilePath;

    // 常见章节格式的正则表达式（预编译，提升性能）
    // 使用 find() 匹配行内子串，而非 matches() 全行匹配；兼容不可见字符和全角数字
    private static final Pattern[] CHAPTER_PATTERN_COMPILED = {
            Pattern.compile("第[0-9一二三四五六七八九十百千零壹贰叁肆伍陆柒捌玖拾佰仟０-９]+\\s*[章节回卷集部篇]"),
            Pattern.compile("[Cc][Hh][Aa][Pp][Tt][Ee][Rr]\\s*[0-9]+"),
            Pattern.compile("第\\s*[0-9０-９]+\\s*[章节回卷集部篇]"),
            Pattern.compile("\\d+[\\.、]\\s*\\S")
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
                    int pathHash = Math.abs(filePath.hashCode());
                    chapters.add(new Chapter("全文", "local_" + pathHash + "_chapter_0"));
                    // 缓存：整本书作为一章
                    cachedFilePath = filePath;
                    cachedLines = lines;
                    cachedChapterStartLines = new ArrayList<>();
                    cachedChapterStartLines.add(0);
                    return chapters;
                }

                // 缓存解析结果，供 getChapterContent 复用
                cachedFilePath = filePath;
                cachedLines = lines;
                cachedChapterStartLines = chapterStartLines;

                // 创建章节对象，key 包含文件路径哈希以区分不同书籍
                int pathHash = Math.abs(filePath.hashCode());
                for (int i = 0; i < chapterStartLines.size(); i++) {
                    String title = chapterTitles.get(i);
                    String chapterId = "local_" + pathHash + "_chapter_" + i;
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
        // 优先使用缓存的解析结果
        if (cachedFilePath != null && cachedFilePath.equals(filePath)
                && cachedLines != null && cachedChapterStartLines != null) {
            return getChapterContentFromCache(chapterIndex);
        }

        // 缓存未命中，回退到全量读取（兼容直接调用 getChapterContent 而未先 parseChapters 的情况）
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
     * 从缓存中获取章节内容（避免重复读取文件）
     */
    private static String getChapterContentFromCache(int chapterIndex) {
        if (chapterIndex < 0 || chapterIndex >= cachedChapterStartLines.size()) {
            return "";
        }

        int startLine = cachedChapterStartLines.get(chapterIndex);
        int endLine = (chapterIndex + 1 < cachedChapterStartLines.size()) ?
                cachedChapterStartLines.get(chapterIndex + 1) : cachedLines.size();

        StringBuilder content = new StringBuilder();
        for (int i = startLine; i < endLine; i++) {
            content.append(cachedLines.get(i)).append("\n");
        }
        return content.toString();
    }

    /**
     * 清除缓存（切换书籍时调用）
     */
    public static void clearCache() {
        cachedLines = null;
        cachedChapterStartLines = null;
        cachedFilePath = null;
    }

    /**
     * 判断一行是否是章节标题
     */
    private static boolean isChapterTitle(String line) {
        if (line == null || line.isEmpty() || line.length() > 200) {
            return false;
        }

        // 清理不可见字符（BOM、NBSP、零宽字符等），统一空格
        String cleaned = line.replaceAll("[\\uFEFF\\u00A0\\u200B\\u200C\\u200D\\u2060\\u3000]", " ").trim();
        if (cleaned.isEmpty()) return false;

        for (Pattern pattern : CHAPTER_PATTERN_COMPILED) {
            if (pattern.matcher(cleaned).find()) {
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
