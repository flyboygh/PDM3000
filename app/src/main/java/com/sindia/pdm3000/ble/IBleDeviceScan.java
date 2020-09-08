package com.sindia.pdm3000.ble;

import android.bluetooth.le.ScanResult;

import java.util.ArrayList;

public interface IBleDeviceScan {
    void onBleDeviceChanged(ArrayList<ScanResult> deviceList);
}
