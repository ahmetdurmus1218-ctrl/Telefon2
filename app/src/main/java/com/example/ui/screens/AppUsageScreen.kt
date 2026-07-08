package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.CallLogEntity
import com.example.ui.viewmodel.ScreenPulseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(
    viewModel: ScreenPulseViewModel,
    onContactClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val callLogs by viewModel.allCallLogs.collectAsState()
    val contacts by viewModel.allContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val dialpadInput by viewModel.dialpadInput.collectAsState()
    val selectedFilter by viewModel.selectedCallFilter.collectAsState()

    var showDialpad by remember { mutableStateOf(false) }
    var showCallSimulatorDialog by remember { mutableStateOf(false) }
    var simNumber by remember { mutableStateOf("") }
    var simName by remember { mutableStateOf("") }
    var simContactId by remember { mutableStateOf<Long?>(null) }

    // Filter calls based on the active tab and search query
    val filteredLogs = callLogs.filter { log ->
        val matchesSearch = if (searchQuery.isNotEmpty()) {
            log.name.contains(searchQuery, ignoreCase = true) || log.number.contains(searchQuery)
        } else if (dialpadInput.isNotEmpty()) {
            log.number.contains(dialpadInput) || log.name.contains(dialpadInput, ignoreCase = true)
        } else {
            true
        }

        val matchesTab = when (selectedFilter) {
            "Cevapsız" -> log.callType == "Cevapsız"
            "Gelen" -> log.callType == "Gelen"
            "Giden" -> log.callType == "Giden"
            else -> true // "Tümü"
        }

        matchesSearch && matchesTab
    }

    // Group logs into Today vs Older
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val groupedLogs = filteredLogs.groupBy { log ->
        if (log.timestamp >= todayStart) "Bugün" else "Daha eski"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aramalar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAllCallLogs() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Arama Kaydını Temizle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Seçenekler",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!showDialpad) {
                FloatingActionButton(
                    onClick = { showDialpad = true },
                    containerColor = Color(0xFF4CAF50), // Dialer green
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dialpad,
                        contentDescription = "Dialpad Aç",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                SearchBarComponent(
                    query = if (showDialpad) dialpadInput else searchQuery,
                    onQueryChange = {
                        if (showDialpad) {
                            // If dialpad is showing, let keyboard type into dialpad
                            /* Handled by dialpad keys */
                        } else {
                            viewModel.setSearchQuery(it)
                        }
                    }
                )

                // Filter Tabs (Tümü, Cevapsız, Gelen, Giden)
                FilterTabsComponent(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { viewModel.setSelectedCallFilter(it) }
                )

                // Calls List
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhoneCallback,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Arama kaydı yok",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Show Today Group if present
                        val todayLogs = groupedLogs["Bugün"]
                        if (!todayLogs.isNullOrEmpty()) {
                            item {
                                SectionHeader(text = "Bugün")
                            }
                            items(todayLogs) { log ->
                                CallLogItem(
                                    log = log,
                                    onCallClick = {
                                        simNumber = log.number
                                        simName = log.name
                                        simContactId = log.contactId
                                        showCallSimulatorDialog = true
                                    },
                                    onInfoClick = {
                                        log.contactId?.let { onContactClick(it) } ?: run {
                                            // Handle unknown contact details (we can create contact or show details)
                                        }
                                    }
                                )
                            }
                        }

                        // Show Older Group if present
                        val olderLogs = groupedLogs["Daha eski"]
                        if (!olderLogs.isNullOrEmpty()) {
                            item {
                                SectionHeader(text = "Daha eski")
                            }
                            items(olderLogs) { log ->
                                CallLogItem(
                                    log = log,
                                    onCallClick = {
                                        simNumber = log.number
                                        simName = log.name
                                        simContactId = log.contactId
                                        showCallSimulatorDialog = true
                                    },
                                    onInfoClick = {
                                        log.contactId?.let { onContactClick(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Real-time Dialpad Overlay at the bottom
            AnimatedVisibility(
                visible = showDialpad,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                DialpadComponent(
                    dialpadInput = dialpadInput,
                    onDigitClick = { viewModel.appendToDialpad(it) },
                    onBackspaceClick = { viewModel.backspaceDialpad() },
                    onMinimizeClick = { showDialpad = false },
                    onCallClick = {
                        if (dialpadInput.isNotEmpty()) {
                            // Find contact if exists
                            val contact = contacts.find { it.phone.replace(" ", "") == dialpadInput.replace(" ", "") }
                            simNumber = dialpadInput
                            simName = contact?.name ?: dialpadInput
                            simContactId = contact?.id
                            showCallSimulatorDialog = true
                            showDialpad = false
                        }
                    }
                )
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
                        text = "$simName ($simNumber) numaralı kişi aranıyor. Lütfen yapmak istediğiniz arama türünü seçin:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Outgoing simulated
                    Card(
                        onClick = {
                            showCallSimulatorDialog = false
                            viewModel.startSimulatedCall(context, simNumber, simName, "Giden", 0)
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
                            viewModel.startSimulatedCall(context, simNumber, simName, "Gelen", 5)
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
                            viewModel.makeCall(simNumber, simName, simContactId)
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
fun FilterTabsComponent(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("Tümü", "Cevapsız", "Gelen", "Giden")
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                val bgColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                val shadowModifier = if (isSelected) Modifier.shadow(1.dp, RoundedCornerShape(18.dp)) else Modifier

                Surface(
                    onClick = { onFilterSelected(filter) },
                    shape = RoundedCornerShape(18.dp),
                    color = bgColor,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(shadowModifier)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun CallLogItem(
    log: CallLogEntity,
    onCallClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val dateStr = remember(log.timestamp) {
        val date = Date(log.timestamp)
        val today = Calendar.getInstance()
        val logCal = Calendar.getInstance().apply { time = date }
        if (today.get(Calendar.DATE) == logCal.get(Calendar.DATE)) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } else {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCallClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Contact Avatar or generic
        AvatarView(name = log.name, badge = "", size = 44.dp)
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Call status icon
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = log.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (log.callType == "Cevapsız") Color.Red else MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val durationText = if (log.callType != "Cevapsız" && log.durationSeconds > 0) {
                " • " + formatDuration(log.durationSeconds)
            } else ""
            Text(
                text = "${log.category} • $dateStr$durationText",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action info icon (i)
        IconButton(onClick = onInfoClick) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Bilgi",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DialpadComponent(
    dialpadInput: String,
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onMinimizeClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Number Output
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dialpadInput.ifEmpty { " " },
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (dialpadInput.isNotEmpty()) {
                    Surface(
                        onClick = onBackspaceClick,
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Geri Sil",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            if (dialpadInput.isNotEmpty()) {
                Text(
                    text = "Türkiye",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Dialpad 3x4 Grid
            val keys = listOf(
                Pair('1', ""), Pair('2', "ABC"), Pair('3', "DEF"),
                Pair('4', "GHI"), Pair('5', "JKL"), Pair('6', "MNO"),
                Pair('7', "PQRS"), Pair('8', "TUV"), Pair('9', "WXYZ"),
                Pair('*', ""), Pair('0', "+"), Pair('#', "")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(290.dp)
                    .padding(bottom = 16.dp)
            ) {
                for (row in 0..3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            val key = keys[index]
                            DialpadButton(
                                digit = key.first,
                                letters = key.second,
                                onClick = { onDigitClick(key.first) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Call Row (Green call button + Minimize button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left spacing or extra action
                Box(modifier = Modifier.size(54.dp))

                // Green Circle Call Button
                Surface(
                    onClick = onCallClick,
                    shape = CircleShape,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Ara",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Right Minimize Button
                Surface(
                    onClick = onMinimizeClick,
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Klavye Kapat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DialpadButton(
    digit: Char,
    letters: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFFF0F3F8), // Pure light-grey circular keys
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = digit.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return ""
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes} dk ${remainingSeconds} sn"
    } else {
        "${remainingSeconds} sn"
    }
}
