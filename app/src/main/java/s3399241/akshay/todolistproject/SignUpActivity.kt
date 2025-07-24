package s3399241.akshay.todolistproject

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase


@Composable
fun TodoListRegister(onActionClicked: (clickType: Int) -> Unit) {
    var fullname by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var confirmpassword by remember { mutableStateOf("") }

    val context = LocalContext.current as Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Green)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
        {
            Spacer(modifier = Modifier.weight(1f))


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
            {

                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    painter = painterResource(id = R.drawable.logo_todolist),
                    contentDescription = "TodoList",
                )

                Spacer(modifier = Modifier.height(16.dp))


                Text(
                    text = "Register To TODO List App",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64A70B),
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center

                )


                Spacer(modifier = Modifier.height(16.dp))


                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    value = fullname,
                    onValueChange = { fullname = it },
                    label = { Text("Enter FullName") }
                )



                Spacer(modifier = Modifier.height(6.dp))


                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter Your Email") }
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("Enter Your Country") }
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter Your Password") }
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    value = confirmpassword,
                    onValueChange = { confirmpassword = it },
                    label = { Text("Confirm Password") }
                )

                Spacer(modifier = Modifier.height(15.dp))

                Button(
                    onClick = {
                        when {
                            email.isEmpty() -> {
                                Toast.makeText(context, " Please Enter Mail", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            fullname.isEmpty() -> {
                                Toast.makeText(context, " Please Enter Name", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            country.isEmpty() -> {
                                Toast.makeText(context, " Please Enter Country", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            password.isEmpty() -> {
                                Toast.makeText(
                                    context,
                                    " Please Enter Password",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                            else -> {

                                val userData = UserData(
                                    fullName = fullname,
                                    gender = "Male",
                                    email = email,
                                    password = password
                                )
                                doesUserExits(userData, context, onActionClicked)
                            }

                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text(text = "Sign Up", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Forgot Password Text
                Text(
                    text = "Login",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable {
                            onActionClicked.invoke(2)
                        }
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(30.dp))

            }

            Spacer(modifier = Modifier.weight(1f))

        }

    }

}


private fun doesUserExits(
    userData1: UserData,
    context: Activity,
    onActionClicked: (clickType: Int) -> Unit
) {
    val db = FirebaseDatabase.getInstance()
    val sanitizedUid = userData1.email.replace(".", ",")
    val ref = db.getReference("Users").child(sanitizedUid)

    ref.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val userData = task.result?.getValue(UserData::class.java)
            if (userData != null) {
                Toast.makeText(context, "User already exists", Toast.LENGTH_SHORT).show()
            } else {
                saveUserData(userData1, context, onActionClicked)
            }
        } else {
            Toast.makeText(
                context,
                "Failed to retrieve user data: ${task.exception?.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


private fun saveUserData(
    userData: UserData,
    context: Activity,
    onActionClicked: (clickType: Int) -> Unit
) {
    val db = FirebaseDatabase.getInstance()
    val ref = db.getReference("Users")

    ref.child(userData.email.replace(".", ",")).setValue(userData)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onActionClicked.invoke(1)
                Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "User Registration Failed: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(
                context,
                "User Registration Failed: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
}


data class UserData(
    val fullName: String = "",
    val gender: String = "",
    val email: String = "",
    val password: String = ""
)
