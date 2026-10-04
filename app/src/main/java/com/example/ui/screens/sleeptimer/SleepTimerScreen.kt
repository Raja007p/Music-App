package com.example.ui.screens.sleeptimer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatDuration
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PurpleBlueGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SleepTimerScreen(
    remainingMs: Long?,
    isEndAfterCurrentSong: Boolean,
    onBack: () -> Unit,
    onStartTimer: (Int) -> Unit,
    onSetEndAfterSong: (Boolean) -> Unit,
    onCancelTimer: () -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(30) }
    var customMinutesInput by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }

    val isRunning = remainingMs != null || isEndAfterCurrentSong

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cosmic Moon Illustration Card
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF090C15))
                        )
                    )
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Subtle stars background
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(alpha = 0.7f), radius = 2f, center = Offset(size.width * 0.3f, size.height * 0.25f))
                    drawCircle(Color.White.copy(alpha = 0.5f), radius = 1.5f, center = Offset(size.width * 0.7f, size.height * 0.2f))
                    drawCircle(Color.White.copy(alpha = 0.8f), radius = 2.5f, center = Offset(size.width * 0.8f, size.height * 0.45f))
                    drawCircle(Color.White.copy(alpha = 0.4f), radius = 1.5f, center = Offset(size.width * 0.2f, size.height * 0.6f))
                    drawCircle(Color(0xFF06B6D4), radius = 3f, center = Offset(size.width * 0.65f, size.height * 0.7f))
                }

                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = "Moon",
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier.size(68.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Timer display status
            if (isRunning) {
                if (isEndAfterCurrentSong) {
                    Text(
                        text = "Stops after current track",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                } else if (remainingMs != null) {
                    Text(
                        text = formatDuration(remainingMs),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 32.sp
                        )
                    )
                    val finishTime = remember(remainingMs) {
                        val targetTime = System.currentTimeMillis() + remainingMs
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(targetTime))
                    }
                    Text(
                        text = "Until $finishTime",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }
            } else {
                Text(
                    text = "$selectedMinutes minutes",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                )
                val targetTime = remember(selectedMinutes) {
                    val finish = System.currentTimeMillis() + selectedMinutes * 60 * 1000L
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(finish))
                }
                Text(
                    text = "Until $targetTime",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // End after current song
                TimerOptionRow(
                    label = "End after current song",
                    isSelected = isEndAfterCurrentSong,
                    onClick = {
                        onSetEndAfterSong(!isEndAfterCurrentSong)
                    }
                )

                // Presets: 5, 10, 30, 45, 60 minutes
                listOf(5, 10, 30, 45, 60).forEach { mins ->
                    TimerOptionRow(
                        label = "$mins minutes",
                        isSelected = !isEndAfterCurrentSong && !isCustomSelected && selectedMinutes == mins,
                        onClick = {
                            isCustomSelected = false
                            selectedMinutes = mins
                            if (isRunning) {
                                onStartTimer(mins)
                            }
                        }
                    )
                }

                // Custom time option
                TimerOptionRow(
                    label = "Custom time",
                    isSelected = isCustomSelected,
                    onClick = {
                        isCustomSelected = true
                    }
                )

                if (isCustomSelected) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customMinutesInput,
                            onValueChange = {
                                customMinutesInput = it.filter { ch -> ch.isDigit() }
                                val parsed = customMinutesInput.toIntOrNull()
                                if (parsed != null && parsed > 0) {
                                    selectedMinutes = parsed
                                }
                            },
                            placeholder = { Text("Minutes", color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Button
            Button(
                onClick = {
                    if (isRunning) {
                        onCancelTimer()
                    } else {
                        onStartTimer(selectedMinutes)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isRunning) "Stop Timer" else "Start Timer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TimerOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else DarkBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) TextWhite else TextMuted
            )
        )
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
