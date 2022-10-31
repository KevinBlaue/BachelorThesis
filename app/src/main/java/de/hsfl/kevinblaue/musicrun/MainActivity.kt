package de.hsfl.kevinblaue.musicrun

import android.Manifest.permission.*
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import de.hsfl.kevinblaue.musicrun.fragments.MainMenuFragment
import de.hsfl.kevinblaue.musicrun.services.PolarBluetoothService
import de.hsfl.kevinblaue.musicrun.viewmodels.ActivityViewModel
import de.hsfl.kevinblaue.musicrun.viewmodels.MainMenuViewModel

private const val PERMISSION_REQUEST_CODE = 1
private const val DEVICE_ID = "B291691D"

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val mainMenuViewModel: MainMenuViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by viewModels()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var polarBluetoothService: PolarBluetoothService? = null
    private val requestBluetooth =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                mainMenuViewModel.bluetoothEnabled.value = true
                connectPolarDevice()
            }
        }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up repository
        activityViewModel.setRepository(this.application)

        // Go to main menu fragment
        if (savedInstanceState == null) {
            toMainMenu()
        }

        // Set up backpress button
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment = supportFragmentManager.findFragmentByTag("MAIN_MENU")
                    if (fragment !== null && fragment.isVisible) {
                        finish()
                    } else {
                        toMainMenu()
                    }
                }
            })

        // Permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(
                    arrayOf(
                        BLUETOOTH_SCAN,
                        BLUETOOTH_CONNECT,
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
                    ), PERMISSION_REQUEST_CODE
                )
            } else {
                requestPermissions(
                    arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION
                    ), PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        polarBluetoothService?.api?.foregroundEntered()
    }

    override fun onDestroy() {
        super.onDestroy()
        polarBluetoothService?.api?.shutDown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allGranted = false
                }
            }

            if (allGranted) {
                setupBluetooth()
            } else {
                Toast.makeText(this, "Zugriff verweigert", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBluetooth() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        enableBluetooth()
    }

    private fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        } else {
            connectPolarDevice()
        }
    }

    private fun connectPolarDevice() {
        // Connects to nearby device and observe heart rate
        polarBluetoothService = PolarBluetoothService(this)
        polarBluetoothService?.heartRate?.observe(this) { heartRate ->
            activityViewModel.heartRate.value = heartRate
        }
        polarBluetoothService?.connected?.observe(this) { connected ->
            mainMenuViewModel.beltConnected.value = connected
        }
        polarBluetoothService?.api?.connectToDevice(DEVICE_ID)
    }

    private fun toMainMenu() {
        supportFragmentManager.commit {
            replace<MainMenuFragment>(R.id.fragment_container_view, "MAIN_MENU")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }
}
