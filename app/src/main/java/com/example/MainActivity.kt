package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Listing
import com.example.ui.components.AppDestination
import com.example.ui.components.JaarchiBottomNav
import com.example.ui.components.JaarchiTopBar
import com.example.ui.screens.*
import com.example.ui.theme.JaarchiTheme
import com.example.util.NotificationHelper
import com.example.viewmodel.JaarchiViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: JaarchiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            LaunchedEffect(uiState.toastMessage) {
                uiState.toastMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    viewModel.clearToast()
                }
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                JaarchiTheme(darkTheme = uiState.isDarkTheme) {
                var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
                var showSupportChat by remember { mutableStateOf(false) }
                var initialPaymentListing by remember { mutableStateOf<Listing?>(null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val shareAppAction = {
                        val sendIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, viewModel.getAppShareText())
                            type = "text/plain"
                        }
                        val shareIntent = android.content.Intent.createChooser(sendIntent, "ارسال و معرفی برنامه جارچی به دیگران")
                        context.startActivity(shareIntent)
                    }

                    if (showSupportChat) {
                        SupportChatScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onBack = { showSupportChat = false }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                JaarchiTopBar(
                                    title = when (currentDestination) {
                                        AppDestination.HOME -> "${uiState.appName} | ${uiState.appEnglishName}"
                                        AppDestination.POST_AD -> "ثبت و انتشار آگهی"
                                        AppDestination.DASHBOARD -> "مدیریت و نظارت بر آگهی‌ها"
                                        AppDestination.PROFILE -> "پروفایل کاربری"
                                    },
                                    subtitle = when (currentDestination) {
                                        AppDestination.HOME -> "پلتفرم هوشمند نیازمندی‌ها"
                                        AppDestination.POST_AD -> "احراز هویت پیامکی، عکس و نقشه"
                                        AppDestination.DASHBOARD -> "پنل نظارت و پیام‌رسانی مدیریت"
                                        AppDestination.PROFILE -> "حساب کاربری و تنظیمات"
                                    },
                                    isVip = uiState.isVipActivated,
                                    selectedCity = uiState.selectedCity,
                                    availableCities = viewModel.availableCities,
                                    onCitySelected = if (currentDestination == AppDestination.HOME) { { viewModel.selectCity(it) } } else null,
                                    onShareClick = shareAppAction,
                                    onSupportClick = { showSupportChat = true }
                                )
                            },
                            bottomBar = {
                                JaarchiBottomNav(
                                    currentDestination = currentDestination,
                                    onNavigate = { currentDestination = it }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentDestination) {
                                    AppDestination.HOME -> HomeScreen(
                                        uiState = uiState,
                                        viewModel = viewModel
                                    )
                                    AppDestination.POST_AD -> PostAdScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onAdPublishedSuccess = { currentDestination = AppDestination.HOME }
                                    )
                                    AppDestination.DASHBOARD -> DashboardScreen(
                                        uiState = uiState,
                                        viewModel = viewModel
                                    )
                                    AppDestination.PROFILE -> ProfileScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onNavigateToSupport = { showSupportChat = true },
                                        onNavigateToPostAd = { currentDestination = AppDestination.POST_AD }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
