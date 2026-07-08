package com.example

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.telecom.Call
import android.telecom.VideoProfile
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ScreenPulseDatabase
import com.example.data.database.CallLogEntity
import com.example.service.CallManager
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val call = CallManager.activeCall
        val isSimulation = intent.getBooleanExtra("is_simulation", false)
        val simNumber = intent.getStringExtra("sim_number") ?: ""
        val simName = intent.getStringExtra("sim_name") ?: ""
        val simType = intent.getStringExtra("sim_type") ?: "Gelen"

        if (call == null && !isSimulation) {
            finish()
            return
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                if (isSimulation) {
                    SimulatedCallScreenContent(
                        number = simNumber,
                        initialName = simName,
                        type = simType,
                        onFinished = { finish() }
                    )
                } else {
                    CallScreenContent(call = call!!, onFinished = { finish() })
                }
            }
        }
    }
}

@Composable
fun CallScreenContent(
    call: Call,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var callState by remember { mutableStateOf(call.state) }
    
    // Listen to call state changes
    DisposableEffect(call) {
        val callback = object : Call.Callback() {
            override fun onStateChanged(c: Call, state: Int) {
                callState = state
                if (state == Call.STATE_DISCONNECTED) {
                    onFinished()
                }
            }
        }
        call.registerCallback(callback)
        onDispose {
            call.unregisterCallback(callback)
        }
    }

    // Get number and fetch contact name
    val number = remember(call) {
        val handle = call.details?.handle
        handle?.schemeSpecificPart ?: "Bilinmeyen"
    }

    var callerName by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(number) {
        try {
            val database = ScreenPulseDatabase.getDatabase(context)
            val contacts = database.usageDao().getAllContacts().first()
            val match = contacts.find { it.phone.replace(" ", "") == number.replace(" ", "") }
            callerName = match?.name
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Timer for active call
    var secondsElapsed by remember { mutableStateOf(0) }
    LaunchedEffect(callState) {
        if (callState == Call.STATE_ACTIVE) {
            while (true) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    val durationText = remember(secondsElapsed) {
        val mins = secondsElapsed / 60
        val secs = secondsElapsed % 60
        String.format("%02d:%02d", mins, secs)
    }

    val stateText = when (callState) {
        Call.STATE_RINGING -> "Gelen Arama"
        Call.STATE_DIALING -> "Aranıyor..."
        Call.STATE_ACTIVE -> "Arama Aktif"
        Call.STATE_HOLDING -> "Beklemede"
        Call.STATE_DISCONNECTING -> "Kapatılıyor..."
        Call.STATE_DISCONNECTED -> "Arama Sonlandırıldı"
        else -> "Bağlanıyor..."
    }

    // Speaker management
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    var isSpeakerOn by remember { mutableStateOf(audioManager.isSpeakerphoneOn) }

    // Pulsing circle animation for ringing state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1319)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Text(
                    text = stateText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (callState == Call.STATE_ACTIVE) {
                    Text(
                        text = durationText,
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Central Avatar Area
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                if (callState == Call.STATE_RINGING) {
                    // Pulsing Ring Backdrops
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF0066FF).copy(alpha = pulseAlpha))
                            .scale(pulseScale)
                    )
                }

                // Main Avatar Icon Card
                val initial = (callerName ?: number).firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Caller Identity and Dynamic Action Row
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = callerName ?: "Bilinmeyen Numara",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = number,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Action Call Control Panel
                if (callState == Call.STATE_RINGING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reject / Decline Call
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FloatingActionButton(
                                onClick = {
                                    try {
                                        call.reject(false, null)
                                    } catch (e: Exception) {
                                        call.disconnect()
                                    }
                                },
                                containerColor = Color(0xFFE53935),
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Reddet",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Reddet", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }

                        // Answer Call
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FloatingActionButton(
                                onClick = {
                                    call.answer(VideoProfile.STATE_AUDIO_ONLY)
                                },
                                containerColor = Color(0xFF43A047),
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Cevapla",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cevapla", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                } else {
                    // Active Call Control: Mute/Speaker Row + Red Decline FAB centered below
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speaker Action
                        FilledIconToggleButton(
                            checked = isSpeakerOn,
                            onCheckedChange = {
                                isSpeakerOn = it
                                audioManager.isSpeakerphoneOn = it
                            },
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledIconToggleButtonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                contentColor = Color.White,
                                checkedContainerColor = Color.White,
                                checkedContentColor = Color.Black
                            )
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Hoparlör"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Red Hang up circle centered
                    FloatingActionButton(
                        onClick = {
                            call.disconnect()
                        },
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Sonlandır",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedCallScreenContent(
    number: String,
    initialName: String,
    type: String,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var callState by remember { mutableStateOf(if (type == "Gelen") 2 else 1) } // 2 = STATE_RINGING, 1 = STATE_DIALING, 3 = STATE_ACTIVE
    
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isMuteOn by remember { mutableStateOf(false) }
    var isHoldOn by remember { mutableStateOf(false) }
    var isShowDialpad by remember { mutableStateOf(false) }
    var dtmfDigits by remember { mutableStateOf("") }

    // Timer for active call
    var secondsElapsed by remember { mutableStateOf(0) }
    LaunchedEffect(callState, isHoldOn) {
        if (callState == 1) { // DIALING
            delay(2000)
            callState = 3 // ACTIVE
        }
        if (callState == 3 && !isHoldOn) { // ACTIVE & NOT HOLDING
            while (true) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    val durationText = remember(secondsElapsed) {
        val mins = secondsElapsed / 60
        val secs = secondsElapsed % 60
        String.format("%02d:%02d", mins, secs)
    }

    val stateText = when {
        isHoldOn -> "Beklemede"
        callState == 2 -> "Gelen Arama"
        callState == 1 -> "Aranıyor..."
        callState == 3 -> "Arama Aktif"
        else -> "Bağlanıyor..."
    }

    // Pulsing circle animation for ringing state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Save helper
    val endCallAndSaveLog: (Boolean) -> Unit = { wasAnswered ->
        coroutineScope.launch {
            try {
                val database = ScreenPulseDatabase.getDatabase(context)
                val cleanNum = number.replace(" ", "")
                val contacts = database.usageDao().getAllContacts().first()
                val match = contacts.find { it.phone.replace(" ", "") == cleanNum }
                
                val finalCallType = if (type == "Gelen") {
                    if (wasAnswered) "Gelen" else "Cevapsız"
                } else {
                    "Giden"
                }
                
                database.usageDao().insertCallLog(
                    CallLogEntity(
                        contactId = match?.id,
                        number = number,
                        name = match?.name ?: initialName,
                        callType = finalCallType,
                        timestamp = System.currentTimeMillis(),
                        durationSeconds = if (finalCallType == "Cevapsız") 0 else secondsElapsed,
                        category = "Mobil"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onFinished()
        }
    }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1319)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                Text(
                    text = stateText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (callState == 3) {
                    Text(
                        text = durationText,
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Central Visual Area: Waveform Visualizer or Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                if (callState == 2) {
                    // Pulsing Ring Backdrops
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF0066FF).copy(alpha = pulseAlpha))
                            .scale(pulseScale)
                    )
                }

                if (callState == 3 && !isHoldOn && !isShowDialpad) {
                    AudioWaveformVisualizer()
                } else {
                    // Main Avatar Icon Card
                    val displayName = initialName.ifEmpty { number }
                    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Caller Identity and Dynamic Action Row
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = initialName.ifEmpty { "Bilinmeyen Numara" },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = number,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // DTMF Dialpad overlay
                if (isShowDialpad) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16202C)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dtmfDigits.ifEmpty { " " },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 2.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val digits = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("*", "0", "#")
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                digits.forEach { row ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        row.forEach { digit ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White.copy(alpha = 0.08f))
                                                    .clickable {
                                                        dtmfDigits += digit
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = digit,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            TextButton(onClick = { isShowDialpad = false }) {
                                Text("Gizle", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Action Call Control Panel
                if (callState == 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reject / Decline Call
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FloatingActionButton(
                                onClick = { endCallAndSaveLog(false) },
                                containerColor = Color(0xFFE53935),
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = "Reddet",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Reddet", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }

                        // Answer Call
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FloatingActionButton(
                                onClick = { callState = 3 },
                                containerColor = Color(0xFF43A047),
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Cevapla",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cevapla", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        }
                    }
                } else {
                    // Active Call Control: 2x3 Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CallActionButton(
                                icon = if (isMuteOn) Icons.Default.MicOff else Icons.Default.Mic,
                                label = "Sessiz",
                                checked = isMuteOn,
                                onClick = { isMuteOn = !isMuteOn }
                            )
                            CallActionButton(
                                icon = Icons.Default.Dialpad,
                                label = "Klavye",
                                checked = isShowDialpad,
                                onClick = { isShowDialpad = !isShowDialpad }
                            )
                            CallActionButton(
                                icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                label = "Hoparlör",
                                checked = isSpeakerOn,
                                onClick = {
                                    isSpeakerOn = !isSpeakerOn
                                    audioManager.isSpeakerphoneOn = !isSpeakerOn
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CallActionButton(
                                icon = if (isHoldOn) Icons.Default.PlayArrow else Icons.Default.Pause,
                                label = if (isHoldOn) "Sürdür" else "Beklet",
                                checked = isHoldOn,
                                onClick = { isHoldOn = !isHoldOn }
                            )
                            CallActionButton(
                                icon = Icons.Default.PersonAdd,
                                label = "Kişi Ekle",
                                checked = false,
                                onClick = {
                                    android.widget.Toast.makeText(context, "Kişi ekleme simüle ediliyor...", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                            CallActionButton(
                                icon = Icons.Default.Videocam,
                                label = "Görüntülü",
                                checked = false,
                                onClick = {
                                    android.widget.Toast.makeText(context, "Görüntülü aramaya geçiş simüle ediliyor...", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Red Hang up circle centered
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        FloatingActionButton(
                            onClick = { endCallAndSaveLog(true) },
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Sonlandır",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        FilledIconToggleButton(
            checked = checked,
            onCheckedChange = { if (enabled) onClick() },
            shape = CircleShape,
            modifier = Modifier.size(56.dp),
            enabled = enabled,
            colors = IconButtonDefaults.filledIconToggleButtonColors(
                containerColor = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
                checkedContainerColor = Color.White,
                checkedContentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                disabledContentColor = Color.White.copy(alpha = 0.3f)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (enabled) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AudioWaveformVisualizer() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animValues = List(7) { index ->
        infiniteTransition.animateFloat(
            initialValue = 10f,
            targetValue = 60f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 300 + (index * 120),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Row(
        modifier = Modifier
            .height(70.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animValues.forEach { anim ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(anim.value.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            )
        }
    }
}
