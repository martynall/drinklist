package com.example.drinklist

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.Saver
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.drinklist.model.SplashScreen
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.saveable.listSaver
import android.content.Intent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType


val LazyGridStateSaver: Saver<LazyGridState, *> = listSaver(
    save = { listOf(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) },
    restore = { LazyGridState(it[0], it[1]) }
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrinkListTheme {
                Surface {
                    var showSplash by rememberSaveable { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        delay(2000) // splash screen duration
                        showSplash = false
                    }

                    if (showSplash) {
                        SplashScreen(navigateToMain = { showSplash = false })
                    } else {
                        MainApp()
                    }
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MainApp() {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTabletLayout = screenWidthDp >= 600
    val selectedDrinkId = rememberSaveable { mutableStateOf<String?>(null) }
    val selectedTabIndex = rememberSaveable { mutableIntStateOf(0) } // Track selected tab
    val drinkViewModel: DrinkViewModel = viewModel(factory = DrinkViewModelFactory())
    val phoneNumber: MutableState<String> = rememberSaveable { mutableStateOf("") }

    if (isTabletLayout) {
        TabletLayout(
            drinkViewModel = drinkViewModel,
            selectedDrinkId = selectedDrinkId,
            selectedTabIndex = selectedTabIndex
        )
    } else {
        PhoneLayout(
            drinkViewModel = drinkViewModel,
            selectedDrinkId = selectedDrinkId,
            selectedTabIndex = selectedTabIndex,
            phoneNumber = phoneNumber.value,
            onPhoneNumberChanged = { phoneNumber.value = it }
        )

    }
}

@Composable
fun TabletLayout(
    drinkViewModel: DrinkViewModel,
    selectedDrinkId: MutableState<String?>,
    selectedTabIndex: MutableIntState
) {
    Row(Modifier.fillMaxSize()) {
        if (selectedDrinkId.value == null) {
            // Full screen tabs when no drink is selected
            DrinkTabsScreen(
                drinkViewModel = drinkViewModel,
                onDrinkSelected = { selectedDrinkId.value = it },
                selectedTabIndex = selectedTabIndex,
                isTablet = true
            )
        } else {
            // Split view when drink is selected
            Box(Modifier.weight(0.5f)) {
                DrinkTabsScreen(
                    drinkViewModel = drinkViewModel,
                    onDrinkSelected = { selectedDrinkId.value = it },
                    selectedTabIndex = selectedTabIndex,
                    isTablet = true
                )
            }
            Box(Modifier.weight(0.5f)) {
                val context = LocalContext.current
                val intent =  Intent(LocalContext.current, DetailActivity::class.java)
                intent.putExtra("drinkId", selectedDrinkId.value)
                intent.putExtra("phoneNumber", "111111")
                context.startActivity(intent)

                selectedDrinkId.value?.let {
                    DrinkDetailScreen(
                        viewModel = drinkViewModel,
                        drinkId = it,
                        onBack = { selectedDrinkId.value = null }
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneLayout(
    drinkViewModel: DrinkViewModel,
    selectedDrinkId: MutableState<String?>,
    selectedTabIndex: MutableIntState,
    phoneNumber: String,
    onPhoneNumberChanged: (String) -> Unit = {}
) {
    var currentScreen by rememberSaveable { mutableStateOf("tabs") }
    val context = LocalContext.current
    if (currentScreen == "tabs") {
        DrinkTabsScreen(
            drinkViewModel = drinkViewModel,
            onDrinkSelected = {
                selectedDrinkId.value = it
                currentScreen = "detail"
            },
            selectedTabIndex = selectedTabIndex
        )
    } else {
        val intent =  Intent(LocalContext.current, DetailActivity::class.java)
        intent.putExtra("drinkId", selectedDrinkId.value)
        intent.putExtra("phoneNumber", phoneNumber)
        context.startActivity(intent)

        selectedDrinkId.value?.let {
            DrinkDetailScreen(
                viewModel = drinkViewModel,
                drinkId = it,
                onBack = { currentScreen = "tabs" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkTabsScreen(
    drinkViewModel: DrinkViewModel,
    onDrinkSelected: (String) -> Unit,
    selectedTabIndex: MutableIntState,
    isTablet: Boolean = false
) {
    val tabs = listOf("App Info", "Alcoholic", "Non-Alcoholic")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex.intValue) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex.intValue == index,
                    onClick = {
                        selectedTabIndex.intValue = index
                        if (index > 0) { // Don't fetch for App Info tab
                            drinkViewModel.fetchDrinksIfNeeded(
                                when (index) {
                                    1 -> "Alcoholic"
                                    2 -> "Non_Alcoholic"
                                    else -> null
                                }
                            )
                        }
                    },
                    text = { Text(text = title) }
                )
            }
        }
        val phoneNumber = rememberSaveable { mutableStateOf("") }
        // Content for each tab
        when (selectedTabIndex.intValue) {
            0 -> AppInfoScreen(isTablet,phoneNumber.value)
            1 -> DrinkListContent(drinkViewModel, onDrinkSelected)
            2 -> DrinkListContent(drinkViewModel, onDrinkSelected)
        }
    }
}

@Composable
fun PhoneNumberInput(
    phoneNumber: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Numer telefonu",
    placeholder: String = "Wprowadź numer telefonu"
) {
    OutlinedTextField(
        value = phoneNumber,
        onValueChange = { newValue ->
            // Optionally, add basic input validation here
            // For example, allow only digits and '+'
            val filteredValue = newValue.filter { it.isDigit() || it == '+' }
            onValueChange(filteredValue)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // Ensures phone number keyboard
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun AppInfoScreen(isTablet: Boolean, phoneNumber: String) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cocktail Explorer",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
                    AsyncImage(
                    model = "https://www.thecocktaildb.com/images/logo.png",
            contentDescription = "App Logo",
            modifier = Modifier
                .size(if (isTablet) 200.dp else 150.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Discover and explore delicious cocktail recipes from around the world.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Features:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                Text("• Browse hundreds of cocktail recipes")
                Text("• Filter by alcoholic/non-alcoholic")
                Text("• Detailed preparation instructions")
                Text("• Responsive design for all devices")
            }
//            PhoneNumberInput()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                    text = "Data provided by TheCocktailDB",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun DrinkListContent(
    drinkViewModel: DrinkViewModel,
    onDrinkSelected: (String) -> Unit
) {
    val drinks by drinkViewModel.drinkList.collectAsState()
    val loading by drinkViewModel.loading.collectAsState()

    if (loading && drinks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val gridState = rememberSaveable(saver = LazyGridStateSaver) {
            LazyGridState()
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = drinks, key = { it.idDrink ?: "" }) { drink ->
                DrinkItem(
                    drink = drink,
                    onClick = { drink.idDrink?.let(onDrinkSelected) }
                )
            }
        }
    }
}
@Composable
fun DrinkListScreen(
    drinkViewModel: DrinkViewModel,
    onDrinkSelected: (String) -> Unit,
    filter: String? = null
) {
    val context = LocalContext.current
    val drinks by drinkViewModel.drinkList.collectAsState()
    val loading by drinkViewModel.loading.collectAsState()

    LaunchedEffect(filter) {
        drinkViewModel.fetchDrinksIfNeeded(filter)
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val gridState = rememberSaveable(saver = LazyGridStateSaver) {
            LazyGridState()
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = drinks,
                key = { drink -> drink.idDrink ?: "" }
            ) { drink ->
                DrinkItem(
                    drink = drink,
                    onClick = {
                        drink.idDrink?.let { id -> onDrinkSelected(id) }
                    }
                )
            }
        }
    }
}

@Composable
fun DrinkItem(drink: DrinkSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Square image container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(drink.strDrinkThumb)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = drink.strDrink ?: "Brak nazwy",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(8.dp)
                    .heightIn(min = 40.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDetailScreen(
    viewModel: DrinkViewModel,
    drinkId: String,
    onBack: (() -> Unit)? = null
) {
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

@Preview(showBackground = true)
@Composable
fun PreviewPhoneLayout() {
    val drinkViewModel: DrinkViewModel = viewModel(factory = DrinkViewModelFactory())
    DrinkListScreen(
        drinkViewModel = drinkViewModel,
        onDrinkSelected = {}
    )
}

@Composable
fun fabSMS(onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = { onClick() },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(Icons.Filled.Add, "Small floating action button.")
    }
}





