package com.mthl.mithrilapplication

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_controll.*
import java.lang.Exception

class ControllActivity : AppCompatActivity() {
        private var isOn = true
        private class bluetoothReciever(c: Context, val notifier: ControllActivity.notifRecieve) : AsyncTask<Void, Void, String>() {

            private var context: Context

            init {
                this.context = c
            }

            override fun onPreExecute() {
                super.onPreExecute()
            }

            override fun doInBackground(vararg params: Void?): String {
                while ((context as ControllActivity).isOn) {
                    try {
                        Thread.sleep(500)
                        val data: ByteArray = ByteArray(100)
                        val len = BluetoothActivity.m_bluetoothSocket!!.inputStream.read(data)
                        notifier.showData(data.sliceArray(0..(len - 1)))
                    }
                    catch (e: Exception) {

                    }
                }

                return null.toString()
            }
        }


        private class notifRecieve(val c: ControllActivity) {
            fun showData(data: ByteArray) {
                c.dataTV.setText(data.toString(Charsets.US_ASCII))
            }
        }

        private lateinit var btTask: bluetoothReciever

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
            btTask = bluetoothReciever(this, notifRecieve(this))
            btTask.execute()
        }

    override fun onDestroy() {
        super.onDestroy()
        isOn = false
    }
}
