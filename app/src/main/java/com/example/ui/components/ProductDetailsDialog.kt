package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun ProductDetailsDialog(
    product: Product,
    store: Store?,
    isLiked: Boolean,
    onClose: () -> Unit,
    onLikeToggle: () -> Unit,
    onAddToCart: (quantity: Int, size: String?, color: String?, flavor: String?, weight: ProductWeight?, extras: List<ProductExtra>, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentImgIndex by remember { mutableIntStateOf(0) }
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableStateOf(product.sizes.firstOrNull()) }
    var selectedColor by remember { mutableStateOf(product.colors.firstOrNull()) }
    var selectedFlavor by remember { mutableStateOf(product.flavors.firstOrNull()) }
    var selectedWeight by remember { mutableStateOf(product.weights.firstOrNull()) }
    val selectedExtras = remember { mutableStateListOf<ProductExtra>() }
    var specialNotes by remember { mutableStateOf("") }

    val images = if (product.images.isNotEmpty()) product.images else listOf("")
    val inStock = product.stock == null || product.stock > 0
    val maxStock = product.stock ?: 99

    val basePrice = selectedWeight?.price ?: product.price
    val extrasSum = selectedExtras.sumOf { it.price }
    val itemFinalPrice = basePrice + extrasSum
    val totalPrice = itemFinalPrice * quantity

    val discount = if (product.oldPrice != null && product.oldPrice > basePrice) {
        (((product.oldPrice - basePrice) / product.oldPrice) * 100).toInt()
    } else 0

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 85.dp)
            ) {
                // Image Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .background(Color(0xFFF1F5F9))
                ) {
                    AsyncImage(
                        model = images.getOrNull(currentImgIndex),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Top Bar Floating
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                                .testTag("details_back_button")
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = TextPrimary)
                        }

                        IconButton(
                            onClick = onLikeToggle,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                                .testTag("details_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "إعجاب",
                                tint = if (isLiked) MatrouhRed else TextSecondary
                            )
                        }
                    }

                    // Discount tag
                    if (discount > 0 && inStock) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrouhRed)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("وفر $discount%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Thumbnails
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        images.forEachIndexed { idx, img ->
                            val isSel = idx == currentImgIndex
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(2.dp, if (isSel) MatrouhPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { currentImgIndex = idx }
                            ) {
                                AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }

                // Info Section
                Column(modifier = Modifier.padding(16.dp)) {
                    // Badges (Store & Category)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (store != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                AsyncImage(
                                    model = store.logo.ifBlank { store.image },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(store.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        if (product.category.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MatrouhPrimary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(product.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MatrouhPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = product.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    // Price card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MatrouhPrimary.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${itemFinalPrice.toInt()} ج.م",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = MatrouhPrimary
                                )
                                if (product.oldPrice != null && product.oldPrice > itemFinalPrice) {
                                    Text(
                                        text = "${product.oldPrice.toInt()} ج.م",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (inStock) MatrouhGreen.copy(alpha = 0.15f) else MatrouhRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (!inStock) "نفد المخزون" else if (product.stock != null) "متوفر (${product.stock})" else "متوفر",
                                    color = if (inStock) MatrouhGreen else MatrouhRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Description
                    if (product.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("الوصف", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description + if (product.specs.isNotBlank()) "\n" + product.specs else "",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }

                    // Weights / Sizes / Colors Options
                    if (product.weights.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("الحجم / الوزن", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            product.weights.forEach { w ->
                                val isSel = selectedWeight?.name == w.name
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedWeight = w },
                                    label = { Text("${w.name} • ${w.price.toInt()} ج.م", fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    if (product.sizes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("المقاس", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            product.sizes.forEach { sz ->
                                val isSel = selectedSize == sz
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedSize = sz },
                                    label = { Text(sz, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    if (product.colors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("اللون", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            product.colors.forEach { c ->
                                val isSel = selectedColor == c
                                if (c.startsWith("#") && c.length in listOf(4, 7)) {
                                    val col = try { Color(android.graphics.Color.parseColor(c)) } catch (_: Exception) { Color.Gray }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (isSel) 3.dp else 1.dp,
                                                color = if (isSel) MatrouhPrimary else Color.LightGray,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColor = c },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSel) Icon(Icons.Default.Check, contentDescription = null, tint = if (col == Color.White) Color.Black else Color.White, modifier = Modifier.size(18.dp))
                                    }
                                } else {
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedColor = c },
                                        label = { Text(c, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }
                    }

                    if (product.flavors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("النكهة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            product.flavors.forEach { f ->
                                val isSel = selectedFlavor == f
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedFlavor = f },
                                    label = { Text(f, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    if (product.extras.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("إضافات إضافية", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        product.extras.forEach { extra ->
                            val isChecked = selectedExtras.any { it.name == extra.name }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isChecked) selectedExtras.removeAll { it.name == extra.name }
                                        else selectedExtras.add(extra)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedExtras.add(extra)
                                        else selectedExtras.removeAll { it.name == extra.name }
                                    }
                                )
                                Text(extra.name, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("+${extra.price.toInt()} ج.م", color = MatrouhPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Special notes
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("ملاحظات خاصة للبائع (اختياري)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = specialNotes,
                        onValueChange = { specialNotes = it },
                        placeholder = { Text("اكتب أي ملاحظة مثل: بدون بصل، تغليف هدية...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Sticky Bottom Bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Qty Stepper
                    if (inStock) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "تقليل", modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = quantity.toString(),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { if (quantity < maxStock) quantity++ },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Add to cart Button
                    Button(
                        onClick = {
                            if (inStock) {
                                onAddToCart(
                                    quantity,
                                    selectedSize,
                                    selectedColor,
                                    selectedFlavor,
                                    selectedWeight,
                                    selectedExtras.toList(),
                                    specialNotes
                                )
                                onClose()
                            }
                        },
                        enabled = inStock,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_to_cart_confirm_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MatrouhPrimary)
                    ) {
                        Icon(Icons.Default.ShoppingBasket, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (inStock) "أضف للسلة (${totalPrice.toInt()} ج.م)" else "نفد المخزون",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
