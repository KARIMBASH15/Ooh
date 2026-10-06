package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Category
import com.example.ui.MallViewModel
import com.example.ui.Screen
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MatrouhMallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MatrouhMallTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MatrouhMallApp()
                }
            }
        }
    }
}

@Composable
fun MatrouhMallApp(viewModel: MallViewModel = viewModel()) {
    val context = LocalContext.current

    // State collected from ViewModel
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val stores by viewModel.stores.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val ads by viewModel.ads.collectAsStateWithLifecycle()
    val storeBanners by viewModel.storeBanners.collectAsStateWithLifecycle()
    val storeSections by viewModel.storeSections.collectAsStateWithLifecycle()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeCategory by viewModel.activeCategory.collectAsStateWithLifecycle()
    val activeStore by viewModel.activeStore.collectAsStateWithLifecycle()
    val activeStoreSection by viewModel.activeStoreSection.collectAsStateWithLifecycle()

    val detailsProduct by viewModel.detailsProduct.collectAsStateWithLifecycle()
    val storeInfoDialog by viewModel.storeInfoDialog.collectAsStateWithLifecycle()
    val chatStore by viewModel.chatStore.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()

    val isCartOpen by viewModel.isCartOpen.collectAsStateWithLifecycle()
    val isNavDrawerOpen by viewModel.isNavDrawerOpen.collectAsStateWithLifecycle()
    val showProfileDialog by viewModel.showProfileDialog.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val storeSearchQuery by viewModel.storeSearchQuery.collectAsStateWithLifecycle()
    val locationFilter by viewModel.locationFilter.collectAsStateWithLifecycle()
    val storeSort by viewModel.storeSort.collectAsStateWithLifecycle()
    val storeCols by viewModel.storeCols.collectAsStateWithLifecycle()

    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val customerProfile by viewModel.customerProfile.collectAsStateWithLifecycle()
    val orderHistory by viewModel.orderHistory.collectAsStateWithLifecycle()
    val wishlist by viewModel.wishlist.collectAsStateWithLifecycle()
    val flashRemainingMs by viewModel.flashRemainingMs.collectAsStateWithLifecycle()

    val flashProducts = remember(products) { products.filter { !it.flashSaleEndTime.isNullOrBlank() } }
    val cartCount = remember(cart) { cart.sumOf { it.quantity } }

    // Handle Hardware & Gesture Back
    BackHandler(
        enabled = detailsProduct != null || isCartOpen || isNavDrawerOpen || storeInfoDialog != null || chatStore != null || showProfileDialog || currentScreen != Screen.HOME
    ) {
        when {
            detailsProduct != null -> viewModel.closeProductDetails()
            isCartOpen -> viewModel.setCartOpen(false)
            isNavDrawerOpen -> viewModel.setNavDrawerOpen(false)
            storeInfoDialog != null -> viewModel.closeStoreInfo()
            chatStore != null -> viewModel.closeChat()
            showProfileDialog -> viewModel.setShowProfileDialog(false)
            currentScreen != Screen.HOME -> viewModel.navigateTo(Screen.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            if (currentScreen != Screen.STORE && detailsProduct == null) {
                TopHeader(
                    appName = settings.appName,
                    logoUrl = settings.logoUrl,
                    notifications = settings.notifications,
                    cartCount = cartCount,
                    onMenuClick = { viewModel.setNavDrawerOpen(true) },
                    onCartClick = { viewModel.setCartOpen(true) },
                    onLogoClick = { viewModel.navigateTo(Screen.HOME) }
                )
            }
        },
        bottomBar = {
            if (detailsProduct == null) {
                BottomNavBar(
                    currentScreen = currentScreen,
                    cartCount = cartCount,
                    onNavigate = { viewModel.navigateTo(it) },
                    onOpenCart = { viewModel.setCartOpen(true) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> {
                    HomeScreen(
                        banners = banners,
                        categories = categories,
                        stores = stores,
                        products = products,
                        ads = ads,
                        flashProducts = flashProducts,
                        flashRemainingMs = flashRemainingMs,
                        wishlist = wishlist,
                        searchQuery = searchQuery,
                        locationFilter = locationFilter,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onLocationChange = { viewModel.setLocationFilter(it) },
                        onProductClick = { viewModel.openProductDetails(it) },
                        onStoreClick = { viewModel.openStore(it) },
                        onCategoryClick = { viewModel.openCategory(it) },
                        onQuickAdd = { viewModel.addToCart(it) },
                        onLikeToggle = { viewModel.toggleWishlist(it) },
                        onNavigate = { viewModel.navigateTo(it) },
                        onStoreInfo = { viewModel.openStoreInfo(it) }
                    )
                }

                Screen.CATEGORY -> {
                    activeCategory?.let { category ->
                        CategoryScreen(
                            category = category,
                            stores = stores,
                            wishlist = wishlist,
                            onBack = { viewModel.navigateTo(Screen.HOME) },
                            onStoreClick = { viewModel.openStore(it) },
                            onStoreInfo = { viewModel.openStoreInfo(it) },
                            onLikeToggle = { viewModel.toggleWishlist(it) }
                        )
                    } ?: viewModel.navigateTo(Screen.HOME)
                }

                Screen.STORE -> {
                    activeStore?.let { store ->
                        StoreScreen(
                            store = store,
                            products = products,
                            banners = storeBanners,
                            sections = storeSections,
                            activeSection = activeStoreSection,
                            searchQuery = storeSearchQuery,
                            sortOption = storeSort,
                            columnCount = storeCols,
                            wishlist = wishlist,
                            onBack = { viewModel.navigateTo(Screen.HOME) },
                            onSectionChange = { viewModel.setStoreSection(it) },
                            onSearchChange = { viewModel.setStoreSearchQuery(it) },
                            onSortChange = { viewModel.setStoreSort(it) },
                            onColsChange = { viewModel.setStoreCols(it) },
                            onProductClick = { viewModel.openProductDetails(it) },
                            onQuickAdd = { viewModel.addToCart(it) },
                            onLikeToggle = { viewModel.toggleWishlist(it) },
                            onStoreInfo = { viewModel.openStoreInfo(store) }
                        )
                    } ?: viewModel.navigateTo(Screen.HOME)
                }

                Screen.FLASH_SALE -> {
                    FlashSaleScreen(
                        flashProducts = flashProducts,
                        remainingMs = flashRemainingMs,
                        wishlist = wishlist,
                        onBack = { viewModel.navigateTo(Screen.HOME) },
                        onProductClick = { viewModel.openProductDetails(it) },
                        onQuickAdd = { viewModel.addToCart(it) },
                        onLikeToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                Screen.ALL_FEATURED_PRODUCTS -> {
                    val featuredList = remember(products) { products.filter { it.isFeatured } }
                    FlashSaleScreen(
                        flashProducts = featuredList,
                        remainingMs = 0L,
                        wishlist = wishlist,
                        onBack = { viewModel.navigateTo(Screen.HOME) },
                        onProductClick = { viewModel.openProductDetails(it) },
                        onQuickAdd = { viewModel.addToCart(it) },
                        onLikeToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                Screen.ALL_FEATURED_STORES -> {
                    val featuredStoresList = remember(stores) { stores.filter { it.isFeatured } }
                    CategoryScreen(
                        category = Category(name = "متاجر مميزة"),
                        stores = featuredStoresList,
                        wishlist = wishlist,
                        onBack = { viewModel.navigateTo(Screen.HOME) },
                        onStoreClick = { viewModel.openStore(it) },
                        onStoreInfo = { viewModel.openStoreInfo(it) },
                        onLikeToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                Screen.WISHLIST -> {
                    WishlistScreen(
                        stores = stores,
                        products = products,
                        wishlist = wishlist,
                        onProductClick = { viewModel.openProductDetails(it) },
                        onStoreClick = { viewModel.openStore(it) },
                        onQuickAdd = { viewModel.addToCart(it) },
                        onLikeToggle = { viewModel.toggleWishlist(it) }
                    )
                }

                Screen.ORDERS -> {
                    OrdersScreen(
                        orders = orderHistory,
                        customerProfile = customerProfile,
                        onEditProfile = { viewModel.setShowProfileDialog(true) },
                        onReorder = { viewModel.reorder(it) },
                        onDeleteOrder = { viewModel.deleteOrder(it) }
                    )
                }
            }

            // Product Details Full Screen / Modal
            detailsProduct?.let { product ->
                val currentStore = remember(product, stores) { stores.find { it.id == product.storeId } }
                ProductDetailsDialog(
                    product = product,
                    store = currentStore,
                    isLiked = wishlist.contains(product.id),
                    onClose = { viewModel.closeProductDetails() },
                    onLikeToggle = { viewModel.toggleWishlist(product.id) },
                    onAddToCart = { qty, sz, col, flav, weight, extras, notes ->
                        viewModel.addToCart(
                            product = product,
                            quantity = qty,
                            selectedSize = sz,
                            selectedColor = col,
                            selectedFlavor = flav,
                            selectedWeight = weight,
                            selectedExtras = extras,
                            specialNotes = notes
                        )
                    }
                )
            }

            // Cart Modal
            val cartStore = remember(cart, stores) {
                if (cart.isNotEmpty()) stores.find { it.id == cart.first().product.storeId } else null
            }
            CartDialog(
                isOpen = isCartOpen,
                cart = cart,
                store = cartStore,
                appliedCoupon = appliedCoupon,
                customerProfile = customerProfile,
                onClose = { viewModel.setCartOpen(false) },
                onUpdateQty = { item, delta -> viewModel.updateCartQuantity(item, delta) },
                onRemoveItem = { item -> viewModel.removeFromCart(item) },
                onApplyCoupon = { code -> viewModel.applyCoupon(code) },
                onSaveProfile = { n, p, a -> viewModel.saveCustomerProfile(n, p, a) },
                onCheckout = { n, p, a -> viewModel.checkout(context, n, p, a) }
            )

            // Store Info Dialog
            storeInfoDialog?.let { store ->
                StoreInfoDialog(
                    store = store,
                    onDismiss = { viewModel.closeStoreInfo() },
                    onStartChat = { viewModel.openChat(store) }
                )
            }

            // Customer Chat Dialog
            chatStore?.let { store ->
                CustomerChatDialog(
                    store = store,
                    messages = chatMessages,
                    onDismiss = { viewModel.closeChat() },
                    onSendMessage = { text -> viewModel.sendChatMessage(text) }
                )
            }

            // Customer Profile Dialog
            if (showProfileDialog) {
                CustomerProfileDialog(
                    profile = customerProfile,
                    onDismiss = { viewModel.setShowProfileDialog(false) },
                    onSave = { n, p, a -> viewModel.saveCustomerProfile(n, p, a) }
                )
            }

            // Nav Drawer
            NavDrawer(
                isOpen = isNavDrawerOpen,
                logoUrl = settings.logoUrl,
                categories = categories,
                onClose = { viewModel.setNavDrawerOpen(false) },
                onNavigate = { viewModel.navigateTo(it) },
                onCategoryClick = { viewModel.openCategory(it) }
            )
        }
    }
}
