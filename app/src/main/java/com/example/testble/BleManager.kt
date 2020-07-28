package com.example.testble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.*

class BleManager private constructor() {

    private val TAG: String = BleManager::class.java.simpleName
    private val UUID_SEVICE = "0000ffe0-0000-1000-8000-00805f9b34fb"
    private val UUID_CHARWRITE = "0000ffe1-0000-1000-8000-00805f9b34fb"
    lateinit private var mBluetoothAdapter: BluetoothAdapter

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

