package s3399241.akshay.todolistproject

//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//// Composable function for the Add Task Screen
//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun AddTaskScreen(
//    onAddTask: (String, String) -> Unit, // Callback when a task is added
//    onBack: () -> Unit // Callback to navigate back
//) {
//    // State variables to hold the input for task title and description
//    var taskTitle by remember { mutableStateOf("") }
//    var taskDescription by remember { mutableStateOf("") }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Add New Task", color = Color.White) },
//                backgroundColor = MaterialTheme.colors.primary,
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues) // Apply padding from Scaffold
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between elements
//        ) {
//            Text(
//                text = "What's on your mind?",
//                fontSize = 24.sp,
//                color = MaterialTheme.colors.onBackground,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            // Task Title Input Field
//            OutlinedTextField(
//                value = taskTitle,
//                onValueChange = { taskTitle = it },
//                label = { Text("Task Title") },
//                modifier = Modifier.fillMaxWidth(),
//                singleLine = true,
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = MaterialTheme.colors.primary,
//                    unfocusedBorderColor = Color.Gray,
//                    focusedLabelColor = MaterialTheme.colors.primary
//                )
//            )
//
//            // Task Description Input Field
//            OutlinedTextField(
//                value = taskDescription,
//                onValueChange = { taskDescription = it },
//                label = { Text("Task Description (Optional)") },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .heightIn(min = 120.dp), // Minimum height for description
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = MaterialTheme.colors.primary,
//                    unfocusedBorderColor = Color.Gray,
//                    focusedLabelColor = MaterialTheme.colors.primary
//                )
//            )
//
//            Spacer(modifier = Modifier.height(8.dp)) // Spacer for better layout
//
//            // Add Task Button
//            Button(
//                onClick = {
//                    // Only add task if title is not empty
//                    if (taskTitle.isNotBlank()) {
//                        onAddTask(taskTitle, taskDescription)
//                        // Clear fields after adding
//                        taskTitle = ""
//                        taskDescription = ""
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp),
//                shape = RoundedCornerShape(12.dp),
//                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
//            ) {
//                Text("Add Task", color = Color.White, fontSize = 18.sp)
//            }
//        }
//    }
//}
//
//// Preview function for the AddTaskScreen
//@Preview(showBackground = true)
//@Composable
//fun PreviewAddTaskScreen() {
//    MaterialTheme { // Use MaterialTheme for proper preview styling
//        AddTaskScreen(
//            onAddTask = { title, description ->
//                // This is a preview, so we just print to console
//                println("Task Added: Title - $title, Description - $description")
//            },
//            onBack = {
//                println("Navigating back")
//            }
//        )
//    }
//}