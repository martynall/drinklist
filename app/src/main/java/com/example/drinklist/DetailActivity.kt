package com.example.drinklist

import com.example.drinklist.model.DrinkDetail
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
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
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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


class DetailActivity : ComponentActivity() {
    @ExperimentalMaterial3WindowSizeClassApi
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val drinkId = intent.getStringExtra("drinkId") ?: ""
        setContent {
            val drinkViewModel = viewModel<DrinkViewModel>(factory = DrinkViewModelFactory())
            val windowSizeClass = calculateWindowSizeClass(this)
            val drink by drinkViewModel.selectedDrink.collectAsState()

            LaunchedEffect(drinkId) { // Użyj LaunchedEffect z kluczem, aby wywołać tylko raz dla danego drinkId
                drinkViewModel.fetchDrinkDetail(drinkId)
            }

            DrinkListTheme {
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        LargeTopAppBar( // Użyj odpowiedniego TopAppBar (Large, Medium, CenterAligned, Small)
                            title = {
                                Column(
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
                                                .fillMaxSize(), // Ustaw wysokość obrazka
                                            contentScale = ContentScale.Fit,
                                        )
                                    }
                                    Text("Mój Dynamiczny Tytuł")
                                }
                            },
                            scrollBehavior = scrollBehavior // Przypisz zachowanie przewijania do paska
                        )

                    }) {
                    internalPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(internalPadding)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {

                        }
                    }
                }
            }
        }
    }
}


@Composable
fun id(){
    val context = LocalContext.current
    val message = (context as? ComponentActivity)?.intent?.getStringExtra("drinkId") ?: "No message"
    Text(text = message)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDetailScreen(
    sizeClass : WindowSizeClass,
    viewModel: DrinkViewModel,
    drinkId: String,
    onBack: (() -> Unit)? = null
) {
    val drinkViewModel: DrinkViewModel = viewModel(factory = DrinkViewModelFactory())
    val drinkDetail by viewModel.selectedDrink.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(drinkId) {
        viewModel.fetchDrinkDetail(drinkId)
    }

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
                            .fillMaxSize(),
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
                }
            }
        }
    }
}



//@ExperimentalMaterial3WindowSizeClassApi
//@Composable
//fun rememberWindowSizeClass(): WindowSizeClass {
//
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp
//    val screenHeight = configuration.screenHeightDp
//
//    val widthSizeClass = when {
//        screenWidth < 600 -> WindowWidthSizeClass.Compact // Typowy smartfon
//        screenWidth < 840 -> WindowWidthSizeClass.Medium // Małe tablety, składane
//        else -> WindowWidthSizeClass.Expanded // Większe tablety, desktop
//    }
//
//    val heightSizeClass = when {
//        screenHeight < 480 -> WindowHeightSizeClass.Compact
//        screenHeight < 900 -> WindowHeightSizeClass.Medium
//        else -> WindowHeightSizeClass.Expanded
//    }
//
//    return WindowSizeClass(widthSizeClass, heightSizeClass)
//}


