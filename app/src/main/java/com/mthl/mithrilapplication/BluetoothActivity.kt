package com.mthl.mithrilapplication

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_bluetooth.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class BluetoothActivity : AppCompatActivity() {

    private val REQUEST_CODE_ENABLE_BT: Int = 1
    private val REQUEST_CODE_DISCOVERABLE_BT: Int = 2

    // Bluetooth adapter
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var pairedDevices: Array<BluetoothDevice>

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    private class ConnectToDevice(c: Context, val notifier: notifConnection) : AsyncTask<Void, Void, String>() {
        var connectSuccess: Boolean = true
        private var context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "Please wait")
        }

        override fun doInBackground(vararg params: Void?): String {
            try {
                if (m_bluetoothSocket != null)
                    m_bluetoothSocket!!.close()
                m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                m_bluetoothSocket!!.connect()
            }
            catch (e: Exception) {
                connectSuccess = false
            }
            return null.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            m_progress.dismiss()
            if (connectSuccess) {
                m_isConnected = true
                notifier.notifySuccess()
            }
            else {
                m_isConnected = false
                notifier.notifyFailure()
            }
        }
    }

    private class notifConnection(val c: BluetoothActivity) {
       fun notifySuccess() {
           c.bluetoothConnectedTV.setText("Connected to $m_address")
           c.setResult(1, Intent())
       }
        fun notifyFailure() {
            c.bluetoothConnectedTV.setText("Disconnected")
            c.setResult(0, Intent())
        }
    }

    private fun updateDevices() {
        if (bluetoothAdapter.isEnabled) {
            bluetoothIV.setImageResource(R.drawable.ic_bluetooth_on)
            pairedDevices = bluetoothAdapter!!.bondedDevices.toTypedArray()
            if (pairedDevices.isNotEmpty()) {
                bluetoothPairedTV.text = "Paired devices:"
                var list: ArrayList<String> = ArrayList()

                for (device in pairedDevices) {
                    list.add("Name: ${device.name}, id: $device")
                }
                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list)
                bluetoothPairedLV.adapter = adapter
                bluetoothPairedLV.setOnItemClickListener { parent, view, position, id ->
                    val choosedDevice = pairedDevices[position]
                    m_address = choosedDevice.address

                    var connection = ConnectToDevice(this, notifConnection(this))
                    connection.execute()
                }
            }

            else {
                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
                bluetoothPairedLV.adapter = adapter
                bluetoothPairedTV.text = "There are no paired devices"
            }
        }
        else {
            bluetoothPairedTV.text = "Bluetooth is off"
            bluetoothIV.setImageResource(R.drawable.ic_bluetooth_off)
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
            bluetoothPairedLV.adapter = adapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)

        // Init adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // Check bluetooth state
        if (bluetoothAdapter == null) {
            bluetoothStatusTV.text = "Bluetooth is not available"
            return
        }
        else
            bluetoothStatusTV.text = "Bluetooth is available"

        // Set image according to state
        if (bluetoothAdapter.isEnabled) {
            bluetoothIV.setImageResource(R.drawable.ic_bluetooth_on)
            updateDevices()
        }
        else
            bluetoothIV.setImageResource(R.drawable.ic_bluetooth_off)

        // Turn on bluetooth
        bluetoothTurnOnBtn.setOnClickListener {
            if (bluetoothAdapter.isEnabled)
                Toast.makeText(this, "Already on", Toast.LENGTH_LONG).show()
            else {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, REQUEST_CODE_ENABLE_BT)
            }
        }

        // Turn off bluetooth
        bluetoothTurnOffBtn.setOnClickListener {
            if (!bluetoothAdapter.isEnabled)
                Toast.makeText(this, "Already off", Toast.LENGTH_LONG).show()
            else {
                bluetoothAdapter.disable()
                bluetoothIV.setImageResource(R.drawable.ic_bluetooth_off)
                Toast.makeText(this, "Bluetooth is off", Toast.LENGTH_LONG).show()
                updateDevices()
                bluetoothConnectedTV.setText("Disconnected")
            }
        }

        // Discoverable
        bluetoothDiscoverableBtn.setOnClickListener {
            if (!bluetoothAdapter.isDiscovering) {
                Toast.makeText(this, "Making device discoverable", Toast.LENGTH_LONG).show()
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                startActivityForResult(intent, REQUEST_CODE_DISCOVERABLE_BT)
            }
        }

        // Get paired devices
        bluetoothPairedBtn.setOnClickListener {
            if (!bluetoothAdapter.isEnabled)
                Toast.makeText(this, "Turn on bluetooth first", Toast.LENGTH_LONG).show()
            updateDevices()
        }

        setResult(0, Intent())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothIV.setImageResource(R.drawable.ic_bluetooth_on)
                    Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_LONG).show()
                    updateDevices()
                }
                else {
                    Toast.makeText(this, "Failed to turn on bluetooth", Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        updateDevices()

        if (m_isConnected) {
            bluetoothConnectedTV.setText("Connected to $m_address")
        }
        else {
            bluetoothConnectedTV.setText("Disconnected")
        }

    }

}
