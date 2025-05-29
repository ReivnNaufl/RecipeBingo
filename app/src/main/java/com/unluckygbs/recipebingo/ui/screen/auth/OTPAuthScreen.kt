package com.unluckygbs.recipebingo.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unluckygbs.recipebingo.data.client.KeyClient
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun OTPAuthScreen(navController: NavController, authViewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val email = navController.previousBackStackEntry?.savedStateHandle?.get<String>("email") ?: ""
    val password = navController.previousBackStackEntry?.savedStateHandle?.get<String>("password") ?: ""
    val username = navController.previousBackStackEntry?.savedStateHandle?.get<String>("username") ?: ""
    val context = LocalContext.current
    var otp by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "OTP Verification",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Enter the 6-digit OTP sent to your email",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        BasicTextField(
            value = otp,
            onValueChange = { newValue ->
                if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                    otp = newValue
                }
            },
            modifier = Modifier
                .width(200.dp)
                .height(56.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .padding(16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = button@{
                val otpInt = otp.toIntOrNull()
                if (otpInt == null || otp.length != 6) {
                    Toast.makeText(context, "Enter valid 4-digit OTP", Toast.LENGTH_SHORT).show()
                    return@button
                }

                isVerifying = true
                authViewModel.verifyOtp(
                    email = email,
                    otp = otpInt,
                    onSuccess = {
                        authViewModel.register(email, password, username)
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onError = { message ->
                        isVerifying = false
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = !isVerifying,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(
                text = if (isVerifying) "Verifying..." else "Verify",
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Resend OTP?",
            fontSize = 14.sp,
            color = Color(0xFF4CAF50),
            modifier = Modifier.clickable {
                scope.launch {
                    try {
                        val body = mapOf("email" to email)
                        val response = KeyClient.apiService.postOtpRequest(body = body)
                        if (response.isSuccessful) {
                            Toast.makeText(context, "OTP Resent", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}