package com.example.drinklist

import android.annotation.SuppressLint
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import android.telephony.SmsManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.drinklist.model.DrinkDetail
import com.example.drinklist.model.DrinkSummary
import com.example.drinklist.model.SplashScreen
import com.example.drinklist.ui.theme.DrinkListTheme
import com.example.drinklist.viewmodel.DrinkViewModel
import com.example.drinklist.viewmodel.DrinkViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.runtime.derivedStateOf
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FloatingActionButton

val LazyGridStateSaver: Saver<LazyGridState, *> = listSaver(
    save = { listOf(it.firstVisibleItemIndex, it.firstVisibleItemScrollOffset) },
    restore = { LazyGridState(it[0], it[1]) }
)

private fun sendSms(context: Context, ingredients: List<String>) {
    val smsText = "Składniki drinka:\n" + ingredients.joinToString("\n")
    val uri = Uri.parse("smsto:")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
        putExtra("sms_body", smsText)
    }
    context.startActivity(intent)
}

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
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTabletLayout = remember (screenWidthDp, screenHeightDp) {
        (screenHeightDp >= 600) && (screenWidthDp >= 600)
    }
    val selectedDrinkId = rememberSaveable { mutableStateOf<String?>(null) }
    val selectedTabIndex = rememberSaveable { mutableIntStateOf(0) } // Track selected tab
    val drinkViewModel: DrinkViewModel = viewModel(factory = DrinkViewModelFactory())
    val phoneNumber = rememberSaveable { mutableStateOf("")}

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Add a key to trigger refresh when selectedTabIndex changes
    val refreshKey = remember { mutableStateOf(0) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text(text = "App Info") },
                    selected = selectedTabIndex.intValue == 0,
                    onClick = {
                        coroutineScope.launch {
                            selectedTabIndex.intValue = 0
                            selectedDrinkId.value = null // Reset selected drink
                            refreshKey.value++
                            drawerState.close()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "App Info") }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Alcoholic") },
                    selected = selectedTabIndex.intValue == 1,
                    onClick = {
                        coroutineScope.launch {
                            selectedTabIndex.intValue = 1
                            selectedDrinkId.value = null // Reset selected drink
                            refreshKey.value++
                            drawerState.close()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Alcoholic") }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Non-Alcoholic") },
                    selected = selectedTabIndex.intValue == 2,
                    onClick = {
                        coroutineScope.launch {
                            selectedTabIndex.intValue = 2
                            selectedDrinkId.value = null // Reset selected drink
                            refreshKey.value++
                            drawerState.close()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Non-Alcoholic") }
                )
                NavigationDrawerItem(
                    label = { Text(text = "Close Menu") },
                    selected = false,
                    onClick = { coroutineScope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        if (isTabletLayout) {
            TabletLayout(
                drinkViewModel = drinkViewModel,
                selectedDrinkId = selectedDrinkId,
                selectedTabIndex = selectedTabIndex,
                openDrawer = { coroutineScope.launch { drawerState.open() } },
                refreshKey = refreshKey, // Pass the refresh key
                updateTabIndex = { index -> selectedTabIndex.intValue = index },
                phoneNumber = phoneNumber.value ,
                onPhoneConfirmed = {phone -> phoneNumber.value = phone }
                // Pass the lambda
            )
        } else {
            PhoneLayout(
                drinkViewModel = drinkViewModel,
                selectedDrinkId = selectedDrinkId,
                selectedTabIndex = selectedTabIndex,
                openDrawer = { coroutineScope.launch { drawerState.open() } },
                refreshKey = refreshKey, // Pass the refresh key
                updateTabIndex = { index -> selectedTabIndex.intValue = index },
                phoneNumber = phoneNumber.value, // Pass the lambda
                onPhoneConfirmed = { phone -> phoneNumber.value = phone }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLayout(
    drinkViewModel: DrinkViewModel,
    selectedDrinkId: MutableState<String?>,
    selectedTabIndex: MutableIntState,
    openDrawer: () -> Unit,
    refreshKey: MutableState<Int>, // Receive the refresh key
    updateTabIndex: (Int) -> Unit,
    phoneNumber:String,
    onPhoneConfirmed: (String) -> Unit
) {
    var currentScreen by rememberSaveable { mutableStateOf("tabs") }
    val context = LocalContext.current

    Scaffold { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                "tabs" -> {
                    DrinkTabsScreen(
                        drinkViewModel = drinkViewModel,
                        onDrinkSelected = {
                            selectedDrinkId.value = it
                            currentScreen = "detail"
                        },
                        selectedTabIndex = selectedTabIndex,
                        openDrawer = openDrawer,
                        onTabSelected = {
                            currentScreen = "tabs"
                            selectedDrinkId.value = null
                        },
                        refreshKey = refreshKey // Pass the refresh key
                    )
                }
                "detail" -> {
                    selectedDrinkId.value?.let {
                        DrinkDetailScreen(
                            viewModel = drinkViewModel,
                            drinkId = it,
                            onBack = { currentScreen = "tabs" },
                            onSendSms = { ingredients -> sendSms(context, ingredients) },
                            onMenuClick = openDrawer,
                            phoneNumber = phoneNumber,
                            onPhoneConfirmed = {phone -> onPhoneConfirmed(phone)}
                        )
                    } ?: run {
                        currentScreen = "tabs"
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletLayout(
    drinkViewModel: DrinkViewModel,
    selectedDrinkId: MutableState<String?>,
    selectedTabIndex: MutableIntState,
    openDrawer: () -> Unit,
    refreshKey: MutableState<Int>, // Receive the refresh key
    updateTabIndex: (Int) -> Unit,
    phoneNumber:String,
    onPhoneConfirmed: (String) -> Unit
) {
    val context = LocalContext.current

    Scaffold() { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (selectedDrinkId.value == null) {
                // Full screen tabs when no drink is selected
                Box(Modifier.fillMaxSize()) {
                    DrinkTabsScreen(
                        drinkViewModel = drinkViewModel,
                        onDrinkSelected = { selectedDrinkId.value = it },
                        selectedTabIndex = selectedTabIndex,
                        isTablet = true,
                        openDrawer = openDrawer,
                        refreshKey = refreshKey, // Pass the refresh key
                        onTabSelected = { index -> updateTabIndex(index) } // Pass the refresh key
                    )
                }
            } else {
                // Split view when drink is selected
                Box(Modifier.weight(0.5f)) {
                    DrinkTabsScreen(
                        drinkViewModel = drinkViewModel,
                        onDrinkSelected = { selectedDrinkId.value = it },
                        selectedTabIndex = selectedTabIndex,
                        isTablet = true,
                        openDrawer = openDrawer,
                        refreshKey = refreshKey, // Pass the refresh key
                        onTabSelected = { index -> updateTabIndex(index) }
                    )
                }
                Box(Modifier.weight(0.5f)) {
                    selectedDrinkId.value?.let {
                        DrinkDetailScreen(
                            viewModel = drinkViewModel,
                            drinkId = it,
                            onBack = { selectedDrinkId.value = null },
                            onSendSms = { ingredients -> sendSms(context, ingredients) },
                            phoneNumber = phoneNumber,
                            onPhoneConfirmed = {phone -> onPhoneConfirmed(phone)}

                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DrinkTabsScreen(
    drinkViewModel: DrinkViewModel,
    onDrinkSelected: (String) -> Unit,
    selectedTabIndex: MutableIntState,
    isTablet: Boolean = false,
    openDrawer: (() -> Unit)? = null,
    onTabSelected: ((Int) -> Unit)? = null, // Add this lambda
    refreshKey: MutableState<Int> // Receive the refresh key
) {
    val tabs = listOf("App Info", "Alcoholic", "Non-Alcoholic")
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex.intValue,
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    val alcoholicDrinks = drinkViewModel.alcoholicDrinks.collectAsState()
    val nonAlcoholicDrinks = drinkViewModel.nonAlcoholicDrinks.collectAsState()
    val loading = drinkViewModel.loading.collectAsState()
    val filteredDrinks = remember(searchText, pagerState.currentPage, alcoholicDrinks.value, nonAlcoholicDrinks.value) {
        when (pagerState.currentPage) {
            1 -> alcoholicDrinks.value.filter { it.strDrink?.contains(searchText, ignoreCase = true) ?: false }
            2 -> nonAlcoholicDrinks.value.filter { it.strDrink?.contains(searchText, ignoreCase = true) ?: false }
            else -> emptyList()
        }
    }
    DrinkListContent(drinkViewModel, onDrinkSelected, filteredDrinks)

    LaunchedEffect(selectedTabIndex.intValue) {
        if (selectedTabIndex.intValue != pagerState.currentPage) {
            pagerState.animateScrollToPage(selectedTabIndex.intValue)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                selectedTabIndex.intValue = page
            }
    }

    LaunchedEffect(pagerState.currentPage, refreshKey.value) {
        when (pagerState.currentPage) {
            1 -> drinkViewModel.fetchDrinks("Alcoholic")
            2 -> drinkViewModel.fetchDrinks("Non_Alcoholic")
        }
    }

    val loadingState = drinkViewModel.loading.collectAsState()
    //val drinkListState = drinkViewModel.drinkList.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DrinkBase") },
                navigationIcon = {
                    if (openDrawer != null) {
                        IconButton(onClick = { openDrawer() }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                },
                actions = {
                    if (pagerState.currentPage != 0) { // Hide on "App Info" tab
                        IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                    onTabSelected?.invoke(index)
                                }
                            },
                            text = { Text(text = title) }
                        )
                    }
                }

                if (isSearchVisible && pagerState.currentPage != 0) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search drink by name") }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> AppInfoScreen(isTablet)
                        1 -> {
                            if (loadingState.value) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                DrinkListContent(drinkViewModel, onDrinkSelected, filteredDrinks)
                            }
                        }
                        2 -> {
                            if (loadingState.value) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                DrinkListContent(drinkViewModel, onDrinkSelected, filteredDrinks)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AppInfoScreen(isTablet: Boolean) {
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
    onDrinkSelected: (String) -> Unit,
    filteredDrinks: List<DrinkSummary>
) {
    val loading by drinkViewModel.loading.collectAsState()

    if (loading && filteredDrinks.isEmpty()) {
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
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = filteredDrinks, key = { it.idDrink ?: "" }) { drink ->
                DrinkItem(
                    drink = drink,
                    onClick = { drink.idDrink?.let(onDrinkSelected) }
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
        Column (
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ){
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
                    .heightIn(min = 60.dp)
                    .weight(1f),
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
    onBack: (() -> Unit)? = null,
    onSendSms: ((List<String>) -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null ,
    phoneNumber: String,
    onPhoneConfirmed: (String) -> Unit
) {
    val drinkDetail by viewModel.selectedDrink.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(drinkId) {
        viewModel.fetchDrinkDetail(drinkId)
    }

    val timeLeft = rememberSaveable { mutableStateOf(0) }
    val isRunning = rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        drinkDetail?.let { dr ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(dr.strDrinkThumb)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxHeight(),
                                contentScale = ContentScale.Fit,
                            )
                        }
                        drinkDetail?.strDrink?.let { Text(it) }
                    }
                },
                navigationIcon = {
                    Row {
                        if (onMenuClick != null) {
                            IconButton(onClick = { onMenuClick() }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        }
                        if (onBack != null) {
                            IconButton(onClick = { onBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )

        },
        floatingActionButton = {
            if (drinkDetail != null && phoneNumber.isNotBlank()) {
                FloatingActionButton(
                    onClick = {
                    }
                ) {
                    SmsSenderScreenWithPermissions(
                        drink = drinkDetail,
                        phoneNumber = phoneNumber
                    )
                    Icon(Icons.Filled.Send, "Wyślij SMS")

                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
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

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TimerFragment(
                            timeLeft = timeLeft.value,
                            setTimeLeft = { timeLeft.value = it },
                            isRunning = isRunning.value,
                            setIsRunning = { isRunning.value = it }
                        )
                        PhoneNumberInput(initialPhoneNumber = phoneNumber, onPhoneNumberConfirmed = {number -> onPhoneConfirmed(number)})
                    }
                }
            }
        }
    }
}


@Composable
fun PhoneNumberInput(
    initialPhoneNumber: String, // Zmieniono nazwę dla jasności, że to wartość początkowa
    onPhoneNumberConfirmed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Użyj initialPhoneNumber jako klucza dla remember, aby zresetować stan,
    // jeśli wartość początkowa zmieni się z zewnątrz.
    var currentPhoneNumber by remember(initialPhoneNumber) { mutableStateOf(initialPhoneNumber) }
    var phoneNumberError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = currentPhoneNumber,
            onValueChange = { newValue ->
                currentPhoneNumber = newValue.filter { it.isDigit() }
                phoneNumberError = false // Resetuj błąd przy każdej zmianie
            },
            label = { Text("Numer telefonu") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done // <--- DODAJ TĘ LINIJKĘ
            ),
            isError = phoneNumberError,
            supportingText = {
                if (phoneNumberError) {
                    Text("Proszę wprowadzić poprawny numer telefonu (min. 9 cyfr).")
                } else {
                    Text("Wprowadź swój numer telefonu (np. 123456789)")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Opcjonalnie: najpierw zwaliduj i potwierdź, jeśli użytkownik kliknął "Done"
                    // zamiast przycisku, jeśli to pożądane zachowanie.
                    // if (currentPhoneNumber.length >= 9) {
                    //     onPhoneNumberConfirmed(currentPhoneNumber)
                    // } else {
                    //     phoneNumberError = true
                    // }
                    focusManager.clearFocus() // Schowaj klawiaturę
                }
            )
        )
        Button(
            onClick = {
                if (currentPhoneNumber.length >= 9) {
                    onPhoneNumberConfirmed(currentPhoneNumber)
                    focusManager.clearFocus() // Opcjonalnie: schowaj klawiaturę także po kliknięciu przycisku
                } else {
                    phoneNumberError = true
                }
            },
            enabled = currentPhoneNumber.length >= 9,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Potwierdź numer")
        }
    }
}



fun sendSmsDirectly(context: Context, phoneNumber: String, message: String) {

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

    try {
        val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION") // Suppress lint warning for deprecated getDefault()
            SmsManager.getDefault()
        }

        val parts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)

        Toast.makeText(context, "wiadomość: $message", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Błąd wysyłania SMS: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        e.printStackTrace() // Wypisz stos wyjątku do logcat dla debugowania
    }
}




@Composable
fun SmsSenderScreenWithPermissions(drink: DrinkDetail?, phoneNumber: String) {
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(context, "Uprawnienie do wysyłania SMS odrzucone.", Toast.LENGTH_SHORT).show()
        }
    }
    val messageToSend by remember(drink) {
        derivedStateOf {
            if (drink == null) {
                ""
            } else {
                val builder = StringBuilder()
                builder.append("Drink: ${drink.strDrink}\n")
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
                        if(deviceCanSendSms(context)) {
                            // Uprawnienie już przyznane, wyślij SMS-a bezpośrednio
                            sendSmsDirectly(context, phoneNumber, messageToSend)
                        }
                        else{
                            Toast.makeText(context, "Twoje urządzenie nie obsługuje wysyłania SMS.", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else -> {
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
    return packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
}