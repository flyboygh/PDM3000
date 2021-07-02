package com.sindia.pdm3000;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sindia.pdm3000.http.PdHttpRequest;

import java.io.InputStream;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class TestImageActivity extends AppCompatActivity {

    // 顶部导航栏相关
    private SubNavigation mNavigation;
    //测试谱图相关
    private TextView mWaitText;
    private LinearLayout mSpectrumLayout;
    private ImageView mSpectrumPhaseA;
    private ImageView mSpectrumPhaseB;
    private ImageView mSpectrumPhaseC;
    // 定时器
    private static final int kTestImageTimerID = 102;
    private static final long kTestImageDelay = 1000;
    private Handler mTimerHandler;

    private static int mGetSpectrumMaxTimes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test_image);
        // 创建顶部主导航
        mNavigation = super.findViewById(R.id.naviMain);
        mNavigation.setTitle("测试图片");
        mNavigation.setClickCallback(mMainNavigationCallBack);

        mWaitText = super.findViewById(R.id.textWaitImage);
        mWaitText.setText("正在获取测试谱图......");
        mGetSpectrumMaxTimes = 0;
        mWaitText.setVisibility(View.VISIBLE);

        mSpectrumLayout = super.findViewById(R.id.layoutSpectrum);
        mSpectrumLayout.setVisibility(View.INVISIBLE);
        mSpectrumPhaseA = super.findViewById(R.id.imagePhaseA);
        mSpectrumPhaseB = super.findViewById(R.id.imagePhaseB);
        mSpectrumPhaseC = super.findViewById(R.id.imagePhaseC);
        // 创建主定时器并唤起
        mTimerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == kTestImageTimerID) {
                    onMainTimerMessage(msg.what);
                }
            }
        };
        //进入界面立即执行一次
        mTimerHandler.sendEmptyMessage(kTestImageTimerID);
    }


    // 顶部导航栏事件
    private SubNavigation.ClickCallback mMainNavigationCallBack = new SubNavigation.ClickCallback() {
        @Override
        public void onBackClick() {
            finish();
        }

        @Override
        public void onRightClick() {

        }
    };

    //定时器消息
    private void onMainTimerMessage(int  msgType) {
        //向设备查询测试谱图，返回则取消定时器，否则每隔一秒钟查询一次
        final int f_msg_type = msgType;
        PdHttpRequest.doGetTestSpectrum( new PdHttpRequest.Callback() {
            @Override
            public void onResponse(PdHttpRequest.ResponseBase resp) {
                boolean is_success = true;
                if (resp.errCode == 0) {
                    PdHttpRequest.GetSpectrumResp spectrumResponse = ( PdHttpRequest.GetSpectrumResp )resp;
                    if(spectrumResponse.getSpectrumInfo.size() ==0){
                        is_success = false;
                    }
                    else{
                        mTimerHandler.removeMessages(f_msg_type);
                        //获取测试谱图成功
                        mWaitText.setVisibility(View.VISIBLE);
                        mSpectrumLayout.setVisibility(View.INVISIBLE);
                        if(spectrumResponse.getSpectrumInfo.size() == 3){
                            mSpectrumPhaseA.setImageBitmap(spectrumResponse.getSpectrumInfo.get(0).spectrumData);
                            mSpectrumPhaseB.setImageBitmap(spectrumResponse.getSpectrumInfo.get(1).spectrumData);
                            mSpectrumPhaseC.setImageBitmap(spectrumResponse.getSpectrumInfo.get(2).spectrumData);
                        }
                    }
                }
                else{
                    is_success = false;
                }
                if(is_success){
                    mTimerHandler.removeMessages(f_msg_type);
                }
                else{
                    mTimerHandler.removeMessages(f_msg_type);
                    mGetSpectrumMaxTimes++;
                    if(mGetSpectrumMaxTimes >5){
                        mGetSpectrumMaxTimes = 0;
                        mWaitText.setText("获取测试谱图失败");
                    }
                    else{
                        mTimerHandler.sendEmptyMessageDelayed(f_msg_type, kTestImageDelay);
                    }
                }
            }
        });
    }
}