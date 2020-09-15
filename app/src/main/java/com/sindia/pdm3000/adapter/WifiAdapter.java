package com.sindia.pdm3000.adapter;

import android.net.wifi.ScanResult;
import android.content.Context;
import android.net.wifi.WifiInfo;
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
        public void connectWifiClick(int index);
    }

    private Context mContext = null;
    private Callback mCallback = null;
    public List<ScanResult> mScanList = new ArrayList<>();

    public WifiAdapter(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
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
        TextView txt_aName = view.findViewById(R.id.textViewDeviceName);
        ScanResult s_result = mScanList.get(i);
        String s = s_result.SSID;
        if (s != null) {
            txt_aName.setText(s);
        } else {
            Log.d("", "");
        }

        Button btnConnect = view.findViewById(R.id.buttonConnect);
        WifiInfo wifiInfo = WifiAdmin.getConnectWifiInfo(mContext);// getInstance().getWifiManager().getConnectionInfo();
        String curBSSID = ( wifiInfo == null ? "" : wifiInfo.getBSSID() );//  WifiAdmin.getInstance().getWifiManager().getConnectionInfo().getBSSID();// getBSSID();
        if (curBSSID.equals(s_result.BSSID)) {
            btnConnect.setText(R.string.disconn);
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
        }
        btnConnect.setOnClickListener(this);
        btnConnect.setTag(i);*/
        return view;
    }

    @Override
    public void onClick(View view) {

    }
}
