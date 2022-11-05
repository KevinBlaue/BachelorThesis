package de.hsfl.kevinblaue.musicrun.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApi.DeviceStreamingFeature
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHrData
import io.reactivex.rxjava3.annotations.NonNull
import java.util.*


class PolarBluetoothService(context: Context) {
    var api: PolarBleApi = defaultImplementation(context, PolarBleApi.ALL_FEATURES)
    var connected: MutableLiveData<Boolean>? = MutableLiveData(false)
    var heartRate: MutableLiveData<Int>? = MutableLiveData(0)

    init {
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d("MyApp", "BLE power: $powered")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "CONNECTED: " + polarDeviceInfo.deviceId)
                connected?.value = true
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "CONNECTING: " + polarDeviceInfo.deviceId)
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "DISCONNECTED: " + polarDeviceInfo.deviceId)
            }

            override fun streamingFeaturesReady(
                identifier: String,
                features: Set<DeviceStreamingFeature>
            ) {
                for (feature in features) {
                    Log.d("MyApp", "Streaming feature $feature is ready")
                }
            }

            override fun hrFeatureReady(identifier: String) {
                Log.d("MyApp", "HR READY: $identifier")
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                Log.d("MyApp", "Info: $identifier $uuid $value")
            }

            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d("MyApp", "Battery level: $identifier $level")
            }

            override fun hrNotificationReceived(
                @NonNull identifier: String,
                @NonNull data: PolarHrData
            ) {
                heartRate?.value = data.hr
            }

            override fun polarFtpFeatureReady(@NonNull s: String) {}
        })
    }
}