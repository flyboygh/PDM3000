package com.sindia.pdm3000.http;

import org.json.JSONObject;

// http业务请求类
public class PdHttpRequest extends OkHttpHelper {
    // 常量定义
    //private static final String kServiceURL = "https://192.168.1.108";
    private static final String kServiceURL = "https://hq.sinajs.cn/list=sh600028";// 这个是测试用的
    private static final long kHeartBeatIntervalMillis = 10000;// 30000; // 发送心跳的间隔时间

    // 普通成员
    private static long mLastRespondMillis = 0; // 最后一次响应或超时的时间，-1表示正在发送中

    // 请求响应基类
    public static class ResponseBase {
        public int respCode; // 响应码
        public boolean isSucc; // 是否成功
    }

    // 请求响应回调（测试接口）
    public interface Callback {
        void onResponse(ResponseBase resp);
    }

    // 是否到了发送心跳包的时间
    public static boolean shouldPostHeartBeat() {
        if (mLastRespondMillis >= 0) { // 已经响应或超时了
            long curMillis = System.currentTimeMillis();
            if (curMillis > mLastRespondMillis + kHeartBeatIntervalMillis) {
                return true;
            }
        }
        return false;
    }

    // 发送心跳
    public static boolean postHeartBeat(final Callback callback) {
        String body = null;
        try {
            // 组json包
            JSONObject rootObject = new JSONObject();
            rootObject.put("type", 1);
            rootObject.put("name", "123");
            JSONObject obj = new JSONObject("{name1:value1,name2:value2}");
            rootObject.put("data", obj);

            body = rootObject.toString();
            //JsonWriter writer = new JsonWriter();

        } catch (Exception e) {

        }

        mLastRespondMillis = -1;
        boolean b = postHttpRequest(kServiceURL, body, new OkHttpCallback() {
            @Override
            public void onHttpRespond(int code, String body) {
                ResponseBase resp = new ResponseBase();
                resp.respCode = code;
                resp.isSucc = ( code == 200 );
                callback.onResponse(resp);
                // 设置最后一次成员的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });
        return b;
    }
}
