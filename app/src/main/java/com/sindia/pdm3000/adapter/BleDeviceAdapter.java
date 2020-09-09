package com.sindia.pdm3000.adapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.sindia.pdm3000.R;

import java.util.ArrayList;

public class BleDeviceAdapter extends BaseAdapter {
    public Context mContext = null;
    public BluetoothDevice mConnDevice = null;
    public ArrayList<ScanResult> mScanList = new ArrayList<>();

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
        view = LayoutInflater.from(mContext).inflate(R.layout.ble_device_cell, viewGroup,false);
        TextView txt_aName = view.findViewById(R.id.textViewDeviceName);
        String s = mScanList.get(i).getScanRecord().getDeviceName();
        txt_aName.setText(s);
        Button btnConnect = view.findViewById(R.id.buttonConnect);
        if (mConnDevice != null && mConnDevice.equals(mScanList.get(i).getDevice())) {
            btnConnect.setText(R.string.disconn);
        } else {
            btnConnect.setText(R.string.connect);
        }
        return view;
    }
}