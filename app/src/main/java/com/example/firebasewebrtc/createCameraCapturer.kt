package com.example.firebasewebrtc

import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer

fun createCameraCapturer(enumerator: CameraEnumerator): CameraVideoCapturer? {
    for (deviceName in enumerator.deviceNames) {
        if (enumerator.isFrontFacing(deviceName)) {
            return enumerator.createCapturer(deviceName, null)
        }
    }
    for (deviceName in enumerator.deviceNames) {
        if (!enumerator.isFrontFacing(deviceName)) {
            return enumerator.createCapturer(deviceName, null)
        }
    }
    return null
}