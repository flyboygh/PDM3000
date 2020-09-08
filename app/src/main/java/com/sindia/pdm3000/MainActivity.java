package com.sindia.pdm3000;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

import com.sindia.pdm3000.ble.IBleDeviceScan;
import com.sindia.pdm3000.ble.BleManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
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

        BleManager.getInstance().bleDeviceScan = new IBleDeviceScan() {
            @Override
            public void onBleDeviceChanged(ArrayList<ScanResult> deviceList) {

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
}