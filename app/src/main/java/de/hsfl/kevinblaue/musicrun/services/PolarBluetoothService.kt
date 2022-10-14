package de.hsfl.kevinblaue.musicrun.services

import android.content.Context
import android.os.Handler
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


class PolarBluetoothService(context: Context, private val handler: Handler) {
    var api: PolarBleApi = defaultImplementation(context, PolarBleApi.ALL_FEATURES)
    var heartRate: MutableLiveData<Int>? = MutableLiveData(0)

    init {
        api.setApiCallback(object : PolarBleApiCallback() {
            override fun blePowerStateChanged(powered: Boolean) {
                Log.d("MyApp", "BLE power: $powered")
            }

            override fun deviceConnected(@NonNull polarDeviceInfo: PolarDeviceInfo) {
                handler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_CONNECTED, -1).sendToTarget()
                Log.d("MyApp", "CONNECTED: " + polarDeviceInfo.deviceId)
            }

            override fun deviceConnecting(@NonNull polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "CONNECTING: " + polarDeviceInfo.deviceId)
            }

            override fun deviceDisconnected(@NonNull polarDeviceInfo: PolarDeviceInfo) {
                Log.d("MyApp", "DISCONNECTED: " + polarDeviceInfo.deviceId)
            }

            override fun streamingFeaturesReady(
                @NonNull identifier: String,
                @NonNull features: Set<DeviceStreamingFeature>
            ) {
                for (feature in features) {
                    Log.d("MyApp", "Streaming feature $feature is ready")
                }
            }

            override fun hrFeatureReady(@NonNull identifier: String) {
                Log.d("MyApp", "HR READY: $identifier")
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
            }

            override fun batteryLevelReceived(@NonNull identifier: String, level: Int) {}

            override fun hrNotificationReceived(
                @NonNull identifier: String,
                @NonNull data: PolarHrData
            ) {
                heartRate?.value = data.hr
                Log.d("MyApp", "HR: " + data.hr)
            }

            override fun polarFtpFeatureReady(@NonNull s: String) {}
        })
    }
}