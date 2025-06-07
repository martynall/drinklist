package com.example.drinklist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.drinklist.model.DrinkSummary
import com.example.drinklist.model.SplashScreen
import com.example.drinklist.ui.theme.DrinkListTheme
import com.example.drinklist.viewmodel.DrinkViewModel
import com.example.drinklist.viewmodel.DrinkViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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
                openDrawer = { coroutineScope.launch { drawerState.open() } }
            )
        } else {
            PhoneLayout(
                drinkViewModel = drinkViewModel,
                selectedDrinkId = selectedDrinkId,
                selectedTabIndex = selectedTabIndex,
                openDrawer = { coroutineScope.launch { drawerState.open() } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletLayout(
    drinkViewModel: DrinkViewModel,
    selectedDrinkId: MutableState<String?>,
    selectedTabIndex: MutableIntState,
    openDrawer: () -> Unit
) {
    val context = LocalContext.current

    Scaffold() { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (selectedDrinkId.value == null) {
                // Full screen tabs when no drink is selected
                DrinkTabsScreen(
                    drinkViewModel = drinkViewModel,
                    onDrinkSelected = { selectedDrinkId.value = it },
                    selectedTabIndex = selectedTabIndex,
                    isTablet = true,
                    openDrawer = openDrawer
                )
            } else {
                // Split view when drink is selected
                Box(Modifier.weight(0.5f)) {
                    DrinkTabsScreen(
                        drinkViewModel = drinkViewModel,
                        onDrinkSelected = { selectedDrinkId.value = it },
                        selectedTabIndex = selectedTabIndex,
                        isTablet = true,
                        openDrawer = openDrawer
                    )
                }
                Box(Modifier.weight(0.5f)) {
                    selectedDrinkId.value?.let {
                        DrinkDetailScreen(
                            viewModel = drinkViewModel,
                            drinkId = it,
                            onBack = { selectedDrinkId.value = null },
                            onSendSms = { ingredients -> sendSms(context, ingredients) },
                            onMenuClick = openDrawer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLayout(
    drinkViewModel: DrinkViewModel,
    selectedDrinkId: MutableState<String?>,
    selectedTabIndex: MutableIntState,
    openDrawer: () -> Unit
) {
    var currentScreen by rememberSaveable { mutableStateOf("tabs") }
    val context = LocalContext.current

    Scaffold(
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (currentScreen == "tabs") {
                DrinkTabsScreen(
                    drinkViewModel = drinkViewModel,
                    onDrinkSelected = {
                        selectedDrinkId.value = it
                        currentScreen = "detail"
                    },
                    selectedTabIndex = selectedTabIndex,
                    openDrawer = openDrawer
                )
            } else {
                selectedDrinkId.value?.let {
                    DrinkDetailScreen(
                        viewModel = drinkViewModel,
                        drinkId = it,
                        onBack = { currentScreen = "tabs" },
                        onSendSms = { ingredients -> sendSms(context, ingredients) },
                        onMenuClick = openDrawer
                    )
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
    openDrawer: (() -> Unit)? = null
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

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                selectedTabIndex.intValue = page
            }
    }

    LaunchedEffect(selectedTabIndex.intValue) {
        if (selectedTabIndex.intValue != pagerState.currentPage) {
            pagerState.animateScrollToPage(selectedTabIndex.intValue)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val currentPage = pagerState.currentPage
        if (currentPage > 0) {
            drinkViewModel.fetchDrinksIfNeeded(
                when (currentPage) {
                    1 -> "Alcoholic"
                    2 -> "Non_Alcoholic"
                    else -> null
                }
            )
        }
    }

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
                            val drinkListState = drinkViewModel.drinkList.collectAsState()
                            val filteredDrinks = remember(searchText, drinkListState.value) {
                                drinkListState.value.filter {
                                    it.strDrink?.contains(searchText, ignoreCase = true) ?: false
                                }
                            }
                            DrinkListContent(drinkViewModel, onDrinkSelected, filteredDrinks)
                        }
                        2 -> {
                            val drinkListState = drinkViewModel.drinkList.collectAsState()
                            val filteredDrinks = remember(searchText, drinkListState.value) {
                                drinkListState.value.filter {
                                    it.strDrink?.contains(searchText, ignoreCase = true) ?: false
                                }
                            }
                            DrinkListContent(drinkViewModel, onDrinkSelected,filteredDrinks)
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
fun DrinkListScreen(
    drinkViewModel: DrinkViewModel,
    onDrinkSelected: (String) -> Unit,
    filter: String? = null
) {
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
            contentPadding = PaddingValues(15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
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
    onMenuClick: (() -> Unit)? = null // Add this parameter
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
            if (onSendSms != null && drinkDetail != null) {
                SmallFloatingActionButton(
                    onClick = {
                        val ingredients = listOfNotNull(
                            drinkDetail?.strIngredient1,
                            drinkDetail?.strIngredient2,
                            drinkDetail?.strIngredient3,
                            drinkDetail?.strIngredient4,
                            drinkDetail?.strIngredient5,
                            drinkDetail?.strIngredient6,
                            drinkDetail?.strIngredient7,
                            drinkDetail?.strIngredient8,
                            drinkDetail?.strIngredient9,
                            drinkDetail?.strIngredient10,
                            drinkDetail?.strIngredient11,
                            drinkDetail?.strIngredient12,
                            drinkDetail?.strIngredient13,
                            drinkDetail?.strIngredient14,
                            drinkDetail?.strIngredient15,
                        )
                        onSendSms(ingredients)
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.AutoMirrored.Filled.Message, "Wyślij SMS ze składnikami")
                }
            }
        }
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
                    }
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