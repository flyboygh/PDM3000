package com.sindia.pdm3000.util;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// 本地文件访问工具类
public class PDFileUtil {
    static final String kSyncDataPath = "data";

    // 取得线路名称数组
    public static List<String> getLineNames(Context context) {
        String path = kSyncDataPath;
        return getFileNames(context, path);
    }

    // 取得线路接头数组
    public static List<String> getLinePosNames(Context context, String lineName) {
        String path = kSyncDataPath + File.separator + lineName;
        return getFileNames(context, path);
    }

    private static List<String> getFileNames(Context context, String path) {
        File filesDir = context.getFilesDir();
        String filesPath = filesDir.getAbsolutePath();
        String dataPath = filesPath + File.separator + path;

        List<String> list = new ArrayList<>();
        File dataDir = new File(dataPath);
        File []lineNameFiles = dataDir.listFiles();
        for (int i = 0; i < lineNameFiles.length; i++) {
            File lineNameFile = lineNameFiles[i];
            String s = lineNameFile.getName();
            list.add(s);
        }
        return list;
    }
}
