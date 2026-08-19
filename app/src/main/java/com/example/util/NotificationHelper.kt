package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

/**
 * System Notification Manager for "رسانه آریا" (Resane Aria).
 * Handles Notification Channels, System Bar Push Notifications, Sounds, and Vibrations.
 */
object NotificationHelper {

    const val CHANNEL_ADS_ID = "resane_aria_ads_channel"
    const val CHANNEL_ANNOUNCEMENTS_ID = "resane_aria_announcements_channel"
    const val CHANNEL_UPDATES_ID = "resane_aria_updates_channel"
    const val CHANNEL_SYSTEM_ID = "resane_aria_system_channel"

    private const val NOTIF_ID_TEST = 1001
    private const val NOTIF_ID_AD = 1002
    private const val NOTIF_ID_ANNOUNCEMENT = 1003
    private const val NOTIF_ID_UPDATE = 1004
    private const val NOTIF_ID_CLOUD = 1005

    /**
     * Initializes all Android Notification Channels (Android 8.0+ / API 26+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // 1. Channel for Listings & Ads
            val adsChannel = NotificationChannel(
                CHANNEL_ADS_ID,
                "آگهی‌ها و نردبان (رسانه آریا)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان ثبت، تایید و نردبان آگهی‌های جدید در شهرهای ۴ گانه"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            // 2. Channel for Admin Announcements & In-App Messages
            val announcementsChannel = NotificationChannel(
                CHANNEL_ANNOUNCEMENTS_ID,
                "اطلاعیه‌ها و پیام‌های مدیریت",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "پیام‌های مهم مدیریت، اخبار و اطلاعیه‌های رسمی سامانه رسانه آریا"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }

            // 3. Channel for App Updates
            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES_ID,
                "به‌روزرسانی‌های برنامه",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اطلاع‌رسانی انتشار نسخه‌های جدید رسانه آریا"
                enableLights(true)
                lightColor = Color.YELLOW
            }

            // 4. Channel for System & Cloud Sync
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM_ID,
                "سیستم و همگام‌سازی ابری",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "وضعیت اتصال ابری و همگام‌سازی زنده اطلاعات"
            }

            notificationManager.createNotificationChannels(listOf(adsChannel, announcementsChannel, updatesChannel, systemChannel))
        }
    }

    /**
     * Sends a rich Test Notification to verify user's phone notification pipeline.
     */
    fun sendTestNotification(context: Context): Boolean {
        return sendNotification(
            context = context,
            channelId = CHANNEL_ANNOUNCEMENTS_ID,
            notificationId = NOTIF_ID_TEST,
            title = "🔔 رسانه آریا | اعلان تستی فعال است",
            message = "سیستم اعلان‌ها و مجوزهای برنامه در گوشی شما با موفقیت فعال و هماهنگ گردید.",
            bigText = "سلام کاربر گرامی رسانه آریا! هم‌اکنون اعلان‌های مربوط به تایید آگهی‌ها، تخفیف‌ها و پیام‌های فوری در گوشی شما دریافت خواهند شد."
        )
    }

    /**
     * Sends notification when an ad is approved.
     */
    fun sendAdApprovedNotification(context: Context, adTitle: String): Boolean {
        return sendNotification(
            context = context,
            channelId = CHANNEL_ADS_ID,
            notificationId = (System.currentTimeMillis() % 100000).toInt(),
            title = "✓ آگهی شما تایید و منتشر شد",
            message = "آگهی «$adTitle» توسط مدیریت تایید و در رسانه آریا منتشر گردید.",
            bigText = "آگهی شما با موفقیت در صفاشهر، بوانات، قادرآباد و پاسارگاد منتشر شد و در دسترس عموم کاربران قرار گرفت."
        )
    }

    /**
     * Sends notification for new listings in city.
     */
    fun sendNewListingNotification(context: Context, title: String, category: String, city: String): Boolean {
        return sendNotification(
            context = context,
            channelId = CHANNEL_ADS_ID,
            notificationId = (System.currentTimeMillis() % 100000).toInt(),
            title = "📢 آگهی جدید در $city ($category)",
            message = title,
            bigText = "آگهی جدیدی در دسته $category در شهر $city ثبت شد: $title"
        )
    }

    /**
     * Sends notification for admin broadcast message.
     */
    fun sendAdminAnnouncementNotification(context: Context, title: String, message: String): Boolean {
        return sendNotification(
            context = context,
            channelId = CHANNEL_ANNOUNCEMENTS_ID,
            notificationId = (System.currentTimeMillis() % 100000).toInt(),
            title = "📢 اطلاعیه رسانه آریا: $title",
            message = message,
            bigText = message
        )
    }

    /**
     * Sends notification when new pending ads arrive for admin moderation.
     */
    fun sendAdminModerationAlertNotification(context: Context, pendingCount: Int): Boolean {
        return sendNotification(
            context = context,
            channelId = CHANNEL_ANNOUNCEMENTS_ID,
            notificationId = 1009,
            title = "🔔 آگهی جدید کاربر در صف تایید مدیریت",
            message = "تعداد $pendingCount آگهی جدید از کاربران در صف بررسی و تایید قرار دارد.",
            bigText = "کاربران به تازگی آگهی جدیدی ثبت کرده‌اند. لطفاً جهت بررسی و تایید به پنل مدیریت مراجعه فرمایید."
        )
    }

    /**
     * Sends notification for app updates.
     */
    fun sendAppUpdateNotification(context: Context, version: String, note: String = ""): Boolean {
        return sendNotification(
            context = context,
            channelId = CHANNEL_UPDATES_ID,
            notificationId = NOTIF_ID_UPDATE,
            title = "🚀 نسخه جدید رسانه آریا منتشر شد ($version)",
            message = note.ifBlank { "نسخه جدید با امکانات بهینه‌تر آماده دریافت است." },
            bigText = "نسخه $version رسانه آریا منتشر گردید.\nتوضیحات: ${note.ifBlank { "بهبود کارایی و امکانات جدید" }}"
        )
    }

    /**
     * Base Helper to send Notification with PendingIntent and fallback checks.
     */
    fun sendNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_UPDATES_ID,
        notificationId: Int = NOTIF_ID_UPDATE,
        bigText: String? = null
    ): Boolean {
        try {
            // Check POST_NOTIFICATIONS permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }

            createNotificationChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText ?: message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(longArrayOf(0, 250, 150, 250))
                .setContentIntent(pendingIntent)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(notificationId, builder.build())
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
