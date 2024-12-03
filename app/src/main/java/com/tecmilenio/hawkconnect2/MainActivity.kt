package com.tecmilenio.hawkconnect2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tecmilenio.hawkconnect2.ui.theme.HawkConnect2Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HawkConnect2Theme {
                MainScreen()
            }
        }
    }
}

// Configuración de Retrofit
private fun getRetrofitHack(): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://hawkconnect.azurewebsites.net/users.aspx/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

// Función para registrar un usuario
private fun registerUser(
    name: String,
    lastName: String,
    email: String,
    password: String,
    studentNumber: String, // Cambiado a String
    campusId: Int?,
    onResult: (RegisterResult?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val registerRequest = RegisterRequest(
                datos = NewUser(
                    name = name,
                    lastName = lastName,
                    email = email,
                    password = password,
                    studentNumber = studentNumber, // Ahora es String
                    campusId = campusId ?: 0
                )
            )
            println("Enviando solicitud de registro: $registerRequest")
            val response = RetrofitInstance.api.registerUser(registerRequest)
            if (response.isSuccessful) {
                println("Registro exitoso, respuesta: ${response.body()}")
                withContext(Dispatchers.Main) {
                    onResult(response.body()?.data)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("Error en el registro, código de error: ${response.code()}")
                println("Mensaje de error: $errorBody")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        } catch (e: Exception) {
            println("Excepción en el registro: ${e.message}")
            withContext(Dispatchers.Main) {
                onResult(null)
            }
        }
    }
}


// Función para manejar el login de usuario
private fun loginUser(correo: String, contrasena: String, onResult: (LoginResult?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val loginRequest = LoginRequest(LoginData(correo, contrasena))
            val response = RetrofitInstance.api.loginUser(loginRequest)
            if (response.isSuccessful) {
                val responseBody = response.body()
                println("Respuesta de la API de login: $responseBody") // Agrega esta línea
                if (responseBody?.data?.executeResult == "OK") {
                    withContext(Dispatchers.Main) {
                        onResult(responseBody.data)
                    }
                } else {
                    println("Error de login: ${responseBody?.data?.message ?: "Respuesta no válida"}")
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("Error en la solicitud de login, código de error: ${response.code()}")
                println("Mensaje de error: $errorBody")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        } catch (e: Exception) {
            println("Excepción durante el login: ${e.message}")
            withContext(Dispatchers.Main) {
                onResult(null)
            }
        }
    }
}

// Composable principal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var isUserRegistered by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf<Int?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showFriendsScreen by remember { mutableStateOf(false) }
    var showFeedScreen by remember { mutableStateOf(false) }
    var showViewPostsScreen by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var selectedFriendIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var userName by remember { mutableStateOf("") }
    var userLastName by remember { mutableStateOf("") }
    // Agrega otras variables necesarias, como el userId obtenido después del login

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (!showDialog && !isUserRegistered && !showFriendsScreen && !showFeedScreen) {
                FloatingAddButton(onClick = { showDialog = true })
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    showViewPostsScreen -> {
                        ViewPostsScreen(
                            friendIds = selectedFriendIds,
                            onBack = { showViewPostsScreen = false }
                        )
                    }
                    showFeedScreen -> {
                        userId?.let { id ->
                            FeedScreen(
                                userId = id, // Usa 'id' aquí
                                onBack = { showFeedScreen = false }
                            )
                        } ?: run {
                            // Manejar el caso cuando userId es null
                            println("Error: userId es null. Por favor, inicia sesión nuevamente.")
                            isUserRegistered = false
                            showFeedScreen = false
                        }
                    }


                    showFriendsScreen -> {
                        userId?.let { id ->
                            println("Navegando a FriendsScreen con userId: $id")
                            FriendsScreen(
                                userId = id,
                                onBack = { showFriendsScreen = false },
                                onViewPosts = { friendIds ->
                                    selectedFriendIds = friendIds
                                    showViewPostsScreen = true
                                }
                            )
                        } ?: run {
                            println("Error: userId es null. Por favor, inicia sesión nuevamente.")
                            isUserRegistered = false
                            showFriendsScreen = false
                        }
                    }

                    isUserRegistered -> {
                        WelcomeScreen(
                            name = userName,
                            lastName = userLastName,
                            onLogOut = {
                                isUserRegistered = false
                                userName = ""
                                userLastName = ""
                                userId = null // Restablecer userId al cerrar sesión
                            },
                            onShowFriends = { showFriendsScreen = true },
                            onViewFeed = { showFeedScreen = true }
                        )
                    }
                    else -> {

                        MainContent(
                            modifier = Modifier.fillMaxSize(),
                            onRegisterSuccess = { showDialog = true },
                            onLoginSuccess = { id, name, lastName ->
                                isUserRegistered = true
                                userName = name
                                userLastName = lastName
                                userId = id // Almacena el userId
                                println("userId almacenado en MainScreen: $userId")
                            }

                        )

                    }
                }

                // Manejo de diálogos de registro y confirmación
                if (showDialog) {
                    SignUpDialog(
                        onDismiss = { showDialog = false },
                        onRegisterSuccess = { name, lastName ->
                            showDialog = false
                            userName = name
                            userLastName = lastName
                            showConfirmationDialog = true
                        }
                    )
                }

                if (showConfirmationDialog) {
                    ConfirmationDialog(
                        onDismiss = {
                            showConfirmationDialog = false
                            isUserRegistered = true
                        }
                    )
                }
            }
        }
    )
}


// Botón flotante
@Composable
fun FloatingAddButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF0a301d),
        contentColor = Color.White,
        modifier = Modifier.padding(16.dp)
    ) {
        Text("+", fontSize = 24.sp)
    }
}

// Composable para el formulario de inicio de sesión
@Composable
fun MainContent(
    modifier: Modifier,
    onRegisterSuccess: () -> Unit,
    onLoginSuccess: (Int, String, String) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var loginResult by remember { mutableStateOf<LoginResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFD9F2D0))
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.tecmilenio1),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = "Hawk Connect",
            color = Color.Black,
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Campos de entrada para el correo y la contraseña
        TextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo electrónico") })
        TextField(value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña") })

        // Botón para iniciar sesión
        Button(
            onClick = {
                loginUser(correo, contrasena) { result ->
                    if (result?.executeResult == "OK") {
                        val user = result.userLogged?.firstOrNull()
                        if (user != null) {
                            // Redirige al usuario a la pantalla de bienvenida con nombre y apellido
                            onLoginSuccess(user.userId, user.name, user.lastName)
                            println("Inicio de sesión exitoso. userId: ${user.userId}")

                        }
                        errorMessage = null
                    } else {
                        loginResult = null
                        errorMessage = result?.message ?: "Usuario no válido"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0a301d)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
        ) {
            Text(text = "Entrar", color = Color.White)
        }

        // Mostrar mensaje de error si hubo un error en el login
        errorMessage?.let {
            Text(
                text = "Resultado: ERROR\nMensaje: $it",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        // Al final de MainContent, antes de cerrar la columna
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = { onRegisterSuccess() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "¿No tienes una cuenta? Regístrate aquí")
        }

    }
}

// Composable para el diálogo de confirmación de registro
@Composable
fun ConfirmationDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Usuario registrado") },
        text = { Text("El usuario ha sido registrado exitosamente.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}

// Composable para el diálogo de registro
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpDialog(
    onDismiss: () -> Unit,
    onRegisterSuccess: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCampus by remember { mutableStateOf<Campus?>(null) }
    var campuses by remember { mutableStateOf<List<Campus>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar los campus
    LaunchedEffect(Unit) {
        try {
            val campusResponse = RetrofitInstance.api.getCampuses()
            if (campusResponse.isSuccessful) {
                campuses = campusResponse.body()?.campuses ?: emptyList()
            } else {
                errorMessage = "Error al cargar campus: ${campusResponse.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }


    Column(
        modifier = Modifier
            .background(Color(0xFF00A499), shape = MaterialTheme.shapes.medium)
            .padding(20.dp)
            .wrapContentSize()
    ) {
        Text(text = "Crear Nueva Cuenta", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Menú desplegable para seleccionar campus
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCampus?.campusName ?: "Seleccionar Campus",
                onValueChange = {},
                readOnly = true,
                label = { Text("Campus") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                campuses.forEach { campus ->
                    DropdownMenuItem(
                        text = { Text(text = campus.campusName) },
                        onClick = {
                            selectedCampus = campus
                            expanded = false
                        }
                    )
                }
            }
        }

        // Otros campos del formulario
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
        TextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") })
        TextField(value = matricula, onValueChange = { matricula = it }, label = { Text("Matrícula") })
        TextField(value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña") })

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                val campusId = selectedCampus?.campusID ?: 0
                val studentNumber = matricula // Ahora es String
                if (studentNumber.isBlank()) {
                    errorMessage = "El número de matrícula no puede estar vacío"
                    return@TextButton
                }
                registerUser(
                    name = nombre,
                    lastName = apellidos,
                    email = email,
                    password = contrasena,
                    studentNumber = studentNumber, // Pasamos la String
                    campusId = campusId
                ) { result ->
                    if (result?.executeResult == "OK") {
                        onRegisterSuccess(nombre, apellidos)
                    } else {
                        errorMessage = result?.message ?: "Usuario no válido"
                    }
                }
            }) {
                Text("Crear", color = Color.White)
            }

        }

        errorMessage?.let {
            Text("Error: $it", color = Color.Red)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    name: String,
    lastName: String,
    onLogOut: () -> Unit,
    onShowFriends: () -> Unit,
    onViewFeed: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Tec Milenio", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF0a301d)),
                navigationIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú de opciones",
                            tint = Color.White
                        )
                    }

                    // Asociar el DropdownMenu directamente con el IconButton
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("View feed") },
                            onClick = {
                                expanded = false
                                onViewFeed()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("My friends") },
                            onClick = {
                                expanded = false
                                onShowFriends()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("My info") },
                            onClick = { /* Acción para My info */ }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Log out") },
                            onClick = {
                                expanded = false
                                onLogOut()
                            }
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFD9F2D0))
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Hola", fontSize = 24.sp, color = Color.Black)
                    Text(text = "$name $lastName", fontSize = 20.sp, color = Color.Black)
                }
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userId: Int,
    onBack: () -> Unit,
    onViewPosts: (List<Int>) -> Unit
) {
    println("FriendsScreen iniciado con userId: $userId")
    // Variables de estado
    var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var campuses by remember { mutableStateOf<List<Campus>>(emptyList()) }
    var selectedCampus by remember { mutableStateOf<Campus?>(null) }
    var nameFilter by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFriends by remember { mutableStateOf(setOf<Int>()) }
    var expanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // **Definición de la función saveSelectedFriends**
    fun saveSelectedFriends() {
        println("saveSelectedFriends llamado. userId: $userId")
        println("Amigos seleccionados: $selectedFriends")
        coroutineScope.launch {
            try {
                val friendsIds = selectedFriends.joinToString(",")
                val saveRequest = SaveFriendsRequest(FriendsList(userId, friendsIds))
                println("Enviando solicitud para guardar amigos: $saveRequest")
                val response = RetrofitInstance.api.saveFriends(saveRequest)
                println("Respuesta recibida: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    println("Cuerpo de la respuesta: $responseBody")
                    val executeResult = responseBody?.data?.executeResult
                    val message = responseBody?.data?.message
                    println("executeResult: $executeResult")
                    println("Mensaje: $message")
                    if (executeResult == "OK") {
                        withContext(Dispatchers.Main) {
                            showSuccessDialog = true
                            errorMessage = null
                            println("Amigos guardados con éxito. showSuccessDialog = $showSuccessDialog")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = message ?: "Error desconocido"
                            println("Error al guardar amigos: $errorMessage")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error al guardar amigos: ${response.code()} - $errorBody"
                        println("Error en la respuesta: $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Excepción: ${e.message}"
                    println("Excepción al guardar amigos: ${e.message}")
                }
            }
        }
    }


    // **Función para cargar amigos**
    fun loadFriends() {
        coroutineScope.launch {
            try {
                val campusId = selectedCampus?.campusID ?: 0
                val name = nameFilter.trim()
                val friendsResponse = RetrofitInstance.api.getFriends(
                    FriendFilterRequest(
                        FriendsFilter(
                            loggedUserId = userId,
                            campusId = campusId,
                            name = name
                        )
                    )
                )
                if (friendsResponse.isSuccessful) {
                    val responseData = friendsResponse.body()?.data
                    if (responseData?.executeResult == "OK") {
                        friends = responseData.friends ?: emptyList()
                        errorMessage = null
                    } else {
                        errorMessage = "Error: ${responseData?.message ?: "Desconocido"}"
                    }
                } else {
                    errorMessage = "Error al cargar amigos: ${friendsResponse.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            }
        }
    }

    // Cargar campuses y amigos al iniciar la pantalla
    LaunchedEffect(Unit) {
        // Cargar campuses
        try {
            val campusResponse = RetrofitInstance.api.getCampuses()
            if (campusResponse.isSuccessful) {
                campuses = campusResponse.body()?.campuses ?: emptyList()
                errorMessage = null
            } else {
                errorMessage = "Error al cargar campuses: ${campusResponse.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }

        // Cargar amigos
        loadFriends()
    }

    // Contenido de la UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF0a301d)),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFD9F2D0))
                    .padding(16.dp)
            ) {
                // Selector de campus
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedCampus?.campusName ?: "Seleccionar Campus",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Campus") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        campuses.forEach { campus ->
                            DropdownMenuItem(
                                text = { Text(text = campus.campusName) },
                                onClick = {
                                    selectedCampus = campus
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Campo de búsqueda por nombre
                TextField(
                    value = nameFilter,
                    onValueChange = { nameFilter = it },
                    label = { Text("Buscar por nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Botones: Buscar, Save, View Posts
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        // Acción del botón "Buscar"
                        loadFriends()
                    }) {
                        Text("Buscar")
                    }

                    Button(onClick = {
                        // Acción del botón "Save"
                        if (selectedFriends.isEmpty()) {
                            errorMessage = "No hay amigos seleccionados para guardar"
                            return@Button
                        }
                        saveSelectedFriends() // **Asegúrate de que esta llamada esté presente**
                    }) {
                        Text("Save")
                    }

                    Button(onClick = {
                        // Acción del botón "View Posts"
                        if (selectedFriends.isEmpty()) {
                            errorMessage = "No hay amigos seleccionados para ver posts"
                            return@Button
                        }
                        onViewPosts(selectedFriends.toList())
                    }) {
                        Text("View Posts")
                    }
                }

                // Lista de amigos
                if (friends.isEmpty()) {
                    Text("No hay amigos para mostrar", modifier = Modifier.padding(8.dp))
                } else {
                    LazyColumn {
                        items(friends) { friend ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedFriends.contains(friend.userId),
                                    onCheckedChange = { isChecked ->
                                        selectedFriends = if (isChecked) {
                                            selectedFriends + friend.userId
                                        } else {
                                            selectedFriends - friend.userId
                                        }
                                    }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(
                                        text = friend.completeName ?: "Nombre desconocido",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Campus: ${friend.campusName ?: "Desconocido"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // Mensaje de error
                errorMessage?.let {
                    Text(text = it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    )

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Amigos guardados con éxito") },
            text = { Text("Los amigos seleccionados se han guardado correctamente.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPostsScreen(friendIds: List<Int>, onBack: () -> Unit) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cargar posts de amigos seleccionados
    LaunchedEffect(friendIds) {
        posts = emptyList() // Limpia la lista antes de cargar
        try {
            val allPosts = mutableListOf<Post>()
            for (friendId in friendIds) {
                val response = RetrofitInstance.api.getPosts(PostFilterRequest(PostFilter(userId = friendId)))
                if (response.isSuccessful) {
                    response.body()?.data?.posts?.let { allPosts.addAll(it) }
                } else {
                    errorMessage = "Error al cargar posts para el usuario $friendId: ${response.code()}"
                }
            }
            posts = allPosts
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Posts de Amigos", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF0a301d)),
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFD9F2D0))
                    .padding(innerPadding)
            ) {
                if (posts.isEmpty()) {
                    Text("No hay publicaciones", modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn {
                        items(posts) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = post.completeName ?: "Nombre desconocido",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = post.content ?: "Contenido no disponible")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${post.campusName ?: "Campus desconocido"} - ${post.timeStamp ?: ""}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    userId: Int,
    onBack: () -> Unit
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Función para cargar publicaciones
    fun loadPosts() {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.getPosts(PostFilterRequest(PostFilter(userId)))
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val postsList = responseBody?.data?.posts
                    posts = postsList ?: emptyList() // Maneja la lista de posts
                } else {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error al cargar publicaciones: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Excepción: ${e.message}"
                }
            }
        }
    }

    // Carga de publicaciones cuando `userId` cambia
    LaunchedEffect(userId) {
        loadPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tec Milenio - FEED", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF0a301d)),
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePostDialog = true },
                containerColor = Color(0xFF0a301d),
                contentColor = Color.White
            ) {
                Text("+", fontSize = 24.sp)
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFD9F2D0))
            ) {
                if (posts.isEmpty()) {
                    Text("No hay publicaciones", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn {
                        items(posts) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = post.completeName ?: "Nombre desconocido",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = post.content ?: "Contenido no disponible")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${post.campusName ?: "Campus desconocido"} - ${post.timeStamp ?: ""}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                errorMessage?.let {
                    Text(text = it, color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            }
        }
    )

    // Muestra el diálogo de creación de posts
    if (showCreatePostDialog) {
        CreatePostDialog(
            userId = userId,
            onDismiss = { showCreatePostDialog = false },
            onPostCreated = {
                showCreatePostDialog = false
                loadPosts()
            }
        )
    }
}

@Composable
fun CreatePostDialog(
    userId: Int,
    onDismiss: () -> Unit,
    onPostCreated: () -> Unit
) {
    var postContent by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Post") },
        text = {
            Column {
                TextField(
                    value = postContent,
                    onValueChange = { postContent = it },
                    label = { Text("Escribe algo...") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Text(text = it, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (postContent.isNotBlank()) {
                    // Hacer la llamada para crear el post
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val request = NewPostRequest(
                                post = PostData(
                                    userId = userId,
                                    message = postContent,
                                    operationType = "CREATE"
                                )
                            )
                            val response = RetrofitInstance.api.createPost(request)
                            val responseBody = response.body()

                            if (response.isSuccessful && responseBody != null && responseBody.data.executeResult == "OK") {
                                withContext(Dispatchers.Main) {
                                    onPostCreated() // Llama a la función de éxito
                                }
                            } else if (responseBody != null) {
                                val errorMessageFromServer = responseBody.data.message
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Error del servidor: $errorMessageFromServer"
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Error de respuesta: ${response.code()} - ${errorBody ?: "Sin detalles"}"
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Excepción: ${e.message}"
                            }
                        }
                    }
                } else {
                    errorMessage = "El contenido no puede estar vacío"
                }
            }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}