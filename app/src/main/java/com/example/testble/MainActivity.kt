package com.example.testble

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private var IMEI: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPremission()
        initBle()
        initListener()
    }

    private fun initListener() {
        bt_connect.setOnClickListener {

            if (et_ble.text.isNotEmpty()) {
                IMEI = et_ble.text.toString().trim()
                startSearchBle() //查找设备
            } else {
                Toast.makeText(this, "请输入设备号！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initBle() {
        BleManager.instance.iniBle(this.applicationContext)
        BleManager.instance.mOnBleStateListener = object : BleManager.OnBleStateListener {
            override fun onSearchBleSuccess(bleDevice: BluetoothDeviceBean?) {
//                TODO("Not yet implemented")

                Log.d(TAG, "OnBleStateListener  onSearchBleSuccess")
                bleDevice?.run {
                    if (this?.name.equals(IMEI)) {
                        BleManager.instance.stopScanBLE()
                        BleManager.instance.connectDevice(this@MainActivity, this)
                    }
                }


            }

            override fun onBleConnect(deviceAddress: String?, deviceName: String?) {
//                TODO("Not yet implemented")
                Log.d(TAG, "OnBleStateListener  onBleConnect")
                setDeviceState()
            }

            override fun onBleDisconnect() {
//                TODO("Not yet implemented")
            }

            override fun onReceiveBleMsg(msg: String?) {
//                TODO("Not yet implemented")
            }

        }
    }

    private fun setDeviceState() {
        if (BleManager.instance.sIsBleConnect) {
            tv_ble_state.text = "已连接"
        } else {
            tv_ble_state.text = "未已连接"
        }

    }

    private fun startSearchBle() {
        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter == null) {
        } else {
            bluetoothManager.adapter.enable()
        }

        if (bluetoothManager.adapter.isEnabled) {
            BleManager.instance.startScanBLE()
        }
    }

    private fun requestPremission() {
        //判断是否有权限
        //判断是否有权限
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //请求权限
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                102
            )
            //判断是否需要 向用户解释，为什么要申请该权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "shouldShowRequestPermissionRationale",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}