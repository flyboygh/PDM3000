package com.sindia.pdm3000;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.view.View;
import android.widget.ListView;

import com.sindia.pdm3000.adapter.BleDeviceAdapter;
import com.sindia.pdm3000.ble.IBleDeviceScan;
import com.sindia.pdm3000.ble.BleManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private BleDeviceAdapter mDeviceAdapter;
    private ListView mListView = null;
    //private ScanResult mConnected = null;
    private BluetoothGatt mBleGatt = null;
    private boolean mScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //请求权限
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION},
                0);

        // 检查并开启蓝牙
        BleManager.getInstance().checkBluetoothOpened(this);

        mDeviceAdapter = new BleDeviceAdapter();
        mDeviceAdapter.mContext = this;

        mListView = findViewById(R.id.listviewDevices);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mDeviceAdapter);

        BleManager.getInstance().bleDeviceScan = new IBleDeviceScan() {
            @Override
            public void onBleDeviceChanged(ArrayList<ScanResult> deviceList) {
                mDeviceAdapter.mScanList = deviceList;
                mListView.setAdapter(mDeviceAdapter);
            }
        };
    }

    public void buttonScanClick(View view) {
        //switch (view.getId()) {
        //    case R.id.buttonScan:
        //    {
        Button btn = findViewById(R.id.buttonScan);
        if (!mScanning) {
            if (BleManager.getInstance().startScanBleDevice(this)) {
                mScanning = true;
                btn.setText(R.string.stop_scan);
            }
        } else {
            if (BleManager.getInstance().stopScanBleDevice(this)) {
                mScanning = false;
                btn.setText(R.string.start_scan);
            }
        }
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
}