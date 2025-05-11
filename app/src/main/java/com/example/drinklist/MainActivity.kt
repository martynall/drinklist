package com.example.drinklist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drinklist.viewmodel.DrinkViewModel
import com.example.drinklist.viewmodel.DrinkViewModelFactory
import com.example.drinklist.model.DrinkSummary
import com.example.drinklist.ui.theme.DrinkListTheme
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.drinklist.model.SplashScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrinkListTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(navigateToMain = { showSplash = false })
                } else {
                    val screenWidthDp = LocalConfiguration.current.screenWidthDp
                    val isTabletLayout = screenWidthDp >= 600
                    val timeLeft = remember { mutableStateOf(0) }
                    val isRunning = remember { mutableStateOf(false) }
                    val selectedDrinkId = remember { mutableStateOf<String?>(null) }
                    val drinkViewModel: DrinkViewModel = viewModel(
                        factory = DrinkViewModelFactory()
                    )

                    if (isTabletLayout) {
                        Row(Modifier.fillMaxSize()) {
                            Box(Modifier.weight(1f)) {
                                DrinkListScreen(
                                    drinkViewModel = drinkViewModel,
                                    onDrinkSelected = { selectedDrinkId.value = it }
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                selectedDrinkId.value?.let {
                                    DrinkDetailScreen(
                                        viewModel = drinkViewModel,
                                        drinkId = it,
                                        onBack = { selectedDrinkId.value = null }
                                    )
                                }
                            }
                        }
                    } else {
                        var currentScreen by remember { mutableStateOf("list") }

                        if (currentScreen == "list") {
                            DrinkListScreen(
                                drinkViewModel = drinkViewModel,
                                onDrinkSelected = {
                                    selectedDrinkId.value = it
                                    currentScreen = "detail"
                                }
                            )
                        } else {
                            selectedDrinkId.value?.let {
                                DrinkDetailScreen(
                                    viewModel = drinkViewModel,
                                    drinkId = it,
                                    onBack = { currentScreen = "list" },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrinkListScreen(
    drinkViewModel: DrinkViewModel,
    onDrinkSelected: (String) -> Unit
) {
    val drinks by drinkViewModel.drinkList.collectAsState()
    val loading by drinkViewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        if (drinks.isEmpty()) { // Załaduj tylko jeśli lista jest pusta
            drinkViewModel.fetchDrinks()
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(drinks) { drink ->
                DrinkItem(drink = drink, onClick = {
                    drink.idDrink?.let { id -> onDrinkSelected(id) }
                })
            }
        }
    }
}

@Composable
fun DrinkItem(drink: DrinkSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            text = drink.strDrink ?: "Brak nazwy",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDetailScreen(
    viewModel: DrinkViewModel,
    drinkId: String,
    onBack: (() -> Unit)? = null // null na tablecie, przekazany na telefonie
) {
    val drinkDetail by viewModel.selectedDrink.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // Wykonaj fetch szczegółów tylko raz, kiedy drinkId się zmieni
    LaunchedEffect(drinkId) {
        viewModel.fetchDrinkDetail(drinkId)
    }

    // Zarządzanie stanem timera
    val timeLeft = rememberSaveable { mutableStateOf(0) } // Zapisywanie stanu czasu
    val isRunning = rememberSaveable { mutableStateOf(false) } // Zapisywanie stanu stopera (czy działa)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // <-- Scroll!
    ) {
        if (onBack != null) {
            CenterAlignedTopAppBar(
                title = { Text("Drink Details") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

                    // Wywołanie TimerFragment z odpowiednimi parametrami
                    TimerFragment(
                        timeLeft = timeLeft.value,
                        setTimeLeft = { timeLeft.value = it },
                        isRunning = isRunning.value,
                        setIsRunning = { isRunning.value = it }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPhoneLayout() {
    val drinkViewModel: DrinkViewModel = viewModel(factory = DrinkViewModelFactory())
    DrinkListScreen(
        drinkViewModel = drinkViewModel,
        onDrinkSelected = {}
    )
}


