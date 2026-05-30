package com.example.point_6.data.sensor

import android.content.Context
import android.util.Log
import com.samsung.android.sdk.penremote.AirMotionEvent
import com.samsung.android.sdk.penremote.SpenEventListener
import com.samsung.android.sdk.penremote.SpenRemote
import com.samsung.android.sdk.penremote.SpenUnit
import com.samsung.android.sdk.penremote.SpenUnitManager

class SpenManager(private val context: Context) {
    private var spenRemote: SpenRemote = SpenRemote.getInstance()
    private var unitManager: SpenUnitManager? = null
    var onSpenDataReceived: ((Float, Float) -> Unit)? = null

    private val airMotionListener = SpenEventListener { event ->
        val airEvent = AirMotionEvent(event)
        onSpenDataReceived?.invoke(airEvent.deltaX, airEvent.deltaY)
    }

    fun connectSpen() {
        if (!spenRemote.isConnected) {
            spenRemote.connect(context, object : SpenRemote.ConnectionResultCallback {
                override fun onSuccess(manager: SpenUnitManager) {
                    unitManager = manager
                    val airMotionUnit = manager.getUnit(SpenUnit.TYPE_AIR_MOTION)
                    manager.registerSpenEventListener(airMotionListener, airMotionUnit)
                }

                override fun onFailure(error: Int) {
                    Log.e("SpenManager", "S-Pen Connection Failed: error code $error")
                }
            })
        }
    }

    fun disconnectSpen() {
        // 에스펜이 실제로 연결되어 있는 상태에서만 리스너 해제 및 서비스 연결 해제를 수행하도록 방어 코드 추가
        if (spenRemote.isConnected) {
            unitManager?.let {
                val airMotionUnit = it.getUnit(SpenUnit.TYPE_AIR_MOTION)
                it.unregisterSpenEventListener(airMotionUnit)
            }
            spenRemote.disconnect(context)
        }
    }
}