package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ContactEntity
import com.example.ui.viewmodel.ScreenPulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    contactId: Long?, // null if adding new
    viewModel: ScreenPulseViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.allContacts.collectAsState()
    val contact = remember(contactId, contacts) {
        if (contactId != null) contacts.find { it.id == contactId } else null
    }

    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phone ?: "") }
    var email by remember { mutableStateOf(contact?.email ?: "") }
    var birthday by remember { mutableStateOf(contact?.birthday ?: "") }
    var group by remember { mutableStateOf(contact?.group ?: "") }
    var other by remember { mutableStateOf(contact?.other ?: "") }
    var isFavorite by remember { mutableStateOf(contact?.isFavorite ?: false) }
    var favoriteBadge by remember { mutableStateOf(contact?.favoriteBadge ?: "") }

    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (contactId == null) "Yeni Kişi" else "Kişiyi Düzenle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "İptal")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isBlank() || phone.isBlank()) {
                                showError = true
                            } else {
                                if (contactId == null) {
                                    viewModel.addContact(
                                        name = name,
                                        phone = phone,
                                        email = email,
                                        birthday = birthday,
                                        group = group,
                                        other = other,
                                        isFavorite = isFavorite || favoriteBadge.isNotEmpty(),
                                        favoriteBadge = favoriteBadge,
                                        onComplete = onBackClick
                                    )
                                } else {
                                    contact?.let {
                                        viewModel.updateContact(
                                            it.copy(
                                                name = name,
                                                phone = phone,
                                                email = email,
                                                birthday = birthday,
                                                group = group,
                                                other = other,
                                                isFavorite = isFavorite || favoriteBadge.isNotEmpty(),
                                                favoriteBadge = favoriteBadge
                                            ),
                                            onComplete = onBackClick
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Kaydet",
                            tint = MaterialTheme.colorScheme.primary
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            item {
                if (showError) {
                    Text(
                        text = "Lütfen Ad ve Telefon bilgilerini doldurun.",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("Ad Soyad") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; showError = false },
                    label = { Text("Telefon Numarası") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    label = { Text("Doğum Günü (Örn: 15 Mayıs 1995)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    label = { Text("Grup (Örn: Arkadaşlar, Aile, İş)") },
                    leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = other,
                    onValueChange = { other = it },
                    label = { Text("Notlar / Diğer") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Öne Çıkanlar Badge Seçenekleri",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        val badges = listOf("Annem", "Babam", "Eşim", "Canım", "")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            badges.forEach { badge ->
                                val labelText = badge.ifEmpty { "Yok" }
                                val isSelected = favoriteBadge == badge
                                val chipColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                                Surface(
                                    onClick = { 
                                        favoriteBadge = badge 
                                        if (badge.isNotEmpty()) {
                                            isFavorite = true
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = chipColor,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = labelText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Favorilere Ekle",
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Switch(
                                checked = isFavorite || favoriteBadge.isNotEmpty(),
                                onCheckedChange = { isFavorite = it }
                            )
                        }
                    }
                }
            }

            if (contactId != null) {
                item {
                    Button(
                        onClick = {
                            contact?.let {
                                viewModel.deleteContact(it, onComplete = onBackClick)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.1f),
                            contentColor = Color.Red
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kişiyi Sil", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
