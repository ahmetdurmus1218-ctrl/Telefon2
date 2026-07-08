package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ContactEntity
import com.example.ui.viewmodel.ScreenPulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsChartsScreen(
    viewModel: ScreenPulseViewModel,
    onContactClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favoriteContacts.collectAsState()
    val allContacts by viewModel.allContacts.collectAsState()
    var showAddFavoriteDialog by remember { mutableStateOf(false) }
    var searchFavoriteQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Favoriler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${favorites.size} favori kişi",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showAddFavoriteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Favori Ekle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showAddFavoriteDialog = true }) {
                        Text(
                            text = "Düzenle",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
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
        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz favori eklenmedi",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showAddFavoriteDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Favori Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(favorites) { contact ->
                    FavoriteGridItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) }
                    )
                }
            }
        }
    }

    if (showAddFavoriteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddFavoriteDialog = false
                searchFavoriteQuery = ""
            },
            title = {
                Text(
                    text = "Favorileri Düzenle",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    OutlinedTextField(
                        value = searchFavoriteQuery,
                        onValueChange = { searchFavoriteQuery = it },
                        placeholder = { Text("Kişilerde ara...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    val filtered = allContacts.filter {
                        it.name.contains(searchFavoriteQuery, ignoreCase = true) ||
                        it.phone.contains(searchFavoriteQuery, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Kişi bulunamadı",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtered) { contact ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleFavorite(contact) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AvatarView(name = contact.name, badge = "", size = 40.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = contact.name,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = contact.phone,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Checkbox(
                                        checked = contact.isFavorite,
                                        onCheckedChange = { viewModel.toggleFavorite(contact) }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showAddFavoriteDialog = false
                        searchFavoriteQuery = ""
                    }
                ) {
                    Text("Kapat", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun FavoriteGridItem(
    contact: ContactEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Heart Badge indicator
            val badgeColor = when (contact.favoriteBadge) {
                "Annem" -> Color(0xFF4CAF50) // Green
                "Canım" -> Color(0xFFFFEB3B) // Yellow
                "Eşim" -> Color(0xFFE91E63) // Pink/Red
                else -> Color.Transparent
            }

            if (badgeColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Badge",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            } else {
                // If no special badge, show a generic arrow chevron
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Detay",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                    )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarView(name = contact.name, badge = "", size = 64.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Mobil",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
