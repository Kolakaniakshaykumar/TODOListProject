package s3399241.akshay.todolistproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import s3399241.akshay.todolistproject.ui.theme.TODOListProjectTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TODOListProjectTheme {
                MyAppNavGraph()
            }
        }
    }
}

@Composable
fun MyAppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.Splash.route // Start with the splash screen
    ) {
        // 1. Splash Screen Destination
        composable(AppDestinations.Splash.route) {
            SplashScreen(navController = navController)
        }

        // 2. Login Screen Destination
        composable(AppDestinations.Login.route) {
            UserLogin(
                onLoginSuccess = {
                    // Navigate to Home and pop up to Login screen (and including it)
                    // This prevents the user from going back to Login after successful login
                    if(it==1) {
                        navController.navigate(AppDestinations.Home.route) {
                            popUpTo(AppDestinations.Login.route) {
                                inclusive = true // Remove Login from back stack
                            }
                        }
                    }else{
                        navController.navigate(AppDestinations.Register.route) {
                            popUpTo(AppDestinations.Login.route) {
                                inclusive = false // Remove Login from back stack
                            }
                        }
                    }
                }
            )
        }

        composable(AppDestinations.Register.route)
        {
            TodoListRegister(
                onActionClicked = {
                    navController.popBackStack()
                }
            )
        }

        // 3. Home Screen Destination (Dashboard)
        composable(AppDestinations.Home.route) {
            HomeScreenDesign(
                onMyTasksClick = { navController.navigate(AppDestinations.MyTasks.route) },
                onRemindersClick = { navController.navigate(AppDestinations.Reminders.route) },
                onCompletedListsClick = { navController.navigate(AppDestinations.CompletedLists.route) },
                onHistoryClick = { navController.navigate(AppDestinations.History.route) },
                onQuickAddTaskClick = { navController.navigate(AppDestinations.AddTask.route) },
                onSettingsClick = { navController.navigate(AppDestinations.Settings.route) }
            )
        }

        // 4. My Tasks Screen
        composable(AppDestinations.MyTasks.route) {
            MyTasksScreen(navController = navController)
        }

        // 5. Reminders Screen
        composable(AppDestinations.Reminders.route) {
            RemindersScreen(navController = navController)
        }

        // 6. Completed Lists Screen
        composable(AppDestinations.CompletedLists.route) {
            CompletedListsScreen(navController = navController)
        }

        // 7. History Screen
        composable(AppDestinations.History.route) {
            HistoryScreen(navController = navController)
        }

        // 8. Add Task Screen
        composable(AppDestinations.AddTask.route) {
            AddTaskScreen(navController = navController)
        }

        // 9. Settings Screen
        composable(AppDestinations.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}

// --- Placeholder Composables for other screens ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("My Tasks Content Here")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("Reminders Content Here")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedListsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completed Lists") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("Completed Lists Content Here")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("History Content Here")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Task") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("Add Task Form Here")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .wrapContentSize(Alignment.Center)
        ) {
            Text("Settings Content Here")
        }
    }
}

// --- SplashScreen (from previous response) ---
@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000) // Delay for 3 seconds
        navController.navigate(AppDestinations.Login.route) {
            popUpTo(AppDestinations.Splash.route) { inclusive = true }
        }
    }
    StartUpView() // Your actual splash screen UI
}


//Old Code

@Composable
fun StartUpViewScreen() {
    var canSplash by remember { mutableStateOf(true) }
    val context = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        delay(3000)
        canSplash = false
    }

    if (canSplash) {
        StartUpView()

    } else {
        context.startActivity(Intent(context, SignInActivity::class.java))
        context.finish()
    }
}

@Composable
fun StartUpView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Green),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.Transparent),
                )
                {
                    Image(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        painter = painterResource(id = R.drawable.logo_todolist),
                        contentDescription = "ToDo List App",
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "ToDO List App\nby Akshay",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64A70B), // Green color similar to the design
                        fontSize = 26.sp,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                }
            }


        }
    }

}


@Preview(showBackground = true)
@Composable
fun StartUpViewPreview() {
    StartUpView()
}