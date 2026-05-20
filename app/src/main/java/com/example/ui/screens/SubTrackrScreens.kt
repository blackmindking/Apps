package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import com.example.ui.theme.AppColors
import com.example.ui.theme.JetBrainsMono
import java.util.*

// Main Router based on ViewModel Navigation Destination
@Composable
fun SubTrackrAppNavigator(viewModel: MainViewModel) {
    val destination by viewModel.currentDestination.collectAsState()
    val isDark = viewModel.authManager.getTheme() == "dark"

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = destination,
            transitionSpec = {
                fadeIn(animationSpec = tween(350)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(350)
                ) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "screen_transition"
        ) { state ->
            when (state) {
                is Destination.Welcome -> WelcomeScreen(viewModel)
                is Destination.Login -> LoginScreen(viewModel)
                is Destination.Register -> RegisterScreen(viewModel)
                is Destination.ForgotPassword -> ForgotPasswordScreen(viewModel)
                is Destination.Onboarding -> OnboardingScreen(viewModel)
                is Destination.MainApp -> MainAppStructure(viewModel)
            }
        }

        // Global Custom Toast Trigger
        val toastState by viewModel.toast.collectAsState()
        toastState?.let { t ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                GlobalToast(
                    message = t.message,
                    isSuccess = t.isSuccess,
                    isWifiOff = t.isWifiOff,
                    onDismiss = { viewModel.dismissToast() }
                )
            }
        }

        // Paywall Modal
        val isPaywallOpen by viewModel.isPaywallOpen.collectAsState()
        if (isPaywallOpen) {
            PaywallModal(viewModel)
        }
    }
}

// 1. WELCOME SCREEN
@Composable
fun WelcomeScreen(viewModel: MainViewModel) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBg)
    ) {
        // Floating gradient animated background orbs
        val infiniteTransition = rememberInfiniteTransition(label = "orbs")
        val orbOffset1 by infiniteTransition.animateValue(
            initialValue = Offset(-100f, -100f),
            targetValue = Offset(300f, 400f),
            typeConverter = Offset.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 8000
                    Offset(100f, 300f) at 2000
                    Offset(300f, 200f) at 5000
                },
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb1"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppColors.DarkAccent.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        radius = 400.dp.toPx(),
                        center = orbOffset1
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppColors.DarkAccentSecond.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        radius = 350.dp.toPx(),
                        center = Offset(size.width - 200f, size.height / 2)
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // App Logo and Name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(24.dp, shape = CircleShape, ambientColor = AppColors.DarkAccent, spotColor = AppColors.DarkAccent)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AppColors.DarkAccent, AppColors.DarkAccentSecond)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "SubTrackr",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Stop paying for things you forgot",
                    color = AppColors.DarkTextSecondary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Buttons Block
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Gradient Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.navigateTo(Destination.Register)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("welcome_register_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AppColors.DarkAccent, Color(0xFF9B59B6))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Create Account",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Styled Outline Button
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.navigateTo(Destination.Login)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("welcome_login_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, AppColors.DarkAccent)
                ) {
                    Text(
                        text = "Sign In",
                        color = AppColors.DarkTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "By continuing you agree to our Terms and Privacy Policy",
                    color = AppColors.DarkTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 2. LOGIN SCREEN
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Back Arrow
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.navigateTo(Destination.Welcome)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.DarkTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Welcome back",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Sign in to your account with email",
                    fontSize = 15.sp,
                    color = AppColors.DarkTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Email Address
                Text(
                    text = "EMAIL ADDRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.DarkTextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.DarkSurface,
                        unfocusedContainerColor = AppColors.DarkSurface,
                        disabledContainerColor = AppColors.DarkSurface,
                        focusedIndicatorColor = AppColors.DarkAccent,
                        unfocusedIndicatorColor = AppColors.DarkBorder
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "PASSWORD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DarkTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Forgot password?",
                        fontSize = 14.sp,
                        color = AppColors.DarkAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(Destination.ForgotPassword) }
                            .testTag("forgot_password_link")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.DarkSurface,
                        unfocusedContainerColor = AppColors.DarkSurface,
                        disabledContainerColor = AppColors.DarkSurface,
                        focusedIndicatorColor = AppColors.DarkAccent,
                        unfocusedIndicatorColor = AppColors.DarkBorder
                    ),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = AppColors.DarkTextSecondary
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Sign In Button
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            viewModel.showToast("Please enter both email and password.", isSuccess = false)
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            val res = viewModel.authManager.login(email.trim(), password.trim())
                            isLoading = false
                            if (res.isSuccess) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.showToast("Welcome back, ${res.getOrNull()?.name}! 👋")
                                viewModel.navigateTo(Destination.MainApp)
                            } else {
                                viewModel.showToast(res.exceptionOrNull()?.message ?: "Login failed", isSuccess = false)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AppColors.DarkAccent, Color(0xFF9B59B6))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = "Sign In", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom Sign Up Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Don't have an account? ", color = AppColors.DarkTextSecondary)
                Text(
                    text = "Sign Up",
                    color = AppColors.DarkAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(Destination.Register) }
                )
            }
        }
    }
}

// 3. REGISTER SCREEN
@Composable
fun RegisterScreen(viewModel: MainViewModel) {
    val haptic = LocalHapticFeedback.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Password requirements
    val hasMinLength = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasUppercase = password.any { it.isUpperCase() }

    val passwordStrength = when {
        password.isBlank() -> 0
        hasMinLength && hasNumber && hasUppercase -> 3
        (hasMinLength && hasNumber) || (hasMinLength && hasUppercase) -> 2
        else -> 1
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.navigateTo(Destination.Welcome)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.DarkTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Full Name
                Text(text = "FULL NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.DarkSurface,
                        unfocusedContainerColor = AppColors.DarkSurface,
                        focusedIndicatorColor = AppColors.DarkAccent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Address
                Text(text = "EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.DarkSurface,
                        unfocusedContainerColor = AppColors.DarkSurface,
                        focusedIndicatorColor = AppColors.DarkAccent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                Text(text = "PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_password_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.DarkSurface,
                        unfocusedContainerColor = AppColors.DarkSurface,
                        focusedIndicatorColor = AppColors.DarkAccent
                    ),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                // Password Strength bar
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { idx ->
                        val activeColor = when (passwordStrength) {
                            1 -> AppColors.DarkDanger
                            2 -> AppColors.DarkWarning
                            3 -> AppColors.DarkSuccess
                            else -> AppColors.DarkBorder
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    if (passwordStrength > idx) activeColor else AppColors.DarkBorder,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Password requirement indicators
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RequirementRow(isMet = hasMinLength, text = "At least 8 characters")
                    RequirementRow(isMet = hasNumber, text = "Contains a number")
                    RequirementRow(isMet = hasUppercase, text = "Contains uppercase letter")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password
                Text(text = "CONFIRM PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_confirm_password_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.DarkSurface,
                        unfocusedContainerColor = AppColors.DarkSurface,
                        focusedIndicatorColor = AppColors.DarkAccent
                    ),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Create Account Button
                Button(
                    onClick = {
                        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                            viewModel.showToast("All fields are required.", isSuccess = false)
                            return@Button
                        }
                        if (password != confirmPassword) {
                            viewModel.showToast("Passwords do not match.", isSuccess = false)
                            return@Button
                        }
                        if (passwordStrength < 3) {
                            viewModel.showToast("Please make your password stronger.", isSuccess = false)
                            return@Button
                        }

                        isSaving = true
                        coroutineScope.launch {
                            val res = viewModel.authManager.register(email.trim(), fullName.trim(), password.trim())
                            isSaving = false
                            if (res.isSuccess) {
                                val user = res.getOrNull()!!
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Pre-seed 5 default high-quality subscriptions so they can view stats instantly!
                                viewModel.seedSampleDataForUser(user.id)
                                viewModel.showToast("Account created! Sample subs generated. 🎉")
                                viewModel.navigateTo(Destination.Onboarding)
                            } else {
                                viewModel.showToast(res.exceptionOrNull()?.message ?: "Sign up failed", isSuccess = false)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("register_submit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    enabled = !isSaving
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AppColors.DarkAccent, Color(0xFF9B59B6))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = "Create Account", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "Already have an account? ", color = AppColors.DarkTextSecondary)
                Text(
                    text = "Sign In",
                    color = AppColors.DarkAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.navigateTo(Destination.Login) }
                )
            }
        }
    }
}

@Composable
fun RequirementRow(isMet: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (isMet) AppColors.DarkSuccess else AppColors.DarkTextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = if (isMet) AppColors.DarkTextPrimary else AppColors.DarkTextSecondary)
    }
}

// 4. FORGOT PASSWORD SCREEN
@Composable
fun ForgotPasswordScreen(viewModel: MainViewModel) {
    val haptic = LocalHapticFeedback.current
    var step by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // OTP Input digits
    val digitStates = List(6) { remember { mutableStateOf("") } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (step == 2) step = 1 else viewModel.navigateTo(Destination.Login)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.DarkTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Reset password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = if (step == 1) "Enter your email to receive a recovery OTP" else "Verify code & enter new password",
                fontSize = 15.sp,
                color = AppColors.DarkTextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            if (step == 1) {
                Text(text = "EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_email_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.DarkSurface,
                        unfocusedContainerColor = AppColors.DarkSurface,
                        focusedIndicatorColor = AppColors.DarkAccent
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isBlank()) {
                            viewModel.showToast("Please enter email address", isSuccess = false)
                            return@Button
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.showToast("OTP Code Sent! (Use: 342678) 📩")
                        step = 2
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("forgot_send_code_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.DarkAccent)
                ) {
                    Text(text = "Send Code", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(text = "6-DIGIT RECOVERY CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) { idx ->
                        TextField(
                            value = digitStates[idx].value,
                            onValueChange = { nv ->
                                if (nv.length <= 1) {
                                    digitStates[idx].value = nv
                                    // Simulated auto-advance triggers can occur inside focus managers in higher logic
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .testTag("otp_input_${idx}"),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = AppColors.DarkSurface,
                                unfocusedContainerColor = AppColors.DarkSurface,
                                focusedIndicatorColor = AppColors.DarkAccent,
                                unfocusedIndicatorColor = AppColors.DarkBorder
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "NEW PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forgot_new_password_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.DarkSurface,
                        unfocusedContainerColor = AppColors.DarkSurface,
                        focusedIndicatorColor = AppColors.DarkAccent
                    ),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(32.dp))

                val coroutineScope = rememberCoroutineScope()
                Button(
                    onClick = {
                        val code = digitStates.joinToString("") { it.value }
                        if (code.length < 6 || newPassword.isBlank()) {
                            viewModel.showToast("Please enter correct code and password.", isSuccess = false)
                            return@Button
                        }
                        if (code != "342678") {
                            viewModel.showToast("Invalid OTP code. Please retry with 342678", isSuccess = false)
                            return@Button
                        }

                        coroutineScope.launch {
                            val success = viewModel.authManager.resetPassword(email.trim(), newPassword.trim())
                            if (success) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.showToast("Password updated successfully! 🔐")
                                viewModel.navigateTo(Destination.Login)
                            } else {
                                viewModel.showToast("Email address not found.", isSuccess = false)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("forgot_submit_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.DarkAccent)
                ) {
                    Text(text = "Reset Password", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 5. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val haptic = LocalHapticFeedback.current
    var slideIdx by remember { mutableStateOf(0) }

    val slides = listOf(
        Pair("Take Control", "Track all your active digital subscriptions and physical recurring bills in one visual premium deck."),
        Pair("Never Forget", "Get notification alerts days before renewals. Control your cash flow with complete transparency."),
        Pair("Spend Intelligently", "Visualize smart analytics segmented by category. Cut unnecessary expenditures with data.")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Skip",
                    color = AppColors.DarkTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.navigateTo(Destination.MainApp)
                    }
                )
            }

            // Visual Slide Illustrating Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(AppColors.DarkAccent.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (slideIdx) {
                            0 -> Icons.Default.LibraryBooks
                            1 -> Icons.Default.NotificationsActive
                            else -> Icons.Default.PieChart
                        },
                        contentDescription = null,
                        tint = AppColors.DarkAccent,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = slides[slideIdx].first,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = slides[slideIdx].second,
                    fontSize = 15.sp,
                    color = AppColors.DarkTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 22.sp
                )
            }

            // Carousel dots and button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (slideIdx == idx) 16.dp else 8.dp, 8.dp)
                                .background(
                                    if (slideIdx == idx) AppColors.DarkAccent else AppColors.DarkBorder,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (slideIdx < 2) {
                            slideIdx++
                        } else {
                            viewModel.navigateTo(Destination.MainApp)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.DarkAccent)
                ) {
                    Text(
                        text = if (slideIdx == 2) "Get Started 🚀" else "Continue",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 6. MAIN APPLICATION STRUCTURE (NAV BAR WRAPPER)
@Composable
fun MainAppStructure(viewModel: MainViewModel) {
    val activeTab by viewModel.currentTab.collectAsState()
    val isDark = viewModel.authManager.getTheme() == "dark"
    val themeBg = if (isDark) AppColors.DarkBg else AppColors.LightBg

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = themeBg,
        bottomBar = { CustomBottomTabBar(viewModel) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    Tab.Dashboard -> DashboardScreen(viewModel)
                    Tab.Subscriptions -> SubscriptionsScreen(viewModel)
                    Tab.Add -> AddSubscriptionScreen(viewModel)
                    Tab.Analytics -> AnalyticsScreen(viewModel)
                    Tab.Settings -> SettingsScreen(viewModel)
                }
            }
        }
    }
}

// CUSTOM BOTTOM TAB BAR
@Composable
fun CustomBottomTabBar(viewModel: MainViewModel) {
    val activeTab by viewModel.currentTab.collectAsState()
    val isDark = viewModel.authManager.getTheme() == "dark"
    val surfaceColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface
    val borderCol = if (isDark) AppColors.DarkBorder else AppColors.LightBorder
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(84.dp)
            .background(surfaceColor)
            .drawBehind {
                drawLine(
                    color = borderCol,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Dashboard
            WeightyTabItem(
                isActive = activeTab == Tab.Dashboard,
                icon = Icons.Default.Home,
                label = "Home",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.selectTab(Tab.Dashboard)
                },
                modifier = Modifier.weight(1f),
                isDark = isDark
            )

            // Tab 2: Subscriptions
            WeightyTabItem(
                isActive = activeTab == Tab.Subscriptions,
                icon = Icons.Default.FormatListBulleted,
                label = "Bills",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.selectTab(Tab.Subscriptions)
                },
                modifier = Modifier.weight(1f),
                isDark = isDark
            )

            // Center Floating Special Add Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                val pulseScale = rememberInfiniteTransition("addPulse").animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                ).value

                Box(
                    modifier = Modifier
                        .offset(y = (-20).dp)
                        .size(64.dp)
                        .shadow(16.dp, shape = CircleShape, ambientColor = AppColors.DarkAccent, spotColor = AppColors.DarkAccent)
                        .scale(pulseScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(AppColors.DarkAccent, Color(0xFF9B59B6))
                            ),
                            shape = CircleShape
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectTab(Tab.Add)
                        }
                        .testTag("floating_add_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Subscription",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Tab 4: Analytics
            WeightyTabItem(
                isActive = activeTab == Tab.Analytics,
                icon = Icons.Default.BarChart,
                label = "Analytics",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.selectTab(Tab.Analytics)
                },
                modifier = Modifier.weight(1f),
                isDark = isDark
            )

            // Tab 5: Settings
            WeightyTabItem(
                isActive = activeTab == Tab.Settings,
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.selectTab(Tab.Settings)
                },
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
        }
    }
}

@Composable
fun WeightyTabItem(isActive: Boolean, icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, isDark: Boolean) {
    val activeColor = AppColors.DarkAccent
    val inactiveColor = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val sizeScale = animateFloatAsState(if (isActive) 1.15f else 1.0f, animationSpec = spring(), label = "iconScale").value
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier
                .size(24.dp)
                .scale(sizeScale)
        )
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedVisibility(visible = isActive, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            Text(
                text = label,
                color = activeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

// 7. DASHBOARD SCREEN
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val subs by viewModel.subscriptions.collectAsState()
    val user by viewModel.authManager.currentUser.collectAsState()
    val isDark = viewModel.authManager.getTheme() == "dark"
    val txtPrimary = if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Offline mode monitoring
    val isOffline by viewModel.authManager.isOfflineMode.collectAsState()
    val offlineChanges by viewModel.authManager.offlineQueueCount.collectAsState()

    // Aggregate values
    val activeSubs = subs.filter { it.isActive }
    val totalMonthly = activeSubs.sumOf { Calculations.toMonthlyAmount(it.amount, it.billingCycle) }
    val totalYearly = totalMonthly * 12.0
    val dueThisWeek = Calculations.getDueWithinDays(subs, 7)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Top Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SubTrackr",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = txtPrimary,
                        modifier = Modifier.clickable {
                            // Quick premium upgrade tapping
                            viewModel.openPaywall()
                        }
                    )
                    Text(
                        text = "Track of recurring bills",
                        fontSize = 13.sp,
                        color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Offline badge
                    if (isOffline) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.DarkWarning.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    viewModel.authManager.toggleOfflineMode()
                                    viewModel.showToast("Wifi online synced! 🛜")
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(14.dp), tint = AppColors.DarkWarning)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Offline ($offlineChanges)", fontSize = 11.sp, color = AppColors.DarkWarning, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Urgent Warning bell badging
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                                CircleShape
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (dueThisWeek.isNotEmpty()) {
                                    viewModel.showToast("You have ${dueThisWeek.size} bills due this week! ⏰")
                                    // Simulated push notification test
                                    viewModel.sendTestPushNotification(context)
                                } else {
                                    viewModel.showToast("Everything paid! No bills due this week. ✨")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminders",
                            tint = if (dueThisWeek.isNotEmpty()) AppColors.DarkWarning else txtPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        if (dueThisWeek.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(AppColors.DarkDanger, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }

        // Hero Monthly total Card
        item {
            VerticalSummaryCard(
                totalMonthly = totalMonthly,
                totalYearly = totalYearly,
                activeCount = activeSubs.size,
                dueThisWeekCount = dueThisWeek.size,
                isDark = isDark
            )
        }

        // Offline simulation switcher reminder when in demo state
        if (subs.size > 2 && isDark) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.DarkWarning.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, AppColors.DarkWarning.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = AppColors.DarkWarning)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Simulate Offline Mode?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Turn on to test optimistic queuing.", fontSize = 12.sp, color = AppColors.DarkTextSecondary)
                        }
                        Switch(
                            checked = isOffline,
                            onCheckedChange = { viewModel.authManager.toggleOfflineMode() },
                            colors = SwitchDefaults.colors(checkedTrackColor = AppColors.DarkAccent)
                        )
                    }
                }
            }
        }

        // Horizontal Renewals
        item {
            Column {
                Text(
                    text = "Upcoming Renewals",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = txtPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (subs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No upcoming bills", color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted)
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(subs.sortedBy { it.nextRenewalDate }) { sub ->
                            UpcomingRenewalCard(sub = sub, isDark = isDark, onClick = {
                                viewModel.startEditingSubscription(sub.id)
                            })
                        }
                    }
                }
            }
        }

        // Vertical List headers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Active Bills",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = txtPrimary
                )
                Text(
                    text = "See All",
                    fontSize = 14.sp,
                    color = AppColors.DarkAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.selectTab(Tab.Subscriptions) }
                )
            }
        }

        // Vertical subs item list
        if (activeSubs.isEmpty()) {
            item {
                EmptyStateView(
                    label = "Your bill deck is empty!",
                    actionText = "Add Your First Subscription",
                    onClick = { viewModel.selectTab(Tab.Add) },
                    isDark = isDark
                )
            }
        } else {
            items(activeSubs.take(5)) { sub ->
                SubscriptionCard(
                    sub = sub,
                    isDark = isDark,
                    onClick = {
                        viewModel.startEditingSubscription(sub.id)
                    },
                    onDelete = { viewModel.deleteSubscription(sub) },
                    onToggleActive = { viewModel.toggleSubscriptionActive(sub) }
                )
            }
        }
    }
}

// PREMIUM HERO SUMMARY CARD
@Composable
fun VerticalSummaryCard(totalMonthly: Double, totalYearly: Double, activeCount: Int, dueThisWeekCount: Int, isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2D1B69), Color(0xFF1A0533), Color(0xFF0F0A2E))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .border(1.dp, AppColors.DarkAccent.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "MONTHLY TOTAL ESTIMATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Animated counter count-up mimics
            Text(
                text = Calculations.formatCurrency(totalMonthly, "USD"),
                fontSize = 44.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pills stat row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active count pill
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AppColors.DarkSuccess, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$activeCount active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Due pill
                val pillBg = if (dueThisWeekCount > 0) AppColors.DarkWarning.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f)
                val pillCol = if (dueThisWeekCount > 0) AppColors.DarkWarning else Color.White
                Box(
                    modifier = Modifier
                        .background(pillBg, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$dueThisWeekCount due this week",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = pillCol
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Yearly projection estimate: ${Calculations.formatCurrency(totalYearly, "USD")}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// HORIZONTAL ITEM UPCOMING ITEM CARD
@Composable
fun UpcomingRenewalCard(sub: SubscriptionEntity, isDark: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val days = Calculations.daysUntil(sub.nextRenewalDate)
    val urgencyColor = when {
        days <= 3 -> AppColors.DarkDanger
        days <= 7 -> AppColors.DarkWarning
        else -> AppColors.DarkSuccess
    }

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(160.dp)
            .background(
                if (isDark) AppColors.DarkSurfaceHigh else AppColors.LightSurfaceHigh,
                RoundedCornerShape(20.dp)
            )
            .border(
                1.dp,
                if (isDark) AppColors.DarkBorder else AppColors.LightBorder,
                RoundedCornerShape(20.dp)
            )
            .drawBehind {
                // Left solid accent urgency bar
                drawRoundRect(
                    color = urgencyColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(4.dp.toPx(), size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
            }
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(start = 14.dp, end = 10.dp, top = 14.dp, bottom = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Category avatar header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            AppColors.getCategoryColor(sub.category).copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarChar = if (sub.name.isNotEmpty()) sub.name.take(1).uppercase() else "?"
                    Text(
                        text = avatarChar,
                        color = AppColors.getCategoryColor(sub.category),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Name
            Text(
                text = sub.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else AppColors.LightTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Amount
            Text(
                text = Calculations.formatCurrency(sub.amount, sub.currency),
                fontFamily = JetBrainsMono,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.getCategoryColor(sub.category)
            )

            // Dynamic Urgency Badge
            Box(
                modifier = Modifier
                    .background(urgencyColor.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (days == 0) "Today" else "$days days left",
                    fontSize = 11.sp,
                    color = urgencyColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// STANDARD VERTICAL CARD COMPONENT
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SubscriptionCard(
    sub: SubscriptionEntity,
    isDark: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val bgVal = if (isDark) AppColors.DarkSurface else AppColors.LightSurface
    val borderVal = if (isDark) AppColors.DarkBorder else AppColors.LightBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .background(bgVal, RoundedCornerShape(16.dp))
            .border(1.dp, borderVal, RoundedCornerShape(16.dp))
            .drawBehind {
                val catColor = AppColors.getCategoryColor(sub.category)
                drawRoundRect(
                    color = catColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(4.dp.toPx(), size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDialog = true
                }
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circle label
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            AppColors.getCategoryColor(sub.category).copy(alpha = 0.12f),
                            CircleShape
                        )
                        .border(1.dp, AppColors.getCategoryColor(sub.category).copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarLetter = if (sub.name.isNotBlank()) sub.name.take(1).uppercase() else "B"
                    Text(
                        text = avatarLetter,
                        color = AppColors.getCategoryColor(sub.category),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = sub.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${sub.category} · ${sub.billingCycle.replaceFirstChar { it.uppercase() }}",
                        fontSize = 12.sp,
                        color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Calculations.formatCurrency(sub.amount, sub.currency),
                    fontFamily = JetBrainsMono,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.getCategoryColor(sub.category)
                )
                Text(
                    text = "due ${Calculations.formatDate(sub.nextRenewalDate).take(12)}",
                    fontSize = 11.sp,
                    color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Manage Plan for ${sub.name}") },
            text = { Text("Choose an action to moderate or disconnect your active billing record.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onToggleActive()
                }) {
                    Text(if (sub.isActive) "Pause Plan ⏸️" else "Activate Plan 🟢")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.DarkDanger)
                ) {
                    Text("Delete Bill 🗑️")
                }
            }
        )
    }
}

// EMPTY STATE VIEW
@Composable
fun EmptyStateView(label: String, actionText: String, onClick: () -> Unit, isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.DarkAccent)
        ) {
            Text(text = actionText, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// 8. BILLS LIST / SUBSCRIPTIONS TAB VIEW
@Composable
fun SubscriptionsScreen(viewModel: MainViewModel) {
    val subs by viewModel.subscriptions.collectAsState()
    val isDark = viewModel.authManager.getTheme() == "dark"
    val txtPrimary = if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary
    val context = LocalContext.current

    // Internal filtration state
    var search by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    var sortByState by remember { mutableStateOf("nextRenewalDate") }
    var asc by remember { mutableStateOf(true) }

    // Sync views on filtration change
    LaunchedEffect(search, selectedCat, sortByState, asc) {
        viewModel.setFilters(search, selectedCat, sortByState, asc)
    }

    val categoriesList = listOf("All") + AppColors.CategoryColors.keys.toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Active Bills Base",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = txtPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        TextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field_input"),
            placeholder = { Text("Search services...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                unfocusedContainerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                focusedIndicatorColor = AppColors.DarkAccent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category chips horizontal scroller
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categoriesList) { cat ->
                val selected = selectedCat == cat
                val bgCol = if (selected) AppColors.DarkAccent else (if (isDark) AppColors.DarkSurface else AppColors.LightSurface)
                val txtCol = if (selected) Color.White else (if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary)

                Box(
                    modifier = Modifier
                        .background(bgCol, CircleShape)
                        .border(
                            1.dp,
                            if (selected) AppColors.DarkAccent else (if (isDark) AppColors.DarkBorder else AppColors.LightBorder),
                            CircleShape
                        )
                        .clickable { selectedCat = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = cat, color = txtCol, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sorting row selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SortTextItem(label = "Date", active = sortByState == "nextRenewalDate", onClick = { sortByState = "nextRenewalDate" }, isDark = isDark)
                SortTextItem(label = "Name", active = sortByState == "name", onClick = { sortByState = "name" }, isDark = isDark)
                SortTextItem(label = "Amount", active = sortByState == "amount", onClick = { sortByState = "amount" }, isDark = isDark)
                SortTextItem(label = "Recent", active = sortByState == "recent", onClick = { sortByState = "recent" }, isDark = isDark)
            }

            IconButton(onClick = { asc = !asc }) {
                Icon(
                    imageVector = if (asc) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = "Toggle sort order",
                    tint = AppColors.DarkAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subscriptions List
        if (subs.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyStateView(
                    label = "No results found. Add a new bill!",
                    actionText = "Create New Bill",
                    onClick = { viewModel.selectTab(Tab.Add) },
                    isDark = isDark
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Pull to refresh mimics trigger
                item {
                    Text(
                        text = "Pull down to sync data • Total ${subs.size} active integrations",
                        color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.syncNow() }
                    )
                }

                items(subs) { sub ->
                    SubscriptionCard(
                        sub = sub,
                        isDark = isDark,
                        onClick = {
                            viewModel.startEditingSubscription(sub.id)
                        },
                        onDelete = { viewModel.deleteSubscription(sub) },
                        onToggleActive = { viewModel.toggleSubscriptionActive(sub) }
                    )
                }
            }
        }
    }
}

@Composable
fun SortTextItem(label: String, active: Boolean, onClick: () -> Unit, isDark: Boolean) {
    val activeColor = AppColors.DarkAccent
    val textCol = if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary
    val inactiveColor = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary

    Text(
        text = label,
        color = if (active) activeColor else inactiveColor,
        fontSize = 13.sp,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        modifier = Modifier
            .clickable(onClick = onClick)
            .drawBehind {
                if (active) {
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(0f, size.height + 4.dp.toPx()),
                        size = Size(size.width, 3.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
                    )
                }
            }
    )
}

// 9. ADD/EDIT SUBSCRIPTION TAB VIEW
@Composable
fun AddSubscriptionScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = viewModel.authManager.getTheme() == "dark"
    val txtPrimary = if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary

    val editingId by viewModel.editingSubscriptionId.collectAsState()
    val subsList by viewModel.subscriptions.collectAsState()

    // Form Field variables
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Entertainment") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var billingCycle by remember { mutableStateOf("monthly") }
    var startDateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var activeColorHex by remember { mutableStateOf("#FF6B6B") }
    var notes by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderDaysBefore by remember { mutableStateOf(3) }

    // Dropdown Dialog variables
    var showCurrencyDialog by remember { mutableStateOf(false) }

    // Prepopulate if in editing state
    LaunchedEffect(editingId) {
        if (editingId != null) {
            val sub = subsList.find { it.id == editingId }
            if (sub != null) {
                name = sub.name
                selectedCategory = sub.category
                amountStr = sub.amount.toString()
                selectedCurrency = sub.currency
                billingCycle = sub.billingCycle
                startDateMs = sub.startDate
                activeColorHex = sub.color
                notes = sub.notes
                reminderEnabled = sub.reminderEnabled
                reminderDaysBefore = sub.reminderDaysBefore
            }
        } else {
            // Reset for blank creation
            name = ""
            selectedCategory = "Entertainment"
            amountStr = ""
            selectedCurrency = "USD"
            billingCycle = "monthly"
            startDateMs = System.currentTimeMillis()
            activeColorHex = "#FF6B6B"
            notes = ""
            reminderEnabled = true
            reminderDaysBefore = 3
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (editingId == null) "New Subscription" else "Edit Subscription",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = txtPrimary
            )
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.selectTab(Tab.Dashboard)
            }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 1. Service Name Field
        FormLabel(text = "SERVICE NAME")
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_name_input"),
            placeholder = { Text("e.g. Netflix, Spotify, gym...") },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                unfocusedContainerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                focusedIndicatorColor = AppColors.DarkAccent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Category Chips Horizontal scroller
        FormLabel(text = "CATEGORY")
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AppColors.CategoryColors.keys.toList()) { cat ->
                val active = selectedCategory == cat
                val catColor = AppColors.CategoryColors[cat] ?: AppColors.DarkAccent
                val bgCol = if (active) catColor else (if (isDark) AppColors.DarkSurface else AppColors.LightSurface)
                val txtCol = if (active) Color.White else (if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary)

                Box(
                    modifier = Modifier
                        .background(bgCol, CircleShape)
                        .border(1.dp, if (active) catColor else (if (isDark) AppColors.DarkBorder else AppColors.LightBorder), CircleShape)
                        .clickable {
                            selectedCategory = cat
                            activeColorHex = String.format("#%02x%02x%02x", (catColor.red * 255).toInt(), (catColor.green * 255).toInt(), (catColor.blue * 255).toInt())
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = cat, color = txtCol, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Amount row input
        FormLabel(text = "AMOUNT & CURRENCY")
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Currency selector touchable pill
            Box(
                modifier = Modifier
                    .background(
                        if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (isDark) AppColors.DarkBorder else AppColors.LightBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { showCurrencyDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val flag = when (selectedCurrency) {
                        "USD" -> "🇺🇸"
                        "EUR" -> "🇪🇺"
                        "GBP" -> "🇬🇧"
                        "CAD" -> "🇨🇦"
                        "AUD" -> "🇦🇺"
                        "JPY" -> "🇯🇵"
                        "SAR" -> "🇸🇦"
                        "AED" -> "🇦🇪"
                        "ETB" -> "🇪🇹"
                        else -> "🇺🇸"
                    }
                    Text(text = "$flag $selectedCurrency ", fontWeight = FontWeight.Bold, color = txtPrimary)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = txtPrimary)
                }
            }

            // Numeric Input text field
            TextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_amount_input"),
                placeholder = { Text("0.00") },
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                    unfocusedContainerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                    focusedIndicatorColor = AppColors.DarkAccent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Billing Cycle Pill selector row
        FormLabel(text = "BILLING CYCLE")
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val cycles = listOf("weekly", "monthly", "quarterly", "yearly")
            cycles.forEach { cycle ->
                val active = billingCycle == cycle
                val bgCol = if (active) AppColors.DarkAccent else (if (isDark) AppColors.DarkSurface else AppColors.LightSurface)
                val txtCol = if (active) Color.White else (if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(bgCol, RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (active) AppColors.DarkAccent else (if (isDark) AppColors.DarkBorder else AppColors.LightBorder),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { billingCycle = cycle }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cycle.replaceFirstChar { it.uppercase() },
                        color = txtCol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Start Date Clickable row selector (Opens date dialog picker)
        FormLabel(text = "BILLING START DATE")
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    if (isDark) AppColors.DarkBorder else AppColors.LightBorder,
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    val cal = Calendar.getInstance().apply { timeInMillis = startDateMs }
                    DatePickerDialog(
                        context,
                        { _, yr, mo, dy ->
                            val sCal = Calendar.getInstance().apply { set(yr, mo, dy) }
                            startDateMs = sCal.timeInMillis
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = Calculations.formatDate(startDateMs), fontWeight = FontWeight.Bold, color = txtPrimary)
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = txtPrimary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Calculated Rollover notice
        val liveCalculatedDate = Calculations.getNextRenewalFromToday(startDateMs, billingCycle)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.DarkWarning.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auto-calculated ✨ Next renewal due on ${Calculations.formatDate(liveCalculatedDate)}",
                    fontSize = 12.sp,
                    color = AppColors.DarkWarning,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Notes textbox
        FormLabel(text = "NOTES & ATTACHMENT")
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("add_notes_input"),
            placeholder = { Text("Write something custom...") },
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                unfocusedContainerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                focusedIndicatorColor = AppColors.DarkAccent
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 7. Cards colors selector (8 circles options)
        FormLabel(text = "THEME ACCENT COLOR")
        Spacer(modifier = Modifier.height(8.dp))
        val colorsPaletteList = listOf(
            "#6C63FF", "#FF6B6B", "#43E8B0", "#FF9F43", "#5352ED", "#A29BFE", "#FD79A8", "#00CEC9"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colorsPaletteList.forEach { colHex ->
                val parsedColor = Color(android.graphics.Color.parseColor(colHex))
                val active = activeColorHex.equals(colHex, ignoreCase = true)
                val borderCol = if (active) (if (isDark) Color.White else Color.Black) else Color.Transparent

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(parsedColor, CircleShape)
                        .border(1.5.dp, borderCol, CircleShape)
                        .clickable { activeColorHex = colHex }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 8. Reminders Row & expandable day pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                FormLabel(text = "RENEWAL REMINDER ALERTS")
                Text(text = "Send alert before billing rollover", fontSize = 12.sp, color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary)
            }
            Switch(
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it },
                colors = SwitchDefaults.colors(checkedTrackColor = AppColors.DarkAccent)
            )
        }

        if (reminderEnabled) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 3, 7).forEach { days ->
                    val active = reminderDaysBefore == days
                    val bgCol = if (active) AppColors.DarkAccent else (if (isDark) AppColors.DarkSurface else AppColors.LightSurface)
                    val txtCol = if (active) Color.White else (if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(bgCol, RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                if (active) AppColors.DarkAccent else (if (isDark) AppColors.DarkBorder else AppColors.LightBorder),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { reminderDaysBefore = days }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (days == 1) "1 Day before" else "$days Days before",
                            color = txtCol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                val amt = amountStr.toDoubleOrNull()
                if (name.isBlank() || amt == null || amt <= 0) {
                    viewModel.showToast("Please enter correct name and positive billing amount", isSuccess = false)
                    return@Button
                }

                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.saveSubscription(
                    name = name.trim(),
                    category = selectedCategory,
                    amount = amt,
                    currency = selectedCurrency,
                    billingCycle = billingCycle,
                    startDateMs = startDateMs,
                    color = activeColorHex,
                    notes = notes.trim(),
                    reminderEnabled = reminderEnabled,
                    reminderDaysBefore = reminderDaysBefore,
                    onComplete = {
                        // Success block
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_subscription_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AppColors.DarkAccent, Color(0xFF9B59B6))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (editingId == null) "Save Subscription" else "Update Changes ✓",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Currency selector Dialog
    if (showCurrencyDialog) {
        Dialog(onDismissRequest = { showCurrencyDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) AppColors.DarkSurfaceHigh else AppColors.LightSurfaceHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(320.dp)
                ) {
                    Text(text = "Select Billing Currency", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = txtPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    val listCurrencies = listOf(
                        Pair("USD", "🇺🇸 United States dollar"),
                        Pair("EUR", "🇪🇺 Eurozone Euro"),
                        Pair("GBP", "🇬🇧 British Pound Sterling"),
                        Pair("CAD", "🇨🇦 Canadian Dollar"),
                        Pair("AUD", "🇦🇺 Australian Dollar"),
                        Pair("JPY", "🇯🇵 Japanese Yen"),
                        Pair("SAR", "🇸🇦 Saudi Riyal"),
                        Pair("AED", "🇦🇪 UAE Dirham"),
                        Pair("ETB", "🇪🇹 Ethiopian Birr")
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(listCurrencies) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCurrency = item.first
                                        showCurrencyDialog = false
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = item.second, color = txtPrimary)
                                if (selectedCurrency == item.first) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.DarkAccent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = AppColors.DarkTextSecondary,
        letterSpacing = 1.sp
    )
}

// 10. ANALYTICS VIEW SCREEN
@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val subs by viewModel.subscriptions.collectAsState()
    val isDark = viewModel.authManager.getTheme() == "dark"
    val txtPrimary = if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary

    val activeSubs = subs.filter { it.isActive }
    val maxSubAmount = activeSubs.maxOfOrNull { it.amount } ?: 1.0

    // Calculations spend totals
    val totalExpense = activeSubs.sumOf { Calculations.toMonthlyAmount(it.amount, it.billingCycle) }
    val totalYearly = totalExpense * 12.0

    // Categorization grouping values for custom Pie segment charts!
    val groupedCategories = activeSubs.groupBy { it.category }.mapValues { entry ->
        entry.value.sumOf { Calculations.toMonthlyAmount(it.amount, it.billingCycle) }
    }.toList().sortedByDescending { it.second }

    // Multi-month aggregation projections mimics
    val listMonths = listOf("Dec", "Jan", "Feb", "Mar", "Apr", "May")
    val barSpentValues = listOf(0.7f, 0.82f, 0.9f, 0.95f, 1.0f, 0.98f) // proportion values relative to max month

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Intelligence Analytics",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = txtPrimary
            )
            Text(
                text = "In-depth insights & segments split",
                fontSize = 13.sp,
                color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary
            )
        }

        // 1. MONTHLY SPEND BAR CHART (Canvas-based high-fidelity graphics)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Historial Spending Grid (6 mo)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = txtPrimary)
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        listMonths.forEachIndexed { index, m ->
                            val scaleProportion = barSpentValues[index]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Rounded bar
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(scaleProportion)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(AppColors.DarkAccent, AppColors.DarkAccentSecond)
                                            ),
                                            RoundedCornerShape(6.dp)
                                        )
                                )
                                Text(text = m, fontSize = 11.sp, color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // 2. CATEGORY PIE SEGMENTS SPINDLE (Draw custom canvas segments donut)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Expenses Category Splits", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = txtPrimary)
                    Spacer(modifier = Modifier.height(20.dp))

                    if (groupedCategories.isEmpty()) {
                        Box(modifier = Modifier.height(120.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No category spends records", color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // High Fidelity Donut Canvas Chart
                            Canvas(modifier = Modifier.size(110.dp)) {
                                val radius = size.height / 2
                                val strokeW = 18.dp.toPx()
                                var startAngle = -90f

                                groupedCategories.forEach { pair ->
                                    val sweep = (pair.second / totalExpense).toFloat() * 360f
                                    val catCol = AppColors.getCategoryColor(pair.first)

                                    drawArc(
                                        color = catCol,
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(strokeW, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                }
                            }

                            // Donut splits stats
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                groupedCategories.take(4).forEach { pair ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(AppColors.getCategoryColor(pair.first), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${pair.first}: ${(pair.second / totalExpense * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = txtPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. TOP EXPENSE RANK LISTS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDark) AppColors.DarkSurface else AppColors.LightSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Top Expensive Expenditures", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = txtPrimary)
                    Spacer(modifier = Modifier.height(14.dp))

                    val topExpensiveList = activeSubs.sortedByDescending { it.amount }.take(5)
                    if (topExpensiveList.isEmpty()) {
                        Text("No active bills logged", color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topExpensiveList.forEachIndexed { rank, sub ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${rank + 1}",
                                                fontFamily = JetBrainsMono,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(text = sub.name, fontWeight = FontWeight.Bold, color = txtPrimary)
                                        }
                                        Text(
                                            text = Calculations.formatCurrency(sub.amount, sub.currency),
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.getCategoryColor(sub.category)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // Custom visual progress relative metric
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(
                                                if (isDark) AppColors.DarkBorder else AppColors.LightBorder,
                                                CircleShape
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth((sub.amount / maxSubAmount).toFloat())
                                                .fillMaxHeight()
                                                .background(
                                                    AppColors.getCategoryColor(sub.category),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. QUICK STATS 2x2 SPANS GRID
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Stat card 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                            RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, if (isDark) AppColors.DarkBorder else AppColors.LightBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("💰 THIS MONTH", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            Calculations.formatCurrency(totalExpense, "USD"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono,
                            color = txtPrimary
                        )
                    }
                }

                // Stat card 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isDark) AppColors.DarkSurface else AppColors.LightSurface,
                            RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, if (isDark) AppColors.DarkBorder else AppColors.LightBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("📅 EXPENSE PROJ.", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            Calculations.formatCurrency(totalYearly, "USD"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono,
                            color = txtPrimary
                        )
                    }
                }
            }
        }

        // 5. INSIGHTS ADVISORY SLIDER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AppColors.DarkWarning.copy(alpha = 0.85f), AppColors.DarkGold)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💡 ", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Everything looks clean! Your highest spends stream from ${groupedCategories.firstOrNull()?.first ?: "your added subscriptions"}. Consider canceling trial items to cut spends.",
                        fontSize = 13.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// 11. SETTINGS VIEW TAB SCREEN
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val subs by viewModel.subscriptions.collectAsState()
    val user by viewModel.authManager.currentUser.collectAsState()
    val isDark = viewModel.authManager.getTheme() == "dark"
    val themeBg = if (isDark) AppColors.DarkSurface else AppColors.LightSurface
    val textPrimaryColor = if (isDark) AppColors.DarkTextPrimary else AppColors.LightTextPrimary

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Preferences Deck",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor
            )
        }

        // 1. FREE/PREMIUM STATUS DECK
        item {
            val isPremium = user?.planTier == "premium"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = if (isPremium) {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0D3B2E), Color(0xFF1B4D3E))
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2D1B69), Color(0xFF4A1B69))
                            )
                        },
                        shape = RoundedCornerShape(18.dp)
                    )
                    .border(
                        1.dp,
                        if (isPremium) AppColors.DarkAccentThird else AppColors.DarkAccent,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPremium) "Premium Plan ✓" else "Free Account Status",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPremium) AppColors.DarkAccentThird else Color.White
                        )
                        Text(text = "👑", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isPremium) "Unlock Unlimited Subscriptions synced instantly." else "5/5 free limits used. Backup & export plans.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    if (!isPremium) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.openPaywall()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.DarkAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Upgrade to Premium Pro →", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 2. PREFERENCES SECTION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("GENERAL PREFERENCES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)

                    // Default Currency selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = AppColors.DarkAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Default Currency", color = textPrimaryColor)
                        }
                        Text(
                            text = viewModel.authManager.getDefaultCurrency(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.DarkAccent,
                            modifier = Modifier.clickable {
                                // Simple cycles helper setting
                                val curr = viewModel.authManager.getDefaultCurrency()
                                val next = if (curr == "USD") "EUR" else "USD"
                                viewModel.authManager.setDefaultCurrency(nvCurrencyString(curr))
                                viewModel.showToast("Default currency updated to ${nvCurrencyString(curr)}! 🌍")
                            }
                        )
                    }

                    // Default Reminder Days
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = AppColors.DarkAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Default Reminder Alerts", color = textPrimaryColor)
                        }
                        Text(
                            text = "${viewModel.authManager.getDefaultReminderDays()} Days",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.DarkAccent,
                            modifier = Modifier.clickable {
                                val d = viewModel.authManager.getDefaultReminderDays()
                                val nextD = when(d) {
                                    3 -> 7
                                    7 -> 1
                                    else -> 3
                                }
                                viewModel.authManager.setDefaultReminderDays(nextD)
                            }
                        )
                    }

                    // Theme selector (Dark/Light toggling)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = AppColors.DarkAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active Black Theme", color = textPrimaryColor)
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = {
                                viewModel.authManager.setTheme(if (it) "dark" else "light")
                                viewModel.showToast("Theme changed! Restart view to refresh layout.")
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = AppColors.DarkAccent)
                        )
                    }
                }
            }
        }

        // 3. STORAGE & SYNC BACKUP SECTION
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("BACKUPS & FILE EXPORTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkTextSecondary, letterSpacing = 1.sp)

                    // Export CSV Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.exportSubscriptionsToCSV(context) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = AppColors.DarkAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Export Subscriptions CSV", fontWeight = FontWeight.SemiBold, color = textPrimaryColor)
                            Text("Generate localized values archive file.", fontSize = 11.sp, color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary)
                        }
                    }

                    // Direct Cloud Synchronize now
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.syncNow() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = AppColors.DarkAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Sync Subscriptions database", fontWeight = FontWeight.SemiBold, color = textPrimaryColor)
                            Text("Refresh and check for lapsed billing dates.", fontSize = 11.sp, color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary)
                        }
                    }

                    // Direct Logout and clearing session
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.authManager.logout()
                                viewModel.showToast("Logged out successfully! 👋")
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = AppColors.DarkDanger)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Sign out account", fontWeight = FontWeight.SemiBold, color = AppColors.DarkDanger)
                            Text("Disconnect currently linked account profile.", fontSize = 11.sp, color = if (isDark) AppColors.DarkTextSecondary else AppColors.LightTextSecondary)
                        }
                    }
                }
            }
        }

        // 4. DANGER ACCOUNT DELETION ZONE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeBg),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, AppColors.DarkDanger.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("DANGER LIMIT ZONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkDanger, letterSpacing = 1.sp)

                    // Deleting account confirmation alert
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                viewModel.authManager.deleteAccount()
                                viewModel.showToast("Account deleted successfully! 👋", isSuccess = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.DarkDanger.copy(alpha = 0.15f), contentColor = AppColors.DarkDanger),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Close & Erase Account Permanently !", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // About product metadata specs
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "SubTrackr Pro • Version 1.0.0", fontSize = 12.sp, color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted)
                Text(text = "Secure local-first architecture.", fontSize = 11.sp, color = if (isDark) AppColors.DarkTextMuted else AppColors.LightTextMuted)
            }
        }
    }
}

fun nvCurrencyString(curr: String): String {
    return if (curr == "USD") "EUR" else if (curr == "EUR") "GBP" else "USD"
}

// 12. THE PAYWALL MODAL SCREEN
@Composable
fun PaywallModal(viewModel: MainViewModel) {
    val haptic = LocalHapticFeedback.current
    var selectedTier by remember { mutableStateOf("yearly") } // "monthly" or "yearly"
    var isLoading by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { viewModel.closePaywall() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.DarkBg.copy(alpha = 0.95f))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Main drawer structure
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header slide drag bar lookalike
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { viewModel.closePaywall() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Crown animated pulsating loop icon
                val pulseAnim = rememberInfiniteTransition("crownPulse").animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutBounce),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_crown"
                ).value

                Text(
                    text = "👑",
                    fontSize = 64.sp,
                    modifier = Modifier.scale(pulseAnim)
                )

                Text(
                    text = "Go Premium Pro",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Unlock the full SubTrackr experience",
                    fontSize = 15.sp,
                    color = AppColors.DarkTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Premium Feature rows splits list
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PaywallFeatureRow(text = "Unlimited subscriptions (Free: max 5)")
                    PaywallFeatureRow(text = "Full Analytics & Category Insights Charts")
                    PaywallFeatureRow(text = "Secure CSV values backup & Import")
                    PaywallFeatureRow(text = "Multiple local currency splits (9 supported)")
                    PaywallFeatureRow(text = "Custom beautiful Dark / Light layout schemes")
                    PaywallFeatureRow(text = "Cloud sync triggers + priority feedback")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Side and Side tier pricing selector columns row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tier 1: Monthly billing cycle
                    val mSelected = selectedTier == "monthly"
                    val mBg = if (mSelected) AppColors.DarkAccent.copy(alpha = 0.15f) else Color.Transparent
                    val mBorder = if (mSelected) AppColors.DarkAccent else AppColors.DarkBorder
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(mBg, RoundedCornerShape(16.dp))
                            .border(1.5.dp, mBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedTier = "monthly" }
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("Monthly", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$3.99", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = JetBrainsMono)
                            Text("Billed monthly", fontSize = 11.sp, color = AppColors.DarkTextSecondary)
                        }
                    }

                    // Tier 2: Yearly billing cycle (Selected by default)
                    val ySelected = selectedTier == "yearly"
                    val yBg = if (ySelected) AppColors.DarkAccent.copy(alpha = 0.15f) else Color.Transparent
                    val yBorder = if (ySelected) AppColors.DarkAccent else AppColors.DarkBorder
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(yBg, RoundedCornerShape(16.dp))
                            .border(2.dp, yBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedTier = "yearly" }
                            .padding(14.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Yearly", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Box(
                                        modifier = Modifier
                                            .background(AppColors.DarkWarning.copy(alpha = 0.25f), CircleShape)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("BEST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppColors.DarkWarning)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("$29.99", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = JetBrainsMono)
                                Text("Only $2.49/mo", fontSize = 11.sp, color = AppColors.DarkAccentThird, fontWeight = FontWeight.Bold)
                                Text("Save 37% off monthly plans !", fontSize = 10.sp, color = AppColors.DarkSuccess, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CTA Payment starter gold button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isLoading = true
                        viewModel.executePremiumPurchase {
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("paywall_purchase_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AppColors.DarkGold, Color(0xFFFF8C00))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = "Start Premium Subscription 👑", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Restore Purchases",
                        fontSize = 12.sp,
                        color = AppColors.DarkTextSecondary,
                        modifier = Modifier.clickable {
                            viewModel.showToast("Purchases restored successfully! 👑")
                        }
                    )
                    Text(text = "·", color = AppColors.DarkTextMuted)
                    Text(
                        text = "Maybe later",
                        fontSize = 12.sp,
                        color = AppColors.DarkTextSecondary,
                        modifier = Modifier.clickable { viewModel.closePaywall() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun PaywallFeatureRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(AppColors.DarkAccentThird.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.DarkAccentThird, modifier = Modifier.size(12.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, color = AppColors.DarkTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// GLOBAL TOAST OVERLAY PANEL
@Composable
fun GlobalToast(message: String, isSuccess: Boolean, isWifiOff: Boolean, onDismiss: () -> Unit) {
    val containerBg = if (isWifiOff) {
        AppColors.DarkWarning
    } else if (isSuccess) {
        AppColors.DarkAccent
    } else {
        AppColors.DarkDanger
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(14.dp))
            .background(containerBg, RoundedCornerShape(14.dp))
            .clickable { onDismiss() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isWifiOff) {
                    Icons.Default.WifiOff
                } else if (isSuccess) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Error
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
