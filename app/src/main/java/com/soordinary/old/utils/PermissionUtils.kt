package com.soordinary.old.utils

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PermissionUtils {

    // 请求单个权限
    fun requestPermission(
        activity: FragmentActivity,
        permissionLauncher: ActivityResultLauncher<String>,
        permission: String,
        onGranted: () -> Unit,
    ) {
        // 如果权限已经授予，直接执行 onGranted
        if (ContextCompat.checkSelfPermission(activity, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            permissionLauncher.launch(permission)
        }
    }
}
