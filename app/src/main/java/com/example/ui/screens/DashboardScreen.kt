package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AdminInAppMessage
import com.example.model.CategoryType
import com.example.model.Listing
import com.example.model.ListingApprovalStatus
import com.example.model.LocationPoint
import com.example.ui.components.PersianBadge
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import com.example.util.PermissionHelper
import com.example.viewmodel.JaarchiUiState
import com.example.viewmodel.JaarchiViewModel

enum class DashboardTab(val faTitle: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MODERATION_QUEUE("صف تایید آگهی‌ها", Icons.Filled.HourglassTop),
    NOTIFICATIONS_AND_PERMISSIONS("اعلان‌ها و مجوزها", Icons.Filled.NotificationsActive),
    CLOUD_SYNC("پایگاه ابری آنلاین", Icons.Filled.CloudSync),
    ACTIVE_LISTINGS("مدیریت و حذف همه آگهی‌ها", Icons.Filled.DeleteSweep),
    UPDATE_MANAGEMENT("کنترل وضعیت بروزرسانی", Icons.Filled.SystemUpdateAlt),
    APP_IDENTITY("تغییر نام و برند برنامه", Icons.Filled.DriveFileRenameOutline),
    VIP_MANAGEMENT("اشتراک هوشمند VIP", Icons.Filled.AutoAwesome),
    IN_APP_CHAT("پیام‌رسانی با کاربران", Icons.Filled.Forum),
    REGIONS("شهرهای ۴ گانه", Icons.Filled.LocationCity),
    SECURITY_SETTINGS("تغییر رمز عبور", Icons.Filled.Lock)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: JaarchiUiState,
    viewModel: JaarchiViewModel
) {
    val context = LocalContext.current

    // If Admin is not logged in, show the Admin Security Gate
    if (!uiState.isAdminLoggedIn) {
        AdminLoginGate(
            appName = uiState.appName,
            error = uiState.adminLoginError,
            onLogin = { pin -> viewModel.loginAdmin(pin) }
        )
        return
    }

    var selectedTab by remember { mutableStateOf(DashboardTab.MODERATION_QUEUE) }
    var moderationFilterStatus by remember { mutableStateOf<ListingApprovalStatus?>(ListingApprovalStatus.PENDING) }
    var showDirectMessageDialog by remember { mutableStateOf<Listing?>(null) }
    var customMessageText by remember { mutableStateOf("") }
    var adminListingSearchQuery by remember { mutableStateOf("") }
    var adminListingFilterStatus by remember { mutableStateOf<ListingApprovalStatus?>(null) }

    val pendingListings = remember(uiState.listings) {
        uiState.listings.filter { it.approvalStatus == ListingApprovalStatus.PENDING }
    }
    val approvedListings = remember(uiState.listings) {
        uiState.listings.filter { it.approvalStatus == ListingApprovalStatus.APPROVED }
    }
    val rejectedListings = remember(uiState.listings) {
        uiState.listings.filter { it.approvalStatus == ListingApprovalStatus.REJECTED }
    }

    val filteredModerationListings = remember(uiState.listings, moderationFilterStatus) {
        if (moderationFilterStatus == null) {
            uiState.listings
        } else {
            uiState.listings.filter { it.approvalStatus == moderationFilterStatus }
        }
    }

    val allPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        NotificationHelper.createNotificationChannels(context)
    }
    val adminManagedListings = remember(uiState.listings, adminListingSearchQuery, adminListingFilterStatus) {
        uiState.listings.filter { listing ->
            val matchesStatus = adminListingFilterStatus == null || listing.approvalStatus == adminListingFilterStatus
            val matchesQuery = adminListingSearchQuery.isBlank() ||
                listing.title.contains(adminListingSearchQuery, ignoreCase = true) ||
                listing.sellerName.contains(adminListingSearchQuery, ignoreCase = true) ||
                listing.sellerPhone.contains(adminListingSearchQuery) ||
                listing.city.contains(adminListingSearchQuery, ignoreCase = true) ||
                listing.category.faTitle.contains(adminListingSearchQuery, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen_list"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Admin Profile & Status Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SoberNavy),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(26.dp))
                            }
                            Column {
                                Text("پنل نظارت و مدیریت ${uiState.appName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ایمیل رسمی مدیریت: Aliaghili1353@gmail.com", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.logoutAdmin() },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = "خروج از پنل مدیریت")
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("شهرهای فعال: صفاشهر، بوانات، قادرآباد، پاسارگاد", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                        PersianBadge(text = "دسترسی مدیریت فعال ✓", containerColor = SoftGreen, contentColor = EmeraldGreen)
                    }
                }
            }
        }

        // Stats Overview Grid (Interactive Quick Selectors)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "منتظر تایید",
                    value = JaarchiViewModel.toPersianDigits(pendingListings.size.toString()),
                    icon = Icons.Filled.HourglassTop,
                    iconTint = AmberAccent,
                    bgColor = SoftAmber,
                    modifier = Modifier.weight(1f),
                    isSelected = selectedTab == DashboardTab.MODERATION_QUEUE && moderationFilterStatus == ListingApprovalStatus.PENDING,
                    onClick = {
                        selectedTab = DashboardTab.MODERATION_QUEUE
                        moderationFilterStatus = ListingApprovalStatus.PENDING
                    }
                )
                StatCard(
                    title = "فعال در گروه‌ها",
                    value = JaarchiViewModel.toPersianDigits(approvedListings.size.toString()),
                    icon = Icons.Filled.CheckCircle,
                    iconTint = EmeraldGreen,
                    bgColor = SoftGreen,
                    modifier = Modifier.weight(1f),
                    isSelected = (selectedTab == DashboardTab.MODERATION_QUEUE && moderationFilterStatus == ListingApprovalStatus.APPROVED) || selectedTab == DashboardTab.ACTIVE_LISTINGS,
                    onClick = {
                        selectedTab = DashboardTab.MODERATION_QUEUE
                        moderationFilterStatus = ListingApprovalStatus.APPROVED
                    }
                )
                StatCard(
                    title = "پیام‌های کاربران",
                    value = JaarchiViewModel.toPersianDigits(uiState.adminMessages.size.toString()),
                    icon = Icons.Filled.MarkEmailUnread,
                    iconTint = SoberNavy,
                    bgColor = SoftNavy,
                    modifier = Modifier.weight(1f),
                    isSelected = selectedTab == DashboardTab.IN_APP_CHAT,
                    onClick = {
                        selectedTab = DashboardTab.IN_APP_CHAT
                    }
                )
            }
        }

        // Tab Selector Row
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SoberNavy,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                edgePadding = 8.dp,
                divider = {}
            ) {
                DashboardTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(tab.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(tab.faTitle, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                                if (tab == DashboardTab.MODERATION_QUEUE && pendingListings.isNotEmpty()) {
                                    Badge(containerColor = CrimsonRed, contentColor = Color.White) {
                                        Text(JaarchiViewModel.toPersianDigits(pendingListings.size.toString()), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Content for selected tab
        when (selectedTab) {
            DashboardTab.MODERATION_QUEUE -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftNavy),
                        border = BorderStroke(1.dp, SoberNavy.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.CloudSync, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(18.dp))
                                    Text("اتصال ابری زنده چندکاربره", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SoberNavy)
                                }
                                Text(
                                    "آگهی‌های ثبت‌شده توسط کاربران در کلیه گوشی‌ها فوراً در این صف بارگذاری می‌شوند.",
                                    fontSize = 10.sp,
                                    color = NeutralMedium
                                )
                            }
                            Button(
                                onClick = { viewModel.syncWithCloud(showToast = true) },
                                enabled = !uiState.isCloudSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = SoberNavy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                if (uiState.isCloudSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("در حال دریافت...", fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("بروزرسانی زنده", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Moderation Status Filter Chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = moderationFilterStatus == ListingApprovalStatus.PENDING,
                            onClick = { moderationFilterStatus = ListingApprovalStatus.PENDING },
                            label = {
                                Text(
                                    "⏳ منتظر تایید (${JaarchiViewModel.toPersianDigits(pendingListings.size.toString())})",
                                    fontSize = 11.sp,
                                    fontWeight = if (moderationFilterStatus == ListingApprovalStatus.PENDING) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftAmber,
                                selectedLabelColor = AmberAccent
                            )
                        )
                        FilterChip(
                            selected = moderationFilterStatus == ListingApprovalStatus.APPROVED,
                            onClick = { moderationFilterStatus = ListingApprovalStatus.APPROVED },
                            label = {
                                Text(
                                    "✓ فعال در گروه (${JaarchiViewModel.toPersianDigits(approvedListings.size.toString())})",
                                    fontSize = 11.sp,
                                    fontWeight = if (moderationFilterStatus == ListingApprovalStatus.APPROVED) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftGreen,
                                selectedLabelColor = EmeraldGreen
                            )
                        )
                        FilterChip(
                            selected = moderationFilterStatus == ListingApprovalStatus.REJECTED,
                            onClick = { moderationFilterStatus = ListingApprovalStatus.REJECTED },
                            label = {
                                Text(
                                    "✕ رد شده (${JaarchiViewModel.toPersianDigits(rejectedListings.size.toString())})",
                                    fontSize = 11.sp,
                                    fontWeight = if (moderationFilterStatus == ListingApprovalStatus.REJECTED) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftCrimson,
                                selectedLabelColor = CrimsonRed
                            )
                        )
                    }
                }

                if (filteredModerationListings.isEmpty()) {
                    item {
                        when (moderationFilterStatus) {
                            ListingApprovalStatus.PENDING -> {
                                EmptyQueueCard(
                                    title = "هیچ آگهی در صف انتظار نیست",
                                    subtitle = "تمامی آگهی‌های ارسالی توسط کاربران بررسی و تعیین تکلیف شده‌اند.",
                                    primaryButtonText = "➕ ایجاد آگهی آزمایشی در صف (جهت تست دکمه‌های تایید/رد مدیریت)",
                                    onPrimaryClick = {
                                        viewModel.publishAd(
                                            title = "آگهی تستی مدیریت جهت بررسی تایید و رد",
                                            category = CategoryType.SERVICES,
                                            priceTomans = 2500000L,
                                            city = "صفاشهر",
                                            neighborhood = "خیابان امام خمینی",
                                            description = "این آگهی جهت آزمایش و بررسی عملکرد دکمه‌های تایید، رد و پیام‌رسانی پنل مدیریت ایجاد شده است.",
                                            isUrgent = true,
                                            location = LocationPoint(addressTitle = "صفاشهر، مرکز شهر"),
                                            mediaUris = emptyList(),
                                            videoUri = null,
                                            userFullName = "کاربر تستی رسانه آریا",
                                            userPhone = "09170000000",
                                            userEmail = "testuser@gmail.com"
                                        )
                                    },
                                    secondaryButtonText = "مشاهده آگهی‌های فعال در گروه‌ها (${JaarchiViewModel.toPersianDigits(approvedListings.size.toString())})",
                                    onSecondaryClick = {
                                        moderationFilterStatus = ListingApprovalStatus.APPROVED
                                    }
                                )
                            }
                            ListingApprovalStatus.APPROVED -> {
                                EmptyQueueCard(
                                    title = "هیچ آگهی تایید‌شده‌ای یافت نشد",
                                    subtitle = "هنوز آگهی‌ای در وضعیت تایید شده قرار نگرفته است.",
                                    primaryButtonText = "مشاهده صف آگهی‌های در انتظار تایید",
                                    onPrimaryClick = {
                                        moderationFilterStatus = ListingApprovalStatus.PENDING
                                    }
                                )
                            }
                            ListingApprovalStatus.REJECTED -> {
                                EmptyQueueCard(
                                    title = "هیچ آگهی رد‌شده‌ای وجود ندارد",
                                    subtitle = "تاکنون هیچ آگهی توسط مدیریت رد نشده است.",
                                    primaryButtonText = "مشاهده صف انتظار تایید",
                                    onPrimaryClick = {
                                        moderationFilterStatus = ListingApprovalStatus.PENDING
                                    }
                                )
                            }
                            null -> {
                                EmptyQueueCard(
                                    title = "هیچ آگهی در سیستم ثبت نشده است",
                                    subtitle = "در حال حاضر آگهی‌ای در پایگاه داده وجود ندارد."
                                )
                            }
                        }
                    }
                } else {
                    items(filteredModerationListings, key = { it.id }) { listing ->
                        ModerationListingCard(
                            listing = listing,
                            onApprove = { viewModel.approveListing(listing.id) },
                            onReject = { viewModel.rejectListing(listing.id) },
                            onDelete = { viewModel.deleteListing(listing.id) },
                            onContactUser = { showDirectMessageDialog = listing }
                        )
                    }
                }
            }

            DashboardTab.NOTIFICATIONS_AND_PERMISSIONS -> {
                item {
                    AdminNotificationsPermissionsCard(
                        context = context,
                        onRequestAllPermissions = {
                            allPermissionsLauncher.launch(PermissionHelper.getAllRequiredPermissions())
                        },
                        onSendTestNotification = {
                            viewModel.triggerTestNotification()
                        },
                        onSendBroadcastPush = { title, message ->
                            val sent = NotificationHelper.sendAdminAnnouncementNotification(context, title, message)
                            if (sent) {
                                viewModel.sendBroadcastAdminMessage(title, message)
                            }
                        },
                        onOpenSettings = {
                            PermissionHelper.openAppSettings(context)
                        }
                    )
                }
            }

            DashboardTab.IN_APP_CHAT -> {
                item {
                    InAppAdminMessagingHeader(
                        onEmailContactClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:Aliaghili1353@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "پیام از سامانه رسانه آریا")
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    )
                }

                if (uiState.adminMessages.isEmpty()) {
                    item {
                        EmptyQueueCard(
                            title = "هیچ پیام درون‌برنامه‌ای ثبت نشده است",
                            subtitle = "مدیریت می‌تواند با آگهی‌دهندگان مستقیماً از طریق پیام‌رسان درون اپلیکیشن در ارتباط باشد."
                        )
                    }
                } else {
                    items(uiState.adminMessages, key = { it.id }) { msg ->
                        AdminMessageCard(
                            message = msg,
                            onReplyClick = {
                                val target = uiState.listings.find { it.id == msg.listingId }
                                showDirectMessageDialog = target
                            }
                        )
                    }
                }
            }

            DashboardTab.ACTIVE_LISTINGS -> {
                // Search and Filter Bar for All Listings
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = adminListingSearchQuery,
                                onValueChange = { adminListingSearchQuery = it },
                                placeholder = { Text("جستجوی آگهی بر اساس عنوان، فروشنده، شماره، شهر...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SoberNavy) },
                                trailingIcon = {
                                    if (adminListingSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { adminListingSearchQuery = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = "پاک کردن", tint = NeutralMedium)
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterChip(
                                    selected = adminListingFilterStatus == null,
                                    onClick = { adminListingFilterStatus = null },
                                    label = { Text("همه (${uiState.listings.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = adminListingFilterStatus == ListingApprovalStatus.APPROVED,
                                    onClick = { adminListingFilterStatus = ListingApprovalStatus.APPROVED },
                                    label = { Text("تایید شده (${approvedListings.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = adminListingFilterStatus == ListingApprovalStatus.PENDING,
                                    onClick = { adminListingFilterStatus = ListingApprovalStatus.PENDING },
                                    label = { Text("در انتظار (${pendingListings.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = adminListingFilterStatus == ListingApprovalStatus.REJECTED,
                                    onClick = { adminListingFilterStatus = ListingApprovalStatus.REJECTED },
                                    label = { Text("رد شده", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                if (adminManagedListings.isEmpty()) {
                    item {
                        EmptyQueueCard(
                            title = "هیچ آگهی‌ای یافت نشد",
                            subtitle = "با تغییر فیلتر یا عبارت جستجو آگهی‌های دیگر را بررسی فرمایید."
                        )
                    }
                } else {
                    items(adminManagedListings, key = { it.id }) { listing ->
                        ActiveListingRow(
                            listing = listing,
                            onLadder = { viewModel.ladderListing(listing.id) },
                            onDelete = { viewModel.deleteListing(listing.id) },
                            onContact = { showDirectMessageDialog = listing }
                        )
                    }
                }
            }

            DashboardTab.VIP_MANAGEMENT -> {
                item {
                    VipManagementCard(
                        isVipActivated = uiState.isVipActivated,
                        onToggleVip = { viewModel.setVipStatus(it) }
                    )
                }
            }

            DashboardTab.UPDATE_MANAGEMENT -> {
                item {
                    AdminUpdateSettingsDashboardCard(
                        updateInfo = uiState.appUpdateInfo,
                        onSaveUpdateSettings = { version, active, force, note ->
                            viewModel.updateAppVersionConfig(
                                latestVersion = version,
                                isActive = active,
                                isForce = force,
                                adminNote = note
                            )
                        },
                        onToggleStatus = { viewModel.setUpdateFeatureStatus(it) }
                    )
                }
            }

            DashboardTab.CLOUD_SYNC -> {
                item {
                    CloudSyncManagementCard(
                        uiState = uiState,
                        onSyncNow = { viewModel.syncWithCloud(showToast = true) },
                        onForcePush = { viewModel.forcePushAllToCloud() },
                        onForcePull = { viewModel.forcePullAllFromCloud() },
                        onPurgeOfflineData = { viewModel.purgeOfflineMockDataAndForceCloudPull() },
                        onSetCustomUrl = { viewModel.setCustomCloudUrl(it) },
                        onSpeedTest = { viewModel.runCloudSpeedTest() }
                    )
                }
            }

            DashboardTab.APP_IDENTITY -> {
                item {
                    AppIdentitySettingsCard(
                        currentAppName = uiState.appName,
                        currentEnglishName = uiState.appEnglishName,
                        onUpdateAppName = { faName, enName ->
                            viewModel.updateAppName(faName, enName)
                        }
                    )
                }
            }

            DashboardTab.REGIONS -> {
                item {
                    CitiesOverviewCard(listings = uiState.listings)
                }
            }

            DashboardTab.SECURITY_SETTINGS -> {
                item {
                    AdminSecuritySettingsCard(
                        onChangePassword = { oldPass, newPass ->
                            viewModel.changeAdminPassword(oldPass, newPass)
                        },
                        onResetData = {
                            viewModel.resetAllDataToFactoryDefaults()
                        }
                    )
                }
            }
        }
    }

    // Direct In-App Message Dialog (Admin to User)
    if (showDirectMessageDialog != null) {
        val targetListing = showDirectMessageDialog!!
        AlertDialog(
            onDismissRequest = { showDirectMessageDialog = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SoftNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(26.dp))
                }
            },
            title = {
                Text("پیام مستقیم درون‌برنامه‌ای به کاربر", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "گیرنده: ${targetListing.sellerName} (${targetListing.sellerPhone})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoberNavy
                    )
                    Text(
                        text = "مربوط به آگهی: ${targetListing.title}",
                        fontSize = 11.sp,
                        color = NeutralMedium
                    )

                    OutlinedTextField(
                        value = customMessageText,
                        onValueChange = { customMessageText = it },
                        placeholder = { Text("متن پیام، تذکر یا راهنمایی به کاربر را وارد کنید...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Text("پیام‌های آماده مدیریت:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = { customMessageText = "لطفاً تصاویر واضح‌تری از کالای خود در آگهی قرار دهید." },
                            label = { Text("درخواست عکس 📸", fontSize = 10.sp) }
                        )
                        AssistChip(
                            onClick = { customMessageText = "آگهی شما با موفقیت تایید و در گروه منتشر گردید." },
                            label = { Text("اعلام تایید ✓", fontSize = 10.sp) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customMessageText.isNotBlank()) {
                            viewModel.sendAdminMessageToUser(targetListing.id, customMessageText, targetListing)
                            customMessageText = ""
                            showDirectMessageDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ارسال پیام به کاربر")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectMessageDialog = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun AdminLoginGate(
    appName: String = "رسانه آریا",
    error: String?,
    onLogin: (String) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("admin_login_gate"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(SoftNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "قفل پنل مدیریت",
                        tint = SoberNavy,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "ورود به پنل اختصاصی مدیریت $appName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoberNavy,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "این بخش منحصراً مختص مدیران سامانه $appName می‌باشد. دسترسی کاربران عادی به این بخش محدود شده است.",
                    fontSize = 12.sp,
                    color = NeutralMedium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("رمز عبور مدیریت") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )

                if (error != null) {
                    Text(
                        text = error,
                        color = CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { onLogin(pinInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
                ) {
                    Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ورود به سامانه مدیریت", fontWeight = FontWeight.Bold)
                }

                // Direct email contact info
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SoftNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ایمیل مدیریت: Aliaghili1353@gmail.com", fontSize = 11.sp, color = SoberNavy, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun InAppAdminMessagingHeader(
    onEmailContactClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SoftNavy)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Forum, contentDescription = null, tint = SoberNavy)
                    Text("ارتباط مستقیم با کاربران درون اپلیکیشن", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SoberNavy)
                }

                OutlinedButton(
                    onClick = onEmailContactClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, SoberNavy)
                ) {
                    Icon(Icons.Filled.AlternateEmail, contentDescription = null, modifier = Modifier.size(14.dp), tint = SoberNavy)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ارسال ایمیل جداگانه", fontSize = 11.sp, color = SoberNavy)
                }
            }

            Text(
                text = "از این بخش می‌توانید بدون خروج از برنامه با صاحبان آگهی در صفاشهر، بوانات، قادرآباد و پاسارگاد پیام رد و بدل کنید.",
                fontSize = 11.sp,
                color = DignifiedSlate
            )
        }
    }
}

@Composable
fun AdminMessageCard(
    message: AdminInAppMessage,
    onReplyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SoftNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(message.targetUserName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(message.targetUserPhone, fontSize = 10.sp, color = NeutralMedium)
                    }
                }

                PersianBadge(text = message.timestamp, containerColor = SoftSlate, contentColor = DignifiedSlate)
            }

            Text(
                text = "مربوط به: «${message.listingTitle}»",
                fontSize = 11.sp,
                color = SoberNavy,
                fontWeight = FontWeight.SemiBold
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SoftSlate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message.messageText,
                    fontSize = 12.sp,
                    color = NeutralDark,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onReplyClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoberNavy),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Reply, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ارسال پیام مجدد", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ModerationListingCard(
    listing: Listing,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit,
    onContactUser: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("moderation_card_${listing.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (listing.approvalStatus) {
                    ListingApprovalStatus.PENDING -> {
                        PersianBadge(
                            text = "در انتظار تایید مدیریت ⏳",
                            containerColor = SoftAmber,
                            contentColor = AmberAccent,
                            icon = Icons.Filled.HourglassTop
                        )
                    }
                    ListingApprovalStatus.APPROVED -> {
                        PersianBadge(
                            text = "منتشر شده و فعال در گروه ✓",
                            containerColor = SoftGreen,
                            contentColor = EmeraldGreen,
                            icon = Icons.Filled.CheckCircle
                        )
                    }
                    ListingApprovalStatus.REJECTED -> {
                        PersianBadge(
                            text = "رد شده توسط مدیریت ✕",
                            containerColor = SoftCrimson,
                            contentColor = CrimsonRed,
                            icon = Icons.Filled.Cancel
                        )
                    }
                }

                PersianBadge(
                    text = listing.city,
                    containerColor = SoftNavy,
                    contentColor = SoberNavy,
                    icon = Icons.Filled.Place
                )
            }

            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قیمت: ${JaarchiViewModel.formatPrice(listing.priceTomans)} تومان",
                    fontWeight = FontWeight.Bold,
                    color = SoberNavy,
                    fontSize = 13.sp
                )
                Text(
                    text = "گروه: ${listing.category.faTitle}",
                    fontSize = 12.sp,
                    color = NeutralMedium
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SoftSlate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("مشخصات ثبت‌کننده (احراز شده):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoberNavy)
                    Text("نام: ${listing.sellerName} | تماس: ${listing.sellerPhone}", fontSize = 11.sp)
                    Text("ایمیل: ${listing.sellerEmail}", fontSize = 10.sp, color = NeutralMedium)
                    Text("نشانی: ${listing.location.addressTitle}", fontSize = 10.sp, color = NeutralMedium)
                }
            }

            Text(
                text = listing.description,
                fontSize = 12.sp,
                color = NeutralDark,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (listing.approvalStatus != ListingApprovalStatus.APPROVED) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (listing.approvalStatus == ListingApprovalStatus.REJECTED) "تایید مجدد" else "تایید آگهی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onContactUser,
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SoberNavy)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(13.dp), tint = SoberNavy)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("پیام", fontSize = 11.sp, color = SoberNavy)
                }

                if (listing.approvalStatus != ListingApprovalStatus.REJECTED) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAccent),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.6f))
                    ) {
                        Text(if (listing.approvalStatus == ListingApprovalStatus.APPROVED) "تعلیق" else "رد", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("حذف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("حذف دائم آگهی توسط مدیریت", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Text("آیا از حذف دائم و قطعی آگهی «${listing.title}» اطمینان دارید؟ این عمل غیرقابل بازگشت است.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("بله، حذف کامل آگهی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun ActiveListingRow(
    listing: Listing,
    onLadder: () -> Unit,
    onDelete: () -> Unit,
    onContact: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (listing.approvalStatus) {
                                ListingApprovalStatus.APPROVED -> SoftGreen
                                ListingApprovalStatus.PENDING -> SoftAmber
                                ListingApprovalStatus.REJECTED -> SoftCrimson
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (listing.approvalStatus) {
                            ListingApprovalStatus.APPROVED -> Icons.Filled.CheckCircle
                            ListingApprovalStatus.PENDING -> Icons.Filled.HourglassTop
                            ListingApprovalStatus.REJECTED -> Icons.Filled.Cancel
                        },
                        contentDescription = null,
                        tint = when (listing.approvalStatus) {
                            ListingApprovalStatus.APPROVED -> EmeraldGreen
                            ListingApprovalStatus.PENDING -> AmberAccent
                            ListingApprovalStatus.REJECTED -> CrimsonRed
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(listing.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (listing.approvalStatus) {
                                ListingApprovalStatus.APPROVED -> SoftGreen
                                ListingApprovalStatus.PENDING -> SoftAmber
                                ListingApprovalStatus.REJECTED -> SoftCrimson
                            }
                        ) {
                            Text(
                                text = when (listing.approvalStatus) {
                                    ListingApprovalStatus.APPROVED -> "تایید شده"
                                    ListingApprovalStatus.PENDING -> "در انتظار"
                                    ListingApprovalStatus.REJECTED -> "رد شده"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (listing.approvalStatus) {
                                    ListingApprovalStatus.APPROVED -> EmeraldGreen
                                    ListingApprovalStatus.PENDING -> AmberAccent
                                    ListingApprovalStatus.REJECTED -> CrimsonRed
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text("${listing.city} • ${listing.category.faTitle} • ${listing.sellerName}", fontSize = 11.sp, color = NeutralMedium)
                    Text("${JaarchiViewModel.formatPrice(listing.priceTomans)} تومان", fontWeight = FontWeight.Bold, color = SoberNavy, fontSize = 12.sp)
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onContact,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("پیام", fontSize = 11.sp, color = SoberNavy)
                }

                if (listing.approvalStatus == ListingApprovalStatus.APPROVED) {
                    OutlinedButton(
                        onClick = onLadder,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نردبان", fontSize = 11.sp, color = AmberAccent)
                    }
                }

                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف دائم آگهی", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("حذف دائم آگهی توسط مدیریت", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Text("آیا از حذف دائم آگهی «${listing.title}» متعلق به ${listing.sellerName} (${listing.sellerPhone}) اطمینان دارید؟ این عمل تمام اطلاعات آگهی را از سامانه پاک می‌کند.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("بله، حذف کامل آگهی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun VipManagementCard(
    isVipActivated: Boolean,
    onToggleVip: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isVipActivated) SoftAmber else SoftNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = if (isVipActivated) AmberAccent else SoberNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text("مدیریت اشتراک هوشمند VIP هوش مصنوعی", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SoberNavy)
                    Text("فعال‌سازی یا غیرفعال‌سازی توسط مدیریت سامانه", fontSize = 11.sp, color = NeutralMedium)
                }
            }

            Text(
                text = "کنترل وضعیت اشتراک هوشمند VIP انحصاراً در اختیار مدیریت سامانه قرار دارد. با فعال بودن این گزینه، امکانات پیشرفته هوش مصنوعی نظیر تخمین هوشمند قیمت و تولید خودکار پوستر برای کاربران در دسترس خواهد بود.",
                fontSize = 11.sp,
                color = DignifiedSlate,
                lineHeight = 17.sp
            )

            HorizontalDivider(color = NeutralLight)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVipActivated) SoftGreen else SoftAmber
                ),
                border = BorderStroke(1.dp, if (isVipActivated) EmeraldGreen.copy(alpha = 0.4f) else AmberAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isVipActivated) "وضعیت VIP: فعال است ✓" else "وضعیت VIP: غیرفعال است ✕",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isVipActivated) EmeraldGreen else CrimsonRed
                        )
                        Text(
                            text = if (isVipActivated) "تمام امکانات هوش مصنوعی برای کاربران باز است" else "دسترسی کاربران به امکانات VIP محدود شده است",
                            fontSize = 11.sp,
                            color = NeutralDark
                        )
                    }

                    Switch(
                        checked = isVipActivated,
                        onCheckedChange = onToggleVip,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldGreen
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CitiesOverviewCard(listings: List<Listing>) {
    val cities = listOf("صفاشهر", "بوانات", "قادرآباد", "پاسارگاد")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("آمار آگهی‌های شهرهای ۴ گانه", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            cities.forEach { cityName ->
                val cityCount = listings.count { it.city.contains(cityName) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(18.dp))
                        Text(cityName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }
                    PersianBadge(
                        text = "${JaarchiViewModel.toPersianDigits(cityCount.toString())} آگهی",
                        containerColor = SoftNavy,
                        contentColor = SoberNavy
                    )
                }
                Divider(color = NeutralLight)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) bgColor.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) iconTint else NeutralLight
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(iconTint)
                    )
                }
            }
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) iconTint else NeutralMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun EmptyQueueCard(
    title: String,
    subtitle: String,
    primaryButtonText: String? = null,
    onPrimaryClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.FactCheck, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(48.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = NeutralMedium, textAlign = TextAlign.Center)

            if (primaryButtonText != null && onPrimaryClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onPrimaryClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SoberNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(primaryButtonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (secondaryButtonText != null && onSecondaryClick != null) {
                OutlinedButton(
                    onClick = onSecondaryClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SoberNavy)
                ) {
                    Text(secondaryButtonText, fontSize = 11.sp, color = SoberNavy)
                }
            }
        }
    }
}

@Composable
fun AdminSecuritySettingsCard(
    onChangePassword: (oldPass: String, newPass: String) -> Boolean,
    onResetData: () -> Unit = {}
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPassVisible by remember { mutableStateOf(false) }
    var newPassVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Key, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(20.dp))
                }
                Text("تغییر رمز عبور اختصاصی پنل مدیریت", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SoberNavy)
            }

            Text(
                text = "جهت حفظ امنیت سامانه و نظارت انحصاری بر آگهی‌های صفاشهر، بوانات، قادرآباد و پاسارگاد، می‌توانید رمز عبور مدیریت را در هر زمان تغییر دهید.",
                fontSize = 11.sp,
                color = DignifiedSlate,
                lineHeight = 17.sp
            )

            HorizontalDivider(color = NeutralLight)

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { 
                    currentPassword = it 
                    errorMessage = null
                    successMessage = null
                },
                label = { Text("رمز عبور فعلی مدیریت") },
                singleLine = true,
                visualTransformation = if (oldPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { oldPassVisible = !oldPassVisible }) {
                        Icon(
                            imageVector = if (oldPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "نمایش رمز"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it 
                    errorMessage = null
                    successMessage = null
                },
                label = { Text("رمز عبور جدید (حداقل ۴ کاراکتر)") },
                singleLine = true,
                visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newPassVisible = !newPassVisible }) {
                        Icon(
                            imageVector = if (newPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "نمایش رمز"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it 
                    errorMessage = null
                    successMessage = null
                },
                label = { Text("تکرار رمز عبور جدید") },
                singleLine = true,
                visualTransformation = if (newPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = CrimsonRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (successMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftGreen,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Text(successMessage!!, color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = {
                    if (currentPassword.isBlank() || newPassword.isBlank()) {
                        errorMessage = "لطفاً رمز عبور فعلی و جدید را وارد کنید."
                        return@Button
                    }
                    if (newPassword.length < 4) {
                        errorMessage = "رمز عبور جدید باید حداقل ۴ کاراکتر باشد."
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        errorMessage = "رمز عبور جدید با تکرار آن یکسان نیست!"
                        return@Button
                    }

                    val success = onChangePassword(currentPassword, newPassword)
                    if (success) {
                        successMessage = "رمز عبور مدیریت با موفقیت تغییر یافت و ذخیره شد ✓"
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        errorMessage = null
                    } else {
                        errorMessage = "رمز عبور فعلی نادرست است."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
            ) {
                Icon(Icons.Filled.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ثبت و تغییر رمز عبور", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            HorizontalDivider(color = NeutralLight, modifier = Modifier.padding(vertical = 4.dp))

            // Persistence Guarantee Information
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftGreen.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.CloudDone, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                    Column {
                        Text("ذخیره‌سازی دائمی تمام تغییرات مدیریت", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldGreen)
                        Text("تمام عملیات حذف یا تایید آگهی‌ها، تغییر نام برنامه، رمز عبور و تنظیمات آپدیت به صورت دائمی در حافظه ذخیره می‌شوند و با بستن برنامه حفظ خواهند شد.", fontSize = 10.sp, color = DignifiedSlate, lineHeight = 15.sp)
                    }
                }
            }

            // Factory Reset Option
            OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed)
            ) {
                Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("بازنشانی اطلاعات اولیه (Reset to Defaults)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("بازنشانی به تنظیمات پیش‌فرض؟", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            },
            text = {
                Text("آیا از بازگردانی آگهی‌ها و تنظیمات به حالت اولیه اولیه مطمئن هستید؟", fontSize = 12.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("بله، بازنشانی شود")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun AppIdentitySettingsCard(
    currentAppName: String,
    currentEnglishName: String,
    onUpdateAppName: (faName: String, enName: String) -> Boolean
) {
    var faNameInput by remember(currentAppName) { mutableStateOf(currentAppName) }
    var enNameInput by remember(currentEnglishName) { mutableStateOf(currentEnglishName) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("مدیریت و تغییر نام سامانه", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SoberNavy)
                    Text("نام فعلی: $currentAppName | $currentEnglishName", fontSize = 11.sp, color = NeutralMedium)
                }
            }

            Text(
                text = "مدیریت محترم می‌تواند عنوان و برند اپلیکیشن (مانند «جارچی» یا هر عنوان دلخواه دیگر) را تغییر دهد. این نام بلافاصله در بالای برنامه، هدر صفحات، و متن پیام‌های اشتراک‌گذاری به‌روزرسانی خواهد شد.",
                fontSize = 11.sp,
                color = DignifiedSlate,
                lineHeight = 17.sp
            )

            HorizontalDivider(color = NeutralLight)

            OutlinedTextField(
                value = faNameInput,
                onValueChange = { 
                    faNameInput = it 
                    errorMsg = null
                    successMsg = null
                },
                label = { Text("نام فارسی برنامه") },
                placeholder = { Text("مثلاً: جارچی، شیپور شهر، بازارچه") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Store, contentDescription = null, tint = SoberNavy)
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = enNameInput,
                onValueChange = { 
                    enNameInput = it 
                    errorMsg = null
                    successMsg = null
                },
                label = { Text("نام انگلیسی / عنوان مکمل") },
                placeholder = { Text("مثلاً: Jaarchi") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Abc, contentDescription = null, tint = SoberNavy)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Preset Quick Ideas
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("پیشنهادهای سریع برای نامگذاری:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeutralDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "جارچی" to "Jaarchi",
                        "بازارچه ۴ شهر" to "4-City Market",
                        "دیوار منطقه" to "Regional Ads",
                        "نیازمندی‌های فارس" to "Fars Ads"
                    )
                    presets.forEach { (fa, en) ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    faNameInput = fa
                                    enNameInput = en
                                    errorMsg = null
                                    successMsg = null
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = SoftNavy,
                            border = BorderStroke(1.dp, SoberNavy.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = fa,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SoberNavy,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = CrimsonRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (successMsg != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftGreen,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Text(successMsg!!, color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = {
                    if (faNameInput.isBlank()) {
                        errorMsg = "لطفاً نام فارسی برنامه را وارد کنید."
                        return@Button
                    }
                    val ok = onUpdateAppName(faNameInput, enNameInput)
                    if (ok) {
                        successMsg = "نام برنامه با موفقیت به «${faNameInput.trim()}» تغییر یافت ✓"
                        errorMsg = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ذخیره و اعمال نام جدید برنامه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun AdminUpdateSettingsDashboardCard(
    updateInfo: com.example.model.AppUpdateInfo,
    onSaveUpdateSettings: (version: String, active: Boolean, force: Boolean, note: String) -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    var isActive by remember(updateInfo.isUpdateFeatureActive) { mutableStateOf(updateInfo.isUpdateFeatureActive) }
    var latestVersion by remember(updateInfo.latestVersion) { mutableStateOf(updateInfo.latestVersion) }
    var adminNote by remember(updateInfo.adminNote) { mutableStateOf(updateInfo.adminNote) }
    var isForce by remember(updateInfo.isForceUpdate) { mutableStateOf(updateInfo.isForceUpdate) }
    var savedSuccess by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isActive) EmeraldGreen.copy(alpha = 0.5f) else CrimsonRed.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.SystemUpdateAlt,
                        contentDescription = null,
                        tint = if (isActive) EmeraldGreen else CrimsonRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text("مدیریت و کنترل وضعیت به‌روزرسانی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("فعال یا غیرفعال‌سازی دسترسی کاربران به آپدیت جدید", fontSize = 10.sp, color = NeutralMedium)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) SoftGreen else SoftCrimson
                ) {
                    Text(
                        text = if (isActive) "فعال برای کاربران" else "غیرفعال توسط مدیر",
                        color = if (isActive) EmeraldGreen else CrimsonRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Main Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isActive) SoftGreen.copy(alpha = 0.4f) else SoftCrimson.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isActive) "سیستم بروزرسانی فعال است" else "سیستم بروزرسانی غیرفعال شد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isActive) EmeraldGreen else CrimsonRed
                        )
                        Text(
                            text = if (isActive)
                                "تمام کاربران در صفحه پروفایل و اعلانات پیام آپدیت به نسخه $latestVersion را دریافت می‌کنند."
                            else
                                "دریافت آپدیت متوقف است و کاربران مطلع می‌شوند که نسخه جدیدی ارائه نشده است.",
                            fontSize = 10.sp,
                            color = NeutralDark
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = {
                            isActive = it
                            savedSuccess = false
                            onToggleStatus(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGreen,
                            checkedTrackColor = SoftGreen,
                            uncheckedThumbColor = CrimsonRed,
                            uncheckedTrackColor = SoftCrimson
                        )
                    )
                }
            }

            OutlinedTextField(
                value = latestVersion,
                onValueChange = {
                    latestVersion = it
                    savedSuccess = false
                },
                label = { Text("شماره آخرین نسخه منتشر شده") },
                leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null, tint = SoberNavy) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = adminNote,
                onValueChange = {
                    adminNote = it
                    savedSuccess = false
                },
                label = { Text("پیام و اطلاعیه مدیریت برای کاربران") },
                leadingIcon = { Icon(Icons.Filled.Announcement, contentDescription = null, tint = SoberNavy) },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("بروزرسانی اجباری (Force Update)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("الزام کاربران به دانلود آخرین نسخه قبل از استفاده", fontSize = 10.sp, color = NeutralMedium)
                }
                Switch(
                    checked = isForce,
                    onCheckedChange = {
                        isForce = it
                        savedSuccess = false
                    }
                )
            }

            if (savedSuccess) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftGreen,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Text("تنظیمات با موفقیت ذخیره و در سرور اعمال گردید ✓", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = {
                    onSaveUpdateSettings(latestVersion, isActive, isForce, adminNote)
                    savedSuccess = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ذخیره و انتشار تنظیمات بروزرسانی", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CloudSyncManagementCard(
    uiState: JaarchiUiState,
    onSyncNow: () -> Unit,
    onForcePush: () -> Unit,
    onForcePull: () -> Unit,
    onPurgeOfflineData: () -> Unit = {},
    onSetCustomUrl: (String?) -> Unit,
    onSpeedTest: () -> Unit = {}
) {
    var customUrlInput by remember { mutableStateOf("") }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, if (uiState.isCloudConnected) EmeraldGreen.copy(alpha = 0.5f) else AmberAccent.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isCloudConnected) SoftGreen else SoftAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CloudSync,
                            contentDescription = null,
                            tint = if (uiState.isCloudConnected) EmeraldGreen else AmberAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text("مرکز کنترل سرورهای ابری فوق‌سریع", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("خوشه ابری توزیع‌شده با اتصال لحظه‌ای تمام دستگاه‌ها", fontSize = 10.sp, color = NeutralMedium)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (uiState.isCloudConnected) SoftGreen else SoftAmber
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isCloudConnected) EmeraldGreen else AmberAccent)
                        )
                        Text(
                            text = if (uiState.isCloudSyncing) "در حال هماهنگی..." else if (uiState.isCloudConnected) "متصل ⚡" else "تلاش مجدد ⚠️",
                            color = if (uiState.isCloudConnected) EmeraldGreen else NeutralDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "سامانه رسانه آریا به سریع‌ترین شبکه ابری توزیع‌شده مجهز است. با ثبت یا تایید هر آگهی و انتشار آپدیت جدید، اطلاعات به صورت زنده و آنلاین در تمام گوشی‌ها همگام‌سازی می‌گردد.",
                fontSize = 11.sp,
                color = DignifiedSlate,
                lineHeight = 18.sp
            )

            // Status Metrics & Latency
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftNavy.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("سرور فعال ابری:", fontSize = 11.sp, color = NeutralDark)
                        Text(uiState.cloudProviderName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoberNavy)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("پینگ و تاخیر اتصال:", fontSize = 11.sp, color = NeutralDark)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SoftGreen
                        ) {
                            Text(
                                text = "⚡ ${uiState.cloudLatencyMs} میلی‌ثانیه (فوق‌العاده سریع)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("آخرین زمان همگام‌سازی:", fontSize = 11.sp, color = NeutralDark)
                        Text(uiState.lastCloudSyncTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تعداد کل آگهی‌های آنلاین:", fontSize = 11.sp, color = NeutralDark)
                        Text("${uiState.listings.size} آگهی زنده ابری", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoberNavy)
                    }
                }
            }

            // Quick Sync & Speed Test Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSyncNow,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
                ) {
                    if (uiState.isCloudSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("بروزرسانی زنده", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSpeedTest,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Speed, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تست سرعت ابری", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoberNavy)
                }
            }

            // Dedicated Purge Offline & Direct Cloud Sync Action
            Button(
                onClick = onPurgeOfflineData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Icon(Icons.Filled.CloudDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حذف داده‌های آفلاین و اتصال مستقیم به پایگاه ابری زنده", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
                onClick = onForcePush,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ارسال مستقیم و یکپارچه تمامی آگهی‌ها به سرور ابری", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoberNavy)
            }

            // Advanced Cloud Config Dropdown
            HorizontalDivider(color = NeutralLight)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdvancedSettings = !showAdvancedSettings }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تنظیمات پیشرفته اتصال سرور اختصاصی", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NeutralDark)
                Icon(
                    if (showAdvancedSettings) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = NeutralMedium
                )
            }

            if (showAdvancedSettings) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("آدرس سرور اختصاصی (اختیاری)") },
                        placeholder = { Text("https://my-api-server.com/ads") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSetCustomUrl(customUrlInput.ifBlank { null }) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeutralDark)
                    ) {
                        Text("اعمال سرور اختصاصی", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNotificationsPermissionsCard(
    context: android.content.Context,
    onRequestAllPermissions: () -> Unit,
    onSendTestNotification: () -> Unit,
    onSendBroadcastPush: (title: String, message: String) -> Unit,
    onOpenSettings: () -> Unit
) {
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }
    var broadcastSentSuccess by remember { mutableStateOf(false) }
    var permissionsList by remember { mutableStateOf(PermissionHelper.getAllPermissionsStatus(context)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, RoyalBlue.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Color.White)
                    }
                    Column {
                        Text("مرکز اعلان‌ها و مجوزهای موبایل", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SoberNavy)
                        Text("مدیریت دسترسی‌های دستگاه و ارسال نوتیفیکیشن", fontSize = 10.sp, color = NeutralMedium)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftBlue
                ) {
                    Text(
                        "سامانه فعال",
                        color = RoyalBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Action: Request All Permissions & Test
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRequestAllPermissions,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فعال‌سازی همگانی مجوزها", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSendTestNotification,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تست اعلان فوری 🔔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                }
            }

            // Permissions Status List
            Text("وضعیت دسترسی‌های فعال روی گوشی موبایل:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SoberNavy)

            permissionsList.forEach { perm ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (perm.isGranted) SoftGreen.copy(alpha = 0.4f) else SoftAmber.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (perm.isGranted) EmeraldGreen.copy(alpha = 0.5f) else AmberAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (perm.isGranted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (perm.isGranted) EmeraldGreen else AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(perm.titleFa, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = SoberNavy)
                                Text(perm.descriptionFa, fontSize = 9.sp, color = NeutralMedium, maxLines = 1)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (perm.isGranted) EmeraldGreen else AmberAccent
                        ) {
                            Text(
                                if (perm.isGranted) "فعال ✓" else "غیرفعال",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // App Settings button
            TextButton(
                onClick = onOpenSettings,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("باز کردن صفحه تنظیمات سیستمی اپ در گوشی ⚙️", fontSize = 11.sp, color = SoberNavy)
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Broadcast Instant Push Notification to Users
            Text("ارسال اعلان همگانی (Push Notification) به همه کاربران:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SoberNavy)

            OutlinedTextField(
                value = broadcastTitle,
                onValueChange = { broadcastTitle = it },
                label = { Text("عنوان اعلان همگانی") },
                placeholder = { Text("مثال: تخفیف ویژه ۵۰٪ ثبت آگهی نردبان در صفاشهر و بوانات") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = broadcastMessage,
                onValueChange = { broadcastMessage = it },
                label = { Text("متن اعلان پیامکی/نوتیفیکیشن") },
                placeholder = { Text("متن اطلاعیه یا پیام مهم مدیریت جهت نمایش در نوار وضعیت کاربران...") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = {
                    if (broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank()) {
                        onSendBroadcastPush(broadcastTitle.trim(), broadcastMessage.trim())
                        broadcastSentSuccess = true
                        broadcastTitle = ""
                        broadcastMessage = ""
                    }
                },
                enabled = broadcastTitle.isNotBlank() && broadcastMessage.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
            ) {
                Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ارسال فوری اعلان به گوشی کاربران 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (broadcastSentSuccess) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftGreen,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "✓ اعلان با موفقیت به دستگاه و سرور ابری ارسال گردید.",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

