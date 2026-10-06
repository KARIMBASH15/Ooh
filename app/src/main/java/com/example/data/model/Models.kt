package com.example.data.model

data class MallSettings(
    val appName: String = "مول مطروح",
    val logoUrl: String = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPoLb_aEF0rhvlPW4x4sx3k6wswd77l7DMmN2B54gmlw&s=10",
    val notifications: List<String> = listOf("✨ مرحباً بك في مول مطروح"),
    val primaryColor: String = "#1D4475",
    val accentColor: String = "#C8A24B"
)

data class Category(
    val id: String = "",
    val name: String = "",
    val icon: String = "Tag",
    val description: String = "",
    val image: String? = null
)

data class Store(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val coverImage: String = "",
    val logo: String = "",
    val image: String = "",
    val brandColor: String? = null,
    val location: String = "مرسى مطروح",
    val hours: String = "١٠:٠٠ ص - ١٢:٠٠ م",
    val whatsapp: String = "201000000000",
    val deliveryTime: String = "٣٠-٤٥ دقيقة",
    val minOrder: Double = 0.0,
    val isFeatured: Boolean = false,
    val isSuspended: Boolean = false,
    val tagline: String = "",
    val mapImage: String = ""
)

data class ProductWeight(
    val name: String = "",
    val price: Double = 0.0
)

data class ProductExtra(
    val name: String = "",
    val price: Double = 0.0
)

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val specs: String = "",
    val category: String = "",
    val storeId: String = "",
    val price: Double = 0.0,
    val oldPrice: Double? = null,
    val images: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val isOffer: Boolean = false,
    val isExclusive: Boolean = false,
    val isNewArrival: Boolean = false,
    val stock: Int? = null,
    val flashSaleEndTime: String? = null,
    val flashOriginalPrice: Double? = null,
    val flashOriginalOldPrice: Double? = null,
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val weights: List<ProductWeight> = emptyList(),
    val flavors: List<String> = emptyList(),
    val extras: List<ProductExtra> = emptyList()
)

data class BannerItem(
    val id: String = "",
    val image: String = "",
    val link: String? = null,
    val title: String? = null,
    val active: Boolean = true
)

data class AdItem(
    val id: String = "",
    val title: String? = null,
    val image: String = "",
    val placement: String = "home-top",
    val type: String = "image",
    val size: String = "wide",
    val active: Boolean = true,
    val storeId: String? = null,
    val advertiserName: String? = "مول مطروح",
    val link: String? = null,
    val linkType: String? = null,
    val targetStoreId: String? = null,
    val targetProductId: String? = null,
    val targetSection: String? = null,
    val productIds: List<String> = emptyList(),
    val storeIds: List<String> = emptyList()
)

data class StoreBanner(
    val id: String = "",
    val storeId: String = "",
    val image: String = "",
    val title: String? = null,
    val size: String = "wide",
    val active: Boolean = true
)

data class StoreSection(
    val id: String = "",
    val storeId: String = "",
    val name: String = "",
    val icon: String? = null,
    val image: String? = null,
    val order: Int = 0
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selectedSize: String? = null,
    val selectedColor: String? = null,
    val selectedFlavor: String? = null,
    val selectedWeight: ProductWeight? = null,
    val selectedExtras: List<ProductExtra> = emptyList(),
    val specialNotes: String = "",
    val finalPrice: Double = product.price
)

data class CustomerProfile(
    val name: String = "",
    val phone: String = "",
    val address: String = ""
)

data class OrderHistoryItem(
    val id: Long = 0L,
    val date: String = "",
    val storeName: String = "",
    val storeId: String = "",
    val storeWhatsapp: String = "",
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val coupon: String? = null,
    val status: String = "new" // new, processing, delivered, cancelled
)

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val sender: String = "customer", // customer, store, admin
    val senderName: String = "",
    val timestamp: String = ""
)

data class PromoCode(
    val code: String = "",
    val percent: Double = 0.0,
    val active: Boolean = true
)
