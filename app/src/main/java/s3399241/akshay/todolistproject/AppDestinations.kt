package s3399241.akshay.todolistproject

// AppDestinations.kt

sealed class AppDestinations(val route: String) {
    object Splash : AppDestinations("splash_route")
    object Login : AppDestinations("login_route")
    object Register : AppDestinations("register_route")
    object Home : AppDestinations("home_route")
    object MyTasks : AppDestinations("my_tasks_route")
    object Reminders : AppDestinations("reminders_route")
    object CompletedLists : AppDestinations("completed_lists_route")
    object History : AppDestinations("history_route")
    object AddTask : AppDestinations("add_task_route") // For the quick add task
    object Settings : AppDestinations("settings_route") // For settings
}