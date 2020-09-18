package com.sindia.pdm3000.ble;

import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 低功耗蓝牙管理类（单例）
public class BleManager {
    // 单例接口
    private static BleManager instance = new BleManager();
    private BleManager(){}
    public static BleManager getInstance(){
        return instance;
    }

    // 常量
    private static final String TAG = "BleManager";

    // =================================== 下面都是蓝牙连接相关的 ==================================
    private BluetoothGatt mConnBleGatt = null; // 当前连接的低功耗蓝牙协议，类似于socket，只是低功耗蓝牙用的是gatt协议

    public boolean connectBluetoothDevice(Context context, BluetoothDevice device) {
        disconnectBluetoothDevice();
        if (device != null) { // 现在这个一定成立
            // 连接蓝牙设备
            mConnBleGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
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
        return true;
    }

    // 断开与当前蓝牙设备的连接
    public boolean disconnectBluetoothDevice() {
        if (mConnBleGatt != null) { // 上次已连接
            // 先取得当前已连接设备的状态
            //int state = mBleGatt.getConnectionState(device); // 会崩溃
            //if (state == 1) {
            //}
            //if (mBleGatt.getDevice().equals(device)) { // 现在只有一个【激活】操作，所以先不用这个
            //    device = null;
            //}
            mConnBleGatt.disconnect(); // 这个会稍后触发onConnectionStateChange(state:1)
            mConnBleGatt = null;
            //mDeviceAdapter.mConnDevice = null;
            return true;
        }
        return false;
    }

    // =================================== 下面都是蓝牙扫描相关的 ==================================

    public interface BleScanCallback {
        void onBleDeviceChanged(List<ScanResult> deviceList);
    }

    public BleScanCallback mBleScanCallback;
    private List<ScanResult> mScanResultList = new ArrayList<>();

    // 开始扫描蓝牙设备
    public boolean startScanBleDevice(Context context) {
        disconnectBluetoothDevice();

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
        mScanResultList.clear();
        mBleScanCallback.onBleDeviceChanged(mScanResultList);
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
        //mScanResultList.clear();
        //mBleScanCallback.onBleDeviceChanged(mScanResultList);
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
