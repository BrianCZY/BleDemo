package com.example.testble

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.*

class BleManager private constructor() {


    private var mCharacteristic: BluetoothGattCharacteristic? = null
    private var mBleDevice: BluetoothDeviceBean? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private val TAG: String = BleManager::class.java.simpleName
    private val UUID_SEVICE = "0000ffe0-0000-1000-8000-00805f9b34fb"
    private val UUID_CHARWRITE = "0000ffe1-0000-1000-8000-00805f9b34fb"
    lateinit private var mBluetoothAdapter: BluetoothAdapter
    var sIsBleConnect = false //是否连接状态


    companion object {


        val instance = SingletonHolder.singleton

    }


    private object SingletonHolder {
        val singleton = BleManager()
    }

    var mOnBleStateListener: OnBleStateListener? = null


    var scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            val temp = BluetoothDeviceBean(result.device.name, result.device.address)

            Log.d(TAG, "ScanCallback  onScanResult 搜索到蓝牙设备 --------")
            Log.d(
                TAG,
                "device.name = ${result.device.name}  device.address = ${result.device.address}"
            )
            mOnBleStateListener?.onSearchBleSuccess(temp)

        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    fun startScanBLE() {
        Log.d(TAG, "startScanBLE")
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        //还可以按设备名称，MAC地址等条件过滤
        val scanFilters: MutableList<ScanFilter> =
            ArrayList()
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(UUID_SEVICE))
            .build()
        scanFilters.add(filter)
        mBluetoothAdapter?.getBluetoothLeScanner()
            .startScan(scanFilters, scanSettings, scanCallback)
    }

    fun stopScanBLE() {
        Log.d(TAG, "stopScanBLE")
        try {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun iniBle(context: Context) {
        //方法二
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;
        mBluetoothAdapter = bluetoothManager.adapter

//        if (!mBluetoothAdapter.isEnabled() && !mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(
//                BluetoothAdapter.ACTION_REQUEST_ENABLE
//            );
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }

    }

    fun connectDevice(context: Context, bleDevice: BluetoothDeviceBean) {
        mBleDevice = bleDevice
        //发起连接
//            private void connect(BluetoothDevice device){
//                mBluetoothGatt = device.connectGatt(context, false, mBluetoothGattCallback);
//            }
        val localBluetoothDevice: BluetoothDevice =
            mBluetoothAdapter.getRemoteDevice(bleDevice.address)

        mBluetoothGatt = localBluetoothDevice?.connectGatt(context, false, mBluetoothGattCallback)

    }


    private val mBluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.d(TAG, "BluetoothGattCallback  onCharacteristicRead  --------")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d(TAG, "BluetoothGattCallback  onCharacteristicWrite  --------")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d(TAG, "BluetoothGattCallback  onServicesDiscovered  --------")
            //获取特定UUID的服务
            gatt?.run {

                //获取特定UUID的服务
                val service: BluetoothGattService =
                    getService(UUID.fromString(UUID_SEVICE))

                if (service != null) {
                    //获取该服务下特定UUID的特征
                    mCharacteristic =
                        service.getCharacteristic(UUID.fromString(UUID_CHARWRITE))
                }
                //开启对这个特征的通知
                this.setCharacteristicNotification(mCharacteristic, true)

            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.d(TAG, "BluetoothGattCallback  onDescriptorWrite  --------")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d(TAG, "BluetoothGattCallback  onCharacteristicChanged  --------")
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
            Log.d(TAG, "BluetoothGattCallback  onDescriptorRead  --------")
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d(TAG, "BluetoothGattCallback  onConnectionStateChange  --------")
            when (newState) {
                BluetoothProfile.STATE_DISCONNECTED -> { //断开连接
                    sIsBleConnect = false
                    mBluetoothGatt?.disconnect()
                    mBluetoothGatt?.close()
                    mBluetoothGatt = null
                    mOnBleStateListener?.onBleDisconnect()
                    Log.d(TAG, "BluetoothGattCallback  newState == 0 设备 断开连接  --------")
                }
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "BluetoothGattCallback  newState == 2 设备 已连接  --------")
                    sIsBleConnect = true
                    mBleDevice?.run {
                        mOnBleStateListener?.onBleConnect(
                            address,
                            name
                        )
                    }
                    gatt?.discoverServices() //发现服务


                }
                else -> {
//                    gatt?.disconnect()
//                    gatt?.close()
                }
            }
        }
    }

    fun write(str: String) {
        mCharacteristic?.run {
            var byteArray: ByteArray = byteArrayOf()
            this.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            this.value = byteArray
            mBluetoothGatt?.writeCharacteristic(this)
        }

    }

    interface OnBleStateListener {
        fun onSearchBleSuccess(bleDevice: BluetoothDeviceBean?)
        fun onBleConnect(
            deviceAddress: String?,
            deviceName: String?
        )

        fun onBleDisconnect()
        fun onReceiveBleMsg(msg: String?)
    }

}

