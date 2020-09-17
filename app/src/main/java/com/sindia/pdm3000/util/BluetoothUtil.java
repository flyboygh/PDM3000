package com.sindia.pdm3000.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class BluetoothUtil {
    // 蓝牙状态变化回调
    public interface BluetoothStateCallback {
        public void onBluetoothOpened(boolean open);
    }
    public BluetoothStateCallback mBluetoothCallback;

    private static final String TAG = "BluetoothUtil"; //Main";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothStateBroadcastReceive mReceive;

    public BluetoothUtil() {
        // 获得蓝牙适配器对象
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean getBlueToothState() {
        // 获取蓝牙状态
        return bluetoothAdapter.isEnabled();
    }

    public boolean openBlueTooth() {
        if (getBlueToothState()) return true;
        // 打开蓝牙
        return bluetoothAdapter.enable();
    }

    public boolean colseBlueTooth() {
        if (!getBlueToothState()) return true;
        // 关闭蓝牙
        return bluetoothAdapter.disable();
    }

    // 调用系统的请求打开蓝牙
    public void gotoSystem(Context context){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        context.startActivity(intent);
    }

    public void registerBluetoothReceiver(Context context){
        if(mReceive == null){
            mReceive = new BluetoothStateBroadcastReceive();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        context.registerReceiver(mReceive, intentFilter);
    }

    public void unregisterBluetoothReceiver(Context context){
        if(mReceive != null){
            context.unregisterReceiver(mReceive);
            mReceive = null;
        }
    }

    class BluetoothStateBroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (action){
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Toast.makeText(context , "蓝牙设备:" + device.getName() + "已连接", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onReceive: "+"蓝牙设备:" + device.getName() + "已连接");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Toast.makeText(context , "蓝牙设备:" + device.getName() + "已断开", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onReceive: "+"蓝牙设备:" + device.getName() + "已断开");
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState){
                        case BluetoothAdapter.STATE_OFF:
                            Toast.makeText(context , "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onReceive: "+"蓝牙已关闭:" );
                            mBluetoothCallback.onBluetoothOpened(false);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Toast.makeText(context , "蓝牙已开启"  , Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onReceive: "+"蓝牙已开启:");
                            mBluetoothCallback.onBluetoothOpened(true);
                            break;
                    }
                    break;
            }
        }
    }
}
