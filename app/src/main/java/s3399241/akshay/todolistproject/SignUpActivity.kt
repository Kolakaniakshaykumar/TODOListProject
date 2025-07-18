package s3399241.akshay.todolistproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
//            TodoListRegister()
        }
    }
}

@Composable
fun TodoListRegister(onActionClicked: (clickType:Int) -> Unit) {
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
                                doesUserExits(userData, context,onActionClicked)
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
//                            context.startActivity(Intent(context, SignInActivity::class.java))
//                            context.finish()
                        }
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(30.dp))

            }

        }
        Spacer(modifier = Modifier.weight(1f))

//        Row(
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text(text = "You are an old user ?", fontSize = 14.sp)
//            Spacer(modifier = Modifier.width(4.dp))
//            Text(
//                text = "Sign In",
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Bold,
//                color = colorResource(id = R.color.PureWhite),
//                modifier = Modifier.clickable {
//                    context.startActivity(Intent(context, SignInActivity::class.java))
//                    context.finish()
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))

    }

}


private fun doesUserExits(userData1: UserData, context: Activity,onActionClicked: (clickType:Int) -> Unit) {
    val db = FirebaseDatabase.getInstance()
    val sanitizedUid = userData1.email.replace(".", ",")
    val ref = db.getReference("Users").child(sanitizedUid)

    ref.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val userData = task.result?.getValue(UserData::class.java)
            if (userData != null) {
                Toast.makeText(context, "User already exists", Toast.LENGTH_SHORT).show()
            } else {
                saveUserData(userData1, context,onActionClicked)
            }
        } else {
            // Data retrieval failed
            Toast.makeText(
                context,
                "Failed to retrieve user data: ${task.exception?.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


private fun saveUserData(userData: UserData, context: Activity,onActionClicked: (clickType:Int) -> Unit) {
    val db = FirebaseDatabase.getInstance()
    val ref = db.getReference("Users")

    ref.child(userData.email.replace(".", ",")).setValue(userData)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onActionClicked.invoke(1)
                Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
//                context.startActivity(Intent(context, SignInActivity::class.java))
//                context.finish()

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

fun isValidUsername(username: String): Boolean {
    val regex = "^[a-zA-Z]+$".toRegex()
    return !regex.matches(username)
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return !emailRegex.matches(email)
}

data class UserData(
    val fullName: String = "",
    val gender: String = "",
    val email: String = "",
    val password: String = ""
)
