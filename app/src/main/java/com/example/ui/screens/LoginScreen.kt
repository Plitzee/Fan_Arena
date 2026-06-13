package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.FanArenaViewModel

@Composable
fun LoginScreen(
    viewModel: FanArenaViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(false) }
    var loginMethod by remember { mutableStateOf("email") } // "email" or "phone"

    // Forms fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Phone fields
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val selectedSports = remember { mutableStateListOf<String>("Football") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, null, tint = SecondaryNeonGreen, modifier = Modifier.size(40.dp))
                Text("FanArena", fontFamily = SoraFamily, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, color = OnSurface)
            }
            Text("THE REAL-TIME ARENA", color = PrimaryElectricBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Login Method Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceLowest),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("email" to "Email", "phone" to "Phone").forEach { (id, label) ->
                    val isSelected = loginMethod == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { loginMethod = id; errorMessage = "" }
                            .background(if (isSelected) PrimaryElectricBlue else Color.Transparent)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) BackgroundNavy else OnSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceLowest),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Sign In",
                        fontWeight = FontWeight.Bold, fontSize = 24.sp, color = OnSurface
                    )
                    
                    if (loginMethod == "phone") {
                        Badge(containerColor = SecondaryNeonGreen.copy(alpha = 0.2f), modifier = Modifier.padding(top = 4.dp)) {
                            Text("NEW: SECURE VERIFICATION", color = SecondaryNeonGreen, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = PremiumRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    if (isSignUp) {
                        OutlinedTextField(
                            value = fullName, onValueChange = { fullName = it },
                            label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (loginMethod == "email") {
                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        // Phone Login UI
                        OutlinedTextField(
                            value = phoneNumber, onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number (+84...)") }, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            enabled = !isOtpSent
                        )
                        
                        if (isOtpSent) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = otpCode, onValueChange = { otpCode = it },
                                label = { Text("Enter 6-digit OTP") }, 
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    if (loginMethod == "email" && !isSignUp) {
                        Text(
                            "Quick Access: plitzee@gmail.com / huy123",
                            fontSize = 11.sp, 
                            color = SecondaryNeonGreen, 
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable { 
                                    email = "plitzee@gmail.com"
                                    password = "huy123"
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = ""
                            if (loginMethod == "email") {
                                if (isSignUp) {
                                    viewModel.register(
                                        fullName = fullName.trim(),
                                        email = email.trim(),
                                        pass = password.trim(),
                                        sports = selectedSports.joinToString(","),
                                        onSuccess = {
                                            isLoading = false
                                            onLoginSuccess()
                                        },
                                        onError = { error ->
                                            isLoading = false
                                            errorMessage = error
                                        }
                                    )
                                } else {
                                    viewModel.login(
                                        email = email.trim(),
                                        pass = password.trim(),
                                        onSuccess = {
                                            isLoading = false
                                            onLoginSuccess()
                                        },
                                        onError = { error ->
                                            isLoading = false
                                            errorMessage = error
                                        }
                                    )
                                }
                            } else {
                                // Phone Auth Logic
                                if (!isOtpSent) {
                                    val activity = context as? Activity
                                    if (activity == null) {
                                        isLoading = false
                                        errorMessage = "Activity unavailable for phone verification"
                                    } else {
                                        viewModel.sendOtp(
                                            phoneNumber = phoneNumber.trim(),
                                            activity = activity,
                                            onCodeSent = {
                                                isLoading = false
                                                isOtpSent = true
                                                errorMessage = "Code sent to $phoneNumber"
                                            },
                                            onError = { error ->
                                                isLoading = false
                                                errorMessage = error
                                            }
                                        )
                                    }
                                } else {
                                    viewModel.verifyOtp(
                                        code = otpCode.trim(),
                                        onSuccess = {
                                            isLoading = false
                                            onLoginSuccess()
                                        },
                                        onError = { error ->
                                            isLoading = false
                                            errorMessage = error
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryElectricBlue),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else {
                            val btnText = if (loginMethod == "phone") {
                                if (isOtpSent) "VERIFY & LOGIN" else "SEND OTP"
                            } else {
                                if (isSignUp) "REGISTER" else "LOGIN"
                            }
                            Text(btnText, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    if (isOtpSent) {
                        TextButton(
                            onClick = { isOtpSent = false; otpCode = "" },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Change Phone Number", color = OnSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                if (isSignUp) "Already have an account? Sign In" else "New here? Create Account",
                color = OnSurfaceVariant,
                modifier = Modifier.clickable { isSignUp = !isSignUp; isOtpSent = false }
            )
        }
    }
}
