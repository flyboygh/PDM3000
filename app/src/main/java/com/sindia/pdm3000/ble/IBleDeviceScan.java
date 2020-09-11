package com.sindia.pdm3000.ble;

import android.bluetooth.le.ScanResult;

import java.util.List;

public interface IBleDeviceScan {
    void onBleDeviceChanged(List<ScanResult> deviceList);
}
