package com.sindia.pdm3000.ble;

import android.bluetooth.BluetoothAdapter;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

// 低功耗蓝牙管理类（单例）
public class BleManager {
    // 单例接口
    private static BleManager instance = new BleManager();
    private BleManager(){}
    public static BleManager getInstance(){
        return instance;
    }

    // =================================== 下面都是蓝牙扫描相关的 ==================================

    public interface BleScanCallback {
        void onBleDeviceChanged(List<ScanResult> deviceList);
    }

    public BleScanCallback mBleScanCallback;
    private List<ScanResult> mScanResultList = new ArrayList<>();

    // 开始扫描蓝牙设备
    public boolean startScanBleDevice(Context context) {
        //BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);//这里与标准蓝牙略有不同
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// bluetoothManager.getAdapter();
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(scanCallback);
/*
        final BluetoothLeScanner scanner2 = bluetoothAdapter.getBluetoothLeScanner();//BluetoothLeScanner.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(uuid).build());
        scanner.startScan(filters, settings, scanCallback);
*/
        return true;
    }

    // 停止扫描蓝牙设备
    public boolean stopScanBleDevice(Context context) {
        //BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);//这里与标准蓝牙略有不同
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// bluetoothManager.getAdapter();
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner != null) { // 当从设置里关闭蓝牙，会到这里
            scanner.stopScan(scanCallback);
        }
        mScanResultList.clear();
        mBleScanCallback.onBleDeviceChanged(mScanResultList);
        return true;
    }

    // 蓝牙扫描回调
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            // do nothing
            super.onScanResult(callbackType, result);
            try {
                if (result.getScanRecord() == null) {
                    return;
                }
                String deviceName = result.getScanRecord().getDeviceName();
                if (deviceName != null && deviceName.length() > 0) { // 有名设备
                    int i = mScanResultList.size() - 1;
                    for (; i >= 0; i--) {
                        ScanResult s_result = mScanResultList.get(i);
                        if (s_result.getScanRecord() == null) {
                            continue;
                        }
                        String dn = s_result.getScanRecord().getDeviceName();
                        if (dn != null && dn.equals(deviceName)) {
                            break;
                        }
                    }
                    if (i < 0) {
                        mScanResultList.add(result);
                        mBleScanCallback.onBleDeviceChanged(mScanResultList);
                    }
                }
            } catch (java.lang.NullPointerException e) {
                Log.e("onScanResult", "String is null");
            }
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            super.onBatchScanResults(results);
            //adapter.update(results);
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
            super.onScanFailed(errorCode);
        }
    };

    // 检查并静默开启蓝牙（已经使用BluetoothUtil里的方法了）
    /*public boolean checkBluetoothOpened(Context context) {
        //BluetoothManag bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);//这里与标准蓝牙略有不同
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// bluetoothManager.getAdapter();
        // 隐式打开蓝牙
        boolean opened = true;
        if (!bluetoothAdapter.isEnabled()) {
            opened = bluetoothAdapter.enable();
        }
        return opened;
    }*/
}
