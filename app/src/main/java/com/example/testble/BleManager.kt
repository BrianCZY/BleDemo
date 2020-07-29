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
import kotlin.experimental.and


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

    private var sIsSendNext = true  //一个数据包是否发送完成

    private val mBluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.d(TAG, "BluetoothGattCallback  onCharacteristicRead  --------")
            val arrayOfByte: ByteArray? = characteristic?.getValue()
            val tmpstr: String? = byteArrayToStr(arrayOfByte)
            Log.d(TAG, "BluetoothGattCallback onCharacteristicRead 蓝牙  接收到原始数据:$tmpstr")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            sIsSendNext = true
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
                mBluetoothGatt?.setCharacteristicNotification(mCharacteristic, true)
                //第二步，通过对手机B(远程)中需要开启通知的那个特征的CCCD写入开启通知命令，来打开通知

                //第二步，通过对手机B(远程)中需要开启通知的那个特征的CCCD写入开启通知命令，来打开通知
                val descriptor: BluetoothGattDescriptor? = mCharacteristic?.getDescriptor(
                    UUID.fromString(
                        UUID_CHARWRITE
                    )
                )

                //设置特征的写入类型为默认类型
//                mCharacteristic?.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
//                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                mBluetoothGatt?.writeDescriptor(descriptor)

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
            Log.d(TAG, "BluetoothGattCallback  onCharacteristicChanged  --------")
            super.onCharacteristicChanged(gatt, characteristic)
            val arrayOfByte: ByteArray? = characteristic?.getValue()
            val tmpstr: String? = byteArrayToStr(arrayOfByte)
            Log.d(TAG, "BluetoothGattCallback  onCharacteristicChanged  蓝牙  接收到原始数据:$tmpstr")
//            val tmp: String? = byteArrayToStr(arrayOfByte)
//            val tmp: String? = byteArrayToHexStr(arrayOfByte)
//            Log.d(TAG, "BluetoothGattCallback  onCharacteristicChanged  蓝牙  接收到数据:$tmp")
            mOnBleStateListener?.onReceiveBleMsg(tmpstr)


        }


        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            Log.d(TAG, "BluetoothGattCallback  onDescriptorRead  --------")
            val arrayOfByte: ByteArray? = descriptor?.getValue()
            val tmpstr: String? = byteArrayToStr(arrayOfByte)
            Log.d(TAG, "BluetoothGattCallback 蓝牙  接收到原始数据:$tmpstr")
//            super.onDescriptorRead(gatt, descriptor, status)

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
                    mBluetoothGatt?.discoverServices() //发现服务


                }
                else -> {
//                    gatt?.disconnect()
//                    gatt?.close()
                }
            }
        }
    }

    private val PACKAGE_SIZE = 20  //一个包的大小

    fun byteArrayToStr(byteArray: ByteArray?): String? {
        return byteArray?.let { String(it) }
    }

    fun byteArrayToHexStr(byteArray: ByteArray?): String? {
        if (byteArray == null) {
            return null
        }
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(byteArray.size * 2)
        for (j in byteArray.indices) {
            val v: Int = (byteArray[j] and 0xFF.toByte()).toInt()
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    @Synchronized
    fun writeback(str: String): Boolean {


        mCharacteristic?.run {

            val byteArray = str.toByteArray(charset("GBK"))
            val count = byteArray.size / PACKAGE_SIZE
            try {

                for (i in 0..byteArray.size step PACKAGE_SIZE) {
                    var byteArrayTemp: ByteArray
                    if (byteArray.size < i + PACKAGE_SIZE) {
                        byteArrayTemp = ByteArray(byteArray.size - i)
                        System.arraycopy(
                            byteArray,
                            i,
                            byteArrayTemp,
                            0,
                            byteArray.size - i
                        )
                    } else {
                        byteArrayTemp = ByteArray(PACKAGE_SIZE)
                        System.arraycopy(
                            byteArray,
                            i,
                            byteArrayTemp,
                            0,
                            PACKAGE_SIZE
                        )
                    }

                    if (byteArrayTemp.size > 0) {
                        this.value = byteArrayTemp
                        val state = mBluetoothGatt?.writeCharacteristic(this)
                        Log.d(TAG, "write  state : $state")
                        //TODO 返回 错误
                    }
                    Thread.sleep(10)


                }

            } catch (e: NegativeArraySizeException) {
                e.printStackTrace()
                return false
            } finally {
                return true
            }
        }
        return true

    }


    @Synchronized
    fun write(str: String): Boolean {


        mCharacteristic?.run {

            val byteArray = str.toByteArray(charset("GBK"))
            val count = byteArray.size / PACKAGE_SIZE
            try {
                var writeCount = 0;   //43
                while (writeCount < byteArray.size) {

                    if (sIsSendNext) {
                        var byteArrayTemp: ByteArray
                        if (byteArray.size < writeCount + PACKAGE_SIZE) {
                            byteArrayTemp = ByteArray(byteArray.size - writeCount)
                            System.arraycopy(
                                byteArray,
                                writeCount,
                                byteArrayTemp,
                                0,
                                byteArray.size - writeCount
                            )
                        } else {
                            byteArrayTemp = ByteArray(PACKAGE_SIZE)
                            System.arraycopy(
                                byteArray,
                                writeCount,
                                byteArrayTemp,
                                0,
                                PACKAGE_SIZE
                            )
                        }
                        if (byteArrayTemp.size > 0) {
                            this.value = byteArrayTemp
                            sIsSendNext = false  //写数据中
                            val state = mBluetoothGatt?.writeCharacteristic(this) ?: false
                            if (state) {
//                                sIsSendNext =true
                                writeCount += PACKAGE_SIZE
                            } else {
                                //发送失败了
                                sIsSendNext = true
                            }
                            Log.d(TAG, "write  state : $state")
                            //TODO 返回 错误
                        }
                    }
                    Thread.sleep(10)
                }

                

            } catch (e: NegativeArraySizeException) {
                e.printStackTrace()
                return false
            } finally {
                return true
            }
        }
        return true

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

