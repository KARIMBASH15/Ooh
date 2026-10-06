package com.example.data.remote

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FirebaseRepository {
    private val baseUrl = "https://mulmatroh-266ec-default-rtdb.firebaseio.com"
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getSettings(): MallSettings = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/settings.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext FallbackData.settings
                    val json = JSONObject(body)
                    val appName = json.optString("appName", "مول مطروح")
                    val logoUrl = json.optString("logoUrl").ifBlank {
                        json.optString("logo", FallbackData.settings.logoUrl)
                    }
                    val notifs = mutableListOf<String>()
                    val notifArr = json.optJSONArray("notifications")
                    if (notifArr != null) {
                        for (i in 0 until notifArr.length()) {
                            val item = notifArr.optJSONObject(i)
                            if (item != null && item.optBoolean("active", true)) {
                                notifs.add(item.optString("text"))
                            } else if (notifArr.optString(i).isNotBlank()) {
                                notifs.add(notifArr.optString(i))
                            }
                        }
                    }
                    if (notifs.isEmpty()) notifs.add("✨ مرحباً بك في مول مطروح")
                    return@withContext MallSettings(
                        appName = appName,
                        logoUrl = logoUrl,
                        notifications = notifs
                    )
                }
            }
        } catch (_: Exception) {}
        FallbackData.settings
    }

    suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/categories.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext FallbackData.categories
                    val json = JSONObject(body)
                    val list = mutableListOf<Category>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = json.optJSONObject(key) ?: continue
                        list.add(
                            Category(
                                id = key,
                                name = obj.optString("name", "قسم"),
                                icon = obj.optString("icon", "Tag"),
                                description = obj.optString("description", ""),
                                image = if (obj.has("image")) obj.optString("image") else null
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (_: Exception) {}
        FallbackData.categories
    }

    suspend fun getStores(): List<Store> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/stores.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext FallbackData.stores
                    val json = JSONObject(body)
                    val list = mutableListOf<Store>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = json.optJSONObject(key) ?: continue
                        if (obj.optBoolean("isSuspended", false)) continue
                        val cover = obj.optString("coverImage").ifBlank { obj.optString("image") }
                        val logo = obj.optString("logo").ifBlank { obj.optString("image") }
                        list.add(
                            Store(
                                id = key,
                                name = obj.optString("name", "متجر"),
                                description = obj.optString("description", ""),
                                categoryId = obj.optString("categoryId", ""),
                                coverImage = cover,
                                logo = logo,
                                image = obj.optString("image"),
                                brandColor = if (obj.has("brandColor") && obj.optString("brandColor").startsWith("#")) obj.optString("brandColor") else null,
                                location = obj.optString("location", "مرسى مطروح"),
                                hours = obj.optString("hours", "١٠:٠٠ ص - ١٢:٠٠ م"),
                                whatsapp = obj.optString("whatsapp", "201000000000").replace("+", "").replace(" ", ""),
                                deliveryTime = obj.optString("deliveryTime", "٣٠-٤٥ دقيقة"),
                                minOrder = obj.optDouble("minOrder", 0.0),
                                isFeatured = obj.optBoolean("isFeatured", false),
                                isSuspended = obj.optBoolean("isSuspended", false),
                                tagline = obj.optString("tagline", obj.optString("description")),
                                mapImage = obj.optString("mapImage").ifBlank { cover }
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (_: Exception) {}
        FallbackData.stores
    }

    suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/products.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext FallbackData.products
                    val json = JSONObject(body)
                    val list = mutableListOf<Product>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = json.optJSONObject(key) ?: continue

                        // parse images
                        val images = mutableListOf<String>()
                        val imgArr = obj.optJSONArray("images")
                        if (imgArr != null) {
                            for (i in 0 until imgArr.length()) {
                                val url = imgArr.optString(i)
                                if (url.isNotBlank()) images.add(url)
                            }
                        } else if (obj.has("image")) {
                            val u = obj.optString("image")
                            if (u.isNotBlank()) images.add(u)
                        }

                        // sizes
                        val sizes = mutableListOf<String>()
                        val sizesArr = obj.optJSONArray("sizes")
                        if (sizesArr != null) {
                            for (i in 0 until sizesArr.length()) sizes.add(sizesArr.optString(i))
                        }

                        // colors
                        val colors = mutableListOf<String>()
                        val colorsArr = obj.optJSONArray("colors")
                        if (colorsArr != null) {
                            for (i in 0 until colorsArr.length()) colors.add(colorsArr.optString(i))
                        }

                        // flavors
                        val flavors = mutableListOf<String>()
                        val flavArr = obj.optJSONArray("flavors")
                        if (flavArr != null) {
                            for (i in 0 until flavArr.length()) flavors.add(flavArr.optString(i))
                        }

                        // weights
                        val weights = mutableListOf<ProductWeight>()
                        val weightsArr = obj.optJSONArray("weights")
                        if (weightsArr != null) {
                            for (i in 0 until weightsArr.length()) {
                                val wObj = weightsArr.optJSONObject(i)
                                if (wObj != null) {
                                    weights.add(
                                        ProductWeight(
                                            name = wObj.optString("name"),
                                            price = wObj.optDouble("price", 0.0)
                                        )
                                    )
                                }
                            }
                        }

                        // extras
                        val extras = mutableListOf<ProductExtra>()
                        val extrasArr = obj.optJSONArray("extras")
                        if (extrasArr != null) {
                            for (i in 0 until extrasArr.length()) {
                                val eObj = extrasArr.optJSONObject(i)
                                if (eObj != null) {
                                    extras.add(
                                        ProductExtra(
                                            name = eObj.optString("name"),
                                            price = eObj.optDouble("price", 0.0)
                                        )
                                    )
                                }
                            }
                        }

                        val price = obj.optDouble("price", 0.0)
                        val oldPrice = if (obj.has("oldPrice") && !obj.isNull("oldPrice")) obj.optDouble("oldPrice") else null
                        val stock = if (obj.has("stock") && !obj.isNull("stock")) obj.optInt("stock") else null
                        val flashEnd = if (obj.has("flashSaleEndTime") && !obj.isNull("flashSaleEndTime")) obj.optString("flashSaleEndTime") else null

                        list.add(
                            Product(
                                id = key,
                                name = obj.optString("name", "منتج"),
                                description = obj.optString("description", ""),
                                specs = obj.optString("specs", ""),
                                category = obj.optString("category", ""),
                                storeId = obj.optString("storeId", ""),
                                price = price,
                                oldPrice = oldPrice,
                                images = images,
                                isFeatured = obj.optBoolean("isFeatured", false),
                                isOffer = obj.optBoolean("isOffer", false),
                                isExclusive = obj.optBoolean("isExclusive", false),
                                isNewArrival = obj.optBoolean("isNewArrival", false),
                                stock = stock,
                                flashSaleEndTime = flashEnd,
                                sizes = sizes,
                                colors = colors,
                                weights = weights,
                                flavors = flavors,
                                extras = extras
                            )
                        )
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (_: Exception) {}
        FallbackData.products
    }

    suspend fun getBanners(): List<BannerItem> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/banners.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext FallbackData.banners
                    val json = JSONObject(body)
                    val list = mutableListOf<BannerItem>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = json.optJSONObject(key) ?: continue
                        if (!obj.optBoolean("active", true)) continue
                        val img = obj.optString("image")
                        if (img.isNotBlank()) {
                            list.add(
                                BannerItem(
                                    id = key,
                                    image = img,
                                    link = if (obj.has("link")) obj.optString("link") else null,
                                    title = if (obj.has("title")) obj.optString("title") else null,
                                    active = true
                                )
                            )
                        }
                    }
                    if (list.isNotEmpty()) return@withContext list
                }
            }
        } catch (_: Exception) {}
        FallbackData.banners
    }

    suspend fun getAds(): List<AdItem> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/ads.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext emptyList()
                    val json = JSONObject(body)
                    val list = mutableListOf<AdItem>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = json.optJSONObject(key) ?: continue
                        if (!obj.optBoolean("active", true)) continue
                        list.add(
                            AdItem(
                                id = key,
                                title = if (obj.has("title")) obj.optString("title") else null,
                                image = obj.optString("image"),
                                placement = obj.optString("placement", "home-top"),
                                type = obj.optString("type", "image"),
                                size = obj.optString("size", "wide"),
                                storeId = if (obj.has("storeId")) obj.optString("storeId") else null,
                                advertiserName = obj.optString("advertiserName", "مول مطروح"),
                                link = if (obj.has("link")) obj.optString("link") else null,
                                linkType = if (obj.has("linkType")) obj.optString("linkType") else null,
                                targetStoreId = if (obj.has("targetStoreId")) obj.optString("targetStoreId") else null,
                                targetProductId = if (obj.has("targetProductId")) obj.optString("targetProductId") else null
                            )
                        )
                    }
                    return@withContext list
                }
            }
        } catch (_: Exception) {}
        emptyList()
    }

    suspend fun getStoreBanners(storeId: String): List<StoreBanner> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/storeBanners.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext emptyList()
                    val json = JSONObject(body)
                    val list = mutableListOf<StoreBanner>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = json.optJSONObject(key) ?: continue
                        if (obj.optString("storeId") == storeId && obj.optBoolean("active", true)) {
                            list.add(
                                StoreBanner(
                                    id = key,
                                    storeId = storeId,
                                    image = obj.optString("image"),
                                    title = if (obj.has("title")) obj.optString("title") else null,
                                    size = obj.optString("size", "wide")
                                )
                            )
                        }
                    }
                    return@withContext list
                }
            }
        } catch (_: Exception) {}
        emptyList()
    }

    suspend fun getStoreSections(storeId: String): List<StoreSection> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/storeCategories.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext emptyList()
                    val json = JSONObject(body)
                    val list = mutableListOf<StoreSection>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = json.optJSONObject(key) ?: continue
                        if (obj.optString("storeId") == storeId) {
                            list.add(
                                StoreSection(
                                    id = key,
                                    storeId = storeId,
                                    name = obj.optString("name"),
                                    icon = if (obj.has("icon")) obj.optString("icon") else null,
                                    image = if (obj.has("image")) obj.optString("image") else null,
                                    order = obj.optInt("order", 0)
                                )
                            )
                        }
                    }
                    return@withContext list.sortedBy { it.order }
                }
            }
        } catch (_: Exception) {}
        emptyList()
    }

    suspend fun getPromoCodes(): Map<String, PromoCode> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder().url("$baseUrl/promoCodes.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext FallbackData.promoCodes
                    val json = JSONObject(body)
                    val map = mutableMapOf<String, PromoCode>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next().uppercase()
                        val obj = json.optJSONObject(key) ?: continue
                        map[key] = PromoCode(
                            code = key,
                            percent = obj.optDouble("percent", 0.0) / 100.0,
                            active = obj.optBoolean("active", true)
                        )
                    }
                    if (map.isNotEmpty()) return@withContext map
                }
            }
        } catch (_: Exception) {}
        FallbackData.promoCodes
    }

    suspend fun postOrder(order: OrderHistoryItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("orderNo", order.id.toString().takeLast(6))
                put("customerId", "c_" + order.customerPhone.filter { it.isDigit() })
                put("customerName", order.customerName)
                put("customerPhone", order.customerPhone)
                put("customerAddress", order.customerAddress)
                put("storeId", order.storeId)
                put("storeName", order.storeName)
                put("total", order.total)
                put("status", "new")
                put("createdAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()))
                if (order.coupon != null) put("coupon", order.coupon)

                val itemsArr = JSONArray()
                order.items.forEach { item ->
                    val itObj = JSONObject().apply {
                        put("productId", item.product.id)
                        put("name", item.product.name)
                        put("quantity", item.quantity)
                        put("price", item.finalPrice)
                        val opts = mutableListOf<String>()
                        item.selectedWeight?.let { opts.add(it.name) }
                        item.selectedSize?.let { opts.add(it) }
                        item.selectedColor?.let { opts.add(it) }
                        item.selectedFlavor?.let { opts.add(it) }
                        item.selectedExtras.forEach { opts.add(it.name) }
                        put("options", JSONArray(opts))
                        put("note", item.specialNotes)
                    }
                    itemsArr.put(itObj)
                }
                put("items", itemsArr)
            }

            val body = json.toString().toRequestBody(jsonMediaType)
            val req = Request.Builder().url("$baseUrl/orders.json").post(body).build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val respBody = resp.body?.string() ?: ""
                    val key = JSONObject(respBody).optString("name", "")
                    return@withContext Result.success(key)
                }
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
        Result.success("")
    }

    suspend fun saveCustomer(profile: CustomerProfile) = withContext(Dispatchers.IO) {
        try {
            val phoneDigits = profile.phone.filter { it.isDigit() }
            if (phoneDigits.length < 6) return@withContext
            val id = "c_$phoneDigits"
            val json = JSONObject().apply {
                put("name", profile.name)
                put("phone", profile.phone)
                put("address", profile.address)
                put("updatedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()))
            }
            val body = json.toString().toRequestBody(jsonMediaType)
            val req = Request.Builder().url("$baseUrl/customers/$id.json").patch(body).build()
            client.newCall(req).execute().close()
        } catch (_: Exception) {}
    }

    suspend fun getChatMessages(storeId: String, customerPhone: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val phoneDigits = customerPhone.filter { it.isDigit() }
            if (phoneDigits.isBlank()) return@withContext emptyList()
            val chatId = "${storeId}_c_$phoneDigits"
            val req = Request.Builder().url("$baseUrl/customerChats/$chatId/messages.json").get().build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: return@withContext emptyList()
                    val json = JSONObject(body)
                    val list = mutableListOf<ChatMessage>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val obj = json.optJSONObject(key) ?: continue
                        list.add(
                            ChatMessage(
                                id = key,
                                text = obj.optString("text"),
                                sender = obj.optString("sender", "customer"),
                                senderName = obj.optString("senderName", ""),
                                timestamp = obj.optString("timestamp", "")
                            )
                        )
                    }
                    return@withContext list.sortedBy { it.timestamp }
                }
            }
        } catch (_: Exception) {}
        emptyList()
    }

    suspend fun sendChatMessage(
        storeId: String,
        storeName: String,
        customerName: String,
        customerPhone: String,
        text: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val phoneDigits = customerPhone.filter { it.isDigit() }
            val customerId = "c_$phoneDigits"
            val chatId = "${storeId}_$customerId"
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())

            val metaJson = JSONObject().apply {
                put("storeId", storeId)
                put("storeName", storeName)
                put("customerId", customerId)
                put("customerName", customerName)
                put("customerPhone", customerPhone)
                put("lastMessage", text)
                put("lastSender", "customer")
                put("updatedAt", now)
            }
            client.newCall(Request.Builder().url("$baseUrl/customerChats/$chatId.json").patch(metaJson.toString().toRequestBody(jsonMediaType)).build()).execute().close()

            val msgJson = JSONObject().apply {
                put("text", text)
                put("sender", "customer")
                put("senderName", customerName)
                put("timestamp", now)
            }
            client.newCall(Request.Builder().url("$baseUrl/customerChats/$chatId/messages.json").post(msgJson.toString().toRequestBody(jsonMediaType)).build()).execute().close()
            return@withContext true
        } catch (_: Exception) {
            return@withContext false
        }
    }
}

object FallbackData {
    val settings = MallSettings(
        appName = "مول مطروح",
        logoUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTPoLb_aEF0rhvlPW4x4sx3k6wswd77l7DMmN2B54gmlw&s=10",
        notifications = listOf(
            "✨ مرحباً بك في مول مطروح",
            "🔥 أقوى العروض والتخفيضات اليومية",
            "🚀 توصيل سريع ومباشر داخل مرسى مطروح"
        )
    )

    val promoCodes = mapOf(
        "MATROUH20" to PromoCode("MATROUH20", 0.20),
        "WELCOME10" to PromoCode("WELCOME10", 0.10)
    )

    val banners = listOf(
        BannerItem(
            id = "b1",
            image = "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&q=80&w=1200&h=450",
            title = "عروض الصيف الكبرى"
        ),
        BannerItem(
            id = "b2",
            image = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&q=80&w=1200&h=450",
            title = "تسوق أفضل الأزياء والمطاعم"
        ),
        BannerItem(
            id = "b3",
            image = "https://images.unsplash.com/photo-1555529669-e69e7aa0ba9a?auto=format&fit=crop&q=80&w=1200&h=450",
            title = "خصومات حصرية لفترة محدودة"
        )
    )

    val categories = listOf(
        Category("cat_supermarket", "سوبر ماركت", "ShoppingBag", "مواد غذائية وتموينية"),
        Category("cat_restaurants", "مطاعم وكافيهات", "Utensils", "أشهى المأكولات والمشروبات"),
        Category("cat_clothes", "ملابس وأزياء", "Shirt", "أحدث صيحات الموضة"),
        Category("cat_phones", "هواتف وإلكترونيات", "Smartphone", "أجهزة ذكية وإكسسوارات"),
        Category("cat_perfumes", "عطور وتجميل", "Sparkles", "عطور فرنسية ومستحضرات"),
        Category("cat_sweets", "حلويات وتسالي", "Gift", "حلويات شرقية وغربية")
    )

    val stores = listOf(
        Store(
            id = "store_al_asema",
            name = "سوبر ماركت العاصمة",
            description = "أكبر تشكيلة للمواد الغذائية والمنتجات الطازجة",
            categoryId = "cat_supermarket",
            coverImage = "https://images.unsplash.com/photo-1578916171728-46686eac8d58?auto=format&fit=crop&q=80&w=800",
            logo = "https://images.unsplash.com/photo-1534723452862-4c874018d66d?auto=format&fit=crop&q=80&w=200",
            image = "https://images.unsplash.com/photo-1578916171728-46686eac8d58?auto=format&fit=crop&q=80&w=800",
            brandColor = "#1D4475",
            location = "شارع إسكندرية، مطروح",
            hours = "٢٤ ساعة",
            whatsapp = "201012345678",
            deliveryTime = "٣٠ دقيقة",
            minOrder = 50.0,
            isFeatured = true,
            tagline = "كل احتياجات بيتك في مكان واحد"
        ),
        Store(
            id = "store_shawarma_reem",
            name = "مطعم وشاورما الريم",
            description = "أشهى المأكولات السورية والمشويات والبرجر",
            categoryId = "cat_restaurants",
            coverImage = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&q=80&w=800",
            logo = "https://images.unsplash.com/photo-1561758033-d89a9ad46330?auto=format&fit=crop&q=80&w=200",
            image = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&q=80&w=800",
            brandColor = "#B91C1C",
            location = "شارع الجلاء، مطروح",
            hours = "١١:٠٠ ص - ٠٢:٠٠ ص",
            whatsapp = "201098765432",
            deliveryTime = "٤٠ دقيقة",
            minOrder = 80.0,
            isFeatured = true,
            tagline = "طعم لا يقاوم وأسرع توصيل"
        ),
        Store(
            id = "store_elegance",
            name = "إليجانس فاشون",
            description = "أرقى الملابس الشبابية والكاجوال",
            categoryId = "cat_clothes",
            coverImage = "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&q=80&w=800",
            logo = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&q=80&w=200",
            image = "https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?auto=format&fit=crop&q=80&w=800",
            brandColor = "#0284C7",
            location = "سوق ليبيا، مطروح",
            hours = "٠١:٠٠ م - ١٢:٠٠ م",
            whatsapp = "201123456789",
            deliveryTime = "٤٥ دقيقة",
            minOrder = 100.0,
            isFeatured = true,
            tagline = "أناقتك تبدأ من هنا"
        )
    )

    val products = listOf(
        Product(
            id = "p1",
            name = "وجبة ميكس جريل سوبر مع أرز وبطاطس",
            description = "تشكيلة من الكباب والكفتة والشيش طاووق مع الخبز الطازج والثومية والمخلل والبطاطس المقرمشة.",
            category = "مشويات",
            storeId = "store_shawarma_reem",
            price = 195.0,
            oldPrice = 240.0,
            images = listOf(
                "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&q=80&w=600",
                "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&q=80&w=600"
            ),
            isFeatured = true,
            isOffer = true,
            stock = 15,
            flashSaleEndTime = "2026-10-08T23:59:59.000Z",
            weights = listOf(
                ProductWeight("حجم عادي (فرد)", 195.0),
                ProductWeight("حجم كبير (شخصين)", 350.0),
                ProductWeight("وجبة عائلية (٤ أفراد)", 650.0)
            ),
            extras = listOf(
                ProductExtra("تومية إضافية", 15.0),
                ProductExtra("مخلل مشكل", 10.0),
                ProductExtra("كولا كانز", 25.0)
            )
        ),
        Product(
            id = "p2",
            name = "شاورما عربي دجاج مع صوص خاص وبطاطس",
            description = "رول شاورما دجاج مقطع على الطريقة السورية مع صوص الريم الخاص ومخلل خيار وبطاطس فارم فريتس.",
            category = "شاورما",
            storeId = "store_shawarma_reem",
            price = 85.0,
            oldPrice = 110.0,
            images = listOf(
                "https://images.unsplash.com/photo-1561651823-34feb02250e4?auto=format&fit=crop&q=80&w=600"
            ),
            isFeatured = true,
            isOffer = true,
            stock = 30,
            flashSaleEndTime = "2026-10-07T20:00:00.000Z",
            flavors = listOf("عادي", "حار سبايسي", "ميكس جبن")
        ),
        Product(
            id = "p3",
            name = "تيشيرت بولو قطن مصري فاخر",
            description = "تيشيرت بولو عالي الجودة مصنوع من خامات قطنية معالجة 100% ضد الانكماش مع تطريز راقي على الصدر.",
            category = "ملابس صيفية",
            storeId = "store_elegance",
            price = 280.0,
            oldPrice = 360.0,
            images = listOf(
                "https://images.unsplash.com/photo-1581655353564-df123a1eb820?auto=format&fit=crop&q=80&w=600",
                "https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?auto=format&fit=crop&q=80&w=600"
            ),
            isFeatured = true,
            isExclusive = true,
            stock = 12,
            sizes = listOf("M", "L", "XL", "2XL"),
            colors = listOf("#1D4475", "#000000", "#FFFFFF", "#DC2626")
        ),
        Product(
            id = "p4",
            name = "زيت زيتون مطروحي بكر معصور على البارد (١ لتر)",
            description = "زيت زيتون أصلي نقي 100% من مزارع مرسى مطروح الشهيرة، عصرة أولى على البارد بنسبة حموضة أقل من 0.8%.",
            category = "منتجات طبيعية",
            storeId = "store_al_asema",
            price = 220.0,
            oldPrice = 270.0,
            images = listOf(
                "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&q=80&w=600"
            ),
            isFeatured = true,
            isNewArrival = true,
            stock = 45,
            weights = listOf(
                ProductWeight("١ لتر", 220.0),
                ProductWeight("٢ لتر", 420.0),
                ProductWeight("٥ لتر (جالون)", 990.0)
            )
        ),
        Product(
            id = "p5",
            name = "بلح واحات سيوة ممتلئ سكري (عبوة ١ كجم)",
            description = "تمر سيوة الفاخر المعروف بطعمه السكري الغني والقيمة الغذائية العالية معبأ في علبة محكمة الغلق.",
            category = "تمور وياميش",
            storeId = "store_al_asema",
            price = 95.0,
            oldPrice = 120.0,
            images = listOf(
                "https://images.unsplash.com/photo-1589135233689-d56637e10398?auto=format&fit=crop&q=80&w=600"
            ),
            isFeatured = true,
            stock = 25
        )
    )
}
