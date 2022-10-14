package de.hsfl.kevinblaue.musicrun

import android.Manifest.permission.*
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import de.hsfl.kevinblaue.musicrun.fragments.MainMenuFragment
import de.hsfl.kevinblaue.musicrun.services.*
import de.hsfl.kevinblaue.musicrun.viewmodels.ActivityViewModel
import de.hsfl.kevinblaue.musicrun.viewmodels.MainMenuViewModel


private const val PERMISSION_REQUEST_CODE = 1
private const val CONNECTION_TYPE = 0
private const val POLAR = 1
// ToDo: Mac Adresse herausfinden
private const val ADDRESS = "E4:32:7F:1D:A7:BE"

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val mainMenuViewModel: MainMenuViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by viewModels()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothService: BluetoothService? = null
    private var polarBluetoothService: PolarBluetoothService? = null
    private val requestBluetooth =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                mainMenuViewModel.bluetoothEnabled.value = true
                connectToDevice()
            }
        }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                enableBluetooth()
            }
        }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up repository
        activityViewModel.setRepository(this.application)

        if (savedInstanceState == null) {
           toMainMenu()
        }

        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = supportFragmentManager.findFragmentByTag("MAIN_MENU")
                if (fragment !== null && fragment.isVisible) {
                    finish()
                } else {
                    toMainMenu()
                }
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(arrayOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
            } else {
                requestPermissions(arrayOf(ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            }
        }
        requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
    }

    override fun onResume() {
        super.onResume()
        polarBluetoothService?.api?.foregroundEntered()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothService != null) {
            bluetoothService?.stop()
        }
        polarBluetoothService?.api?.shutDown()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (CONNECTION_TYPE == POLAR) {
            connectPolarDevice()
        } else {
            setupBluetooth()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupBluetooth() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                BLUETOOTH_CONNECT
            ) -> {
               enableBluetooth()
            }
            else -> {
                requestPermissionLauncher.launch(
                    BLUETOOTH_CONNECT
                )
            }
        }
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        } else {
            connectToDevice()
        }
    }

    private fun toMainMenu() {
        supportFragmentManager.commit {
            replace<MainMenuFragment>(R.id.fragment_container_view, "MAIN_MENU")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message)
        {
            when (msg.what) {
                MESSAGE_STATE_CHANGE -> {
                    if (msg.arg1 == STATE_CONNECTED) {
                        mainMenuViewModel.beltConnected.value = true
                    }
                }
                MESSAGE_READ -> {
                    /*val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)*/

                    // @ToDo: Take this or the one above
                    activityViewModel.heartRate.value = msg.arg1
                }
            }
        }
    }

    private fun connectToDevice() {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(ADDRESS)
        bluetoothService = BluetoothService(handler, this)
        if (device != null) {
            bluetoothService?.connect(device)
        }
        activityViewModel.heartRate.value = 75
        mainMenuViewModel.beltConnected.value = true
    }

    private fun connectPolarDevice() {
        // Connects to nearby device and observe heart rate
        polarBluetoothService = PolarBluetoothService(this, handler)
        polarBluetoothService?.heartRate?.observe(this) { heartRate ->
            activityViewModel.heartRate.value = heartRate
        }
        polarBluetoothService?.api?.autoConnectToDevice(
            -50, null, null
        )?.subscribe()
    }
}
