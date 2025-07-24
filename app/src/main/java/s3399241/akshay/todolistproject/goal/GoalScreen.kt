package s3399241.akshay.todolistproject.goal

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import s3399241.akshay.todolistproject.R
import s3399241.akshay.todolistproject.roomdb.Goal
import s3399241.akshay.todolistproject.roomdb.GoalType
import s3399241.akshay.todolistproject.roomdb.GoalViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(goalViewModel: GoalViewModel, navController: NavController) {
    val goals by goalViewModel.goals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("My Goals", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.iv_add_goal),
                    "Add new goal",
                    modifier = Modifier.size(36.dp)
                )
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
                            }
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
    onUpdateProgress: (Int) -> Unit
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
                            onCheckedChange = onToggleComplete,
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
                            .clip(RoundedCornerShape(4.dp)),
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Progress for \"${goal.description}\"") },
        text = {
            Column {


                OutlinedTextField(
                    value = progressInput,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                            progressInput = newValue
                        }
                    },
                    label = { Text("Progress (0-100%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(id = R.color.button_color),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = colorResource(id = R.color.button_color),

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
    calendar.firstDayOfWeek = Calendar.MONDAY

    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    if (calendar.timeInMillis > System.currentTimeMillis() &&
        calendar.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    ) {
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
    }

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    for (i in 0 until count) {
        val startOfWeek = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val endOfWeek = calendar.time
        weeks.add("${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}")
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return weeks
}

fun getUpcomingMonths(count: Int = 5): List<String> {
    val months = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    for (i in 0 until count) {
        months.add(dateFormat.format(calendar.time))
        calendar.add(Calendar.MONTH, 1)
    }
    return months
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onAddGoal: (String, GoalType, String) -> Unit) {
    var description by remember { mutableStateOf("") }
    var selectedGoalType by remember { mutableStateOf(GoalType.WEEKLY) }

    var selectedPeriodString by remember { mutableStateOf("") }

    LaunchedEffect(selectedGoalType) {
        val defaultCalendar = Calendar.getInstance()
        selectedPeriodString = when (selectedGoalType) {
            GoalType.WEEKLY -> getUpcomingWeeks(1).first()
            GoalType.MONTHLY -> getUpcomingMonths(1).first()
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
                            selected = selectedPeriodString == period,
                            onClick = { selectedPeriodString = period },
                            label = { Text(period) },
                            enabled = true,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorResource(id = R.color.button_color),
                                selectedLabelColor = Color.White,
                                containerColor = Color.LightGray,
                                labelColor = Color.DarkGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedPeriodString == period,
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
