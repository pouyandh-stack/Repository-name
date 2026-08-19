package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PersianBadge
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import com.example.util.PermissionHelper
import com.example.viewmodel.JaarchiUiState
import com.example.viewmodel.JaarchiViewModel

@Composable
fun ProfileScreen(
    uiState: JaarchiUiState,
    viewModel: JaarchiViewModel,
    onNavigateToSupport: () -> Unit,
    onNavigateToPostAd: () -> Unit
) {
    val context = LocalContext.current
    var showActivationDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminLoginErrorMsg by remember { mutableStateOf<String?>(null) }

    // Launcher for requesting all permissions
    val allPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        NotificationHelper.createNotificationChannels(context)
    }

    // Admin Update Control local state (synced with uiState)
    var adminUpdateActive by remember(uiState.appUpdateInfo.isUpdateFeatureActive) {
        mutableStateOf(uiState.appUpdateInfo.isUpdateFeatureActive)
    }
    var adminLatestVersionInput by remember(uiState.appUpdateInfo.latestVersion) {
        mutableStateOf(uiState.appUpdateInfo.latestVersion)
    }
    var adminNoteInput by remember(uiState.appUpdateInfo.adminNote) {
        mutableStateOf(uiState.appUpdateInfo.adminNote)
    }
    var adminForceUpdate by remember(uiState.appUpdateInfo.isForceUpdate) {
        mutableStateOf(uiState.appUpdateInfo.isForceUpdate)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("profile_screen_list"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isAdminLoggedIn) AmberAccent else SoberNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (uiState.isAdminLoggedIn) Icons.Filled.AdminPanelSettings else Icons.Filled.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (uiState.isAdminLoggedIn) "مدیر ارشد سامانه" else "حساب کاربری",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (uiState.isAdminLoggedIn) {
                                Surface(
                                    color = AmberAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "دسترسی مدیریت فعال",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoberNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text("شماره تماس: ${uiState.verifiedUserPhone.ifEmpty { "ثبت شده در آگهی‌ها" }}", fontSize = 12.sp, color = NeutralMedium)
                        Text("شهرهای تحت پوشش: صفاشهر، بوانات، قادرآباد، پاسارگاد", fontSize = 10.sp, color = SoberNavy)
                    }
                }
            }
        }

        // ==========================================
        // ADMIN UPDATE CONTROL PANEL (پنل مدیریت بروزرسانی)
        // ==========================================
        if (uiState.isAdminLoggedIn) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, if (adminUpdateActive) EmeraldGreen else CrimsonRed),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Icon(
                                    Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = if (adminUpdateActive) EmeraldGreen else CrimsonRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        "پنل مدیریت بروزرسانی اپلیکیشن",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SoberNavy
                                    )
                                    Text(
                                        "کنترل اختصاصی انتشار نسخه و فعال/غیرفعال بودن",
                                        fontSize = 10.sp,
                                        color = NeutralMedium
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (adminUpdateActive) SoftGreen else SoftCrimson
                            ) {
                                Text(
                                    text = if (adminUpdateActive) "● بروزرسانی فعال" else "○ غیرفعال شده",
                                    color = if (adminUpdateActive) EmeraldGreen else CrimsonRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        // Switch: Enable/Disable Update feature for all users
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (adminUpdateActive) SoftGreen.copy(alpha = 0.5f) else SoftCrimson.copy(alpha = 0.5f)
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
                                        "وضعیت انتشار بروزرسانی برای کاربران",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (adminUpdateActive) EmeraldGreen else CrimsonRed
                                    )
                                    Text(
                                        if (adminUpdateActive)
                                            "کاربران اعلان آپدیت جدید را می‌بینند و می‌توانند برنامه را بروزرسانی کنند."
                                        else
                                            "امکان بروزرسانی متوقف شده و به کاربران اعلام می‌شود آپدیت موقتاً غیرفعال است.",
                                        fontSize = 10.sp,
                                        color = NeutralDark
                                    )
                                }
                                Switch(
                                    checked = adminUpdateActive,
                                    onCheckedChange = {
                                        adminUpdateActive = it
                                        viewModel.setUpdateFeatureStatus(it)
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

                        // Version Setting Field
                        OutlinedTextField(
                            value = adminLatestVersionInput,
                            onValueChange = { adminLatestVersionInput = it },
                            label = { Text("شماره آخرین نسخه منتشر شده (مثلاً 1.2.1)") },
                            leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null, tint = SoberNavy) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Admin Note / Announcement Field
                        OutlinedTextField(
                            value = adminNoteInput,
                            onValueChange = { adminNoteInput = it },
                            label = { Text("پیام و توضیحات مدیر برای کاربران") },
                            leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null, tint = SoberNavy) },
                            maxLines = 2,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Force Update Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("الزام به بروزرسانی (آپدیت اجباری)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("در صورت فعال بودن، کاربران تا زمان آپدیت نمی‌توانند ادامه دهند.", fontSize = 10.sp, color = NeutralMedium)
                            }
                            Switch(
                                checked = adminForceUpdate,
                                onCheckedChange = { adminForceUpdate = it }
                            )
                        }

                        // Save Configuration Button
                        Button(
                            onClick = {
                                viewModel.updateAppVersionConfig(
                                    latestVersion = adminLatestVersionInput,
                                    isActive = adminUpdateActive,
                                    isForce = adminForceUpdate,
                                    adminNote = adminNoteInput
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoberNavy),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ذخیره و اعمال تنظیمات بروزرسانی توسط مدیریت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // USER-FACING IN-APP UPDATE CARD / BANNER
        // ==========================================
        item {
            val isFeatureActive = uiState.appUpdateInfo.isUpdateFeatureActive
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFeatureActive) SoftGreen else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    if (isFeatureActive) EmeraldGreen.copy(alpha = 0.4f) else AmberAccent.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isFeatureActive) EmeraldGreen else AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFeatureActive) Icons.Filled.SystemUpdate else Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isFeatureActive) "به‌روزرسانی اپلیکیشن" else "وضعیت به‌روزرسانی (متوقف توسط مدیر)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isFeatureActive) EmeraldGreen else AmberAccent
                                )
                            }
                            Text(
                                text = if (isFeatureActive)
                                    "نسخه فعلی: ${uiState.appUpdateInfo.currentVersion} • آخرین نسخه: ${uiState.appUpdateInfo.latestVersion}"
                                else
                                    "سیستم بروزرسانی موقتاً توسط مدیریت غیرفعال شده است.",
                                fontSize = 11.sp,
                                color = NeutralDark
                            )
                        }

                        Button(
                            onClick = { showUpdateDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFeatureActive) EmeraldGreen else SoberNavy
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (isFeatureActive) "بررسی آپدیت" else "مشاهده وضعیت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // If admin is not logged in, provide quick login button to control update status
                    if (!uiState.isAdminLoggedIn) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftNavy.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAdminLoginDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(14.dp))
                                    Text("ورود به عنوان مدیر جهت فعال/غیرفعال‌سازی بروزرسانی", fontSize = 10.sp, color = SoberNavy, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // Quick Settings Group
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Mobile Permissions & Live Notifications Hub
                    ProfileMenuItem(
                        icon = Icons.Outlined.NotificationsActive,
                        title = "مدیریت اعلان‌ها و تمامی مجوزهای برنامه",
                        subtitle = "فعال‌سازی اعلان‌های تایید آگهی، پیام‌ها، نقشه و دوربین",
                        onClick = { showPermissionsDialog = true }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    ProfileMenuItem(
                        icon = Icons.Outlined.CloudSync,
                        title = "اتصال پایدار و زنده بین تمامی گوشی‌ها",
                        subtitle = "متصل به سرور ابری Anycast (بدون نیاز به لایسنس یا محدودیت)",
                        onClick = { viewModel.syncWithCloud(showToast = true) }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    ProfileMenuItem(
                        icon = Icons.Outlined.HeadsetMic,
                        title = "پشتیبانی و تیکت آنلاین",
                        subtitle = "ارتباط مستقیم با کارشناسان و هوش مصنوعی",
                        onClick = onNavigateToSupport
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    ProfileMenuItem(
                        icon = Icons.Outlined.Bookmark,
                        title = "آگهی‌های نشان‌شده",
                        subtitle = "${JaarchiViewModel.toPersianDigits(uiState.savedAdsCount.toString())} آگهی ذخیره شده",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("حالت تاریک (Dark Mode)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme() }
                        )
                    }
                }
            }
        }

        // App Information Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("سامانه هوشمند نیازمندی‌های ${uiState.appName} | نسخه ${uiState.appUpdateInfo.currentVersion}", fontSize = 11.sp, color = NeutralMedium)
                Text("اتصال ابری زنده و پایدار بین تمامی گوشی‌های همراه (بدون محدودیت لایسنس)", fontSize = 10.sp, color = NeutralMedium)
            }
        }
    }

    // In-App Update Dialog
    if (showUpdateDialog) {
        val isFeatureActive = uiState.appUpdateInfo.isUpdateFeatureActive
        AlertDialog(
            onDismissRequest = { if (!uiState.isDownloadingUpdate) showUpdateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (isFeatureActive) Icons.Filled.SystemUpdate else Icons.Filled.CloudOff,
                        contentDescription = null,
                        tint = if (isFeatureActive) EmeraldGreen else AmberAccent
                    )
                    Text("مدیریت به‌روزرسانی اپلیکیشن", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!isFeatureActive) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftAmber,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("وضعیت: غیرفعال توسط مدیریت", fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 12.sp)
                                Text(uiState.appUpdateInfo.adminNote, fontSize = 11.sp, color = NeutralDark)
                            }
                        }
                    }

                    Text("نسخه فعلی شما: ${uiState.appUpdateInfo.currentVersion}", fontWeight = FontWeight.SemiBold)
                    Text("آخرین نسخه سرور: ${uiState.appUpdateInfo.latestVersion}", fontWeight = FontWeight.Bold, color = RoyalBlue)

                    Text("تغییرات و امکانات جدید این نسخه:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    uiState.appUpdateInfo.changeLog.forEach { item ->
                        Text("• $item", fontSize = 11.sp, color = NeutralDark)
                    }

                    if (uiState.isDownloadingUpdate) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { uiState.updateProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = EmeraldGreen
                            )
                            Text("در حال دانلود و نصب بسته به‌روزرسانی (${(uiState.updateProgress * 100).toInt()}٪)...", fontSize = 11.sp, color = NeutralMedium)
                        }
                    } else if (uiState.isUpdateInstalled || !uiState.appUpdateInfo.isUpdateAvailable) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftGreen,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ برنامه شما به آخرین نسخه (${uiState.appUpdateInfo.currentVersion}) به‌روزرسانی شده است.",
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (isFeatureActive) {
                    Button(
                        onClick = {
                            viewModel.checkAndStartAppUpdate()
                        },
                        enabled = !uiState.isDownloadingUpdate,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text(if (uiState.appUpdateInfo.isUpdateAvailable) "دریافت و بروزرسانی مستقیم" else "بروزرسانی مجدد / بررسی مجدد")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUpdateDialog = false },
                    enabled = !uiState.isDownloadingUpdate
                ) {
                    Text("بستن")
                }
            }
        )
    }

    // Admin Quick Login Dialog (From Profile)
    if (showAdminLoginDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminLoginDialog = false
                adminPinInput = ""
                adminLoginErrorMsg = null
            },
            icon = {
                Icon(Icons.Filled.Security, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("ورود به پنل مدیریت", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("جهت فعال یا غیرفعال کردن سیستم بروزرسانی، رمز عبور مدیریت را وارد فرمایید:")
                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            adminLoginErrorMsg = null
                        },
                        label = { Text("رمز مدیریت (پیش‌فرض: Alpn52)") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = SoberNavy) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (adminLoginErrorMsg != null) {
                        Text(adminLoginErrorMsg!!, color = CrimsonRed, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.loginAdmin(adminPinInput)
                        if (success) {
                            showAdminLoginDialog = false
                            adminPinInput = ""
                            adminLoginErrorMsg = null
                        } else {
                            adminLoginErrorMsg = "رمز عبور مدیریت نادرست است."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
                ) {
                    Text("ورود و نمایش پنل مدیریت")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminLoginDialog = false
                        adminPinInput = ""
                        adminLoginErrorMsg = null
                    }
                ) {
                    Text("انصراف")
                }
            }
        )
    }

    // Activation Code Dialog
    if (showActivationDialog) {
        var code by remember { mutableStateOf(uiState.deviceActivationKey) }
        AlertDialog(
            onDismissRequest = { showActivationDialog = false },
            title = { Text("رمز اختصاصی دستگاه", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("هر دستگاه در هنگام نصب یک رمز اختصاصی دریافت می‌کند:")
                    Card(colors = CardDefaults.cardColors(containerColor = SoftBlue), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "رمز شما: ${uiState.deviceActivationKey}",
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("ورود یا تایید رمز") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank()) {
                            viewModel.verifyActivationCode(code)
                            showActivationDialog = false
                        }
                    }
                ) {
                    Text("فعال‌سازی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActivationDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Permissions & Notifications Management Dialog
    if (showPermissionsDialog) {
        PermissionsManagementDialog(
            context = context,
            onRequestAllPermissions = {
                allPermissionsLauncher.launch(PermissionHelper.getAllRequiredPermissions())
            },
            onTestNotification = {
                viewModel.triggerTestNotification()
            },
            onOpenSettings = {
                PermissionHelper.openAppSettings(context)
            },
            onDismiss = { showPermissionsDialog = false }
        )
    }
}

@Composable
fun PermissionsManagementDialog(
    context: android.content.Context,
    onRequestAllPermissions: () -> Unit,
    onTestNotification: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    var permissionsList by remember { mutableStateOf(PermissionHelper.getAllPermissionsStatus(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = RoyalBlue)
                Text("مدیریت اعلان‌ها و تمامی مجوزهای گوشی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "برای استفاده کامل از قابلیت‌های رسانه آریا (دریافت اعلان تایید آگهی، نقشه هوشمند، عکاسی و تماس مستقیم)، مجوزهای زیر را فعال نمایید:",
                        fontSize = 12.sp,
                        color = NeutralDark,
                        lineHeight = 18.sp
                    )
                }

                items(permissionsList) { perm ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (perm.isGranted) SoftGreen.copy(alpha = 0.6f) else SoftAmber.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, if (perm.isGranted) EmeraldGreen else AmberAccent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (perm.isGranted) EmeraldGreen else AmberAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (perm.isGranted) Icons.Filled.Check else Icons.Filled.PriorityHigh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(perm.titleFa, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SoberNavy)
                                Text(perm.descriptionFa, fontSize = 10.sp, color = NeutralMedium, lineHeight = 14.sp)
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (perm.isGranted) EmeraldGreen else AmberAccent
                            ) {
                                Text(
                                    text = if (perm.isGranted) "فعال ✓" else "غیرفعال",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Test notification action button
                    OutlinedButton(
                        onClick = onTestNotification,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ارسال یک اعلان تستی به گوشی 🔔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRequestAllPermissions()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("درخواست و فعال‌سازی همه مجوزها", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onOpenSettings) {
                Text("تنظیمات سیستمی گوشی ⚙️", fontSize = 11.sp)
            }
        }
    )
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = NeutralMedium)
        }
        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = NeutralMedium)
    }
}
