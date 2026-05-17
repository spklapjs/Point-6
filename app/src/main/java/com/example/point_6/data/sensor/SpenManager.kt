package com.example.point_6.data.sensor

import android.content.Context
import com.samsung.android.sdk.penremote.SpenRemote

class SpenManager(private val context: Context) {
    private var spenRemote: SpenRemote = SpenRemote.getInstance()

    fun connectSpen() {
        // SpenRemote 연결 요청 및 TYPE_AIR_MOTION 이벤트 리스너 등록 로직 구현 예정
    }

    fun disconnectSpen() {
        // S-Pen 프레임워크 연결 해제 로직 구현 예정
    }
}