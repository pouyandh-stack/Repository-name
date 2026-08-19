package com.example.model

enum class CategoryType(val faTitle: String, val iconName: String) {
    ALL("همه", "grid"),
    VEHICLES("خودرو", "car"),
    REAL_ESTATE("املاک", "home"),
    DIGITAL("کالای دیجیتال", "phone"),
    HOME_APPLIANCES("لوازم خانه", "tv"),
    SERVICES("خدمات", "build"),
    JOBS("استخدام", "work")
}

data class LocationPoint(
    val latitude: Double = 30.6127,
    val longitude: Double = 53.1895,
    val addressTitle: String = "صفاشهر، فارس"
)

enum class ListingApprovalStatus(val faTitle: String) {
    PENDING("در انتظار تایید مدیریت"),
    APPROVED("تایید شده و منتشر در گروه"),
    REJECTED("رد شده توسط مدیریت")
}

data class Listing(
    val id: String,
    val title: String,
    val category: CategoryType,
    val priceTomans: Long,
    val isNegotiable: Boolean = false,
    val city: String,
    val neighborhood: String,
    val description: String,
    val timeAgo: String,
    val isUrgent: Boolean = false,
    val hasEscrowGuarantee: Boolean = true,
    val isVerifiedUser: Boolean = true,
    val viewsCount: Int = 124,
    val callsCount: Int = 8,
    val isBookmarked: Boolean = false,
    val sellerPhone: String = "09171234567",
    val sellerName: String = "کاربر رسانه آریا",
    val sellerEmail: String = "",
    val isIdentityVerified: Boolean = true,
    val aiValuationEstimated: Long? = null,
    val aiConfidenceScore: Int = 94,
    val location: LocationPoint = LocationPoint(30.6127, 53.1895, "صفاشهر، خیابان امام"),
    val mediaUris: List<String> = emptyList(),
    val videoUri: String? = null,
    val approvalStatus: ListingApprovalStatus = ListingApprovalStatus.APPROVED,
    val rejectionReason: String? = null
)

data class AdminInAppMessage(
    val id: String,
    val listingId: String,
    val listingTitle: String,
    val targetUserName: String,
    val targetUserPhone: String,
    val targetUserEmail: String,
    val messageText: String,
    val isFromAdmin: Boolean,
    val timestamp: String
)

data class Transaction(
    val id: String,
    val title: String,
    val amountTomans: Long,
    val date: String,
    val isDeposit: Boolean, // true = wallet recharge/income, false = payment/escrow
    val status: String,
    val trackingCode: String,
    val relatedListingId: String? = null
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val quickReplies: List<String> = emptyList()
)

data class AiPriceEstimationResult(
    val category: String,
    val title: String,
    val minPriceTomans: Long,
    val maxPriceTomans: Long,
    val suggestedPriceTomans: Long,
    val marketTrend: String, // "صعودی", "باثبات", "نزولی"
    val recommendations: List<String>
)

data class AiCopyResult(
    val generatedTitle: String,
    val generatedDescription: String,
    val hashtags: List<String>,
    val tips: List<String>,
    val posterImageSeed: String? = null
)

data class AppUpdateInfo(
    val currentVersion: String = "1.2.0",
    val latestVersion: String = "1.2.1",
    val changeLog: List<String> = listOf(
        "بروزرسانی آنلاین و زنده سراسری از سرور ابری توزیع‌شده (نسخه ۱.۲.۱)",
        "حذف کامل داده‌های آفلاین و اتصال مستقیم به پایگاه داده زنده",
        "پوشش اختصاصی شهرهای صفاشهر، بوانات، قادرآباد و پاسارگاد",
        "مدیریت آنلاین و یکپارچه آگهی‌ها، اعلان‌ها و پیام‌های مستقیم",
        "افزایش چشمگیر سرعت اتصال و همگام‌سازی لحظه‌ای با Anycast CDN",
        "احراز هویت پیامکی و قفل اختصاصی پنل مدیریت"
    ),
    val isUpdateAvailable: Boolean = true,
    val isUpdateFeatureActive: Boolean = true,
    val isForceUpdate: Boolean = false,
    val directApkDownloadUrl: String = "",
    val adminNote: String = "نسخه ۱.۲.۱ با اعمال کامل تمام فراوین قبلی، اتصال زنده ابری و بهبودهای سراسری منتشر شد."
)
