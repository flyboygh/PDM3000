package com.sindia.pdm3000;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.sindia.pdm3000.adapter.BleDeviceAdapter;
import com.sindia.pdm3000.adapter.WifiAdapter;
import com.sindia.pdm3000.ble.BleManager;
import com.sindia.pdm3000.http.PdHttpRequest;
import com.sindia.pdm3000.util.ApplicationUtil;
import com.sindia.pdm3000.util.BluetoothUtil;
import com.sindia.pdm3000.util.LocationUtil;
import com.sindia.pdm3000.util.WifiAdmin;

import java.util.List;

public class SystemFragment extends Fragment implements BluetoothUtil.BluetoothStateCallback, BleDeviceAdapter.Callback, WifiAdapter.Callback { //AdapterView.OnItemClickListener,
    // 常量
    private static final String TAG = "SystemFragment";
    private static final int kTotalBleScanS = 10;
    // 定时器
    private static final int kMainTimerID = 101;
    private static final long kMainTimerDelay = 1000;
    private Handler mTimerHandler;
    // 状态相关的
    private boolean mHttpConnected = false; // 上次HTTP状态是否正常
    private boolean mForegndInit = false; // 是否已完成前台状态下的初始化
    private boolean mBleScanning = false; // 低功耗是否正在扫描
    private int mBleScanRemainS = 0; // 低功耗扫描剩余秒数
    // 导航上的控件
    private ImageButton mWifiImageBtn;
    private ImageButton mConnImageBtn;
    // 蓝牙相关的
    private Button mScanBleButton;
    private TextView mCountDownTextView;
    private ListView mDeviceListView = null;
    private BleManager mBleManager;
    private BleDeviceAdapter mDeviceAdapter;
    private BluetoothUtil _BluetoothUtil = null;
    // 无线网相关的
    private Button mOpenWifiButton;
    private ListView mWifiListView = null;
    private WifiAdapter mWifiAdapter;
    private WifiAdmin mWiFiAdmin;
    //private List<android.net.wifi.ScanResult> mWifiList;

    //private WifiManager mWifiManager = null;
    //private WifiInfo wifiInfo = null;       //获得的Wifi信息
    //private Handler handler;
    //private int level;
    //private String macAddress;

    // --------------------------------------- 系统事件 ----------------------------------------
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_system, container, false);
        //Context context = root.getContext();
        //FragmentActivity activity = getActivity();
        //return root;
/*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 导航和标题相关的
        setTitle(R.string.app_title);
*/
        //if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) { // 没有打开那个权限？
        //    Toast.makeText(this, "不支持低功耗", Toast.LENGTH_SHORT).show();
        //    finish();
        //}

        // 常用控件赋值区
        mWifiImageBtn = getActivity().findViewById(R.id.imageButtonWifiStatus);
        mConnImageBtn = getActivity().findViewById(R.id.imageButtonConnStatus);
        mScanBleButton = root.findViewById(R.id.buttonScanBle);
        mOpenWifiButton = root.findViewById(R.id.buttonOpenWifi);
        mCountDownTextView = root.findViewById(R.id.textViewCountDown);
        mDeviceListView = root.findViewById(R.id.listviewDevices);
        mWifiListView = root.findViewById(R.id.listviewWifis);

        // 设置控件事件
        mScanBleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanBleDevice();
            }
        });
        mOpenWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMoblleWifi();
            }
        });

        // 蓝牙工具类
        _BluetoothUtil = new BluetoothUtil();
        _BluetoothUtil.mBluetoothCallback = this;
        // 调用工具模块监听蓝牙状态
        _BluetoothUtil.registerBluetoothReceiver(root.getContext());
        Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());
        //_BluetoothUtil.colseBlueTooth();
        //Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());
        //_BluetoothUtil.openBlueTooth();
        //_BluetoothUtil.gotoSystem(this);
        //Log.i(TAG, "onCreate: BT State : "+ _BluetoothUtil.getBlueToothState());

        // 设备列表相关
        mDeviceAdapter = new BleDeviceAdapter(root.getContext(), this);
        //mDeviceListView.setOnItemClickListener(this);
        //mDeviceListView.setAdapter(mDeviceAdapter);

        // 设备扫描结果回调
        mBleManager = new BleManager();
        mBleManager.mBleScanCallback = new BleManager.BleScanCallback() {
            @Override
            public void onBleDeviceChanged(List<android.bluetooth.le.ScanResult> deviceList) {
                mDeviceAdapter.mScanList = deviceList;
                mDeviceListView.setAdapter(mDeviceAdapter);
            }
        };

        // 无线网相关的
        mWifiAdapter = new WifiAdapter(root.getContext(), this);
        //mWifiListView.setOnItemClickListener(this);
        //mWifiListView.setAdapter(mWifiAdapter);
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
*/
        mWiFiAdmin = new WifiAdmin(root.getContext());
        //mWiFiAdmin.openWifi(); // 觉得启动后提示开启无线网不太好，而且现在也有了手动开启的按钮

        // 判断wifi是否开启
        /*mWifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            if(mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                mWifiManager.setWifiEnabled(true);
            }
        }*
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
        }*
        /*
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
        };*/

        // 更新控件显赫
        updateActivityControls();

        // 创建主定时器并唤起
        mTimerHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == kMainTimerID) {
                    onMainTimerMessage();
                    removeMessages(msg.what); // 确保每秒执行一次
                    sendEmptyMessageDelayed(msg.what, kMainTimerDelay);
                }
            }
        };
        mTimerHandler.sendEmptyMessageDelayed(kMainTimerID, kMainTimerDelay);

        // 延迟执行的方法（已经挪到下面了）
        /*mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 程序启动后自动开始扫描蓝牙
                buttonScanClick(null);
            }
        }, 2000);*/

        /*String url = "https://hq.sinajs.cn/list=sh600028";
        boolean b = OkHttpHelper.requestHttp(url, new OkHttpHelper.OkHttpCallback() {
            @Override
            public void onHttpRespond(int code, String body) {
                Log.e(TAG, body);
            }
        });*/
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // --------------------------------------- 其它事件 ----------------------------------------
    // 主定时器消息
    private void onMainTimerMessage() {
        // 下面都是依赖于前台的
        if (ApplicationUtil.isForeground(getActivity())) {
            // 蓝牙相关的
            if (mBleScanning) {
                mBleScanRemainS--;
                if (mBleScanRemainS > 0) { // 尚未扫描完成,倒计时
                    mCountDownTextView.setText(getString(R.string.stop_afterS, mBleScanRemainS));
                } else { // 停止扫描
                    scanBleDevice();
                }
            }
            // 启动后做一次自动蓝牙扫描
            if (!mForegndInit) {
                mForegndInit = true;
                // 程序启动后自动开始扫描蓝牙
                scanBleDevice();
            }
            // 无线相关的
            if (mWiFiAdmin.checkScanWifis(getActivity())) {
                mWifiAdapter.updateWifiList(mWiFiAdmin.getWifiList());
                mWifiListView.setAdapter(mWifiAdapter);
                // 更新无线按钮状态
                updateActivityControls();
            }
            boolean postHeartBeat = false; // 是否强制post心跳
            boolean httpConnected = mWifiAdapter.hasConnectedSindiaWifi(); // 是否已连接了有效wifi
            if (mHttpConnected != httpConnected) { // WIFI连接发生了变化
                mHttpConnected = httpConnected;
                if (httpConnected) { // 刚刚连接
                    mWifiImageBtn.setBackgroundResource(R.drawable.icon_has_wifi);
                } else { // 刚刚无连接
                    mWifiImageBtn.setBackgroundResource(R.drawable.icon_no_wifi);
                }
                postHeartBeat = true;
            }
            if (postHeartBeat || (httpConnected && PdHttpRequest.shouldPostHeartBeat())) {
                // 定时发送http心跳
                PdHttpRequest.postHeartBeat(new PdHttpRequest.Callback() {
                    @Override
                    public void onResponse(PdHttpRequest.ResponseBase resp) {

                    }
                });
            }
            if (PdHttpRequest.isHttpConnected()) {
                mConnImageBtn.setBackgroundResource(R.drawable.icon_connected);
            } else {
                mConnImageBtn.setBackgroundResource(R.drawable.icon_disconnect);
            }
        }
    }

    // --------------------------------------- 回调重载 ----------------------------------------
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

    //@Override
    //public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    //}

    @Override
    public void onBluetoothOpened(boolean open) {
        if (open) {
            scanBleDevice();
        } else {
            updateActivityControls();
        }
    }

    // 【激活】蓝牙设备按钮点击
    @Override
    public void activateBleClick(BluetoothDevice device) {
        //ScanResult s_result = mDeviceAdapter.mScanList.get(index);
        if (mBleManager.connectBluetoothDevice(getActivity(), device)) {
        }
        //mDeviceAdapter.mConnDevice = device;
        mDeviceListView.setAdapter(mDeviceAdapter);
    }

    // 【连接】wifi按钮点击
    @Override
    public void connectWifiClick(String SSID) {
        //mWiFiAdmin.disconnWifiBySSID(SSID);
    }

    // --------------------------------------- 内部方法 ----------------------------------------
    // 根据功能状态，更新控件状态
    private void updateActivityControls() {
        // 设置蓝牙按钮状态
        boolean bluetoothEnable = _BluetoothUtil.getBlueToothState();
        if (bluetoothEnable) { // 蓝牙已开启
            if (mBleScanning) { // 正在扫描蓝牙
                mScanBleButton.setText(R.string.stop_scan);
            } else {
                mScanBleButton.setText(R.string.start_scan);
            }
            mScanBleButton.setTextColor(Color.BLACK);
            //btn.setBackgroundColor(Color.WHITE);
        } else { // 蓝牙未开启
            if (mBleScanning) { // 正在扫描蓝牙
                //buttonScanClick(null);
                if (mBleManager.stopScanBleDevice(getActivity())) {
                    mBleScanning = false;
                }
            }
            mScanBleButton.setText(R.string.open_ble);
            mScanBleButton.setTextColor(Color.RED);
            //btn.setBackgroundColor(Color.RED);
        }
        // 设置蓝牙倒计时文字
        if (mBleScanning) { // 正在蓝牙扫描
        } else { // 已停止扫描
            mCountDownTextView.setText("");
        }

        // 设置无线按钮状态
        if (mWiFiAdmin.isWifiEnabled()) { // WIFI已开启
            mOpenWifiButton.setVisibility(View.INVISIBLE);
        } else {
            mOpenWifiButton.setTextColor(Color.RED);
            mOpenWifiButton.setVisibility(View.VISIBLE);
        }
    }

    // 开启蓝牙/扫描/停止
    private void scanBleDevice() {
        if (!_BluetoothUtil.getBlueToothState()) { // 蓝牙未开启
            _BluetoothUtil.openBlueTooth(); // 开启蓝牙
            return;
        }

        if (!mBleScanning) { // 开启扫描
            // 请求定位权限，没有定位能力就尝试开启
            if (!LocationUtil.checkLocationPermission(getActivity())) {
                LocationUtil.requestLocationPermission(getActivity());
                return;
            }
            if (mBleManager.startScanBleDevice(getActivity())) {
                mBleScanning = true;
                mBleScanRemainS = kTotalBleScanS;
                //btn.setText(R.string.stop_scan);
            }
        } else { // 停止扫描
            if (mBleManager.stopScanBleDevice(getActivity())) {
                mBleScanning = false;
                //mBleScanRemainS = 0;
                //btn.setText(R.string.start_scan);
            }
        }
        updateActivityControls();
    }

    // 开启无线
    private void openMoblleWifi() {
        mWiFiAdmin.checkOpenWifi(getActivity());
        //mWiFiAdmin.openWifi();
    }

    //private void broadcastUpdate(final String action) {
    //    final Intent intent = new Intent(action);
    //    sendBroadcast(intent);
    //}
}
