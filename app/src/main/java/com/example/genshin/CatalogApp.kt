package com.example.genshin

import API.CharacterDetail
import API.GenshinViewModel
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.genshin.data.local.entity.User
import com.example.genshin.viewmodel.AuthState
import com.example.genshin.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogApp(
    genshinViewModel: GenshinViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var currentScreen by remember { mutableStateOf("main") }
    var selectedCharacter by remember { mutableStateOf<CharacterDetail?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Re-fetch character list when authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            genshinViewModel.fetchCharactersList()
        }
    }

    when (authState) {
        is AuthState.Loading -> {
            SplashScreen()
        }
        is AuthState.Unauthenticated -> {
            if (currentScreen == "register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        currentScreen = "login"
                    },
                    onLoginClick = { currentScreen = "login" },
                    authViewModel = authViewModel
                )
            } else {
                LoginScreen(
                    onLoginSuccess = {
                        // Handled by AuthViewModel state
                    },
                    onRegisterClick = { currentScreen = "register" },
                    authViewModel = authViewModel
                )
            }
        }
        is AuthState.Authenticated -> {
            when (currentScreen) {
                "detail" -> {
                    CharacterDetailScreen(
                        character = selectedCharacter,
                        onBack = { currentScreen = "main" }
                    )
                }
                else -> {
                    Scaffold(
                        modifier = Modifier.background(Color(0xFFFAF1EC)),
                        bottomBar = {
                            NavigationBar(containerColor = Color.White) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Catalogue") },
                                    label = { Text("Catalogue") },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(0xFFF6F9E4),
                                        selectedIconColor = Color(0xFF3D402B),
                                        selectedTextColor = Color(0xFF3D402B)
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Setting") },
                                    label = { Text("Setting") },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(0xFFF6F9E4),
                                        selectedIconColor = Color(0xFF3D402B),
                                        selectedTextColor = Color(0xFF3D402B)
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                0 -> CatalogueScreen(
                                    viewModel = genshinViewModel,
                                    authViewModel = authViewModel,
                                    onViewDetail = { character ->
                                        selectedCharacter = character
                                        currentScreen = "detail"
                                    }
                                )
                                1 -> SettingScreen(
                                    isLoggedIn = true,
                                    onLoginClick = { /* Already logged in */ },
                                    onLogoutClick = {
                                        authViewModel.logout()
                                        currentScreen = "login"
                                    },
                                    onAboutClick = { showAboutDialog = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF1EC)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = R.drawable.splash,
                contentDescription = "Genshin Splash",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Color(0xFFC7B468))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(character: CharacterDetail?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Character Detail", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFC7B468))
            )
        }
    ) { padding ->
        if (character != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFFAF1EC))
            ) {
                // Image Header
                AsyncImage(
                    model = "https://genshin.jmp.blue/characters/${character.id}/portrait",
                    contentDescription = character.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xFFF6F9E4).copy(alpha = 0.1f)),
                    contentScale = ContentScale.Fit
                )

                AsyncImage(
                    model = "https://genshin.jmp.blue/characters/${character.id}/constellation-shape",
                    contentDescription = character.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFF6F9E4).copy(alpha = 0.1f)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info Table Layout
                DetailRow(label = "Character Name", value = character.name)
                DetailRow(label = "Vision", value = character.vision)
                DetailRow(label = "Weapon", value = character.weapon)
                DetailRow(label = "Nation", value = character.nation)
                DetailRow(label = "Description", value = character.description, isMultiLine = true)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isMultiLine: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = if (isMultiLine) Alignment.Top else Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(0.4f),
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = value,
                modifier = Modifier.weight(0.6f),
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = if (isMultiLine) 20.sp else TextUnit.Unspecified
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFC7B468))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAF1EC))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.login(email, password) { success, message ->
                        if (success) {
                            onLoginSuccess()
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6F9E4), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Login", modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text("Don't have an account? ")
                Text(
                    text = "Register",
                    color = Color(0xFF5A5F3A),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onLoginClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFC7B468))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAF1EC))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.register(User(name = name, email = email, password = password)) { success, message ->
                        if (success) {
                            onRegisterSuccess()
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6F9E4), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Register", modifier = Modifier.padding(vertical = 8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text("Already have an account? ")
                Text(
                    text = "Login",
                    color = Color(0xFF5A5F3A),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}

enum class SortOrder {
    AtoZ, ZtoA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueScreen(
    viewModel: GenshinViewModel,
    authViewModel: AuthViewModel,
    onViewDetail: (CharacterDetail) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val products = viewModel.charactersList
    
    var showFavoritesSheet by remember { mutableStateOf(false) }
    var showHiddenSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var sortOrder by remember { mutableStateOf(SortOrder.AtoZ) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var showUserInfoDialog by remember { mutableStateOf(false) }

    val user by authViewModel.currentUser.collectAsState(initial = null)
    val hiddenChars by viewModel.hiddenCharacters.collectAsState(emptyList())
    val hiddenIds = remember(hiddenChars) { hiddenChars.map { it.characterId }.toSet() }

    val sortedProducts = remember(products, searchQuery, sortOrder, hiddenIds) {
        val filtered = products.filter { 
            it.name.contains(searchQuery, ignoreCase = true) && !hiddenIds.contains(it.id) 
        }
        when (sortOrder) {
            SortOrder.AtoZ -> filtered.sortedBy { it.name }
            SortOrder.ZtoA -> filtered.sortedByDescending { it.name }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Genshin Wiki Menu",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A5F3A)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                
                NavigationDrawerItem(
                    label = { Text("Profile Info") },
                    selected = false,
                    onClick = {
                        showUserInfoDialog = true
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF5A5F3A),
                        unselectedTextColor = Color(0xFF5A5F3A)
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Hidden Characters") },
                    selected = false,
                    onClick = {
                        showHiddenSheet = true
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF5A5F3A),
                        unselectedTextColor = Color(0xFF5A5F3A)
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Exit Application") },
                    selected = false,
                    onClick = {
                        (context as? android.app.Activity)?.finish()
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = Color(0xFF5A5F3A),
                        unselectedTextColor = Color(0xFF5A5F3A)
                    )
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFFFAF1EC),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("GENSHIN WIKI", color = Color.Black, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { 
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Black)
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { isSortMenuExpanded = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = Color.Black)
                            }
                            DropdownMenu(
                                expanded = isSortMenuExpanded,
                                onDismissRequest = { isSortMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Name: A-Z") },
                                    onClick = {
                                        sortOrder = SortOrder.AtoZ
                                        isSortMenuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Name: Z-A") },
                                    onClick = {
                                        sortOrder = SortOrder.ZtoA
                                        isSortMenuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFC7B468)
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showFavoritesSheet = true },
                    containerColor = Color(0xFFF6F9E4),
                    contentColor = Color(0xFF3D402B),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = "Favorite")
                }
            }
        ) { padding ->
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFF6F9E4))
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search Character") },
                        trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Recent Added Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Genshin Characters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color(0xFF5A5F3A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.GridView, contentDescription = null, tint = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Product List
                    if (sortedProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No characters found", color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.fetchCharactersList() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(sortedProducts) { character ->
                                CharacterItem(
                                    character = character,
                                    viewModel = viewModel,
                                    onViewDetail = { onViewDetail(character) }
                                )
                            }
                        }
                    }
                }
            }

            if (showFavoritesSheet) {
                val favorites by viewModel.bookmarkedCharacters.collectAsState(emptyList())
                ModalBottomSheet(
                    onDismissRequest = { showFavoritesSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White
                ) {
                    FavoritesMenu(
                        favoriteCharacters = favorites.filter { !hiddenIds.contains(it.catalogId) }.map { bookmark ->
                            CharacterDetail(
                                id = bookmark.catalogId,
                                name = bookmark.title,
                                description = "Bookmarked"
                            )
                        },
                        viewModel = viewModel,
                        onViewDetail = {
                            onViewDetail(it)
                            showFavoritesSheet = false
                        }
                    )
                }
            }

            if (showHiddenSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showHiddenSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White
                ) {
                    HiddenMenu(
                        hiddenCharacters = products.filter { hiddenIds.contains(it.id) },
                        viewModel = viewModel,
                        onViewDetail = {
                            onViewDetail(it)
                            showHiddenSheet = false
                        }
                    )
                }
            }
        }
    }

    if (showUserInfoDialog && user != null) {
        UserInfoDialog(user = user!!, onDismiss = { showUserInfoDialog = false })
    }
}

@Composable
fun UserInfoDialog(user: User, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFC7B468))
            }
        },
        title = {
            Text(text = "User Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF5A5F3A)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF5A5F3A)
                        )
                        Text(
                            text = "Authenticated User",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                DetailRow(label = "Name", value = user.name)
                DetailRow(label = "Email", value = user.email)
                DetailRow(label = "User ID", value = "#${user.id}")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun FavoritesMenu(
    favoriteCharacters: List<CharacterDetail>,
    viewModel: GenshinViewModel,
    onViewDetail: (CharacterDetail) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "Your Favorites",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (favoriteCharacters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No favorites yet", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favoriteCharacters) { character ->
                    CharacterItem(
                        character = character,
                        viewModel = viewModel,
                        onViewDetail = { onViewDetail(character) }
                    )
                }
            }
        }
    }
}

@Composable
fun HiddenMenu(
    hiddenCharacters: List<CharacterDetail>,
    viewModel: GenshinViewModel,
    onViewDetail: (CharacterDetail) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "Hidden Characters",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (hiddenCharacters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hidden characters", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(hiddenCharacters) { character ->
                    CharacterItem(
                        character = character,
                        viewModel = viewModel,
                        onViewDetail = { onViewDetail(character) }
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterItem(
    character: CharacterDetail,
    viewModel: GenshinViewModel,
    onViewDetail: () -> Unit
) {
    val isFavorite by viewModel.isCharacterBookmarked(character.id).collectAsState(false)
    val isHidden by viewModel.isCharacterHidden(character.id).collectAsState(false)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character Icon from API
                AsyncImage(
                    model = "https://genshin.jmp.blue/characters/${character.id}/icon-big",
                    contentDescription = character.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = character.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            if (!isHidden) {
                                IconButton(onClick = { viewModel.toggleBookmark(character) }) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (isFavorite) Color.Red else Color(0xFF5A5F3A)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = if (character.vision.isNotEmpty()) "Vision: ${character.vision}" else "Product Catalogue",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onViewDetail,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6F9E4), contentColor = Color(0xFF3D402B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Catalogue")
                }
                Button(
                    onClick = { viewModel.toggleHide(character.id) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAF1EC), contentColor = Color(0xFF5A5F3A))
                ) {
                    Text(if (isHidden) "Unhide" else "Hide")
                }
            }
        }
    }
}

@Composable
fun SettingScreen(
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF1EC))
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingItem(
            title = if (isLoggedIn) "Logout" else "Login",
            icon = if (isLoggedIn) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
            onClick = { 
                if (isLoggedIn) onLogoutClick() else onLoginClick()
            }
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Transparent)

        SettingItem(
            title = "About",
            icon = Icons.Default.Info,
            onClick = onAboutClick
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Transparent)

        SettingItem(
            title = "Help & Support",
            icon = Icons.AutoMirrored.Filled.Help,
            onClick = { }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Transparent)

        SettingItem(
            title = "Privacy Policy",
            icon = Icons.Default.PrivacyTip,
            onClick = { }
        )
    }
}

@Composable
fun SettingItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF5A5F3A))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFC7B468))
            }
        },
        title = {
            Text(text = "About Application", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Genshin Wiki App",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF5A5F3A)
                )
                Text(
                    text = "Version 1.0.0",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "A comprehensive database for Genshin Impact characters, featuring detailed information about their visions, weapons, nations, and lore.",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Developer Info:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(text = "Name: Fadhel Ibnu Yupardi", fontSize = 14.sp)
                Text(text = "Email: fadhelibnuyupardi20@gmail.com", fontSize = 14.sp)
                Text(text = "GitHub: github.com/fdhl457", fontSize = 14.sp)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
