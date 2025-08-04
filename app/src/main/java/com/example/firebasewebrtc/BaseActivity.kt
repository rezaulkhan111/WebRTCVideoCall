package com.example.firebasewebrtc

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open class BaseActivity : AppCompatActivity() {
    private val permissionRequestCode = 100
    private var onPermissionGranted: (() -> Unit)? = null
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        )
    }

    fun requestPermissionsIfNeeded(onGranted: () -> Unit) {
        onPermissionGranted = onGranted
        if (hasPermissions()) {
            onGranted()
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions, permissionRequestCode)
        }
    }

    private fun hasPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onPermissionGranted?.invoke()
        } else {
            Toast.makeText(
                this, "Camera and Microphone permissions are required.", Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}