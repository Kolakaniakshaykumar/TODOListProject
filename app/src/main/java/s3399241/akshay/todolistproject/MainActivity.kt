package s3399241.akshay.todolistproject

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import s3399241.akshay.todolistproject.goal.GoalScreen
import s3399241.akshay.todolistproject.reminders.NotificationHelper
import s3399241.akshay.todolistproject.reminders.RemindersScreen
import s3399241.akshay.todolistproject.roomdb.AppViewModelFactory
import s3399241.akshay.todolistproject.roomdb.GoalViewModel
import s3399241.akshay.todolistproject.roomdb.ReminderViewModel
import s3399241.akshay.todolistproject.roomdb.TaskViewModel
import s3399241.akshay.todolistproject.tasks.AddTaskScreen
import s3399241.akshay.todolistproject.tasks.CompletedListsScreen
import s3399241.akshay.todolistproject.tasks.MyTasksScreen
import s3399241.akshay.todolistproject.ui.theme.TODOListProjectTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(applicationContext)

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
    val context = LocalContext.current.applicationContext as Application
    val viewModelFactory = remember { AppViewModelFactory(context) }

    val taskViewModel: TaskViewModel = viewModel(factory = viewModelFactory)
    val goalViewModel: GoalViewModel = viewModel(factory = viewModelFactory)
    val reminderViewModel: ReminderViewModel = viewModel(factory = viewModelFactory)


    NavHost(
        navController = navController,
        startDestination = AppDestinations.Splash.route
    ) {
        composable(AppDestinations.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(AppDestinations.Login.route) {
            UserLogin(
                onLoginSuccess = {
                    if (it == 1) {
                        navController.navigate(AppDestinations.Home.route) {
                            popUpTo(AppDestinations.Login.route) {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate(AppDestinations.Register.route) {
                            popUpTo(AppDestinations.Login.route) {
                                inclusive = false
                            }
                        }
                    }
                }
            )
        }

        composable(AppDestinations.Register.route) {
            TodoListRegister(
                onActionClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.Home.route) {
            HomeScreenDesign(
                taskViewModel = taskViewModel,
                reminderViewModel = reminderViewModel,
                onMyTasksClick = { navController.navigate(AppDestinations.MyTasks.route) },
                onRemindersClick = { navController.navigate(AppDestinations.Reminders.route) },
                onCompletedListsClick = { navController.navigate(AppDestinations.CompletedLists.route) },
                onHistoryClick = { navController.navigate(AppDestinations.History.route) },
                onQuickAddTaskClick = { navController.navigate(AppDestinations.AddTask.route) },
                onSettingsClick = { navController.navigate(AppDestinations.Settings.route) }
            )
        }

        composable(AppDestinations.MyTasks.route) {
            MyTasksScreen(taskViewModel = taskViewModel, navController = navController)
        }

        composable(AppDestinations.Reminders.route) {
            RemindersScreen(reminderViewModel = reminderViewModel, navController = navController)
        }

        composable(AppDestinations.CompletedLists.route) {
            CompletedListsScreen(taskViewModel = taskViewModel, navController = navController)
        }

        composable(AppDestinations.History.route) {
            GoalScreen(goalViewModel = goalViewModel, navController = navController)
        }

        composable(AppDestinations.AddTask.route) {
            AddTaskScreen(taskViewModel = taskViewModel, navController = navController)
        }

        composable(AppDestinations.Settings.route) {
            ProfileScreen(navController = navController, onLogout = {
                UserDetails.saveUserLoginStatus(context, false)
                navController.navigate(AppDestinations.Login.route) {
                   popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            })
        }
    }
}

@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        delay(2000)

        val UserStatus = UserDetails.getUserLoginStatus(context)

        if (UserStatus) {
            navController.navigate(AppDestinations.Home.route) {
                popUpTo(AppDestinations.Splash.route) {
                    inclusive = true
                }
            }
        } else {
            navController.navigate(AppDestinations.Login.route) {
                popUpTo(AppDestinations.Splash.route) {
                    inclusive = true
                }
            }
        }

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Green),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_todolist),
            contentDescription = "TODO Logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Todo List App", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Organize Your Life", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

sealed class AppDestinations(val route: String) {
    object Splash : AppDestinations("splash")
    object Login : AppDestinations("login")
    object Register : AppDestinations("register")
    object Home : AppDestinations("home")
    object MyTasks : AppDestinations("myTasks")
    object Reminders : AppDestinations("reminders")
    object CompletedLists : AppDestinations("completedLists")
    object History : AppDestinations("history")
    object AddTask : AppDestinations("addTask")
    object Settings : AppDestinations("settings")
}

