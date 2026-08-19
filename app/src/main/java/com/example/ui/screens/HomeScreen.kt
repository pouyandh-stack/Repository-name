package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CategoryType
import com.example.model.Listing
import com.example.model.LocationPoint
import com.example.ui.components.InteractiveLocationPickerMap
import com.example.ui.components.PersianBadge
import com.example.ui.components.openGoogleMapsDirections
import com.example.ui.theme.*
import com.example.viewmodel.JaarchiUiState
import com.example.viewmodel.JaarchiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: JaarchiUiState,
    viewModel: JaarchiViewModel
) {
    var showCreateAdDialog by remember { mutableStateOf(false) }
    var selectedListingForDetail by remember { mutableStateOf<Listing?>(null) }
    var isCitiesAccordionExpanded by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_list"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Search Bar Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("جستجو در بین آگهی‌های «${uiState.appName}»...", fontSize = 13.sp, color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "جستجو", tint = SoberNavy) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "پاک کردن")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cloud Sync Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isCloudSyncing) AmberAccent else EmeraldGreen)
                            )
                            Text(
                                text = if (uiState.isCloudSyncing) "در حال همگام‌سازی با سرور ابری..." 
                                       else "فضای ابری آنلاین متصل است 🌐",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier.clickable { viewModel.syncWithCloud(showToast = true) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (uiState.isCloudSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.8.dp, color = Color.White)
                                } else {
                                    Icon(Icons.Filled.Sync, contentDescription = "همگام‌سازی", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                Text("بروزرسانی آگهی‌ها", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Online App Update Banner (Prominently notify users about version 12.1)
            if (uiState.appUpdateInfo.isUpdateAvailable && 
                uiState.appUpdateInfo.isUpdateFeatureActive && 
                uiState.appUpdateInfo.currentVersion != uiState.appUpdateInfo.latestVersion
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { showUpdateDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGreen),
                        border = BorderStroke(1.5.dp, EmeraldGreen)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.SystemUpdate,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "بروزرسانی آنلاین به نسخه ${uiState.appUpdateInfo.latestVersion}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = EmeraldGreen
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldGreen
                                        ) {
                                            Text(
                                                "جدید",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "بروزرسانی زنده تمام فراوین و هماهنگی آنلاین ابری",
                                        fontSize = 10.sp,
                                        color = NeutralDark
                                    )
                                }
                            }

                            Button(
                                onClick = { showUpdateDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("دریافت آپدیت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Vertical Expandable City Selection Accordion (Safashahr, Bavanat, Qaderabad, Pasargad)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("vertical_city_selector_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCitiesAccordionExpanded) MaterialTheme.colorScheme.surface else SoftNavy
                    ),
                    border = BorderStroke(1.dp, if (isCitiesAccordionExpanded) SoberNavy.copy(alpha = 0.4f) else NeutralLight)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Accordion Header (Click to Open/Close)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isCitiesAccordionExpanded = !isCitiesAccordionExpanded }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
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
                                        .background(SoberNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.LocationCity,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "فیلتر بر اساس شهر",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SoberNavy
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (uiState.selectedCity == "همه شهرها") NeutralLight else AmberAccent.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = uiState.selectedCity,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (uiState.selectedCity == "همه شهرها") NeutralDark else SoberNavy,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isCitiesAccordionExpanded) "برای انتخاب روی شهر مورد نظر بزنید:" else "جهت تغییر شهر کلیک کنید (صفاشهر، بوانات، قادرآباد، پاسارگاد)",
                                        fontSize = 10.sp,
                                        color = DignifiedSlate
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isCitiesAccordionExpanded = !isCitiesAccordionExpanded },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCitiesAccordionExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (isCitiesAccordionExpanded) "بستن لیست شهرها" else "باز کردن لیست شهرها",
                                    tint = SoberNavy
                                )
                            }
                        }

                        // Vertical Expanded City List
                        AnimatedVisibility(
                            visible = isCitiesAccordionExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HorizontalDivider(color = NeutralLight.copy(alpha = 0.8f))

                                val cityDetails = listOf(
                                    Triple("همه شهرها", "نمایش یکجای تمام آگهی‌های منطقه ۴ گانه", Icons.Filled.Public),
                                    Triple("صفاشهر", "مرکز شهرستان خرم‌بید و منطقه ناحیه ۱ و ۲", Icons.Filled.LocationCity),
                                    Triple("بوانات", "سوریان، مزایجان و بخش‌های سرسبز بوانات", Icons.Filled.Park),
                                    Triple("قادرآباد", "بخش مشهد مرغاب، دشت مرغاب و حومه", Icons.Filled.Apartment),
                                    Triple("پاسارگاد", "سعادت‌شهر، میراث جهانی و روستاهای پاسارگاد", Icons.Filled.AccountBalance)
                                )

                                cityDetails.forEach { (cityName, cityDesc, cityIcon) ->
                                    val isSelected = uiState.selectedCity == cityName
                                    val cityAdCount = uiState.listings.count { it.approvalStatus == com.example.model.ListingApprovalStatus.APPROVED && (cityName == "همه شهرها" || it.city == cityName) }

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                viewModel.selectCity(cityName)
                                                // Automatically close or keep open
                                            }
                                            .testTag("city_item_$cityName"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SoftNavy else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) SoberNavy else NeutralLight.copy(alpha = 0.6f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) SoberNavy else NeutralLight),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = cityIcon,
                                                        contentDescription = null,
                                                        tint = if (isSelected) Color.White else NeutralDark,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = cityName,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) SoberNavy else NeutralDark
                                                        )
                                                        if (isSelected) {
                                                            Text(
                                                                text = "(شهر فعال)",
                                                                fontSize = 10.sp,
                                                                color = AmberAccent,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = cityDesc,
                                                        fontSize = 10.sp,
                                                        color = DignifiedSlate,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) SoberNavy.copy(alpha = 0.1f) else NeutralLight
                                                ) {
                                                    Text(
                                                        text = "$cityAdCount آگهی",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) SoberNavy else NeutralMedium,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }

                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { viewModel.selectCity(cityName) },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = SoberNavy,
                                                        unselectedColor = NeutralMedium
                                                    ),
                                                    modifier = Modifier.size(24.dp)
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

            // Category Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryType.entries.forEach { cat ->
                        val isSelected = uiState.selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat.faTitle, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("category_chip_${cat.name}")
                        )
                    }
                }
            }

            // Quick Filters (Urgent)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { viewModel.toggleUrgentFilter() },
                        label = { Text("فوری‌ها ⚡", fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (uiState.onlyUrgent) AmberAccent.copy(alpha = 0.2f) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, if (uiState.onlyUrgent) AmberAccent else Color.LightGray)
                    )
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تازه‌ترین آگهی‌ها (${JaarchiViewModel.toPersianDigits(uiState.filteredListings.size.toString())})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "مرتب‌سازی: جدیدترین",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralMedium
                    )
                }
            }

            // Listing Items
            if (uiState.filteredListings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, NeutralLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (uiState.isCloudSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = SoberNavy,
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = "در حال بارگذاری آگهی‌ها از فضای ابری...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SoberNavy
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(NeutralLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudSync,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = SoberNavy
                                    )
                                }
                                Text(
                                    text = "هیچ آگهی در این دسته‌بندی یافت نشد",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تمامی داده‌ها مستقیماً با سرور ابری همگام‌سازی می‌شوند. اولین نفری باشید که در این بخش آگهی ثبت می‌کند!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeutralMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.syncWithCloud(showToast = true) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("بروزرسانی ابری")
                                    }
                                    Button(
                                        onClick = { showCreateAdDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
                                    ) {
                                        Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("ثبت آگهی")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(uiState.filteredListings, key = { it.id }) { listing ->
                    ListingCard(
                        listing = listing,
                        isAdmin = uiState.isAdminLoggedIn,
                        onCardClick = { selectedListingForDetail = listing },
                        onBookmarkClick = { viewModel.toggleBookmark(listing.id) },
                        onDeleteClick = { viewModel.deleteListing(listing.id) },
                        onDirectionsClick = {
                            openGoogleMapsDirections(context, listing.location)
                        }
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    if (selectedListingForDetail != null) {
        ListingDetailSheet(
            listing = selectedListingForDetail!!,
            isAdmin = uiState.isAdminLoggedIn,
            onDismiss = { selectedListingForDetail = null },
            onBookmark = { viewModel.toggleBookmark(selectedListingForDetail!!.id) },
            onDeleteByAdmin = {
                val idToDelete = selectedListingForDetail!!.id
                selectedListingForDetail = null
                viewModel.deleteListing(idToDelete)
            },
            onDirectionsClick = {
                openGoogleMapsDirections(context, selectedListingForDetail!!.location)
            }
        )
    }

    // In-App Online Update Dialog
    if (showUpdateDialog) {
        val isFeatureActive = uiState.appUpdateInfo.isUpdateFeatureActive
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDownloadingUpdate) showUpdateDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "بروزرسانی آنلاین «${uiState.appName}»",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("نسخه فعلی شما: ${uiState.appUpdateInfo.currentVersion}", fontWeight = FontWeight.SemiBold)
                    Text(
                        "آخرین نسخه منتشر شده در فضای ابری: ${uiState.appUpdateInfo.latestVersion}",
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SoftGreen,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.appUpdateInfo.adminNote,
                            fontSize = 11.sp,
                            color = NeutralDark,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Text("تغییرات و امکانات نسخه ${uiState.appUpdateInfo.latestVersion}:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    uiState.appUpdateInfo.changeLog.forEach { item ->
                        Text("• $item", fontSize = 11.sp, color = NeutralDark)
                    }

                    if (uiState.isDownloadingUpdate) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { uiState.updateProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = EmeraldGreen
                            )
                            Text(
                                "در حال اتصال به سرور ابری و دانلود بسته نسخه ${uiState.appUpdateInfo.latestVersion} (${(uiState.updateProgress * 100).toInt()}٪)...",
                                fontSize = 11.sp,
                                color = NeutralMedium
                            )
                        }
                    } else if (uiState.isUpdateInstalled || !uiState.appUpdateInfo.isUpdateAvailable) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftGreen,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✓ برنامه شما به جدیدترین نسخه (${uiState.appUpdateInfo.currentVersion}) ارتقا یافت و با فضای ابری همگام است.",
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
                        Text(if (uiState.appUpdateInfo.isUpdateAvailable) "دریافت و بروزرسانی آنلاین به نسخه ${uiState.appUpdateInfo.latestVersion}" else "بروزرسانی مجدد / بررسی مجدد")
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

    // Create New Ad Dialog with Map Pin & Phone Gallery Photo/Video
    if (showCreateAdDialog) {
        CreateAdDialog(
            onDismiss = { showCreateAdDialog = false },
            onSubmit = { title, cat, price, city, neighborhood, desc, isUrgent, loc, photos, video ->
                viewModel.addNewListing(title, cat, price, city, neighborhood, desc, isUrgent, false, loc, photos, video)
                showCreateAdDialog = false
            }
        )
    }
}

@Composable
fun ListingCard(
    listing: Listing,
    isAdmin: Boolean = false,
    onCardClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onDirectionsClick: () -> Unit
) {
    var showAdminDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCardClick() }
            .testTag("listing_card_${listing.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Icon / Photo Preview
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (listing.category) {
                            CategoryType.VEHICLES -> RoyalBlue.copy(alpha = 0.12f)
                            CategoryType.REAL_ESTATE -> EmeraldGreen.copy(alpha = 0.12f)
                            CategoryType.DIGITAL -> DarkCrimson.copy(alpha = 0.12f)
                            else -> AmberAccent.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (listing.mediaUris.isNotEmpty()) {
                    AsyncImage(
                        model = listing.mediaUris.first(),
                        contentDescription = "عکس آگهی",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = when (listing.category) {
                            CategoryType.VEHICLES -> Icons.Filled.DirectionsCar
                            CategoryType.REAL_ESTATE -> Icons.Filled.Home
                            CategoryType.DIGITAL -> Icons.Filled.Smartphone
                            CategoryType.HOME_APPLIANCES -> Icons.Filled.Tv
                            CategoryType.SERVICES -> Icons.Filled.Build
                            CategoryType.JOBS -> Icons.Filled.Work
                            else -> Icons.Filled.Storefront
                        },
                        contentDescription = null,
                        tint = when (listing.category) {
                            CategoryType.VEHICLES -> RoyalBlue
                            CategoryType.REAL_ESTATE -> EmeraldGreen
                            CategoryType.DIGITAL -> CrimsonRed
                            else -> AmberAccent
                        },
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (listing.isUrgent) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                    ) {
                        PersianBadge(
                            text = "فوری",
                            containerColor = CrimsonRed,
                            contentColor = Color.White
                        )
                    }
                }
            }

            // Listing Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (listing.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "نشان کردن",
                            tint = if (listing.isBookmarked) AmberAccent else NeutralMedium,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${listing.city}، ${listing.neighborhood}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralMedium
                    )
                    Text("•", color = NeutralMedium)
                    Text(
                        text = listing.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralMedium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${JaarchiViewModel.formatPrice(listing.priceTomans)} تومان",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Admin quick delete button
                        if (isAdmin && onDeleteClick != null) {
                            FilledTonalIconButton(
                                onClick = { showAdminDeleteConfirm = true },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = SoftCrimson,
                                    contentColor = CrimsonRed
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "حذف توسط مدیریت",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Navigation to Google Maps button
                        FilledTonalIconButton(
                            onClick = onDirectionsClick,
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = RoyalBlue.copy(alpha = 0.15f),
                                contentColor = RoyalBlue
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Directions,
                                contentDescription = "مسیریابی روی نقشه گوگل",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdminDeleteConfirm && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { showAdminDeleteConfirm = false },
            icon = {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("حذف آگهی توسط مدیریت", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("آیا مطمئن هستید که می‌خواهید آگهی «${listing.title}» را به عنوان مدیر حذف نمایید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showAdminDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("حذف آگهی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDeleteConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailSheet(
    listing: Listing,
    isAdmin: Boolean = false,
    onDismiss: () -> Unit,
    onBookmark: () -> Unit,
    onDeleteByAdmin: (() -> Unit)? = null,
    onDirectionsClick: () -> Unit
) {
    val context = LocalContext.current
    var showAdminDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onBookmark) {
                    Icon(
                        imageVector = if (listing.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "ذخیره",
                        tint = if (listing.isBookmarked) AmberAccent else NeutralMedium
                    )
                }
            }

            // Photos gallery if available
            if (listing.mediaUris.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(listing.mediaUris) { uriStr ->
                        AsyncImage(
                            model = uriStr,
                            contentDescription = "تصویر آگهی",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Price & Valuation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftCrimson),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("قیمت درخواستی فروشنده:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${JaarchiViewModel.formatPrice(listing.priceTomans)} تومان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed
                        )
                    }
                }
            }

            // Description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("توضیحات آگهی:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            // Map View & Navigation Action
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                        Text("موقعیت مکانی روی نقشه:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = onDirectionsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مسیریابی با گوگل مپ 🗺️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Embedded Leaflet/Google Maps view with red marker
                InteractiveLocationPickerMap(
                    initialLatitude = listing.location.latitude,
                    initialLongitude = listing.location.longitude,
                    isReadOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            // Contact Seller Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:${listing.sellerPhone}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تماس با فروشنده", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("smsto:${listing.sellerPhone}")
                            putExtra("sms_body", "سلام، در رابطه با آگهی «${listing.title}» پیام می‌دهم.")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ارسال پیامک", fontWeight = FontWeight.Bold)
                }
            }

            // Admin Special Actions Section (if Admin is logged in)
            if (isAdmin && onDeleteByAdmin != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCrimson),
                    border = BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(20.dp))
                            Text("اختیارات ویژه مدیریت سامانه", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrimsonRed)
                        }

                        Text(
                            text = "به عنوان مدیر می‌توانید این آگهی را بلافاصله و برای همیشه از کل سامانه حذف نمایید.",
                            fontSize = 11.sp,
                            color = NeutralDark
                        )

                        Button(
                            onClick = { showAdminDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حذف دائم این آگهی توسط مدیریت", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAdminDeleteConfirm && onDeleteByAdmin != null) {
        AlertDialog(
            onDismissRequest = { showAdminDeleteConfirm = false },
            icon = {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("تایید حذف آگهی توسط مدیریت", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("آیا از حذف دائم و قطعی آگهی «${listing.title}» اطمینان دارید؟ این عمل تمام اطلاعات آگهی را فوراً از سامانه پاک می‌کند.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteByAdmin()
                        showAdminDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("بله، حذف کامل آگهی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDeleteConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun CreateAdDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, CategoryType, Long, String, String, String, Boolean, LocationPoint, List<String>, String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(CategoryType.DIGITAL) }
    var priceText by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("تهران") }
    var neighborhood by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    // Location coordinates picked from the interactive map (automatically saved without manual typing)
    var pickedLatitude by remember { mutableDoubleStateOf(35.6892) }
    var pickedLongitude by remember { mutableDoubleStateOf(51.3890) }

    // Media picked from phone gallery
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedPhotoUris = uris
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedVideoUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت آگهی با نقشه، عکس و فیلم", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان آگهی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("قیمت (تومان)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("شهر") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        label = { Text("محله") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات کالا") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Phone Gallery Media Selection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("انتخاب عکس و فیلم از گالری گوشی:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalBlue)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("افزودن عکس", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = { videoPickerLauncher.launch("video/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.VideoCameraBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (selectedVideoUri != null) "فیلم انتخاب شد ✓" else "افزودن ویدیو", fontSize = 11.sp)
                            }
                        }

                        if (selectedPhotoUris.isNotEmpty()) {
                            Text("${JaarchiViewModel.toPersianDigits(selectedPhotoUris.size.toString())} عکس از گالری انتخاب شد", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Google Maps Location on Mobile
                OutlinedButton(
                    onClick = {
                        val uriStr = "geo:0,0?q=${Uri.encode("$city $neighborhood")}"
                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val fallback = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("$city $neighborhood")}")
                            )
                            context.startActivity(fallback)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalBlue),
                    border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("باز کردن گوگل مپ گوشی موبایل 🗺️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("آگهی فوری (نشان ویژه ⚡)", fontSize = 12.sp)
                    Switch(checked = isUrgent, onCheckedChange = { isUrgent = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toLongOrNull() ?: 0L
                    if (title.isNotBlank() && price > 0) {
                        val loc = LocationPoint(
                            latitude = pickedLatitude,
                            longitude = pickedLongitude,
                            addressTitle = "$city، $neighborhood"
                        )
                        val photoList = selectedPhotoUris.map { it.toString() }
                        val videoStr = selectedVideoUri?.toString()
                        onSubmit(title, selectedCat, price, city, neighborhood, description, isUrgent, loc, photoList, videoStr)
                    }
                },
                enabled = title.isNotBlank() && (priceText.toLongOrNull() ?: 0L) > 0
            ) {
                Text("ارسال جهت تایید مدیریت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
