package s3399241.akshay.todolistproject

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenDesign(
    // Callbacks for navigation to different sections
    onMyTasksClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onCompletedListsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onQuickAddTaskClick: () -> Unit = {}, // For the quick add button
    onSettingsClick: () -> Unit = {} // For settings access
) {
    val primaryDark = colorResource(id = R.color.PrimaryDark)
    val buttonColor = colorResource(id = R.color.button_color)
    val textOnPrimaryDark = colorResource(id = R.color.text_on_primary_dark)
    val textOnButton = colorResource(id = R.color.text_on_button)
    val cardBackgroundLight = colorResource(id = R.color.card_background_light)

    // Mock data for summary statistics (replace with actual data from ViewModel/Repository)
    val pendingTasksCount = 7
    val tasksDueTodayCount = 2
    val upcomingRemindersCount = 3
    val completedTasksTodayCount = 5

    Scaffold(
        topBar = {
            TopAppBar(
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
                    // Search Icon
                    IconButton(onClick = { /* Handle search click */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = textOnPrimaryDark
                        )
                    }
                    // Settings Icon
                    IconButton(onClick = onSettingsClick) {
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
            FloatingActionButton(
                onClick = onQuickAddTaskClick,
                containerColor = buttonColor,
                contentColor = textOnButton,
                shape = RoundedCornerShape(50) // Circular FAB
            ) {
                Icon(Icons.Default.Add, "Add new task")
            }
        },
        containerColor = Color.White // A clean background for the main content
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Make the column scrollable
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Message
            Text(
                text = "Hello, User!", // Personalize this with actual user name
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

            // Summary Statistics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryCard(
                    title = "Pending Tasks",
                    count = pendingTasksCount,
                    icon = Icons.Default.AccountCircle,
                    iconTint = buttonColor,
                    onClick = onMyTasksClick
                )
                SummaryCard(
                    title = "Due Today",
                    count = tasksDueTodayCount,
                    icon = Icons.Default.Notifications, // Using Notifications for reminders/due
                    iconTint = Color(0xFFFFA726), // Orange for urgency
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
                    icon = Icons.Default.AccountCircle,
                    iconTint = Color(0xFF66BB6A), // Green for completion
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

            // Main Navigation Sections
            DashboardSectionCard(
                title = "My Tasks",
                description = "View and manage all your pending tasks.",
                icon = Icons.Default.AccountBox,
                onClick = onMyTasksClick,
                backgroundColor = primaryDark,
                contentColor = textOnPrimaryDark
            )
            DashboardSectionCard(
                title = "Reminders",
                description = "See all your scheduled reminders and deadlines.",
                icon = Icons.Default.Notifications,
                onClick = onRemindersClick,
                backgroundColor = cardBackgroundLight, // Use a lighter card background
                contentColor = Color.DarkGray
            )
            DashboardSectionCard(
                title = "Completed Lists",
                description = "Review your achievements and finished tasks.",
                icon = Icons.Default.AccountBox,
                onClick = onCompletedListsClick,
                backgroundColor = primaryDark,
                contentColor = textOnPrimaryDark
            )
            DashboardSectionCard(
                title = "History",
                description = "Browse your activity log and past entries.",
                icon = Icons.Default.AccountBox,
                onClick = onHistoryClick,
                backgroundColor = cardBackgroundLight,
                contentColor = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}

@Composable
fun RowScope.SummaryCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp)
            .padding(horizontal = 4.dp) // Smaller horizontal padding
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
                color = iconTint // Use icon tint for the count
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
                    color = contentColor.copy(alpha = 0.8f) // Slightly lighter description text
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward, // Right arrow icon
                contentDescription = "Navigate",
                tint = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreenDesign() {
    HomeScreenDesign()
}
