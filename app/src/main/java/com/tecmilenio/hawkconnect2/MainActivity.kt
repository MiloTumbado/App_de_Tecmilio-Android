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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
    studentNumber: String,
    campusId: Int?,
    onResult: (RegisterResult?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val registerRequest = RegisterRequest(
                NewUser(name, lastName, email, password, studentNumber, campusId ?: 0)
            )
            val call = getRetrofitHack().create(APIService::class.java).registerUser(registerRequest)
            if (call.isSuccessful) {
                println("Registro exitoso, respuesta: ${call.body()}")
                onResult(call.body()?.data)
            } else {
                val errorBody = call.errorBody()?.string()
                println("Error en el registro, código de error: ${call.code()}")
                println("Mensaje de error: $errorBody")
                onResult(null)
            }
        } catch (e: Exception) {
            println("Excepción en el registro: ${e.message}")
            onResult(null)
        }
    }
}

// Función para manejar el login de usuario
private fun loginUser(correo: String, contrasena: String, onResult: (LoginResult?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val loginRequest = LoginRequest(LoginData(correo, contrasena))
            val call = getRetrofitHack().create(APIService::class.java).loginUser(loginRequest)
            if (call.isSuccessful) {
                val response = call.body()
                onResult(response?.data)
            } else {
                onResult(null)
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }
}

// Composable principal de la pantalla
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var isUserRegistered by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showFriendsScreen by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var userLastName by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (!showDialog && !isUserRegistered && !showFriendsScreen) {
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
                    // Mostrar FriendsScreen cuando showFriendsScreen es true
                    showFriendsScreen -> {
                        FriendsScreen(
                            userId = 1, // Reemplaza con el ID de usuario real
                            onSaveFriends = { showFriendsScreen = false }, // Regresar a WelcomeScreen después de guardar
                            onLogOut = {
                                isUserRegistered = false
                                userName = ""
                                userLastName = ""
                                showFriendsScreen = false
                            },
                            onNavigateToWelcome = { showFriendsScreen = false } // Navegar a WelcomeScreen
                        )
                    }
                    // Mostrar WelcomeScreen cuando el usuario está registrado y no está en FriendsScreen
                    isUserRegistered -> {
                        WelcomeScreen(
                            name = userName,
                            lastName = userLastName,
                            onLogOut = {
                                isUserRegistered = false
                                userName = ""
                                userLastName = ""
                            },
                            onShowFriends = { showFriendsScreen = true }
                        )
                    }
                    // Mostrar MainContent si el usuario no ha iniciado sesión
                    else -> {
                        MainContent(
                            modifier = Modifier.fillMaxSize(),
                            onRegisterSuccess = { showDialog = true }
                        )
                    }
                }

                // Diálogo de registro
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

                // Diálogo de confirmación de registro exitoso
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






// Composable para el formulario de inicio de sesión
@Composable
fun MainContent(modifier: Modifier, onRegisterSuccess: () -> Unit) {
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
            painter = painterResource(id = R.drawable.tecmilenio_logo),
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

        TextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo electrónico") })
        TextField(value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña") })

        Button(
            onClick = {
                loginUser(correo, contrasena) { result ->
                    if (result?.executeResult == "OK") {
                        loginResult = result
                        errorMessage = null
                        onRegisterSuccess()
                    } else {
                        loginResult = null
                        errorMessage = result?.message ?: "Usuario no válido"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0a301d)),
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp)
        ) {
            Text(text = "Entrar", color = Color.White)
        }

        // Mostrar el resultado exitoso del login
        loginResult?.let { result ->
            result.userLogged?.firstOrNull()?.let { user ->
                Text(
                    text = """
                        Resultado: OK
                        Matrícula: ${user.studentNumber}
                        Nombre: ${user.name} ${user.lastName}
                        Campus: ${user.campusName}
                    """.trimIndent(),
                    color = Color.Green,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // Mostrar mensaje de error si hubo un error en el login
        errorMessage?.let {
            Text(
                text = "Resultado: ERROR\nMensaje: $it",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
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
    var selectedCampus by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Campus hardcodeados
    val hardcodedCampuses = listOf(
        Campus(campusID = 1, campusName = "Torres", isActive = true),
        Campus(campusID = 2, campusName = "Cumbres", isActive = true)
    )

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
                value = selectedCampus ?: "Seleccionar Campus (Opcional)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Campus") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth().padding(bottom = 8.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                hardcodedCampuses.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(text = item.campusName) },
                        onClick = {
                            selectedCampus = item.campusName
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
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White)
            }
            TextButton(onClick = {
                val campusId = hardcodedCampuses.find { it.campusName == selectedCampus }?.campusID ?: 0
                registerUser(
                    name = nombre,
                    lastName = apellidos,
                    email = email,
                    password = contrasena,
                    studentNumber = matricula,
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
    onShowFriends: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tec Milenio", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF0a301d)),
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("View feed") }, onClick = { /* Acción para ver el feed */ })
                        DropdownMenuItem(text = { Text("My friends") }, onClick = {
                            expanded = false
                            onShowFriends()
                        })
                        DropdownMenuItem(text = { Text("My info") }, onClick = { /* Acción para My info */ })
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
        content = { /* Resto del contenido */ }
    )
}





// Botón flotante para abrir el diálogo de registro
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

// FriendsScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userId: Int,
    onSaveFriends: () -> Unit,
    onLogOut: () -> Unit, // Nueva función para manejar el log out
    onNavigateToWelcome: () -> Unit // Función para navegar de regreso a la pantalla de bienvenida
) {
    var campusFilter by remember { mutableStateOf("") }
    var nameFilter by remember { mutableStateOf("") }
    var friendsList by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var selectedFriends by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) } // Estado del menú desplegable de la barra de herramientas

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tec Milenio", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF0a301d)),
                navigationIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú de opciones", tint = Color.White)
                    }
                },
                actions = {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View feed") },
                            onClick = { /* Acción para ver el feed */ }
                        )
                        DropdownMenuItem(
                            text = { Text("My friends") },
                            onClick = {
                                expanded = false
                                // Estamos en "My friends", por lo que no hacemos nada aquí
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .background(Color(0xFFD9F2D0))
            ) {
                Text("MY FRIENDS", fontSize = 24.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                // Campo de texto para filtrar por campus
                TextField(
                    value = campusFilter,
                    onValueChange = { campusFilter = it },
                    label = { Text("Campus") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                // Campo de texto para filtrar por nombre
                TextField(
                    value = nameFilter,
                    onValueChange = { nameFilter = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                // Botón de búsqueda
                Button(
                    onClick = {
                        loadFriends(userId, campusFilter, nameFilter) { result, error ->
                            friendsList = result ?: emptyList()
                            errorMessage = error
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Buscar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Amigos ${selectedFriends.size} de ${friendsList.size}")

                // Lista de amigos
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(friendsList) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedFriends.contains(friend.userId),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedFriends = selectedFriends + friend.userId
                                    } else {
                                        selectedFriends = selectedFriends - friend.userId
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${friend.completeName}\nStudent ID: ${friend.studentNumber}\nCampus: ${friend.campusName}")
                        }
                    }
                }

                // Botón para guardar la selección de amigos
                Button(
                    onClick = {
                        saveFriends(userId, selectedFriends) { success, error ->
                            if (success) {
                                onSaveFriends()
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                ) {
                    Text("Guardar")
                }

                // Mensaje de error
                errorMessage?.let {
                    Text("Error: $it", color = Color.Red, modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    )
}


// Función para cargar amigos desde la API
private fun loadFriends(
    userId: Int,
    campusName: String,
    name: String,
    onResult: (List<Friend>?, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Crear la solicitud con los filtros de campus y nombre
            val filterRequest = FriendFilterRequest(
                FriendsFilter(
                    loggedUserId = userId,
                    campusId = 0, // No filtramos por ID de campus, sino por nombre
                    name = name
                )
            )

            val response = getRetrofitHack().create(APIService::class.java).getFriends(filterRequest)
            if (response.isSuccessful) {
                val friends = response.body()?.data?.friends

                // Filtrar los resultados localmente por el nombre del campus si se ingresó uno
                val filteredFriends = friends?.filter { friend ->
                    campusName.isBlank() || friend.campusName.contains(campusName, ignoreCase = true)
                }

                onResult(filteredFriends, null)
            } else {
                onResult(null, response.errorBody()?.string())
            }
        } catch (e: Exception) {
            onResult(null, e.message)
        }
    }
}

// Función para guardar amigos seleccionados
private fun saveFriends(
    userId: Int,
    selectedFriends: Set<Int>,
    onResult: (Boolean, String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val friendsIds = selectedFriends.joinToString(",")
            val saveRequest = SaveFriendsRequest(FriendsList(userId, friendsIds))
            val response = getRetrofitHack().create(APIService::class.java).saveFriends(saveRequest)
            if (response.isSuccessful) {
                onResult(response.body()?.data?.executeResult == "OK", null)
            } else {
                onResult(false, response.errorBody()?.string())
            }
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }
}



