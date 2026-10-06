package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.ui.Screen
import com.example.ui.theme.*

@Composable
fun NavDrawer(
    isOpen: Boolean,
    logoUrl: String,
    categories: List<Category>,
    onClose: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClose)
            .testTag("nav_drawer_overlay")
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(290.dp)
                .align(Alignment.CenterEnd)
                .background(Color.White)
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MatrouhPrimaryDark, MatrouhPrimary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مول مطروح",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = logoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("أهلاً بك 👋", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("تسوق بذكاء وأمان", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Drawer Links List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                ) {
                    item {
                        Text(
                            text = "التنقل الرئيسي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }

                    val mainLinks = listOf(
                        Triple("الرئيسية", Icons.Default.Home, Screen.HOME),
                        Triple("العروض المميزة", Icons.Default.Star, Screen.ALL_FEATURED_PRODUCTS),
                        Triple("المتاجر", Icons.Default.Store, Screen.ALL_FEATURED_STORES),
                        Triple("فلاش سيل", Icons.Default.Bolt, Screen.FLASH_SALE),
                        Triple("المفضلة", Icons.Default.Favorite, Screen.WISHLIST),
                        Triple("سجل طلباتي", Icons.Default.ReceiptLong, Screen.ORDERS)
                    )

                    items(mainLinks) { (title, icon, screen) ->
                        DrawerRowItem(
                            title = title,
                            icon = icon,
                            onClick = {
                                onNavigate(screen)
                                onClose()
                            }
                        )
                    }

                    if (categories.isNotEmpty()) {
                        item {
                            Divider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp), color = BorderLight)
                            Text(
                                text = "الأقسام",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }

                        items(categories) { category ->
                            DrawerRowItem(
                                title = category.name,
                                icon = getCategoryIcon(category.icon),
                                onClick = {
                                    onCategoryClick(category)
                                    onClose()
                                }
                            )
                        }
                    }
                }

                // Drawer Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("الإصدار 2.0.0", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                        Text("صنع بكل حب لمطروح ❤️", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerRowItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MatrouhPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
    }
}

fun getCategoryIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "shoppingbag", "supermarket", "shopping_bag" -> Icons.Default.ShoppingBag
        "utensils", "restaurant", "food" -> Icons.Default.Restaurant
        "shirt", "clothes", "fashion" -> Icons.Default.Checkroom
        "smartphone", "phones", "tech" -> Icons.Default.Smartphone
        "sparkles", "beauty", "perfume" -> Icons.Default.AutoAwesome
        "gift", "sweets" -> Icons.Default.CardGiftcard
        "tag", "discount" -> Icons.Default.LocalOffer
        else -> Icons.Default.Category
    }
}
