package s3399241.akshay.todolistproject.tasks

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import s3399241.akshay.todolistproject.R
import s3399241.akshay.todolistproject.reminders.NotificationScheduler
import s3399241.akshay.todolistproject.roomdb.TaskPriority
import s3399241.akshay.todolistproject.roomdb.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                showTimePicker = true
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
            hour, minute, false
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
                showReminderTimePicker = true
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
            hour, minute, false
        ).show()
    }


    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Add New Task", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.PrimaryDark)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.Black)
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
                    Column() {
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

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                // Check for SCHEDULE_EXACT_ALARM permission on Android 12+
                                if (NotificationScheduler.canScheduleExactAlarms(context)) {
                                    taskViewModel.addTask(
                                        taskTitle,
                                        taskDescription,
                                        selectedPriority,
                                        selectedDueDate?.timeInMillis,
                                        if (isReminderEnabled) selectedReminderDate?.timeInMillis else null
                                    )
                                    navController.popBackStack()
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
                                taskViewModel.addTask(
                                    taskTitle,
                                    taskDescription,
                                    selectedPriority,
                                    selectedDueDate?.timeInMillis,
                                    if (isReminderEnabled) selectedReminderDate?.timeInMillis else null
                                )
                                navController.popBackStack()
                            } else {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                exactAlarmPermissionLauncher.launch(intent)
                                Toast.makeText(context, "Please grant 'Alarms & reminders' permission in settings.", Toast.LENGTH_LONG).show()
                            }
                        }
                        else {
                            taskViewModel.addTask(
                                taskTitle,
                                taskDescription,
                                selectedPriority,
                                selectedDueDate?.timeInMillis,
                                if (isReminderEnabled) selectedReminderDate?.timeInMillis else null
                            )
                            navController.popBackStack() // Go back after adding task
                        }


                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                )
            ) {
                Text("Add Task", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}