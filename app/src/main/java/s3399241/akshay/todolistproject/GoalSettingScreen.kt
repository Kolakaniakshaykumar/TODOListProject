package s3399241.akshay.todolistproject

class GoalSettingScreen {
    MaterialTheme { // Use MaterialTheme for proper preview styling
        AddTaskScreen(
            onAddTask = { title, description ->
                // This is a preview, so we just print to console
                println("Task Added: Title - $title, Description - $description")
            },
            onBack = {
                println("Navigating back")
            }
        )
    }
}