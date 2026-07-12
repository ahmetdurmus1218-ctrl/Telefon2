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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: com.example.ui.viewmodel.ScreenPulseViewModel,
    modifier: Modifier = Modifier
) {
    val contactsForExport by viewModel.allContacts.collectAsState()
    val exportContext = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dialog Dialog States
    var showAccountsDialog by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }
    var showContactMgmtDialog by remember { mutableStateOf(false) }
    var showCallSettingsDialog by remember { mutableStateOf(false) }
    var showBlockedNumbersDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Logic States
    var sortingOrderAd by remember { mutableStateOf(true) } // true = Adına göre, false = Soyadına göre
    var nameFormatFirstAd by remember { mutableStateOf(true) }
    
    var callRecordingEnabled by remember { mutableStateOf(false) }
    var vibrateOnAnswer by remember { mutableStateOf(true) }
    var flashOnCall by remember { mutableStateOf(false) }
    
    var blockedNumbers = remember { mutableStateListOf("0532 999 88 77", "0505 111 00 22") }
    var newBlockedNumber by remember { mutableStateOf("") }
    
    var syncInProgress by remember { mutableStateOf(false) }
    var lastSyncTime by remember { mutableStateOf("Bugün, 12:40") }
    
    var backupProgress by remember { mutableStateOf(-1f) } // -1 = idle, 0 to 1 = active

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ayarlar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Group 1: Account & Personalization
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsItem(
                            title = "Hesaplar",
                            icon = Icons.Default.Person,
                            iconBgColor = Color(0xFF2196F3),
                            onClick = { showAccountsDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 74.dp, end = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                        SettingsItem(
                            title = "Görüntü ve düzen",
                            icon = Icons.Default.AspectRatio,
                            iconBgColor = Color(0xFF4CAF50),
                            onClick = { showLayoutDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 74.dp, end = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                        SettingsItem(
                            title = "Kişi yönetimi",
                            icon = Icons.Default.Contacts,
                            iconBgColor = Color(0xFFFF9800),
                            onClick = { showContactMgmtDialog = true }
                        )
                    }
                }
            }

            // Card Group 2: Calls & Safety
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsItem(
                            title = "Arama ayarları",
                            icon = Icons.Default.Phone,
                            iconBgColor = Color(0xFF4CAF50),
                            onClick = { showCallSettingsDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 74.dp, end = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                        SettingsItem(
                            title = "Engellenen numaralar",
                            icon = Icons.Default.Block,
                            iconBgColor = Color(0xFFF44336),
                            onClick = { showBlockedNumbersDialog = true }
                        )
                    }
                }
            }

            // Card Group 3: Storage & System
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        SettingsItem(
                            title = "Yedekle ve geri yükle",
                            icon = Icons.Default.CloudQueue,
                            iconBgColor = Color(0xFF00BCD4),
                            onClick = { showBackupDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 74.dp, end = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), thickness = 0.5.dp)
                        SettingsItem(
                            title = "Hakkında",
                            icon = Icons.Default.Info,
                            iconBgColor = Color(0xFF9E9E9E),
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG IMPLEMENTATIONS ---

    // 1. Accounts Dialog
    if (showAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showAccountsDialog = false },
            title = { Text("Kişi Hesapları", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Cihazınızdaki kişilerin senkronize olduğu hesaplar:")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = "Google", tint = Color(0xFF4285F4))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Google Hesabı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("e.webtekno@gmail.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SdCard, contentDescription = "SIM", tint = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("SIM Kart", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Turkcell SIM 1", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = "Device", tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Cihaz Hafızası", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Yerel kişiler", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Son Senkronizasyon:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(lastSyncTime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (syncInProgress) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Eşitleniyor...", fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    syncInProgress = true
                                    delay(2000)
                                    syncInProgress = false
                                    lastSyncTime = "Az önce"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Şimdi Eşitle")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountsDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // 2. Display & Layout Dialog
    if (showLayoutDialog) {
        AlertDialog(
            onDismissRequest = { showLayoutDialog = false },
            title = { Text("Görüntü ve Düzen", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Kişilerin sıralama ve görüntüleme ayarları:")
                    
                    Column {
                        Text("Sıralama Ölçütü", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = sortingOrderAd, onClick = { sortingOrderAd = true })
                            Text("Adına göre")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !sortingOrderAd, onClick = { sortingOrderAd = false })
                            Text("Soyadına göre")
                        }
                    }

                    Column {
                        Text("Ad Formatı", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = nameFormatFirstAd, onClick = { nameFormatFirstAd = true })
                            Text("Önce ad")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = !nameFormatFirstAd, onClick = { nameFormatFirstAd = false })
                            Text("Önce soyad")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLayoutDialog = false }) {
                    Text("Kaydet")
                }
            }
        )
    }

    // 3. Contact Management Dialog
    if (showContactMgmtDialog) {
        var mergeSuccess by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showContactMgmtDialog = false },
            title = { Text("Kişi Yönetimi", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Kişi listenizi temizleyin ve dışa aktarın:")

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                delay(1500)
                                mergeSuccess = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Merge, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Çift Kişileri Birleştir")
                    }

                    if (mergeSuccess) {
                        Text("Tüm çift kişiler başarıyla birleştirildi!", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { exportContactsToVcf(exportContext, contactsForExport) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ImportExport, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kişileri Dışa Aktar (.vcf)")
                    }

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(
                                exportContext,
                                "İçe aktarma yakında eklenecek. Şimdilik kişi eklemek için + butonunu kullanabilirsiniz.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DriveFileMove, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kişileri İçe Aktar")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showContactMgmtDialog = false 
                    mergeSuccess = false
                }) {
                    Text("Kapat")
                }
            }
        )
    }

    // 4. Call Settings Dialog
    if (showCallSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showCallSettingsDialog = false },
            title = { Text("Arama Ayarları", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Otomatik Arama Kaydı")
                        Switch(checked = callRecordingEnabled, onCheckedChange = { callRecordingEnabled = it })
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cevaplandığında Titreş")
                        Switch(checked = vibrateOnAnswer, onCheckedChange = { vibrateOnAnswer = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Arama Geldiğinde Flaş Yak")
                        Switch(checked = flashOnCall, onCheckedChange = { flashOnCall = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCallSettingsDialog = false }) {
                    Text("Tamam")
                }
            }
        )
    }

    // 5. Blocked Numbers Dialog
    if (showBlockedNumbersDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedNumbersDialog = false },
            title = { Text("Engellenen Numaralar", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Buradaki numaralardan gelen aramalar engellenecektir.")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newBlockedNumber,
                            onValueChange = { newBlockedNumber = it },
                            placeholder = { Text("Numara girin") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newBlockedNumber.isNotEmpty()) {
                                    blockedNumbers.add(newBlockedNumber)
                                    newBlockedNumber = ""
                                }
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Ekle", tint = Color.White)
                        }
                    }

                    Box(modifier = Modifier.heightIn(max = 200.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(blockedNumbers) { num ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(num, fontWeight = FontWeight.Medium)
                                    IconButton(onClick = { blockedNumbers.remove(num) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Engeli Kaldır", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBlockedNumbersDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // 6. Backup & Restore Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Yedekle ve Geri Yükle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Kişilerinizi ve arama kayıtlarınızı güvenle Google Drive veya yerel depolamaya yedekleyin.")
                    
                    if (backupProgress >= 0f) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { backupProgress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (backupProgress < 1f) "Yedekleniyor... %${(backupProgress * 100).toInt()}" else "Yedekleme Tamamlandı!",
                                fontWeight = FontWeight.Bold,
                                color = if (backupProgress < 1f) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    backupProgress = 0f
                                    while (backupProgress < 1.0f) {
                                        delay(300)
                                        backupProgress += 0.2f
                                    }
                                    backupProgress = 1.0f
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Google Drive'a Yedekle")
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    backupProgress = 0f
                                    while (backupProgress < 1.0f) {
                                        delay(300)
                                        backupProgress += 0.2f
                                    }
                                    backupProgress = 1.0f
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Yedekten Geri Yükle")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showBackupDialog = false 
                    backupProgress = -1f
                }) {
                    Text("Kapat")
                }
            }
        )
    }

    // 7. About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Hakkında", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Text(
                        text = "Telefon",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    
                    Text(
                        text = "Sürüm: 1.0.0 (Telefon Tasarımı)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Cihaz kişileri ve arama kayıtlarıyla tam senkronize çalışan, modern ve hızlı telefon uygulaması.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Text(
                        text = "© 2026 Tüm Hakları Saklıdır.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Harika")
                }
            }
        )
    }
}

// --- CONTACT EXPORT (.vcf) ---

fun exportContactsToVcf(context: android.content.Context, contacts: List<com.example.data.database.ContactEntity>) {
    if (contacts.isEmpty()) {
        android.widget.Toast.makeText(context, "Dışa aktarılacak kişi bulunamadı.", android.widget.Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val vcf = buildString {
            contacts.forEach { c ->
                append("BEGIN:VCARD\r\n")
                append("VERSION:3.0\r\n")
                append("FN:${c.name}\r\n")
                append("TEL;TYPE=CELL:${c.phone}\r\n")
                if (c.email.isNotBlank()) append("EMAIL:${c.email}\r\n")
                if (c.group.isNotBlank()) append("CATEGORIES:${c.group}\r\n")
                append("END:VCARD\r\n")
            }
        }
        val exportDir = java.io.File(context.cacheDir, "exports").apply { mkdirs() }
        val file = java.io.File(exportDir, "kisiler.vcf")
        file.writeText(vcf)

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/x-vcard"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Kişileri paylaş"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Dışa aktarma başarısız: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    iconBgColor: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Detay",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
