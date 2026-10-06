package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun TopHeader(
    appName: String,
    logoUrl: String,
    notifications: List<String>,
    cartCount: Int,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onLogoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeNotifIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(notifications) {
        if (notifications.size > 1) {
            while (true) {
                delay(4000)
                activeNotifIndex = (activeNotifIndex + 1) % notifications.size
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(2.dp)
    ) {
        // Top Announcement Ticker Bar
        val currentNotification = if (notifications.isNotEmpty()) notifications[activeNotifIndex % notifications.size] else "✨ مرحباً بك في مول مطروح"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MatrouhPrimaryDark)
                .padding(vertical = 5.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MatrouhAccent,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                AnimatedContent(
                    targetState = currentNotification,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "Ticker"
                ) { notif ->
                    Text(
                        text = notif,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Main Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onLogoClick)
                    .testTag("mall_logo_button")
            ) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = "شعار مول مطروح",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, MatrouhPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = appName,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "MATROUH MALL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = TextMuted,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Actions (Drawer & Cart)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("menu_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "القائمة",
                        tint = TextPrimary
                    )
                }

                Box {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MatrouhPrimary.copy(alpha = 0.08f))
                            .testTag("cart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = "سلة المشتريات",
                            tint = MatrouhPrimary
                        )
                    }
                    if (cartCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MatrouhAccent)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartCount.toString(),
                                color = MatrouhPrimaryDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    cartCount: Int,
    onNavigate: (Screen) -> Unit,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 12.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                Triple(Screen.HOME, "الرئيسية", Icons.Default.Home),
                Triple(Screen.ALL_FEATURED_PRODUCTS, "العروض", Icons.Default.Bolt),
                Triple(Screen.WISHLIST, "المفضلة", Icons.Default.Favorite),
                Triple(Screen.ORDERS, "طلباتي", Icons.Default.ReceiptLong)
            )

            navItems.forEach { (screen, label, icon) ->
                val isSelected = currentScreen == screen
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigate(screen) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_tab_${screen.name}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MatrouhPrimary else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MatrouhPrimary else TextMuted
                    )
                }
            }

            // Cart Item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenCart() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("nav_tab_cart")
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "السلة",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (cartCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MatrouhAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartCount.toString(),
                                color = MatrouhPrimaryDark,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = "السلة",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun HeroBannerCarousel(
    banners: List<BannerItem>,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(banners) {
        if (banners.size > 1) {
            while (true) {
                delay(4500)
                currentIndex = (currentIndex + 1) % banners.size
            }
        }
    }

    val banner = banners[currentIndex % banners.size]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFE2E8F0))
                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(22.dp))
                .shadow(4.dp, RoundedCornerShape(22.dp))
        ) {
            AsyncImage(
                model = banner.image,
                contentDescription = banner.title ?: "إعلان بنر",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 100f
                        )
                    )
            )

            if (!banner.title.isNullOrBlank()) {
                Text(
                    text = banner.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                )
            }

            // Indicator Dots
            if (banners.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    banners.forEachIndexed { idx, _ ->
                        val isSel = idx == currentIndex % banners.size
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(6.dp)
                                .width(if (isSel) 22.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSel) Color.White else Color.White.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlashSaleSection(
    flashProducts: List<Product>,
    remainingMs: Long,
    onSeeAll: () -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    if (flashProducts.isEmpty()) return

    val totalSec = (remainingMs / 1000).coerceAtLeast(0)
    val days = totalSec / 86400
    val hours = (totalSec % 86400) / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MatrouhRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "فلاش سيل",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Countdown Boxes
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MatrouhPrimaryDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (days > 0) {
                        Text(text = "${days}d ", color = MatrouhAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onSeeAll,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("المزيد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MatrouhPrimary)
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Card Container Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(MatrouhPrimaryDark, MatrouhPrimary)
                    )
                )
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                flashProducts.forEach { product ->
                    val discount = if (product.oldPrice != null && product.oldPrice > product.price) {
                        (((product.oldPrice - product.price) / product.oldPrice) * 100).toInt()
                    } else 0

                    Card(
                        modifier = Modifier
                            .width(145.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onProductClick(product) }
                            .testTag("flash_product_${product.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                            ) {
                                AsyncImage(
                                    model = product.images.firstOrNull(),
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (discount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MatrouhRed)
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "%$discount-",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${product.price.toInt()} ج.م",
                                    color = MatrouhPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                IconButton(
                                    onClick = { onQuickAdd(product) },
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MatrouhPrimary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "إضافة",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
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

@Composable
fun StoreCard(
    store: Store,
    isLiked: Boolean,
    onStoreClick: () -> Unit,
    onInfoClick: () -> Unit,
    onLikeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onStoreClick)
            .testTag("store_card_${store.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column {
            // Store Cover Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFE2E8F0))
            ) {
                AsyncImage(
                    model = store.coverImage.ifBlank { store.image },
                    contentDescription = store.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Featured Badge
                if (store.isFeatured) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MatrouhAccent)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = MatrouhPrimaryDark, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("مميز", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MatrouhPrimaryDark)
                        }
                    }
                }

                // Quick Action Buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                            .clickable(onClick = onLikeToggle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isLiked) MatrouhRed else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                            .clickable(onClick = onInfoClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "معلومات المتجر",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Store Info & Logo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-20).dp)
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                            .shadow(3.dp, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = store.logo.ifBlank { store.image },
                            contentDescription = store.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.offset(y = (-8).dp)) {
                        Text(
                            text = store.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = TextPrimary
                        )
                        Text(
                            text = store.location,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = store.tagline.ifBlank { store.description },
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset(y = (-6).dp)
                )

                Button(
                    onClick = onStoreClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MatrouhPrimary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("دخول المتجر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FeaturedStoreTile(
    store: Store,
    isLiked: Boolean,
    onEnter: () -> Unit,
    onLikeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onEnter)
            .testTag("featured_store_tile_${store.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Color(0xFFF1F5F9))
            ) {
                AsyncImage(
                    model = store.coverImage.ifBlank { store.image },
                    contentDescription = store.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(onClick = onLikeToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) MatrouhRed else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .offset(y = (-18).dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.White, CircleShape)
                    .shadow(2.dp, CircleShape)
            ) {
                AsyncImage(
                    model = store.logo.ifBlank { store.image },
                    contentDescription = store.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = store.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = store.location,
                    fontSize = 9.sp,
                    color = TextMuted,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MatrouhPrimary)
                        .padding(vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("دخول", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(
    product: Product,
    isLiked: Boolean,
    onDetails: () -> Unit,
    onAdd: () -> Unit,
    onLikeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discount = if (product.oldPrice != null && product.oldPrice > product.price) {
        (((product.oldPrice - product.price) / product.oldPrice) * 100).toInt()
    } else 0

    val inStock = product.stock == null || product.stock > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onDetails)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Flash Sale Top Ribbon if active
            if (!product.flashSaleEndTime.isNullOrBlank() && inStock) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFB91C1C), Color(0xFFEF4444), Color(0xFFF97316))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = MatrouhAccentLight, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("فلاش سيل", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        Text("عروض خاصة", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Image Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .background(Color(0xFFF8FAFC))
            ) {
                AsyncImage(
                    model = product.images.firstOrNull(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Discount Badge
                if (discount > 0 && inStock) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MatrouhRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "%$discount-",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Top Right: Favorite button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(onClick = onLikeToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "إعجاب",
                        tint = if (isLiked) MatrouhRed else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Out of stock overlay
                if (!inStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("نفذت الكمية", color = MatrouhRed, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Details
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                // Sizes & Colors chips if available
                if (product.colors.isNotEmpty() || product.sizes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        product.colors.take(3).forEach { c ->
                            if (c.startsWith("#") && c.length in listOf(4, 7)) {
                                val col = try { Color(android.graphics.Color.parseColor(c)) } catch (_: Exception) { Color.Gray }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(0.5.dp, Color.Gray, CircleShape)
                                )
                            }
                        }
                        product.sizes.take(2).forEach { s ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(s, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        if (product.oldPrice != null && product.oldPrice > product.price) {
                            Text(
                                text = "${product.oldPrice.toInt()} ج.م",
                                fontSize = 10.sp,
                                color = TextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                        Text(
                            text = "${product.price.toInt()} ج.م",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = MatrouhPrimary
                        )
                    }

                    IconButton(
                        onClick = onAdd,
                        enabled = inStock,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (inStock) MatrouhPrimary else Color(0xFFCBD5E1))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "إضافة للسلة",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
