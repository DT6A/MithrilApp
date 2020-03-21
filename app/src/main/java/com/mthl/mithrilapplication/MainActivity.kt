package com.mthl.mithrilapplication

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.security.spec.ECField

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_CONNECT_DEVICE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothSetBtn.setOnClickListener {
            val intent = Intent(this, BluetoothActivity::class.java)
            try {
                startActivityForResult(intent, 1)
            }
            catch (e: Exception) {

            }
        }

        commandsBtn.setOnClickListener {
            val intent = Intent(this, ControllActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_CONNECT_DEVICE -> {
                when (resultCode) {
                    1 -> try {
                        textView.setText("Device id: " + BluetoothActivity.m_address)
                        commandsBtn.isEnabled = true
                    } catch (e: Exception) {
                        textView.setText("Failed to connect")
                    }
                    else -> textView.setText("None device connected")
                }
            }
        }
    }
}
