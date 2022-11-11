package de.hsfl.kevinblaue.musicrun

import android.Manifest.permission.*
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import de.hsfl.kevinblaue.musicrun.fragments.MainMenuFragment
import de.hsfl.kevinblaue.musicrun.repositories.StatisticsRepository
import de.hsfl.kevinblaue.musicrun.services.PolarBluetoothService
import de.hsfl.kevinblaue.musicrun.viewmodels.ActivityViewModel
import de.hsfl.kevinblaue.musicrun.viewmodels.MainMenuViewModel

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val activityViewModel: ActivityViewModel by viewModels()
    private val mainMenuViewModel: MainMenuViewModel by viewModels()
    private var repository: StatisticsRepository? = null
    private val requestBluetooth =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                connectPolarDevice()
            }
        }
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var polarBluetoothService: PolarBluetoothService? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = StatisticsRepository(this.application)
        activityViewModel.setRepository(repository!!)
        mainMenuViewModel.setRepository(repository!!)

        if (savedInstanceState == null) {
            toMainMenu()
        }

        /**
         * When the MainMenuFragment is active the app is closed, otherwise toMainMenu is called.
         */
        onBackPressedDispatcher.addCallback(
            this,
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

        /**
         * Check for permissions
         */
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

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

    /**
     * Creates a connection to the heart rate device via [PolarBluetoothService] and an unique
     * device id. This method also gives the heart rate values to the [ActivityViewModel.heartRate].
     */
    private fun connectPolarDevice() {
        polarBluetoothService = PolarBluetoothService(this)
        polarBluetoothService?.api?.connectToDevice(DEVICE_ID)
        polarBluetoothService?.heartRate?.observe(this) { heartRate ->
            activityViewModel.heartRate.value = heartRate
        }
        polarBluetoothService?.connected?.observe(this) { connected ->
            mainMenuViewModel.beltConnected.value = connected
        }
    }

    /**
     * Checks if Bluetooth is on and asks for activating it or connects the heart rate sensor.
     */
    private fun enableBluetooth() {
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        } else {
            connectPolarDevice()
        }
    }

    /**
     * Prepares the Bluetooth.
     */
    private fun setupBluetooth() {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        enableBluetooth()
    }

    /**
     * Replaces the current Fragment from FragmentManager with the [MainMenuFragment].
     */
    private fun toMainMenu() {
        supportFragmentManager.commit {
            replace<MainMenuFragment>(R.id.fragment_container_view, "MAIN_MENU")
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
        const val DEVICE_ID = "B291691D" // Unique ID of the heart rate sensor
    }
}
