package s3399241.akshay.todolistproject


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import s3399241.akshay.todolistproject.roomdb.ReminderViewModel
import s3399241.akshay.todolistproject.roomdb.TaskViewModel
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenDesign(
    taskViewModel: TaskViewModel,
    reminderViewModel: ReminderViewModel,
    onMyTasksClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onCompletedListsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onQuickAddTaskClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val allTasks by taskViewModel.tasks.collectAsState()
    val allReminders by reminderViewModel.reminders.collectAsState()

    val pendingTasksCount = remember(allTasks) {
        allTasks.count { !it.isCompleted }
    }
    val tasksDueTodayCount = remember(allTasks) {
        allTasks.count { !it.isCompleted && it.dueDate != null && isToday(it.dueDate) }
    }
    val upcomingRemindersCount = remember(allReminders) {
        allReminders.count { !it.isDismissed && it.reminderTime > System.currentTimeMillis() }
    }
    val completedTasksTodayCount = remember(allTasks) {
        allTasks.count { it.isCompleted && isToday(it.createdAt) }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            println("Notification permission granted.")
        } else {

            println("Notification permission denied.")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    val primaryDark = colorResource(id = R.color.PrimaryDark)
    val buttonColor = colorResource(id = R.color.button_color)
    val textOnPrimaryDark = colorResource(id = R.color.text_on_primary_dark)
    val textOnButton = colorResource(id = R.color.text_on_button)
    val cardBackgroundLight = colorResource(id = R.color.card_background_light)

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
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
                    IconButton(onClick = onMyTasksClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = textOnPrimaryDark
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = textOnPrimaryDark
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onQuickAddTaskClick,
                containerColor = buttonColor,
                contentColor = textOnButton,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Add, "Add new task")
            }
        },
        containerColor = Color.White
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
            Text(
                text = "Hello, User!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
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
                    icon = R.drawable.iv_pendingtask,
                    iconTint = buttonColor,
                    onClick = onMyTasksClick
                )
                SummaryCard(
                    title = "Due Today",
                    count = tasksDueTodayCount,
                    icon = R.drawable.iv_due_today,
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
                    icon = R.drawable.iv_completed_task,
                    iconTint = Color(0xFF66BB6A),
                    onClick = onCompletedListsClick
                )
                SummaryCard(
                    title = "Total Reminders",
                    count = upcomingRemindersCount,
                    icon = R.drawable.iv_reminders,
                    iconTint = buttonColor,
                    onClick = onRemindersClick
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DashboardSectionCard(
                title = "My Tasks",
                description = "View and manage all your pending tasks.",
                icon = R.drawable.iv_mytask,
                onClick = onMyTasksClick,
                backgroundColor = primaryDark,
                contentColor = textOnPrimaryDark
            )
            DashboardSectionCard(
                title = "Reminders",
                description = "See all your scheduled reminders and deadlines.",
                icon = R.drawable.iv_add_reminders,
                onClick = onRemindersClick,
                backgroundColor = cardBackgroundLight,
                contentColor = Color.DarkGray
            )
            DashboardSectionCard(
                title = "Completed Lists",
                description = "Review your achievements and finished tasks.",
                icon = R.drawable.iv_completed_task,
                onClick = onCompletedListsClick,
                backgroundColor = primaryDark,
                contentColor = textOnPrimaryDark
            )
            DashboardSectionCard(
                title = "Goals", // Changed title to "Goals"
                description = "Set Monthly and Weekly Goals.",
                icon = R.drawable.iv_goal,
                onClick = onHistoryClick, // This onClick now navigates to GoalsScreen
                backgroundColor = cardBackgroundLight,
                contentColor = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp }
    val cal2 = Calendar.getInstance()

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun RowScope.SummaryCard(
    title: String,
    count: Int,
    icon: Int,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp)
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
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
    icon: Int,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
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

