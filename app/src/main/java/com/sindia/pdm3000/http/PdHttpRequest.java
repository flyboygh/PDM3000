package com.sindia.pdm3000.http;

import com.sindia.pdm3000.model.ParamSetData;

import org.json.JSONObject;

// http业务请求类
public class PdHttpRequest extends OkHttpHelper {
    // 常量定义
    private static final String kServiceURL = "http://192.168.1.110:8080";
    //private static final String kServiceURL = "http://192.168.1.100:8080";
    private static final long kHeartBeatIntervalMillis = 5000;//10000;// 30000; // 发送心跳的间隔时间

    // 普通成员
    private static long mLastRespondMillis = 0; // 最后一次响应或超时的时间，-1表示正在发送中
    private static boolean mIsHttpConnected = false; // 当前连接状态是否正常

    // 请求响应基类
    public static class ResponseBase {
        public int respCode; // 响应码
        public boolean isSucc; // 是否成功
    }

    //public static class ParamSetResp extends ResponseBase {
    //    ParamSetData paramSet;
    //}

    // 请求响应回调（测试接口）
    public interface Callback {
        void onResponse(ResponseBase resp);
        //void onParamSetResp(ParamSetResp resp);
    }

    // 当前HTTP是否连通
    public static boolean isHttpConnected() {
        return mIsHttpConnected;
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
    public static boolean doPostHeartBeat(final Callback callback) {
        String body = "";//null;
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
                if (code == 200) {
                    resp.isSucc = true;
                    mIsHttpConnected = true;
                } else {
                    resp.isSucc = false;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });
        /*if (!b) {
            mIsHttpConnected = false;
            ResponseBase resp = new ResponseBase();
            resp.respCode = -1;
            resp.isSucc = false;
            callback.onResponse(resp);
        }*/
        return b;
    }

    // 发送参数下发
    public static boolean doPostParamSet(final ParamSetData paramSet, final Callback callback) {
        String body = "";//null;
        try {
            // 组json包
            JSONObject rootObject = new JSONObject();
            rootObject.put("type", 10);
            rootObject.put("name", "123");
            JSONObject obj = new JSONObject();//"{name1:value1,name2:value2}");
            //byte[] bytes = paramSet.lineName.getBytes();
            String string1 = paramSet.lineName.toString();
            String string2 = paramSet.jointName.toString();
            //byte[] bytes1 = string1.getBytes("gb18030");
            //byte[] bytes2 = string2.getBytes("gb18030");
            //String s1 = new String(bytes1,"gb18030");
            //String s2 = new String(bytes2,"gb18030");
            //byte[] b1 = s1.getBytes();
            //byte[] b2 = s2.getBytes();
            obj.put("line_name", string1);// s1);
            obj.put("joint_name", string2);// s2);
            obj.put("work_interval", 60);
            obj.put("center_freq", 4.0);
            obj.put("band_width", 300);
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
                if (code == 200) {
                    resp.isSucc = true;
                    mIsHttpConnected = true;
                } else {
                    resp.isSucc = false;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });
        /*if (!b) {
            mIsHttpConnected = false;
            ResponseBase resp = new ResponseBase();
            resp.respCode = -1;
            resp.isSucc = false;
            callback.onResponse(resp);
        }*/
        return b;
    }

    // 发送参数下发
    public static boolean doGetParamSet(final Callback callback) {
        String body = "";//null;
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
                if (code == 200) {
                    resp.isSucc = true;
                    mIsHttpConnected = true;
                } else {
                    resp.isSucc = false;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });
        /*if (!b) {
            mIsHttpConnected = false;
            ResponseBase resp = new ResponseBase();
            resp.respCode = -1;
            resp.isSucc = false;
            callback.onResponse(resp);
        }*/
        return b;
    }
}
