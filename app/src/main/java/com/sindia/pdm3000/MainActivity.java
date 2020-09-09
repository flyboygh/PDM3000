package com.sindia.pdm3000;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sindia.pdm3000.adapter.BleDeviceAdapter;
import com.sindia.pdm3000.ble.IBleDeviceScan;
import com.sindia.pdm3000.ble.BleManager;
import com.sindia.pdm3000.util.BluetoothUtil;
import com.sindia.pdm3000.util.LocationUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    // 常量
    private static final String TAG = "MainActivity";
    // 状态相关的
    private boolean mScanning = false;
    // 控件相关的
    private ListView mListView = null;
    // 蓝牙相关的
    private BluetoothUtil _BluetoothUtil = null;
    private BleDeviceAdapter mDeviceAdapter;
    private BluetoothGatt mBleGatt = null;
    private BluetoothStateBroadcastReceive mBluetoothReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothReceive = new BluetoothStateBroadcastReceive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        this.registerReceiver(mBluetoothReceive, intentFilter);

        //禁止旋转（在xml写了）
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 启动后如果没有定位能力就尝试开启
        //boolean b = LocationUtil::checkLocationPermission();
        LocationUtil.requestLocationPermission(this);

        // 蓝牙工具类
        _BluetoothUtil = new BluetoothUtil();
/*      // 调用工具模块监听蓝牙状态
        _BluetoothUtil.registerBluetoothReceiver(this);
        Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());
        _BluetoothUtil.colseBlueTooth();
        Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());
        _BluetoothUtil.openBlueTooth();
        //_BluetoothUtil.gotoSystem(this);
        Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());
*/
        _BluetoothUtil.openBlueTooth();

        // 设备列表相关
        mDeviceAdapter = new BleDeviceAdapter();
        mDeviceAdapter.mContext = this;
        mListView = findViewById(R.id.listviewDevices);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mDeviceAdapter);

        UpdateActivityControls();

        // 设备扫描结果回调
        BleManager.getInstance().bleDeviceScan = new IBleDeviceScan() {
            @Override
            public void onBleDeviceChanged(ArrayList<ScanResult> deviceList) {
                mDeviceAdapter.mScanList = deviceList;
                mListView.setAdapter(mDeviceAdapter);
            }
        };
    }

    // 根据功能状态，更新控件状态
    private void UpdateActivityControls() {
        Button btn = findViewById(R.id.buttonScan);
        if (mScanning) { // 正在扫描蓝牙
            if (_BluetoothUtil.getBlueToothState()) {
                btn.setText(R.string.stop_scan);
            } else {
                //buttonScanClick(null);
                if (BleManager.getInstance().stopScanBleDevice(this)) {
                    mScanning = false;
                }
                btn.setText(R.string.open_ble);
            }
        } else { // 未在扫描蓝牙
            if (_BluetoothUtil.getBlueToothState()) {
                btn.setText(R.string.start_scan);
            } else {
                btn.setText(R.string.open_ble);
            }
        }
    }

    // 开启蓝牙/扫描/停止
    public void buttonScanClick(View view) {
        //switch (view.getId()) {
        //    case R.id.buttonScan:
        //    {
        if (!_BluetoothUtil.getBlueToothState()) { // 蓝牙未开启
            _BluetoothUtil.openBlueTooth(); // 开启蓝牙
            return;
        }

        Button btn = findViewById(R.id.buttonScan);
        if (!mScanning) { // 开启扫描
            //请求定位权限
            if (!LocationUtil.checkLocationPermission(this)) {
                LocationUtil.requestLocationPermission(this);
                return;
            }
            if (BleManager.getInstance().startScanBleDevice(this)) {
                mScanning = true;
                //btn.setText(R.string.stop_scan);
            }
        } else { // 停止扫描
            if (BleManager.getInstance().stopScanBleDevice(this)) {
                mScanning = false;
                //btn.setText(R.string.start_scan);
            }
        }
        UpdateActivityControls();
        //        break;
        //    }
        //}
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ScanResult sr = mDeviceAdapter.mScanList.get(i);
        //String dn = sr.getScanRecord().getDeviceName();
        BluetoothDevice device = sr.getDevice();
        if (mBleGatt != null) {
            if (mBleGatt.getDevice().equals(device)) {
                device = null;
            }
            mBleGatt.disconnect();
            mBleGatt = null;
            mDeviceAdapter.mConnDevice = null;
        }
        if (device != null) {
            mBleGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
                @Override
                public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                    super.onPhyUpdate(gatt, txPhy, rxPhy, status);
                }

                @Override
                public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                    super.onPhyRead(gatt, txPhy, rxPhy, status);
                }

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                }

                @Override
                public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                    super.onReliableWriteCompleted(gatt, status);
                }

                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                    super.onReadRemoteRssi(gatt, rssi, status);
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);
                }
            });
        }
        mDeviceAdapter.mConnDevice = device;
        mListView.setAdapter(mDeviceAdapter);
    }

    // 蓝牙状态接收
    class BluetoothStateBroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED: {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null && device.getName() != null) {
                        Toast.makeText(context, "蓝牙设备:" + device.getName() + "已连接", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onReceive: " + "蓝牙设备:" + device.getName() + "已连接");
                    }
                    break;
                }
                case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null && device.getName() != null) {
                        Toast.makeText(context, "蓝牙设备:" + device.getName() + "已断开", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onReceive: " + "蓝牙设备:" + device.getName() + "已断开");
                    }
                    break;
                }
                case BluetoothAdapter.ACTION_STATE_CHANGED: {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_OFF:
                            Toast.makeText(context, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onReceive: " + "蓝牙已关闭:");
                            UpdateActivityControls();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Toast.makeText(context, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onReceive: " + "蓝牙已开启:");
                            UpdateActivityControls();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            break;
                    }
                    break;
                }
            }
        }
    }
}