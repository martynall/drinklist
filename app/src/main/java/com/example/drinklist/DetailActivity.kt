package com.example.drinklist


import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.drinklist.model.SplashScreen
import com.example.drinklist.ui.theme.DrinkListTheme
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.drinklist.viewmodel.DrinkViewModel
import com.example.drinklist.viewmodel.DrinkViewModelFactory
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.input.nestedscroll.nestedScroll
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.text.input.KeyboardType
import com.example.drinklist.model.DrinkDetail
import kotlin.text.isNotBlank


class DetailActivity : ComponentActivity() {
    @ExperimentalMaterial3WindowSizeClassApi
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val activityContext: Context = this
        val drinkId = intent.getStringExtra("drinkId") ?: ""
        val telephoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        setContent {
            val drinkViewModel = viewModel<DrinkViewModel>(factory = DrinkViewModelFactory())
            val windowSizeClass = calculateWindowSizeClass(this)
            val drink by drinkViewModel.selectedDrink.collectAsState()
            var activityPhoneNumber by rememberSaveable { mutableStateOf("") }


            LaunchedEffect(drinkId) { // Użyj LaunchedEffect z kluczem, aby wywołać tylko raz dla danego drinkId
                drinkViewModel.fetchDrinkDetail(drinkId)
            }

            DrinkListTheme {
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                Scaffold(
                    floatingActionButton = {
                        if (drink != null && activityPhoneNumber.isNotBlank()) {
                            FloatingActionButton(
                                onClick = {
                                }
                            ) {
                                if(activityPhoneNumber == "731177499") {
                                    SmsSenderScreenWithPermissions(
                                        drink = drink,
                                        phoneNumber = activityPhoneNumber
                                    )
                                    Icon(Icons.Filled.Send, "Wyślij SMS")
                                }
                            }
                        }
                    },
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        CenterAlignedTopAppBar( // Użyj odpowiedniego TopAppBar (Large, Medium, CenterAligned, Small)
                            title = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    drink?.let { dr ->
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(dr.strDrinkThumb)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxHeight(), // Ustaw wysokość obrazka
                                            contentScale = ContentScale.Fit,
                                        )
                                    }
                                    Text("@string/details")
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            scrollBehavior = scrollBehavior // Przypisz zachowanie przewijania do paska
                        )

                    }) {
                        internalPadding ->
                    Box(
                        modifier = Modifier
                            .padding(internalPadding)
                    ) {

                        DrinkDetailScreen(
                            sizeClass = windowSizeClass,
                            viewModel = drinkViewModel,
                            drinkId = drinkId,
                            onBack = null,
                            phoneNumber = activityPhoneNumber,
                            onPhoneNumberChange = { newNumber ->
                                activityPhoneNumber = newNumber
                            })
                    }
                }

            }
        }
    }
}


@Composable
fun App(){


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDetailScreen(
    sizeClass : WindowSizeClass,
    viewModel: DrinkViewModel,
    drinkId: String,
    onBack: (() -> Unit)? = null,
    phoneNumber: String,
    onPhoneNumberChange:(String) -> Unit
) {
    val drinkViewModel: DrinkViewModel = viewModel(factory = DrinkViewModelFactory())
    val drinkDetail by viewModel.selectedDrink.collectAsState()


    LaunchedEffect(drinkId) {
        viewModel.fetchDrinkDetail(drinkId)
    }
    val loading by viewModel.loading.collectAsState()
    val timeLeft = rememberSaveable { mutableStateOf(0) }
    val isRunning = rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (onBack != null) {
            CenterAlignedTopAppBar(
                title = { Text("Drink Details") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }


        Column(modifier = Modifier.padding(16.dp)) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                drinkDetail?.let { drink ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(drink.strDrinkThumb)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = drink.strDrink ?: "Brak nazwy",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = drink.strInstructions ?: "Brak instrukcji",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Składniki:")

                    listOfNotNull(
                        drink.strIngredient1,
                        drink.strIngredient2,
                        drink.strIngredient3,
                        drink.strIngredient4,
                        drink.strIngredient5,
                        drink.strIngredient6,
                        drink.strIngredient7,
                        drink.strIngredient8,
                        drink.strIngredient9,
                        drink.strIngredient10,
                        drink.strIngredient11,
                        drink.strIngredient12,
                        drink.strIngredient13,
                        drink.strIngredient14,
                        drink.strIngredient15,
                    ).forEach { ingredient ->
                        Text(ingredient, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    TimerFragment(
                        timeLeft = timeLeft.value,
                        setTimeLeft = { timeLeft.value = it },
                        isRunning = isRunning.value,
                        setIsRunning = { isRunning.value = it }
                    )
                    PhoneNumberInput(phoneNumber = phoneNumber,onPhoneNumberConfirmed = {numer -> onPhoneNumberChange(numer)})
                }
            }
        }
    }
}



@Composable
fun checkIfSmsSupported(): Boolean {
    val context = LocalContext.current
    return remember(context) { // Użyj remember, żeby to nie było obliczane przy każdej rekompozycji
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }
}


fun sendSmsDirectly(context: Context, phoneNumber: String, message: String) {
    if (!deviceCanSendSms(context)) {
        Toast.makeText(context, "To urządzenie nie obsługuje wysyłania wiadomości SMS.", Toast.LENGTH_LONG).show()
        return
    }

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(context, "Brak uprawnienia do wysyłania SMS. Proszę przyznaj uprawnienie w ustawieniach aplikacji.", Toast.LENGTH_LONG).show()
        return
    }

    if (phoneNumber.isBlank()) {
        Toast.makeText(context, "Numer telefonu nie może być pusty.", Toast.LENGTH_SHORT).show()
        return
    }
    if (message.isBlank()) {
        Toast.makeText(context, "Wiadomość nie może być pusta.", Toast.LENGTH_SHORT).show()
        return
    }

    // 3. Spróbuj wysłać SMS-a
    try {
        // Pobierz instancję SmsManager
        // Używamy nowszego API dla Androida S (API 31) i wyżej, inaczej używamy getDefault()
        val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION") // Suppress lint warning for deprecated getDefault()
            SmsManager.getDefault()
        }

        // Wysyłanie wiadomości tekstowej
        // Ostatnie dwa parametry (sentIntent, deliveryIntent) mogą być użyte do otrzymywania powiadomień
        // o statusie wysyłki i dostarczenia SMS-a, ale dla prostoty w tym przykładzie są null.
        val parts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)

        Toast.makeText(context, "wiadomość: $message", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        // Obsługa błędów wysyłania
        Toast.makeText(context, "Błąd wysyłania SMS: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        e.printStackTrace() // Wypisz stos wyjątku do logcat dla debugowania
    }
}


@Composable
fun PhoneNumberInput(
    phoneNumber: String,
    onPhoneNumberConfirmed: (String) -> Unit,
    modifier: Modifier = Modifier // Pozwala na modyfikację wyglądu z zewnątrz
) {
    var phoneNumber by remember { mutableStateOf(phoneNumber) }
    var phoneNumberError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(), // Wypełnia szerokość dostępnego miejsca
        verticalArrangement = Arrangement.spacedBy(12.dp) // Odstępy między elementami
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { newValue ->
                phoneNumber = newValue.filter { it.isDigit() }
                phoneNumberError = false
            },
            label = { Text("Numer telefonu") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = phoneNumberError,
            supportingText = {
                if (phoneNumberError) {
                    Text("Proszę wprowadzić poprawny numer telefonu (min. 9 cyfr).")
                } else {
                    Text("Wprowadź swój numer telefonu (np. 123456789)")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (phoneNumber.length >= 9) {
                    onPhoneNumberConfirmed(phoneNumber)
                } else {
                    phoneNumberError = true
                }
            },
            enabled = phoneNumber.length >= 9,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Potwierdź numer")
        }
    }
}

@Composable
fun SmsSenderScreenWithPermissions(drink: DrinkDetail?, phoneNumber: String) {
    val context = LocalContext.current // Uzyskaj kontekst


    // Launcher do obsługi prośby o uprawnienia
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(context, "Uprawnienie do wysyłania SMS odrzucone.", Toast.LENGTH_SHORT).show()
        }
    }

    val messageToSend by remember(drink) { // Oblicz ponownie, gdy `drink` się zmieni
        derivedStateOf {
            if (drink == null) {
                "" // Pusta wiadomość, jeśli drink jest null
            } else {
                // Budowanie wiadomości krok po kroku
                val builder = StringBuilder()

                // 1. Nazwa drinka
                builder.append("Drink: ${drink.strDrink}\n")

//                 2. Składniki i miary
                builder.append("Składniki:\n")
                val ingredients = listOfNotNull(
                    drink.strIngredient1,
                    drink.strIngredient2,
                    drink.strIngredient3,
                    drink.strIngredient4,
                    drink.strIngredient5,
                    drink.strIngredient6,
                    drink.strIngredient7,
                    drink.strIngredient8,
                    drink.strIngredient9,
                    drink.strIngredient10,
                    drink.strIngredient11,
                    drink.strIngredient12,
                    drink.strIngredient13,
                    drink.strIngredient14,
                    drink.strIngredient15,
                ).filter { it.isNotBlank() } // Usuń puste wpisy po formatowaniu

                if (ingredients.isNotEmpty()) {
                    ingredients.forEach { builder.append("- $it\n") }
                } else {
                    builder.append("Brak informacji o składnikach.\n")
                }
                builder.toString()
            }
        }
    }

    Column(modifier = Modifier.padding(10.dp)) {
        // ... (Pola OutlinedTextField dla numeru i wiadomości)

        Button(
            onClick = {
                if (messageToSend.isBlank()) {
                    Toast.makeText(context, "Wpisz numer telefonu i treść wiadomości.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Sprawdź, czy uprawnienie SEND_SMS jest już przyznane
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.SEND_SMS // Upewnij się, że to jest poprawny Manifest.permission
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Uprawnienie już przyznane, wyślij SMS-a bezpośrednio
                        sendSmsDirectly(context, phoneNumber, messageToSend)
                    }
                    else -> {
                        // Poproś o uprawnienie za pomocą launchera
                        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS) // Upewnij się, że to jest poprawny Manifest.permission
                    }
                }
            },
            // ... (Modyfikatory przycisku)
        ) {     Icon(
            imageVector = Icons.Filled.Send, // Zmieniona ikona
            contentDescription = "Wyślij SMS" // Zaktualizowany opis
        )
        }
    }
}

fun deviceCanSendSms(context: Context): Boolean {
    val packageManager = context.packageManager
    return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_MESSAGING)
}