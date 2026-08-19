package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CategoryType
import com.example.model.LocationPoint
import com.example.ui.components.PersianBadge
import com.example.ui.theme.*
import com.example.viewmodel.JaarchiUiState
import com.example.viewmodel.JaarchiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostAdScreen(
    uiState: JaarchiUiState,
    viewModel: JaarchiViewModel,
    onAdPublishedSuccess: () -> Unit
) {
    val context = LocalContext.current

    // User Contact Information fields
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

    // Form fields
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CategoryType.DIGITAL) }
    var priceText by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("صفاشهر") }
    var neighborhood by remember { mutableStateOf("خیابان امام") }
    var addressDescription by remember { mutableStateOf("بلوار اصلی، جنب میدان") }
    var description by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }

    // Media from phone gallery
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Map Coordinates
    var pickedLatitude by remember { mutableDoubleStateOf(30.6127) }
    var pickedLongitude by remember { mutableDoubleStateOf(53.1895) }
    var hasOpenedGoogleMaps by remember { mutableStateOf(false) }

    // Success publication dialog
    var showSuccessDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedPhotoUris = (selectedPhotoUris + uris).distinct()
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 90.dp)
            .testTag("post_ad_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SoberNavy)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.PostAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Text(
                        text = "ثبت آگهی و ارسال به مدیریت",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "اطلاعات تماس و جزییات آگهی خود را وارد نمایید. آگهی شما پس از ثبت، در فضای ابری آنلاین رسانه آریا ذخیره شده و برای تمام کاربران در تمامی گوشی‌ها در دسترس خواهد بود.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 17.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                        Text("اتصال ابری آنلاین رسانه آریا فعال است 🌐", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1. User Contact Information Section (Phone number kept, verification checks removed)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SoftNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = SoberNavy, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "مشخصات و شماره تماس آگهی‌دهنده",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SoberNavy
                        )
                        Text(
                            text = "شماره تماس جهت هماهنگی خریداران و درج در آگهی",
                            fontSize = 11.sp,
                            color = NeutralMedium
                        )
                    }
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("شماره تلفن همراه (ضروری)") },
                    placeholder = { Text("مثلاً: ۰۹۱۷۱۲۳۴۵۶۷") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = SoberNavy) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("نام و نام خانوادگی یا عنوان فروشگاه") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = SoberNavy) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emailAddress,
                    onValueChange = { emailAddress = it },
                    label = { Text("آدرس ایمیل (اختیاری)") },
                    placeholder = { Text("مثلاً: email@example.com") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = SoberNavy) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Photos and Videos from Phone Gallery
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تصاویر و ویدیو از گالری گوشی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    PersianBadge(
                        text = "${JaarchiViewModel.toPersianDigits(selectedPhotoUris.size.toString())} تصویر",
                        containerColor = SoftNavy,
                        contentColor = SoberNavy
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoberNavy),
                        border = BorderStroke(1.dp, SoberNavy)
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("انتخاب عکس", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepPurple),
                        border = BorderStroke(1.dp, DeepPurple)
                    ) {
                        Icon(Icons.Filled.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedVideoUri != null) "فیلم انتخاب شد ✓" else "انتخاب ویدیو", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (selectedPhotoUris.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    ) {
                        itemsIndexed(selectedPhotoUris) { index, uri ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, NeutralLight, RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Photo $index",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = {
                                        selectedPhotoUris = selectedPhotoUris.toMutableList().also { it.removeAt(index) }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "حذف", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Ad Specifications & Category
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
                Text("مشخصات و دسته‌بندی آگهی", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان آگهی") },
                    placeholder = { Text("مثال: فروش پژو ۲۰۷ یا گردوی اعلای بوانات") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("انتخاب گروه دسته‌بندی:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryType.entries.filter { it != CategoryType.ALL }.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.faTitle, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoberNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("قیمت (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        val num = priceText.toLongOrNull()
                        if (num != null && num > 0) {
                            Text("${JaarchiViewModel.formatPrice(num)} تومان", color = SoberNavy, fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isUrgent) AmberAccent.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.FlashOn, contentDescription = null, tint = AmberAccent)
                        Text("نشان آگهی فوری ⚡", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Switch(
                        checked = isUrgent,
                        onCheckedChange = { isUrgent = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AmberAccent)
                    )
                }
            }
        }

        // 4. City (Safashahr, Bavanat, Qaderabad, Pasargad) & Google Maps
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
                Text("شهر و موقعیت مکانی (گوگل مپ)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Text("شهر محل آگهی:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("صفاشهر", "بوانات", "قادرآباد", "پاسارگاد").forEach { cityName ->
                        val isSelected = selectedCity == cityName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCity = cityName },
                            label = { Text(cityName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoberNavy,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        label = { Text("محله یا منطقه") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = addressDescription,
                        onValueChange = { addressDescription = it },
                        label = { Text("شرح نشانی") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedButton(
                    onClick = {
                        hasOpenedGoogleMaps = true
                        val uriStr = "geo:0,0?q=${Uri.encode("$selectedCity $neighborhood $addressDescription")}"
                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val fallbackIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("$selectedCity $neighborhood $addressDescription")}")
                            )
                            context.startActivity(fallbackIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoberNavy),
                    border = BorderStroke(1.dp, SoberNavy)
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (hasOpenedGoogleMaps) "موقعیت در گوگل مپ باز شد ✓" else "باز کردن و ثبت در گوگل مپ گوشی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. Description
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("توضیحات تکمیلی آگهی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("جزییات کالا یا خدمات، کیفیت، شرایط بازدید و تماس را بنویسید...") },
                    minLines = 4,
                    maxLines = 7,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Submit Button
        val canSubmit = title.isNotBlank() && (priceText.toLongOrNull() ?: 0L) > 0

        Button(
            onClick = {
                val price = priceText.toLongOrNull() ?: 0L
                val loc = LocationPoint(
                    latitude = pickedLatitude,
                    longitude = pickedLongitude,
                    addressTitle = "$selectedCity، $neighborhood - $addressDescription"
                )
                val photoUrls = selectedPhotoUris.map { it.toString() }
                val videoUrl = selectedVideoUri?.toString()

                viewModel.publishAd(
                    title = title,
                    category = selectedCategory,
                    priceTomans = price,
                    city = selectedCity,
                    neighborhood = neighborhood,
                    description = description.ifBlank { "آگهی ثبت شده در سامانه رسانه آریا ($selectedCity)." },
                    isUrgent = isUrgent,
                    location = loc,
                    mediaUris = photoUrls,
                    videoUri = videoUrl,
                    userFullName = fullName,
                    userPhone = phoneNumber,
                    userEmail = emailAddress
                )
                showSuccessDialog = true
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_publish_ad_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
        ) {
            Icon(Icons.Filled.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ارسال آگهی جهت تایید و نظارت مدیریت", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onAdPublishedSuccess()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SoftGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(32.dp))
                }
            },
            title = {
                Text("آگهی با موفقیت ثبت شد", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "آگهی شما با شماره تماس $phoneNumber با موفقیت ثبت شد و در صف تایید مدیریت قرار گرفت.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "پس از بررسی و تایید مدیریت، آگهی بلافاصله در گروه ${selectedCategory.faTitle} شهر $selectedCity درج خواهد شد.",
                        fontSize = 11.sp,
                        color = NeutralMedium,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onAdPublishedSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoberNavy)
                ) {
                    Text("مشاهده صفحه اصلی")
                }
            }
        )
    }
}
