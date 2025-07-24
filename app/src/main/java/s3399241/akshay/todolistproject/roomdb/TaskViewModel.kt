package s3399241.akshay.todolistproject.roomdb

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import s3399241.akshay.todolistproject.reminders.NotificationScheduler

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

class TaskViewModel(application: Application, private val taskRepository: TaskRepository) :
    AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _filterStatus = MutableStateFlow(FilterStatus.ALL)

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

            reminderTime?.let {
                val notificationId = System.currentTimeMillis().toInt()
                NotificationScheduler.scheduleNotification(
                    getApplication(),
                    notificationId,
                    newTask.title,
                    newTask.description,
                    it
                )
            }
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
            val newReminder = Reminder(
                title = title,
                description = description,
                reminderTime = reminderTime
            )
            reminderRepository.insert(newReminder)

            reminderTime.let {
                // Pass the task ID after insertion (if available, or pass details)
                // For simplicity, we'll pass the details directly.
                // In a real app, you might want to retrieve the ID after insertion
                // and then schedule using that ID for easy cancellation.
                // For now, we'll use a unique ID based on current time.
                val notificationId = System.currentTimeMillis().toInt()
                NotificationScheduler.scheduleNotification(
                    getApplication(),
                    notificationId,
                    title,
                    description,
                    it
                )
            }

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
