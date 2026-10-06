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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun CartDialog(
    isOpen: Boolean,
    cart: List<CartItem>,
    store: Store?,
    appliedCoupon: PromoCode?,
    customerProfile: CustomerProfile,
    onClose: () -> Unit,
    onUpdateQty: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onCheckout: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var couponInput by remember { mutableStateOf("") }
    var isEditingProfile by remember { mutableStateOf(customerProfile.name.isBlank() || customerProfile.phone.isBlank() || customerProfile.address.isBlank()) }
    var nameInput by remember(customerProfile) { mutableStateOf(customerProfile.name) }
    var phoneInput by remember(customerProfile) { mutableStateOf(customerProfile.phone) }
    var addressInput by remember(customerProfile) { mutableStateOf(customerProfile.address) }
    var formError by remember { mutableStateOf("") }

    val subTotal = cart.sumOf { it.finalPrice * it.quantity }
    val discountAmount = if (appliedCoupon != null) subTotal * appliedCoupon.percent else 0.0
    val total = subTotal - discountAmount

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("سلة المشتريات", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                    Text("${cart.size} منتجات مضافة", fontSize = 11.sp, color = TextMuted)
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
            Divider(color = BorderLight)

            if (cart.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("سلتك فارغة!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Text("تصفح المنتجات وأضف ما يعجبك", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onClose,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MatrouhPrimary)
                        ) {
                            Text("ابدأ التسوق الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Items & Checkout info
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    if (store != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = store.logo.ifBlank { store.image },
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("طلب من", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                        Text(store.name, fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // Cart items
                    items(cart) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.product.images.firstOrNull(),
                                    contentDescription = item.product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF1F5F9))
                                )

                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = item.product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { onRemoveItem(item) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = MatrouhRed, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    val optionsText = listOfNotNull(
                                        item.selectedWeight?.name,
                                        item.selectedSize,
                                        item.selectedColor,
                                        item.selectedFlavor
                                    ).joinToString(" • ")

                                    if (optionsText.isNotBlank()) {
                                        Text(
                                            text = optionsText,
                                            fontSize = 10.sp,
                                            color = TextMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${(item.finalPrice * item.quantity).toInt()} ج.م",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = MatrouhPrimary
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF1F5F9))
                                        ) {
                                            IconButton(
                                                onClick = { onUpdateQty(item, -1) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                            }
                                            Text(
                                                text = item.quantity.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp)
                                            )
                                            IconButton(
                                                onClick = { onUpdateQty(item, 1) },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Coupon Code
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = { couponInput = it },
                                placeholder = { Text("كود الخصم (مثال: MATROUH20)", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onApplyCoupon(couponInput) },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MatrouhPrimary)
                            ) {
                                Text("تطبيق", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Totals Breakdown
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المجموع الفرعي", fontSize = 12.sp, color = TextSecondary)
                                    Text("${subTotal.toInt()} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                if (appliedCoupon != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("الخصم (${appliedCoupon.code})", fontSize = 12.sp, color = MatrouhGreen, fontWeight = FontWeight.Bold)
                                        Text("-${discountAmount.toInt()} ج.م", fontSize = 12.sp, color = MatrouhGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("الإجمالي النهائي", fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    Text("${total.toInt()} ج.م", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MatrouhPrimary)
                                }
                            }
                        }
                    }

                    // Delivery Details Form / Card
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MatrouhPrimary.copy(alpha = 0.04f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MatrouhPrimary.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MatrouhPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("بيانات التوصيل", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }

                                    if (!isEditingProfile && customerProfile.name.isNotBlank()) {
                                        TextButton(onClick = { isEditingProfile = true }) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تعديل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (isEditingProfile) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { nameInput = it },
                                        placeholder = { Text("الاسم بالكامل", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = phoneInput,
                                        onValueChange = { phoneInput = it },
                                        placeholder = { Text("رقم الهاتف", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = addressInput,
                                        onValueChange = { addressInput = it },
                                        placeholder = { Text("عنوان التوصيل بالتفصيل (الشارع، الحي...)", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        maxLines = 2
                                    )

                                    if (formError.isNotBlank()) {
                                        Text(formError, color = MatrouhRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (nameInput.isBlank() || phoneInput.isBlank() || addressInput.isBlank()) {
                                                formError = "من فضلك اكتب الاسم ورقم الهاتف والعنوان"
                                            } else {
                                                formError = ""
                                                onSaveProfile(nameInput, phoneInput, addressInput)
                                                isEditingProfile = false
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MatrouhPrimary)
                                    ) {
                                        Text("حفظ البيانات", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("${customerProfile.name} • ${customerProfile.phone}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(customerProfile.address, fontSize = 11.sp, color = TextSecondary, maxLines = 2)
                                }
                            }
                        }
                    }

                    // Checkout Button WhatsApp
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (isEditingProfile) {
                                    if (nameInput.isBlank() || phoneInput.isBlank() || addressInput.isBlank()) {
                                        formError = "من فضلك اكتب الاسم ورقم الهاتف والعنوان"
                                        return@Button
                                    }
                                    formError = ""
                                    onSaveProfile(nameInput, phoneInput, addressInput)
                                    isEditingProfile = false
                                    onCheckout(nameInput, phoneInput, addressInput)
                                } else {
                                    onCheckout(customerProfile.name, customerProfile.phone, customerProfile.address)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_order_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MatrouhWhatsApp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إتمام الطلب عبر واتساب (${total.toInt()} ج.م)",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
