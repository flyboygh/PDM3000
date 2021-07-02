package com.sindia.pdm3000.adapter;

import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.sindia.pdm3000.R;
import com.sindia.pdm3000.util.WifiAdmin;

import java.util.ArrayList;
import java.util.List;

// 无线网记录适配类
public class WifiAdapter extends BaseAdapter implements View.OnClickListener {
    private static final String kSindiaWifiPrefix = "SINDIA"; // 兴迪3000无线网前缀
    private static final String kSindiaWifiPrefix2 = "USR-WIFI"; // 兴迪3000无线网前缀
    private static final String kSindiaWifiPasswd = "wifi@sindia.cn"; // 兴迪3000无线网密码

    private Context mContext = null;
    private Callback mCallback = null;
    private WifiAdmin mWiFiAdmin = null;

    private List<ScanResult> mShowWifiList = new ArrayList<>(); // 当前‘有效的’无线网列表，可以在界面上显示的

    // 上层回调
    public interface Callback {
        public void connectWifiClick(String SSID);
    }

    // 构造函数
    public WifiAdapter(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
        mWiFiAdmin = new WifiAdmin(context);
    }

    // 更新兴迪无线网列表
    public void updateWifiList(List<ScanResult> wifiList) {
        // 先把需要显示的有效元素加进去
        mShowWifiList.clear();
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult s_result = wifiList.get(i);
            if (s_result.BSSID != null && !s_result.BSSID.isEmpty() &&
                s_result.SSID != null && !s_result.SSID.isEmpty()) {
                mShowWifiList.add(s_result);
            }
        }

        // 根据规则排序（已连接的优先，兴迪其次）
        for (int i = 0; i < mShowWifiList.size() - 1; i++) {
            for (int j = i + 1; j < mShowWifiList.size(); j++) {
                ScanResult s1 = mShowWifiList.get(i);
                ScanResult s2 = mShowWifiList.get(j);
                int n1 = getWifiSortInt(s1);
                int n2 = getWifiSortInt(s2);
                if (n1 < n2) {
                    mShowWifiList.set(i, s2);
                    mShowWifiList.set(j, s1);
                }
            }
        }
    }

    // 是否连接到了兴迪无线网
    public boolean hasConnectedSindiaWifi() {
        String curSSID = mWiFiAdmin.getConnectedSSID(mContext);
        if (isSindiaWifi(curSSID)) {
            return true;
        }
        return false;
    }

    // 下面是内部工具方法
    private boolean isSindiaWifi(String ssid) {
        if (ssid.toUpperCase().startsWith(kSindiaWifiPrefix)) {
            return true;
        }
        if (ssid.toUpperCase().startsWith(kSindiaWifiPrefix2)) {
            return true;
        }
        return false;
    }

    private int getWifiSortInt(final ScanResult s_result) {
        String curBSSID = mWiFiAdmin.getConnectedBSSID(mContext);
        if (curBSSID.equals(s_result.BSSID)) {
            return 2;
        }
        if (isSindiaWifi(s_result.SSID)) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getCount() {
        return mShowWifiList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;//0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(mContext).inflate(R.layout.wifi_cell, viewGroup,false);

        // 无线网名称
        TextView txt_aName = view.findViewById(R.id.textViewDeviceName);
        ScanResult s_result = mShowWifiList.get(i);
        String ssid = s_result.SSID;
        if (ssid != null) {
            if (isSindiaWifi(ssid)) {
                txt_aName.getPaint().setFakeBoldText(true);
            } else {
                txt_aName.getPaint().setFakeBoldText(false);
            }
            txt_aName.setText(ssid);
        } else { // 内错
            Log.e("内错", "getView中ssid为空");
        }

        // 无线网连接状态
        int wifiState = mWiFiAdmin.checkState();

        Button btnConnect = view.findViewById(R.id.buttonConnect);
        String curBSSID = mWiFiAdmin.getConnectedBSSID(mContext);
        if (curBSSID.equals(s_result.BSSID)) {
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                btnConnect.setText(R.string.disconn);
                txt_aName.setTextColor(Color.BLUE);
            } else {
                btnConnect.setText(R.string.connecting);
                txt_aName.setTextColor(Color.RED);
            }
        } else {
            btnConnect.setText(R.string.connect);
            txt_aName.setTextColor(Color.BLACK);
        }
        /*Button btnConnect = view.findViewById(R.id.buttonConnect);
        if (mConnDevice != null) {
            if (mConnDevice.equals(mScanList.get(i).getDevice())) {
                btnConnect.setText(R.string.disconn);
                btnConnect.setVisibility(View.VISIBLE);
            } else {
                btnConnect.setVisibility(View.INVISIBLE);
            }
        } else {
            btnConnect.setText(R.string.connect);
            btnConnect.setVisibility(View.VISIBLE);
        }*/
        btnConnect.setOnClickListener(this);
        btnConnect.setTag(i);
        return view;
    }

    @Override
    public void onClick(View view) {
        int index = (int)view.getTag();
        ScanResult s_result = mShowWifiList.get(index);
        String SSID = s_result.SSID;
        String curBSSID = mWiFiAdmin.getConnectedBSSID(mContext);
        if (curBSSID.equals(s_result.BSSID)) { // 当前已连接，需要断开
            mWiFiAdmin.disconnWifiBySSID(mContext, SSID);
            //mCallback.connectWifiClick(SSID);
        } else {
            String PSWD = "";
            if (isSindiaWifi(SSID)) {
                PSWD = kSindiaWifiPasswd;
            }
            mWiFiAdmin.connectWifiBySSID(mContext, SSID, PSWD);
            //mCallback.connectWifiClick(SSID);
        }
    }
}
