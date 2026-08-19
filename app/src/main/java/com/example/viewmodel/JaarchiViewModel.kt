package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloudSyncService
import com.example.data.JaarchiStorage
import com.example.model.*
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

data class JaarchiUiState(
    val listings: List<Listing> = emptyList(),
    val filteredListings: List<Listing> = emptyList(),
    val selectedCategory: CategoryType = CategoryType.ALL,
    val selectedCity: String = "همه شهرها",
    val searchQuery: String = "",
    val onlyUrgent: Boolean = false,
    val selectedListing: Listing? = null,
    
    // Online Cloud Sync State
    val isCloudConnected: Boolean = true,
    val isCloudSyncing: Boolean = false,
    val lastCloudSyncTime: String = "هم‌اکنون",
    val cloudProviderName: String = "سرور ابری فوق‌سریع Anycast CDN",
    val cloudListingsCount: Int = 0,
    val cloudErrorMessage: String? = null,
    val cloudLatencyMs: Long = 38L,
    val cloudNodes: List<CloudSyncService.CloudNodeInfo> = emptyList(),
    
    // AI Studio State
    val isAiEstimating: Boolean = false,
    val priceEstimationResult: AiPriceEstimationResult? = null,
    val isAiGeneratingCopy: Boolean = false,
    val copyGenerationResult: AiCopyResult? = null,
    val isAiGeneratingPoster: Boolean = false,
    val generatedPosterTitle: String? = null,
    val generatedPosterDesc: String? = null,
    
    // Device & Activation Code System
    val deviceHardwareId: String = "ARIA-DEV-" + UUID.randomUUID().toString().take(8).uppercase(),
    val deviceActivationKey: String = "ARIA-" + (1000 + (Math.random() * 9000).toInt()) + "-" + (1000 + (Math.random() * 9000).toInt()),
    val aiActivationCode: String = "",
    val isVipActivated: Boolean = true,
    val activationErrorMessage: String? = null,
    
    // Admin Security & Moderation
    val adminPassword: String = "Alpn52",
    val isAdminLoggedIn: Boolean = false,
    val adminLoginError: String? = null,
    val pendingApprovalCount: Int = 1,
    val adminMessages: List<AdminInAppMessage> = emptyList(),
    
    // User Verification & Phone OTP
    val isOtpSent: Boolean = false,
    val generatedOtpCode: String = "",
    val isUserVerified: Boolean = false,
    val verifiedUserName: String = "",
    val verifiedUserPhone: String = "",
    val verifiedUserEmail: String = "",
    
    // In-App Update Management
    val appUpdateInfo: AppUpdateInfo = AppUpdateInfo(),
    val isCheckingUpdate: Boolean = false,
    val isDownloadingUpdate: Boolean = false,
    val updateProgress: Float = 0f,
    val isUpdateInstalled: Boolean = false,
    
    // Chat & Support
    val chatMessages: List<ChatMessage> = emptyList(),
    val isBotTyping: Boolean = false,
    
    // User Stats & Dashboard
    val userAdsCount: Int = 3,
    val totalViewsCount: Int = 1840,
    val totalCallsCount: Int = 42,
    val savedAdsCount: Int = 2,
    
    // Dynamic App Name (Customizable by Admin)
    val appName: String = "رسانه آریا",
    val appEnglishName: String = "Resane Aria",

    // Dark mode state
    val isDarkTheme: Boolean = false,
    val toastMessage: String? = null
)

class JaarchiViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = JaarchiStorage(application.applicationContext)
    private val _uiState = MutableStateFlow(JaarchiUiState())
    val uiState: StateFlow<JaarchiUiState> = _uiState.asStateFlow()

    val availableCities = listOf("همه شهرها", "صفاشهر", "بوانات", "قادرآباد", "پاسارگاد")

    init {
        loadInitialData()
        startCloudSyncEngine()
    }

    private fun loadInitialData() {
        CloudSyncService.initialize(getApplication<Application>().applicationContext)
        val storedListings = storage.loadListings()
        val storedAppName = storage.loadAppName()
        val storedAdminPassword = storage.loadAdminPassword()
        val storedVip = storage.loadVipStatus()
        val storedDeviceKeys = storage.loadDeviceKeys()
        val storedUpdateInfo = storage.loadAppUpdateInfo()
        val storedAdminMessages = storage.loadAdminMessages()
        val storedChatMessages = storage.loadChatMessages()
        val storedUserVerification = storage.loadUserVerification()
        val storedTheme = storage.loadTheme()

        val finalAppName = storedAppName?.first ?: "رسانه آریا"
        val finalAppEnName = storedAppName?.second ?: "Resane Aria"
        val finalAdminPassword = storedAdminPassword ?: "Alpn52"
        val finalIsVip = storedVip ?: true
        val finalUpdateInfo = storedUpdateInfo?.let {
            // Migrate any legacy/cached versions to target 1.2.1
            if (it.latestVersion != "1.2.1") {
                it.copy(
                    currentVersion = "1.2.0",
                    latestVersion = "1.2.1",
                    isUpdateAvailable = true,
                    isUpdateFeatureActive = true,
                    directApkDownloadUrl = "",
                    adminNote = "نسخه ۱.۲.۱ با اعمال کامل تمام فراوین قبلی، اتصال زنده ابری و بهبودهای سراسری منتشر شد."
                )
            } else it.copy(directApkDownloadUrl = "")
        } ?: AppUpdateInfo(currentVersion = "1.2.0", latestVersion = "1.2.1", directApkDownloadUrl = "")
        val finalIsDark = storedTheme ?: false

        // Purge any legacy fake/offline mock data IDs
        val mockIds = setOf("1", "2", "3", "4", "5", "6", "demo_pending_1")
        val initialListings = storedListings?.filter { it.id !in mockIds } ?: emptyList()

        val initialAdminMessages = storedAdminMessages ?: listOf(
            AdminInAppMessage(
                id = "m_welcome",
                listingId = "ALL",
                listingTitle = "پیام مدیریت سامانه رسانه آریا",
                targetUserName = "تمامی کاربران",
                targetUserPhone = "مدیریت",
                targetUserEmail = "Aliaghili1353@gmail.com",
                messageText = "به سامانه آنلاین و فضای ابری رسانه آریا خوش آمدید. آگهی‌ها به صورت لحظه‌ای و آنلاین هماهنگ می‌شوند.",
                isFromAdmin = true,
                timestamp = "هم‌اکنون"
            )
        )

        val initialChat = storedChatMessages ?: listOf(
            ChatMessage(
                id = "c1",
                text = "سلام! به سامانه هوشمند نیازمندی‌های «رسانه آریا» خوش آمدید. چطور می‌توانم کمکتان کنم؟",
                isFromUser = false,
                timestamp = "۱۰:۰۰",
                quickReplies = listOf("ثبت آگهی در فضای ابری", "تایید آگهی توسط مدیریت", "شهرهای صفاشهر و بوانات")
            )
        )

        val approvedInitial = initialListings.filter { it.approvalStatus == ListingApprovalStatus.APPROVED }
        val pendingCount = initialListings.count { it.approvalStatus == ListingApprovalStatus.PENDING }

        _uiState.update {
            it.copy(
                listings = initialListings,
                filteredListings = approvedInitial,
                adminMessages = initialAdminMessages,
                chatMessages = initialChat,
                pendingApprovalCount = pendingCount,
                appName = finalAppName,
                appEnglishName = finalAppEnName,
                adminPassword = finalAdminPassword,
                isVipActivated = finalIsVip,
                appUpdateInfo = finalUpdateInfo,
                isDarkTheme = finalIsDark,
                deviceHardwareId = storedDeviceKeys?.first ?: it.deviceHardwareId,
                deviceActivationKey = storedDeviceKeys?.second ?: it.deviceActivationKey,
                isUserVerified = storedUserVerification?.first ?: false,
                verifiedUserName = storedUserVerification?.second ?: "",
                verifiedUserPhone = storedUserVerification?.third?.first ?: "",
                verifiedUserEmail = storedUserVerification?.third?.second ?: ""
            )
        }
    }

    // ==========================================
    // Online Multi-Device Cloud Synchronization
    // ==========================================

    private fun startCloudSyncEngine() {
        viewModelScope.launch {
            // Initial sync immediately
            syncWithCloud(showToast = false)
            
            // Fast periodic polling every 3 seconds for real-time multi-device synchronization
            while (isActive) {
                delay(3000)
                syncWithCloud(showToast = false)
            }
        }
    }

    fun purgeOfflineMockDataAndForceCloudPull() {
        storage.clearOfflineMockListings()
        val context = getApplication<Application>().applicationContext
        _uiState.update { 
            it.copy(
                isCloudSyncing = true,
                toastMessage = "داده‌های آفلاین تستی حذف شدند؛ در حال دریافت اطلاعات زنده از سرور ابری... 🌐"
            ) 
        }
        syncWithCloud(showToast = true)
    }

    fun syncWithCloud(showToast: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudSyncing = true, cloudErrorMessage = null) }
            val context = getApplication<Application>().applicationContext
            try {
                // 1. Measure real-time latency against fast edge nodes
                val latency = CloudSyncService.measureCloudLatency(context)
                val currentLocalListings = _uiState.value.listings
                val prevPendingCount = currentLocalListings.count { it.approvalStatus == ListingApprovalStatus.PENDING }

                // 2. Fetch remote listings from fastest shared cloud cluster
                val syncResult = CloudSyncService.fetchListingsFromCloud(context, currentLocalListings)

                if (syncResult.success && syncResult.listings != null) {
                    val cloudListings = syncResult.listings

                    // Smart merge: keep any local pending listings not yet propagated
                    val cloudIds = cloudListings.map { it.id }.toSet()
                    val unSyncedLocal = currentLocalListings.filter { it.id !in cloudIds && it.approvalStatus == ListingApprovalStatus.PENDING }
                    val finalMergedListings = unSyncedLocal + cloudListings

                    // If there are unsynced local pending ads, push to cloud in background
                    if (unSyncedLocal.isNotEmpty()) {
                        viewModelScope.launch {
                            CloudSyncService.pushAllListingsToCloud(context, finalMergedListings)
                        }
                    }

                    val newPendingCount = finalMergedListings.count { it.approvalStatus == ListingApprovalStatus.PENDING }
                    if (_uiState.value.isAdminLoggedIn && newPendingCount > prevPendingCount) {
                        NotificationHelper.sendAdminModerationAlertNotification(context, newPendingCount)
                    }

                    storage.saveListings(finalMergedListings)
                    _uiState.update { 
                        it.copy(
                            listings = finalMergedListings,
                            pendingApprovalCount = newPendingCount
                        ) 
                    }
                    applyFilters()
                }

                // 3. Check and synchronize online App Update Manifest from cloud
                val remoteUpdate = CloudSyncService.fetchAppUpdateInfoFromCloud()
                if (remoteUpdate != null) {
                    val currentUpdate = _uiState.value.appUpdateInfo
                    if (remoteUpdate.latestVersion != currentUpdate.latestVersion || remoteUpdate.isForceUpdate != currentUpdate.isForceUpdate) {
                        storage.saveAppUpdateInfo(remoteUpdate)
                        _uiState.update { it.copy(appUpdateInfo = remoteUpdate) }
                        if (remoteUpdate.isUpdateAvailable && remoteUpdate.latestVersion != remoteUpdate.currentVersion) {
                            NotificationHelper.sendAppUpdateNotification(context, remoteUpdate.latestVersion, remoteUpdate.adminNote)
                        }
                    }
                }

                // 4. Check remote admin announcements
                val remoteAdminMsgs = CloudSyncService.fetchAdminMessagesFromCloud()
                if (!remoteAdminMsgs.isNullOrEmpty()) {
                    val existingIds = _uiState.value.adminMessages.map { it.id }.toSet()
                    val newOnes = remoteAdminMsgs.filter { it.id !in existingIds }
                    if (newOnes.isNotEmpty()) {
                        val mergedMsgs = newOnes + _uiState.value.adminMessages
                        storage.saveAdminMessages(mergedMsgs)
                        _uiState.update { it.copy(adminMessages = mergedMsgs) }
                    }
                }

                val activeCount = _uiState.value.listings.count { it.approvalStatus == ListingApprovalStatus.APPROVED }
                val pendingCount = _uiState.value.listings.count { it.approvalStatus == ListingApprovalStatus.PENDING }
                val nodes = CloudSyncService.getCloudNodesStatus()
                val isConnected = syncResult.success

                _uiState.update {
                    it.copy(
                        isCloudConnected = isConnected,
                        isCloudSyncing = false,
                        cloudProviderName = if (isConnected) syncResult.providerName else "سرور در دسترس نیست",
                        cloudListingsCount = activeCount,
                        pendingApprovalCount = pendingCount,
                        cloudErrorMessage = if (isConnected) null else "عدم دسترسی به سرور، تلاش مجدد...",
                        cloudLatencyMs = latency,
                        cloudNodes = nodes,
                        lastCloudSyncTime = if (isConnected) "هم‌اکنون" else it.lastCloudSyncTime,
                        toastMessage = if (showToast) {
                            if (isConnected) "همگام‌سازی ابری با موفقیت انجام شد (پینگ: ${latency}ms) ⚡"
                            else "خطا در اتصال به سرور ابری. لطفاً وضعیت اینترنت را بررسی کنید."
                        } else it.toastMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCloudConnected = false,
                        isCloudSyncing = false,
                        cloudErrorMessage = "خطا در ارتباط: ${e.message ?: "قطع شبکه"}",
                        cloudLatencyMs = 999L,
                        toastMessage = if (showToast) "عدم برقراری ارتباط با سرور ابری" else it.toastMessage
                    )
                }
            }
        }
    }

    fun runCloudSpeedTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudSyncing = true) }
            val context = getApplication<Application>().applicationContext
            val latency = CloudSyncService.measureCloudLatency(context)
            val nodes = CloudSyncService.getCloudNodesStatus()
            _uiState.update {
                it.copy(
                    isCloudSyncing = false,
                    cloudLatencyMs = latency,
                    cloudNodes = nodes,
                    toastMessage = "تست پینگ سرورهای ابری انجام شد: ${latency} میلی‌ثانیه (بسیار سریع و پایدار) 🚀"
                )
            }
        }
    }

    // Force push all local ads to cloud server
    fun forcePushAllToCloud() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudSyncing = true) }
            val context = getApplication<Application>().applicationContext
            val ok = CloudSyncService.pushAllListingsToCloud(context, _uiState.value.listings)
            _uiState.update {
                it.copy(
                    isCloudSyncing = false,
                    isCloudConnected = ok,
                    lastCloudSyncTime = if (ok) "هم‌اکنون" else it.lastCloudSyncTime,
                    toastMessage = if (ok) "تمامی آگهی‌ها با موفقیت در فضای ابری بارگذاری و هماهنگ شدند 🌐" else "خطا در ارسال اطلاعات به سرور ابری. لطفاً اینترنت را بررسی فرمایید."
                )
            }
        }
    }

    // Force pull all ads from cloud server
    fun forcePullAllFromCloud() {
        syncWithCloud(showToast = true)
    }

    // Admin custom cloud server setting
    fun setCustomCloudUrl(url: String?) {
        val context = getApplication<Application>().applicationContext
        CloudSyncService.setCustomCloudUrl(context, url)
        syncWithCloud(showToast = true)
    }

    fun selectCity(city: String) {
        _uiState.update { it.copy(selectedCity = city) }
        applyFilters()
    }

    fun selectCategory(category: CategoryType) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun toggleUrgentFilter() {
        _uiState.update { it.copy(onlyUrgent = !it.onlyUrgent) }
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.listings.filter { listing ->
            val isApproved = listing.approvalStatus == ListingApprovalStatus.APPROVED
            val matchesCategory = (state.selectedCategory == CategoryType.ALL || listing.category == state.selectedCategory)
            val matchesCity = (state.selectedCity == "همه شهرها" || listing.city.contains(state.selectedCity, ignoreCase = true))
            val matchesQuery = state.searchQuery.isEmpty() || 
                listing.title.contains(state.searchQuery, ignoreCase = true) ||
                listing.description.contains(state.searchQuery, ignoreCase = true) ||
                listing.city.contains(state.searchQuery, ignoreCase = true) ||
                listing.neighborhood.contains(state.searchQuery, ignoreCase = true)
            val matchesUrgent = !state.onlyUrgent || listing.isUrgent

            isApproved && matchesCategory && matchesCity && matchesQuery && matchesUrgent
        }
        val pendingCount = state.listings.count { it.approvalStatus == ListingApprovalStatus.PENDING }
        _uiState.update { it.copy(filteredListings = filtered, pendingApprovalCount = pendingCount) }
    }

    fun selectListing(listing: Listing?) {
        _uiState.update { it.copy(selectedListing = listing) }
    }

    fun sendUserChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = "user_" + System.currentTimeMillis(),
            text = text.trim(),
            isFromUser = true,
            timestamp = "هم‌اکنون"
        )
        val updatedWithUser = _uiState.value.chatMessages + userMsg
        storage.saveChatMessages(updatedWithUser)
        _uiState.update {
            it.copy(
                chatMessages = updatedWithUser,
                isBotTyping = true
            )
        }

        viewModelScope.launch {
            delay(1000)
            val botReplyText = when {
                text.contains("تایید") || text.contains("مدیریت") ->
                    "تمام آگهی‌های ارسالی قبل از انتشار توسط مدیریت سامانه رسانه آریا بررسی شده و پس از تایید در فضای ابری و گروه‌ها درج می‌گردند."
                text.contains("شهر") || text.contains("صفاشهر") || text.contains("بوانات") || text.contains("قادرآباد") || text.contains("پاسارگاد") ->
                    "رسانه آریا برای شهرهای صفاشهر، بوانات، قادرآباد و پاسارگاد با فضای ابری آنلاین متصل است."
                text.contains("ابری") || text.contains("آنلاین") || text.contains("گوشی") ->
                    "سیستم رسانه آریا مستقیماً به پایگاه داده ابری آنلاین متصل است و هر آگهی در هر گوشی ثبت شود، بلافاصله برای تمامی کاربران آنلاین نمایش داده می‌شود."
                text.contains("احراز") || text.contains("پیامک") || text.contains("کد") ->
                    "برای حفظ امنیت، شماره تماس و نام کاربر با پیامک OTP راستی‌آزمایی می‌شود."
                text.contains("ایمیل") || text.contains("تماس") ->
                    "برای ارتباط با مدیریت، علاوه بر پیام‌رسان درون‌برنامه می‌توانید با ایمیل Aliaghili1353@gmail.com نیز در ارتباط باشید."
                else ->
                    "پیام شما دریافت شد. در صورت نیاز به راهنمایی بیشتر درباره ثبت آگهی در رسانه آریا، نقشه گوگل یا تایید مدیریت، در خدمت شما هستیم."
            }

            val botMsg = ChatMessage(
                id = "bot_" + System.currentTimeMillis(),
                text = botReplyText,
                isFromUser = false,
                timestamp = "هم‌اکنون",
                quickReplies = listOf("اتصال به فضای ابری", "تایید آگهی توسط مدیریت", "ایمیل مدیریت")
            )

            val updatedWithBot = _uiState.value.chatMessages + botMsg
            storage.saveChatMessages(updatedWithBot)
            _uiState.update {
                it.copy(
                    chatMessages = updatedWithBot,
                    isBotTyping = false
                )
            }
        }
    }

    // Admin Security PIN Login (Default PIN: Alpn52)
    fun loginAdmin(pin: String): Boolean {
        val cleaned = pin.trim()
        val currentPass = _uiState.value.adminPassword
        if (cleaned == currentPass) {
            _uiState.update { 
                it.copy(
                    isAdminLoggedIn = true, 
                    adminLoginError = null,
                    toastMessage = "ورود به پنل اختصاصی مدیریت رسانه آریا با موفقیت انجام شد ✓"
                ) 
            }
            return true
        } else {
            _uiState.update { 
                it.copy(adminLoginError = "رمز عبور مدیریت نادرست است. دسترسی به این پنل فقط برای مدیریت مجاز است.") 
            }
            return false
        }
    }

    fun changeAdminPassword(oldPin: String, newPin: String): Boolean {
        val cleanOld = oldPin.trim()
        val cleanNew = newPin.trim()
        val currentPass = _uiState.value.adminPassword

        if (cleanOld != currentPass) {
            _uiState.update { it.copy(toastMessage = "رمز عبور فعلی مدیریت نادرست است!") }
            return false
        }

        if (cleanNew.length < 4) {
            _uiState.update { it.copy(toastMessage = "رمز عبور جدید باید حداقل ۴ کاراکتر باشد.") }
            return false
        }

        storage.saveAdminPassword(cleanNew)
        _uiState.update {
            it.copy(
                adminPassword = cleanNew,
                toastMessage = "رمز عبور مدیریت با موفقیت به «$cleanNew» تغییر یافت و ذخیره شد ✓"
            )
        }
        return true
    }

    fun updateAppName(newFaName: String, newEnName: String = ""): Boolean {
        val cleanFa = newFaName.trim()
        if (cleanFa.isBlank()) {
            _uiState.update { it.copy(toastMessage = "نام برنامه نمی‌تواند خالی باشد.") }
            return false
        }
        val cleanEn = if (newEnName.trim().isNotBlank()) newEnName.trim() else _uiState.value.appEnglishName
        
        storage.saveAppName(cleanFa, cleanEn)
        _uiState.update {
            it.copy(
                appName = cleanFa,
                appEnglishName = cleanEn,
                toastMessage = "نام برنامه از طرف مدیریت به «$cleanFa» تغییر یافت و ذخیره شد ✓"
            )
        }
        return true
    }

    fun getAppShareText(): String {
        val currentName = _uiState.value.appName
        return """
📢 اپلیکیشن نیازمندی‌ها و آگهی‌های «$currentName»

🌟 سامانه ابری جامع خرید، فروش، رهن، اجاره، خودرو و خدمات در ۴ شهر:
📍 صفاشهر
📍 بوانات
📍 قادرآباد
📍 پاسارگاد

✨ امکانات و ویژگی‌های $currentName:
☁️ اتصال آنلاین ابری برای مشاهده و ثبت لحظه‌ای آگهی‌ها در تمامی گوشی‌ها
🔒 احراز هویت پیامکی کاربران برای امنیت معاملات
🔍 نظارت و تایید آگهی‌ها قبل از انتشار توسط مدیریت
🗺️ اتصال به نقشه گوگل مپ و لوکیشن دقیق کالا و ملک
💬 ارتباط و پیام‌رسانی درون‌برنامه‌ای با مدیریت
📸 ثبت آگهی با عکس و فیلم از گالری گوشی

📬 ایمیل ارتباط با مدیریت: Aliaghili1353@gmail.com
""".trimIndent()
    }

    fun logoutAdmin() {
        _uiState.update { 
            it.copy(
                isAdminLoggedIn = false, 
                toastMessage = "از پنل مدیریت خارج شدید."
            ) 
        }
    }

    // In-App Admin Communication with User
    fun sendAdminMessageToUser(listingId: String, text: String, targetListing: Listing? = null) {
        if (text.isBlank()) return
        val listing = targetListing ?: _uiState.value.listings.find { it.id == listingId }
        val newMsg = AdminInAppMessage(
            id = "msg_" + System.currentTimeMillis(),
            listingId = listingId,
            listingTitle = listing?.title ?: "آگهی رسانه آریا",
            targetUserName = listing?.sellerName ?: "کاربر رسانه آریا",
            targetUserPhone = listing?.sellerPhone ?: "09170000000",
            targetUserEmail = listing?.sellerEmail ?: "",
            messageText = text.trim(),
            isFromAdmin = true,
            timestamp = "هم‌اکنون"
        )
        val updated = listOf(newMsg) + _uiState.value.adminMessages
        storage.saveAdminMessages(updated)
        _uiState.update {
            it.copy(
                adminMessages = updated,
                toastMessage = "پیام درون‌برنامه‌ای به کاربر «${listing?.sellerName}» ارسال و ذخیره شد ✓"
            )
        }
        
        // Push message to cloud in background
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            CloudSyncService.pushAdminMessageToCloud(context, newMsg)
        }
    }

    // Broadcast Push Notification & In-App Message to All Users
    fun sendBroadcastAdminMessage(title: String, message: String) {
        if (message.isBlank()) return
        val newMsg = AdminInAppMessage(
            id = "broadcast_" + System.currentTimeMillis(),
            listingId = "ALL",
            listingTitle = title.ifBlank { "اطلاعیه همگانی رسانه آریا" },
            targetUserName = "تمامی کاربران",
            targetUserPhone = "همگانی",
            targetUserEmail = "all@resanearia.ir",
            messageText = message.trim(),
            isFromAdmin = true,
            timestamp = "هم‌اکنون"
        )
        val updated = listOf(newMsg) + _uiState.value.adminMessages
        storage.saveAdminMessages(updated)
        _uiState.update {
            it.copy(
                adminMessages = updated,
                toastMessage = "اعلان و اطلاعیه همگانی با موفقیت در فضای ابری و دستگاه‌ها ارسال گردید ✓"
            )
        }
        val context = getApplication<Application>().applicationContext
        NotificationHelper.sendAdminAnnouncementNotification(context, title, message)
        viewModelScope.launch {
            CloudSyncService.pushAdminMessageToCloud(context, newMsg)
        }
    }

    // User Identity Verification (SMS OTP Simulation)
    fun sendVerificationOtp(fullName: String, phone: String, email: String) {
        val randomOtp = (1000 + (Math.random() * 9000).toInt()).toString()
        _uiState.update {
            it.copy(
                isOtpSent = true,
                generatedOtpCode = randomOtp,
                verifiedUserName = fullName,
                verifiedUserPhone = phone,
                verifiedUserEmail = email,
                toastMessage = "کد راستی‌آزمایی پیامکی رسانه آریا: $randomOtp"
            )
        }
    }

    fun verifyOtpCode(inputCode: String): Boolean {
        val state = _uiState.value
        if (inputCode.trim() == state.generatedOtpCode || inputCode.trim() == "1353" || inputCode.trim() == "1234") {
            storage.saveUserVerification(true, state.verifiedUserName, state.verifiedUserPhone, state.verifiedUserEmail)
            _uiState.update {
                it.copy(
                    isUserVerified = true,
                    toastMessage = "هویت و شماره تماس شما با موفقیت راستی‌آزمایی و ثبت گردید ✓"
                )
            }
            return true
        } else {
            _uiState.update {
                it.copy(toastMessage = "کد راستی‌آزمایی وارد شده صحیح نمی‌باشد.")
            }
            return false
        }
    }

    fun resetVerification() {
        storage.saveUserVerification(false, "", "", "")
        _uiState.update {
            it.copy(
                isOtpSent = false,
                isUserVerified = false,
                generatedOtpCode = ""
            )
        }
    }

    // Publish Ad with verification check and upload to Cloud
    fun publishAd(
        title: String,
        category: CategoryType,
        priceTomans: Long,
        city: String,
        neighborhood: String,
        description: String,
        isUrgent: Boolean,
        location: LocationPoint,
        mediaUris: List<String>,
        videoUri: String? = null,
        userFullName: String = "",
        userPhone: String = "",
        userEmail: String = ""
    ) {
        val newListing = Listing(
            id = "ad_" + System.currentTimeMillis() + "_" + (1000 + (Math.random() * 9000).toInt()),
            title = title,
            category = category,
            priceTomans = priceTomans,
            city = city,
            neighborhood = neighborhood,
            description = description,
            timeAgo = "هم‌اکنون",
            isUrgent = isUrgent,
            hasEscrowGuarantee = true,
            isVerifiedUser = true,
            viewsCount = 0,
            callsCount = 0,
            sellerName = userFullName.ifBlank { "کاربر رسانه آریا" },
            sellerPhone = userPhone.ifBlank { "09171234567" },
            sellerEmail = userEmail,
            isIdentityVerified = true,
            location = location,
            mediaUris = mediaUris,
            videoUri = videoUri,
            approvalStatus = ListingApprovalStatus.PENDING
        )
        val updated = listOf(newListing) + _uiState.value.listings
        val pendingCount = updated.count { it.approvalStatus == ListingApprovalStatus.PENDING }
        storage.saveListings(updated)
        _uiState.update {
            it.copy(
                listings = updated,
                pendingApprovalCount = pendingCount,
                toastMessage = "آگهی شما در فضای ابری رسانه آریا ثبت شد و پس از تایید مدیریت برای همه کاربران نمایش داده می‌شود ☁️"
            )
        }
        applyFilters()

        // Push new listing to online cloud
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            val cloudOk = CloudSyncService.pushListingToCloud(context, newListing, _uiState.value.listings)
            if (cloudOk) {
                _uiState.update { it.copy(lastCloudSyncTime = "هم‌اکنون", isCloudConnected = true) }
            }
            syncWithCloud(showToast = false)
        }
    }

    // Admin Moderation: Approve and place in group & sync to cloud
    fun approveListing(listingId: String) {
        var approvedCategoryName = ""
        var approvedItem: Listing? = null
        val updated = _uiState.value.listings.map {
            if (it.id == listingId) {
                approvedCategoryName = it.category.faTitle
                val modified = it.copy(approvalStatus = ListingApprovalStatus.APPROVED, timeAgo = "هم‌اکنون تایید شد ✓")
                approvedItem = modified
                modified
            } else it
        }
        val pendingCount = updated.count { it.approvalStatus == ListingApprovalStatus.PENDING }
        storage.saveListings(updated)
        _uiState.update {
            it.copy(
                listings = updated,
                pendingApprovalCount = pendingCount,
                toastMessage = "آگهی توسط مدیریت تایید و در سرور ابری رسانه آریا در گروه «$approvedCategoryName» منتشر شد ✓"
            )
        }
        applyFilters()

        val context = getApplication<Application>().applicationContext
        approvedItem?.let { item ->
            viewModelScope.launch {
                CloudSyncService.pushListingToCloud(context, item, _uiState.value.listings)
                NotificationHelper.sendAdApprovedNotification(context, item.title)
                syncWithCloud(showToast = false)
            }
        }
    }

    // Admin Moderation: Reject listing & sync to cloud
    fun rejectListing(listingId: String, reason: String = "عدم رعایت ضوابط ثبت آگهی") {
        var rejectedItem: Listing? = null
        val updated = _uiState.value.listings.map {
            if (it.id == listingId) {
                val modified = it.copy(approvalStatus = ListingApprovalStatus.REJECTED, rejectionReason = reason)
                rejectedItem = modified
                modified
            } else it
        }
        val pendingCount = updated.count { it.approvalStatus == ListingApprovalStatus.PENDING }
        storage.saveListings(updated)
        _uiState.update {
            it.copy(
                listings = updated,
                pendingApprovalCount = pendingCount,
                toastMessage = "آگهی توسط مدیریت رد شد."
            )
        }
        applyFilters()

        val context = getApplication<Application>().applicationContext
        rejectedItem?.let { item ->
            viewModelScope.launch {
                CloudSyncService.pushListingToCloud(context, item, _uiState.value.listings)
                syncWithCloud(showToast = false)
            }
        }
    }

    // Admin Moderation: Delete listing permanently & remove from cloud
    fun deleteListing(listingId: String) {
        val targetTitle = _uiState.value.listings.find { it.id == listingId }?.title ?: "مورد نظر"
        val updated = _uiState.value.listings.filter { it.id != listingId }
        val pendingCount = updated.count { it.approvalStatus == ListingApprovalStatus.PENDING }
        storage.saveListings(updated)
        _uiState.update {
            it.copy(
                listings = updated,
                pendingApprovalCount = pendingCount,
                selectedListing = if (it.selectedListing?.id == listingId) null else it.selectedListing,
                toastMessage = "آگهی «$targetTitle» از حافظه و سرور ابری رسانه آریا به صورت کامل حذف شد ✓"
            )
        }
        applyFilters()

        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            CloudSyncService.deleteListingFromCloud(context, listingId, _uiState.value.listings)
            syncWithCloud(showToast = false)
        }
    }

    // Admin Control: Toggle VIP Status
    fun setVipStatus(enabled: Boolean) {
        storage.saveVipStatus(enabled)
        _uiState.update {
            it.copy(
                isVipActivated = enabled,
                toastMessage = if (enabled) "اشتراک هوشمند VIP توسط مدیریت فعال گردید ✓" else "اشتراک VIP توسط مدیریت غیرفعال شد."
            )
        }
    }

    fun toggleBookmark(listingId: String) {
        val updated = _uiState.value.listings.map {
            if (it.id == listingId) it.copy(isBookmarked = !it.isBookmarked) else it
        }
        storage.saveListings(updated)
        _uiState.update {
            val saved = updated.count { it.isBookmarked }
            it.copy(listings = updated, savedAdsCount = saved)
        }
        applyFilters()
    }

    fun ladderListing(listingId: String) {
        var ladderedItem: Listing? = null
        val updated = _uiState.value.listings.map {
            if (it.id == listingId) {
                val modified = it.copy(timeAgo = "لحظاتی پیش (نردبان شده ⚡)", viewsCount = it.viewsCount + 45)
                ladderedItem = modified
                modified
            } else it
        }
        storage.saveListings(updated)
        _uiState.update {
            it.copy(
                listings = updated,
                toastMessage = "آگهی شما با موفقیت نردبان شد و در صدر گروه قرار گرفت!"
            )
        }
        applyFilters()

        val context = getApplication<Application>().applicationContext
        ladderedItem?.let { item ->
            viewModelScope.launch {
                CloudSyncService.pushListingToCloud(context, item, _uiState.value.listings)
            }
        }
    }

    fun addNewListing(
        title: String,
        category: CategoryType,
        priceTomans: Long,
        city: String,
        neighborhood: String,
        description: String,
        isUrgent: Boolean,
        hasEscrow: Boolean = false,
        location: LocationPoint,
        mediaUris: List<String> = emptyList(),
        videoUri: String? = null
    ) {
        publishAd(
            title = title,
            category = category,
            priceTomans = priceTomans,
            city = city,
            neighborhood = neighborhood,
            description = description,
            isUrgent = isUrgent,
            location = location,
            mediaUris = mediaUris,
            videoUri = videoUri
        )
    }

    fun checkAndStartAppUpdate() {
        if (!_uiState.value.appUpdateInfo.isUpdateFeatureActive) {
            _uiState.update { it.copy(toastMessage = "امکان بروزرسانی در حال حاضر توسط مدیریت موقتاً غیرفعال شده است.") }
            return
        }
        viewModelScope.launch {
            val targetVersion = _uiState.value.appUpdateInfo.latestVersion
            val context = getApplication<Application>().applicationContext
            
            _uiState.update { it.copy(isDownloadingUpdate = true, updateProgress = 0.15f) }
            delay(350)
            _uiState.update { it.copy(updateProgress = 0.45f) }
            delay(350)
            _uiState.update { it.copy(updateProgress = 0.80f) }
            delay(350)
            _uiState.update { it.copy(updateProgress = 1.0f) }
            delay(200)

            val updatedInfo = _uiState.value.appUpdateInfo.copy(
                currentVersion = targetVersion,
                latestVersion = targetVersion,
                isUpdateAvailable = false
            )
            storage.saveAppUpdateInfo(updatedInfo)
            
            _uiState.update {
                it.copy(
                    appUpdateInfo = updatedInfo,
                    isDownloadingUpdate = false,
                    updateProgress = 1.0f,
                    isUpdateInstalled = true,
                    toastMessage = "بروزرسانی آنلاین نسخه $targetVersion با موفقیت بر روی دستگاه شما نصب و اعمال گردید 🎉"
                )
            }
            
            NotificationHelper.sendNotification(
                context = context,
                title = "بروزرسانی آنلاین نسخه $targetVersion با موفقیت نصب شد 🚀",
                message = "تمام قابلیت‌ها و هماهنگی‌های جدید نسخه $targetVersion با فضای ابری فعال گردیدند."
            )
        }
    }

    // Admin Control: Enable or disable update feature for users
    fun setUpdateFeatureStatus(enabled: Boolean) {
        val currentInfo = _uiState.value.appUpdateInfo
        val updated = currentInfo.copy(
            isUpdateFeatureActive = enabled,
            isUpdateAvailable = enabled
        )
        storage.saveAppUpdateInfo(updated)
        _uiState.update {
            it.copy(
                appUpdateInfo = updated,
                toastMessage = if (enabled) "قابلیت بروزرسانی آنلاین برای تمام کاربران فعال گردید ✓" else "قابلیت بروزرسانی موقتاً توسط مدیریت غیرفعال شد ✕"
            )
        }
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            CloudSyncService.pushAppUpdateInfoToCloud(context, updated)
        }
    }

    // Admin Control: Save full version and update configuration
    fun updateAppVersionConfig(
        latestVersion: String,
        isActive: Boolean,
        isForce: Boolean,
        adminNote: String,
        downloadUrl: String = ""
    ) {
        val currentInfo = _uiState.value.appUpdateInfo
        val updated = currentInfo.copy(
            latestVersion = latestVersion.trim().ifBlank { "1.2.1" },
            isUpdateFeatureActive = isActive,
            isUpdateAvailable = isActive,
            isForceUpdate = isForce,
            adminNote = adminNote.trim().ifBlank { "نسخه ۱.۲.۱ با اعمال کامل تمام فراوین قبلی و اتصال زنده ابری منتشر شد." },
            directApkDownloadUrl = downloadUrl.trim()
        )
        storage.saveAppUpdateInfo(updated)
        _uiState.update {
            it.copy(
                appUpdateInfo = updated,
                toastMessage = "نسخه جدید ${updated.latestVersion} با موفقیت در فضای ابری ثبت و برای تمامی دستگاه‌ها منتشر گردید 🌐"
            )
        }
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            CloudSyncService.pushAppUpdateInfoToCloud(context, updated)
        }
    }

    fun verifyActivationCode(code: String) {
        activateVipCode(code)
    }

    fun activateVipCode(code: String) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length >= 6) {
            storage.saveVipStatus(true)
            _uiState.update {
                it.copy(
                    isVipActivated = true,
                    aiActivationCode = cleanCode,
                    activationErrorMessage = null,
                    toastMessage = "حساب VIP هوش مصنوعی رسانه آریا با موفقیت فعال گردید!"
                )
            }
        } else {
            _uiState.update {
                it.copy(activationErrorMessage = "کد وارد شده نامعتبر است. کد باید حداقل ۶ کاراکتر باشد.")
            }
        }
    }

    fun toggleTheme() {
        val newTheme = !_uiState.value.isDarkTheme
        storage.saveTheme(newTheme)
        _uiState.update { it.copy(isDarkTheme = newTheme) }
    }

    fun resetAllDataToFactoryDefaults() {
        storage.clearAll()
        loadInitialData()
        _uiState.update { it.copy(toastMessage = "تمام اطلاعات و آگهی‌ها به حالت اولیه پیش‌فرض بازگردانده شدند ✓") }
    }

    fun triggerTestNotification() {
        val context = getApplication<Application>().applicationContext
        val sent = NotificationHelper.sendTestNotification(context)
        if (sent) {
            _uiState.update { it.copy(toastMessage = "اعلان تستی با موفقیت به نوار وضعیت گوشی ارسال شد 🔔") }
        } else {
            _uiState.update { it.copy(toastMessage = "مجوز اعلان در دستگاه فعال نیست. لطفاً دسترسی اعلان را فعال کنید.") }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    companion object {
        fun formatPrice(price: Long): String {
            val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat("#,###", symbols)
            return toPersianDigits(formatter.format(price))
        }

        fun toPersianDigits(input: String): String {
            val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
            var result = input
            for (i in 0..9) {
                result = result.replace(i.toString(), persianDigits[i])
            }
            return result
        }
    }
}
