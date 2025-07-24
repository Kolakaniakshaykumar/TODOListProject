package s3399241.akshay.todolistproject.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import s3399241.akshay.todolistproject.AppDestinations
import s3399241.akshay.todolistproject.R
import s3399241.akshay.todolistproject.roomdb.FilterStatus
import s3399241.akshay.todolistproject.roomdb.SortOrder
import s3399241.akshay.todolistproject.roomdb.Task
import s3399241.akshay.todolistproject.roomdb.TaskPriority
import s3399241.akshay.todolistproject.roomdb.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTasksScreen(taskViewModel: TaskViewModel, navController: NavController) {
    val tasks by taskViewModel.tasks.collectAsState()
    val searchQuery by taskViewModel.searchQuery.collectAsState()
    val sortOrder by taskViewModel.sortOrder.collectAsState()
    val filterStatus by taskViewModel.filterStatus.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var showSortFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("My Tasks", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Material 3 IconButton
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.Black) // Material 3 Icon
                    }
                },
                actions = {
                    // Search Icon
                    IconButton(onClick = {
                        showSearchBar = !showSearchBar
                    }) {
                        Icon(Icons.Filled.Search, "Search", tint = Color.Black)
                    }

                    IconButton(onClick = { showSortFilterDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.iv_filter),
                            "Sort and Filter",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(AppDestinations.AddTask.route) }) {
                Icon(Icons.Filled.Add, "Add new task")
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background // Set Scaffold background color
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            AnimatedVisibility(
                visible = showSearchBar,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
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
                            IconButton(onClick = { taskViewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Filled.Clear, "Clear search")
                            }
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
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
                    title = { Text("Sort & Filter Options") },
                    text = {
                        Column {
                            Text("Sort By:", fontWeight = FontWeight.Bold)
                            Column(Modifier.padding(start = 8.dp)) {
                                SortOrder.entries.forEach { order ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton( // Material 3 RadioButton
                                            selected = sortOrder == order,
                                            onClick = { taskViewModel.onSortOrderChanged(order) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = colorResource(id = R.color.button_color)
                                            )
                                        )
                                        Text(
                                            order.name.replace("_", " ").lowercase()
                                                .replaceFirstChar { it.uppercase() })
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Filter By Status:",
                                fontWeight = FontWeight.Bold
                            )
                            Column(Modifier.padding(start = 8.dp)) {
                                FilterStatus.entries.forEach { status ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = filterStatus == status,
                                            onClick = { taskViewModel.onFilterStatusChanged(status) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = colorResource(id = R.color.button_color)
                                            )
                                        )
                                        Text(
                                            status.name.lowercase()
                                                .replaceFirstChar { it.uppercase() })
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showSortFilterDialog = false }) {
                            Text("Done")
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
                    Image(
                        painter = painterResource(id = R.drawable.iv_mytask),
                        contentDescription = "No tasks",
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No tasks found. Add a new one!",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(task: Task, onToggleComplete: (Boolean) -> Unit, onDelete: () -> Unit) {
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
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = onToggleComplete,
                        colors = CheckboxDefaults.colors(
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
