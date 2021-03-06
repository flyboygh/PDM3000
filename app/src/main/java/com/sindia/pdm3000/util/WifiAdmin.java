package com.sindia.pdm3000.util;//com.tchip.carlauncher.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.util.Log;

public class WifiAdmin {
    /**
     * 定义一个WifiManager对象,提供Wifi管理的各种主要API，主要包含wifi的扫描、建立连接、配置信息等
     */
    private WifiManager mWifiManager;
    // WIFIConfiguration描述WIFI的链接信息，包括SSID、SSID隐藏、password等的设置
    private List<WifiConfiguration> wifiConfigList;
    // 定义一个WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfigurations;
    WifiLock mWifiLock;
    public static final int TYPE_NO_PASSWD = 0x11;
    public static final int TYPE_WEP = 0x12;
    public static final int TYPE_WPA = 0x13;
    public WifiAdmin(Context context) {
        // 获得WifiManager对象
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }
    /**
     * 打开wifi
     */
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }
    /**
     * 关闭wifi
     */
    public void closeWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }
    /**
     * 检查当前wifi状态
     *
     * @return
     */
    public int checkState() {
        return mWifiManager.getWifiState();
    }
    /**
     * 锁定wifiLock
     */
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }
    /**
     * 解锁wifiLock
     */
    public void releaseWifiLock() {
        // 判断是否锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }
    /**
     * 创建一个wifiLock
     */
    public void createWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("test");
    }
    /**
     * 得到配置好的网络
     *
     * @return
     */
    public List<WifiConfiguration> getWiFiConfiguration() {
        return mWifiConfigurations;
    }
    /**
     * 指定配置好的网络进行连接
     *
     * @param index
     */
    public void connetionConfiguration(int index) {
        if (index > mWifiConfigurations.size()) {
            return;
        }
        // 连接配置好指定ID的网络
        mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId,
                true);
    }
    public void startScan() {
        // 此处必须扫描，否则手机上不会有实时列表
        mWifiManager.startScan();
        // 得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        // 得到配置好的网络连接
        mWifiConfigurations = mWifiManager.getConfiguredNetworks();
    }
    /**
     * 得到网络列表
     *
     * @return
     */
    public List<ScanResult> getWifiList() {
        return mWifiList;
    }
    /**
     * 查看扫描结果
     *
     * @return
     */
    public StringBuffer lookUpScan() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mWifiList.size(); i++) {
            sb.append("Index_" + new Integer(i + 1).toString() + ":");
            // 将ScanResult信息转换成一个字符串包
            // 其中把包括：BSSID、SSID、capabilities、frequency、level
            sb.append((mWifiList.get(i)).toString()).append("\n");
        }
        return sb;
    }
    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }
    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }
    public int getIpAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }
    /**
     * 得到连接的ID
     *
     * @return
     */
    public int getNetWordId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }
    /**
     * 得到wifiInfo的所有信息
     *
     * @return
     */
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }
    /**
     * 添加一个网络并连接
     *
     * @param configuration
     */
    public void addNetwork(WifiConfiguration configuration) {
        int wcgId = mWifiManager.addNetwork(configuration);
        mWifiManager.enableNetwork(wcgId, true);
    }
    public WifiConfiguration createWifiInfo(String SSID, String Password,
                                            int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        WifiConfiguration tempConfig = this.isExsits(SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        if (Type == 1) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }
    private WifiConfiguration isExsits(String ssid) {
        WifiConfiguration config = new WifiConfiguration();
        if (mWifiList.size() > 0) {
            for (int i = 0; i < mWifiList.size(); i++) {
                if (mWifiList.get(i).SSID.equals(ssid)) {
                    config = mWifiConfigurations.get(i);
                }
            }
        }
        return config;
    }
    /**
     * 断开指定ID的网络
     *
     * @param netId
     */
    public void disConnectionWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }
    // ============
    /**
     * 连接指定Id的WIFI
     *
     * @param wifiId
     * @return
     */
    public boolean ConnectWifi(int wifiId) {
        for (int i = 0; i < wifiConfigList.size(); i++) {
            WifiConfiguration wifi = wifiConfigList.get(i);
            if (wifi.networkId == wifiId) {
                while (!(mWifiManager.enableNetwork(wifiId, true))) {// 激活该Id，建立连接
                    Log.i("ConnectWifi",
                            String.valueOf(wifiConfigList.get(wifiId).status));// status:0--已经连接，1--不可连接，2--可以连接
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 得到Wifi配置好的信息
     */
    public void getConfiguration() {
        wifiConfigList = mWifiManager.getConfiguredNetworks();// 得到配置好的网络信息
        for (int i = 0; i < wifiConfigList.size(); i++) {
            Log.i("getConfiguration", wifiConfigList.get(i).SSID);
            Log.i("getConfiguration",
                    String.valueOf(wifiConfigList.get(i).networkId));
        }
    }
    /**
     * 判定指定WIFI是否已经配置好,依据WIFI的地址BSSID,返回NetId
     *
     * @param SSID
     * @return
     */
    public int IsConfiguration(String SSID) {
        Log.i("IsConfiguration", String.valueOf(wifiConfigList.size()));
        for (int i = 0; i < wifiConfigList.size(); i++) {
            Log.i(wifiConfigList.get(i).SSID,
                    String.valueOf(wifiConfigList.get(i).networkId));
            if (wifiConfigList.get(i).SSID.equals(SSID)) {// 地址相同
                return wifiConfigList.get(i).networkId;
            }
        }
        return -1;
    }
    /**
     * 添加指定WIFI的配置信息,原列表不存在此SSID
     *
     * @param wifiList
     * @param ssid
     * @param pwd
     * @return
     */
    public int AddWifiConfig(List<ScanResult> wifiList, String ssid, String pwd) {
        int wifiId = -1;
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult wifi = wifiList.get(i);
            if (wifi.SSID.equals(ssid)) {
                Log.i("AddWifiConfig", "equals");
                WifiConfiguration wifiCong = new WifiConfiguration();
                wifiCong.SSID = "\"" + wifi.SSID + "\"";// \"转义字符，代表"
                wifiCong.preSharedKey = "\"" + pwd + "\"";// WPA-PSK密码
                wifiCong.hiddenSSID = false;
                wifiCong.status = WifiConfiguration.Status.ENABLED;
                wifiId = mWifiManager.addNetwork(wifiCong);// 将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
                if (wifiId != -1) {
                    return wifiId;
                }
            }
        }
        return wifiId;
    }

    //////////////////////////////// 下面是自定义的 ////////////////////////////////
/*
    // 单例接口
    private static WifiAdmin instance = new WifiAdmin();
    private WifiAdmin(){}
    public static WifiAdmin getInstance(){
        return instance;
    }
*/
    // 一些静态接口方法
    /*public static WifiInfo getConnectWifiInfo(Context context) {
        // 获得WifiManager对象
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getBSSID() == null) {
            return null;
        }
        return wifiInfo;
    }*/

    // 取得当前已连接的SSID
    public String getConnectedSSID(Context context) {
        // 取得WifiInfo对象
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getSSID() == null) {
            return "";
        }
        String ssid = wifiInfo.getSSID();
        if (ssid.length() >= 2 && ssid.charAt(0) == '"' && ssid.charAt(ssid.length() - 1) == '"') {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    // 取得当前已连接的BSSID
    public String getConnectedBSSID(Context context) {
        // 取得WifiInfo对象
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getBSSID() == null) {
            return "";
        }
        return wifiInfo.getBSSID();
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public void checkOpenWifi(Context context) {
        if (!mWifiManager.isWifiEnabled()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // 29
                mWifiManager.setWifiEnabled(true);
            } else {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);//ACTION_WIRELESS_SETTINGS是系统的设置WIFI页面
                context.startActivity(intent);
            }
        }
    }

        // 扫描和回调
    //public interface WifiScanCallback {
    //}
    //public WifiScanCallback mWifiScanCallback = null;

    // 提返回的一些状态
    //private boolean mWifiChanged = false;
    //private WifiInfo mLastConnWifi = null;

    // 上次无线是否开启
    private boolean mPrevWifiEnabled = false;
    private String mPrevConnectBSSID = "";

    // 扫描无线，返回和上次的比是否发生了变化
    public boolean checkScanWifis(Context context) {
        // 记录上次的信息
        int oldWifiCount = 0;
        final List<ScanResult> oldWifiList = mWifiList;
        if (oldWifiList != null) {
            oldWifiCount = oldWifiList.size();
        }

        // 扫描WIFI
        startScan();

        // 对结果做处理
        /*List<ScanResult> list = new ArrayList<>();
        for (int i = 0; i < mWifiList.size(); i++) {
            ScanResult s_result = mWifiList.get(i);
            if (s_result.BSSID != null) {
                list.add(s_result);
            }
        }*/

        // 取得当前连接wifi信息
        String connectBSSID = getConnectedBSSID(context);

        // 当前WIFI是否可用
        boolean wifiEnabled = mWifiManager.isWifiEnabled();
        if (wifiEnabled) {
        } else {
            connectBSSID = "";
            mWifiList.clear();
        }

        // 比较无线网状态是否相同
        if (wifiEnabled == mPrevWifiEnabled) {
            // 比较连接是否相同
            if (connectBSSID.equals(mPrevConnectBSSID)) {
                // 和上次的结果比较
                int wifiCount = ( mWifiList == null ? 0 : mWifiList.size() );
                if (wifiCount == oldWifiCount) { // 数量相同
                    // 比较顺序
                    int i = 0, count = mWifiList.size();
                    for (i = 0; i < count; i++) {
                        ScanResult sr1 = mWifiList.get(i);
                        ScanResult sr2 = oldWifiList.get(i);
                        if (!sr1.BSSID.equals(sr2.BSSID)) {
                            break;
                        }
                    }
                    if (i == count) { // 顺序也相同
                        return false;
                    }
                }
            }
        }
        mPrevConnectBSSID = connectBSSID;
        mPrevWifiEnabled = wifiEnabled;
        return true;
        /*
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
         */
    }

    private WifiConfiguration getConfigBySSID(String SSID) {
        List<WifiConfiguration> configList = mWifiManager.getConfiguredNetworks();// 得到配置好的网络信息
        for (int i = 0; i < configList.size(); i++) {
            WifiConfiguration wifiConf = configList.get(i);
            String s = wifiConf.SSID;
            if (s.length() > 0 && s.charAt(0) == '"') {
                s = s.substring(1);
            }
            if (s.length() > 0 && s.charAt(s.length()-1) == '"') {
                s = s.substring(0, s.length() - 1);
            }
            if (s.equals(SSID)) {
                return wifiConf;
            }
        }
        return null;
    }

    // 连接指定WIFI
    public boolean connectWifiBySSID(Context context, String SSID, String PSWD) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // 29
            mWifiManager.disconnect(); // 加这一句就能断开当前连接
            int networkId = -1;
            WifiConfiguration wifiConf = getConfigBySSID(SSID);
            if (wifiConf != null) { // 已在缓存中的WIFI
                networkId = wifiConf.networkId;
            } else { // 缓存中没有，需要新建配置
                WifiConfiguration wifiCong = new WifiConfiguration();
                wifiCong.SSID = "\"" + SSID + "\"";// \"转义字符，代表"
                wifiCong.preSharedKey = "\"" + PSWD + "\"";// WPA-PSK密码
                wifiCong.hiddenSSID = false;
                wifiCong.status = WifiConfiguration.Status.ENABLED;
                networkId = mWifiManager.addNetwork(wifiCong);// 将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
            }
            if (networkId != -1) {
                boolean b = mWifiManager.enableNetwork(networkId, true);
                if (b) {
                    //b = mWifiManager.reconnect(); // 感觉没什么用
                }
                return b;
            }
        } else {
            NetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                            .setSsidPattern(new PatternMatcher(SSID, PatternMatcher.PATTERN_PREFIX))
                            .setWpa2Passphrase(PSWD)//WiFi密码
                            .build();

            NetworkRequest request = new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            //.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)//网络不受限
                            //.addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)//信任网络，增加这个连个参数让设备连接wifi之后还联网。
                            .setNetworkSpecifier(specifier)
                            .build();

            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //Network[] list = connectivityManager.getAllNetworks();
            //int n = list.length;

            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    // do success processing here..
                    Log.d("", "wifi available");
                }

                @Override
                public void onUnavailable() {
                    // do failure processing here..
                    Log.d("", "wifi unavailable");
                }
            };
            connectivityManager.requestNetwork(request, networkCallback);
            // Release the request when done.
            // connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        return false;
    }

    // 断开与WIFI的连接
    public boolean disconnWifiBySSID(Context context, String SSID) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // 29
            WifiConfiguration wifiConf = getConfigBySSID(SSID);
            if (wifiConf != null) {
                int networkId = wifiConf.networkId;
                //mWifiManager.disableNetwork(networkId); // 这个在android9上会弹一次提示
                mWifiManager.disconnect(); // 这个在android9上会弹一次提示 // 加这一句就能断开当前连接
                //disConnectionWifi(networkId);
                return true;
            }
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = connectivityManager.getActiveNetwork();
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = networkInfo.isConnected();
            int networkType = networkInfo.getType();
            if (networkType == ConnectivityManager.TYPE_WIFI) {
            } else if (networkType == ConnectivityManager.TYPE_MOBILE) {
            }
            // 下面都不好用
            /*WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                mWifiManager.disableNetwork(wifiInfo.getNetworkId());
            }
            mWifiManager.disconnect(); // 这个在android9上会弹一次提示 // 加这一句就能断开当前连接
             */
        }
        return false;
    }
}
