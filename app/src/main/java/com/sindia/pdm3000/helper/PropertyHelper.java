package com.sindia.pdm3000.helper;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

// 运行时的配置文件助手
public class PropertyHelper {
    private static final String kRunPropPath = "run_prop";

    Context mContext;
    Properties mProperties = new Properties();

    public PropertyHelper(Context context) {
        mContext = context;
    }

    // 加载配置文件
    public boolean loadFromFile(String fileName) {
        File filesDir = mContext.getFilesDir();
        String filesPath = filesDir.getAbsolutePath();
        String iniPath = filesPath + File.separator + kRunPropPath + File.separator + fileName;
        try {
            InputStream inStream = new FileInputStream(iniPath);// mContext.openFileInput(iniPath);
            mProperties.load(inStream);
            return true;
        } catch (Exception e) { // 无法打开文件
            return false;
            //throw new RuntimeException(e);
        }
    }

    public boolean saveToFile(String fileName) {
        File filesDir = mContext.getFilesDir();
        String filesPath = filesDir.getAbsolutePath();
        String iniPath = filesPath + File.separator + kRunPropPath + File.separator + fileName;
        try {
            //props.load(context.openFileInput(iniPath));
            // 这样就可以了
            FileOutputStream out = new FileOutputStream(iniPath);//context.openFileOutput(iniPath, Context.MODE_PRIVATE);
            mProperties.store(out, null);
            return true;
        } catch (Exception e) {
            //throw new RuntimeException(e);
        }
        return false;
    }

    public String getString(String key) {//}, String def) {
        String s = mProperties.getProperty(key);
        if (s != null) {
            return s;
        }
        return "";
    }

    public boolean setString(String key, String value) {
        mProperties.setProperty(key, value);
        return true;
    }
}
