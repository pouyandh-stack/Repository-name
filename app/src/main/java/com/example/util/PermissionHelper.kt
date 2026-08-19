package com.example.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Utility for managing and requesting all Android Mobile Permissions for "رسانه آریا".
 */
object PermissionHelper {

    data class PermissionItem(
        val key: String,
        val titleFa: String,
        val descriptionFa: String,
        val isGranted: Boolean,
        val isCritical: Boolean = false,
        val manifestPermissions: List<String>
    )

    /**
     * Retrieves the comprehensive list of required permissions and their current status on device.
     */
    fun getAllPermissionsStatus(context: Context): List<PermissionItem> {
        val list = mutableListOf<PermissionItem>()

        // 1. Notifications
        val notifGranted = areNotificationsEnabled(context)
        val notifPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }
        list.add(
            PermissionItem(
                key = "NOTIFICATIONS",
                titleFa = "اعلان‌ها و پیام‌های فوری (Push Notifications)",
                descriptionFa = "جهت دریافت لحظه‌ای تایید آگهی، نردبان، پیام‌های مدیریت و هشدارهای مهم",
                isGranted = notifGranted,
                isCritical = true,
                manifestPermissions = notifPerms
            )
        )

        // 2. Camera
        val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        list.add(
            PermissionItem(
                key = "CAMERA",
                titleFa = "دوربین عکاسی و فیلمبرداری",
                descriptionFa = "جهت ثبت و عکاسی زنده از کالا، خودرو یا ملک هنگام درج آگهی جدید",
                isGranted = cameraGranted,
                isCritical = false,
                manifestPermissions = listOf(Manifest.permission.CAMERA)
            )
        )

        // 3. Media & Storage
        val storageGranted = isStoragePermissionGranted(context)
        val mediaPerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        list.add(
            PermissionItem(
                key = "STORAGE",
                titleFa = "دسترسی به گالری و فایل‌ها",
                descriptionFa = "جهت انتخاب عکس‌های آگهی از گالری و ذخیره تصاویر محلی",
                isGranted = storageGranted,
                isCritical = true,
                manifestPermissions = mediaPerms
            )
        )

        // 4. Location
        val locationGranted = isLocationPermissionGranted(context)
        list.add(
            PermissionItem(
                key = "LOCATION",
                titleFa = "موقعیت مکانی و نقشه (GPS)",
                descriptionFa = "جهت تشخیص خودکار شهر (صفاشهر، بوانات، قادرآباد، پاسارگاد) و ثبت لوکیشن دقیق",
                isGranted = locationGranted,
                isCritical = false,
                manifestPermissions = listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        )

        // 5. Phone Call
        val phoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        list.add(
            PermissionItem(
                key = "PHONE",
                titleFa = "تماس تلفنی مستقیم",
                descriptionFa = "جهت برقراری تماس مستقیم و امن با فروشنده یا کارشناسان پشتیبانی",
                isGranted = phoneGranted,
                isCritical = false,
                manifestPermissions = listOf(Manifest.permission.CALL_PHONE)
            )
        )

        return list
    }

    /**
     * Checks if notifications are active for this app.
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * Checks storage/media permission.
     */
    fun isStoragePermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks GPS Location permission.
     */
    fun isLocationPermissionGranted(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Returns an array of all permissions to request at once.
     */
    fun getAllRequiredPermissions(): Array<String> {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
            list.add(Manifest.permission.READ_MEDIA_IMAGES)
            list.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return list.toTypedArray()
    }

    /**
     * Opens system app settings page so user can toggle any permissions manually.
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
