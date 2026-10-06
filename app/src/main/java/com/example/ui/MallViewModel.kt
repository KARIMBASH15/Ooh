package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.remote.FallbackData
import com.example.data.remote.FirebaseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Screen {
    HOME,
    CATEGORY,
    STORE,
    ALL_FEATURED_PRODUCTS,
    ALL_FEATURED_STORES,
    FLASH_SALE,
    WISHLIST,
    ORDERS
}

class MallViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = FirebaseRepository()
    private val prefs = application.getSharedPreferences("matrouh_mall_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(MallSettings())
    val settings: StateFlow<MallSettings> = _settings.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _stores = MutableStateFlow<List<Store>>(emptyList())
    val stores: StateFlow<List<Store>> = _stores.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _banners = MutableStateFlow<List<BannerItem>>(emptyList())
    val banners: StateFlow<List<BannerItem>> = _banners.asStateFlow()

    private val _ads = MutableStateFlow<List<AdItem>>(emptyList())
    val ads: StateFlow<List<AdItem>> = _ads.asStateFlow()

    private val _storeBanners = MutableStateFlow<List<StoreBanner>>(emptyList())
    val storeBanners: StateFlow<List<StoreBanner>> = _storeBanners.asStateFlow()

    private val _storeSections = MutableStateFlow<List<StoreSection>>(emptyList())
    val storeSections: StateFlow<List<StoreSection>> = _storeSections.asStateFlow()

    private val _promoCodes = MutableStateFlow<Map<String, PromoCode>>(emptyList<Pair<String, PromoCode>>().toMap())
    val promoCodes: StateFlow<Map<String, PromoCode>> = _promoCodes.asStateFlow()

    // Navigation and UI state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeCategory = MutableStateFlow<Category?>(null)
    val activeCategory: StateFlow<Category?> = _activeCategory.asStateFlow()

    private val _activeStore = MutableStateFlow<Store?>(null)
    val activeStore: StateFlow<Store?> = _activeStore.asStateFlow()

    private val _activeStoreSection = MutableStateFlow("ALL")
    val activeStoreSection: StateFlow<String> = _activeStoreSection.asStateFlow()

    private val _detailsProduct = MutableStateFlow<Product?>(null)
    val detailsProduct: StateFlow<Product?> = _detailsProduct.asStateFlow()

    private val _storeInfoDialog = MutableStateFlow<Store?>(null)
    val storeInfoDialog: StateFlow<Store?> = _storeInfoDialog.asStateFlow()

    private val _chatStore = MutableStateFlow<Store?>(null)
    val chatStore: StateFlow<Store?> = _chatStore.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isCartOpen = MutableStateFlow(false)
    val isCartOpen: StateFlow<Boolean> = _isCartOpen.asStateFlow()

    private val _isNavDrawerOpen = MutableStateFlow(false)
    val isNavDrawerOpen: StateFlow<Boolean> = _isNavDrawerOpen.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    // Search and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _storeSearchQuery = MutableStateFlow("")
    val storeSearchQuery: StateFlow<String> = _storeSearchQuery.asStateFlow()

    private val _locationFilter = MutableStateFlow("")
    val locationFilter: StateFlow<String> = _locationFilter.asStateFlow()

    private val _storeSort = MutableStateFlow("default")
    val storeSort: StateFlow<String> = _storeSort.asStateFlow()

    private val _storeCols = MutableStateFlow(2)
    val storeCols: StateFlow<Int> = _storeCols.asStateFlow()

    // Cart and checkout
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<PromoCode?>(null)
    val appliedCoupon: StateFlow<PromoCode?> = _appliedCoupon.asStateFlow()

    // Local state
    private val _customerProfile = MutableStateFlow(CustomerProfile())
    val customerProfile: StateFlow<CustomerProfile> = _customerProfile.asStateFlow()

    private val _wishlist = MutableStateFlow<Set<String>>(emptySet())
    val wishlist: StateFlow<Set<String>> = _wishlist.asStateFlow()

    private val _orderHistory = MutableStateFlow<List<OrderHistoryItem>>(emptyList())
    val orderHistory: StateFlow<List<OrderHistoryItem>> = _orderHistory.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _flashRemainingMs = MutableStateFlow(0L)
    val flashRemainingMs: StateFlow<Long> = _flashRemainingMs.asStateFlow()

    private var tickerJob: Job? = null
    private var chatPollJob: Job? = null

    init {
        loadLocalData()
        fetchData()
        startCountdownTicker()
    }

    private fun loadLocalData() {
        val name = prefs.getString("user_name", "") ?: ""
        val phone = prefs.getString("user_phone", "") ?: ""
        val addr = prefs.getString("user_address", "") ?: ""
        _customerProfile.value = CustomerProfile(name, phone, addr)

        val favSet = prefs.getStringSet("wishlist_ids", emptySet()) ?: emptySet()
        _wishlist.value = favSet

        val ordersJson = prefs.getString("order_history_json", null)
        if (!ordersJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(ordersJson)
                val list = mutableListOf<OrderHistoryItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        OrderHistoryItem(
                            id = o.optLong("id"),
                            date = o.optString("date"),
                            storeName = o.optString("storeName"),
                            storeId = o.optString("storeId"),
                            storeWhatsapp = o.optString("storeWhatsapp"),
                            total = o.optDouble("total"),
                            customerName = o.optString("customerName"),
                            customerPhone = o.optString("customerPhone"),
                            customerAddress = o.optString("customerAddress"),
                            coupon = if (o.has("coupon")) o.optString("coupon") else null,
                            status = o.optString("status", "new")
                        )
                    )
                }
                _orderHistory.value = list
            } catch (_: Exception) {}
        }
    }

    private fun saveOrdersLocally(list: List<OrderHistoryItem>) {
        try {
            val arr = JSONArray()
            list.forEach { o ->
                val obj = JSONObject().apply {
                    put("id", o.id)
                    put("date", o.date)
                    put("storeName", o.storeName)
                    put("storeId", o.storeId)
                    put("storeWhatsapp", o.storeWhatsapp)
                    put("total", o.total)
                    put("customerName", o.customerName)
                    put("customerPhone", o.customerPhone)
                    put("customerAddress", o.customerAddress)
                    if (o.coupon != null) put("coupon", o.coupon)
                    put("status", o.status)
                }
                arr.put(obj)
            }
            prefs.edit().putString("order_history_json", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun fetchData() {
        viewModelScope.launch {
            _settings.value = repo.getSettings()
            _categories.value = repo.getCategories()
            _stores.value = repo.getStores()
            _products.value = repo.getProducts()
            _banners.value = repo.getBanners()
            _ads.value = repo.getAds()
            _promoCodes.value = repo.getPromoCodes()
        }
    }

    private fun startCountdownTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val flashProducts = _products.value.filter { !it.flashSaleEndTime.isNullOrBlank() }
                if (flashProducts.isNotEmpty()) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    var nearestMs = Long.MAX_VALUE
                    flashProducts.forEach { p ->
                        try {
                            val timeStr = p.flashSaleEndTime!!.replace("Z", "")
                            val date = sdf.parse(timeStr)
                            if (date != null && date.time > now) {
                                if (date.time < nearestMs) nearestMs = date.time
                            }
                        } catch (_: Exception) {}
                    }
                    if (nearestMs != Long.MAX_VALUE) {
                        _flashRemainingMs.value = (nearestMs - now).coerceAtLeast(0L)
                    } else {
                        // default 24h countdown if date parse is relative
                        _flashRemainingMs.value = (24 * 3600 * 1000L) - (now % (24 * 3600 * 1000L))
                    }
                } else {
                    _flashRemainingMs.value = 0L
                }
                delay(1000)
            }
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        _detailsProduct.value = null
        if (screen == Screen.HOME) {
            _activeCategory.value = null
            _activeStore.value = null
        }
    }

    fun openCategory(category: Category) {
        _activeCategory.value = category
        _currentScreen.value = Screen.CATEGORY
        _detailsProduct.value = null
    }

    fun openStore(store: Store) {
        _activeStore.value = store
        _activeStoreSection.value = "ALL"
        _storeSearchQuery.value = ""
        _currentScreen.value = Screen.STORE
        _detailsProduct.value = null
        viewModelScope.launch {
            _storeBanners.value = repo.getStoreBanners(store.id)
            _storeSections.value = repo.getStoreSections(store.id)
        }
    }

    fun openProductDetails(product: Product) {
        _detailsProduct.value = product
    }

    fun closeProductDetails() {
        _detailsProduct.value = null
    }

    fun openStoreInfo(store: Store) {
        _storeInfoDialog.value = store
    }

    fun closeStoreInfo() {
        _storeInfoDialog.value = null
    }

    fun openChat(store: Store) {
        _chatStore.value = store
        _storeInfoDialog.value = null
        pollChat(store.id)
    }

    fun closeChat() {
        _chatStore.value = null
        chatPollJob?.cancel()
    }

    private fun pollChat(storeId: String) {
        chatPollJob?.cancel()
        chatPollJob = viewModelScope.launch {
            while (_chatStore.value != null) {
                val phone = _customerProfile.value.phone
                if (phone.isNotBlank()) {
                    _chatMessages.value = repo.getChatMessages(storeId, phone)
                }
                delay(4000)
            }
        }
    }

    fun sendChatMessage(text: String) {
        val store = _chatStore.value ?: return
        val profile = _customerProfile.value
        if (profile.name.isBlank() || profile.phone.isBlank()) {
            showToast("من فضلك سجّل بياناتك أولاً لبدء المحادثة")
            _showProfileDialog.value = true
            return
        }
        viewModelScope.launch {
            val success = repo.sendChatMessage(
                storeId = store.id,
                storeName = store.name,
                customerName = profile.name,
                customerPhone = profile.phone,
                text = text
            )
            if (success) {
                _chatMessages.value = repo.getChatMessages(store.id, profile.phone)
            } else {
                showToast("تعذر إرسال الرسالة، حاول مرة أخرى")
            }
        }
    }

    fun setNavDrawerOpen(open: Boolean) {
        _isNavDrawerOpen.value = open
    }

    fun setCartOpen(open: Boolean) {
        _isCartOpen.value = open
    }

    fun setShowProfileDialog(show: Boolean) {
        _showProfileDialog.value = show
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setStoreSearchQuery(q: String) {
        _storeSearchQuery.value = q
    }

    fun setLocationFilter(loc: String) {
        _locationFilter.value = loc
    }

    fun setStoreSection(sec: String) {
        _activeStoreSection.value = sec
    }

    fun setStoreSort(sort: String) {
        _storeSort.value = sort
    }

    fun setStoreCols(cols: Int) {
        _storeCols.value = cols
    }

    fun toggleWishlist(id: String) {
        val current = _wishlist.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
            showToast("تم الحذف من المفضلة")
        } else {
            current.add(id)
            showToast("تمت الإضافة للمفضلة")
        }
        _wishlist.value = current
        prefs.edit().putStringSet("wishlist_ids", current).apply()
    }

    fun saveCustomerProfile(name: String, phone: String, address: String) {
        val p = CustomerProfile(name.trim(), phone.trim(), address.trim())
        _customerProfile.value = p
        prefs.edit()
            .putString("user_name", p.name)
            .putString("user_phone", p.phone)
            .putString("user_address", p.address)
            .apply()
        _showProfileDialog.value = false
        viewModelScope.launch {
            repo.saveCustomer(p)
        }
        showToast("تم حفظ البيانات بنجاح")
    }

    fun addToCart(
        product: Product,
        quantity: Int = 1,
        selectedSize: String? = null,
        selectedColor: String? = null,
        selectedFlavor: String? = null,
        selectedWeight: ProductWeight? = null,
        selectedExtras: List<ProductExtra> = emptyList(),
        specialNotes: String = ""
    ): Boolean {
        // Single store restriction check
        if (_cart.value.isNotEmpty() && _cart.value.first().product.storeId != product.storeId) {
            showToast("لا يمكن الطلب من متجرين مختلفين في نفس السلة. أفرغ السلة أولاً.")
            return false
        }

        // Stock availability check
        if (product.stock != null && product.stock <= 0) {
            showToast("عذراً، هذا المنتج غير متوفر في المخزون")
            return false
        }

        val basePrice = selectedWeight?.price ?: product.price
        val extrasSum = selectedExtras.sumOf { it.price }
        val finalItemPrice = basePrice + extrasSum

        val list = _cart.value.toMutableList()
        val index = list.indexOfFirst {
            it.product.id == product.id &&
            it.selectedSize == selectedSize &&
            it.selectedColor == selectedColor &&
            it.selectedFlavor == selectedFlavor &&
            it.selectedWeight?.name == selectedWeight?.name &&
            it.selectedExtras.size == selectedExtras.size
        }

        if (index >= 0) {
            val existing = list[index]
            val newQty = existing.quantity + quantity
            if (product.stock != null && newQty > product.stock) {
                showToast("الكمية المطلوبة تتجاوز المخزون المتوفر (${product.stock})")
                return false
            }
            list[index] = existing.copy(quantity = newQty)
        } else {
            list.add(
                CartItem(
                    product = product,
                    quantity = quantity,
                    selectedSize = selectedSize,
                    selectedColor = selectedColor,
                    selectedFlavor = selectedFlavor,
                    selectedWeight = selectedWeight,
                    selectedExtras = selectedExtras,
                    specialNotes = specialNotes,
                    finalPrice = finalItemPrice
                )
            )
        }

        _cart.value = list
        showToast("تمت الإضافة للسلة")
        return true
    }

    fun updateCartQuantity(item: CartItem, delta: Int) {
        val list = _cart.value.toMutableList()
        val idx = list.indexOf(item)
        if (idx >= 0) {
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                list.removeAt(idx)
                showToast("تم حذف المنتج من السلة")
            } else {
                if (item.product.stock != null && newQty > item.product.stock) {
                    showToast("الكمية المتاحة: ${item.product.stock}")
                    return
                }
                list[idx] = item.copy(quantity = newQty)
            }
            _cart.value = list
        }
    }

    fun removeFromCart(item: CartItem) {
        val list = _cart.value.toMutableList()
        list.remove(item)
        _cart.value = list
        showToast("تم الحذف من السلة")
    }

    fun clearCart() {
        _cart.value = emptyList()
        _appliedCoupon.value = null
    }

    fun applyCoupon(code: String) {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank()) return

        val found = _promoCodes.value[trimmed]
        if (found != null && found.active) {
            _appliedCoupon.value = found
            showToast("تم تطبيق كود الخصم بنجاح! خصم ${(found.percent * 100).toInt()}%")
        } else if (trimmed == "MATROUH20") {
            val c = PromoCode("MATROUH20", 0.20)
            _appliedCoupon.value = c
            showToast("تم تطبيق كود الخصم بنجاح! خصم 20%")
        } else if (trimmed == "WELCOME10") {
            val c = PromoCode("WELCOME10", 0.10)
            _appliedCoupon.value = c
            showToast("تم تطبيق كود الخصم بنجاح! خصم 10%")
        } else {
            showToast("كود الخصم غير صحيح أو غير متاح")
        }
    }

    fun checkout(context: Context, customerName: String, customerPhone: String, customerAddress: String) {
        if (_cart.value.isEmpty()) {
            showToast("السلة فارغة")
            return
        }

        val storeId = _cart.value.first().product.storeId
        val store = _stores.value.find { it.id == storeId } ?: FallbackData.stores.first()

        // Calculate totals
        val subTotal = _cart.value.sumOf { it.finalPrice * it.quantity }
        val discount = if (_appliedCoupon.value != null) subTotal * _appliedCoupon.value!!.percent else 0.0
        val finalTotal = subTotal - discount

        // Format WhatsApp message exactly as in web app
        val sb = StringBuilder()
        sb.append("*طلب جديد من تطبيق مول مطروح*\n\n")
        sb.append("*اسم العميل:* $customerName\n")
        sb.append("*العنوان:* $customerAddress\n")
        sb.append("*رقم الهاتف:* $customerPhone\n\n")
        sb.append("*الطلبات:*\n")

        _cart.value.forEach { item ->
            val itemTotal = item.finalPrice * item.quantity
            sb.append("- ${item.product.name} ")
            item.selectedWeight?.let { sb.append("[${it.name}] ") }
            item.selectedSize?.let { sb.append("[${it}] ") }
            item.selectedColor?.let { sb.append("[${it}] ") }
            item.selectedFlavor?.let { sb.append("[${it}] ") }
            if (item.selectedExtras.isNotEmpty()) {
                sb.append("(إضافات: ${item.selectedExtras.joinToString(", ") { it.name }}) ")
            }
            if (item.specialNotes.isNotBlank()) {
                sb.append("(ملاحظة: ${item.specialNotes}) ")
            }
            sb.append("x ${item.quantity} = ${itemTotal.toInt()} ج.م\n")
        }

        _appliedCoupon.value?.let {
            sb.append("\n*كود الخصم:* ${it.code} (خصم ${discount.toInt()} ج.م)")
        }
        sb.append("\n*الإجمالي النهائي:* ${finalTotal.toInt()} ج.م")

        // Save new order to history
        val newOrder = OrderHistoryItem(
            id = System.currentTimeMillis(),
            date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()),
            storeName = store.name,
            storeId = store.id,
            storeWhatsapp = store.whatsapp,
            items = _cart.value,
            total = finalTotal,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            coupon = _appliedCoupon.value?.code,
            status = "new"
        )
        val updatedHistory = listOf(newOrder) + _orderHistory.value
        _orderHistory.value = updatedHistory
        saveOrdersLocally(updatedHistory)

        // Post order to Firebase
        viewModelScope.launch {
            repo.postOrder(newOrder)
            repo.saveCustomer(CustomerProfile(customerName, customerPhone, customerAddress))
        }

        // Open WhatsApp
        val encodedMsg = URLEncoder.encode(sb.toString(), "UTF-8")
        val cleanWhatsapp = store.whatsapp.filter { it.isDigit() }
        val waUrl = "https://wa.me/$cleanWhatsapp?text=$encodedMsg"

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(waUrl)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }

        // Clear Cart
        _cart.value = emptyList()
        _appliedCoupon.value = null
        _isCartOpen.value = false
        showToast("تم إرسال الطلب عبر واتساب بنجاح!")
    }

    fun reorder(order: OrderHistoryItem) {
        if (_cart.value.isNotEmpty() && _cart.value.first().product.storeId != order.storeId) {
            clearCart()
        }
        order.items.forEach { item ->
            addToCart(
                product = item.product,
                quantity = item.quantity,
                selectedSize = item.selectedSize,
                selectedColor = item.selectedColor,
                selectedFlavor = item.selectedFlavor,
                selectedWeight = item.selectedWeight,
                selectedExtras = item.selectedExtras,
                specialNotes = item.specialNotes
            )
        }
        _isCartOpen.value = true
        showToast("تمت إضافة عناصر الطلب إلى السلة")
    }

    fun deleteOrder(orderId: Long) {
        val updated = _orderHistory.value.filter { it.id != orderId }
        _orderHistory.value = updated
        saveOrdersLocally(updated)
        showToast("تم حذف الطلب من السجل")
    }
}
