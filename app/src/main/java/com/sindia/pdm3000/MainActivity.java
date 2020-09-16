package com.sindia.pdm3000;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.AdapterView;
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
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, BleDeviceAdapter.Callback, WifiAdapter.Callback {
    // 常量
    private static final String TAG = "MainActivity";
    // 状态相关的
    private boolean mScanning = false;
    // 蓝牙相关的
    private ListView mDeviceListView = null;
    private BleDeviceAdapter mDeviceAdapter;
    private BluetoothUtil _BluetoothUtil = null;
    private BluetoothGatt mBleGatt = null;
    private BluetoothStateBroadcastReceive mBluetoothReceive;
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
        mDeviceAdapter = new BleDeviceAdapter(this, this);
        mDeviceListView = findViewById(R.id.listviewDevices);
        mDeviceListView.setOnItemClickListener(this);
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
        mWifiListView.setOnItemClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    // 【激活】蓝牙设备按钮点击
    @Override
    public void activateBleClick(BluetoothDevice device) {
        //ScanResult s_result = mDeviceAdapter.mScanList.get(index);
        if (mBleGatt != null) { // 上次已连接
            // 先取得当前已连接设备的状态
            //int state = mBleGatt.getConnectionState(device); // 会崩溃
            //if (state == 1) {
            //}
            //if (mBleGatt.getDevice().equals(device)) { // 现在只有一个【激活】操作，所以先不用这个
            //    device = null;
            //}
            mBleGatt.disconnect(); // 这个会稍后触发onConnectionStateChange(state:1)
            mBleGatt = null;
            mDeviceAdapter.mConnDevice = null;
        }
        if (device != null) { // 现在这个一定成立
            // 连接蓝牙设备
            mBleGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
                @Override
                public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                    super.onPhyUpdate(gatt, txPhy, rxPhy, status);
                }

                @Override
                public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                    super.onPhyRead(gatt, txPhy, rxPhy, status);
                }

                //当连接状态发生改变的时候
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    // newState-2：连接成功；newState-0：连接断开
                    super.onConnectionStateChange(gatt, status, newState);
                    switch (status){

                        case BluetoothGatt.GATT_SUCCESS://0
                            break;
                        case BluetoothGatt.GATT_FAILURE://257
                            break;
                        case 133:
                        case 8:
                        case 22:
                            break;
                    }

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //intentAction = ACTION_GATT_CONNECTED;
                        //mConnectionState = STATE_CONNECTED;
                        //broadcastUpdate(intentAction);
                        //boolean b = mBleGatt.connect();看到网上有个例子调用了这个
                        Log.i(TAG, "Connected to GATT server.");
                        // Attempts to discover services after successful connection.
                        Log.i(TAG, "Attempting to start service discovery:" +
                                gatt.discoverServices());//连接成功，开始搜索服务，一定要调用此方法，否则获取不到服务

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        //intentAction = ACTION_GATT_DISCONNECTED;
                        //mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        //broadcastUpdate(intentAction);
                    }
                }
                //当服务被发现的时候回调的结果
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        return;
                    }
                    // 连接到设备之后获取设备的服务(Service)和服务对应的Characteristic
                    List<BluetoothGattService> services = gatt.getServices();
                    int serviceCount = services.size();
                    for (int service_i = 0; service_i < serviceCount; service_i++) {
                        BluetoothGattService bluetoothGattServer = services.get(service_i);
                        UUID serviceUuid = bluetoothGattServer.getUuid();
                        List<BluetoothGattCharacteristic> characteristics = bluetoothGattServer.getCharacteristics();
                        int charactCount = characteristics.size();
                        for (int charact_i = 0; charact_i < charactCount; charact_i++) {
                            BluetoothGattCharacteristic characteristic = characteristics.get(charact_i);
                            UUID charactUuid = characteristic.getUuid();
                            // 获取到特征之后，找到服务中可以向下位机写指令的特征，向该特征写入指令。
                            // Check characteristic property
                            final int properties = characteristic.getProperties();
                            //if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0) {
                            if (properties == (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) { // 必须要这么写，不能写成上面那个，否则第二次写入会失败
                                // PROPERTY_WRITE | PROPERTY_READ成功
                                // PROPERTY_WRITE | PROPERTY_WRITE_NO_RESPONSE失败
                                //gatt.setCharacteristicNotification(characteristic, true); // 加这个有用么
                                String msg = "AT+LOWL:1";
                                characteristic.setValue(msg.getBytes());
                                //characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                                if (gatt.writeCharacteristic(characteristic)) {
                                    Log.i(TAG, "Write Characteristic success.");
                                } else {
                                    Log.e(TAG, "Write Characteristic failed.");
                                }
                            }
                        }
                    }
                }
                //回调响应特征读操作的结果
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                }
                //回调响应特征写操作的结果
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {

                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                }
                // 当连接能被被读的操作
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