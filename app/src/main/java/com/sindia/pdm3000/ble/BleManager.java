package com.sindia.pdm3000.ble;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import androidx.annotation.NonNull;

//import java.util.ArrayList;
import java.util.List;

// 低功耗蓝牙管理类（单例）
public class BleManager {
    // 单例接口
    private static BleManager instance = new BleManager();
    private BleManager(){}
    public static BleManager getInstance(){
        return instance;
    }

    // 检查并静默开启蓝牙
    public boolean checkBluetoothOpened(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);//这里与标准蓝牙略有不同
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        /*隐式打开蓝牙*/
        boolean opened = true;
        if (!bluetoothAdapter.isEnabled()) {
            opened = bluetoothAdapter.enable();
        }
        return opened;
    }

    // 扫描蓝牙设备
    public boolean scanBleDevice(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);//这里与标准蓝牙略有不同
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
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

    // 蓝牙扫描回调
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            // do nothing
            super.onScanResult(callbackType, result);
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
}
