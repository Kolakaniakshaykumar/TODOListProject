package s3399241.akshay.todolistproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase


class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
//            UserLogin()
        }
    }
}


@Composable
fun UserLogin(onLoginSuccess: (type:Int) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    val context = LocalContext.current as Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
            .background(Color.Green),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {


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
            { // Logo (replace with acorn image resource if you have one)
                Image(
                    painter = painterResource(id = R.drawable.logo_todolist), // Replace with your drawable resource
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Login Title
                Text(
                    text = "Login To TODO List App",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64A70B), // Green color similar to the design
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center

                )


                Spacer(modifier = Modifier.height(16.dp))

                // Email TextField
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Password TextField
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text(text = "Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                        )
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Login Button
                Button(
                    onClick = {
                        // context.startActivity(Intent(context, StocksActivity::class.java))
                        //context.finish()
                        when {
                            email.isEmpty() -> {
                                Toast.makeText(context, " Please Enter Mail", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            password.isEmpty() -> {
                                Toast.makeText(
                                    context,
                                    " Please Enter Password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                signInWithuseremail(email, password, context,onLoginSuccess)
                            }

                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text(text = "Login")
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Forgot Password Text
                Text(
                    text = "Register",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable {

                            onLoginSuccess.invoke(2)
//                             context.startActivity(Intent(context, SignUpActivity::class.java))
//                            context.finish()
                        }
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
            Spacer(modifier = Modifier.weight(1f))

        }

    }
}


@Preview(showBackground = true)
@Composable
fun UserLoginPreview() {
//    UserLogin()
}

private fun signInWithuseremail(useremail: String, userpassword: String, context: Activity, onLoginSuccess: (type:Int) -> Unit) {
    val db = FirebaseDatabase.getInstance()
    val sanitizedUid = useremail.replace(".", ",")
    val ref = db.getReference("Users").child(sanitizedUid)

    ref.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val userData = task.result?.getValue(UserData::class.java)
            if (userData != null) {
                if (userData.password == userpassword) {
                    //Save User Details
                    saveUserDetails(userData, context)
//                    UserDetails.saveUserLoginStatus(context,true)
//                    UserDetails.saveEmail(context,useremail)

                    onLoginSuccess.invoke(1)
//                    context.startActivity(Intent(context, DashboardActivity::class.java))
//                    context.finish()

                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid Password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No user data found", Toast.LENGTH_SHORT).show()
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

fun saveUserDetails(user: UserData, context: Context) {
    UserDetails.saveUserLoginStatus(context = context, true)
    UserDetails.saveName(context, user.fullName)
    UserDetails.saveEmail(context, user.email)
}
