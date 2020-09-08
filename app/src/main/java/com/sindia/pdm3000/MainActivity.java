package com.sindia.pdm3000;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;

import com.sindia.pdm3000.ble.BleManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //请求权限
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION},
                0);

        // 检查并开启蓝牙
        BleManager.getInstance().checkBluetoothOpened(this);

        BleManager.getInstance().scanBleDevice(this);
    }
}