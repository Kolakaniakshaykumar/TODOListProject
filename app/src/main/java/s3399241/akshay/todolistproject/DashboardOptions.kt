package s3399241.akshay.todolistproject


import android.app.Activity
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import s3399241.akshay.todolistproject.ui.theme.TODOListProjectTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//region 1. Data Layer (Room Database)

// In your existing Task.kt or where Task data class is defined
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: TaskPriority = TaskPriority.LOW,
    val dueDate: Long? = null, // New: Optional due date/time
    val reminderTime: Long? = null // New: Optional reminder time
)


@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val type: GoalType, // WEEKLY, MONTHLY
    val targetPeriod: String, // e.g., "Week 30, 2025" or "July 2025"
    val currentProgress: Int = 0, // 0 to 100
    val isCompleted: Boolean = false, // Can be derived if currentProgress == 100
    val createdAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val reminderTime: Long, // Timestamp for the reminder
    val isDismissed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// 1.2. DAOs (Data Access Objects)
@Dao
interface TaskDao {
    // Get all tasks, ordered by creation date descending
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    // Search tasks by title or description
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchTasks(query: String): Flow<List<Task>>

    // Insert a new task or replace if it exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    // Update an existing task
    @Update
    suspend fun updateTask(task: Task)

    // Delete a task
    @Delete
    suspend fun deleteTask(task: Task)
}

@Dao
interface GoalDao {
    // Get all goals, ordered by creation date descending
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<Goal>>

    // Insert a new goal or replace if it exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    // Update an existing goal
    @Update
    suspend fun updateGoal(goal: Goal)

    // Delete a goal
    @Delete
    suspend fun deleteGoal(goal: Goal)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY reminderTime ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}

// 1.3. Database
@Database(entities = [Task::class, Goal::class, Reminder::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Register the type converters
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun goalDao(): GoalDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Get the singleton instance of the database
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_database" // Database file name
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 1.4. Type Converters for Room (for enums)
class Converters {
    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toTaskPriority(name: String): TaskPriority {
        return TaskPriority.valueOf(name)
    }

    @TypeConverter
    fun fromGoalType(type: GoalType): String {
        return type.name
    }

    @TypeConverter
    fun toGoalType(name: String): GoalType {
        return GoalType.valueOf(name)
    }
}

// 1.5. Repositories
class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun update(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }

    fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(query)
    }
}

class GoalRepository(private val goalDao: GoalDao) {
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()

    suspend fun insert(goal: Goal) {
        goalDao.insertGoal(goal)
    }

    suspend fun update(goal: Goal) {
        goalDao.updateGoal(goal)
    }

    suspend fun delete(goal: Goal) {
        goalDao.deleteGoal(goal)
    }
}

class ReminderRepository(private val reminderDao: ReminderDao) {
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun insert(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun update(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun delete(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }
}

//endregion

//region 2. ViewModel Layer

// 2.1. Enums for Sorting and Filtering
enum class SortOrder {
    NONE, DATE_ASC, DATE_DESC, PRIORITY_HIGH_TO_LOW, PRIORITY_LOW_TO_HIGH
}

enum class FilterStatus {
    ALL, COMPLETED, PENDING
}

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class GoalType {
    WEEKLY, MONTHLY
}

// 2.2. ViewModels
class TaskViewModel(application: Application, private val taskRepository: TaskRepository) :
    AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _filterStatus = MutableStateFlow(FilterStatus.ALL)

    // Combine flows to get the final list of tasks for the UI
    val tasks: StateFlow<List<Task>> = combine(
        taskRepository.allTasks,
        _searchQuery,
        _sortOrder,
        _filterStatus
    ) { allTasks, query, sortOrder, filterStatus ->
        var filteredList = if (query.isNotBlank()) {
            allTasks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        } else {
            allTasks
        }

        filteredList = when (filterStatus) {
            FilterStatus.COMPLETED -> filteredList.filter { it.isCompleted }
            FilterStatus.PENDING -> filteredList.filter { !it.isCompleted }
            else -> filteredList
        }

        when (sortOrder) {
            SortOrder.DATE_ASC -> filteredList.sortedBy { it.createdAt }
            SortOrder.DATE_DESC -> filteredList.sortedByDescending { it.createdAt }
            SortOrder.PRIORITY_HIGH_TO_LOW -> filteredList.sortedWith(compareByDescending<Task> { it.priority.ordinal }.thenByDescending { it.createdAt })
            SortOrder.PRIORITY_LOW_TO_HIGH -> filteredList.sortedWith(compareBy<Task> { it.priority.ordinal }.thenByDescending { it.createdAt })
            SortOrder.NONE -> filteredList // Default or no specific sort
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    val filterStatus: StateFlow<FilterStatus> = _filterStatus.asStateFlow()

    fun addTask(
        title: String,
        description: String,
        priority: TaskPriority,
        dueDate: Long?,
        reminderTime: Long?
    ) {
        viewModelScope.launch {
            val newTask = Task(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate,
                reminderTime = reminderTime
            )
            taskRepository.insert(newTask)

            // Schedule notification if reminderTime is set
//            reminderTime?.let {
//                // Pass the task ID after insertion (if available, or pass details)
//                // For simplicity, we'll pass the details directly.
//                // In a real app, you might want to retrieve the ID after insertion
//                // and then schedule using that ID for easy cancellation.
//                // For now, we'll use a unique ID based on current time.
//                val notificationId = System.currentTimeMillis().toInt()
//                NotificationScheduler.scheduleNotification(
//                    getApplication(),
//                    notificationId,
//                    newTask.title,
//                    newTask.description,
//                    it
//                )
//            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.update(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task)
        }
    }

    fun markTaskAsDone(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.update(task.copy(isCompleted = isCompleted))
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderChanged(order: SortOrder) {
        _sortOrder.value = order
    }

    fun onFilterStatusChanged(status: FilterStatus) {
        _filterStatus.value = status
    }
}

class GoalViewModel(application: Application, private val goalRepository: GoalRepository) :
    AndroidViewModel(application) {
    val goals: StateFlow<List<Goal>> = goalRepository.allGoals.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Updated addGoal signature
    fun addGoal(description: String, type: GoalType, targetPeriod: String) {
        viewModelScope.launch {
            // Initial progress is 0, isCompleted is false
            goalRepository.insert(
                Goal(
                    description = description,
                    type = type,
                    targetPeriod = targetPeriod,
                    currentProgress = 0,
                    isCompleted = false
                )
            )
        }
    }

    // New function to update goal progress
    fun updateGoalProgress(goal: Goal, newProgress: Int) {
        viewModelScope.launch {
            val clampedProgress =
                newProgress.coerceIn(0, 100) // Ensure progress is between 0 and 100
            val updatedGoal = goal.copy(
                currentProgress = clampedProgress,
                isCompleted = clampedProgress >= 100
            )
            goalRepository.update(updatedGoal)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.delete(goal)
        }
    }

    // This function is now less critical as completion is tied to progress,
    // but kept for explicit marking if needed.
    fun markGoalAsDone(goal: Goal, isCompleted: Boolean) {
        viewModelScope.launch {
            val progress = if (isCompleted) 100 else goal.currentProgress
            goalRepository.update(goal.copy(isCompleted = isCompleted, currentProgress = progress))
        }
    }
}

class ReminderViewModel(
    application: Application,
    private val reminderRepository: ReminderRepository
) : AndroidViewModel(application) {
    val reminders: StateFlow<List<Reminder>> = reminderRepository.allReminders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addReminder(title: String, description: String, reminderTime: Long) {
        viewModelScope.launch {
            reminderRepository.insert(
                Reminder(
                    title = title,
                    description = description,
                    reminderTime = reminderTime
                )
            )
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.update(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.delete(reminder)
        }
    }

    fun dismissReminder(reminder: Reminder, isDismissed: Boolean) {
        viewModelScope.launch {
            reminderRepository.update(reminder.copy(isDismissed = isDismissed))
        }
    }
}

// 2.3. ViewModel Factory for injecting repositories
class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDatabase.getDatabase(application)
        return when {
            modelClass.isAssignableFrom(TaskViewModel::class.java) -> {
                val repository = TaskRepository(database.taskDao())
                @Suppress("UNCHECKED_CAST")
                TaskViewModel(application, repository) as T
            }

            modelClass.isAssignableFrom(GoalViewModel::class.java) -> {
                val repository = GoalRepository(database.goalDao())
                @Suppress("UNCHECKED_CAST")
                GoalViewModel(application, repository) as T
            }

            modelClass.isAssignableFrom(ReminderViewModel::class.java) -> {
                val repository = ReminderRepository(database.reminderDao())
                @Suppress("UNCHECKED_CAST")
                ReminderViewModel(application, repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

//endregion

//region 3. UI Layer (Jetpack Compose)

// 3.1. App Destinations
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

// 3.2. MainActivity (Your existing code)
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            TODOListProjectTheme {
//                MyAppNavGraph()
//            }
//        }
//    }
//}

// 3.3. MyAppNavGraph (Updated to include ViewModels)
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
                taskViewModel = taskViewModel, // Pass taskViewModel
                reminderViewModel = reminderViewModel, // Pass reminderViewModel
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
//            HistoryScreen(navController = navController)

            GoalScreen(goalViewModel = goalViewModel, navController = navController)
        }

        composable(AppDestinations.AddTask.route) {
            AddTaskScreen(taskViewModel = taskViewModel, navController = navController)
        }

        composable(AppDestinations.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}

// 3.4. HomeScreenDesign (Your existing code, with minor adjustments for icons/colors if needed)
@OptIn(ExperimentalMaterial3Api::class) // This OptIn is required for M3 FloatingActionButton, Scaffold, TopAppBar
@Composable
fun HomeScreenDesign(
    taskViewModel: TaskViewModel, // Added TaskViewModel parameter
    reminderViewModel: ReminderViewModel, // Added ReminderViewModel parameter
    onMyTasksClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onCompletedListsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onQuickAddTaskClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    // Collect data from ViewModels
    val allTasks by taskViewModel.tasks.collectAsState()
    val allReminders by reminderViewModel.reminders.collectAsState()

    // Calculate actual counts
    val pendingTasksCount = remember(allTasks) {
        allTasks.count { !it.isCompleted }
    }
    val tasksDueTodayCount = remember(allTasks) {
        // This assumes "due today" means pending tasks with a dueDate that falls on today.
        // If your Task entity has a 'dueDate: Long' field, this is how you'd filter.
        allTasks.count { !it.isCompleted && it.dueDate != null && isToday(it.dueDate) }
    }
    val upcomingRemindersCount = remember(allReminders) {
        allReminders.count { !it.isDismissed && it.reminderTime > System.currentTimeMillis() }
    }
    val completedTasksTodayCount = remember(allTasks) {
        allTasks.count { it.isCompleted && isToday(it.createdAt) }
    }


    // Assuming these colors are defined in your R.color
    val primaryDark = colorResource(id = R.color.PrimaryDark)
    val buttonColor = colorResource(id = R.color.button_color)
    val textOnPrimaryDark = colorResource(id = R.color.text_on_primary_dark)
    val textOnButton = colorResource(id = R.color.text_on_button)
    val cardBackgroundLight = colorResource(id = R.color.card_background_light)

    Scaffold( // Now using Material 3 Scaffold
        topBar = {
            androidx.compose.material3.TopAppBar( // Using Material 3 TopAppBar
                title = {
                    Text(
                        "Dashboard",
                        color = textOnPrimaryDark,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryDark
                ),
                actions = {
                    // Search Icon (can navigate to MyTasksScreen with search activated)
                    IconButton(onClick = onMyTasksClick) { // Using Material 3 IconButton
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = textOnPrimaryDark
                        )
                    }
                    // Settings Icon
                    IconButton(onClick = onSettingsClick) { // Using Material 3 IconButton
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = textOnPrimaryDark
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton( // Now using Material 3 FloatingActionButton
                onClick = onQuickAddTaskClick,
                containerColor = buttonColor, // Use containerColor for Material 3 FAB
                contentColor = textOnButton,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Add, "Add new task") // Using Material 3 Icon
            }
        },
        containerColor = Color.White // Use containerColor for Material 3 Scaffold background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text( // Material 3 Text
                text = "Hello, User!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth()
            )
            Text( // Material 3 Text
                text = "Here's your overview for today.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryCard(
                    title = "Pending Tasks",
                    count = pendingTasksCount,
                    icon = Icons.Default.Info,
                    iconTint = buttonColor,
                    onClick = onMyTasksClick
                )
                SummaryCard(
                    title = "Due Today",
                    count = tasksDueTodayCount,
                    icon = Icons.Default.Info,
                    iconTint = Color(0xFFFFA726),
                    onClick = onRemindersClick
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryCard(
                    title = "Completed Today",
                    count = completedTasksTodayCount,
                    icon = Icons.Default.Info,
                    iconTint = Color(0xFF66BB6A),
                    onClick = onCompletedListsClick
                )
                SummaryCard(
                    title = "Total Reminders",
                    count = upcomingRemindersCount,
                    icon = Icons.Default.Notifications,
                    iconTint = buttonColor,
                    onClick = onRemindersClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DashboardSectionCard(
                title = "My Tasks",
                description = "View and manage all your pending tasks.",
                icon = Icons.Default.Info,
                onClick = onMyTasksClick,
                backgroundColor = primaryDark,
                contentColor = textOnPrimaryDark
            )
            DashboardSectionCard(
                title = "Reminders",
                description = "See all your scheduled reminders and deadlines.",
                icon = Icons.Default.Info,
                onClick = onRemindersClick,
                backgroundColor = cardBackgroundLight,
                contentColor = Color.DarkGray
            )
            DashboardSectionCard(
                title = "Completed Lists",
                description = "Review your achievements and finished tasks.",
                icon = Icons.Default.CheckCircle,
                onClick = onCompletedListsClick,
                backgroundColor = primaryDark,
                contentColor = textOnPrimaryDark
            )
            DashboardSectionCard(
                title = "Goals",
                description = "Set Monthly and Weekly Goals.",
                icon = Icons.Default.Info,
                onClick = onHistoryClick,
                backgroundColor = cardBackgroundLight,
                contentColor = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Helper function to check if a timestamp falls on the current day
private fun isToday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
    val cal2 = Calendar.getInstance() // Current time

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun RowScope.SummaryCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card( // Using Material 3 Card
        modifier = Modifier
            .weight(1f)
            .height(120.dp)
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Material 3 CardDefaults
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Material 3 CardDefaults
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
        }
    }
}

@Composable
fun DashboardSectionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color
) {
    androidx.compose.material3.Card( // Using Material 3 Card
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor), // Material 3 CardDefaults
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Material 3 CardDefaults
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(40.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 3.5. MyTasksScreen (Previously TaskListScreen, renamed and adapted)

@OptIn(ExperimentalMaterial3Api::class) // This OptIn is now required for M3 Scaffold, TopAppBar, FloatingActionButton, OutlinedTextField, AlertDialog, Button, RadioButton, Checkbox, SwipeToDismissBox
@Composable
fun MyTasksScreen(taskViewModel: TaskViewModel, navController: NavController) {
    val tasks by taskViewModel.tasks.collectAsState()
    val searchQuery by taskViewModel.searchQuery.collectAsState()
    val sortOrder by taskViewModel.sortOrder.collectAsState()
    val filterStatus by taskViewModel.filterStatus.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var showSortFilterDialog by remember { mutableStateOf(false) }

    Scaffold( // Material 3 Scaffold
        topBar = {
            androidx.compose.material3.TopAppBar( // Using Material 3 TopAppBar
                title = { Text("My Tasks", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Material 3 IconButton
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White) // Material 3 Icon
                    }
                },
                actions = {
                    // Search Icon
                    IconButton(onClick = {
                        showSearchBar = !showSearchBar
                    }) { // Material 3 IconButton
                        Icon(Icons.Filled.Search, "Search", tint = Color.White) // Material 3 Icon
                    }
                    // Sort/Filter Icon
                    IconButton(onClick = { showSortFilterDialog = true }) { // Material 3 IconButton
                        Icon(
                            Icons.Filled.Info,
                            "Sort and Filter",
                            tint = Color.White
                        ) // Material 3 Icon
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(AppDestinations.AddTask.route) }) { // Material 3 FloatingActionButton
                Icon(Icons.Filled.Add, "Add new task") // Material 3 Icon
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background // Set Scaffold background color
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Animated Search Bar
            AnimatedVisibility(
                visible = showSearchBar,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField( // Material 3 OutlinedTextField
                    value = searchQuery,
                    onValueChange = { taskViewModel.onSearchQueryChanged(it) },
                    label = { Text("Search tasks...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { taskViewModel.onSearchQueryChanged("") }) { // Material 3 IconButton
                                Icon(Icons.Filled.Clear, "Clear search") // Material 3 Icon
                            }
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors( // Material 3 TextFieldDefaults
                        focusedBorderColor = colorResource(id = R.color.button_color),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = colorResource(id = R.color.button_color)
                    )
                )
            }

            // Sort and Filter Dialog
            if (showSortFilterDialog) {
                AlertDialog( // Material 3 AlertDialog
                    onDismissRequest = { showSortFilterDialog = false },
                    title = { Text("Sort & Filter Options") }, // Material 3 Text
                    text = {
                        Column {
                            Text("Sort By:", fontWeight = FontWeight.Bold) // Material 3 Text
                            Column(Modifier.padding(start = 8.dp)) {
                                SortOrder.entries.forEach { order ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton( // Material 3 RadioButton
                                            selected = sortOrder == order,
                                            onClick = { taskViewModel.onSortOrderChanged(order) },
                                            colors = RadioButtonDefaults.colors( // Material 3 RadioButtonDefaults
                                                selectedColor = colorResource(id = R.color.button_color)
                                            )
                                        )
                                        Text(
                                            order.name.replace("_", " ").lowercase()
                                                .replaceFirstChar { it.uppercase() }) // Material 3 Text
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Filter By Status:",
                                fontWeight = FontWeight.Bold
                            ) // Material 3 Text
                            Column(Modifier.padding(start = 8.dp)) {
                                FilterStatus.entries.forEach { status ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton( // Material 3 RadioButton
                                            selected = filterStatus == status,
                                            onClick = { taskViewModel.onFilterStatusChanged(status) },
                                            colors = RadioButtonDefaults.colors( // Material 3 RadioButtonDefaults
                                                selectedColor = colorResource(id = R.color.button_color)
                                            )
                                        )
                                        Text(
                                            status.name.lowercase()
                                                .replaceFirstChar { it.uppercase() }) // Material 3 Text
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showSortFilterDialog = false }) { // Material 3 Button
                            Text("Done") // Material 3 Text
                        }
                    }
                )
            }

            if (tasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No tasks",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    ) // Material 3 Icon
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No tasks found. Add a new one!",
                        color = Color.Gray,
                        fontSize = 18.sp
                    ) // Material 3 Text
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        // TaskItem is already updated to use Material 3 components
                        TaskItem(task = task,
                            onToggleComplete = { isChecked ->
                                taskViewModel.markTaskAsDone(
                                    task,
                                    isChecked
                                )
                            },
                            onDelete = { taskViewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // This annotation is necessary for using SwipeToDismissBox
@Composable
fun TaskItem(task: Task, onToggleComplete: (Boolean) -> Unit, onDelete: () -> Unit) {
    // Use rememberSwipeToDismissBoxState from Material 3
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            // Check if the dismissal value indicates a full swipe
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                onDelete() // Call the delete action
                true // Confirm that the dismiss action should proceed
            } else {
                false // Do not dismiss
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true, // Allows swiping from left to right
        enableDismissFromEndToStart = true,   // Allows swiping from right to left
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.Settled -> Color.LightGray // Item is in its default, settled position
                SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f) // Swiping from right to left
                SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f) // Swiping from left to right
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Checkbox( // Material 3 Checkbox
                        checked = task.isCompleted,
                        onCheckedChange = onToggleComplete,
                        colors = CheckboxDefaults.colors( // Material 3 CheckboxDefaults
                            checkedColor = colorResource(id = R.color.button_color),
                            uncheckedColor = Color.Gray
                        )
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = task.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            color = if (task.isCompleted) Color.Gray else Color.Black
                        )
                        if (task.description.isNotBlank()) {
                            Text(
                                text = task.description,
                                fontSize = 14.sp,
                                color = if (task.isCompleted) Color.LightGray else Color.DarkGray,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                            )
                        }
                        // Display Due Date
                        task.dueDate?.let {
                            Text(
                                text = "Due: ${
                                    SimpleDateFormat(
                                        "MMM dd, yyyy HH:mm",
                                        Locale.getDefault()
                                    ).format(Date(it))
                                }",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                        Text(
                            text = "Priority: ${
                                task.priority.name.lowercase().replaceFirstChar { it.uppercase() }
                            }",
                            fontSize = 12.sp,
                            color = when (task.priority) {
                                TaskPriority.HIGH -> Color.Red
                                TaskPriority.MEDIUM -> Color.Blue
                                TaskPriority.LOW -> Color.Green
                            }
                        )
                        Text(
                            text = "Created: ${
                                SimpleDateFormat(
                                    "MMM dd, yyyy",
                                    Locale.getDefault()
                                ).format(Date(task.createdAt))
                            }",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    // Show Reminder Icon if reminderTime is set
                    if (task.reminderTime != null) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Reminder Set",
                            tint = colorResource(id = R.color.button_color),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    )

}

// 3.6. AddTaskScreen (Updated to use TaskViewModel)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    taskViewModel: TaskViewModel,
    navController: NavController
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.LOW) }

    // State for Due Date and Time
    var selectedDueDate: Calendar? by remember { mutableStateOf(null) } // Calendar object for combined date/time
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // State for Reminder
    var isReminderEnabled by remember { mutableStateOf(false) }
    var selectedReminderDate: Calendar? by remember { mutableStateOf(null) } // Separate calendar for reminder
    var showReminderDatePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }


    val context = LocalContext.current

    // Date Picker Dialog
    if (showDatePicker) {
        val year = selectedDueDate?.get(Calendar.YEAR) ?: Calendar.getInstance().get(Calendar.YEAR)
        val month =
            selectedDueDate?.get(Calendar.MONTH) ?: Calendar.getInstance().get(Calendar.MONTH)
        val day = selectedDueDate?.get(Calendar.DAY_OF_MONTH) ?: Calendar.getInstance()
            .get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                selectedDueDate = (selectedDueDate ?: Calendar.getInstance()).apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                showDatePicker = false
                showTimePicker = true // Automatically open time picker after date
            },
            year, month, day
        ).show()
    }

    // Time Picker Dialog for Due Date
    if (showTimePicker) {
        val hour = selectedDueDate?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance()
            .get(Calendar.HOUR_OF_DAY)
        val minute =
            selectedDueDate?.get(Calendar.MINUTE) ?: Calendar.getInstance().get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                selectedDueDate = (selectedDueDate ?: Calendar.getInstance()).apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                showTimePicker = false
            },
            hour, minute, false // 24-hour format
        ).show()
    }

    // Reminder Date Picker Dialog
    if (showReminderDatePicker) {
        val year =
            selectedReminderDate?.get(Calendar.YEAR) ?: Calendar.getInstance().get(Calendar.YEAR)
        val month =
            selectedReminderDate?.get(Calendar.MONTH) ?: Calendar.getInstance().get(Calendar.MONTH)
        val day = selectedReminderDate?.get(Calendar.DAY_OF_MONTH) ?: Calendar.getInstance()
            .get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                selectedReminderDate = (selectedReminderDate ?: Calendar.getInstance()).apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                showReminderDatePicker = false
                showReminderTimePicker = true // Automatically open time picker after date
            },
            year, month, day
        ).show()
    }

    // Reminder Time Picker Dialog
    if (showReminderTimePicker) {
        val hour = selectedReminderDate?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance()
            .get(Calendar.HOUR_OF_DAY)
        val minute = selectedReminderDate?.get(Calendar.MINUTE) ?: Calendar.getInstance()
            .get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                selectedReminderDate = (selectedReminderDate ?: Calendar.getInstance()).apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                showReminderTimePicker = false
            },
            hour, minute, false // 24-hour format
        ).show()
    }


    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar( // Using Material 3 TopAppBar
                title = { Text("Add New Task", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What's on your mind?",
                fontSize = 24.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.button_color),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = colorResource(id = R.color.button_color)
                )
            )

            OutlinedTextField(
                value = taskDescription,
                onValueChange = { taskDescription = it },
                label = { Text("Task Description (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(id = R.color.button_color),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = colorResource(id = R.color.button_color)
                )
            )

            // Priority Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("Priority:", fontWeight = FontWeight.SemiBold)
                TaskPriority.entries.forEach { priority ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            colors = RadioButtonDefaults.colors(selectedColor = colorResource(id = R.color.button_color))
                        )
                        Text(priority.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }

            // Due Date and Time Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Due Date:", fontWeight = FontWeight.SemiBold)
                Button(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    Spacer(Modifier.width(8.dp))
                    Text(selectedDueDate?.let {
                        SimpleDateFormat(
                            "MMM dd, yyyy HH:mm",
                            Locale.getDefault()
                        ).format(it.time)
                    } ?: "Select Date & Time")
                }
            }

            // Set Reminder Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Set Reminder:", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = {
                        isReminderEnabled = it
                        if (it && selectedReminderDate == null) {
                            // If reminder enabled and no date set, default to due date or now
                            selectedReminderDate = selectedDueDate ?: Calendar.getInstance()
                        }
                    },
                    colors = SwitchDefaults.colors( // Material 3 SwitchDefaults
                        checkedThumbColor = colorResource(id = R.color.button_color),
                        checkedTrackColor = colorResource(id = R.color.button_color).copy(alpha = 0.5f)
                    )
                )
            }

            // Reminder Date and Time Selection (only if enabled)
            if (isReminderEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Reminder Time:", fontWeight = FontWeight.SemiBold)
                    Button(onClick = { showReminderDatePicker = true }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Select Reminder Time"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(selectedReminderDate?.let {
                            SimpleDateFormat(
                                "MMM dd, yyyy HH:mm",
                                Locale.getDefault()
                            ).format(it.time)
                        } ?: "Select Reminder Time")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        taskViewModel.addTask(
                            taskTitle,
                            taskDescription,
                            selectedPriority,
                            selectedDueDate?.timeInMillis,
                            if (isReminderEnabled) selectedReminderDate?.timeInMillis else null
                        )
                        navController.popBackStack() // Go back after adding task
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
//                    backgroundColor = colorResource(id = R.color.button_color) // Material 2 Button color
                )
            ) {
                Text("Add Task", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Required for M3 Scaffold, TopAppBar, FloatingActionButton, AlertDialog, Button, OutlinedTextField
@Composable
fun RemindersScreen(reminderViewModel: ReminderViewModel, navController: NavController) {
    val reminders by reminderViewModel.reminders.collectAsState()
    var showAddReminderDialog by remember { mutableStateOf(false) }

    Scaffold( // Material 3 Scaffold
        topBar = {
            androidx.compose.material3.TopAppBar( // Material 3 TopAppBar
                title = { Text("My Reminders", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Material 3 IconButton
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White) // Material 3 Icon
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showAddReminderDialog = true
            }) { // Material 3 FloatingActionButton
                Icon(Icons.Filled.Info, "Add new reminder") // Material 3 Icon
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background // Scaffold background color
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (reminders.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No reminders",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    ) // Material 3 Icon
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No reminders set. Add one!",
                        color = Color.Gray,
                        fontSize = 18.sp
                    ) // Material 3 Text
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onDismiss = { reminderViewModel.dismissReminder(reminder, true) },
                            onDelete = { reminderViewModel.deleteReminder(reminder) }
                        )
                    }
                }
            }
        }
    }

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onAddReminder = { title, description, time ->
                reminderViewModel.addReminder(title, description, time)
                showAddReminderDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Required for SwipeToDismissBox, Card, Icon, IconButton
@Composable
fun ReminderItem(reminder: Reminder, onDismiss: () -> Unit, onDelete: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState( // Material 3 rememberSwipeToDismissBoxState
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                onDelete() // Or onDismiss() depending on desired swipe action
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox( // Material 3 SwipeToDismissBox
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.Settled -> Color.LightGray
                SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                ) // Material 3 Icon
            }
        },
        content = {
            Card( // Material 3 Card
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Material 3 CardDefaults
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White) // Material 3 CardDefaults
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text( // Material 3 Text
                            text = reminder.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isDismissed) Color.Gray else Color.Black,
                            textDecoration = if (reminder.isDismissed) TextDecoration.LineThrough else null
                        )
                        if (reminder.description.isNotBlank()) {
                            Text( // Material 3 Text
                                text = reminder.description,
                                fontSize = 14.sp,
                                color = if (reminder.isDismissed) Color.LightGray else Color.DarkGray,
                                textDecoration = if (reminder.isDismissed) TextDecoration.LineThrough else null
                            )
                        }
                        Text( // Material 3 Text
                            text = "Due: ${
                                SimpleDateFormat(
                                    "MMM dd, yyyy HH:mm",
                                    Locale.getDefault()
                                ).format(Date(reminder.reminderTime))
                            }",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    if (!reminder.isDismissed) {
                        IconButton(onClick = onDismiss) { // Material 3 IconButton
                            Icon(
                                Icons.Default.Info,
                                "Dismiss Reminder",
                                tint = colorResource(id = R.color.button_color)
                            ) // Material 3 Icon
                        }
                    } else {
                        Icon(Icons.Default.Done, "Dismissed", tint = Color.Gray) // Material 3 Icon
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class) // Required for AlertDialog, Button, OutlinedTextField
@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onAddReminder: (String, String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDateTime: Calendar? by remember { mutableStateOf(null) } // Combined date and time
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Date Picker Dialog
    if (showDatePicker) {
        val year = selectedDateTime?.get(Calendar.YEAR) ?: Calendar.getInstance().get(Calendar.YEAR)
        val month =
            selectedDateTime?.get(Calendar.MONTH) ?: Calendar.getInstance().get(Calendar.MONTH)
        val day = selectedDateTime?.get(Calendar.DAY_OF_MONTH) ?: Calendar.getInstance()
            .get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                selectedDateTime = (selectedDateTime ?: Calendar.getInstance()).apply {
                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                }
                showDatePicker = false
                showTimePicker = true // Automatically open time picker after date
            },
            year, month, day
        ).show()
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val hour = selectedDateTime?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance()
            .get(Calendar.HOUR_OF_DAY)
        val minute =
            selectedDateTime?.get(Calendar.MINUTE) ?: Calendar.getInstance().get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                selectedDateTime = (selectedDateTime ?: Calendar.getInstance()).apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                showTimePicker = false
            },
            hour, minute, false // 24-hour format
        ).show()
    }

    AlertDialog( // Material 3 AlertDialog
        onDismissRequest = onDismiss,
        title = { Text("Add New Reminder") }, // Material 3 Text
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField( // Material 3 OutlinedTextField
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title") }, // Material 3 Text
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors( // Material 3 TextFieldDefaults
                        focusedBorderColor = colorResource(id = R.color.button_color),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = colorResource(id = R.color.button_color)
                    )
                )
                OutlinedTextField( // Material 3 OutlinedTextField
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") }, // Material 3 Text
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors( // Material 3 TextFieldDefaults
                        focusedBorderColor = colorResource(id = R.color.button_color),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = colorResource(id = R.color.button_color)
                    )
                )
                // Date and Time Picker Button
                Button( // Material 3 Button
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors( // Material 3 ButtonDefaults
                        containerColor = colorResource(id = R.color.button_color),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select Date and Time"
                    ) // Material 3 Icon
                    Spacer(Modifier.width(8.dp))
                    Text(selectedDateTime?.let {
                        SimpleDateFormat(
                            "MMM dd, yyyy HH:mm",
                            Locale.getDefault()
                        ).format(it.time)
                    } ?: "Select Date & Time") // Material 3 Text
                }
            }
        },
        confirmButton = {
            Button( // Material 3 Button
                onClick = {
                    if (title.isNotBlank() && selectedDateTime != null) {
                        onAddReminder(title, description, selectedDateTime!!.timeInMillis)
                    }
                },
                colors = ButtonDefaults.buttonColors( // Material 3 ButtonDefaults
                    containerColor = colorResource(id = R.color.button_color),
                    contentColor = Color.White
                )
            ) {
                Text("Add") // Material 3 Text
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { // Material 3 Button
                Text("Cancel") // Material 3 Text
            }
        }
    )
}


// 3.8. CompletedListsScreen (New)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedListsScreen(taskViewModel: TaskViewModel, navController: NavController) {
    // Filter tasks to show only completed ones
//    val completedTasks by taskViewModel.tasks.collectAsState().value.filter { it.isCompleted }.toMutableStateList()

    val allTasks by taskViewModel.tasks.collectAsState()
    val completedTasks = remember(allTasks) {
        allTasks.filter { it.isCompleted }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Completed Tasks", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (completedTasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No completed tasks",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tasks completed yet!", color = Color.Gray, fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(completedTasks, key = { it.id }) { task ->
                        // Display completed tasks, perhaps without the checkbox or with it disabled
                        TaskItem(task = task,
                            onToggleComplete = { isChecked ->
                                taskViewModel.markTaskAsDone(
                                    task,
                                    isChecked
                                )
                            },
                            onDelete = { taskViewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

// 3.9. HistoryScreen (New - Placeholder)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("History", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "History",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your activity history will appear here.", color = Color.Gray, fontSize = 18.sp)
            Text(
                "This screen can show a log of tasks added, completed, etc.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

// 3.10. GoalScreen (Placeholder for future goal setting)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(goalViewModel: GoalViewModel, navController: NavController) {
    val goals by goalViewModel.goals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("My Goals", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) {
                Icon(Icons.Filled.Info, "Add new goal")
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (goals.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No goals",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No goals set yet. Set a new goal!", color = Color.Gray, fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        GoalItem(
                            goal = goal,
                            onToggleComplete = { isChecked ->
                                goalViewModel.markGoalAsDone(
                                    goal,
                                    isChecked
                                )
                            },
                            onDelete = { goalViewModel.deleteGoal(goal) },
                            onUpdateProgress = { newProgress ->
                                goalViewModel.updateGoalProgress(
                                    goal,
                                    newProgress
                                )
                            } // Pass the update progress callback
                        )
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAddGoal = { description, type, targetPeriod ->
                goalViewModel.addGoal(description, type, targetPeriod)
                showAddGoalDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalItem(
    goal: Goal,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onUpdateProgress: (Int) -> Unit // New callback for updating progress
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    var showUpdateProgressDialog by remember { mutableStateOf(false) }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.Settled -> Color.LightGray
                SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.8f)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Goal Description
                        Text(
                            text = goal.description,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (goal.isCompleted) Color.Gray else Color.Black,
                            textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )
                        // Edit Progress Button
                        IconButton(onClick = { showUpdateProgressDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                "Edit Progress",
                                tint = colorResource(id = R.color.button_color)
                            )
                        }
                        // Checkbox for completion (optional, if progress is the main driver)
                        Checkbox(
                            checked = goal.isCompleted,
                            onCheckedChange = onToggleComplete, // This will mark 100% or reset
                            colors = CheckboxDefaults.colors(
                                checkedColor = colorResource(id = R.color.button_color),
                                uncheckedColor = Color.Gray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Goal Type and Period
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Type: ${
                                goal.type.name.lowercase().replaceFirstChar { it.uppercase() }
                            }",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Period: ${goal.targetPeriod}",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    Text(
                        text = "Progress: ${goal.currentProgress}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    LinearProgressIndicator(
                        progress = goal.currentProgress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)), // Rounded corners for progress bar
                        color = colorResource(id = R.color.button_color),
                        trackColor = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Creation Date
                    Text(
                        text = "Created: ${
                            SimpleDateFormat(
                                "MMM dd, yyyy",
                                Locale.getDefault()
                            ).format(Date(goal.createdAt))
                        }",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    )

    if (showUpdateProgressDialog) {
        UpdateGoalProgressDialog(
            goal = goal,
            onDismiss = { showUpdateProgressDialog = false },
            onUpdateProgress = { newProgress ->
                onUpdateProgress(newProgress)
                showUpdateProgressDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateGoalProgressDialog(goal: Goal, onDismiss: () -> Unit, onUpdateProgress: (Int) -> Unit) {
    var progressInput by remember { mutableStateOf(goal.currentProgress.toString()) }
    val context = LocalContext.current // Used for Toast, if you want to show error messages

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Progress for \"${goal.description}\"") },
        text = {
            Column {


                OutlinedTextField(
                    value = progressInput,
                    onValueChange = { newValue ->
                        // Allow only digits and limit length
                        if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                            progressInput = newValue
                        }
                    },
                    label = { Text("Progress (0-100%)") },
                    // Correct usage of keyboardOptions for Material 3 OutlinedTextField
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    // Use Material 3 TextFieldDefaults for colors
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(id = R.color.button_color),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = colorResource(id = R.color.button_color),
                        // Add other colors if needed, e.g., containerColor
                        // containerColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newProgress = progressInput.toIntOrNull()
                    if (newProgress != null && newProgress >= 0 && newProgress <= 100) {
                        onUpdateProgress(newProgress)
                    } else {
                        // Optionally show a Toast or snackbar for invalid input
                        // Toast.makeText(context, "Please enter a valid percentage (0-100)", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.button_color),
                    contentColor = Color.White
                )
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getUpcomingWeeks(count: Int = 5): List<String> {
    val weeks = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY // Set Monday as the first day of the week

    // Adjust to the start of the current week (Monday)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    // If the calculated Monday is in the future (e.g., today is Sunday and we jumped to next Monday),
    // go back one week to ensure the current week is included if it's not over.
    if (calendar.timeInMillis > System.currentTimeMillis() &&
        calendar.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
    }

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    for (i in 0 until count) {
        val startOfWeek = calendar.time // Get Monday's date
        calendar.add(Calendar.DAY_OF_YEAR, 6) // Move to Sunday
        val endOfWeek = calendar.time // Get Sunday's date
        weeks.add("${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}")
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to next Monday for the next iteration
    }
    return weeks
}

// Helper function to get upcoming months
fun getUpcomingMonths(count: Int = 5): List<String> {
    val months = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    for (i in 0 until count) {
        months.add(dateFormat.format(calendar.time))
        calendar.add(Calendar.MONTH, 1) // Move to next month
    }
    return months
}

@OptIn(ExperimentalMaterial3Api::class) // Required for AlertDialog, Button, OutlinedTextField, RadioButton, FilterChip
@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onAddGoal: (String, GoalType, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var selectedGoalType by remember { mutableStateOf(GoalType.WEEKLY) }

    // State for the selected period string (e.g., "Jul 28 - Aug 03" or "July 2025")
    var selectedPeriodString by remember { mutableStateOf("") }

    // Initialize selectedPeriodString when the dialog first appears or goal type changes
    LaunchedEffect(selectedGoalType) {
        val defaultCalendar = Calendar.getInstance()
        selectedPeriodString = when (selectedGoalType) {
            GoalType.WEEKLY -> getUpcomingWeeks(1).first() // Get current week
            GoalType.MONTHLY -> getUpcomingMonths(1).first() // Get current month
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Goal Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(id = R.color.button_color),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = colorResource(id = R.color.button_color)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Goal Type:", fontWeight = FontWeight.SemiBold)
                    GoalType.entries.forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedGoalType == type,
                                onClick = {
                                    selectedGoalType = type
                                    // Reset selected period string when type changes
                                    selectedPeriodString = when (type) {
                                        GoalType.WEEKLY -> getUpcomingWeeks(1).first()
                                        GoalType.MONTHLY -> getUpcomingMonths(1).first()
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colorResource(id = R.color.button_color))
                            )
                            Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chips for selecting period
                Text("Select Period:", fontWeight = FontWeight.SemiBold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    val periods = when (selectedGoalType) {
                        GoalType.WEEKLY -> getUpcomingWeeks()
                        GoalType.MONTHLY -> getUpcomingMonths()
                    }
                    items(periods) { period ->
                        FilterChip( // Material 3 FilterChip
                            selected = selectedPeriodString == period, // Correctly passing 'selected'
                            onClick = { selectedPeriodString = period },
                            label = { Text(period) },
                            enabled = true, // Explicitly setting 'enabled' to true
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorResource(id = R.color.button_color),
                                selectedLabelColor = Color.White,
                                containerColor = Color.LightGray,
                                labelColor = Color.DarkGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, // Pass enabled state
                                selected = selectedPeriodString == period, // Pass selected state
                                borderColor = if (selectedPeriodString == period) colorResource(id = R.color.button_color) else Color.Gray,
                                borderWidth = 1.dp,
                                selectedBorderColor = colorResource(id = R.color.button_color)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank() && selectedPeriodString.isNotBlank()) {
                        onAddGoal(description, selectedGoalType, selectedPeriodString)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.button_color),
                    contentColor = Color.White
                )
            ) {
                Text("Set Goal")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
// 3.11. SplashScreen (Your existing placeholder)
@Composable
fun SplashScreen(navController: NavController) {

    val context = LocalContext.current as Activity

    LaunchedEffect(Unit) {
        // Simulate some loading time
        kotlinx.coroutines.delay(2000) // 2 second delay


        val UserStatus = UserDetails.getUserLoginStatus(context)

        if (UserStatus) {
            navController.navigate(AppDestinations.Home.route) {
                // This pops up to the start destination (Splash) and removes it
                popUpTo(AppDestinations.Splash.route) {
                    inclusive = true
                }
            }
        } else {
            navController.navigate(AppDestinations.Login.route) {
                // This pops up to the start destination (Splash) and removes it
                popUpTo(AppDestinations.Splash.route) {
                    inclusive = true
                }
            }
        }

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.PrimaryDark)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Todo List App", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Organize Your Life", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

// 3.12. UserLogin (Your existing placeholder)
@Composable
fun UserLogin1(onLoginSuccess: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.card_background_light)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onLoginSuccess(1) }) { // Simulate successful login
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onLoginSuccess(0) }) { // Simulate navigation to register
            Text("Register")
        }
    }
}

// 3.13. TodoListRegister (Your existing placeholder)
@Composable
fun TodoListRegister(onActionClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.card_background_light)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onActionClicked) {
            Text("Go Back to Login")
        }
    }
}

// 3.14. SettingsScreen (Your existing placeholder)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Settings", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Settings",
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Settings options will be available here.", color = Color.Gray, fontSize = 18.sp)
        }
    }
}

//endregion

//region 4. Preview (for testing individual components)
@Preview(showBackground = true)
@Composable
fun PreviewMyTasksScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModelFactory = remember { AppViewModelFactory(application) }
    val taskViewModel: TaskViewModel = viewModel(factory = viewModelFactory)
    val navController = rememberNavController() // Dummy NavController for preview

    TODOListProjectTheme {
        MyTasksScreen(taskViewModel = taskViewModel, navController = navController)
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewAddTaskScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModelFactory = remember { AppViewModelFactory(application) }
    val taskViewModel: TaskViewModel = viewModel(factory = viewModelFactory)
    val navController = rememberNavController() // Dummy NavController for preview

    TODOListProjectTheme {
        AddTaskScreen(taskViewModel = taskViewModel, navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReminderScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModelFactory = remember { AppViewModelFactory(application) }
    val reminderViewModel: ReminderViewModel = viewModel(factory = viewModelFactory)
    val navController = rememberNavController() // Dummy NavController for preview

    TODOListProjectTheme {
        RemindersScreen(reminderViewModel = reminderViewModel, navController = navController)
    }
}

//endregion
