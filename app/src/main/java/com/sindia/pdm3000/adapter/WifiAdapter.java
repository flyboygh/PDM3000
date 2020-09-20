package com.sindia.pdm3000.adapter;

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
    public interface Callback {
        public void connectWifiClick(String SSID);
    }

    private Context mContext = null;
    private Callback mCallback = null;
    private WifiAdmin mWiFiAdmin;
    public List<ScanResult> mScanList = new ArrayList<>();

    public WifiAdapter(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
        mWiFiAdmin = new WifiAdmin(context);
    }

    @Override
    public int getCount() {
        return mScanList.size();
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
        ScanResult s_result = mScanList.get(i);
        String s = s_result.SSID;
        if (s != null) {
            txt_aName.setText(s);
        } else {
            Log.d("", "");
        }

        // 无线网连接状态
        int wifiState = mWiFiAdmin.checkState();

        Button btnConnect = view.findViewById(R.id.buttonConnect);
        String curBSSID = mWiFiAdmin.getConnectedBSSID(mContext);
        if (curBSSID.equals(s_result.BSSID)) {
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                btnConnect.setText(R.string.disconn);
            } else {
                btnConnect.setText(R.string.connecting);
            }
        } else {
            btnConnect.setText(R.string.connect);
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
        ScanResult s_result = mScanList.get(index);
        String SSID = s_result.SSID;
        String curBSSID = mWiFiAdmin.getConnectedBSSID(mContext);
        if (curBSSID.equals(s_result.BSSID)) { // 当前已连接，需要断开
            mWiFiAdmin.disconnWifiBySSID(mContext, SSID);
            //mCallback.connectWifiClick(SSID);
        } else {
            mWiFiAdmin.connectWifiBySSID(mContext, SSID);
            //mCallback.connectWifiClick(SSID);
        }
    }
}
