package com.sindia.pdm3000.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// okhttp相关的接口（先自己尝试封装，以后直接用别人家的）
public class OkHttpHelper {
    // 常用宏定义
    private static final int kHttpPostTimeoutS = 5;
    private static final int MAX_REV_LEN = 1024000;
    private static final int RETURN_TYPE_JSON = 1;
    private static final int RETURN_TYPE_CHUNK = 2;

    // 向URL链接POST数据，成功返回true，失败返回false
    public static boolean postHttpRequest(final String url, final String body, final OkHttpCallback callback) {
        // Android 4.0 之后不能在主线程中请求HTTP请求
        new Thread(new Runnable(){
            @Override
            public void run() {
                // 创建OkHttpClient对象，这种方式创建，设置都是default。如果要设置超时时间，比如读取的超时时间，可以使用newBuild()方法设置
                OkHttpClient client = new OkHttpClient()
                        .newBuilder()
                        .readTimeout(kHttpPostTimeoutS, TimeUnit.SECONDS)
                        .build();

                // 创建Request对象
                //创建Request和RequestBody
                //对于GET方法，我们不需要RequestBody，参数直接加在URL上传值就行了。
                //后台比如spring boot可以使用@RequestParam(“参数名”)的方式拿到。
                //我们这样使用OkHttp框架的Request
                //Request request = new Request.Builder().url(url).get().build();

                //JSONObject sender = new JSONObject();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                //MediaType需要指定,我们从字符串解析它
                RequestBody requestBody = RequestBody.create(JSON, body);
                //RequestBody requestBody = FormBody.create( JSON, sender);
                //我们指定了RequestBody中的MediaType和要发送的数据
                Request request = new Request.Builder().url(url).post(requestBody).build();
//我们创建了一个Request对象并设置为POST方法,把requestBody加入了它

                int code = -1;
                byte[] resultByte = new byte[MAX_REV_LEN];
                int resultType = RETURN_TYPE_CHUNK;
                try {
                    Response response = client.newCall(request).execute();
                    code = response.code();
                    String responseHeader = response.header("Content-Type");
                    if(responseHeader.equals("application/json")){
                        resultType = RETURN_TYPE_JSON;
                    }
                    else{
                        resultType = RETURN_TYPE_CHUNK;
                    }
                    resultByte = response.body().bytes();
                    //byte[] byteData = response.body().bytes();//获得byte对象
                    //InputStream is = response.body().byteStream();//获得输入流

                    //String s = String.valueOf(response.body());
                    //Log.i("", s);
                } catch (IOException e) { // 无网络会到这里
                    Log.e("", e.getMessage());
                }
                final int f_code = code;
                final int f_type = resultType;
                final byte[]  f_result_byte = resultByte;
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                            callback.onHttpRespond(f_code,f_type,f_result_byte);
                            //callback.onHttpRespond(code, result);
                    }
                });
            }
        }).start();
        return true;
    }

    // 请求响应回调（测试接口）
    public interface OkHttpCallback {
        //public void onHttpRespond(int code, String body);
        public void onHttpRespond(int code ,int type,byte[]body);
    }

    // http请求（测试方法）
    public static boolean requestHttp(final String url, final OkHttpCallback callback) {
        // Android 4.0 之后不能在主线程中请求HTTP请求
        new Thread(new Runnable(){
            @Override
            public void run() {
                //cachedImage = asyncImageLoader.loadDrawable(imageUrl, position);
                //imageView.setImageDrawable(cachedImage);
                // 创建OkHttpClient对象，这种方式创建，设置都是default。如果要设置超时时间，比如读取的超时时间，可以使用newBuild()方法设置
                OkHttpClient client = new OkHttpClient()
                        .newBuilder()
                        .readTimeout(5, TimeUnit.SECONDS)
                        .build();

                // 创建Request对象
                //创建Request和RequestBody
                //对于GET方法，我们不需要RequestBody，参数直接加在URL上传值就行了。
                //后台比如spring boot可以使用@RequestParam(“参数名”)的方式拿到。
                //我们这样使用OkHttp框架的Request
                //Request request = new Request.Builder().url(url).get().build();

                //JSONObject sender = new JSONObject();
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                //MediaType需要指定,我们从字符串解析它
                RequestBody requestBody = RequestBody.create(JSON, "");
                //RequestBody requestBody = FormBody.create( JSON, sender);
                //我们指定了RequestBody中的MediaType和要发送的数据
                Request request = new Request.Builder().url(url).post(requestBody).build();
//我们创建了一个Request对象并设置为POST方法,把requestBody加入了它

                try {
                    Response response = client.newCall(request).execute();
                    int code = response.code();
                    String responseHeader = response.header("Content-Type");
                    byte[] resultByte  = response.body().bytes();//获得String对象
                    //byte[] byteData = response.body().bytes();//获得byte对象
                    //InputStream is = response.body().byteStream();//获得输入流

                    //String s = String.valueOf(response.body());
                    //Log.i("", s);
                    int resultType = RETURN_TYPE_CHUNK;
                    if(responseHeader.equals("application/json")){
                        resultType = RETURN_TYPE_JSON;
                    }
                    else{
                        resultType = RETURN_TYPE_CHUNK;
                    }
                    callback.onHttpRespond(code, resultType,resultByte);
                } catch (IOException e) {
                    Log.e("", e.getMessage());
                }
            }
        }).start();
        return false;
    }
}
