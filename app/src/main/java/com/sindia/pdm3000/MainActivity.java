package com.sindia.pdm3000;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sindia.pdm3000.adapter.BleDeviceAdapter;
import com.sindia.pdm3000.adapter.WifiAdapter;
import com.sindia.pdm3000.ble.BleManager;
import com.sindia.pdm3000.util.BluetoothUtil;
import com.sindia.pdm3000.util.LocationUtil;
import com.sindia.pdm3000.util.WifiAdmin;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements BluetoothUtil.BluetoothStateCallback, BleDeviceAdapter.Callback, WifiAdapter.Callback { //AdapterView.OnItemClickListener,
    // 常量
    private static final String TAG = "MainActivity";
    // 状态相关的
    private boolean mScanning = false;
    // 蓝牙相关的
    private ListView mDeviceListView = null;
    private BleDeviceAdapter mDeviceAdapter;
    private BluetoothUtil _BluetoothUtil = null;
    // 无线网相关的
    private ListView mWifiListView = null;
    private WifiAdapter mWifiAdapter;
    private WifiAdmin wiFiAdmin;
    private List<android.net.wifi.ScanResult> mWifiList;

    //private WifiManager mWifiManager = null;
    private WifiInfo wifiInfo = null;       //获得的Wifi信息
    private Handler handler;
    private int level;
    private String macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //禁止旋转（在xml写了）
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { // 没有打开那个权限？
        //    Toast.makeText(this, "不支持低功耗", Toast.LENGTH_SHORT).show();
        //    finish();
        //}

        // 启动后如果没有定位能力就尝试开启
        if (!LocationUtil.checkLocationPermission(this)) {
            LocationUtil.requestLocationPermission(this);
        }

        // 蓝牙工具类
        _BluetoothUtil = new BluetoothUtil();
        _BluetoothUtil.mBluetoothCallback = this;
        // 调用工具模块监听蓝牙状态
        _BluetoothUtil.registerBluetoothReceiver(this);
        Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());
        //_BluetoothUtil.colseBlueTooth();
        //Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());
        _BluetoothUtil.openBlueTooth();
        //_BluetoothUtil.gotoSystem(this);
        //Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());

        // 设备列表相关
        mDeviceAdapter = new BleDeviceAdapter(this, this);
        mDeviceListView = findViewById(R.id.listviewDevices);
        //mDeviceListView.setOnItemClickListener(this);
        mDeviceListView.setAdapter(mDeviceAdapter);

        // 设备扫描结果回调
        BleManager.getInstance().mBleScanCallback = new BleManager.BleScanCallback() {
            @Override
            public void onBleDeviceChanged(List<android.bluetooth.le.ScanResult> deviceList) {
                mDeviceAdapter.mScanList = deviceList;
                mDeviceListView.setAdapter(mDeviceAdapter);
            }
        };

        // 无线网相关的
        mWifiAdapter = new WifiAdapter(this, this);
        mWifiListView = findViewById(R.id.listviewWifis);
        //mWifiListView.setOnItemClickListener(this);
        mWifiListView.setAdapter(mWifiAdapter);
/*
        //Check for permissions
        int n1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int n2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        if ((n1 != PackageManager.PERMISSION_GRANTED) || (n2 != PackageManager.PERMISSION_GRANTED))
        {
            Log.d(TAG, "Requesting permissions");

            //Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.ACCESS_NETWORK_STATE},
                    123);
        }
        else
            Log.d(TAG, "Permissions already granted");
        int n = wiFiAdmin.checkState();
*/
        wiFiAdmin = new WifiAdmin(this);
        wiFiAdmin.openWifi();

        // 判断wifi是否开启
        /*mWifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            if(mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                mWifiManager.setWifiEnabled(true);
            }
        }*/
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //showToast("自Android 6.0开始需要打开位置权限");
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }*/
// 使用定时器,每隔5秒获得一次信号强度值
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                wiFiAdmin.startScan();
                mWifiList = wiFiAdmin.getWifiList();

                List<android.net.wifi.ScanResult> listb = mWifiList;//mWifiManager.getScanResults();
                Log.d("wifi","listb"+listb);
                Log.d("wifi","listb.size()"+listb.size());
                //数组初始化要注意
                String[] listSSID = new String[listb.size()];
                String[] listBSSID = new String[listb.size()];
                int[] listLevel = new int[listb.size()];
                if (listb != null) {
                    for (int i = 0; i < listb.size(); i++) {
                        android.net.wifi.ScanResult scanResult = listb.get(i);
                        listSSID[i] = scanResult.SSID;
                        listBSSID[i] = scanResult.BSSID;
                        listLevel[i] = scanResult.level;
                    }
                }
                String[] listSSID0 = new String[listb.size()];
                String[] listBSSID0 = new String[listb.size()];
                int[] listLevel0 = new int[listb.size()];
                if (listb == null) {
                    listSSID0[0] = "NoWiFi";
                    listBSSID0[0] = "NoWiFi";
                    listLevel[0] = -200; //-200默认没有wifi
                } else {
                    listSSID0 = listSSID;
                    listBSSID0 = listBSSID;
                    listLevel0 = listLevel;
                }
                for (int i = 0; i < listb.size(); i++) {
                    Log.d("wifi", listSSID0[i] + "//" + listBSSID0[i] + "//" + listLevel0[i]);
                }
                Log.d("wifi", "=======================");
                wifiInfo = WifiAdmin.getConnectWifiInfo(MainActivity.this);// wiFiAdmin.getWifiManager().getConnectionInfo();//  mWifiManager.getConnectionInfo();
                //获得信号强度值
                level = wifiInfo.getRssi();
                macAddress = wifiInfo.getBSSID();
                Message msg = new Message();
                handler.sendMessage(msg);
            }

        }, 1000, 5000);
        // 使用Handler实现UI线程与Timer线程之间的信息传递,每5秒告诉UI线程获得wifiInto
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(MainActivity.this,
                        "信号强度：" + level + "  BSSID: " + macAddress, Toast.LENGTH_SHORT)
                        .show();
                mWifiAdapter.mScanList = mWifiList;
                mWifiListView.setAdapter(mWifiAdapter);
            }
        };

        // 更新控件显赫
        UpdateActivityControls();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
            }
        } else {*/
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //}
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

    //@Override
    //public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    //}

    @Override
    public void onBluetoothOpened(boolean open) {
        UpdateActivityControls();
    }

    // 【激活】蓝牙设备按钮点击
    @Override
    public void activateBleClick(BluetoothDevice device) {
        //ScanResult s_result = mDeviceAdapter.mScanList.get(index);
        if (BleManager.getInstance().connectBluetoothDevice(this, device)) {
        }
        mDeviceAdapter.mConnDevice = device;
        mDeviceListView.setAdapter(mDeviceAdapter);
    }

    // 【连接】wifi按钮点击
    @Override
    public void connectWifiClick(int index) {

    }

    //private void broadcastUpdate(final String action) {
    //    final Intent intent = new Intent(action);
    //    sendBroadcast(intent);
    //}
}