package com.app.gdl.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {

    private const val REQUEST_CODE_ALL_PERMISSIONS = 123

    val REQUIRED_PERMISSIONS: Array<String>
        get() {
            val basePermissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                basePermissions.addAll(
                    listOf(
                        //Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            } else {
                basePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            return basePermissions.toTypedArray()
        }

    fun hasAllPermissions(activity: Activity): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestAllPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_ALL_PERMISSIONS
        )
    }

    fun handlePermissionsResult(
        activity: Activity,
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onAllGranted: () -> Unit,
        onSomeDenied: (deniedPermissions: List<String>) -> Unit
    ) {
        if (requestCode == REQUEST_CODE_ALL_PERMISSIONS) {
            val denied = permissions.zip(grantResults.toTypedArray())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
                .map { it.first }

            if (denied.isEmpty()) {
                onAllGranted()
            } else {
                onSomeDenied(denied)
            }
        }
    }
}
