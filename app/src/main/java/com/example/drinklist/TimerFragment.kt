// TimerFragment.kt
package com.example.drinklist

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TimerFragment(
    timeLeft: Int, // Przekazany czas
    setTimeLeft: (Int) -> Unit, // Funkcja do ustawiania nowego czasu
    isRunning: Boolean, // Przekazany stan (czy timer działa)
    setIsRunning: (Boolean) -> Unit // Funkcja do ustawiania stanu stopera
) {
    var currentTimeLeft by rememberSaveable { mutableStateOf(timeLeft) } // Zapisywanie stanu
    var isTimerRunning by rememberSaveable { mutableStateOf(isRunning) } // Zapisywanie stanu stopera

    // Funkcja formatowania czasu
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = formatTime(currentTimeLeft),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start timer
            IconButton(onClick = {
                isTimerRunning = true
                setIsRunning(true)
            }) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start")
            }

            // Pause timer
            IconButton(onClick = {
                isTimerRunning = false
                setIsRunning(false)
            }) {
                Icon(Icons.Default.Pause, contentDescription = "Pause")
            }

            // Stop timer
            IconButton(onClick = {
                isTimerRunning = false
                setIsRunning(false)
                currentTimeLeft = 0
                setTimeLeft(0)
            }) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Umożliwia ustawienie czasu (w sekundach)
        OutlinedTextField(
            value = currentTimeLeft.toString(),
            onValueChange = { value ->
                currentTimeLeft = value.toIntOrNull() ?: 0
                setTimeLeft(currentTimeLeft)
            },
            label = { Text("Ustaw czas (sekundy)") }
        )
    }

    // Obsługa odliczania
    LaunchedEffect(isTimerRunning, currentTimeLeft) {
        if (isTimerRunning && currentTimeLeft > 0) {
            while (isTimerRunning && currentTimeLeft > 0) {
                delay(1000L)
                currentTimeLeft--
                setTimeLeft(currentTimeLeft)
            }
            isTimerRunning = false
            setIsRunning(false)
        }
    }
}

// Pomocnicza funkcja formatowania czasu
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}