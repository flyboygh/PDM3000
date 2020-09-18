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
import java.util.List;

public class BleDeviceAdapter extends BaseAdapter implements View.OnClickListener {
    public interface Callback {
        public void activateBleClick(BluetoothDevice device);//int index);
    }

    private Context mContext = null;
    private Callback mCallback = null;
    //public BluetoothDevice mConnDevice = null; // 当前连接的蓝牙设备
    public List<ScanResult> mScanList = new ArrayList<>();

    public BleDeviceAdapter(Context context, Callback callback) {
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
        view = LayoutInflater.from(mContext).inflate(R.layout.ble_device_cell, viewGroup,false);
        TextView txt_aName = view.findViewById(R.id.textViewDeviceName);
        String s = mScanList.get(i).getScanRecord().getDeviceName();
        txt_aName.setText(s);
        Button btnActivate = view.findViewById(R.id.buttonActivate);
        /*if (mConnDevice != null) {
            if (mConnDevice.equals(mScanList.get(i).getDevice())) {
                btnActivate.setText(R.string.disconn);
                btnActivate.setVisibility(View.VISIBLE);
            } else {
                btnActivate.setVisibility(View.INVISIBLE);
            }
        } else {
            btnActivate.setText(R.string.activate);
            btnActivate.setVisibility(View.VISIBLE);
        }*/
        btnActivate.setOnClickListener(this);
        btnActivate.setTag(i);
        return view;
    }

    @Override
    public void onClick(View view) {
        int index = (int)view.getTag();
        ScanResult s_result = mScanList.get(index);
        mCallback.activateBleClick(s_result.getDevice());
    }
}
