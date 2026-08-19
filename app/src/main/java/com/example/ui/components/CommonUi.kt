package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

enum class AppDestination(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME("home", "آگهی‌ها", Icons.Outlined.Storefront, Icons.Filled.Storefront),
    POST_AD("post_ad", "ثبت آگهی", Icons.Outlined.AddCircleOutline, Icons.Filled.AddCircle),
    DASHBOARD("dashboard", "مدیریت", Icons.Outlined.AdminPanelSettings, Icons.Filled.AdminPanelSettings),
    PROFILE("profile", "پروفایل", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JaarchiTopBar(
    title: String,
    subtitle: String? = null,
    isVip: Boolean = false,
    selectedCity: String = "همه شهرها",
    availableCities: List<String> = listOf("همه شهرها", "صفاشهر", "بوانات", "قادرآباد", "پاسارگاد"),
    onCitySelected: ((String) -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onSupportClick: (() -> Unit)? = null
) {
    var showCityDialog by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = Modifier.testTag("jaarchi_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.classic_app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isVip) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AmberAccent,
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = "VIP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        // Admin contact email under Jaarchi title as requested
                        Text(
                            text = "Aliaghili1353@gmail.com",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.90f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // City Selector Button opposite the Jaarchi text
                if (onCitySelected != null) {
                    Surface(
                        onClick = { showCityDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("top_bar_city_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationCity,
                                contentDescription = "انتخاب شهر",
                                tint = AmberAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = selectedCity,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        actions = {
            if (onShareClick != null) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("top_bar_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "معرفی و ارسال برنامه به دوستان"
                    )
                }
            }
            if (onSupportClick != null) {
                IconButton(
                    onClick = onSupportClick,
                    modifier = Modifier.testTag("top_bar_support_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.HeadsetMic,
                        contentDescription = "پشتیبانی و چت"
                    )
                }
            }
        }
    )

    // City Selection Dialog (Safashahr, Bavanat, Qaderabad, Pasargad, All Cities)
    if (showCityDialog && onCitySelected != null) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SoftNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text("انتخاب شهر آگهی‌ها", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "شهر مورد نظر خود را جهت مشاهده آگهی‌ها انتخاب کنید:",
                        fontSize = 12.sp,
                        color = NeutralMedium
                    )

                    availableCities.forEach { city ->
                        val isSelected = city == selectedCity
                        Surface(
                            onClick = {
                                onCitySelected(city)
                                showCityDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SoftNavy else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SoberNavy) else null,
                            tonalElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (city == "همه شهرها") Icons.Filled.Public else Icons.Filled.Place,
                                        contentDescription = null,
                                        tint = if (isSelected) SoberNavy else NeutralMedium,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = city,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SoberNavy else NeutralDark,
                                        fontSize = 14.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("بستن", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun JaarchiBottomNav(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("jaarchi_bottom_navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        AppDestination.entries.forEach { destination ->
            val isSelected = currentDestination == destination
            NavigationBarItem(
                modifier = Modifier.testTag("nav_item_${destination.route}"),
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.icon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SoberNavy,
                    selectedTextColor = SoberNavy,
                    indicatorColor = SoftNavy,
                    unselectedIconColor = NeutralMedium,
                    unselectedTextColor = NeutralMedium
                )
            )
        }
    }
}

@Composable
fun PersianBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
