package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

class JaarchiStorage(context: Context) {
    private val prefs: SharedPreferences = run {
        val newPrefs = context.getSharedPreferences("resanearia_persistent_prefs", Context.MODE_PRIVATE)
        val oldPrefs = context.getSharedPreferences("jaarchi_persistent_prefs", Context.MODE_PRIVATE)
        // If new prefs is empty and old prefs has data, migrate
        if (!newPrefs.contains(KEY_LISTINGS) && oldPrefs.contains(KEY_LISTINGS)) {
            val editor = newPrefs.edit()
            oldPrefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                }
            }
            editor.apply()
        }
        newPrefs
    }

    companion object {
        private const val KEY_LISTINGS = "key_persistent_listings"
        private const val KEY_APP_NAME_FA = "key_app_name_fa"
        private const val KEY_APP_NAME_EN = "key_app_name_en"
        private const val KEY_ADMIN_PASSWORD = "key_admin_password"
        private const val KEY_IS_VIP = "key_is_vip"
        private const val KEY_DEVICE_HW_ID = "key_device_hw_id"
        private const val KEY_DEVICE_ACT_KEY = "key_device_act_key"
        private const val KEY_UPDATE_INFO = "key_update_info"
        private const val KEY_ADMIN_MESSAGES = "key_admin_messages"
        private const val KEY_CHAT_MESSAGES = "key_chat_messages"
        private const val KEY_IS_DARK_THEME = "key_is_dark_theme"
        private const val KEY_IS_USER_VERIFIED = "key_is_user_verified"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_PHONE = "key_user_phone"
        private const val KEY_USER_EMAIL = "key_user_email"
    }

    // ==========================================
    // Listings Persistence
    // ==========================================
    fun saveListings(listings: List<Listing>) {
        try {
            val jsonArray = JSONArray()
            for (item in listings) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("category", item.category.name)
                    put("priceTomans", item.priceTomans)
                    put("isNegotiable", item.isNegotiable)
                    put("city", item.city)
                    put("neighborhood", item.neighborhood)
                    put("description", item.description)
                    put("timeAgo", item.timeAgo)
                    put("isUrgent", item.isUrgent)
                    put("hasEscrowGuarantee", item.hasEscrowGuarantee)
                    put("isVerifiedUser", item.isVerifiedUser)
                    put("viewsCount", item.viewsCount)
                    put("callsCount", item.callsCount)
                    put("isBookmarked", item.isBookmarked)
                    put("sellerPhone", item.sellerPhone)
                    put("sellerName", item.sellerName)
                    put("sellerEmail", item.sellerEmail)
                    put("isIdentityVerified", item.isIdentityVerified)
                    if (item.aiValuationEstimated != null) put("aiValuationEstimated", item.aiValuationEstimated)
                    put("aiConfidenceScore", item.aiConfidenceScore)
                    
                    val locObj = JSONObject().apply {
                        put("latitude", item.location.latitude)
                        put("longitude", item.location.longitude)
                        put("addressTitle", item.location.addressTitle)
                    }
                    put("location", locObj)

                    val mediaArr = JSONArray()
                    item.mediaUris.forEach { mediaArr.put(it) }
                    put("mediaUris", mediaArr)

                    if (item.videoUri != null) put("videoUri", item.videoUri)
                    put("approvalStatus", item.approvalStatus.name)
                    if (item.rejectionReason != null) put("rejectionReason", item.rejectionReason)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_LISTINGS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadListings(): List<Listing>? {
        val jsonString = prefs.getString(KEY_LISTINGS, null) ?: return null
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Listing>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val catName = obj.optString("category", CategoryType.SERVICES.name)
                val cat = try { CategoryType.valueOf(catName) } catch (e: Exception) { CategoryType.SERVICES }
                
                val statusName = obj.optString("approvalStatus", ListingApprovalStatus.APPROVED.name)
                val status = try { ListingApprovalStatus.valueOf(statusName) } catch (e: Exception) { ListingApprovalStatus.APPROVED }

                val locObj = obj.optJSONObject("location")
                val location = if (locObj != null) {
                    LocationPoint(
                        latitude = locObj.optDouble("latitude", 30.6127),
                        longitude = locObj.optDouble("longitude", 53.1895),
                        addressTitle = locObj.optString("addressTitle", "صفاشهر، فارس")
                    )
                } else {
                    LocationPoint()
                }

                val mediaList = mutableListOf<String>()
                val mediaArr = obj.optJSONArray("mediaUris")
                if (mediaArr != null) {
                    for (m in 0 until mediaArr.length()) {
                        mediaList.add(mediaArr.getString(m))
                    }
                }

                val listing = Listing(
                    id = obj.optString("id", System.currentTimeMillis().toString()),
                    title = obj.optString("title", "آگهی رسانه آریا"),
                    category = cat,
                    priceTomans = obj.optLong("priceTomans", 0L),
                    isNegotiable = obj.optBoolean("isNegotiable", false),
                    city = obj.optString("city", "صفاشهر"),
                    neighborhood = obj.optString("neighborhood", "مرکز شهر"),
                    description = obj.optString("description", ""),
                    timeAgo = obj.optString("timeAgo", "به‌تازگی"),
                    isUrgent = obj.optBoolean("isUrgent", false),
                    hasEscrowGuarantee = obj.optBoolean("hasEscrowGuarantee", true),
                    isVerifiedUser = obj.optBoolean("isVerifiedUser", true),
                    viewsCount = obj.optInt("viewsCount", 1),
                    callsCount = obj.optInt("callsCount", 0),
                    isBookmarked = obj.optBoolean("isBookmarked", false),
                    sellerPhone = obj.optString("sellerPhone", "09171234567"),
                    sellerName = obj.optString("sellerName", "کاربر رسانه آریا"),
                    sellerEmail = obj.optString("sellerEmail", ""),
                    isIdentityVerified = obj.optBoolean("isIdentityVerified", true),
                    aiValuationEstimated = if (obj.has("aiValuationEstimated")) obj.optLong("aiValuationEstimated") else null,
                    aiConfidenceScore = obj.optInt("aiConfidenceScore", 90),
                    location = location,
                    mediaUris = mediaList,
                    videoUri = if (obj.has("videoUri")) obj.optString("videoUri") else null,
                    approvalStatus = status,
                    rejectionReason = if (obj.has("rejectionReason")) obj.optString("rejectionReason") else null
                )
                list.add(listing)
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearAllListings() {
        prefs.edit().remove(KEY_LISTINGS).apply()
    }

    fun clearOfflineMockListings() {
        val existing = loadListings() ?: return
        val mockIds = setOf("1", "2", "3", "4", "5", "6", "demo_pending_1")
        val clean = existing.filter { it.id !in mockIds }
        saveListings(clean)
    }

    // ==========================================
    // App Branding & Names Persistence
    // ==========================================
    fun saveAppName(faName: String, enName: String) {
        prefs.edit()
            .putString(KEY_APP_NAME_FA, faName)
            .putString(KEY_APP_NAME_EN, enName)
            .apply()
    }

    fun loadAppName(): Pair<String, String>? {
        val fa = prefs.getString(KEY_APP_NAME_FA, null)
        val en = prefs.getString(KEY_APP_NAME_EN, null)
        return if (fa != null && en != null) Pair(fa, en) else null
    }

    // ==========================================
    // Admin Security Persistence
    // ==========================================
    fun saveAdminPassword(password: String) {
        prefs.edit().putString(KEY_ADMIN_PASSWORD, password).apply()
    }

    fun loadAdminPassword(): String? {
        return prefs.getString(KEY_ADMIN_PASSWORD, null)
    }

    // ==========================================
    // VIP & Device Key Persistence
    // ==========================================
    fun saveVipStatus(isVip: Boolean) {
        prefs.edit().putBoolean(KEY_IS_VIP, isVip).apply()
    }

    fun loadVipStatus(): Boolean? {
        return if (prefs.contains(KEY_IS_VIP)) prefs.getBoolean(KEY_IS_VIP, false) else null
    }

    fun saveDeviceKeys(hwId: String, actKey: String) {
        prefs.edit()
            .putString(KEY_DEVICE_HW_ID, hwId)
            .putString(KEY_DEVICE_ACT_KEY, actKey)
            .apply()
    }

    fun loadDeviceKeys(): Pair<String, String>? {
        val hw = prefs.getString(KEY_DEVICE_HW_ID, null)
        val key = prefs.getString(KEY_DEVICE_ACT_KEY, null)
        return if (hw != null && key != null) Pair(hw, key) else null
    }

    // ==========================================
    // App Update Info Persistence
    // ==========================================
    fun saveAppUpdateInfo(info: AppUpdateInfo) {
        try {
            val obj = JSONObject().apply {
                put("currentVersion", info.currentVersion)
                put("latestVersion", info.latestVersion)
                put("isUpdateAvailable", info.isUpdateAvailable)
                put("isUpdateFeatureActive", info.isUpdateFeatureActive)
                put("isForceUpdate", info.isForceUpdate)
                put("directApkDownloadUrl", info.directApkDownloadUrl)
                put("adminNote", info.adminNote)
                
                val logsArr = JSONArray()
                info.changeLog.forEach { logsArr.put(it) }
                put("changeLog", logsArr)
            }
            prefs.edit().putString(KEY_UPDATE_INFO, obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAppUpdateInfo(): AppUpdateInfo? {
        val jsonString = prefs.getString(KEY_UPDATE_INFO, null) ?: return null
        return try {
            val obj = JSONObject(jsonString)
            val logs = mutableListOf<String>()
            val logsArr = obj.optJSONArray("changeLog")
            if (logsArr != null) {
                for (i in 0 until logsArr.length()) {
                    logs.add(logsArr.getString(i))
                }
            } else {
                logs.addAll(AppUpdateInfo().changeLog)
            }

            AppUpdateInfo(
                currentVersion = obj.optString("currentVersion", "1.2.0"),
                latestVersion = obj.optString("latestVersion", "1.2.1"),
                changeLog = logs,
                isUpdateAvailable = obj.optBoolean("isUpdateAvailable", true),
                isUpdateFeatureActive = obj.optBoolean("isUpdateFeatureActive", true),
                isForceUpdate = obj.optBoolean("isForceUpdate", false),
                directApkDownloadUrl = obj.optString("directApkDownloadUrl", ""),
                adminNote = obj.optString("adminNote", "نسخه ۱.۲.۱ با اعمال کامل تمام فراوین قبلی، اتصال زنده ابری و بهبودهای سراسری منتشر شد.")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==========================================
    // Admin In-App Messages Persistence
    // ==========================================
    fun saveAdminMessages(messages: List<AdminInAppMessage>) {
        try {
            val arr = JSONArray()
            for (msg in messages) {
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("listingId", msg.listingId)
                    put("listingTitle", msg.listingTitle)
                    put("targetUserName", msg.targetUserName)
                    put("targetUserPhone", msg.targetUserPhone)
                    put("targetUserEmail", msg.targetUserEmail)
                    put("messageText", msg.messageText)
                    put("isFromAdmin", msg.isFromAdmin)
                    put("timestamp", msg.timestamp)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_ADMIN_MESSAGES, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAdminMessages(): List<AdminInAppMessage>? {
        val jsonString = prefs.getString(KEY_ADMIN_MESSAGES, null) ?: return null
        return try {
            val arr = JSONArray(jsonString)
            val list = mutableListOf<AdminInAppMessage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AdminInAppMessage(
                        id = obj.optString("id", ""),
                        listingId = obj.optString("listingId", ""),
                        listingTitle = obj.optString("listingTitle", ""),
                        targetUserName = obj.optString("targetUserName", ""),
                        targetUserPhone = obj.optString("targetUserPhone", ""),
                        targetUserEmail = obj.optString("targetUserEmail", ""),
                        messageText = obj.optString("messageText", ""),
                        isFromAdmin = obj.optBoolean("isFromAdmin", true),
                        timestamp = obj.optString("timestamp", "هم‌اکنون")
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==========================================
    // Support Chat Messages Persistence
    // ==========================================
    fun saveChatMessages(messages: List<ChatMessage>) {
        try {
            val arr = JSONArray()
            for (msg in messages) {
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("text", msg.text)
                    put("isFromUser", msg.isFromUser)
                    put("timestamp", msg.timestamp)
                    val quickArr = JSONArray()
                    msg.quickReplies.forEach { quickArr.put(it) }
                    put("quickReplies", quickArr)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_CHAT_MESSAGES, arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadChatMessages(): List<ChatMessage>? {
        val jsonString = prefs.getString(KEY_CHAT_MESSAGES, null) ?: return null
        return try {
            val arr = JSONArray(jsonString)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val qList = mutableListOf<String>()
                val qArr = obj.optJSONArray("quickReplies")
                if (qArr != null) {
                    for (q in 0 until qArr.length()) qList.add(qArr.getString(q))
                }
                list.add(
                    ChatMessage(
                        id = obj.optString("id", ""),
                        text = obj.optString("text", ""),
                        isFromUser = obj.optBoolean("isFromUser", false),
                        timestamp = obj.optString("timestamp", ""),
                        quickReplies = qList
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ==========================================
    // User Verification & Phone Persistence
    // ==========================================
    fun saveUserVerification(isVerified: Boolean, name: String, phone: String, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_USER_VERIFIED, isVerified)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun loadUserVerification(): Triple<Boolean, String, Pair<String, String>>? {
        if (!prefs.contains(KEY_IS_USER_VERIFIED)) return null
        val verified = prefs.getBoolean(KEY_IS_USER_VERIFIED, false)
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val phone = prefs.getString(KEY_USER_PHONE, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        return Triple(verified, name, Pair(phone, email))
    }

    // ==========================================
    // Theme Mode Persistence
    // ==========================================
    fun saveTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_IS_DARK_THEME, isDark).apply()
    }

    fun loadTheme(): Boolean? {
        return if (prefs.contains(KEY_IS_DARK_THEME)) prefs.getBoolean(KEY_IS_DARK_THEME, false) else null
    }

    // ==========================================
    // Reset Data (Restore Initial Defaults)
    // ==========================================
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
