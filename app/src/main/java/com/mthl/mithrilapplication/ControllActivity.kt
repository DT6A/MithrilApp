package com.mthl.mithrilapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_controll.*
import java.lang.Exception

class ControllActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controll)

        sendCommandBtn.setOnClickListener {
            if (BluetoothActivity.m_bluetoothSocket != null) {
                try {
                    BluetoothActivity.m_bluetoothSocket!!.outputStream.write(commandET.text.toString().toByteArray())
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
