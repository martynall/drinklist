// TimerFragment.kt
package com.example.drinklist


import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalFocusManager
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


        TimerInputField(
            timeLeft = currentTimeLeft, // Czas, który jest aktualnie używany przez logikę timera
            setTimeLeft = { newTime ->
                currentTimeLeft = newTime
                if (newTime > 0 && !isTimerRunning) {
                    isTimerRunning = true // Opcjonalnie: uruchom timer automatycznie
                } else if (newTime == 0) {
                    isTimerRunning = false // Opcjonalnie: zatrzymaj timer automatycznie
                }
            } // Callback do aktualizacji czasu timera
        )
        // Umożliwia ustawienie czasu (w sekundach)
//        OutlinedTextField(
//            value = currentTimeLeft.toString(),
//            onValueChange = { value ->
//                currentTimeLeft = value.toIntOrNull() ?: 0
//                setTimeLeft(currentTimeLeft)
//            },
//            label = { Text("Ustaw czas (sekundy)") }
//        )
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


@Composable
fun TimerInputField(
    timeLeft: Int, // Czas, który jest aktualnie używany przez logikę timera
    setTimeLeft: (Int) -> Unit // Callback do aktualizacji czasu timera
) {
    // Zamiast Int, przechowuj wartość String w polu tekstowym
    var textInput by rememberSaveable { mutableStateOf(timeLeft.toString()) }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = textInput, // Wyświetlamy String
        onValueChange = { newValue ->
            // Zezwól na wprowadzanie tylko cyfr, jeśli chcesz
            val filteredValue = newValue.filter { it.isDigit() }
            textInput = filteredValue

            // Konwertuj na Int i zaktualizuj timer TYLKO jeśli to jest poprawna liczba
            // lub jeśli pole nie jest puste
            val newTime = filteredValue.toIntOrNull() ?: 0
            setTimeLeft(newTime)
        },
        label = { Text("Ustaw czas (sekundy)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), // Ustaw klawiaturę numeryczną i akcję Done
        keyboardActions = KeyboardActions(
            onDone = {
                // Po kliknięciu "Done" (Enter) na klawiaturze
                val finalTime = textInput.toIntOrNull() ?: 0
                setTimeLeft(finalTime)
                // Opcjonalnie: ukryj klawiaturę
                focusManager.clearFocus()
            }
        )
    )

    // Opcjonalnie: wyświetl aktualną wartość timera
    // Text("Aktualny czas timera: $timeLeft")
}