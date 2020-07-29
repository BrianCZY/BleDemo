package com.example.testble

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        bt_test.setOnClickListener {
            sendbleMsg("#db_sk_b1_cmd,0b1,B116340002,B8.01,1,7,0,da")
            //#db_sk_b1_ip,0b1,B116340002,B8.01,1,20,1,234,1.23,1.24,0.01,0.01,0.01,0,24,183,52,0,2017-06-02 16:30:33,0\r\n
        }
    }


    fun sendbleMsg(str: String) {

        GlobalScope.launch {
            val sendSuccess = BleManager.instance.write(str)
            if (sendSuccess) {
                withContext(Dispatchers.Main) {
                    tv_send_text_content.text = str
                }

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
                runOnUiThread {
                    setDeviceState()
                }

            }

            override fun onBleDisconnect() {
                runOnUiThread {
                    setDeviceState()
                }

            }

            override fun onReceiveBleMsg(msg: String?) {
                //接收到数据
                //根据具体的业务解释数据
                msg?.run {
                    stringBuilder.append(msg) //拼接多个数据包
                    if (this.endsWith("\r\n")) {
                        //一组数据结束
                        val strTemp = stringBuilder.toString()
                        stringBuilder.clear()

                        if (strTemp.contains("db_sk_b1_ip")) {
                            sendbleMsg("#db_sk_b1_ip,0b1,B116340002,B8.01,1,20,1,234,1.23,1.24,0.01,0.01,0.01,0,24,183,52,0,2017-06-02 16:30:33,0,da")
                        }

                        if (strTemp.contains("db_sk_report_info_b1")) {
                            sendbleMsg("#db_sk_report_info_b1,0b1,B116340002,B8.01,1,11,1,0,0,0,0,da")
                        }

                        runOnUiThread {
                            tv_receiver_text_content.text = strTemp
                        }
                    }

                }

            }

        }
    }

    private val stringBuilder = StringBuilder("")
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