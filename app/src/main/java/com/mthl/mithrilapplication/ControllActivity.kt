package com.mthl.mithrilapplication

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_controll.*
import java.lang.Exception

class ControllActivity : AppCompatActivity() {

        private val CALIBRATION_BYTE: Char = 'C'
        private val POSTURE_ON_BYTE: String = "P"
        private val POSTURE_OFF_BYTE: String = "D"

        companion object {
            var isPostureOn:Boolean = true
            var isCalibrationRunning:Boolean = false
            lateinit var progress: ProgressDialog
        }

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
                while (!isCancelled() and (context as ControllActivity).isOn) {
                    while (isCalibrationRunning) {}
                    try {
                        Thread.sleep(100)
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

        private lateinit var btTask: bluetoothReciever
        private class calibrationReciever(c: Context) : AsyncTask<Void, Void, String>() {

            private var context: Context

            init {
                this.context = c
            }

            override fun onPreExecute() {
                super.onPreExecute()
                isCalibrationRunning = true
                BluetoothActivity.m_bluetoothSocket!!.outputStream.write((context as ControllActivity).CALIBRATION_BYTE.toString().toByteArray())
                progress = ProgressDialog.show(context, "Calibrating", "Please stay still")
            }

            override fun doInBackground(vararg params: Void?): String {
                val tBegin = System.currentTimeMillis()

                while (System.currentTimeMillis() - tBegin < 4000) {
                    val data: ByteArray = ByteArray(100)
                    val len = BluetoothActivity.m_bluetoothSocket!!.inputStream.read(data)
                    if (data.indexOf((context as ControllActivity).CALIBRATION_BYTE.toByte()) != -1)
                        break
                }

                return null.toString()
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                isCalibrationRunning = false
                progress.dismiss()
            }
        }
        private class notifRecieve(val c: ControllActivity) {
            fun showData(data: ByteArray) {
                c.dataTV.setText(data.toString(Charsets.US_ASCII))
            }
        }

        private lateinit var calibrTask: calibrationReciever

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

            if (isPostureOn)
                switchPosture.isChecked = true
            switchPosture.setOnCheckedChangeListener { _, isChecked ->
                isPostureOn = isChecked
                if (BluetoothActivity.m_bluetoothSocket != null) {
                    try {
                        var v: String = if (isChecked) POSTURE_ON_BYTE else POSTURE_OFF_BYTE
                        BluetoothActivity.m_bluetoothSocket!!.outputStream.write(v.toByteArray())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            btTask = bluetoothReciever(this, notifRecieve(this))
            btTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

            calibrCommandBtn.setOnClickListener {
                if (BluetoothActivity.m_bluetoothSocket != null) {
                    try {
                        calibrTask = calibrationReciever(this)
                        calibrTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        isOn = false
    }
}
