package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.Screen
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HomeScreen(
    banners: List<BannerItem>,
    categories: List<Category>,
    stores: List<Store>,
    products: List<Product>,
    ads: List<AdItem>,
    flashProducts: List<Product>,
    flashRemainingMs: Long,
    wishlist: Set<String>,
    searchQuery: String,
    locationFilter: String,
    onSearchChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onStoreClick: (Store) -> Unit,
    onCategoryClick: (Category) -> Unit,
    onQuickAdd: (Product) -> Unit,
    onLikeToggle: (String) -> Unit,
    onNavigate: (Screen) -> Unit,
    onStoreInfo: (Store) -> Unit,
    modifier: Modifier = Modifier
) {
    val uniqueLocations = remember(stores) {
        stores.map { it.location }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val isSearching = searchQuery.isNotBlank() || locationFilter.isNotBlank()

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products else products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val filteredStores = remember(stores, searchQuery, locationFilter) {
        stores.filter { store ->
            val matchSearch = searchQuery.isBlank() || store.name.contains(searchQuery, ignoreCase = true)
            val matchLoc = locationFilter.isBlank() || store.location == locationFilter
            matchSearch && matchLoc
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatrouhBackground)
            .padding(bottom = 70.dp)
    ) {
        // Hero Banners
        item {
            HeroBannerCarousel(banners = banners)
        }

        // Search & Location Filter Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Search text input
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = MatrouhPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text("ابحث عن منتج، متجر، مطعم...", fontSize = 12.sp, color = TextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            )
                        )
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Location Selector Row
                    if (uniqueLocations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = locationFilter.isBlank(),
                                onClick = { onLocationChange("") },
                                label = { Text("كل المناطق", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            uniqueLocations.forEach { loc ->
                                FilterChip(
                                    selected = locationFilter == loc,
                                    onClick = { onLocationChange(if (locationFilter == loc) "" else loc) },
                                    label = { Text(loc, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isSearching) {
            // Search Results Mode
            item {
                Text(
                    text = "نتائج البحث (${filteredProducts.size + filteredStores.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            if (filteredStores.isNotEmpty()) {
                item {
                    Text(
                        text = "المتاجر (${filteredStores.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                items(filteredStores.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { st ->
                            StoreCard(
                                store = st,
                                isLiked = wishlist.contains(st.id),
                                onStoreClick = { onStoreClick(st) },
                                onInfoClick = { onStoreInfo(st) },
                                onLikeToggle = { onLikeToggle(st.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (filteredProducts.isNotEmpty()) {
                item {
                    Text(
                        text = "المنتجات (${filteredProducts.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(filteredProducts.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { p ->
                            ProductCardItem(
                                product = p,
                                isLiked = wishlist.contains(p.id),
                                onDetails = { onProductClick(p) },
                                onAdd = { onQuickAdd(p) },
                                onLikeToggle = { onLikeToggle(p.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (filteredProducts.isEmpty() && filteredStores.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("لا توجد نتائج مطابقة للبحث", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                    }
                }
            }
        } else {
            // Normal Home View

            // Flash Sale Section
            item {
                FlashSaleSection(
                    flashProducts = flashProducts,
                    remainingMs = flashRemainingMs,
                    onSeeAll = { onNavigate(Screen.FLASH_SALE) },
                    onProductClick = onProductClick,
                    onQuickAdd = onQuickAdd
                )
            }

            // Featured Products Section Header
            val featuredProds = products.filter { it.isFeatured }
            if (featuredProds.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MatrouhAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("منتجات مختارة", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                        }
                        TextButton(onClick = { onNavigate(Screen.ALL_FEATURED_PRODUCTS) }) {
                            Text("المزيد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MatrouhPrimary)
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // 2-column grid of featured products
                items(featuredProds.take(6).chunked(2)) { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { p ->
                            ProductCardItem(
                                product = p,
                                isLiked = wishlist.contains(p.id),
                                onDetails = { onProductClick(p) },
                                onAdd = { onQuickAdd(p) },
                                onLikeToggle = { onLikeToggle(p.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Featured Stores Section Header
            val featuredStores = stores.filter { it.isFeatured }
            if (featuredStores.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MatrouhPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("متاجر مميزة", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                        }
                        TextButton(onClick = { onNavigate(Screen.ALL_FEATURED_STORES) }) {
                            Text("المزيد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MatrouhPrimary)
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // 3-column compact store tiles
                items(featuredStores.take(9).chunked(3)) { trio ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        trio.forEach { st ->
                            FeaturedStoreTile(
                                store = st,
                                isLiked = wishlist.contains(st.id),
                                onEnter = { onStoreClick(st) },
                                onLikeToggle = { onLikeToggle(st.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - trio.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Categories Section
            if (categories.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = null, tint = MatrouhPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الأقسام", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                    }
                }

                items(categories.chunked(3)) { trio ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        trio.forEach { cat ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onCategoryClick(cat) },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(MatrouhPrimary.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(cat.icon),
                                            contentDescription = null,
                                            tint = MatrouhPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = cat.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        repeat(3 - trio.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CategoryScreen(
    category: Category,
    stores: List<Store>,
    wishlist: Set<String>,
    onBack: () -> Unit,
    onStoreClick: (Store) -> Unit,
    onStoreInfo: (Store) -> Unit,
    onLikeToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var locationFilter by remember { mutableStateOf("") }
    val uniqueLocations = remember(stores) {
        stores.map { it.location }.filter { it.isNotBlank() }.distinct()
    }

    val categoryStores = remember(stores, category, locationFilter) {
        stores.filter {
            it.categoryId == category.id && (locationFilter.isBlank() || it.location == locationFilter)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatrouhBackground)
            .padding(bottom = 70.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(category.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                    }

                    if (uniqueLocations.isNotEmpty()) {
                        TextButton(onClick = { /* cycle or reset */ locationFilter = "" }) {
                            Text(if (locationFilter.isBlank()) "كل المناطق" else locationFilter, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (categoryStores.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = TextMuted, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("عفواً، لا توجد متاجر في هذا القسم حالياً", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(categoryStores.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { st ->
                        StoreCard(
                            store = st,
                            isLiked = wishlist.contains(st.id),
                            onStoreClick = { onStoreClick(st) },
                            onInfoClick = { onStoreInfo(st) },
                            onLikeToggle = { onLikeToggle(st.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun StoreScreen(
    store: Store,
    products: List<Product>,
    banners: List<StoreBanner>,
    sections: List<StoreSection>,
    activeSection: String,
    searchQuery: String,
    sortOption: String,
    columnCount: Int,
    wishlist: Set<String>,
    onBack: () -> Unit,
    onSectionChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onColsChange: (Int) -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit,
    onLikeToggle: (String) -> Unit,
    onStoreInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val storeProducts = remember(products, store, activeSection, searchQuery, sortOption) {
        var list = products.filter { it.storeId == store.id }
        when (activeSection) {
            "__offer" -> list = list.filter { it.isOffer }
            "__exclusive" -> list = list.filter { it.isExclusive }
            "__new" -> list = list.filter { it.isNewArrival }
            "ALL" -> {}
            else -> list = list.filter { it.category == activeSection }
        }
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        when (sortOption) {
            "asc" -> list.sortedBy { it.price }
            "desc" -> list.sortedByDescending { it.price }
            "disc" -> list.sortedByDescending { if (it.oldPrice != null) (it.oldPrice - it.price) else 0.0 }
            else -> list
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(bottom = 70.dp)
    ) {
        // Top Store Header Bar
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        AsyncImage(
                            model = store.logo.ifBlank { store.image },
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = store.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = store.brandColor?.let { try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { TextPrimary } } ?: TextPrimary
                        )
                    }

                    Row {
                        IconButton(onClick = { onLikeToggle(store.id) }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (wishlist.contains(store.id)) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (wishlist.contains(store.id)) MatrouhRed else TextSecondary
                            )
                        }
                        IconButton(onClick = onStoreInfo, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary)
                        }
                    }
                }
            }
        }

        // Store Banners
        if (banners.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    AsyncImage(
                        model = banners.first().image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // In-store Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("ابحث داخل ${store.name}...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Section Tabs Row
        item {
            val baseSections = listOf(
                Pair("ALL", "الكل"),
                Pair("__offer", "العروض"),
                Pair("__exclusive", "حصرياً"),
                Pair("__new", "وصل حديثاً")
            )
            val customSections = sections.map { Pair(it.name, it.name) }
            val allSections = baseSections + customSections

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allSections.forEach { (key, label) ->
                    val isSel = activeSection == key
                    FilterChip(
                        selected = isSel,
                        onClick = { onSectionChange(key) },
                        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MatrouhPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Sort & Column Toggle Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الترتيب:", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = {
                        val next = when (sortOption) {
                            "default" -> "asc"
                            "asc" -> "desc"
                            "desc" -> "disc"
                            else -> "default"
                        }
                        onSortChange(next)
                    }) {
                        Text(
                            when (sortOption) {
                                "asc" -> "الأقل سعراً"
                                "desc" -> "الأعلى سعراً"
                                "disc" -> "الأعلى خصماً"
                                else -> "الافتراضي"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Row {
                    listOf(1, 2, 3).forEach { c ->
                        IconButton(
                            onClick = { onColsChange(c) },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (columnCount == c) Color(0xFFF1F5F9) else Color.Transparent)
                        ) {
                            Icon(
                                imageVector = if (c == 1) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                contentDescription = null,
                                tint = if (columnCount == c) MatrouhPrimary else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Products count
        item {
            Text(
                text = "المنتجات (${storeProducts.size})",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Products Grid
        if (storeProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد منتجات مطابقة", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            items(storeProducts.chunked(columnCount)) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { p ->
                        ProductCardItem(
                            product = p,
                            isLiked = wishlist.contains(p.id),
                            onDetails = { onProductClick(p) },
                            onAdd = { onQuickAdd(p) },
                            onLikeToggle = { onLikeToggle(p.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columnCount - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersScreen(
    orders: List<OrderHistoryItem>,
    customerProfile: CustomerProfile,
    onEditProfile: () -> Unit,
    onReorder: (OrderHistoryItem) -> Unit,
    onDeleteOrder: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showArchive by remember { mutableStateOf(false) }
    var expandedOrderId by remember { mutableStateOf<Long?>(null) }

    val recentOrders = orders.take(5)
    val archiveOrders = orders.drop(5)
    val displayOrders = if (showArchive) orders else recentOrders

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatrouhBackground)
            .padding(bottom = 70.dp)
    ) {
        // Customer Profile Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MatrouhPrimaryDark, MatrouhPrimary)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = customerProfile.name.ifBlank { "عميل عزيز" },
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = customerProfile.phone.ifBlank { "لم يتم تسجيل الهاتف" },
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                            if (customerProfile.address.isNotBlank()) {
                                Text(
                                    text = customerProfile.address,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تعديل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("سجل طلباتي", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("${orders.size} طلب", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("لا توجد طلبات سابقة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("ستظهر هنا تفاصيل طلباتك بعد الشراء", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        } else {
            items(displayOrders) { order ->
                val isExpanded = expandedOrderId == order.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { expandedOrderId = if (isExpanded) null else order.id },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(order.storeName, fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val (badgeText, badgeBg, badgeFg) = when (order.status) {
                                        "processing" -> Triple("قيد التجهيز", Color(0xFFFEF3C7), Color(0xFFB45309))
                                        "delivered" -> Triple("تم التسليم", Color(0xFFDCFCE7), Color(0xFF15803D))
                                        "cancelled" -> Triple("ملغي", Color(0xFFFEE2E2), Color(0xFFB91C1C))
                                        else -> Triple("جديد", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeFg)
                                    }
                                }
                                Text(order.date.take(10), fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("${order.total.toInt()} ج.م", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MatrouhPrimary)
                                Text("${order.items.size} منتجات", fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        // Expanded Order Items
                        if (isExpanded) {
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = BorderLight)
                            order.items.forEach { itm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${itm.product.name} × ${itm.quantity}", fontSize = 12.sp, color = TextSecondary)
                                    Text("${(itm.finalPrice * itm.quantity).toInt()} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!order.coupon.isNullOrBlank()) {
                                Text("كود الخصم: ${order.coupon}", fontSize = 11.sp, color = MatrouhGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onReorder(order) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MatrouhPrimary)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة للسلة مجدداً", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { onDeleteOrder(order.id) },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFEE2E2))
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = MatrouhRed)
                                }
                            }
                        }
                    }
                }
            }

            if (archiveOrders.isNotEmpty()) {
                item {
                    TextButton(
                        onClick = { showArchive = !showArchive },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (showArchive) "إخفاء الأرشيف" else "عرض الأرشيف (${archiveOrders.size} طلب أقدم)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MatrouhPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistScreen(
    stores: List<Store>,
    products: List<Product>,
    wishlist: Set<String>,
    onProductClick: (Product) -> Unit,
    onStoreClick: (Store) -> Unit,
    onQuickAdd: (Product) -> Unit,
    onLikeToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favStores = remember(stores, wishlist) { stores.filter { wishlist.contains(it.id) } }
    val favProducts = remember(products, wishlist) { products.filter { wishlist.contains(it.id) } }

    var selectedTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatrouhBackground)
            .padding(bottom = 70.dp)
    ) {
        item {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp, color = Color.White) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("المنتجات (${favProducts.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("المتاجر (${favStores.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            if (favProducts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("لا توجد منتجات في المفضلة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(favProducts.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { p ->
                            ProductCardItem(
                                product = p,
                                isLiked = true,
                                onDetails = { onProductClick(p) },
                                onAdd = { onQuickAdd(p) },
                                onLikeToggle = { onLikeToggle(p.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            if (favStores.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = TextMuted, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("لا توجد متاجر في المفضلة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(favStores.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { st ->
                            StoreCard(
                                store = st,
                                isLiked = true,
                                onStoreClick = { onStoreClick(st) },
                                onInfoClick = {},
                                onLikeToggle = { onLikeToggle(st.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun FlashSaleScreen(
    flashProducts: List<Product>,
    remainingMs: Long,
    wishlist: Set<String>,
    onBack: () -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit,
    onLikeToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSec = (remainingMs / 1000).coerceAtLeast(0)
    val days = totalSec / 86400
    val hours = (totalSec % 86400) / 3600
    val mins = (totalSec % 3600) / 60
    val secs = totalSec % 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MatrouhBackground)
            .padding(bottom = 70.dp)
    ) {
        item {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 2.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فلاش سيل", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MatrouhPrimaryDark)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (days > 0) "${days}d ${String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)}"
                            else String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        items(flashProducts.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { p ->
                    ProductCardItem(
                        product = p,
                        isLiked = wishlist.contains(p.id),
                        onDetails = { onProductClick(p) },
                        onAdd = { onQuickAdd(p) },
                        onLikeToggle = { onLikeToggle(p.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
