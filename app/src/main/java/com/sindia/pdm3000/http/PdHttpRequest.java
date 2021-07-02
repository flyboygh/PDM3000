package com.sindia.pdm3000.http;

import android.graphics.BitmapFactory;

import com.sindia.pdm3000.model.CatalogueData;
import com.sindia.pdm3000.model.DownloadData;
import com.sindia.pdm3000.model.FilterCatalogueData;
import com.sindia.pdm3000.model.LineData;
import com.sindia.pdm3000.model.ParamSetData;
import com.sindia.pdm3000.model.SpectrumData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

// http业务请求类
public class PdHttpRequest extends OkHttpHelper {
    // 常量定义
    private static final String kServiceURL = "http://192.168.1.110:8080";
    //private static final String kServiceURL = "http://192.168.1.100:8080";
    private static final long kHeartBeatIntervalMillis = 5000;//10000;// 30000; // 发送心跳的间隔时间
    //消息类型定义
    private static final int MSG_TYPE_HEARTBEAT = 1;   //心跳消息
    private static final int MSG_TYPE_SET_PARAM = 10;  //设置参数
    private static final int MSG_TYPE_GET_PARAM = 11;  //获取参数
    private static final int MSG_TYPE_GET_TEST_SPECTRUM = 12;   //获取测试谱图
    private static final int MSG_TYPE_GET_LINE_INFO = 21;   //获取线路和接头的下拉列表
    private static final int MSG_TYPE_FILTER_CATALOGUE = 22;   //过滤目录
    private static final int MSG_TYPE_GET_DATA = 23;   //获取数据
    //消息长度定义
    private static final int MSG_LEN_CHUNK = 4 ;//块长度
    private static final int MSG_LEN_FILE_NAME = 64; //文件名称长度
    //返回值定义
    private static final int HTTP_CODE_OK = 200; //http正确返回
    // 普通成员
    private static long mLastRespondMillis = 0; // 最后一次响应或超时的时间，-1表示正在发送中
    private static boolean mIsHttpConnected = false; // 当前连接状态是否正常

    // 请求响应基类
    public static class ResponseBase {
        public int respCode; // 响应码（200-正常）
        public int errCode; // 错误码（0-成功）
        public String errMsg = ""; // 错误信息
    }
    //获取参数响应类
    public static class ParamSetResp extends ResponseBase {
        public ParamSetData paramSet;
    }

    //获取谱图响应类
    public  static class GetSpectrumResp extends  ResponseBase {
        public ArrayList<SpectrumData> getSpectrumInfo;
    }

    //获取下拉列表响应类
    public static class GetLineInfoResp  extends  ResponseBase{
        public ArrayList<LineData>  lineList;
    }

    //过滤目录响应类
    public static class FilterCatalogueResp extends  ResponseBase{
        public ArrayList<CatalogueData> catalogueList;
    }

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
            rootObject.put("type", MSG_TYPE_HEARTBEAT);
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
            public void onHttpRespond(int code,int type,byte[]body) {
                ResponseBase resp = new ResponseBase();
                resp.respCode = code;
                if (code == HTTP_CODE_OK) {
                    resp.errCode = 0;
                    mIsHttpConnected = true;
                } else {
                    resp.errCode = -1;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });

        return b;
    }

    // 发送参数下发
    public static boolean doPostParamSet(final ParamSetData paramSet, final Callback callback) {
        String body = "";//null;
        try {
            // 组json包
            JSONObject rootObject = new JSONObject();
            rootObject.put("type", MSG_TYPE_SET_PARAM);
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
            obj.put("work_interval",paramSet.work_interval);
            obj.put("center_freq", paramSet.freq_center);
            obj.put("band_width", paramSet.freq_width);
            rootObject.put("data", obj);

            body = rootObject.toString();
            //JsonWriter writer = new JsonWriter();

        } catch (Exception e) {

        }

        mLastRespondMillis = -1;
        boolean b = postHttpRequest(kServiceURL, body, new OkHttpCallback() {
            @Override
            public void onHttpRespond(int code,int type,byte[]body) {
                ResponseBase resp = new ResponseBase();
                resp.respCode = code;
                if (code == HTTP_CODE_OK) {
                    try {
                        String resultString = body.toString();
                        JSONObject rootObject = new JSONObject(resultString);
                        //JSONObject dicData = rootObject.getJSONObject("data");
                        resp.errCode = rootObject.getInt("errcode");
                        resp.errMsg = rootObject.getString("errmsg");
                    } catch (Exception e) {
                        resp.errCode = -2;
                    }
                    mIsHttpConnected = true;
                } else {
                    resp.errCode = -1;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });

        return b;
    }

    // 获取参数
    public static boolean doGetParamSet(final Callback callback) {
        String body = "";//null;
        try {
            // 组request json包
            JSONObject rootObject = new JSONObject();
            rootObject.put("type", MSG_TYPE_GET_PARAM);
            JSONObject obj = new JSONObject("{name1:value1,name2:value2}");
            rootObject.put("data", obj);

            body = rootObject.toString();
            //JsonWriter writer = new JsonWriter();

        } catch (Exception e) {

        }

        mLastRespondMillis = -1;
        boolean b = postHttpRequest(kServiceURL, body, new OkHttpCallback() {
            @Override
            public void onHttpRespond( int code,int type,byte[]body ) {
                ParamSetResp resp = new ParamSetResp();
                resp.paramSet = new ParamSetData();
                resp.respCode = code;
                if (code == HTTP_CODE_OK) {
                    try {
                        String resultString = body.toString();
                        JSONObject rootObject = new JSONObject(resultString);
                        JSONObject dicData = rootObject.getJSONObject("data");
                        resp.paramSet.lineName = dicData.getString("line_name");
                        resp.paramSet.jointName = dicData.getString("joint_name");
                        resp.paramSet.work_interval = Integer.valueOf(dicData.getString("work_interval"));
                        resp.paramSet.freq_center = Integer.valueOf(dicData.getString("center_freq"));
                        resp.paramSet.freq_width = Integer.valueOf(dicData.getString("band_width"));
                        resp.errCode = 0;
                    } catch (Exception e) {
                        resp.errCode = -2;
                    }
                    mIsHttpConnected = true;
                } else {
                    resp.errCode = -1;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });

        return b;
    }

    //获取测试谱图
    public static boolean doGetTestSpectrum(final Callback callback)
    {
        String body = "";//null;
        try {
            // 组request json包
            JSONObject rootObject = new JSONObject();
            rootObject.put("type", MSG_TYPE_GET_TEST_SPECTRUM);
            body = rootObject.toString();

        } catch (Exception e) {
        }
        mLastRespondMillis = -1;
        boolean b = postHttpRequest(kServiceURL, body, new OkHttpCallback() {
            @Override
            public void onHttpRespond( int code,int type,byte[]body ) {
                GetSpectrumResp resp = new GetSpectrumResp();
                resp.respCode = code;
                if (code == HTTP_CODE_OK) {
                    try {
                        int offset = 0;
                        ByteBuffer buffer = ByteBuffer.allocate(body.length);
                        while(body[offset] !=0){
                            SpectrumData spectrum_data = new SpectrumData();
                            buffer.put(body,offset,MSG_LEN_CHUNK);
                            int chunkLen = buffer.getInt();
                            offset += MSG_LEN_CHUNK;
                            buffer.put(body,offset,MSG_LEN_FILE_NAME);
                            spectrum_data.spectrumName = buffer.toString();
                            offset += MSG_LEN_FILE_NAME;
                            buffer.put(body,offset,chunkLen-MSG_LEN_FILE_NAME);
                            buffer.flip();
                            byte[] spectrumByte = buffer.array();
                            spectrum_data.spectrumData = BitmapFactory.decodeByteArray(spectrumByte,0,spectrumByte.length);
                            offset += (chunkLen-MSG_LEN_FILE_NAME);
                            resp.getSpectrumInfo.add(spectrum_data);
                        }
                        resp.errCode = 0;
                    } catch (Exception e) {
                        resp.errCode = -2;
                    }
                    mIsHttpConnected = true;
                } else {
                    resp.errCode = -1;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });
        return b;
    }
    //获取下拉列表
    public static boolean doGetLineInfo(final Callback callback)
    {
        String body = "";//null;
        try {
            // 组request json包
            JSONObject rootObject = new JSONObject();
            rootObject.put("type", MSG_TYPE_GET_LINE_INFO);
            body = rootObject.toString();

        } catch (Exception e) {
        }
        mLastRespondMillis = -1;
        boolean b = postHttpRequest(kServiceURL, body, new OkHttpCallback() {
            @Override
            public void onHttpRespond( int code,int type,byte[]body ) {
                GetLineInfoResp resp = new GetLineInfoResp();
                resp.respCode = code;
                if (code == HTTP_CODE_OK) {
                    try {
                        //返回值格式
                        //“data” : [
			            //[“line_name1”, “line_pos1” , “line_pos2” , “line_pos3”,……],
                        //[“line_name2”, “line_pos1” , “line_pos2” , “line_pos3” ,……],
	                    //]
                        String resultString = body.toString();
                        JSONObject rootObject = new JSONObject(resultString);
                        JSONArray arrLineInfoData = rootObject.getJSONArray("data");
                        int size = arrLineInfoData.length();
                        for(int i  = 0 ; i<size ; i++){
                            LineData lineInfoData = new LineData();
                            String lineString = arrLineInfoData.getString(i);
                            String[] returnString = lineString.split(",");
                            int index = 0;
                            for (String str:returnString
                                 ) {
                                if(index == 0){
                                    lineInfoData.lineName =  str;
                                }
                                else{
                                    lineInfoData.arrJointName.add(str);
                                }
                                index++;
                            }
                            resp.lineList.add(lineInfoData);
                        }
                        resp.errCode = 0;
                    } catch (Exception e) {
                        resp.errCode = -2;
                    }
                    mIsHttpConnected = true;
                } else {
                    resp.errCode = -1;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });
        return b;
    }

    //按条件查目录
    public static boolean doFilterCatalogue(FilterCatalogueData filterCase,final Callback callback)
    {
        String body = "";//null;
        try {
            // 组request json包
            JSONObject rootObject = new JSONObject();
            rootObject.put("type", MSG_TYPE_FILTER_CATALOGUE);
            JSONObject dataObj = new JSONObject();
            dataObj.put("line_name",filterCase.lineName);
            dataObj.put("joint_name",filterCase.jointName);
            dataObj.put("start_date",filterCase.startTime);
            dataObj.put("end_date",filterCase.endTime);
            dataObj.put("conclusion",filterCase.conclusionType);
            rootObject.put("data",dataObj);
            body = rootObject.toString();

        } catch (Exception e) {
        }
        mLastRespondMillis = -1;
        boolean b = postHttpRequest(kServiceURL, body, new OkHttpCallback() {
            @Override
            public void onHttpRespond( int code,int type,byte[]body ) {
                FilterCatalogueResp resp = new FilterCatalogueResp();
                resp.respCode = code;
                if (code == HTTP_CODE_OK) {
                    try {
                        //返回值格式
                        //“data” : [
                        //[“line_name”, “line_pos” , “date” , “time1,time2,……”, ”conclusion”],
                        //[“line_name”, “line_pos” , “date” , “time1,time2,……”, ”conclusion”],
                        //]
                        String resultString = body.toString();
                        JSONObject rootObject = new JSONObject(resultString);
                        JSONArray arrCatalogueData = rootObject.getJSONArray("data");
                        int size =arrCatalogueData.length();
                        for(int i  = 0 ; i<size ; i++){
                            CatalogueData catalogueData = new CatalogueData();
                            String catalogue = arrCatalogueData.getString(i);
                            String[] returnString = catalogue.split(",");
                            if(returnString.length == 5){
                                catalogueData.lineName = returnString[0];
                                catalogueData.jointName = returnString[1];
                                catalogueData.dateInfo = returnString[2];
                                String[] arrTime = returnString[3].split(",");
                                for(String time:arrTime){
                                    catalogueData.arrTimeInfo.add(time);
                                }
                                catalogueData.conclusion = Integer.getInteger(returnString[4]);

                                resp.catalogueList.add(catalogueData);
                            }
                            resp.catalogueList.add(catalogueData);
                        }
                        resp.errCode = 0;
                    } catch (Exception e) {
                        resp.errCode = -2;
                    }
                    mIsHttpConnected = true;
                } else {
                    resp.errCode = -1;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });
        return b;
    }

    //下载数据
    public static boolean doDownloadData(DownloadData downloadData, final Callback callback)
    {
        String body = "";//null;
        try {
            // 组request json包
            JSONObject rootObject = new JSONObject();
            rootObject.put("type", MSG_TYPE_FILTER_CATALOGUE);
            JSONObject dataObj = new JSONObject();
            dataObj.put("line_name",downloadData.lineName);
            dataObj.put("joint_name",downloadData.jointName);
            dataObj.put("date",downloadData.date);
            dataObj.put("time",downloadData.time);

            rootObject.put("data",dataObj);
            body = rootObject.toString();

        } catch (Exception e) {
        }
        mLastRespondMillis = -1;
        boolean b = postHttpRequest(kServiceURL, body, new OkHttpCallback() {
            @Override
            public void onHttpRespond( int code,int type,byte[]body ) {
                GetSpectrumResp resp = new GetSpectrumResp();
                resp.respCode = code;
                if (code == HTTP_CODE_OK) {
                    try {
                        int offset = 0;
                        ByteBuffer buffer = ByteBuffer.allocate(body.length);
                        while(body[offset] !=0){
                            SpectrumData spectrum_data = new SpectrumData();
                            buffer.put(body,offset,MSG_LEN_CHUNK);
                            int chunkLen = buffer.getInt();
                            offset += MSG_LEN_CHUNK;
                            buffer.put(body,offset,MSG_LEN_FILE_NAME);
                            spectrum_data.spectrumName = buffer.toString();
                            offset += MSG_LEN_FILE_NAME;
                            buffer.put(body,offset,chunkLen-MSG_LEN_FILE_NAME);
                            buffer.flip();
                            byte[] spectrumByte = buffer.array();
                            spectrum_data.spectrumData = BitmapFactory.decodeByteArray(spectrumByte,0,spectrumByte.length);
                            offset += (chunkLen-MSG_LEN_FILE_NAME);
                            resp.getSpectrumInfo.add(spectrum_data);
                        }
                        resp.errCode = 0;
                    }
                    catch (Exception e) {
                        resp.errCode = -2;
                    }
                    mIsHttpConnected = true;
                } else {
                    resp.errCode = -1;
                    mIsHttpConnected = false;
                }
                callback.onResponse(resp);
                // 设置最后一次成功的时钟
                mLastRespondMillis = System.currentTimeMillis();
            }
        });
        return b;
    }
}
