package s3399241.akshay.todolistproject.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import s3399241.akshay.todolistproject.MainActivity
import s3399241.akshay.todolistproject.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {

    private const val CHANNEL_ID = "todo_reminder_channel"
    private const val CHANNEL_NAME = "Todo Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for your tasks and reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH // Make it a high importance notification
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, notificationId: Int, title: String, description: String) {
        // Create an Intent for the activity you want to launch when the notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.iv_add_reminders) // Use a proper icon, e.g., your app's launcher icon
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up notifications
            .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
            .setAutoCancel(true) // Automatically removes the notification when the user taps it

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    const val NOTIFICATION_ID_EXTRA = "notification_id"
    const val NOTIFICATION_TITLE_EXTRA = "notification_title"
    const val NOTIFICATION_DESCRIPTION_EXTRA = "notification_description"

    /**
     * Schedules a notification to be displayed at a specific time.
     *
     * @param context The application context.
     * @param notificationId A unique ID for this notification. Used for scheduling and cancellation.
     * @param title The title of the notification.
     * @param description The detailed text of the notification.
     * @param triggerTimeMillis The time in milliseconds (System.currentTimeMillis() format) when the notification should appear.
     */
    fun scheduleNotification(
        context: Context,
        notificationId: Int,
        title: String,
        description: String,
        triggerTimeMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            // Put notification details into the intent so the receiver can use them
            putExtra(NOTIFICATION_ID_EXTRA, notificationId)
            putExtra(NOTIFICATION_TITLE_EXTRA, title)
            putExtra(NOTIFICATION_DESCRIPTION_EXTRA, description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId, // Use unique ID for request code to allow multiple distinct alarms
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            Log.d(TAG, "Scheduled exact notification for ID $notificationId at ${Date(triggerTimeMillis)}")
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            Log.d(TAG, "Scheduled notification for ID $notificationId at ${Date(triggerTimeMillis)}")
        }

        // Show a Toast message after scheduling the notification
        val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val scheduledTime = timeFormat.format(Date(triggerTimeMillis))
        Toast.makeText(context, "Reminder set for $scheduledTime: $title", Toast.LENGTH_LONG).show()
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31) and above
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Permission not required or granted at install time for older Android versions
        }
    }

    /**
     * Cancels a previously scheduled notification.
     *
     * @param context The application context.
     * @param notificationId The unique ID of the notification to cancel.
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            Log.d(TAG, "Cancelled notification for ID $notificationId")
            Toast.makeText(context, "Reminder cancelled.", Toast.LENGTH_SHORT).show() // Optional: Show toast on cancel
        } ?: Log.d(TAG, "No pending intent found to cancel for ID $notificationId")
    }
}

class NotificationReceiver : BroadcastReceiver() {
    private val TAG = "NotificationReceiver"

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     * It extracts notification details from the intent and then calls NotificationHelper
     * to display the notification.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Extract notification details passed from NotificationScheduler
        val notificationId = intent.getIntExtra(NotificationScheduler.NOTIFICATION_ID_EXTRA, 0)
        val title = intent.getStringExtra(NotificationScheduler.NOTIFICATION_TITLE_EXTRA) ?: "Reminder"
        val description = intent.getStringExtra(NotificationScheduler.NOTIFICATION_DESCRIPTION_EXTRA) ?: "You have a task due!"

        Log.d(TAG, "Received broadcast for notification ID: $notificationId, Title: $title")

        // Only show notification if a valid ID is received
        if (notificationId != 0) {
            NotificationHelper.showNotification(context, notificationId, title, description)
        } else {
            Log.e(TAG, "Received broadcast with invalid notification ID (0).")
        }

        // --- IMPORTANT: For handling BOOT_COMPLETED ---
        // If you enabled RECEIVE_BOOT_COMPLETED permission and intent filter,
        // you would add logic here to re-schedule all pending alarms from your database.
        // Example (conceptual, requires database query logic):
        /*
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_QUICKBOOT_POWERON) {
            Log.d(TAG, "Device rebooted. Rescheduling pending reminders.")
            // You would typically use a CoroutineScope and query your database for
            // all tasks/reminders that have a future reminderTime and are not completed/dismissed.
            // Then, for each such item, call NotificationScheduler.scheduleNotification(...) again.
            // This would likely involve injecting your ReminderViewModel or ReminderRepository
            // or directly accessing the database DAO from a background thread.
            // Example:
            // val database = AppDatabase.getDatabase(context)
            // val reminderDao = database.reminderDao()
            // CoroutineScope(Dispatchers.IO).launch {
            //     reminderDao.getAllReminders().first().forEach { reminder ->
            //         if (!reminder.isDismissed && reminder.reminderTime > System.currentTimeMillis()) {
            //             NotificationScheduler.scheduleNotification(
            //                 context,
            //                 reminder.id, // Use reminder's ID for consistent cancellation
            //                 reminder.title,
            //                 reminder.description,
            //                 reminder.reminderTime
            //             )
            //         }
            //     }
            // }
        }
        */
    }
}