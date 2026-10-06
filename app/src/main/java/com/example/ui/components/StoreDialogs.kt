package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.Store
import com.example.ui.theme.*

@Composable
fun StoreInfoDialog(
    store: Store,
    onDismiss: () -> Unit,
    onStartChat: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(26.dp))
                    .clickable(enabled = false) {},
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(26.dp)
            ) {
                Column {
                    // Header Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .background(Color(0xFFE2E8F0))
                    ) {
                        AsyncImage(
                            model = store.mapImage.ifBlank { store.coverImage },
                            contentDescription = store.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Text(store.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text(store.description, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    // Store Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF7ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MatrouhAccent, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("العنوان", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(store.location, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("مواعيد العمل", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(store.hours, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        // Delivery & Minimum Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (store.deliveryTime.isNotBlank()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("توصيل: ${store.deliveryTime}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                            if (store.minOrder > 0) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("أقل طلب: ${store.minOrder.toInt()} ج", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        // Customer Chat Button
                        Button(
                            onClick = onStartChat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_chat_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MatrouhPrimary)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("محادثة مباشرة مع المتجر", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // WhatsApp Button
                        Button(
                            onClick = {
                                val cleanNum = store.whatsapp.filter { it.isDigit() }
                                val url = "https://wa.me/$cleanNum"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("whatsapp_contact_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MatrouhWhatsApp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تواصل عبر واتساب", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerChatDialog(
    store: Store,
    messages: List<ChatMessage>,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp),
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MatrouhPrimaryDark, MatrouhPrimary)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = store.logo.ifBlank { store.image },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(store.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text("محادثة مباشرة مع المتجر", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Messages list
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("اكتب رسالتك الأولى للمتجر", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            val isCustomer = msg.sender == "customer"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 280.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isCustomer) 16.dp else 4.dp,
                                                bottomEnd = if (isCustomer) 4.dp else 16.dp
                                            )
                                        )
                                        .background(if (isCustomer) MatrouhPrimary else Color.White)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Column {
                                        if (!isCustomer) {
                                            Text(
                                                text = if (msg.sender == "admin") "إدارة المول" else store.name,
                                                color = MatrouhPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Text(
                                            text = msg.text,
                                            color = if (isCustomer) Color.White else TextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("اكتب رسالتك هنا...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    onSendMessage(messageText)
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (messageText.isNotBlank()) MatrouhPrimary else Color.LightGray)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
