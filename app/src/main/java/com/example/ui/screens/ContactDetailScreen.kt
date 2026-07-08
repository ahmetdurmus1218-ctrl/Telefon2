package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ContactEntity
import com.example.ui.viewmodel.ScreenPulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: Long,
    viewModel: ScreenPulseViewModel,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.allContacts.collectAsState()
    val contact = contacts.find { it.id == contactId }
    var showCallSimulatorDialog by remember { mutableStateOf(false) }

    val callLogs by viewModel.allCallLogs.collectAsState()
    val contactLogs = remember(callLogs, contact) {
        if (contact == null) {
            emptyList()
        } else {
            callLogs.filter { 
                it.contactId == contact.id || 
                it.number.replace(" ", "") == contact.phone.replace(" ", "") 
            }
        }
    }

    if (contact == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Kişi bulunamadı")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(contact) }) {
                        Icon(
                            imageVector = if (contact.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favori",
                            tint = if (contact.isFavorite) Color(0xFFFF9800) else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { onEditClick(contact.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Hero Area with MagicOS sky fluid gradient background
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF81D4FA), // Light Sky Blue
                                    Color(0xFFE1F5FE),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        AvatarView(name = contact.name, badge = "", size = 96.dp)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = contact.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (contact.isFavorite) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Starred",
                                    tint = Color(0xFFFFEB3B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = contact.phone,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Türkiye",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Quick Actions Circle Buttons Row (Ara, Mesaj, Görüntülü, E-posta)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionCircleButton(
                        label = "Ara",
                        icon = Icons.Default.Phone,
                        onClick = { showCallSimulatorDialog = true }
                    )
                    QuickActionCircleButton(
                        label = "Mesaj",
                        icon = Icons.Default.Message,
                        onClick = { /* Message */ }
                    )
                    QuickActionCircleButton(
                        label = "Görüntülü",
                        icon = Icons.Default.VideoCall,
                        onClick = { /* Video Call */ }
                    )
                    QuickActionCircleButton(
                        label = "E-posta",
                        icon = Icons.Default.Email,
                        onClick = { /* Email */ }
                    )
                }
            }

            // Structured Details Card List
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailItemRow(
                            label = contact.phone,
                            subLabel = "Mobil",
                            icon = Icons.Default.Phone
                        )
                        
                        if (contact.email.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            DetailItemRow(
                                label = contact.email,
                                subLabel = "E-posta (İş)",
                                icon = Icons.Default.Email
                            )
                        }

                        if (contact.birthday.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            DetailItemRow(
                                label = contact.birthday,
                                subLabel = "Doğum günü",
                                icon = Icons.Default.CalendarToday
                            )
                        }

                        if (contact.group.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            DetailItemRow(
                                label = contact.group,
                                subLabel = "Grup",
                                icon = Icons.Default.People
                            )
                        }

                        if (contact.other.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            DetailItemRow(
                                label = contact.other,
                                subLabel = "Diğer",
                                icon = Icons.Default.Notes
                            )
                        }
                    }
                }
            }

            if (contactLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "Son Aramalar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }

                items(contactLogs.take(5)) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (log.callType) {
                                "Cevapsız" -> Icons.Default.CallMissed
                                "Gelen" -> Icons.AutoMirrored.Filled.CallReceived
                                else -> Icons.AutoMirrored.Filled.CallMade
                            }
                            val iconColor = when (log.callType) {
                                "Cevapsız" -> Color.Red
                                "Gelen" -> Color(0xFF4CAF50)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = log.callType,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.callType + " Arama",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = if (log.callType == "Cevapsız") Color.Red else MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                                Text(
                                    text = dateStr,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (log.callType != "Cevapsız" && log.durationSeconds > 0) {
                                val durationText = if (log.durationSeconds / 60 > 0) {
                                    "${log.durationSeconds / 60} dk ${log.durationSeconds % 60} sn"
                                } else {
                                    "${log.durationSeconds % 60} sn"
                                }
                                Text(
                                    text = durationText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCallSimulatorDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showCallSimulatorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Arama Seçenekleri", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${contact.name} (${contact.phone}) numaralı kişi aranıyor. Lütfen yapmak istediğiniz arama türünü seçin:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Outgoing simulated
                    Card(
                        onClick = {
                            showCallSimulatorDialog = false
                            viewModel.startSimulatedCall(context, contact.phone, contact.name, "Giden", 0)
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Giden Arama Simülasyonu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Uygulama içi giden arama ekranını açar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Incoming simulated with 5 seconds delay
                    Card(
                        onClick = {
                            showCallSimulatorDialog = false
                            android.widget.Toast.makeText(context, "Gelen arama 5 saniye içinde simüle edilecek...", android.widget.Toast.LENGTH_SHORT).show()
                            viewModel.startSimulatedCall(context, contact.phone, contact.name, "Gelen", 5)
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RingVolume, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Gelen Arama Simülasyonu (5sn)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("5 saniye bekler ve gelen arama ekranını açar.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Real call
                    Card(
                        onClick = {
                            showCallSimulatorDialog = false
                            viewModel.makeCall(contact.phone, contact.name, contact.id)
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Gerçek Arama Başlat", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Cihazın kendi telefon şebekesini kullanır.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCallSimulatorDialog = false }) {
                    Text("İptal", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun QuickActionCircleButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun DetailItemRow(
    label: String,
    subLabel: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subLabel,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}
