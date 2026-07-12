package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ContactEntity
import com.example.ui.viewmodel.ScreenPulseViewModel
import android.content.Context
import android.app.role.RoleManager
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ScreenPulseViewModel,
    onContactClick: (Long) -> Unit,
    onAddContactClick: () -> Unit,
    onNavigateToFavorites: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Check if permission or default dialer is missing
    var hasContactsPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCallLogPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCallPhonePermission by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var isDefaultDialerApp by remember {
        mutableStateOf(isDefaultDialer(context))
    }

    // Refresh status on launch or periodically
    LaunchedEffect(Unit) {
        hasContactsPermission = context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        hasCallLogPermission = context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED
        hasCallPhonePermission = context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        isDefaultDialerApp = isDefaultDialer(context)
        if (hasContactsPermission && hasCallLogPermission) {
            viewModel.syncDeviceData()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasContactsPermission = permissions[android.Manifest.permission.READ_CONTACTS] ?: hasContactsPermission
        hasCallLogPermission = permissions[android.Manifest.permission.READ_CALL_LOG] ?: hasCallLogPermission
        hasCallPhonePermission = permissions[android.Manifest.permission.CALL_PHONE] ?: hasCallPhonePermission
        
        if (hasContactsPermission && hasCallLogPermission) {
            viewModel.syncDeviceData()
        }
    }

    val dialerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultDialerApp = isDefaultDialer(context)
    }

    val contacts by viewModel.allContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showGroupsDialog by remember { mutableStateOf(false) }
    var groupFilter by remember { mutableStateOf<String?>(null) }

    val searchFiltered = if (searchQuery.isEmpty()) {
        contacts
    } else {
        contacts.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.phone.contains(searchQuery, ignoreCase = true) 
        }
    }
    val filteredContacts = groupFilter?.let { g ->
        searchFiltered.filter { it.group.equals(g, ignoreCase = true) }
    } ?: searchFiltered

    // Group contacts by first letter of their name
    val groupedContacts = filteredContacts.groupBy { 
        it.name.firstOrNull()?.uppercaseChar() ?: '#' 
    }.toSortedMap()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Kişiler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = groupFilter?.let { "${filteredContacts.size} kişi • Grup: $it" } ?: "${contacts.size} kişi",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddContactClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Kişi Ekle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Seçenekler",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Kişileri Yeniden Eşitle") },
                                onClick = {
                                    showMenu = false
                                    viewModel.syncDeviceData()
                                },
                                leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Grupları Yönet") },
                                onClick = {
                                    showMenu = false
                                    showGroupsDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Favorileri Düzenle") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToFavorites()
                                },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                            )
                            if (groupFilter != null) {
                                DropdownMenuItem(
                                    text = { Text("Grup Filtresini Kaldır") },
                                    onClick = {
                                        showMenu = false
                                        groupFilter = null
                                    },
                                    leadingIcon = { Icon(Icons.Default.FilterAltOff, contentDescription = null) }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Search Bar in MagicOS 11 style
                SearchBarComponent(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) }
                )

                groupFilter?.let { g ->
                    Surface(
                        onClick = { groupFilter = null },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Grup: $g",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Filtreyi Kaldır",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (searchQuery.isEmpty()) {
                        // Permission & Default Dialer Banner
                        item {
                            PermissionAndDefaultDialerBanner(
                                hasContacts = hasContactsPermission,
                                hasCallLogs = hasCallLogPermission,
                                hasCallPhone = hasCallPhonePermission,
                                isDefaultDialer = isDefaultDialerApp,
                                onRequestPermissions = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.READ_CONTACTS,
                                            android.Manifest.permission.WRITE_CONTACTS,
                                            android.Manifest.permission.READ_CALL_LOG,
                                            android.Manifest.permission.WRITE_CALL_LOG,
                                            android.Manifest.permission.CALL_PHONE
                                        )
                                    )
                                },
                                onRequestDefaultDialer = {
                                    requestDefaultDialer(context, dialerLauncher)
                                }
                            )
                        }

                        // "Kişi grupları" Banner
                        item {
                            GroupsBanner(onClick = { showGroupsDialog = true })
                        }

                        // "Öne çıkanlar" (Featured horizontal list)
                        val featured = contacts.filter { it.favoriteBadge.isNotEmpty() || it.isFavorite }
                        if (featured.isNotEmpty()) {
                            item {
                                FeaturedContactsSection(
                                    featuredContacts = featured,
                                    onContactClick = onContactClick,
                                    onSeeAllClick = onNavigateToFavorites
                                )
                            }
                        }
                    }

                    // Alphabetical Grouped Contacts List
                    if (groupedContacts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Eşleşen kişi bulunamadı",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    } else {
                        groupedContacts.forEach { (letter, contactList) ->
                            item {
                                Text(
                                    text = letter.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }

                            items(contactList) { contact ->
                                ContactRowItem(
                                    contact = contact,
                                    onClick = { onContactClick(contact.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Quick Scroll Alphabet Index on the right (MagicOS style)
            if (searchQuery.isEmpty() && groupedContacts.isNotEmpty()) {
                AlphabetSidebar(
                    letters = groupedContacts.keys.toList()
                )
            }
        }
    }

    if (showGroupsDialog) {
        val groupCounts = contacts
            .mapNotNull { it.group.takeIf { g -> g.isNotBlank() } }
            .groupingBy { it }
            .eachCount()
            .toSortedMap()

        AlertDialog(
            onDismissRequest = { showGroupsDialog = false },
            title = {
                Text("Kişi Grupları", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column {
                    if (groupCounts.isEmpty()) {
                        Text(
                            text = "Henüz bir grup yok. Bir kişiyi düzenlerken \"Grup\" alanına bir isim yazarak (ör. Aile, İş) yeni bir grup oluşturabilirsiniz.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Bir gruba dokunarak kişi listesini o gruba göre filtreleyebilirsiniz.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                            items(groupCounts.entries.toList()) { (group, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            groupFilter = group
                                            showGroupsDialog = false
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.People,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(group, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                    Text("$count kişi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupsDialog = false }) {
                    Text("Kapat", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Ara",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Kişilerde ara",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Temizle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Sesle Ara",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun GroupsBanner(onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Gruplar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kişi grupları",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gruplarınızı yönetin",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Git",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FeaturedContactsSection(
    featuredContacts: List<ContactEntity>,
    onContactClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Öne çıkanlar",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tümünü gör >",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(featuredContacts) { contact ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onContactClick(contact.id) }
                        .padding(vertical = 4.dp)
                ) {
                    AvatarView(
                        name = contact.name,
                        badge = contact.favoriteBadge,
                        size = 56.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = contact.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Mobil",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ContactRowItem(
    contact: ContactEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarView(name = contact.name, badge = "", size = 44.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contact.phone,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 84.dp, end = 24.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )
    }
}

@Composable
fun AvatarView(
    name: String,
    badge: String,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    val initial = name.firstOrNull()?.toString()?.uppercase() ?: "?"
    
    // Choose beautiful background color based on name hash
    val colors = listOf(
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Orange
        Color(0xFFEF4444), // Red
        Color(0xFF06B6D4)  // Cyan
    )
    val colorIndex = Math.abs(name.hashCode()) % colors.size
    val bgColor = colors[colorIndex]

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bgColor, bgColor.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            )
        }
        
        if (badge.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
            )
        }
    }
}

@Composable
fun AlphabetSidebar(
    letters: List<Char>
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(24.dp)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Simple fast scroll list (MagicOS style)
        val allLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#".toList()
        allLetters.forEach { letter ->
            val isPresent = letters.contains(letter)
            Text(
                text = letter.toString(),
                fontSize = 10.sp,
                fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Normal,
                color = if (isPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = isPresent) {
                        /* Handle Fast Scroll Jump if needed */
                    }
            )
        }
    }
}

@Composable
fun PermissionAndDefaultDialerBanner(
    hasContacts: Boolean,
    hasCallLogs: Boolean,
    hasCallPhone: Boolean,
    isDefaultDialer: Boolean,
    onRequestPermissions: () -> Unit,
    onRequestDefaultDialer: () -> Unit
) {
    if (hasContacts && hasCallLogs && hasCallPhone && isDefaultDialer) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = "Telefon Entegrasyonu",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Telefon Entegrasyonu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Gerçek kişilerinizin ve aramalarınızın görünebilmesi için izin verin ve varsayılan yapın.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!hasContacts || !hasCallLogs || !hasCallPhone) {
                    TextButton(
                        onClick = onRequestPermissions,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("İzinleri Ver", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                if (!isDefaultDialer) {
                    Button(
                        onClick = onRequestDefaultDialer,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Varsayılan Yap", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

fun isDefaultDialer(context: Context): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) ?: false
    } else {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
        telecomManager?.defaultDialerPackage == context.packageName
    }
}

fun requestDefaultDialer(context: Context, launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            launcher.launch(intent)
        }
    } else {
        val intent = Intent(android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            .putExtra(android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
        launcher.launch(intent)
    }
}
