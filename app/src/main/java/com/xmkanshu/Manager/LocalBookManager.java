package com.xmkanshu.Manager;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 本地书籍管理类
 * 负责将用户选择的txt文件复制到应用私有目录
 */
public class LocalBookManager {
    private static final String LOCAL_BOOK_DIR = "local_books";
    private Context context;

    public LocalBookManager(Context context) {
        this.context = context;
    }

    /**
     * 获取本地书籍存储目录
     */
    public File getLocalBookDir() {
        File dir = new File(context.getFilesDir(), LOCAL_BOOK_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 从Uri复制文件到本地目录
     * @param uri 用户选择的文件Uri
     * @param fileName 保存的文件名
     * @return 复制后的本地文件路径，失败返回null
     */
    public String copyFileToLocal(Uri uri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File localFile = new File(getLocalBookDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(localFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return localFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取本地书籍文件
     * @param fileName 文件名
     * @return 文件对象
     */
    public File getLocalBookFile(String fileName) {
        return new File(getLocalBookDir(), fileName);
    }

    /**
     * 删除本地书籍文件
     * @param fileName 文件名
     * @return 是否删除成功
     */
    public boolean deleteLocalBook(String fileName) {
        File file = getLocalBookFile(fileName);
        return file.exists() && file.delete();
    }

    /**
     * 生成唯一的文件名，避免重名
     * @param originalName 原始文件名
     * @return 唯一文件名
     */
    public String generateUniqueFileName(String originalName) {
        File dir = getLocalBookDir();
        File file = new File(dir, originalName);
        if (!file.exists()) {
            return originalName;
        }

        // 如果文件已存在，添加数字后缀
        String name = originalName;
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            name = originalName.substring(0, dotIndex);
            extension = originalName.substring(dotIndex);
        }

        int counter = 1;
        while (file.exists()) {
            String newName = name + "_" + counter + extension;
            file = new File(dir, newName);
            counter++;
        }
        return file.getName();
    }
}
