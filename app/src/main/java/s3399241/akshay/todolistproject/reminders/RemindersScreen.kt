package s3399241.akshay.todolistproject.reminders

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import s3399241.akshay.todolistproject.R
import s3399241.akshay.todolistproject.roomdb.Reminder
import s3399241.akshay.todolistproject.roomdb.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(reminderViewModel: ReminderViewModel, navController: NavController) {
    val reminders by reminderViewModel.reminders.collectAsState()
    var showAddReminderDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current


    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Re-attempt save and schedule after permission is granted
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                "Notification permission denied. Reminders may not show.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val exactAlarmPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (NotificationScheduler.canScheduleExactAlarms(context)) {
            Toast.makeText(context, "Alarms & reminders permission granted!", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(context, "Alarms & reminders permission denied. Exact reminders may not work.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("My Reminders", color = Color.Black) },
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
            FloatingActionButton(onClick = {
                showAddReminderDialog = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.iv_add_reminder_new),
                    "Add new reminder",
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
            if (reminders.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iv_reminders),
                        contentDescription = "No reminders",
                        modifier = Modifier.size(64.dp)
                    ) // Material 3 Icon
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No reminders set. Add one!",
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Check for SCHEDULE_EXACT_ALARM permission on Android 12+
                if (NotificationScheduler.canScheduleExactAlarms(context)) {
                    AddReminderDialog(
                        onDismiss = { showAddReminderDialog = false },
                        onAddReminder = { title, description, time ->
                            reminderViewModel.addReminder(title, description, time)
                            showAddReminderDialog = false
                        }
                    )
                } else {
                    // Request SCHEDULE_EXACT_ALARM permission
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    exactAlarmPermissionLauncher.launch(intent)
                    Toast.makeText(context, "Please grant 'Alarms & reminders' permission in settings.", Toast.LENGTH_LONG).show()
                }
            } else {
                // Request POST_NOTIFICATIONS permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                Toast.makeText(context, "Please grant notification permission to set reminders.", Toast.LENGTH_LONG).show()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) to Android 12L (API 32)
            // Only SCHEDULE_EXACT_ALARM is required here, POST_NOTIFICATIONS is runtime for 33+
            if (NotificationScheduler.canScheduleExactAlarms(context)) {
                AddReminderDialog(
                    onDismiss = { showAddReminderDialog = false },
                    onAddReminder = { title, description, time ->
                        reminderViewModel.addReminder(title, description, time)
                        showAddReminderDialog = false
                    }
                )
            } else {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                exactAlarmPermissionLauncher.launch(intent)
                Toast.makeText(context, "Please grant 'Alarms & reminders' permission in settings.", Toast.LENGTH_LONG).show()
            }
        }
        else {
            AddReminderDialog(
                onDismiss = { showAddReminderDialog = false },
                onAddReminder = { title, description, time ->
                    reminderViewModel.addReminder(title, description, time)
                    showAddReminderDialog = false
                }
            )
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItem(reminder: Reminder, onDismiss: () -> Unit, onDelete: () -> Unit) {
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
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = reminder.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isDismissed) Color.Gray else Color.Black,
                            textDecoration = if (reminder.isDismissed) TextDecoration.LineThrough else null
                        )
                        if (reminder.description.isNotBlank()) {
                            Text(
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
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Info,
                                "Dismiss Reminder",
                                tint = colorResource(id = R.color.button_color)
                            )
                        }
                    } else {
                        Icon(Icons.Default.Done, "Dismissed", tint = Color.Gray) // Material 3 Icon
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onAddReminder: (String, String, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDateTime: Calendar? by remember { mutableStateOf(null) }
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
                showTimePicker = true
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
            hour, minute, false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField( // Material 3 OutlinedTextField
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(id = R.color.button_color),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = colorResource(id = R.color.button_color)
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorResource(id = R.color.button_color),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = colorResource(id = R.color.button_color)
                    )
                )
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.button_color),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select Date and Time"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(selectedDateTime?.let {
                        SimpleDateFormat(
                            "MMM dd, yyyy HH:mm",
                            Locale.getDefault()
                        ).format(it.time)
                    } ?: "Select Date & Time")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDateTime != null) {
                        onAddReminder(title, description, selectedDateTime!!.timeInMillis)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.button_color),
                    contentColor = Color.White
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
